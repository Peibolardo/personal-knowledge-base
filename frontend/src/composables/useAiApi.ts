import { useAiApiStore } from '@/stores/ai-api.store'
import { useConversationStore } from '@/stores/conversationStore'
import type ChatRequest from '@/interfaces/ChatRequest'

export function useAiApi(){

    const AiApiStore = useAiApiStore()
    const conversationStore = useConversationStore()

    /**
     * Function to send the request to the store and receive the data
     * @param input Message sent to the AI
     * @returns response sent by the AI
     */
    async function sendMessageToApi(input: string){
        
        const request: ChatRequest = {
            input: input,
            conversationId: conversationStore.activeConversationId
        }
        
        const response = await AiApiStore.sendMessage(request)
        return response?.content

    }

    /**
     * Sends a user message to the AI service and processes the response as a Server-Sent Events (SSE) stream.
     * Reads the response progressively, decodes incoming bytes into text, and parses complete SSE events.
     * Handles each event according to its type:
     * 
     * metadata: Sets the active conversation ID if no conversation is currently active.
     * content: Passes each text chunk to the onContent callback so the view can display the response progressively.
     * error: Passes the error message to the onError callback so the view can display it.
     * done: Stops processing the stream when the AI response is complete.
     * @param input The message sent by the user.
     * @param onContent Callback invoked with each content chunk received from the AI.
     * @param onError Callback invoked with the error message received from the AI.
*/
    async function sendMessageStreaming(input: string, onContent: (content: string) => void, onError: (message: string) => void) {

        const request: ChatRequest = {
            input,
            conversationId: conversationStore.activeConversationId
        }

        const response = await AiApiStore.sendMessageStream(request)

        if (!response.body) {
            throw new Error('The response does not contain a readable stream')
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder()

        let buffer = ''
        let streamFinished = false

        try {
            while (!streamFinished) {
                const { value, done: readerDone } = await reader.read()

                if (readerDone) {
                    if (!streamFinished) {
                        throw new Error('The stream ended before a completion or error event was received.')
                    }
                    break
                }

                buffer += decoder.decode(value, { stream: true })

                // A network chunk can contain partial events or multiple events.
                const events = buffer.split('\n\n')
                buffer = events.pop() ?? ''

                for (const eventFrame of events) {
                    const lines = eventFrame.split('\n')

                    const eventName = lines
                        .find(line => line.startsWith('event:'))
                        ?.slice('event:'.length)
                        .trim()

                    const data = lines
                        .filter(line => line.startsWith('data:'))
                        .map(line => line.slice('data:'.length).trimStart())
                        .join('\n')

                    if (!eventName || !data) continue

                    const payload = JSON.parse(data)

                    switch (eventName) {
                        case 'metadata':
                            if (!conversationStore.activeConversationId && payload.conversationId) {
                                conversationStore.setActiveConversationId(payload.conversationId)
                            }
                            break

                        case 'content':
                            onContent(payload.content)
                            break

                        case 'error':
                            onError(payload.message)
                            streamFinished = true
                            break

                        case 'done':
                            streamFinished = true
                            break
                    }

                    if (streamFinished) break
                }
            }
        } finally {
            
            await reader.cancel()
            reader.releaseLock()
            AiApiStore.finishMessageStreaming()
        }
    }

    function abortStreaming() {

        AiApiStore.cancelMessageStreaming()

    }
        

    return {
        sendMessageToApi,
        sendMessageStreaming,
        abortStreaming
    }


}