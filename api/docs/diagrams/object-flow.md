# Forma API — Object Flow Diagram

## Auth Flow

```
Client
  │
  │  POST /api/auth/register  { username, password }
  │  ──────────────────────────────────────────────▶  AuthController
  │                                                        │
  │                                                        │  AuthRequest(username, password)
  │                                                        ▼
  │                                                   AuthService
  │                                                        │
  │                                                        │  User(username, passwordHash, role)
  │                                                        ▼
  │                                                  UserRepository ──▶ DB
  │                                                        │
  │                                                        │  JwtService.generateToken(UserDetails)
  │                                                        ▼
  │  ◀──────────────────────────────────────────  AuthResponse(token)
  │
  │  POST /api/auth/login  { username, password }
  │  ──────────────────────────────────────────────▶  AuthController
  │                                                        │
  │                                                        │  AuthRequest(username, password)
  │                                                        ▼
  │                                                   AuthService
  │                                                        │
  │                                                        │  UserRepository.findByUsername()
  │                                                        │  PasswordEncoder.matches()
  │                                                        │  JwtService.generateToken()
  │                                                        ▼
  │  ◀──────────────────────────────────────────  AuthResponse(token)
```

---

## Request Authentication (every protected route)

```
Client
  │
  │  Authorization: Bearer <token>
  │  ──────────────────────────────▶  JwtAuthFilter
  │                                        │
  │                                        │  JwtService.extractUsername(token)
  │                                        │  UserDetailsService.loadUserByUsername()
  │                                        │  JwtService.isTokenValid()
  │                                        │
  │                                        │  sets SecurityContextHolder
  │                                        ▼
  │                                   Controller (@AuthenticationPrincipal UserDetails)
```

---

## Post Flow

```
Client
  │
  │  POST /api/posts  { title, body }
  │  ─────────────────────────────────▶  PostController
  │                                           │
  │                                           │  CreatePostRequest(title, body)
  │                                           │  UserDetails (from SecurityContext)
  │                                           ▼
  │                                       PostService
  │                                           │
  │                                           │  UserRepository.findByUsername()  ──▶ User
  │                                           │  Post(title, body, author)
  │                                           │  PostRepository.save(post)        ──▶ DB
  │                                           ▼
  │  ◀──────────────────────────────  PostResponse(id, title, body, authorUsername, ...)
  │
  │  GET /api/posts
  │  ─────────────────────────────────▶  PostController
  │                                           │
  │                                           ▼
  │                                       PostService
  │                                           │
  │                                           │  PostRepository.findAllByOrderByUpdatedAtDesc()
  │                                           │  PostLikeRepository.countByPostId()
  │                                           ▼
  │  ◀──────────────────────────────  List<PostResponse>
```

---

## Comment Flow

```
Client
  │
  │  POST /api/posts/{id}/comments  { body }
  │  ────────────────────────────────────────▶  PostController
  │                                                  │
  │                                                  │  CreateCommentRequest(body)
  │                                                  │  postId (path variable)
  │                                                  │  UserDetails (from SecurityContext)
  │                                                  ▼
  │                                             PostService
  │                                                  │
  │                                                  │  PostRepository.findById(postId)   ──▶ Post
  │                                                  │  UserRepository.findByUsername()   ──▶ User
  │                                                  │  Comment(post, author, body)
  │                                                  │  CommentRepository.save(comment)   ──▶ DB
  │                                                  ▼
  │  ◀──────────────────────────────────  CommentResponse(id, authorUsername, body, createdAt)
```

---

## Like Flow

```
Client
  │
  │  POST /api/posts/{id}/like
  │  ──────────────────────────▶  PostController
  │                                    │
  │                                    │  postId, UserDetails
  │                                    ▼
  │                               PostService
  │                                    │
  │                                    │  PostLikeRepository.existsByPostIdAndUserId()  ──▶ check duplicate
  │                                    │  PostLike(post, user)
  │                                    │  PostLikeRepository.save(postLike)             ──▶ DB
  │                                    ▼
  │  ◀──────────────────────  204 No Content
  │
  │  DELETE /api/posts/{id}/like
  │  ──────────────────────────▶  PostController
  │                                    │
  │                                    ▼
  │                               PostService
  │                                    │
  │                                    │  PostLikeRepository.findByPostIdAndUserId()
  │                                    │  PostLikeRepository.delete(postLike)  ──▶ DB
  │                                    ▼
  │  ◀──────────────────────  204 No Content
```

---

## Moderation Flow

```
Client (MODERATOR role only)
  │
  │  GET /api/moderator
  │  ────────────────────▶  ModeratorController
  │                               │
  │                               ▼
  │                          PostService
  │                               │
  │                               │  PostRepository.findByFlaggedMisleadingTrue()
  │                               ▼
  │  ◀──────────────────  List<PostResponse>  (flaggedMisleading = true)
  │
  │  POST /api/posts/{id}/flag
  │  ────────────────────────────▶  PostController
  │                                      │
  │                                      ▼
  │                                 PostService
  │                                      │
  │                                      │  PostRepository.findById()
  │                                      │  post.setFlaggedMisleading(true)
  │                                      │  PostRepository.save(post)  ──▶ DB
  │                                      ▼
  │  ◀──────────────────────────  204 No Content
```
