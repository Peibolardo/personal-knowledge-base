import { useConversationStore } from '@/stores/conversationStore'

export function useConversation(){

    const conversationStore = useConversationStore()

    /**
     * Function to get all conversation objects
     * @returns List with all conversations
     */
    async function getAllConversations(){
        return conversationStore.getAllConversations()
    }

    return {
        getAllConversations
    }

}