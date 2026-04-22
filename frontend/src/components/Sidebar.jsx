import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { Home, TrendingUp, Upload, Users, LogOut, Wallet } from 'lucide-react'
import { getInitials } from '../data/mockData'
import { logout } from '../services/authService'
import './Sidebar.css'

// Lấy user từ localStorage (lưu lúc login) hoặc fallback
const getUser = () => {
  try {
    const raw = localStorage.getItem('user')
    return raw ? JSON.parse(raw) : { username: 'user', fullName: 'Người dùng' }
  } catch {
    return { username: 'user', fullName: 'Người dùng' }
  }
}

const navItems = [
  { to: '/feed', icon: Home, label: 'Trang chủ' },
  { to: '/report', icon: TrendingUp, label: 'Báo cáo' },
  { to: '/upload', icon: Upload, label: 'Đăng bài' },
  { to: '/friends', icon: Users, label: 'Bạn bè' },
]

export default function Sidebar() {
  const navigate = useNavigate()
  const currentUser = getUser()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <aside className="sidebar">
      {/* Logo */}
      <div className="sidebar-logo">
        <div className="sidebar-logo-icon">
          <Wallet size={20} />
        </div>
        <div>
          <div className="sidebar-logo-title">Locket</div>
          <div className="sidebar-logo-sub">Finance</div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="sidebar-nav">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `sidebar-nav-item ${isActive ? 'active' : ''}`
            }
          >
            <Icon size={20} />
            <span>{label}</span>
            {to === '/upload' && <span className="nav-badge-dot" />}
          </NavLink>
        ))}
      </nav>

      {/* User profile */}
      <div className="sidebar-user">
        <div className="sidebar-user-info">
          <div className="avatar-fallback avatar-sm sidebar-avatar">
            {getInitials(currentUser.fullName)}
          </div>
          <div className="sidebar-user-text">
            <div className="sidebar-user-name">{currentUser.fullName}</div>
            <div className="sidebar-user-sub">@{currentUser.username}</div>
          </div>
        </div>
        <button className="btn btn-icon btn-ghost sidebar-logout" onClick={handleLogout} title="Đăng xuất">
          <LogOut size={16} />
        </button>
      </div>
    </aside>
  )
}
