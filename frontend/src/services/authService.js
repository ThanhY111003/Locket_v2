import api from './api'

/**
 * POST /api/auth/register
 */
export const register = async ({ username, password, email, fullName }) => {
  const { data } = await api.post('/auth/register', { username, password, email, fullName })
  return data
}

/**
 * POST /api/auth/login
 * Lưu token vào localStorage sau khi đăng nhập thành công
 * @returns {{ token, tokenType, username, fullName, profilePictureUrl }}
 */
export const login = async (username, password) => {
  const response = await api.post('/auth/login', { username, password })
  const { token, fullName, profilePictureUrl, role } = response.data
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify({
    username,
    fullName,
    profilePictureUrl,
    role
  }))
  return response.data
}

export const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export const getCurrentUser = () => {
  const raw = localStorage.getItem('user')
  return raw ? JSON.parse(raw) : null
}

export const isAuthenticated = () => !!localStorage.getItem('token')
