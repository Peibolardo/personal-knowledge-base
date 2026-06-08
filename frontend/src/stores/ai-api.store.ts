import type ChatRequest from '@/interfaces/ChatRequest'
import { defineStore } from 'pinia'
import { useAxios } from '@/composables/useAxios'
const axiosInstance = useAxios({ baseURL: import.meta.env.VITE_AI_API_URL })

export const useAiApiStore = defineStore('ai-api', () =>{

    async function sendMessage(chatRequest: ChatRequest){
        
        try{
            console.log("Trying to send a Message to open AI")
            const response = await axiosInstance.post("/chat",
                chatRequest
            )
            console.log("Successfully retrieved the Response from the AI")
            return response.data
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