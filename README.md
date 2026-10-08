# 🧠 Personal Knowledge Base

A personal AI-powered application designed to chat with artificial intelligence, and work alongside it to organise, optimise and store information learned over time.

Built as a self-driven learning project with the goal of understanding how to integrate AI into real applications, applying clean architecture patterns across a multi-service stack, and progressively introducing advanced AI concepts.

---

## 🎯 Project Goals

- Develop a full-stack application from scratch as a solo developer
- Learn Python and its role in modern AI-powered backends
- Understand and apply key AI concepts: context management, RAG, embeddings and agents
- Build a portfolio-worthy project demonstrating microservice architecture with AI integration

---

## 🏗️ Architecture

The project is divided into three independent layers that communicate over HTTP:

```
[ Vue 3 Frontend ]          :3000
        ↕ HTTP (Axios)
[ Spring Boot Backend ]     :8081  ←── Main API, business logic, database
        ↕ HTTP (WebClient)
[ Python FastAPI Service ]  :8080  ←── AI gateway, communicates with OpenAI
        ↕
[ OpenAI API ]
 
[ PostgreSQL ]              :5432  ←── Used by Spring Boot
```

> **Planned (v2.3):** responses will be streamed. Python streams chunks from OpenAI to Spring Boot, which relays them to the frontend over SSE while accumulating the full message to persist it once the stream ends. The frontend never talks to the AI service directly.
---

## 🛠️ Tech Stack

### Frontend
| Technology | Purpose |
|---|---|
| Vue 3 | UI Framework |
| TypeScript | Typed JavaScript |
| Vuetify 3 | Material Design component library |
| Pinia | State management |
| Vue Router | Client-side routing |
| Axios | HTTP client |
| Vite | Build tool |
| pnpm | Package manager |

### Backend (Main API)
| Technology | Purpose |
|---|---|
| Java 21 | Language |
| Spring Boot 3.4 | Main framework |
| Spring Data JPA + Hibernate | Database ORM |
| Spring WebClient | Inter-service HTTP calls |
| MapStruct + Lombok | DTO/entity mapping and boilerplate reduction |
| PostgreSQL 16 | Relational database |
| Maven | Dependency management |

### AI Microservice
| Technology | Purpose |
|---|---|
| Python 3.14 | Language |
| FastAPI | Web framework |
| Uvicorn | ASGI server |
| Pydantic | Data validation and DTOs |
| OpenAI SDK | Communication with OpenAI API |
| python-dotenv | Environment variable management |

### Infrastructure
| Technology | Purpose |
|---|---|
| Docker + Docker Compose | Containerisation (Phase 6) |
| pgAdmin 4 | PostgreSQL GUI |
| Git + GitHub | Version control |

---

## 📋 Project Phases

| **1** | Foundation & First AI Call | REST APIs, inter-service communication, OpenAI basics |

| Version | Phase | Name | Key Concepts |
|---|---|---|---|
| **v1.0** | 1 | Foundation & First AI Call | REST APIs, inter-service communication, OpenAI basics |
| **v2.1** | 2.1 | Context Window | Conversation history, context windows, tokens, persistence ← *current* |
| **v2.2** | 2.2 | LLM Engineering | System prompts, structured output, error handling, retries, token tracking ← *current* |
| **v2.3** | 2.3 | Streaming & Delivery | OpenAI streaming, FastAPI StreamingResponse, SSE, Spring WebClient + Flux, Vue streaming, persistence on completion |
| **v2.4** | 2.4 | Context Engineering | Token budget, conversation summarization, dynamic context |
| **v3.0** | 3 | Knowledge Base | Full notes CRUD, document import, frontend for notes |
| **v4.0** | 4 | Embeddings + Vector DB | pgvector, OpenAI embeddings API, chunking, tiktoken |
| **v5.0** | 5 | RAG | Built from scratch — query processing, vector search, relevant chunks injected as context |
| **v5.5** | 5.5 | Evaluation | Recall, faithfulness, relevancy metrics across RAG versions |
| **v6.0** | 6 | Auth + AI Security | JWT, prompt injection awareness, scoped data per user |
| **v7.0** | 7 | Tools | Function calling, AI executes actions (search_knowledge, create_note, search_web...) |
| **v8.0** | 8 | Agents | ReAct loop, tool chaining, autonomous AI decisions |
| **v9.0** | 9 | Advanced Agents | LangGraph, MCP, multi-agent, human-in-the-loop, workflows |
| **v10.0** | 10 | Production / LLMOps | Docker, deployment, observability, tracing, cost monitoring, CI/CD |

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Node.js 24+ and pnpm
- Python 3.14+
- PostgreSQL 16+

### 1. Clone the repository
```bash
git clone https://github.com/your-username/personal-knowledge-base.git
cd personal-knowledge-base
```

### 2. Backend (Spring Boot)
```bash
cd backend
# Configure your database in src/main/resources/application.properties
./mvnw spring-boot:run
# Runs on http://localhost:8081
```

### 3. Frontend (Vue 3)
```bash
cd frontend
pnpm install
pnpm run dev
# Runs on http://localhost:3000
```

### 4. AI Microservice (Python)
```bash
cd ai-service
python -m venv venv
source venv/Scripts/activate  # Windows
pip install -r requirements.txt
# Add your OpenAI API key to .env: OPENAI_API_KEY=your-key-here
uvicorn main:app --reload --port 8080
# Runs on http://localhost:8080
```

---

## 🔑 Environment Variables

### Backend — `application.properties`
```properties
server.port=8081
 
spring.datasource.url=jdbc:postgresql://localhost:5432/pkb
spring.datasource.username=postgres
spring.datasource.password=your_password
 
# URL of the Python AI service
python.service.url.api=http://localhost:8080
```

### AI Service — `.env`
```
OPENAI_API_KEY=your-openai-api-key
OPENAI_MODEL=gpt-4o-mini
# Generation settings (temperature, max tokens, top_p, ...) go here too
```

### Frontend — `.env`
```
VITE_AI_API_URL=http://localhost:8081/api/v1
```

---

## 📁 Project Structure

```
personal-knowledge-base/
├── frontend/        ← Vue 3 + Vuetify app
├── backend/         ← Spring Boot main API
├── ai-service/      ← Python FastAPI AI microservice
│   ├── routers/     ← Endpoint definitions
│   ├── services/    ← Business logic + OpenAI calls
│   ├── DTOs/        ← Request and response models
│   └── main.py      ← App entry point
└── README.md
```

---

## 🔄 Current Status

**v2.3 — Streaming & Delivery** *(in progress)*
 
- ✅ v1.0 complete — Vue chat UI, Spring Boot API, Python microservice and OpenAI integration all connected and working
- ✅ v2.1 complete — conversation history persisted, last 9 messages + current sent to the model, token tracking, conversation sidebar
- ✅ v2.2 complete — system prompts, structured output, error handling, retries, token tracking
- 🔄 v2.3 in progress — Streaming & Delivery (separate branch, merged after v2.2 is tagged on `main`)
