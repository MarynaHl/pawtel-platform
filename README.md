# Pawtel Platform — Auth API + Data API (Docker Compose)

Two small Spring Boot services with a Postgres database, wired together via docker-compose.  
This meets the WinWin Travel backend mini test requirements.

---

## Architecture

- auth-api (Service A)
  - Spring Boot: Web, Security, JPA
  - Endpoints:
    - POST /api/auth/register – register user (email + password, BCrypt)
    - POST /api/auth/login – login, returns JWT
    - POST /api/process – protected via JWT; calls data-api and stores a log row
  - Persists to Postgres: tables `users` and `processing_log`

- data-api (Service B)
  - Spring Boot: Web
  - Endpoint:
    - POST /api/transform – transforms text (reverse + uppercase)
  - Accepts requests only when header `X-Internal-Token: <secret>` matches env

- postgres
  - DB name: `pawtel`, user: `pawtel`, password: `pawtel`

Both services run in one Docker network; auth-api reaches data-api at `http://data-api:8081`.

---

## Quick Start

From the repository root:

    # 1) (Optional) Build jars locally for faster docker builds
    mvn -f auth-api/pom.xml clean package -DskipTests
    mvn -f data-api/pom.xml clean package -DskipTests

    # 2) Start everything
    docker compose up -d --build

    # 3) Check containers
    docker compose ps

Expected ports:
- auth-api → http://localhost:8080  
- data-api → http://localhost:8081  
- Postgres → internal only (no host port binding required)

---

## How to Test

1) Register a user

    curl -i -X POST http://localhost:8080/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{"email":"a@a.com","password":"pass"}'

   Expected: HTTP/1.1 201 and `{"message":"User registered successfully"}`  
   (If user already exists → 409 and `{"message":"User already exists"}`)

2) Login and capture JWT

    TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email":"a@a.com","password":"pass"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
    echo "$TOKEN"

3) Call protected /api/process

    curl -i -X POST http://localhost:8080/api/process \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"text":"hello world"}'

   Expected JSON:

    {"result":"DLROW OLLEH"}

4) (Optional) Call data-api directly to see the 403 without internal token

    curl -i -X POST http://localhost:8081/api/transform \
      -H "Content-Type: application/json" \
      -d '{"text":"hello"}'     # → 403

   With the internal token:

    curl -i -X POST http://localhost:8081/api/transform \
      -H "Content-Type: application/json" \
      -H "X-Internal-Token: topsecret" \
      -d '{"text":"hello"}'     # → {"result":"OLLEH"}

5) Verify processing log in Postgres

    docker compose exec -it postgres psql -U pawtel -d pawtel -c \
    "select id, user_id, input_text, output_text, created_at
     from processing_log
     order by created_at desc
     limit 5;"

---

## Configuration

Environment variables are provided in `docker-compose.yml`:

- auth-api
  - POSTGRES_URL=jdbc:postgresql://postgres:5432/pawtel
  - POSTGRES_USER=pawtel
  - POSTGRES_PASSWORD=pawtel
  - JWT_SECRET=mysecretkey
  - INTERNAL_TOKEN=topsecret
  - DATA_API_URL=http://data-api:8081

- data-api
  - INTERNAL_TOKEN=topsecret

- postgres
  - POSTGRES_DB=pawtel
  - POSTGRES_USER=pawtel
  - POSTGRES_PASSWORD=pawtel

App properties (resolved from env in containers):

- auth-api `/src/main/resources/application.properties`:

    spring.datasource.url=${POSTGRES_URL:jdbc:postgresql://localhost:5433/pawtel}
    spring.datasource.username=${POSTGRES_USER:pawtel}
    spring.datasource.password=${POSTGRES_PASSWORD:pawtel}
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true

    jwt.secret=${JWT_SECRET:mysecretkey}

    app.data-api-url=${DATA_API_URL:http://data-api:8081}
    app.internal-token=${INTERNAL_TOKEN:topsecret}

- data-api `/src/main/resources/application.properties`:

    server.port=8081
    app.internal-token=${INTERNAL_TOKEN:topsecret}

---

## Endpoints

- auth-api
  - POST /api/auth/register → 201 | 409
  - POST /api/auth/login → 200 {"token":"..."} | 401
  - POST /api/process (JWT) → 200 {"result":"..."} | 401

- data-api
  - POST /api/transform (requires `X-Internal-Token`) → 200 {"result":"..."} | 403

---

## Docker

- docker-compose.yml (root) starts three services: postgres, auth-api, data-api
- Each service has its own Dockerfile:
  - `auth-api/Dockerfile` builds with Maven, then runs with Temurin JRE 21
  - `data-api/Dockerfile` same pattern; exposes 8081

Typical lifecycle:

    # rebuild one service
    docker compose build auth-api
    docker compose up -d auth-api

    # logs
    docker compose logs -f auth-api
    docker compose logs -f data-api
    docker compose logs -f postgres

    # stop / remove
    docker compose down
    # drop volumes as well (removes DB data)
    docker compose down -v

---

## Troubleshooting

- Port already in use (8080/8081/5432)
  
      lsof -nP -iTCP:8081 | grep LISTEN
      kill -9 <PID>
      docker compose up -d --build

- 401 on `/api/process`  
  Use a **fresh** token and include header: `Authorization: Bearer <token>`.

- 403 on `/api/transform`  
  Include header `X-Internal-Token` and make sure it matches both services’ `INTERNAL_TOKEN`.

- DB table empty  
  Ensure you actually invoked `/api/process` successfully and that `spring.jpa.hibernate.ddl-auto=update` is set.

---

## Acceptance Criteria Mapping

- Register/login with BCrypt + JWT ✅  
- Protected `/process` calls Service B with `X-Internal-Token` ✅  
- Service B rejects requests without/with wrong token ✅  
- Service A stores `processing_log` rows in Postgres ✅  
- All runs via `docker compose up` and tested with `curl` ✅

---
