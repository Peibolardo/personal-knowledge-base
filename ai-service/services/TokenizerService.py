import tiktoken
from openai import OpenAI

'''
## Important to know about this service:
Tiktoken is a good way to estimate how many tokens a text is going to cost.
However the true cost of these calls are measured by openAITokenizer method because it calls directly the official API.
So we are working with this service to count the estimated tokens of the messages not the thruthful ones.
'''
class TokenizerService:

    '''
    Contructor to set the encode model
    '''
    def __init__(self, model:str):
        self.encoding = tiktoken.encoding_for_model(model)
        self.client = OpenAI()
        self.model = model

    '''
    Function to count the tokens by the text passed
    '''
    def count_tokens(self, text:str):
        return len(self.encoding.encode(text))

    '''
    Function to retrieve all the tokensIds from a text
    '''
    def encodeText(self, text:str):
        return self.encoding.encode(text)

    '''
    Function to decode the tokensIds to text
    '''
    def decodeText(self, tokens: list[int]):
        return self.encoding.decode(tokens)

    '''
    Function to retrieve the tokens used in a call to AI API
    '''
    def openAITokenizer(self, text:str):

        response = self.client.responses.input_tokens.count(
                            model = self.model,
                            input=text
                        )
        return response.input_tokens

    '''
    Function to split text into token-based chunks while preserving an overlap between consecutive chunks and balancing the final two chunks when possible.

    chunk_size: Maximum token count for a chunk, including its repeated overlap.
    overlap: Number of tokens repeated from the previous chunk at the start of the next one.

    With chunk_size=300 and overlap=30, each full step contributes 270 new tokens.
    The final two chunks are resized to equal or nearly equal token counts while keeping
    the configured overlap. If their combined size is odd, their sizes differ by one token.
    '''
    def tokenizeAndChunkingText(self, text: str, chunk_size: int, overlap: int):

        textTokens = self.encodeText(text)

        if not textTokens:
            return []

        if overlap < 0 or overlap >= chunk_size:
            raise ValueError("Overlap must be between 0 and chunk_size")

        chunks = []
        start = 0

        # Record token ranges so all chunks refer to the original token sequence.
        # Each chunk contains at most chunk_size tokens, including its repeated overlap.
        while start < len(textTokens):

            end = min(start + chunk_size, len(textTokens))

            chunks.append({
                "start": start,
                "end": end
            })

            if end == len(textTokens):
                break

            # Move forward by only the non-overlapping portion of this chunk.
            start = end - overlap

        # Balance the last two chunks
        if len(chunks) >= 2:

            firstStart = chunks[-2]["start"]
            lastEnd = chunks[-1]["end"]

            # Add the shared overlap to the union span to get the combined size of both chunks.
            totalTokens = lastEnd - firstStart + overlap

            # Integer division can leave the final two chunks one token apart.
            firstChunkSize = totalTokens // 2

            firstEnd = firstStart + firstChunkSize
            # Keep exactly the configured overlap between the balanced pair.
            secondStart = firstEnd - overlap

            chunks[-2] = {
                "start": firstStart,
                "end": firstEnd
            }

            chunks[-1] = {
                "start": secondStart,
                "end": lastEnd
            }

        # Decode tokens and build the final chunks
        result = []

        for index, chunk in enumerate(chunks):

            actualTokens = textTokens[chunk["start"]:chunk["end"]]

            result.append({
                "content": self.decodeText(actualTokens),
                "chunk_index": index,
                "token_count": len(actualTokens)
            })

        return result
