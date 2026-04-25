import api from './axios'

export const getFlaggedPosts = () =>
  api.get('/moderator')

export const flagPost = (id) =>
  api.post(`/posts/${id}/flag`)

export const unflagPost = (id) =>
  api.delete(`/posts/${id}/flag`)
