import api from './api'

export const getAdminUsers = async (page = 0, size = 50) => {
  const response = await api.get(`/admin/users?page=${page}&size=${size}`)
  return response.data
}

export const adminDeleteUser = async (userId) => {
  const response = await api.delete(`/admin/users/${userId}`)
  return response.data
}

export const getAdminPosts = async (page = 0, size = 50) => {
  const response = await api.get(`/admin/posts?page=${page}&size=${size}`)
  return response.data
}

export const adminDeletePost = async (postId) => {
  const response = await api.delete(`/admin/posts/${postId}`)
  return response.data
}
