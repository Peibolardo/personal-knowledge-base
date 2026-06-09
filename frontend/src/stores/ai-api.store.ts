import { useAxios } from '@/composables/useAxios'
import type ChatRequest from '@/interfaces/ChatRequest'
import type ChatResponse from '@/interfaces/ChatResponse'
import { defineStore } from 'pinia'

export const useAiApiStore = defineStore('ai-api', () =>{

const axiosInstance = useAxios({ baseURL: import.meta.env.VITE_AI_API_URL })

    /**
     * POST a request to the AI Api and retrieve a response from it
     * @param chatRequest DTO that contains the message to send
     * @returns ChatResponse object with the response from the AI
     */
    async function sendMessage(chatRequest: ChatRequest): Promise<ChatResponse | undefined>{
        
        try{
            console.log("Trying to send a Message to open AI")
            const response = await axiosInstance.post("/chat",
                chatRequest
            )
            console.log("Successfully retrieved the Response from the AI")
            return response.data as ChatResponse
        }
        catch(error: any){
            console.log("Could not execute the message properly")
            console.error(error)
        }
    }

    return{
        sendMessage
    }
    
})