import React, { useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Loader2 } from 'lucide-react'

export default function OAuth2CallbackPage() {
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    // URL format: /oauth2/callback?token=...&username=...&fullName=...&avatar=...
    const urlParams = new URLSearchParams(location.search)
    const token = urlParams.get('token')
    const username = urlParams.get('username')
    const fullName = urlParams.get('fullName')
    const profilePictureUrl = urlParams.get('avatar')
    const role = urlParams.get('role')

    if (token) {
      // Save data like normal login
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify({
        username,
        fullName,
        profilePictureUrl,
        role
      }))
      
      setTimeout(() => {
        if (role === 'ROLE_ADMIN') {
          navigate('/admin')
        } else {
          navigate('/feed')
        }
      }, 100)
    } else {
      console.error('No token found in OAuth2 callback')
      navigate('/login')
    }
  }, [location, navigate])

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', background: 'var(--bg-primary)' }}>
      <Loader2 className="spinner" size={32} style={{ color: 'var(--primary)', marginBottom: 16, animation: 'spin 1s linear infinite' }} />
      <div style={{ color: 'var(--text-secondary)', fontSize: 14 }}>Đang đăng nhập...</div>
    </div>
  )
}
