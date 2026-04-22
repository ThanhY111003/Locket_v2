import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { 
  Users, Image as ImageIcon, Trash2, Search, LayoutDashboard,
  LogOut, ShieldCheck, UserCog
} from 'lucide-react'
import { getAdminUsers, adminDeleteUser, getAdminPosts, adminDeletePost } from '../services/adminService'
import { formatDate } from '../data/mockData'
import './AdminPage.css'

export default function AdminPage() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('users') // 'users' or 'posts'
  const [users, setUsers] = useState([])
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')

  useEffect(() => {
    // Basic UI auth check
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role !== 'ROLE_ADMIN' && user.username !== 'admin') {
      navigate('/feed')
      return
    }
    fetchData()
  }, [activeTab])

  const fetchData = async () => {
    setLoading(true)
    try {
      if (activeTab === 'users') {
        const res = await getAdminUsers()
        setUsers(res.content)
      } else {
        const res = await getAdminPosts()
        setPosts(res.content)
      }
    } catch (error) {
      console.error('Lỗi tải dữ liệu Admin:', error)
      alert("Bạn không có quyền hoặc phiên đăng nhập đã hết hạn!")
      navigate('/login')
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteUser = async (id, name) => {
    if (window.confirm(`⚠️ BẠN CÓ CHẮC MUỐN XOÁ TÀI KHOẢN "${name}"?\nToàn bộ bài viết, bình luận và dữ liệu của người này sẽ bị xoá vĩnh viễn!`)) {
      try {
        await adminDeleteUser(id)
        setUsers(users.filter(u => u.id !== id))
      } catch (e) {
        alert("Xoá thất bại!")
      }
    }
  }

  const handleDeletePost = async (id) => {
    if (window.confirm('Xoá bài đăng này vĩnh viễn?')) {
      try {
        await adminDeletePost(id)
        setPosts(posts.filter(p => p.id !== id))
      } catch (e) {
        alert("Xoá thất bại!")
      }
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    navigate('/login')
  }

  // Filter logic
  const filteredUsers = users.filter(u => 
    (u.username || '').toLowerCase().includes(search.toLowerCase()) || 
    (u.fullName || '').toLowerCase().includes(search.toLowerCase())
  )

  const filteredPosts = posts.filter(p => 
    (p.caption || '').toLowerCase().includes(search.toLowerCase()) || 
    (p.user?.username || '').toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="admin-layout">
      {/* Sidebar */}
      <div className="admin-sidebar">
        <div className="admin-brand">
          <ShieldCheck size={28} style={{ color: 'var(--primary)' }} />
          <h2>Locket Admin</h2>
        </div>

        <div className="admin-nav">
          <button 
            className={`admin-nav-item ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            <Users size={20} />
            Quản lý Người dùng
          </button>
          <button 
            className={`admin-nav-item ${activeTab === 'posts' ? 'active' : ''}`}
            onClick={() => setActiveTab('posts')}
          >
            <ImageIcon size={20} />
            Quản lý Bài đăng
          </button>
        </div>

        <div className="admin-bottom-nav">
          <button className="admin-nav-item text-danger" onClick={handleLogout}>
            <LogOut size={20} />
            Đăng xuất
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="admin-main">
        <header className="admin-header">
          <div className="admin-header-title">
            <LayoutDashboard size={24} />
            <h1>{activeTab === 'users' ? 'Danh sách tài khoản' : 'Tất cả bài đăng'}</h1>
          </div>
          
          <div className="admin-search">
            <Search size={18} className="search-icon" />
            <input 
              type="text" 
              placeholder={`Tìm kiếm ${activeTab === 'users' ? 'người dùng' : 'bài đăng'}...`}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </header>

        <div className="admin-content-area">
          {loading ? (
            <div className="admin-loading">
              <div className="spinner"></div>
              <p>Đang tải dữ liệu...</p>
            </div>
          ) : (
            <div className="admin-table-wrapper">
              {activeTab === 'users' ? (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Avatar</th>
                      <th>Username</th>
                      <th>Họ Tên</th>
                      <th>Email</th>
                      <th>Ngày tham gia</th>
                      <th>Nguồn</th>
                      <th>Role</th>
                      <th>Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredUsers.map(user => (
                      <tr key={user.id}>
                        <td>
                          <img src={user.profilePictureUrl || 'https://i.pravatar.cc/150'} alt="avt" className="admin-table-avt" />
                        </td>
                        <td style={{ fontWeight: 'bold' }}>{user.username}</td>
                        <td>{user.fullName}</td>
                        <td>{user.email}</td>
                        <td>{formatDate(user.createdAt)}</td>
                        <td>
                          <span className={`badge ${user.authProvider === 'GOOGLE' ? 'badge-google' : 'badge-local'}`}>
                            {user.authProvider || 'LOCAL'}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${user.role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-user'}`}>
                            {user.role}
                          </span>
                        </td>
                        <td>
                          {user.role !== 'ROLE_ADMIN' && (
                            <button 
                              className="btn-icon btn-danger" 
                              onClick={() => handleDeleteUser(user.id, user.username)}
                              title="Xoá người dùng"
                            >
                              <Trash2 size={16} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                    {filteredUsers.length === 0 && (
                      <tr><td colSpan={8} className="text-center">Không tìm thấy người dùng.</td></tr>
                    )}
                  </tbody>
                </table>
              ) : (
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Ảnh</th>
                      <th>Tác giả</th>
                      <th>Nội dung (Caption)</th>
                      <th>Ngày đăng</th>
                      <th>Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPosts.map(post => (
                      <tr key={post.id}>
                        <td>
                          <img src={post.imageUrl} alt="post" className="admin-table-post-img" />
                        </td>
                        <td style={{ fontWeight: 'bold' }}>{post.user?.username}</td>
                        <td style={{ maxWidth: 300, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {post.caption}
                        </td>
                        <td>{formatDate(post.createdAt)}</td>
                        <td>
                          <button 
                            className="btn-icon btn-danger" 
                            onClick={() => handleDeletePost(post.id)}
                            title="Xoá bài viết"
                          >
                            <Trash2 size={16} />
                          </button>
                        </td>
                      </tr>
                    ))}
                    {filteredPosts.length === 0 && (
                      <tr><td colSpan={5} className="text-center">Không tìm thấy bài post.</td></tr>
                    )}
                  </tbody>
                </table>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
