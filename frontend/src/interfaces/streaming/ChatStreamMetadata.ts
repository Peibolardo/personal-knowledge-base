export default interface ChatStreamMetadata {
    conversationId: string | null
    tokens_input_message: number | null
    model: string
}