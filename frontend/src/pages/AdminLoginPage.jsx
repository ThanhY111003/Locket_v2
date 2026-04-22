import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ShieldCheck, Lock, User, AlertCircle } from 'lucide-react'
import { login } from '../services/authService'
import './AdminLoginPage.css'

export default function AdminLoginPage() {
  const [form, setForm] = useState({ username: '', password: '' })
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
      
      // Strict role check
      if (userData.role !== 'ROLE_ADMIN') {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        setError('Tài khoản không có quyền truy cập quản trị viên.')
        setLoading(false)
        return
      }

      navigate('/admin')
    } catch (err) {
      setError('Thông tin đăng nhập không chính xác hoặc lỗi hệ thống.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-login-layout">
      <div className="admin-login-container">
        <div className="admin-login-header">
          <ShieldCheck size={48} className="admin-login-icon" />
          <h1>Locket Admin</h1>
          <p>Hệ thống Quản trị & Điều hành</p>
        </div>

        {error && (
          <div className="admin-login-error">
            <AlertCircle size={18} />
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="admin-login-form">
          <div className="admin-input-group">
            <label>Tài khoản Quản trị</label>
            <div className="admin-input-wrapper">
              <User size={18} className="input-icon" />
              <input
                type="text"
                placeholder="Nhập usename quản trị..."
                value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="admin-input-group">
            <label>Mật khẩu Khóa</label>
            <div className="admin-input-wrapper">
              <Lock size={18} className="input-icon" />
              <input
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn-admin-submit"
            disabled={loading}
          >
            {loading ? <div className="spinner-small" /> : 'Trao Quyền Đăng Nhập'}
          </button>
        </form>

        <div className="admin-login-footer">
          Chỉ dành cho cá nhân được cấp quyền hợp lệ.
        </div>
      </div>
    </div>
  )
}
