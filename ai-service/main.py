import uvicorn
from dotenv import load_dotenv
from fastapi import FastAPI

from exceptions.AIServiceException import AIServiceException
from exceptions.AIExceptionHandler import handle_ai_exception

load_dotenv()

# Import router
from routers.ChatRouter import router as chat_router

# Singleton instance of FastAPI in the hole project
app = FastAPI()

# Connect router to the main app
app.include_router(chat_router)

# Add exception handler
app.add_exception_handler(AIServiceException, handle_ai_exception)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8080, reload=True)

