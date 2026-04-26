# 🌐 Forma API

A Spring Boot REST API powering Forma — a minimalist social posting platform with Claude AI content moderation, JWT authentication, and role-based access control.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| ☕ Runtime | Java 26 + Spring Boot 4.0.5 |
| 🐘 Database | PostgreSQL (via Spring Data JPA / Hibernate) |
| 🔐 Auth | JWT (JJWT 0.12.6), BCrypt password hashing |
| 🤖 AI Moderation | Claude Haiku (`claude-haiku-4-5`) via Anthropic Java SDK |
| 📖 Docs | Springdoc OpenAPI 2.8.8 (Swagger UI) |
| 🔨 Build | Gradle |

---

## 🤖 How AI Moderation Works

Every post is analysed by Claude the moment it is created. Claude evaluates the content across four dimensions and returns a score between `0.0` and `1.0` along with a plain-English reasoning explanation.

| Dimension | What Claude looks for |
|---|---|
| 🤬 Toxicity | Profanity, hate speech, slurs, threats, harassment |
| 🧪 Misinformation | Claims contradicting established scientific or historical consensus |
| 🎭 Manipulative language | Fearmongering, clickbait, emotionally charged or misleading phrasing |
| 📢 Spam signals | Excessive caps/punctuation, repetitive or nonsensical content |

**Score > 0.6** → `aiFlagged = true`. The post enters the moderation queue.

A moderator can then:
- ✅ **Confirm** — `POST /api/posts/{id}/flag` → sets `flaggedMisleading = true`. Post shows a 🚩 badge in the feed.
- 🗑️ **Dismiss** — `DELETE /api/posts/{id}/flag` → clears `aiFlagged` and `flaggedMisleading`. Post leaves the queue entirely.

See [`docs/diagrams/object-flow.md`](docs/diagrams/object-flow.md) for the full moderation flow diagram.

---

## 🔐 Authentication

JWT-based stateless auth. Tokens are valid for **24 hours**.

Include the token on all protected requests:

```
Authorization: Bearer <token>
```

### 👤 Roles

| Role | Capabilities |
|---|---|
| `REGULAR_USER` | Read posts, create posts, like/unlike, comment |
| `MODERATOR` | Everything above + view moderation queue + confirm/dismiss AI flags |

---

## 📡 API Endpoints

Full interactive docs available at [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html) after running locally.

### 🔑 Auth — `/api/auth`

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| `POST` | `/register` | Create account, returns JWT | ❌ |
| `POST` | `/login` | Authenticate, returns JWT | ❌ |
| `GET` | `/me` | Get current user (id, username, role) | ✅ |

### 📝 Posts — `/api/posts`

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| `GET` | `/` | Get all posts, newest first | ❌ |
| `GET` | `/{id}` | Get post by ID | ❌ |
| `POST` | `/` | Create post (triggers AI analysis) | ✅ |
| `GET` | `/{id}/comments` | Get all comments on a post | ❌ |
| `POST` | `/{id}/comments` | Add a comment | ✅ |
| `POST` | `/{id}/like` | Like a post | ✅ |
| `DELETE` | `/{id}/like` | Unlike a post | ✅ |
| `POST` | `/{id}/flag` | Confirm post as misleading | 🔒 Moderator |
| `DELETE` | `/{id}/flag` | Dismiss AI flag | 🔒 Moderator |

### 🛡️ Moderator — `/api/moderator`

| Method | Endpoint | Description | Auth required |
|---|---|---|---|
| `GET` | `/` | Get all AI-flagged posts with scores and reasoning | 🔒 Moderator |

---

## ⚠️ Error Responses

All errors are returned as plain-text strings with the appropriate HTTP status code.

| Status | Cause |
|---|---|
| `400` | Validation failure (blank fields, length constraints) |
| `401` | Missing, expired, or invalid JWT token / bad credentials |
| `403` | Authenticated but insufficient role (MODERATOR required) |
| `404` | Post or user not found |
| `409` | Conflict — username taken, post already liked, or self-like attempted |
| `500` | Unexpected server error (logged server-side) |

---

## ⚙️ Environment Variables

All secrets are injected via environment variables. Set them in `.envrc` (with direnv) or export them directly.

| Variable | Description |
|---|---|
| `DB_URL` | JDBC connection URL, e.g. `jdbc:postgresql://localhost:5432/forma` |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Base64-encoded HMAC secret (min 256 bits) |
| `AI_API_KEY` | Anthropic API key (`sk-ant-...`) |

```bash
# .envrc example
export DB_URL=jdbc:postgresql://localhost:5432/forma
export DB_USERNAME=forma
export DB_PASSWORD=password
export JWT_SECRET=your_base64_secret_here
export AI_API_KEY=sk-ant-api03-...
```

---

## 🏃 Running Locally

**Prerequisites:** Java 26, PostgreSQL running with a `forma` database and user.

```bash
# 1. Clone
git clone https://github.com/your-username/forma-api.git
cd forma-api

# 2. Load environment variables
source .envrc

# 3. Run
./gradlew bootRun
```

The API starts on `http://localhost:8080`.
Swagger UI: [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)

### 🌱 Load seed data

```bash
psql -U forma -d forma -f seed.sql
```

Seed users (password for all: `password123`):

| Username | Role |
|---|---|
| `alice` | REGULAR_USER |
| `bob` | REGULAR_USER |
| `charlie` | REGULAR_USER |
| `diana` | REGULAR_USER |
| `moderator` | MODERATOR |

---

## 🗂️ Project Structure

```
src/main/java/com/forma/api/
├── config/          # OpenAPI config (Swagger), AI service wiring
├── controller/      # REST controllers (Auth, Post, Moderator)
├── dto/             # Request and response records with @Schema annotations
├── model/           # JPA entities (User, Post, Comment, PostLike, Role)
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, JWT service, security config
├── service/         # Business logic (AuthService, PostService, ClaudeAIService)
└── utils/
    └── exceptions/  # Custom exceptions + GlobalExceptionHandler

docs/
├── diagrams/
│   ├── entities.puml      # PlantUML entity-relationship diagram
│   └── object-flow.md     # Request/response flow for every endpoint
seed.sql                   # Seed data for local development
```

---

## 📊 Diagrams

🗃️ Entity relationships: [`docs/diagrams/entities.puml`](docs/diagrams/entities.puml) — render with [PlantUML](https://plantuml.com) or the VS Code PlantUML extension.

🔀 Object flows: [`docs/diagrams/object-flow.md`](docs/diagrams/object-flow.md) — covers auth, JWT filter, post creation (with AI), comments, likes, and the full moderation workflow.
