import { useAxios } from '@/composables/useAxios'
import { useConversationStore } from '@/stores/conversationStore'
import type ChatRequest from '@/interfaces/ChatRequest'
import type ChatResponse from '@/interfaces/ChatResponse'
import { defineStore } from 'pinia'

export const useAiApiStore = defineStore('ai-api', () =>{

const axiosInstance = useAxios({ baseURL: import.meta.env.VITE_AI_API_URL })

    const conversationStore = useConversationStore()

    // AbortController to cancel the streaming request if needed 
    let abortController: AbortController | null = null

    /**
     * POST a request to the AI Api and retrieve a response from it
     * @param chatRequest DTO that contains the message to send
     * @returns ChatResponse object with the response from the AI
     */
    async function sendMessage(chatRequest: ChatRequest): Promise<ChatResponse | undefined>{
        
        try{
            
            console.log("Trying to send a Message to open AI")
            const response = await axiosInstance.post<ChatResponse>("/chat",
                chatRequest
            )
            
            console.log("Successfully retrieved the Response from the AI")

            // Saves the conversationId in case of new Conversation
            if (!conversationStore.activeConversationId) {
                conversationStore.activeConversationId = response.data.conversationId
            }
            return response.data as ChatResponse
        }
        catch(error: any){
            console.log("Could not execute the message properly")
            console.error(error)
        }
    }

    /**
     * POST a request to the AI Api and retrieve a Stream response from it
     * @param chatRequest DTO that contains the message to send
     * @returns Promise<Response> With the event stream response from the AI
     */
    async function sendMessageStream(chatRequest: ChatRequest): Promise<Response> {

        console.log("Trying to send a Message to open AI using streaming")

        const controller = new AbortController()
        abortController = controller

        try{
        const response = await fetch(`${import.meta.env.VITE_AI_API_URL}/chat/stream`, 
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'text/event-stream'
                },
                body: JSON.stringify(chatRequest),
                signal: abortController.signal
            }
        )

        if (!response.ok) {
            throw new Error(`Streaming request failed: HTTP ${response.status}`)
        }

        return response

        }
        catch(error: any){

            // The request may fail before the response headers arrive.
            if (abortController === controller) {
                abortController = null
            }

            throw error

        }
    }
    function cancelMessageStreaming() {
        abortController?.abort();
    }

    function finishMessageStreaming() {
        abortController = null
    }
       

    return{
        sendMessage,
        sendMessageStream,
        cancelMessageStreaming,
        finishMessageStreaming
    }
    
})