from fastapi import status

'''
Exception class to hold the exceptions coming from the API Service
AIServiceException descends from Exception class

AIRateLimitException descends from AIServiceException class and return 429 code
AIAuthenticationException descends from AIServiceException class and return 401 code
AIConnectionException descends from AIServiceException class and return 503 code
AIRateLimitException descends from AIServiceException class and return 504 code
'''
class AIServiceException(Exception):
    def __init__(self, message: str, status_code:int):
        super().__init__(message)
        self.status_code = status_code

class AIRateLimitException(AIServiceException):
    def __init__(self, message: str):
        super().__init__(message, status.HTTP_429_TOO_MANY_REQUESTS)


class AIAuthenticationException(AIServiceException):
    def __init__(self, message):
        super().__init__(message, status.HTTP_401_UNAUTHORIZED)


class AIConnectionException(AIServiceException):
    def __init__(self, message):
        super().__init__(message, status.HTTP_503_SERVICE_UNAVAILABLE)


class AITimeoutException(AIServiceException):
    def __init__(self, message):
            super().__init__(message, status.HTTP_504_GATEWAY_TIMEOUT)