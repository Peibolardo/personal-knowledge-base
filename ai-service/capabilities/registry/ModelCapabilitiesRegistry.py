'''
Registry of model capabilities for different LLM models.
This dictionary maps model names to their respective capabilities, 
such as context window size and maximum completion tokens.
'''
MODEL_CAPABILITIES = {
    "gpt-4o-mini": {
        "context_window_tokens": 128000,
        "max_completion_tokens": 16384
    },
    "gpt-5o-mini": {
        "context_window_tokens": 400000,
        "max_completion_tokens": 128000
    }
}