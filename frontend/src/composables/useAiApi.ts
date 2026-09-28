import { useAiApiStore } from '@/stores/ai-api.store'
import type ChatRequest from '@/interfaces/ChatRequest'

export function useAiApi(){

    const AiApiStore = useAiApiStore()

    /**
     * Function to send the request to the store and receive the data
     * @param input Message sent to the AI
     * @returns response sent by the AI
     */
    async function sendMessageToApi(input: string){
        
        const request: ChatRequest = {
            input: input,
            conversationId: AiApiStore.conversationId
        }
        
        const response = await AiApiStore.sendMessage(request)
        return response?.response

    }


    /**
     * Function to reset conversationId
     * Sets to null the ref conversationId in ai-api.store
     */
    function resetConversation(){
        AiApiStore.conversationId = null
    }

    return {
        sendMessageToApi,
        resetConversation
    }


}