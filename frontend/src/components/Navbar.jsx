import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav style={{ borderBottom: '1px solid var(--border)', background: 'var(--card)', boxShadow: 'var(--shadow)' }}
         className="sticky top-0 z-10">
      <div className="max-w-2xl mx-auto px-4 py-3 flex items-center justify-between">
        <Link to="/" className="flex items-center">
          <img src="/logo.svg" alt="Forma" height="32" style={{ height: '32px', width: 'auto' }} />
        </Link>

        <div className="flex items-center gap-4 text-sm">
          {user ? (
            <>
              <Link to="/" style={{ color: 'var(--text-muted)' }} className="hover:text-gray-900 transition">Feed</Link>
              {user.role === 'MODERATOR' && (
                <Link to="/moderator" style={{ color: 'var(--green)', fontWeight: 600 }} className="transition hover:opacity-80">
                  Moderation
                </Link>
              )}
              <Link to="/profile" style={{ color: 'var(--text-muted)' }} className="hover:text-gray-900 transition">Profile</Link>
              <button
                onClick={handleLogout}
                style={{ color: 'var(--text-muted)' }}
                className="hover:text-red-500 transition"
              >
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ color: 'var(--text-muted)' }} className="hover:text-gray-900 transition">Sign in</Link>
              <Link
                to="/register"
                style={{ background: 'var(--brand)', color: 'var(--white)' }}
                className="px-3 py-1.5 rounded-lg transition hover:opacity-90"
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}
