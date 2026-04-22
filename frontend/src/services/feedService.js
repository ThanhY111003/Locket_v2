import api from './api'

// GET /api/feed
export const getFeed = async (page = 0, size = 20) => {
  const { data } = await api.get('/feed', { params: { page, size } })
  return data.content ?? data
}
