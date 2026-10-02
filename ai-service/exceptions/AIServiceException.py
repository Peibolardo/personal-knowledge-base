class AIServiceException(Exception):
    pass


class AIRateLimitException(AIServiceException):
    pass


class AIAuthenticationException(AIServiceException):
    pass


class AIConnectionException(AIServiceException):
    pass


class AITimeoutException(AIServiceException):
    pass