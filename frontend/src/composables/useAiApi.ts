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
            input: input
        }
        
        const response = await AiApiStore.sendMessage(request)
        return response?.response

    }

    return {
        sendMessageToApi
    }


}