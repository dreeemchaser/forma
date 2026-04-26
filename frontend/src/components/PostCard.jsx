import { useState } from 'react'
import { addComment, getComments } from '../api/posts'

export default function PostCard({ post, currentUser, onLike, onPostUpdated }) {
  const [showComments, setShowComments] = useState(false)
  const [comments, setComments] = useState([])
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [comment, setComment] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const isOwnPost = currentUser?.username === post.authorUsername

  const loadComments = async () => {
    setCommentsLoading(true)
    try {
      const res = await getComments(post.id)
      setComments(res.data)
    } catch (err) {
      console.error(err)
    } finally {
      setCommentsLoading(false)
    }
  }

  const handleToggleComments = () => {
    const next = !showComments
    setShowComments(next)
    if (next && comments.length === 0) {
      loadComments()
    }
  }

  const handleComment = async (e) => {
    e.preventDefault()
    if (!comment.trim()) return
    setSubmitting(true)
    try {
      await addComment(post.id, comment)
      setComment('')
      await loadComments()
      onPostUpdated()
    } catch (err) {
      console.error(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="rounded-2xl p-5 transition"
         style={{
           background: 'var(--card)',
           border: post.aiFlagged ? '1px solid rgba(122,154,53,0.35)' : '1px solid var(--border)',
           boxShadow: 'var(--shadow)'
         }}>
      {/* Flags */}
      <div className="flex gap-2 mb-2">
        {post.aiFlagged && (
          <span className="text-xs px-2 py-0.5 rounded-full font-medium"
                style={{ background: 'var(--green-light)', color: 'var(--green)', border: '1px solid rgba(122,154,53,0.3)' }}>
            ⚠️ AI Flagged
          </span>
        )}
        {post.flaggedMisleading && (
          <span className="text-xs px-2 py-0.5 rounded-full font-medium"
                style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fca5a5' }}>
            🚩 Misleading
          </span>
        )}
      </div>

      {/* Header */}
      <h2 className="font-semibold text-base leading-snug mb-1" style={{ color: 'var(--text-heading)' }}>
        {post.title}
      </h2>

      <p className="text-sm leading-relaxed mb-3" style={{ color: 'var(--text)' }}>{post.body}</p>

      {/* Meta */}
      <div className="flex items-center justify-between text-xs mb-3" style={{ color: 'var(--text-muted)' }}>
        <span style={{ color: 'var(--accent)', fontWeight: 600 }}>@{post.authorUsername}</span>
        <span>{new Date(post.updatedAt).toLocaleDateString()}</span>
      </div>

      {/* AI reasoning */}
      {post.aiFlagged && post.aiReasoning && (
        <div className="rounded-lg px-3 py-2 mb-3 text-xs"
             style={{ background: 'var(--green-light)', border: '1px solid rgba(122,154,53,0.2)', color: 'var(--green)' }}>
          <span className="font-semibold">AI reasoning: </span>{post.aiReasoning}
        </div>
      )}

      {/* Actions */}
      <div className="flex items-center gap-4 text-sm">
        {currentUser && !isOwnPost && (
          <button
            onClick={() => onLike(post)}
            className="flex items-center gap-1 transition"
            style={{ color: post.liked ? 'var(--brand)' : 'var(--text-muted)' }}
          >
            <span>{post.liked ? '♥' : '♡'}</span>
            <span>{post.likeCount}</span>
          </button>
        )}
        {(!currentUser || isOwnPost) && (
          <span className="flex items-center gap-1 text-xs" style={{ color: 'var(--text-muted)' }}>
            ♡ <span>{post.likeCount}</span>
          </span>
        )}

        <button
          onClick={handleToggleComments}
          className="transition text-sm"
          style={{ color: showComments ? 'var(--brand)' : 'var(--text-muted)' }}
        >
          💬 {showComments ? 'Hide' : 'Comments'}
        </button>
      </div>

      {/* Comments section */}
      {showComments && (
        <div className="mt-4 pt-4 space-y-3" style={{ borderTop: '1px solid var(--border)' }}>
          {commentsLoading ? (
            <p className="text-xs" style={{ color: 'var(--text-muted)' }}>Loading comments...</p>
          ) : comments.length === 0 ? (
            <p className="text-xs" style={{ color: 'var(--text-muted)' }}>No comments yet.</p>
          ) : (
            comments.map((c) => (
              <div key={c.id} className="flex gap-2">
                <span className="text-xs font-semibold shrink-0" style={{ color: 'var(--accent)' }}>@{c.authorUsername}</span>
                <span className="text-xs" style={{ color: 'var(--text)' }}>{c.body}</span>
              </div>
            ))
          )}

          {currentUser && (
            <form onSubmit={handleComment} className="flex gap-2 mt-2">
              <input
                type="text"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Write a comment..."
                className="flex-1 rounded-lg px-3 py-1.5 text-sm focus:outline-none"
                style={{ border: '1px solid var(--border)', color: 'var(--text-heading)', background: 'var(--white)' }}
                onFocus={e => e.target.style.boxShadow = '0 0 0 2px var(--accent)'}
                onBlur={e => e.target.style.boxShadow = 'none'}
              />
              <button
                type="submit"
                disabled={submitting || !comment.trim()}
                className="text-sm px-3 py-1.5 rounded-lg transition disabled:opacity-50"
                style={{ background: 'var(--brand)', color: 'var(--white)' }}
              >
                Post
              </button>
            </form>
          )}
        </div>
      )}
    </div>
  )
}
