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

    /**
     * Get all the messages belonging to a Conversation
     * @param conversationId The unique identifier of the conversation
     * @returns All the messages from a conversationId
     */
    async function getAllMessages(conversationId: string){
        return conversationStore.getAllMessagesFromConversation(conversationId)
    }

    /**
     * Sets the given conversation as the active one
     * @param conversationId The unique identifier of the conversation selected
     */
    function selectConversation(conversationId: string){
        conversationStore.selectConversation(conversationId)
    }

    /**
     * Function to reset the active conversation Identifier
     * Sets to null activeConversationId
     */
    function resetConversation(){
        conversationStore.resetConversation()
    }


    return {
        getAllConversations,
        getAllMessages,
        resetConversation,
        selectConversation
    }

}