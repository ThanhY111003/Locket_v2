import api from './api'

// POST /api/posts (multipart)
export const createPost = async ({ image, amount, categoryId, caption, transactionDate, notes }) => {
  const formData = new FormData()
  formData.append('image', image)
  formData.append('amount', amount)
  formData.append('transactionDate', transactionDate)
  if (categoryId) formData.append('categoryId', categoryId)
  if (caption)    formData.append('caption', caption)
  if (notes)      formData.append('notes', notes)
  const { data } = await api.post('/posts', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

// DELETE /api/posts/:postId
export const deletePost = async (postId) => {
  const { data } = await api.delete(`/posts/${postId}`)
  return data
}

// GET /api/posts/search?q=keyword
export const searchPosts = async (q, page = 0, size = 20) => {
  const { data } = await api.get('/posts/search', { params: { q, page, size } })
  return data.content ?? data
}

// GET /api/posts/me
export const getMyPosts = async () => {
  const { data } = await api.get('/posts/me')
  return data
}

// POST /api/posts/:postId/like  (toggle)
export const toggleLike = async (postId) => {
  const { data } = await api.post(`/posts/${postId}/like`)
  return data   // { liked: boolean, likeCount: number }
}

// GET /api/posts/:postId/comments
export const getComments = async (postId) => {
  const { data } = await api.get(`/posts/${postId}/comments`)
  return data
}

// POST /api/posts/:postId/comments
export const addComment = async (postId, content) => {
  const { data } = await api.post(`/posts/${postId}/comments`, { content })
  return data
}

// DELETE /api/posts/comments/:commentId
export const deleteComment = async (commentId) => {
  const { data } = await api.delete(`/posts/comments/${commentId}`)
  return data
}
