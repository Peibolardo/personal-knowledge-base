import uvicorn
from dotenv import load_dotenv
from fastapi import FastAPI

load_dotenv()

# Import router
from routers.ChatRouter import router as chat_router

# Singleton instance of FastAPI in the hole project
app = FastAPI()

# Connect router to the main app
app.include_router(chat_router)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8080, reload=True)
