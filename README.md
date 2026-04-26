# 🗣️ Forma

A minimalist web forum with AI-powered content moderation. Users can create posts, comment, and like. Moderators review posts automatically pre-flagged by Claude AI for toxicity, misinformation, and manipulative language.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| 🎨 Frontend | React 19, Vite, Tailwind CSS v4 |
| ☕ Backend | Java 26, Spring Boot 4.0.5, Gradle |
| 🐘 Database | PostgreSQL 16 (Spring Data JPA / Hibernate) |
| 🔐 Auth | JWT (JJWT 0.12.6), BCrypt |
| 🤖 AI Moderation | Claude Haiku (`claude-haiku-4-5`) via Anthropic Java SDK |
| 📖 API Docs | Swagger UI (Springdoc OpenAPI) |
| 🐳 Infrastructure | Docker, Docker Compose |

---

## ✅ Prerequisites

- [Docker](https://www.docker.com/products/docker-desktop) and Docker Compose
- An [Anthropic API key](https://console.anthropic.com/) (`sk-ant-...`)

That's it — everything else runs inside Docker.

---

## 🚀 Running the Project

### 1. Clone the repository

```bash
git clone https://github.com/your-username/forma.git
cd forma
```

### 2. Configure environment variables

Copy the example below into a `.env` file at the project root:

```bash
DB_PASSWORD=password123
JWT_SECRET=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktbXVzdC1iZS1sb25nLWVub3VnaA==
AI_PROVIDER=claude
AI_API_KEY=sk-ant-your-key-here
```

> **JWT_SECRET** must be a Base64-encoded string of at least 256 bits. The example value above is safe to use for local testing.

### 3. Start all services

```bash
docker compose up --build
```

This starts three containers:

| Service | URL |
|---|---|
| 🌐 Frontend | http://localhost:3000 |
| ⚙️ Backend API | http://localhost:8080 |
| 📖 Swagger UI | http://localhost:8080/swagger-ui.html |
| 🐘 PostgreSQL | localhost:5432 |

Wait for the backend to log `Started ApiApplication` before using the app.

### 4. Load seed data

In a separate terminal, once the database container is healthy:

```bash
docker compose exec postgres psql -U forma -d forma -f /dev/stdin < api/seed.sql
```

This creates 5 users, 7 posts (2 AI-flagged), likes, and comments.

**🌱 Seed accounts** (password for all: `password123`):

| Username | Role |
|---|---|
| `alice` | 👤 Regular user |
| `bob` | 👤 Regular user |
| `charlie` | 👤 Regular user |
| `diana` | 👤 Regular user |
| `moderator` | 🛡️ Moderator |

---

## 🤖 How AI Moderation Works

Every post is analysed by Claude the moment it is submitted. Claude scores the content from `0.0` (clean) to `1.0` (highly problematic) across four dimensions:

| Dimension | What Claude looks for |
|---|---|
| 🤬 Toxicity | Profanity, hate speech, slurs, threats, harassment |
| 🧪 Misinformation | Claims contradicting established scientific or historical consensus |
| 🎭 Manipulative language | Fearmongering, clickbait, emotionally charged or misleading phrasing |
| 📢 Spam signals | Excessive caps/punctuation, repetitive or nonsensical content |

**Score > 0.6** → `aiFlagged = true`. The post appears in the moderation queue.

A moderator can then:
- ✅ **Confirm** (`POST /api/posts/{id}/flag`) → marks the post as misleading. A red "Misleading or false information" label appears on the post for all users.
- 🗑️ **Dismiss** (`DELETE /api/posts/{id}/flag`) → clears the flag entirely (false positive).

---

## 📡 API Documentation

📬 **[Postman Collection](https://www.postman.com/orange-shuttle-88986/workspace/public-workspace/collection/27852135-d08c5ef3-72bf-4e5c-b440-19b71d7d7940?action=share&creator=27852135)** — import directly into Postman to test all endpoints.

Interactive Swagger UI is also available at `http://localhost:8080/swagger-ui.html` when the backend is running.

### 🔑 Auth — `/api/auth`

| Method | Endpoint | Auth |
|---|---|---|
| `POST` | `/api/auth/register` | Public |
| `POST` | `/api/auth/login` | Public |
| `GET` | `/api/auth/me` | JWT required |

### 📝 Posts — `/api/posts`

| Method | Endpoint | Auth |
|---|---|---|
| `GET` | `/api/posts` | Public |
| `GET` | `/api/posts/{id}` | Public |
| `POST` | `/api/posts` | JWT required |
| `GET` | `/api/posts/{id}/comments` | Public |
| `POST` | `/api/posts/{id}/comments` | JWT required |
| `POST` | `/api/posts/{id}/like` | JWT required |
| `DELETE` | `/api/posts/{id}/like` | JWT required |
| `POST` | `/api/posts/{id}/flag` | 🛡️ Moderator only |
| `DELETE` | `/api/posts/{id}/flag` | 🛡️ Moderator only |

### 🛡️ Moderator — `/api/moderator`

| Method | Endpoint | Auth |
|---|---|---|
| `GET` | `/api/moderator` | 🛡️ Moderator only |

Include the JWT on protected requests:

```
Authorization: Bearer <token>
```

---

## 🗂️ Project Structure

```
forma/
├── api/                        # Spring Boot backend
│   ├── src/main/java/com/forma/api/
│   │   ├── config/             # Swagger + Anthropic client config
│   │   ├── controller/         # REST controllers (Auth, Post, Moderator)
│   │   ├── dto/                # Request/response records
│   │   ├── model/              # JPA entities (User, Post, Comment, PostLike)
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── security/           # JWT filter, JWT service, SecurityConfig
│   │   └── service/            # AuthService, PostService, ClaudeAIService
│   ├── docs/diagrams/          # Entity diagram (PlantUML) + flow diagrams
│   ├── seed.sql                # Test data
│   └── Dockerfile
├── frontend/                   # React + Vite frontend
│   ├── src/
│   │   ├── api/                # Axios client + endpoint modules
│   │   ├── components/         # Navbar, PostCard, CreatePostModal
│   │   ├── context/            # AuthContext (JWT storage + user state)
│   │   └── pages/              # Feed, Login, Register, Profile, Moderator
│   ├── nginx.conf              # Production Nginx config (serves SPA + proxies /api)
│   └── Dockerfile
└── docker-compose.yml
```

---

## 💡 Design Decisions

**☕ Spring Boot + PostgreSQL** — Spring Boot is the industry standard for Java REST APIs. Spring Data JPA gives first-class PostgreSQL integration with zero boilerplate, and Hibernate manages the schema automatically (`ddl-auto: update`). PostgreSQL is a proven, relational datastore well-suited to a forum's structured, relational data.

**🔐 JWT authentication** — Stateless, no session storage needed, trivially scalable. Tokens are signed with HMAC-SHA and expire after 24 hours. Passwords are hashed with BCrypt.

**🤖 Claude Haiku for moderation** — Fast and cost-efficient for high-frequency inference on short text. The `PostAnalysisInterface` abstraction means the AI provider can be swapped out via the `AI_PROVIDER` environment variable without touching service logic.

**🎨 React + Vite** — Lightweight SPA with fast hot module replacement in development. Tailwind CSS v4 for styling. In production, Nginx serves the built static files and proxies `/api/*` to the backend container.
