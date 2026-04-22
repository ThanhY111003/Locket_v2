import api from './api'

// GET /api/users/me
export const getMe = async () => {
  const { data } = await api.get('/users/me')
  return data
}

// GET /api/users/search?q=keyword
export const searchUsers = async (q) => {
  const { data } = await api.get('/users/search', { params: { q } })
  return data
}
