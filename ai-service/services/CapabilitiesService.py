import logging
from pathlib import Path

from capabilities.ChatModelCapabilities import ChatModelCapabilities
from capabilities.OperationPromptCapabilities import OperationPromptCapabilities
from capabilities.PromptCapabilities import PromptCapabilities
from capabilities.registry.ChatModelCapabilitiesRegistry import ChatModelCapabilitiesRegistry
from capabilities.EmbeddingsCapabilities import EmbeddingsCapabilities
from capabilities.registry.EmbeddingCapabilitiesRegistry import EmbeddingCapabilitiesRegistry
from config.EmbeddingsConfig import EmbeddingsConfig
from config.OpenAIConfig import OpenAIConfig
from exceptions.AIServiceException import AIServiceException
from infraestructure.TokenizerService import TokenizerService


'''
Service to assemble the configured model limits and static prompt token counts.
The prompt counts are built once so the capabilities endpoint can reuse the same result for each request.
'''
class CapabilitiesService:

    PROMPTS_DIR = Path(__file__).resolve().parent.parent / "prompts"
    LOGGER = logging.getLogger("uvicorn.error")

    '''
    Initializes the service configuration, validates the selected model, loads required prompts, and builds capabilities.

    config: Optional OpenAI configuration; the environment-backed configuration is used by default.
    model_capabilities_registry: Optional registry used to resolve the configured model's token limits.
    tokenizer: Optional tokenizer for counting prompt tokens with the configured model encoding.
    '''
    def __init__(self):

    
        self.config = OpenAIConfig()
        self.model_capabilities_registry = ChatModelCapabilitiesRegistry()

        self.embedding_capabilities_registry = EmbeddingCapabilitiesRegistry()
        self.embeddings_config = EmbeddingsConfig()

        try:
            self.model_limits = self.model_capabilities_registry.get_model_capabilities(self.config.model)
        except ValueError as error:
            raise AIServiceException(str(error)) from error

        if not self.config.system_prompt or not self.config.system_prompt.strip():
            raise AIServiceException("The SYSTEM_PROMPT environment variable cannot be blank")

        self.summary_system_prompt = self._read_prompt_file(
            self.PROMPTS_DIR / "summarization" / "summary_system_prompt.txt"
        )
        self.summary_context_prompt = self._read_prompt_file(
            self.PROMPTS_DIR / "chat" / "summary_context.txt"
        )

        self.tokenizer = TokenizerService(self.config.model)
        self._capabilities = self._build_capabilities()



    '''
    Returns the configured model name, its registered context and completion limits, and cached prompt counts.
    '''
    def get_capabilities(self) -> ChatModelCapabilities:

        return ChatModelCapabilities(
            model=self.config.model,
            context_window_tokens=self.model_limits["context_window_tokens"],
            model_max_completion_tokens=self.model_limits["max_completion_tokens"],
            prompts=self._capabilities,
        )


    '''
    Returns the configured embedding model name and its registered embedding dimensions.
    '''
    def get_embedding_capabilities(self) -> EmbeddingsCapabilities:

        # Retrieve the embedding model from the embeddings configuration, raising an exception if it cannot be retrieved
        try:    
            embedding_model = self.embeddings_config.model
        except ValueError as error:
            raise AIServiceException("Cannot retrieve the embedding model from the environment configuration") from error

        return EmbeddingsCapabilities(

            model=embedding_model,
            dimensions=self.embedding_capabilities_registry.get_embedding_capabilities(embedding_model)["dimensions"],

        )

    '''
    Counts the configured chat instructions and the developer prompt used when a conversation summary is present.
    Returns: Prompt token counts in the OperationPromptCapabilities response structure.
    '''
    def get_chat_prompt_capabilities(self) -> OperationPromptCapabilities:

        developer_prompt_tokens = self.tokenizer.count_tokens(self.summary_context_prompt) + self.tokenizer.count_tokens(self.config.system_prompt)

        return OperationPromptCapabilities(
            system_prompt_tokens=0,
            developer_prompt_tokens=developer_prompt_tokens,
        )

    '''
    Counts the static summarization system prompt.
    The summary and message payload is sent as a user message and varies per request, so it is not counted here.
    Returns: Static prompt token counts in the OperationPromptCapabilities response structure.
    '''
    def get_summarization_prompt_capabilities(self) -> OperationPromptCapabilities:

        system_prompt_tokens = self.tokenizer.count_tokens(self.summary_system_prompt)

        return OperationPromptCapabilities(
            system_prompt_tokens=system_prompt_tokens,
            developer_prompt_tokens=0,
        )

    '''
    Builds the prompt capability objects for chat and summarization.
    Returns: PromptCapabilities containing the static prompt counts for both operations.
    '''
    def _build_capabilities(self) -> PromptCapabilities:

        chat_prompt_capabilities = self.get_chat_prompt_capabilities()
        
        summarization_prompt_capabilities = self.get_summarization_prompt_capabilities()

        return PromptCapabilities(
            chat=chat_prompt_capabilities,
            summarization=summarization_prompt_capabilities,
        )

    '''
    Reads and validates a required UTF-8 prompt file.

    prompt_path: Path to the prompt file relative to the AI service prompt directory.
    Returns: Prompt text used to count static prompt tokens.
    Raises: AIServiceException if the file cannot be read or contains only whitespace.
    '''
    def _read_prompt_file(self, prompt_path: Path) -> str:

        try:
            prompt = prompt_path.read_text(encoding="utf-8")
        except OSError as error:
            self.LOGGER.exception("Could not read prompt file %s", prompt_path)
            raise AIServiceException(f"Could not read required prompt file '{prompt_path.name}'") from error

        if not prompt.strip():
            raise AIServiceException(f"Required prompt file '{prompt_path.name}' is empty")

        return prompt
