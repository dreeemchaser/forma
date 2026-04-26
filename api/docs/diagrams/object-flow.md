# Forma API — Object Flow Diagrams

---

## 1. Auth Flow

### Register

```
Client
  │
  │  POST /api/auth/register  { username, password }
  │  ──────────────────────────────────────────────▶  AuthController
  │                                                        │
  │                                                        │  @Valid AuthRequest
  │                                                        │  → MethodArgumentNotValidException (400) if invalid
  │                                                        ▼
  │                                                   AuthService
  │                                                        │
  │                                                        │  UserRepository.existsByUsername()
  │                                                        │  → UsernameAlreadyExistsException (409) if taken
  │                                                        │
  │                                                        │  PasswordEncoder.encode(password)
  │                                                        │  User(username, passwordHash, role=REGULAR_USER)
  │                                                        │  UserRepository.save(user)  ──▶ DB
  │                                                        │
  │                                                        │  JwtService.generateToken(user)
  │                                                        ▼
  │  ◀──────────────────────────────────────────  201  AuthResponse { token }
```

### Login

```
Client
  │
  │  POST /api/auth/login  { username, password }
  │  ──────────────────────────────────────────────▶  AuthController
  │                                                        │
  │                                                        ▼
  │                                                   AuthService
  │                                                        │
  │                                                        │  UserRepository.findByUsername()
  │                                                        │  → BadCredentialsException (401) if not found
  │                                                        │
  │                                                        │  PasswordEncoder.matches(password, hash)
  │                                                        │  → BadCredentialsException (401) if mismatch
  │                                                        │
  │                                                        │  JwtService.generateToken(user)
  │                                                        ▼
  │  ◀──────────────────────────────────────────  200  AuthResponse { token }
```

### Get current user

```
Client
  │
  │  GET /api/auth/me
  │  Authorization: Bearer <token>
  │  ──────────────────────────────────────────────▶  JwtAuthFilter  (see section 2)
  │                                                        │
  │                                                        ▼
  │                                                   AuthController
  │                                                        │
  │                                                        │  @AuthenticationPrincipal User
  │                                                        ▼
  │  ◀──────────────────────────────────────────  200  UserResponse { id, username, role }
```

---

## 2. JWT Authentication Filter (all protected routes)

```
Client
  │
  │  Authorization: Bearer <token>
  │  ──────────────────────────────▶  JwtAuthFilter
  │                                        │
  │                                        │  Extract token from Authorization header
  │                                        │  JwtService.extractUsername(token)
  │                                        │  → 401 if token malformed or expired
  │                                        │
  │                                        │  UserDetailsService.loadUserByUsername()
  │                                        │  → 401 if user no longer exists
  │                                        │
  │                                        │  JwtService.isTokenValid(token, userDetails)
  │                                        │  → 401 if invalid
  │                                        │
  │                                        │  SecurityContextHolder.setAuthentication(...)
  │                                        │  (authorities: ["ROLE_REGULAR_USER"] or ["ROLE_MODERATOR"])
  │                                        ▼
  │                                   Controller
  │                                   (@AuthenticationPrincipal User injected automatically)
```

---

## 3. Post Flow

### Create post (with AI moderation)

```
Client
  │
  │  POST /api/posts  { title, body }
  │  Authorization: Bearer <token>
  │  ─────────────────────────────────▶  PostController
  │                                           │
  │                                           │  @Valid CreatePostRequest
  │                                           │  → 400 if title or body blank / too long
  │                                           │
  │                                           │  @AuthenticationPrincipal User (author)
  │                                           ▼
  │                                       PostService.createPost()
  │                                           │
  │                                           │  ClaudeAIService.analysePost(title, body)
  │                                           │  ─────────────────────────────────────────▶  Anthropic API
  │                                           │  ◀─────────────────────────────────────────
  │                                           │  PostAnalysisResponse { score, reasoning }
  │                                           │
  │                                           │  aiFlagged = (score > 0.6)
  │                                           │
  │                                           │  Post { title, body, author,
  │                                           │         aiFlagged, aiScore, aiReasoning }
  │                                           │  PostRepository.save(post)  ──▶ DB
  │                                           ▼
  │  ◀──────────────────────────────  201  PostResponse { id, title, body, authorUsername,
  │                                                        likeCount, liked, aiFlagged,
  │                                                        aiScore, aiReasoning,
  │                                                        flaggedMisleading, updatedAt }
```

### Get all posts

```
Client
  │
  │  GET /api/posts
  │  ─────────────────────────────────▶  PostController
  │                                           │
  │                                           │  @AuthenticationPrincipal User (nullable)
  │                                           ▼
  │                                       PostService.getAllPosts(currentUser)
  │                                           │
  │                                           │  PostRepository.findAllByOrderByUpdatedAtDesc()
  │                                           │  For each post:
  │                                           │    PostLikeRepository.countByPostId()
  │                                           │    PostLikeRepository.existsByPostIdAndUserId()
  │                                           │    (liked = false if currentUser is null)
  │                                           ▼
  │  ◀──────────────────────────────  200  List<PostResponse>
```

### Get post by ID

```
Client
  │
  │  GET /api/posts/{id}
  │  ─────────────────────────────────▶  PostController
  │                                           │
  │                                           ▼
  │                                       PostService.getPostById(id, currentUser)
  │                                           │
  │                                           │  PostRepository.findById(id)
  │                                           │  → PostNotFoundException (404) if missing
  │                                           ▼
  │  ◀──────────────────────────────  200  PostResponse
```

---

## 4. Comment Flow

### Get comments

