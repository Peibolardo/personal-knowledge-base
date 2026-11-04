-- Run once against the target PostgreSQL database before starting the backend.
-- pgvector must be installed on the PostgreSQL server.
CREATE EXTENSION IF NOT EXISTS vector;
