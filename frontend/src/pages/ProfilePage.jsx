import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import { getAllPosts, likePost, unlikePost } from '../api/posts'
import PostCard from '../components/PostCard'

export default function ProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [myPosts, setMyPosts] = useState([])
  const [totalLikes, setTotalLikes] = useState(0)
  const [loading, setLoading] = useState(true)

  const fetchPosts = async () => {
    try {
      const res = await getAllPosts()
      const all = res.data
      const mine = all.filter(p => p.authorUsername === user.username)
      setMyPosts(mine)
      setTotalLikes(mine.reduce((sum, p) => sum + p.likeCount, 0))
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (user) fetchPosts()
  }, [user])

  const handleLike = async (post) => {
    try {
      if (post.liked) {
        await unlikePost(post.id)
      } else {
        await likePost(post.id)
      }
      fetchPosts()
    } catch (err) {
      console.error(err)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (!user) return null

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">

      {/* Profile card */}
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

        {/* Stats */}
        <div className="grid grid-cols-2 gap-4 mb-6">
          <div className="rounded-xl p-4 text-center" style={{ background: 'var(--bg)', border: '1px solid var(--border)' }}>
            <p className="text-2xl font-bold" style={{ color: 'var(--brand)' }}>{myPosts.length}</p>
            <p className="text-xs mt-1" style={{ color: 'var(--text-muted)' }}>Posts</p>
          </div>
          <div className="rounded-xl p-4 text-center" style={{ background: 'var(--bg)', border: '1px solid var(--border)' }}>
            <p className="text-2xl font-bold" style={{ color: 'var(--brand)' }}>{totalLikes}</p>
            <p className="text-xs mt-1" style={{ color: 'var(--text-muted)' }}>Likes received</p>
          </div>
        </div>

        <button
          onClick={handleLogout}
          className="w-full text-sm font-medium py-2 rounded-lg transition hover:opacity-80"
          style={{ border: '1px solid #fca5a5', color: '#ef4444', background: 'transparent' }}
        >
          Sign out
        </button>
      </div>

      {/* User's posts */}
      <div>
        <h2 className="text-sm font-semibold mb-3" style={{ color: 'var(--text-muted)' }}>
          YOUR POSTS
        </h2>
        {loading ? (
          <p className="text-sm" style={{ color: 'var(--text-muted)' }}>Loading...</p>
        ) : myPosts.length === 0 ? (
          <div className="rounded-2xl p-8 text-center" style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
            <p className="text-sm" style={{ color: 'var(--text-muted)' }}>You haven't posted anything yet.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {myPosts.map(post => (
              <PostCard
                key={post.id}
                post={post}
                currentUser={user}
                onLike={handleLike}
                onPostUpdated={fetchPosts}
              />
            ))}
          </div>
        )}
      </div>

    </div>
  )
}
