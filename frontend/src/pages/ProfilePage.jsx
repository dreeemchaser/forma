import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

export default function ProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (!user) return null

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="rounded-2xl p-8" style={{ background: 'var(--card)', border: '1px solid var(--border)', boxShadow: 'var(--shadow)' }}>
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 rounded-full flex items-center justify-center text-2xl font-bold"
               style={{ background: 'var(--accent-light)', color: 'var(--brand)' }}>
            {user.username[0].toUpperCase()}
          </div>
          <div>
            <h1 className="text-xl font-semibold" style={{ color: 'var(--text-heading)' }}>{user.username}</h1>
            <span className="text-xs font-medium px-2 py-0.5 rounded-full"
                  style={user.role === 'MODERATOR'
                    ? { background: 'var(--green-light)', color: 'var(--green)' }
                    : { background: 'var(--accent-light)', color: 'var(--accent)' }}>
              {user.role === 'MODERATOR' ? '🛡️ Moderator' : 'Regular User'}
            </span>
          </div>
        </div>

        <div className="pt-6 space-y-3" style={{ borderTop: '1px solid var(--border)' }}>
          <div className="flex justify-between text-sm">
            <span style={{ color: 'var(--text-muted)' }}>User ID</span>
            <span className="font-mono text-xs" style={{ color: 'var(--text)' }}>{user.id}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span style={{ color: 'var(--text-muted)' }}>Role</span>
            <span style={{ color: 'var(--text)' }}>{user.role}</span>
          </div>
        </div>

        <div className="mt-8">
          <button
            onClick={handleLogout}
            className="w-full text-sm font-medium py-2 rounded-lg transition hover:opacity-80"
            style={{ border: '1px solid #fca5a5', color: '#ef4444', background: 'transparent' }}
          >
            Sign out
          </button>
        </div>
      </div>
    </div>
  )
}
