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

    return {
        sendMessageToApi,
    }


}