export default interface ChatStreamDone {
    tokens_prompt: number | null
    tokens_completion: number | null
    tokens_total: number | null
}