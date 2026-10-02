import { defineStore } from "pinia";
import { useAxios } from '@/composables/useAxios'
import type Conversation from "@/interfaces/Conversation";
import type { Message } from "@/interfaces/Message";
import { ref } from 'vue'

export const useConversationStore = defineStore('conversationStore', () =>{

    const axiosInstance = useAxios({ baseURL: import.meta.env.VITE_AI_API_URL })
    const activeConversationId = ref<string | null>(null)

    /**
     * Fetch all the conversations from the database
     * @returns List of all Conversations
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

    /**
     * Fetch all the messages of a conversation
     * @returns List of all messages belonging to a conversationId
     */
    async function getAllMessagesFromConversation(conversationId: string){

        try{

            console.log("Trying to fetch all messages belonging to a conversation");

            const response = await axiosInstance.get<Message[]>(`/conversations/${conversationId}/messages`)

            console.log("Successfully retrieved all messages belonging to a conversation")

            return response.data as Message[]

        }
        catch(error:any){

            console.log("Could not execute the message properly")
            console.error(error)

        }
    }

    /**
     * Reset the activeConversationId value to null
     */
    function resetConversation(){
        activeConversationId.value = null
    }

    /**
     * Sets the active conversation to the one selected by the user
     * @param conversationId The unique identifier of the conversation selected
     */
    function selectConversation(conversationId: string) {
        activeConversationId.value = conversationId
    }

    return {
        getAllConversations,
        getAllMessagesFromConversation,
        resetConversation,
        selectConversation,
        activeConversationId
    }

})