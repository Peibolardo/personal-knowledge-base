from fastapi import FastAPI

app = FastAPI()


@app.post("/chat")
def sendMessageToChat():
    