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
[ Vue 3 Frontend ]
        ↕ HTTP (Axios)
[ Spring Boot Backend ]     ←── Main API, business logic, database
        ↕ HTTP (WebClient)
[ Python FastAPI Service ]  ←── AI gateway, communicates with OpenAI
        ↕
[ OpenAI API ]

[ PostgreSQL ]              ←── Used by Spring Boot
```

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
| Java 25 | Language |
| Spring Boot 3.4 | Main framework |
| Spring Data JPA + Hibernate | Database ORM |
| Spring WebClient | Inter-service HTTP calls |
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

| Phase | Name | Key Concepts |
|---|---|---|
| **1** | Foundation & First AI Call | REST APIs, inter-service communication, OpenAI basics |
| **2** | Context & Conversation Memory | PostgreSQL, session management, context windows |
| **3** | Notes & Personal Knowledge | Full CRUD, REST design, frontend-backend data flow |
| **4** | RAG — Retrieval Augmented Generation | Embeddings, vector search, pgvector, document ingestion |
| **5** | Auth & Multi-user | JWT, security, scoped data per user |
| **6** | Deploy | Docker Compose, cloud deployment |
| **7** | AI Agents | Autonomous AI actions, tool use, agentic patterns |

---

## 🚀 Getting Started

### Prerequisites
- Java 25+
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
# Runs on http://localhost:8080
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
uvicorn main:app --reload --port 8000
# Runs on http://localhost:8000
```

---

## 🔑 Environment Variables

### Backend — `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pkb
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### AI Service — `.env`
```
OPENAI_API_KEY=your-openai-api-key
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

**Phase 2 — Context & Conversation Memory** *(in progress)*

- ✅ Phase 1 complete — Vue chat UI, Spring Boot API, Python microservice and OpenAI integration all connected and working
- 🔄 Phase 2 in progress — implementing conversation history and context management so the AI maintains memory across messages
