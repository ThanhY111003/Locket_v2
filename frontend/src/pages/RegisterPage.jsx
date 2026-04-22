import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { User, Mail, Lock, AlertCircle, Wallet } from 'lucide-react'
import { register } from '../services/authService'
import './AuthPage.css'

export default function RegisterPage() {
  const [form, setForm] = useState({ fullName: '', username: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      // Đăng ký thành công → chuyển sang login
      navigate('/login', { state: { message: 'Đăng ký thành công! Hãy đăng nhập.' } })
    } catch (err) {
      const msg = err.response?.data?.message
      if (msg) {
        setError(msg)
      } else if (err.code === 'ERR_NETWORK') {
        setError('Không thể kết nối đến server. Vui lòng kiểm tra backend đang chạy.')
      } else {
        setError('Đăng ký thất bại. Vui lòng thử lại.')
      }
    } finally {
      setLoading(false)
    }
  }

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  return (
    <div className="auth-page">
      <div className="auth-left">
        <div className="auth-brand">
          <div className="auth-brand-icon">
            <Wallet size={32} />
          </div>
          <h1 className="auth-brand-name">Locket Finance</h1>
          <p className="auth-brand-tagline">
            Tham gia cùng hàng ngàn người<br />đang quản lý tài chính thông minh hơn
          </p>
        </div>

        <div className="auth-steps">
          {['Tạo tài khoản miễn phí', 'Chụp ảnh mua sắm của bạn', 'Xem báo cáo chi tiêu ngay', 'Kết nối với bạn bè'].map((step, i) => (
            <div key={i} className="auth-step">
              <div className="auth-step-num">{i + 1}</div>
              <span>{step}</span>
            </div>
          ))}
        </div>

        <div className="auth-bg-glow" />
      </div>

      <div className="auth-right">
        <div className="auth-form-container">
          <div className="auth-form-header">
            <h2>Đăng ký tài khoản</h2>
            <p>Bắt đầu hành trình quản lý tài chính của bạn</p>
          </div>

          {error && (
            <div className="auth-error">
              <AlertCircle size={16} />
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="input-group">
              <label className="input-label">Họ tên đầy đủ</label>
              <div className="input-field-icon">
                <User size={18} className="input-icon" />
                <input className="input-field" type="text" placeholder="Nguyễn Văn An"
                  value={form.fullName} onChange={update('fullName')} required />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Tên đăng nhập</label>
              <div className="input-field-icon">
                <User size={18} className="input-icon" />
                <input className="input-field" type="text" placeholder="nguyenvana"
                  value={form.username} onChange={update('username')} required minLength={3} />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Email</label>
              <div className="input-field-icon">
                <Mail size={18} className="input-icon" />
                <input className="input-field" type="email" placeholder="an@email.com"
                  value={form.email} onChange={update('email')} required />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Mật khẩu</label>
              <div className="input-field-icon">
                <Lock size={18} className="input-icon" />
                <input className="input-field" type="password" placeholder="Ít nhất 6 ký tự"
                  value={form.password} onChange={update('password')} minLength={6} required />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={loading}>
              {loading ? <div className="spinner" /> : '🚀 Tạo tài khoản'}
            </button>
          </form>

          <p className="auth-switch">
            Đã có tài khoản?{' '}
            <Link to="/login" className="auth-link">Đăng nhập</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
