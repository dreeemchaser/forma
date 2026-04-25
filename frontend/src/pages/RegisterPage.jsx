import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register as registerApi } from '../api/auth'
import { useAuth } from '../context/AuthContext'

export default function RegisterPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await registerApi(form.username, form.password)
      await login(res.data.token)
      navigate('/')
    } catch (err) {
      setError(err.response?.data || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center" style={{ background: 'var(--bg)' }}>
      <div className="rounded-2xl p-8 w-full max-w-md" style={{ background: 'var(--card)', boxShadow: 'var(--shadow)' }}>
        <div className="mb-6">
          <img src="/logo.svg" alt="Forma" style={{ height: '36px', width: 'auto', marginBottom: '4px' }} />
          <h1 className="text-xl font-semibold mb-1" style={{ color: 'var(--text-heading)' }}>Create an account</h1>
          <p className="text-sm" style={{ color: 'var(--text-muted)' }}>Join Forma today</p>
        </div>

        {error && (
          <div className="text-sm rounded-lg px-4 py-3 mb-4" style={{ background: '#fef2f2', color: '#dc2626' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1" style={{ color: 'var(--text)' }}>Username</label>
            <input
              type="text"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              className="w-full rounded-lg px-3 py-2 text-sm focus:outline-none"
              style={{ border: '1px solid var(--border)', color: 'var(--text-heading)', background: 'var(--white)' }}
              onFocus={e => e.target.style.boxShadow = '0 0 0 2px var(--accent)'}
              onBlur={e => e.target.style.boxShadow = 'none'}
              placeholder="3–20 characters"
              minLength={3}
              maxLength={20}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1" style={{ color: 'var(--text)' }}>Password</label>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              className="w-full rounded-lg px-3 py-2 text-sm focus:outline-none"
              style={{ border: '1px solid var(--border)', color: 'var(--text-heading)', background: 'var(--white)' }}
              onFocus={e => e.target.style.boxShadow = '0 0 0 2px var(--accent)'}
              onBlur={e => e.target.style.boxShadow = 'none'}
              placeholder="8–16 characters"
              minLength={8}
              maxLength={16}
              required
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full font-medium py-2 rounded-lg text-sm transition disabled:opacity-50"
            style={{ background: 'var(--brand)', color: 'var(--white)' }}
            onMouseEnter={e => e.target.style.background = 'var(--brand-hover)'}
            onMouseLeave={e => e.target.style.background = 'var(--brand)'}
          >
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="text-sm text-center mt-6" style={{ color: 'var(--text-muted)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--accent)', fontWeight: 600 }} className="hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
