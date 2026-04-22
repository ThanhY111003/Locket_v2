import api from './api'

// GET /api/friends
export const getFriends = async () => {
  const { data } = await api.get('/friends')
  return data
}

// GET /api/friends/pending
export const getPendingRequests = async () => {
  const { data } = await api.get('/friends/pending')
  return data
}

// POST /api/friends/request
export const sendFriendRequest = async (userId) => {
  const { data } = await api.post('/friends/request', { userId })
  return data
}

// PUT /api/friends/accept/:senderUserId
export const acceptFriendRequest = async (senderUserId) => {
  const { data } = await api.put(`/friends/accept/${senderUserId}`)
  return data
}

// PUT /api/friends/reject/:senderUserId
export const rejectFriendRequest = async (senderUserId) => {
  const { data } = await api.put(`/friends/reject/${senderUserId}`)
  return data
}

// DELETE /api/friends/:friendId
export const removeFriend = async (friendId) => {
  const { data } = await api.delete(`/friends/${friendId}`)
  return data
}
