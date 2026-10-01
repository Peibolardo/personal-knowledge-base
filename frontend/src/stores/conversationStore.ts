import { defineStore } from "pinia";
import { useAxios } from '@/composables/useAxios'
import type Conversation from "@/interfaces/Conversation";

export const useConversationStore = defineStore('conversationStore', () =>{

    const axiosInstance = useAxios({ baseURL: import.meta.env.VITE_AI_API_URL })
    
    /**
     * Fetch all the conversations from the database
     * @returns
     */
    async function getAllConversations(){

        try{

            console.log("Trying to fetch all conversations");

            const response = await axiosInstance.get<Conversation[]>("/conversations")

            console.log("Successfully retrieved all the conversations")

            return response.data as Conversation[]

        }
        catch(error: any){

            console.log("Could not execute the message properly")
            console.error(error)

        }

    }

    return {
        getAllConversations
    }

})