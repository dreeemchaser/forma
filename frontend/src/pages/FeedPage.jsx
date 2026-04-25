import { useState, useEffect } from 'react'
import { getAllPosts, likePost, unlikePost } from '../api/posts'
import { useAuth } from '../context/AuthContext'
import PostCard from '../components/PostCard'
import CreatePostModal from '../components/CreatePostModal'

export default function FeedPage() {
  const { user } = useAuth()
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)

  const fetchPosts = () => {
    getAllPosts()
      .then((res) => setPosts(res.data))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    fetchPosts()
  }, [])

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

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-semibold" style={{ color: 'var(--text-heading)' }}>Feed</h1>
        {user && (
          <button
            onClick={() => setShowModal(true)}
            className="text-sm font-medium px-4 py-2 rounded-lg transition hover:opacity-90"
            style={{ background: 'var(--brand)', color: 'var(--white)' }}
          >
            + New Post
          </button>
        )}
      </div>

      {loading ? (
        <div className="text-center py-16 text-sm" style={{ color: 'var(--text-muted)' }}>Loading posts...</div>
      ) : posts.length === 0 ? (
        <div className="text-center py-16 text-sm" style={{ color: 'var(--text-muted)' }}>No posts yet. Be the first!</div>
      ) : (
        <div className="space-y-4">
          {posts.map((post) => (
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

      {showModal && (
        <CreatePostModal
          onClose={() => setShowModal(false)}
          onCreated={() => { setShowModal(false); fetchPosts() }}
        />
      )}
    </div>
  )
}
