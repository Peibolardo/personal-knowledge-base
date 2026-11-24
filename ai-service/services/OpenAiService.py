import os
import time
import logging
import json
from openai import AsyncOpenAI
from DTOs.ChatResponse import ChatResponse
from DTOs.Message import Message
from exceptions.AIServiceException import AIServiceException
from typing import List
from infraestructure.TokenizerService import TokenizerService
from infraestructure.OpenAIRetryService import OpenAIRetryService
from DTOs.ChatContext import ChatContext
from pathlib import Path
from config.OpenAIConfig import OpenAIConfig

class OpenAiService:

    '''
    Directory for storing prompts
    __file__ identifies the Python file where this code runs.
    resolve() method returns the absolute path of the file.
    parent.parent navigates two levels up in the directory structure.
    The final path is constructed by appending "prompts" to the parent directory.
    '''
    PROMPTS_DIR = Path(__file__).resolve().parent.parent / "prompts"

    # Read the summary system prompt from a file
    SUMMARY_SYSTEM_PROMPT = (
        PROMPTS_DIR / "summarization" / "summary_system_prompt.txt"
    ).read_text(encoding="utf-8")

    #Constructor to initialize the class
    def __init__(self):
        self.client = AsyncOpenAI(max_retries=0)
        self.logger = logging.getLogger("uvicorn.error")

        #Set the model and system prompt from the environment configuration
        self.config = OpenAIConfig()
        self.model = self.config.model
        self.system_prompt = self.config.system_prompt

        #TokenizerService with the model established in the env file
        self.tokenizer = TokenizerService(self.model)

        #Service to execute the OpenAI API calls with retry logic
        self.retry_service = OpenAIRetryService()

        self.summary_system_prompt = self.SUMMARY_SYSTEM_PROMPT

    """
    Function that receives a list of Messages with the context and send it to the OpenAI API
    Receives a JSON with the response and different fields.
    Sends to the API JSON structured { model: self.model, messages : { role: system, content: self.system_prompt}, { role: user, ...}, {role: assistant. ...}, { role:user, ...}, ... }
    First with the System Prompt to define how the model should behave.

    Throws different Exceptions based on the  issue, depending on the cause of it, they can be retryable to a max of three attemps before interrupting the flow.
    """
    async def send_message_to_api(self, context: List[Message]) -> ChatResponse:

        #Set up maximum attempts to retryable routine
        max_attempts = 3
        #Set up seconds to sleep in case of retry routine
        delay = 1

        for attempt in range(1, max_attempts + 1):
                
            self.logger.info("Attempting to send a message to the AI")
            self.logger.info("AI API request started | model=%s | messages=%d", self.model, len(context))

            start_time = time.perf_counter()

            messages = []

            #Check if the system prompt is null
            if not self.system_prompt:
                raise ValueError("SYSTEM_PROMPT environment variable is not configured")

            messages.append({
                "role": "system",
                "content": self.system_prompt
            })

            for msg in context:
                messages.append({
                    "role": msg.role.value,
                    "content": msg.content
                })
            self.logger.info("SYSTEM PROMPT: %s", self.system_prompt)
            
            message_sending_operation = lambda: self.client.chat.completions.create(
                        model = self.model,
                        messages = messages
                    )
            
            response = await self.retry_service.execute(message_sending_operation)

            ## Tokens used by the request message
            tokens_input_message = self.tokenizer.count_tokens(context[-1].content)

            duration_ms = (time.perf_counter() - start_time) * 1000

            self.logger.info(
                "OpenAI request completed | model=%s | messages=%d | "
                "prompt_tokens=%d | completion_tokens=%d | total_tokens=%d | "
                "duration_ms=%d",
                response.model,
                len(context),
                response.usage.prompt_tokens,
                response.usage.completion_tokens,
                response.usage.total_tokens,
                duration_ms
            )

            return ChatResponse(
                content=response.choices[0].message.content,
                tokens_prompt=response.usage.prompt_tokens,
                tokens_completion=response.usage.completion_tokens,
                tokens_total=response.usage.total_tokens,
                tokens_input_message = tokens_input_message,
                model_used=response.model
            )


    """
    Function that receives a ChatContext DTO with the current summary of the conversation and the new messages to
    """
    async def summarize_conversation(self, chatContext: ChatContext) -> str:

        self.logger.info("Attempting to summarize the conversation")

        newMessages = []
        
        #Check if the system prompt is null
        if not self.summary_system_prompt:
            raise ValueError("SYSTEM_PROMPT environment variable is not configured")

        # Create a list with the new messages to summarize
        for msg in chatContext.messageDTOList:
            newMessages.append({
                "role": msg.role.value,
                "content": msg.content
        })

        # Create a JSON with the current summary and the new messages to summarize
        summary_input = {
            "current_summary": chatContext.currentSummary,
            "new_messages": newMessages
        }

        # Create a list with the system prompt and the user prompt with the current summary and the new messages to summarize
        messages = [
            {
                "role": "system",
                "content": self.summary_system_prompt
            },
            {
                "role": "user",
                "content": json.dumps(
                    summary_input,
                    ensure_ascii=False
                )
            }
        ]

        self.logger.info("Summarizing conversation with %d new messages", len(newMessages))

        # Send the request to the OpenAI API with the model, the system prompt and the user prompt with the current summary and the new messages to summarize
        summarization_operation = lambda: self.client.chat.completions.create(
                    model = self.model,
                    messages = messages,
                    max_completion_tokens = chatContext.maxCompletionTokens
                )
        
        response = await self.retry_service.execute(summarization_operation)

        # Get the updated summary from the response
        updated_summary = response.choices[0].message.content

        # Check if the updated summary is empty or null, if so, raise an exception
        if not updated_summary or not updated_summary.strip():
            raise AIServiceException(
                "AI service returned an empty summary"
            )

        self.logger.info("Successfully summarized the conversation")
        
        return updated_summary.strip()



