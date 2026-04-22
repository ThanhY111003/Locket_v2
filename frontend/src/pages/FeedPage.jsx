import React, { useEffect, useState, useCallback } from 'react'
import { Sparkles, TrendingUp, Clock, Search, X } from 'lucide-react'
import PostCard from '../components/PostCard'
import { getFeed } from '../services/feedService'
import { getMonthlyReport } from '../services/reportService'
import { searchPosts } from '../services/postService'
import { formatCurrency } from '../data/mockData'
import './FeedPage.css'

export default function FeedPage() {
  const [posts, setPosts] = useState([])
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [searching, setSearching] = useState(false)
  const [searchResults, setSearchResults] = useState(null) // null = not searched yet

  const storedUser = localStorage.getItem('user')
  const user = storedUser ? JSON.parse(storedUser) : { fullName: 'Bạn' }
  const firstName = (user.fullName || 'Bạn').split(' ').at(-1)

  useEffect(() => {
    const now = new Date()
    const fetchData = async () => {
      setLoading(true)
      try {
        const [feedData, reportData] = await Promise.all([
          getFeed(0, 20),
          getMonthlyReport(now.getFullYear(), now.getMonth() + 1),
        ])
        setPosts(feedData)
        setReport(reportData)
      } catch (err) {
        console.error('Feed load error:', err)
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [])

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!searchQuery.trim()) { setSearchResults(null); return }
    setSearching(true)
    try {
      const results = await searchPosts(searchQuery.trim())
      setSearchResults(results)
    } catch {
      setSearchResults([])
    } finally {
      setSearching(false)
    }
  }

  const clearSearch = () => {
    setSearchQuery('')
    setSearchResults(null)
  }

  const handlePostDeleted = useCallback((postId) => {
    setPosts(p => p.filter(post => post.id !== postId))
    setSearchResults(r => r ? r.filter(post => post.id !== postId) : null)
  }, [])

  const displayPosts = searchResults !== null ? searchResults : posts
  const totalThisMonth = report?.totalSpending ?? 0
  const now = new Date()

  return (
    <div className="page-container feed-page">
      {/* Header */}
      <div className="feed-header">
        <div>
          <h1 className="page-title">Xin chào, {firstName} 👋</h1>
          <p className="page-subtitle">Hôm nay bạn đã chi tiêu những gì?</p>
        </div>
        <div className="feed-month-badge">
          <div className="feed-month-label">
            <TrendingUp size={14} />
            Tháng {now.getMonth() + 1}/{now.getFullYear()}
          </div>
          <div className="feed-month-amount">{report ? formatCurrency(totalThisMonth) : '...'}</div>
        </div>
      </div>

      {/* Stat cards */}
      {report && (
        <div className="stat-grid">
          {[
            { label: 'Tổng chi tháng này', value: formatCurrency(totalThisMonth), sub: `${report.transactions?.length ?? 0} giao dịch`, icon: '💸' },
            {
              label: 'Danh mục nhiều nhất',
              value: Object.entries(report.spendingByCategory ?? {}).sort((a, b) => b[1] - a[1])[0]?.[0] ?? '—',
              sub: formatCurrency(Object.entries(report.spendingByCategory ?? {}).sort((a, b) => b[1] - a[1])[0]?.[1] ?? 0),
              icon: '🍜'
            },
            { label: 'Bài đăng trong feed', value: `${posts.length}`, sub: 'từ bạn bè', icon: '📸' },
            { label: 'Danh mục', value: `${Object.keys(report.spendingByCategory ?? {}).length}`, sub: 'loại chi tiêu', icon: '📊' },
          ].map((s, i) => (
            <div key={i} className="stat-card fade-in" style={{ animationDelay: `${i * 60}ms` }}>
              <div style={{ fontSize: 22, marginBottom: 8 }}>{s.icon}</div>
              <div className="stat-label">{s.label}</div>
              <div className="stat-value" style={{ fontSize: 18 }}>{s.value}</div>
              <div className="stat-sub">{s.sub}</div>
            </div>
          ))}
        </div>
      )}

      {/* AI Banner */}
      {report?.spendingByCategory && Object.keys(report.spendingByCategory).length > 0 && (
        <div className="feed-ai-banner">
          <div className="feed-ai-icon"><Sparkles size={18} /></div>
          <div className="feed-ai-text">
            <div className="feed-ai-title">Locket AI</div>
            <div className="feed-ai-desc">
              Tháng này bạn chi cho{' '}
              <strong>{Object.entries(report.spendingByCategory).sort((a, b) => b[1] - a[1])[0]?.[0]}</strong>{' '}
              nhiều nhất. Kiểm tra báo cáo chi tiết để quản lý ngân sách tốt hơn!
            </div>
          </div>
        </div>
      )}

      {/* Search bar */}
      <form className="feed-search-bar" onSubmit={handleSearch}>
        <div className="input-field-icon" style={{ flex: 1 }}>
          <Search size={16} className="input-icon" />
          <input
            className="input-field"
            placeholder="Tìm bài đăng (caption, ghi chú)..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <button type="button" className="input-eye-btn" onClick={clearSearch}>
              <X size={14} />
            </button>
          )}
        </div>
        <button type="submit" className="btn btn-primary" disabled={searching}>
          {searching ? <div className="spinner" /> : 'Tìm'}
        </button>
      </form>

      {/* Section label */}
      <div className="feed-section-header">
        <div className="feed-section-title">
          <Clock size={16} />
          {searchResults !== null ? `Kết quả tìm kiếm "${searchQuery}"` : 'Bài đăng từ bạn bè & bạn'}
        </div>
        <span className="badge badge-purple">{displayPosts.length} bài</span>
      </div>

      {/* Posts */}
      {loading ? (
        <div className="feed-posts">
          {[1,2,3].map(i => (
            <div key={i} className="card skeleton" style={{ height: 440, marginBottom: 20 }} />
          ))}
        </div>
      ) : displayPosts.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">{searchResults !== null ? '🔍' : '📭'}</div>
          <div className="empty-title">
            {searchResults !== null ? 'Không tìm thấy bài đăng nào' : 'Chưa có bài đăng nào'}
          </div>
          <div className="empty-desc">
            {searchResults !== null ? 'Hãy thử từ khoá khác' : 'Kết bạn với mọi người hoặc đăng bài đầu tiên!'}
          </div>
        </div>
      ) : (
        <div className="feed-posts">
          {displayPosts.map((post, i) => (
            <PostCard key={post.id} post={post} delay={i} onDelete={handlePostDeleted} />
          ))}
        </div>
      )}
    </div>
  )
}
