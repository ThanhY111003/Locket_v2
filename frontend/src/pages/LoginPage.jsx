import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Mail, Lock, Eye, EyeOff, Wallet, AlertCircle } from 'lucide-react'
import { login } from '../services/authService'
import './AuthPage.css'

export default function LoginPage() {
  const [form, setForm] = useState({ username: '', password: '' })
  const [showPass, setShowPass] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await login(form.username, form.password)
      const userData = JSON.parse(localStorage.getItem('user') || '{}');
      
      if (userData.role === 'ROLE_ADMIN') {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        setError('Vui lòng đăng nhập tại cổng Quản Trị Viên (/admin/login)')
        setLoading(false)
        return
      }

      navigate('/feed')
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error
      if (msg) {
        setError(msg)
      } else if (err.code === 'ERR_NETWORK' || err.code === 'ECONNREFUSED') {
        // Backend chưa chạy → demo mode
        setError('⚠️ Backend chưa khởi động. Đang chạy demo mode...')
        setTimeout(() => {
          localStorage.setItem('token', 'demo-token')
          localStorage.setItem('user', JSON.stringify({ username: form.username, fullName: form.username }))
          navigate('/feed')
        }, 1200)
      } else {
        setError('Tên đăng nhập hoặc mật khẩu không đúng.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      {/* Left panel */}
      <div className="auth-left">
        <div className="auth-brand">
          <div className="auth-brand-icon">
            <Wallet size={32} />
          </div>
          <h1 className="auth-brand-name">Locket Finance</h1>
          <p className="auth-brand-tagline">
            Theo dõi chi tiêu<br />qua từng khoảnh khắc
          </p>
        </div>

        <div className="auth-features">
          {[
            { emoji: '📸', title: 'Chụp & chia sẻ', desc: 'Đăng ảnh mua sắm và tự động ghi nhận chi tiêu' },
            { emoji: '🤖', title: 'AI phân loại', desc: 'Gemini AI tự động nhận diện danh mục chi tiêu' },
            { emoji: '📊', title: 'Báo cáo thông minh', desc: 'Phân tích tài chính cá nhân theo tháng' },
            { emoji: '👥', title: 'Mạng xã hội', desc: 'Xem chi tiêu của bạn bè và cùng tiết kiệm' },
          ].map((f, i) => (
            <div key={i} className="auth-feature-item">
              <span className="auth-feature-emoji">{f.emoji}</span>
              <div>
                <div className="auth-feature-title">{f.title}</div>
                <div className="auth-feature-desc">{f.desc}</div>
              </div>
            </div>
          ))}
        </div>

        <div className="auth-bg-glow" />
      </div>

      {/* Right panel */}
      <div className="auth-right">
        <div className="auth-form-container">
          <div className="auth-form-header">
            <h2>Chào mừng trở lại!</h2>
            <p>Đăng nhập để theo dõi chi tiêu của bạn</p>
          </div>

          {/* API Status indicator */}
          <div className="api-status-bar">
            <div className="api-dot" />
            <span>Backend: <code>http://localhost:8080</code></span>
          </div>

          {error && (
            <div className="auth-error">
              <AlertCircle size={16} />
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="input-group">
              <label className="input-label">Tên đăng nhập</label>
              <div className="input-field-icon">
                <Mail size={18} className="input-icon" />
                <input
                  id="login-username"
                  className="input-field"
                  type="text"
                  placeholder="username"
                  value={form.username}
                  onChange={e => setForm({ ...form, username: e.target.value })}
                  required
                  autoComplete="username"
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Mật khẩu</label>
              <div className="input-field-icon">
                <Lock size={18} className="input-icon" />
                <input
                  id="login-password"
                  className="input-field"
                  type={showPass ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={e => setForm({ ...form, password: e.target.value })}
                  required
                  autoComplete="current-password"
                  style={{ paddingRight: '44px' }}
                />
                <button
                  type="button"
                  className="input-eye-btn"
                  onClick={() => setShowPass(s => !s)}
                >
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              id="login-submit"
              type="submit"
              className="btn btn-primary btn-lg btn-block"
              disabled={loading}
            >
              {loading ? <div className="spinner" /> : 'Đăng nhập'}
            </button>
          </form>

          <div className="auth-divider" style={{ display: 'flex', alignItems: 'center', margin: '24px 0', color: 'var(--text-muted)', fontSize: '13px' }}>
            <span style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
            <span style={{ padding: '0 12px' }}>HOẶC</span>
            <span style={{ flex: 1, height: '1px', background: 'var(--border)' }} />
          </div>

          <button 
            className="btn btn-outline btn-block" 
            onClick={() => window.location.href = 'http://localhost:8080/oauth2/authorization/google'}
            style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'center', marginBottom: '24px', backgroundColor: 'var(--bg-tertiary)', border: '1px solid var(--border)', width: '100%' }}
            type="button"
          >
            <img src="https://www.google.com/favicon.ico" alt="Google" width="18" height="18" />
            Tiếp tục với Google
          </button>

          <p className="auth-switch">
            Chưa có tài khoản?{' '}
            <Link to="/register" className="auth-link">Đăng ký ngay</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
