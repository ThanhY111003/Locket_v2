import React, { useEffect, useState } from 'react'
import { UserPlus, Check, Users, Search, X, UserMinus } from 'lucide-react'
import { getFriends, getPendingRequests, sendFriendRequest,
         acceptFriendRequest, rejectFriendRequest, removeFriend } from '../services/friendService'
import { searchUsers } from '../services/userService'
import { getInitials, formatDate } from '../data/mockData'
import './FriendsPage.css'

export default function FriendsPage() {
  const [friends, setFriends] = useState([])
  const [pending, setPending] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [searchResults, setSearchResults] = useState(null)
  const [searching, setSearching] = useState(false)
  const [sentTo, setSentTo] = useState({})

  useEffect(() => {
    Promise.all([getFriends(), getPendingRequests()])
      .then(([f, p]) => { setFriends(f); setPending(p) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!search.trim()) { setSearchResults(null); return }
    setSearching(true)
    try {
      const res = await searchUsers(search.trim())
      setSearchResults(res)
    } catch { setSearchResults([]) }
    finally { setSearching(false) }
  }

  const handleSendRequest = async (userId) => {
    try {
      await sendFriendRequest(userId)
      setSentTo(s => ({ ...s, [userId]: true }))
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể gửi lời mời kết bạn')
    }
  }

  const handleAccept = async (senderFriendId) => {
    try {
      await acceptFriendRequest(senderFriendId)
      const accepted = pending.find(p => p.friendId === senderFriendId)
      setPending(p => p.filter(r => r.friendId !== senderFriendId))
      if (accepted) {
        setFriends(f => [...f, {
          friend: { id: accepted.friendId, username: accepted.username, fullName: accepted.fullName },
          status: 'ACCEPTED', createdAt: new Date().toISOString()
        }])
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể chấp nhận lời mời')
    }
  }

  const handleReject = async (senderFriendId) => {
    try {
      await rejectFriendRequest(senderFriendId)
      setPending(p => p.filter(r => r.friendId !== senderFriendId))
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể từ chối lời mời')
    }
  }

  const handleRemoveFriend = async (friendId) => {
    if (!window.confirm('Bạn có chắc muốn huỷ kết bạn?')) return
    try {
      await removeFriend(friendId)
      setFriends(f => f.filter(r => r.friend?.id !== friendId))
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể huỷ kết bạn')
    }
  }

  const filteredFriends = friends.filter(f =>
    !search || (f.friend?.fullName ?? '').toLowerCase().includes(search.toLowerCase()) ||
    (f.friend?.username ?? '').toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Bạn bè</h1>
          <p className="page-subtitle">Kết nối và theo dõi chi tiêu cùng nhau</p>
        </div>
        <div className="friends-stats">
          <div className="friends-stat">
            <div className="friends-stat-num">{loading ? '...' : friends.length}</div>
            <div className="friends-stat-label">Bạn bè</div>
          </div>
          {pending.length > 0 && (
            <div className="friends-stat">
              <div className="friends-stat-num" style={{ color: '#f59e0b' }}>{pending.length}</div>
              <div className="friends-stat-label">Lời mời</div>
            </div>
          )}
        </div>
      </div>

      {/* Pending requests */}
      {pending.length > 0 && (
        <div className="friends-section">
          <div className="friends-section-title">🔔 Lời mời kết bạn ({pending.length})</div>
          <div className="friends-list">
            {pending.map(req => (
              <div key={req.friendId} className="friend-card card">
                <div className="friend-info">
                  <div className="avatar-fallback avatar-md friend-avatar">
                    {getInitials(req.fullName)}
                  </div>
                  <div>
                    <div className="friend-name">{req.fullName}</div>
                    <div className="friend-sub">@{req.username} • {formatDate(req.createdAt)}</div>
                  </div>
                </div>
                <div className="friend-actions">
                  <button className="btn btn-primary btn-sm" onClick={() => handleAccept(req.friendId)}>
                    <Check size={14} /> Chấp nhận
                  </button>
                  <button className="btn btn-ghost btn-sm" onClick={() => handleReject(req.friendId)}>Từ chối</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Search & user discovery */}
      <div className="friends-section">
        <div className="friends-section-header">
          <div className="friends-section-title"><Users size={16} />Tìm bạn bè</div>
        </div>
        <form className="friends-search-form" onSubmit={handleSearch}>
          <div className="friends-search input-field-icon" style={{ flex: 1 }}>
            <Search size={16} className="input-icon" />
            <input className="input-field" type="text" placeholder="Tìm theo tên hoặc username..."
              value={search} onChange={e => { setSearch(e.target.value); if (!e.target.value) setSearchResults(null) }}
              style={{ paddingTop: 8, paddingBottom: 8 }} />
            {search && (
              <button type="button" className="input-eye-btn" onClick={() => { setSearch(''); setSearchResults(null) }}>
                <X size={14} />
              </button>
            )}
          </div>
          <button type="submit" className="btn btn-primary btn-sm" disabled={searching}>
            {searching ? <div className="spinner" /> : 'Tìm'}
          </button>
        </form>

        {/* Search results */}
        {searchResults !== null && (
          <div className="friends-list" style={{ marginTop: 12 }}>
            {searchResults.length === 0 ? (
              <div className="empty-state" style={{ padding: '20px 0' }}>
                <div className="empty-icon">🔍</div>
                <div className="empty-title">Không tìm thấy người dùng</div>
              </div>
            ) : (
              searchResults.map(u => (
                <div key={u.id} className="friend-card card">
                  <div className="friend-info">
                    <div className="avatar-fallback avatar-md friend-avatar">{getInitials(u.fullName)}</div>
                    <div>
                      <div className="friend-name">{u.fullName}</div>
                      <div className="friend-sub">@{u.username}</div>
                    </div>
                  </div>
                  <button
                    className={`btn btn-sm ${sentTo[u.id] ? 'btn-ghost' : 'btn-primary'}`}
                    onClick={() => handleSendRequest(u.id)}
                    disabled={sentTo[u.id]}
                  >
                    {sentTo[u.id] ? <><Check size={14} /> Đã gửi</> : <><UserPlus size={14} /> Kết bạn</>}
                  </button>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {/* Friends list */}
      <div className="friends-section">
        <div className="friends-section-title"><Users size={16} />Bạn bè của bạn ({filteredFriends.length})</div>
        {loading ? (
          <div className="friends-grid">
            {[1,2,3,4].map(i => <div key={i} className="card skeleton" style={{ height: 160 }} />)}
          </div>
        ) : filteredFriends.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">👥</div>
            <div className="empty-title">Chưa có bạn bè</div>
            <div className="empty-desc">Tìm kiếm và gửi lời mời kết bạn!</div>
          </div>
        ) : (
          <div className="friends-grid">
            {filteredFriends.map(({ friend, createdAt }) => (
              friend && (
                <div key={friend.id} className="friend-user-card card card-hover">
                  <div className="fuc-avatar avatar-fallback avatar-lg">{getInitials(friend.fullName)}</div>
                  <div className="fuc-name">{friend.fullName}</div>
                  <div className="fuc-sub">@{friend.username}</div>
                  <div className="fuc-since">Bạn bè từ {new Date(createdAt).toLocaleDateString('vi-VN')}</div>
                  <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                    <span className="badge badge-green">✓ Bạn bè</span>
                    <button className="btn btn-ghost btn-xs" onClick={() => handleRemoveFriend(friend.id)}>
                      <UserMinus size={13} />
                    </button>
                  </div>
                </div>
              )
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
