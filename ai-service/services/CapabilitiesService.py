from config.OpenAIConfig import OpenAIConfig
from capabilities.ModelCapabilities import ModelCapabilities
from capabilities.registry import ModelCapabilitiesRegistry
from capabilities.OperationPromptCapabilities import OperationPromptCapabilities
from capabilities.PromptCapabilities import PromptCapabilities
from services.TokenizerService import TokenizerService
from pathlib import Path

class CapabilitiesService:

    PROMPTS_DIR = Path(__file__).resolve().parent.parent / "prompts"

    # Read the summary system prompt
    SUMMARY_SYSTEM_PROMPT = (
        PROMPTS_DIR / "summarization" / "summary_system_prompt.txt"
    ).read_text(encoding="utf-8")

    # Read the summary context prompt
    SUMMARY_CONTEXT_PROMPT = (
        PROMPTS_DIR / "chat" / "summary_context.txt"
    ).read_text(encoding="utf-8")

    def __init__(self):
       
        self.config = OpenAIConfig()
        self.model_capabilities_registry = ModelCapabilitiesRegistry()
        self.tokenizer = TokenizerService(self.config.model)
        self.summary_system_prompt = self.SUMMARY_SYSTEM_PROMPT
        self.summary_context_prompt = self.SUMMARY_CONTEXT_PROMPT

    def get_capabilities(self) -> ModelCapabilities:
        
        # Logic to retrieve the capabilities of the AI service
       
        chat_prompt_capabilities = self.get_chat_prompt_capabilities()

        summarization_prompt_capabilities = self.get_summarization_prompt_capabilities()

        prompts = PromptCapabilities(
            chat=chat_prompt_capabilities,
            summarization=summarization_prompt_capabilities
        )

        model_capabilities = ModelCapabilities(

            model=self.config.model,
            context_window_tokens=self.model_capabilities_registry.get_context_window_tokens(self.config.model),
            model_max_completion_tokens=self.model_capabilities_registry.get_max_completion_tokens(self.config.model),
            prompts=prompts

        )

        return model_capabilities


    def get_chat_prompt_capabilities(self) -> OperationPromptCapabilities:

        system_prompt_tokens = self.tokenizer.count_tokens(self.config.system_prompt)

        chat_operation_capabilities = OperationPromptCapabilities(

            system_prompt_tokens = system_prompt_tokens,
            developer_prompt_tokens = self.tokenizer.count_tokens(self.summary_context_prompt)

        )

        return chat_operation_capabilities

    def get_summarization_prompt_capabilities(self) -> OperationPromptCapabilities:

        system_prompt_tokens = self.tokenizer.count_tokens(self.summary_system_prompt)

        summarization_operation_capabilities = OperationPromptCapabilities(

            system_prompt_tokens = system_prompt_tokens,

        )

        return summarization_operation_capabilities
