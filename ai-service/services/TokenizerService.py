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
        return [self.encoding.decode([token]) for token in tokens]

    '''
    Function to retrieve the tokens used in a call to AI API
    '''
    def openAITokenizer(self, text:str):

        response = self.client.responses.input_tokens.count(
                            model = self.model,
                            input=text
                        )
        return response.input_tokens