from dotenv import load_dotenv
load_dotenv()  # ← reads .env and sets the environment variables
from fastapi import FastAPI

import uvicorn

if __name__ == "__main__":
    uvicorn.run("routers.ChatRouter:app", host="0.0.0.0", port=8080, reload=True)

app = FastAPI()
