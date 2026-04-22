import React, { useState } from 'react'
import { Heart, MessageCircle, Trash2, Send, X } from 'lucide-react'
import { formatCurrency, formatDate, getInitials, getCategoryByName } from '../data/mockData'
import { toggleLike, getComments, addComment, deleteComment, deletePost } from '../services/postService'
import './PostCard.css'

export default function PostCard({ post: initialPost, delay = 0, onDelete }) {
  const [post, setPost] = useState(initialPost)
  const [imgLoaded, setImgLoaded] = useState(false)
  const [showComments, setShowComments] = useState(false)
  const [comments, setComments] = useState(null)   // null = chưa load
  const [commentInput, setCommentInput] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const category = getCategoryByName(post.transaction?.categoryName)

  // Lấy user hiện tại từ localStorage
  const currentUsername = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}').username || '' } catch { return '' }
  })()
  const isMyPost = post.user?.username === currentUsername

  // ===== Like =====
  const handleLike = async () => {
    try {
      const result = await toggleLike(post.id)
      setPost(p => ({ ...p, likedByMe: result.liked, likeCount: result.likeCount }))
    } catch (e) {
      console.error('Like error:', e)
    }
  }

  // ===== Comments =====
  const handleOpenComments = async () => {
    setShowComments(true)
    if (comments === null) {
      try {
        const data = await getComments(post.id)
        setComments(data)
        setPost(p => ({ ...p, commentCount: data.length }))
      } catch {
        setComments([])
      }
    }
  }

  const handleAddComment = async (e) => {
    e.preventDefault()
    if (!commentInput.trim()) return
    setSubmitting(true)
    try {
      const comment = await addComment(post.id, commentInput.trim())
      setComments(c => [...(c || []), comment])
      setPost(p => ({ ...p, commentCount: (p.commentCount || 0) + 1 }))
      setCommentInput('')
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể thêm comment')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeleteComment = async (commentId) => {
    try {
      await deleteComment(commentId)
      setComments(c => c.filter(cm => cm.id !== commentId))
      setPost(p => ({ ...p, commentCount: Math.max((p.commentCount || 1) - 1, 0) }))
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xoá comment')
    }
  }

  // ===== Delete Post =====
  const handleDeletePost = async () => {
    if (!window.confirm('Bạn có chắc muốn xoá bài đăng này?')) return
    try {
      await deletePost(post.id)
      onDelete?.(post.id)
    } catch (err) {
      alert(err.response?.data?.message || 'Không thể xoá bài đăng')
    }
  }

  return (
    <article className="post-card card fade-in" style={{ animationDelay: `${delay * 80}ms` }}>
      {/* Header */}
      <div className="post-header">
        <div className="post-user">
          <div className="avatar-fallback avatar-md post-avatar">
            {getInitials(post.user?.fullName)}
          </div>
          <div>
            <div className="post-user-name">{post.user?.fullName}</div>
            <div className="post-user-time">{formatDate(post.createdAt)}</div>
          </div>
        </div>
        <div className="post-actions-top">
          {post.transaction?.categoryName && (
            <span className={`badge ${category.badge}`}>
              {category.icon} {post.transaction.categoryName}
            </span>
          )}
          {isMyPost && (
            <button className="btn btn-icon btn-ghost post-more" onClick={handleDeletePost} title="Xoá bài">
              <Trash2 size={15} style={{ color: '#ef4444' }} />
            </button>
          )}
        </div>
      </div>

      {/* Image */}
      <div className="post-image-wrap">
        {!imgLoaded && <div className="skeleton post-image-skeleton" />}
        <img
          src={post.imageUrl}
          alt={post.caption || 'Expense photo'}
          className={`post-image ${imgLoaded ? 'loaded' : ''}`}
          onLoad={() => setImgLoaded(true)}
          loading="lazy"
        />
        {post.transaction && (
          <div className="post-amount-badge">{formatCurrency(post.transaction.amount)}</div>
        )}
      </div>

      {/* Body */}
      <div className="post-body">
        {post.caption && <p className="post-caption">{post.caption}</p>}
        {post.transaction?.notes && (
          <p className="post-notes">📝 {post.transaction.notes}</p>
        )}

        {/* Interaction bar */}
        <div className="post-interactions">
          <div className="post-btns">
            <button
              className={`post-action-btn ${post.likedByMe ? 'liked' : ''}`}
              onClick={handleLike}
              title={post.likedByMe ? 'Bỏ thích' : 'Thích'}
            >
              <Heart size={18} fill={post.likedByMe ? 'currentColor' : 'none'} />
              <span>{post.likeCount || 0}</span>
            </button>
            <button className="post-action-btn" onClick={handleOpenComments} title="Bình luận">
              <MessageCircle size={18} />
              <span>{post.commentCount || 0}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Comment Drawer */}
      {showComments && (
        <div className="comment-drawer">
          <div className="comment-drawer-header">
            <span>Bình luận ({post.commentCount || 0})</span>
            <button className="btn btn-icon btn-ghost" onClick={() => setShowComments(false)}>
              <X size={16} />
            </button>
          </div>

          <div className="comment-list">
            {comments === null ? (
              <div className="comment-loading">Đang tải...</div>
            ) : comments.length === 0 ? (
              <div className="comment-empty">Chưa có bình luận nào. Hãy là người đầu tiên!</div>
            ) : (
              comments.map(cm => (
                <div key={cm.id} className="comment-item">
                  <div className="avatar-fallback avatar-xs comment-avatar">
                    {getInitials(cm.user?.fullName)}
                  </div>
                  <div className="comment-content">
                    <div className="comment-author">{cm.user?.fullName}</div>
                    <div className="comment-text">{cm.content}</div>
                    <div className="comment-time">{formatDate(cm.createdAt)}</div>
                  </div>
                  {cm.user?.username === currentUsername && (
                    <button className="comment-delete-btn" onClick={() => handleDeleteComment(cm.id)}>
                      <Trash2 size={12} />
                    </button>
                  )}
                </div>
              ))
            )}
          </div>

          <form className="comment-form" onSubmit={handleAddComment}>
            <input
              className="input-field comment-input"
              placeholder="Viết bình luận..."
              value={commentInput}
              onChange={e => setCommentInput(e.target.value)}
              disabled={submitting}
            />
            <button type="submit" className="btn btn-icon btn-primary" disabled={submitting || !commentInput.trim()}>
              <Send size={15} />
            </button>
          </form>
        </div>
      )}
    </article>
  )
}
