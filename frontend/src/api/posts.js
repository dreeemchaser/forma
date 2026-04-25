import api from './axios'

export const getAllPosts = () =>
  api.get('/posts')

export const getPostById = (id) =>
  api.get(`/posts/${id}`)

export const createPost = (title, body) =>
  api.post('/posts', { title, body })

export const likePost = (id) =>
  api.post(`/posts/${id}/like`)

export const unlikePost = (id) =>
  api.delete(`/posts/${id}/like`)

export const getComments = (id) =>
  api.get(`/posts/${id}/comments`)

export const addComment = (id, body) =>
  api.post(`/posts/${id}/comments`, { body })
