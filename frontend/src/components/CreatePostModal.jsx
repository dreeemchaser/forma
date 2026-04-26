import { useState } from 'react'
import { createPost } from '../api/posts'

export default function CreatePostModal({ onClose, onCreated }) {
  const [form, setForm] = useState({ title: '', body: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await createPost(form.title, form.body)
      onCreated()
    } catch (err) {
      setError(err.response?.data || 'Failed to create post')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 flex items-center justify-center z-50 px-4" style={{ background: 'rgba(39,24,126,0.3)' }}>
      <div className="rounded-2xl w-full max-w-lg p-6" style={{ background: 'var(--card)', boxShadow: 'var(--shadow)' }}>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold" style={{ color: 'var(--text-heading)' }}>New Post</h2>
          <button onClick={onClose} style={{ color: 'var(--text-muted)' }} className="hover:text-gray-700 text-xl leading-none">×</button>
        </div>

        {error && (
          <div className="text-sm rounded-lg px-4 py-3 mb-4" style={{ background: '#fef2f2', color: '#dc2626' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1" style={{ color: 'var(--text)' }}>Title</label>
            <input
              type="text"
              value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })}
              className="w-full rounded-lg px-3 py-2 text-sm focus:outline-none"
              style={{ border: '1px solid var(--border)', color: 'var(--text-heading)', background: 'var(--white)' }}
              onFocus={e => e.target.style.boxShadow = '0 0 0 2px var(--accent)'}
              onBlur={e => e.target.style.boxShadow = 'none'}
              placeholder="Post title..."
              maxLength={255}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1" style={{ color: 'var(--text)' }}>Body</label>
            <textarea
              value={form.body}
              onChange={(e) => setForm({ ...form, body: e.target.value })}
              className="w-full rounded-lg px-3 py-2 text-sm focus:outline-none resize-none"
              style={{ border: '1px solid var(--border)', color: 'var(--text-heading)', background: 'var(--white)' }}
              onFocus={e => e.target.style.boxShadow = '0 0 0 2px var(--accent)'}
              onBlur={e => e.target.style.boxShadow = 'none'}
              placeholder="What's on your mind?"
              rows={4}
              maxLength={255}
              required
            />
          </div>

          <div className="text-xs" style={{ color: 'var(--text-muted)' }}>
            ✨ Your post will be automatically analysed by AI for content moderation.
          </div>

          <div className="flex gap-3 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 text-sm font-medium py-2 rounded-lg transition hover:opacity-80"
              style={{ border: '1px solid var(--border)', color: 'var(--text-muted)', background: 'transparent' }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 text-sm font-medium py-2 rounded-lg transition disabled:opacity-50 hover:opacity-90"
              style={{ background: 'var(--brand)', color: 'var(--white)' }}
            >
              {loading ? 'Posting...' : 'Post'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
