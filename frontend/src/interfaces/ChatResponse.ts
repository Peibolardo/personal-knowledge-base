export default interface ChatResponse {

    content: string,
    conversationId: string,
    tokens_prompt: number,
    tokens_completion: number,
    tokens_total: number,
    model_used: string
    
}