```
Client
  │
  │  GET /api/posts/{id}/comments
  │  ────────────────────────────────────────▶  PostController
  │                                                  │
  │                                                  ▼
  │                                             PostService.getComments(postId)
  │                                                  │
  │                                                  │  PostRepository.existsById(postId)
  │                                                  │  → PostNotFoundException (404) if missing
  │                                                  │
  │                                                  │  CommentRepository.findByPostId(postId)
  │                                                  ▼
  │  ◀──────────────────────────────────  200  List<CommentResponse>
  │                                            (ordered by createdAt ascending)
```

### Add comment

```
Client
  │
  │  POST /api/posts/{id}/comments  { body }
  │  Authorization: Bearer <token>
  │  ────────────────────────────────────────▶  PostController
  │                                                  │
  │                                                  │  @Valid CreateCommentRequest
  │                                                  │  → 400 if body blank or > 500 chars
  │                                                  │
  │                                                  ▼
  │                                             PostService.addComment(postId, request, user)
  │                                                  │
  │                                                  │  PostRepository.findById(postId)
  │                                                  │  → PostNotFoundException (404) if missing
  │                                                  │
  │                                                  │  Comment { post, author, body }
  │                                                  │  CommentRepository.save(comment)  ──▶ DB
  │                                                  ▼
  │  ◀──────────────────────────────────  201  CommentResponse { id, authorUsername, body, createdAt }
```

---

## 5. Like Flow

### Like a post

```
Client
  │
  │  POST /api/posts/{id}/like
  │  Authorization: Bearer <token>
  │  ──────────────────────────▶  PostController
  │                                    │
  │                                    ▼
  │                               PostService.likePost(postId, user)
  │                                    │
  │                                    │  PostRepository.findById(postId)
  │                                    │  → PostNotFoundException (404) if missing
  │                                    │
  │                                    │  post.authorId == user.id?
  │                                    │  → ConflictException (409) "cannot like own post"
  │                                    │
  │                                    │  PostLikeRepository.existsByPostIdAndUserId()
  │                                    │  → ConflictException (409) "already liked"
  │                                    │
  │                                    │  PostLike { post, user }
  │                                    │  PostLikeRepository.save(postLike)  ──▶ DB
  │                                    ▼
  │  ◀──────────────────────  204 No Content
```

### Unlike a post

```
Client
  │
  │  DELETE /api/posts/{id}/like
  │  Authorization: Bearer <token>
  │  ──────────────────────────▶  PostController
  │                                    │
  │                                    ▼
  │                               PostService.unlikePost(postId, user)
  │                                    │
  │                                    │  PostLikeRepository.findByPostIdAndUserId()
  │                                    │  → ConflictException (409) "not liked" if absent
  │                                    │
  │                                    │  PostLikeRepository.delete(postLike)  ──▶ DB
  │                                    ▼
  │  ◀──────────────────────  204 No Content
```

---

## 6. Moderation Flow

### Get moderation queue

```
Client (MODERATOR role)
  │
  │  GET /api/moderator
  │  Authorization: Bearer <token>
  │  ─────────────────────▶  SecurityConfig
  │                               │  hasRole("MODERATOR")?
  │                               │  → 403 Forbidden if not moderator
  │                               ▼
  │                          ModeratorController
  │                               │
  │                               ▼
  │                          PostService.getAllFlaggedPosts()
  │                               │
  │                               │  PostRepository.findByAiFlaggedTrue()
  │                               │  (ordered by updatedAt desc)
  │                               ▼
  │  ◀──────────────────  200  List<PostResponse>
  │                            (includes aiScore + aiReasoning for each post)
```

### Confirm misleading (moderator action)

```
Client (MODERATOR role)
  │
  │  POST /api/posts/{id}/flag
  │  Authorization: Bearer <token>
  │  ───────────────────────────▶  SecurityConfig
  │                                     │  hasRole("MODERATOR")?
  │                                     │  → 403 if not moderator
  │                                     ▼
  │                                PostController
  │                                     │
  │                                     ▼
  │                                PostService.flagPost(postId)
  │                                     │
  │                                     │  PostRepository.findById(postId)
  │                                     │  → PostNotFoundException (404) if missing
  │                                     │
  │                                     │  post.setFlaggedMisleading(true)
  │                                     │  PostRepository.save(post)  ──▶ DB
  │                                     ▼
  │  ◀────────────────────────  204 No Content
  │                             (post now shows 🚩 Misleading badge in feed)
```

### Dismiss flag (moderator action)

```
Client (MODERATOR role)
  │
  │  DELETE /api/posts/{id}/flag
  │  Authorization: Bearer <token>
  │  ───────────────────────────▶  SecurityConfig
  │                                     │  hasRole("MODERATOR")?
  │                                     │  → 403 if not moderator
  │                                     ▼
  │                                PostController
  │                                     │
  │                                     ▼
  │                                PostService.unflagPost(postId)
  │                                     │
  │                                     │  PostRepository.findById(postId)
  │                                     │  → PostNotFoundException (404) if missing
  │                                     │
  │                                     │  post.setAiFlagged(false)
  │                                     │  post.setFlaggedMisleading(false)
  │                                     │  PostRepository.save(post)  ──▶ DB
  │                                     ▼
  │  ◀────────────────────────  204 No Content
  │                             (post removed from moderation queue entirely)
```

---

## 7. Exception Handling Summary

All errors are handled globally by `GlobalExceptionHandler`:

| Exception | HTTP | Example message |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `Username must be between 3 and 20 characters.` |
| `BadCredentialsException` | 401 | `Bad credentials` |
| `UserNotFoundException` | 404 | `User not found: alice` |
| `PostNotFoundException` | 404 | `Post not found: b100...` |
| `UsernameAlreadyExistsException` | 409 | `Username 'alice' is already taken` |
| `ConflictException` | 409 | `You have already liked this post` |
| `Exception` (generic) | 500 | `An unexpected error occurred: ...` |
