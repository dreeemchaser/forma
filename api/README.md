# 🌐 Forma API

A Spring Boot REST API for a social posting platform with **AI-powered content moderation** via Claude, JWT authentication, and role-based access control.

---

## 🚀 Tech Stack

- **Java 26** + **Spring Boot 4.0.5**
- **PostgreSQL** — persistent storage
- **JWT** — stateless authentication
- **Claude Haiku** (Anthropic) — AI content moderation
- **Springdoc OpenAPI** — Swagger UI

---

## 🤖 How AI Moderation Works

Every post is automatically analysed by Claude when it's created. Claude evaluates the post across four dimensions:

| Dimension | Description |
|---|---|
| 🤬 **Toxicity** | Profanity, slurs, hate speech, threats, harassment |
| 🧪 **Misinformation** | Claims contradicting established scientific/historical consensus |
| 🎭 **Manipulative Language** | Fearmongering, clickbait, emotionally charged misleading phrasing |
| 📢 **Spam Signals** | Repetitive text, nonsensical content, excessive caps/punctuation |

Claude returns a **score between 0.0 and 1.0** and a **reasoning** explanation.

- **Score > 0.6** → post is automatically flagged (`aiFlagged = true`)
- Flagged posts enter the **moderator queue** for human review
- Moderators can **confirm** (`flaggedMisleading = true`) or **dismiss** the flag

---

## 🔐 Authentication

JWT-based authentication. Include the token in all protected requests:

```
Authorization: Bearer <token>
```

### Roles

| Role | Permissions |
|---|---|
| `REGULAR_USER` | Create posts, comment, like/unlike |
| `MODERATOR` | All of the above + flag/unflag posts + view moderation queue |

---

## 📡 API Endpoints

### 🔑 Auth — `/api/auth`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/register` | Register a new user | ❌ |
| `POST` | `/login` | Login and receive JWT token | ❌ |
| `GET` | `/me` | Get current authenticated user | ✅ |

### 📝 Posts — `/api/posts`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/` | Get all posts | ❌ |
| `GET` | `/{id}` | Get post by ID | ❌ |
| `POST` | `/` | Create a post (triggers AI analysis) | ✅ |
| `POST` | `/{id}/comments` | Add a comment | ✅ |
| `POST` | `/{id}/like` | Like a post | ✅ |
| `DELETE` | `/{id}/like` | Unlike a post | ✅ |
| `POST` | `/{id}/flag` | Flag post as misleading | 🔒 Moderator |
| `DELETE` | `/{id}/flag` | Remove misleading flag | 🔒 Moderator |

### 🛡️ Moderator — `/api/moderator`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/` | Get all AI flagged posts with reasoning | 🔒 Moderator |

---

## ⚙️ Configuration

Create a `.env` file or set the following environment variables:

```env
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_base64_encoded_secret
ANTHROPIC_API_KEY=your_anthropic_api_key
```

`application.yaml` expects:

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

anthropic:
  provider: claude
  api-key: ${ANTHROPIC_API_KEY}
```

---

## 📦 Running the Project

```bash
# Clone the repo
git clone https://github.com/your-username/forma-api.git
cd forma-api

# Run with Gradle
./gradlew bootRun
```

Swagger UI available at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🗂️ Project Structure

```
src/main/java/com/forma/api/
├── config/          # Claude & OpenAPI configuration
├── controller/      # REST controllers
├── dto/             # Request & response objects
├── model/           # JPA entities
├── repository/      # Data access layer
├── security/        # JWT filter & security config
├── service/         # Business logic & Claude integration
└── utils/           # Exception handling
```
