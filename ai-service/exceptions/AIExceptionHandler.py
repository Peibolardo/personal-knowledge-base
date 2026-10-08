from fastapi import Request
from fastapi.responses import JSONResponse

from exceptions.AIServiceException import AIServiceException

'''
Function to act as a AIExceptionsHandler
@param request: Param expected by fastAPI add_exception_handler method
@param exc: Class that inherits from AIServiceException
@returns JSON with the status_code and content.error with the message

str(exc) refers to the message added to a exception when created
'''
async def handle_ai_exception(
    request: Request,
    exc: AIServiceException
):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": str(exc)
        }
    )