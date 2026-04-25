import { useEffect, useState } from 'react'
import { getFlaggedPosts, flagPost, unflagPost } from '../api/moderator'

export default function ModeratorPage() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => {
    setLoading(true)
    getFlaggedPosts()
      .then((res) => setPosts(res.data))
      .catch(() => setError('Failed to load flagged posts.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleConfirm = async (id) => {
    await flagPost(id)
    load()
  }

  const handleDismiss = async (id) => {
    await unflagPost(id)
    load()
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-xl font-bold" style={{ color: 'var(--text-heading)' }}>Moderation Queue</h1>
        <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>
          Posts automatically flagged by AI with a score above 0.6
        </p>
      </div>

      {loading && (
        <div className="text-sm py-12 text-center" style={{ color: 'var(--text-muted)' }}>Loading...</div>
      )}

      {error && (
        <div className="text-sm rounded-lg px-4 py-3" style={{ background: '#fef2f2', color: '#dc2626' }}>{error}</div>
      )}

      {!loading && !error && posts.length === 0 && (
        <div className="text-center py-16" style={{ color: 'var(--text-muted)' }}>
          <div className="text-4xl mb-3">✅</div>
          <p className="text-sm">Queue is clear — no flagged posts.</p>
        </div>
      )}

      <div className="space-y-4">
        {posts.map((post) => (
          <div key={post.id} className="rounded-xl p-5"
               style={{ border: '1px solid var(--border)', background: 'var(--card)', boxShadow: 'var(--shadow)' }}>
            {/* Header */}
            <div className="flex items-start justify-between gap-4 mb-3">
              <div>
                <h2 className="font-semibold text-sm" style={{ color: 'var(--text-heading)' }}>{post.title}</h2>
                <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>by {post.authorUsername}</p>
              </div>
              <span className="shrink-0 text-xs font-semibold px-2 py-1 rounded-full"
                    style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fca5a5' }}>
                Score {post.aiScore !== undefined ? post.aiScore.toFixed(2) : '—'}
              </span>
            </div>

            {/* Body */}
            <p className="text-sm leading-relaxed mb-4" style={{ color: 'var(--text)' }}>{post.body}</p>

            {/* AI Reasoning */}
            <div className="rounded-lg px-4 py-3 mb-4"
                 style={{ background: 'var(--green-light)', border: '1px solid rgba(122,154,53,0.3)' }}>
              <p className="text-xs font-semibold mb-1" style={{ color: 'var(--green)' }}>✨ AI Reasoning</p>
              <p className="text-xs leading-relaxed" style={{ color: 'var(--green)' }}>{post.aiReasoning}</p>
            </div>

            {/* Status */}
            {post.flaggedMisleading && (
              <div className="text-xs font-medium mb-3" style={{ color: '#f97316' }}>
                ⚠️ Confirmed misleading by moderator
              </div>
            )}

            {/* Actions */}
            <div className="flex gap-2">
              <button
                onClick={() => handleConfirm(post.id)}
                disabled={post.flaggedMisleading}
                className="flex-1 text-xs font-medium py-2 rounded-lg transition disabled:opacity-40 disabled:cursor-not-allowed"
                style={{ border: '1px solid #fca5a5', color: '#dc2626', background: 'transparent' }}
                onMouseEnter={e => !post.flaggedMisleading && (e.target.style.background = '#fef2f2')}
                onMouseLeave={e => e.target.style.background = 'transparent'}
              >
                Confirm Misleading
              </button>
              <button
                onClick={() => handleDismiss(post.id)}
                className="flex-1 text-xs font-medium py-2 rounded-lg transition"
                style={{ border: '1px solid var(--border)', color: 'var(--text-muted)', background: 'transparent' }}
                onMouseEnter={e => e.target.style.background = 'var(--accent-light)'}
                onMouseLeave={e => e.target.style.background = 'transparent'}
              >
                Dismiss
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
