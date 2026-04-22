import React, { useState, useRef } from 'react'
import { Upload, Camera, Sparkles, X, CheckCircle, AlertCircle } from 'lucide-react'
import { categories, formatCurrency } from '../data/mockData'
import { createPost } from '../services/postService'
import './UploadPage.css'

export default function UploadPage() {
  const [preview, setPreview] = useState(null)
  const [file, setFile] = useState(null)
  const [form, setForm] = useState({
    amount: '', categoryId: '', caption: '',
    transactionDate: new Date().toISOString().slice(0, 10), notes: ''
  })
  const [aiSuggestion, setAiSuggestion] = useState(null)
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')
  const fileRef = useRef()

  const handleFile = (f) => {
    setFile(f)
    setPreview(URL.createObjectURL(f))
    setAiSuggestion(null)
    // Simulate Gemini AI suggestion delay
    setTimeout(() => {
      const cat = categories[Math.floor(Math.random() * 4)]
      setAiSuggestion(cat)
    }, 1200)
  }

  const handleDrop = (e) => {
    e.preventDefault()
    const f = e.dataTransfer.files?.[0]
    if (f) handleFile(f)
  }

  const acceptSuggestion = () => {
    setForm(f => ({ ...f, categoryId: aiSuggestion.id.toString() }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!file) return
    setError('')
    setLoading(true)

    try {
      await createPost({
        image: file,
        amount: Number(form.amount),
        categoryId: form.categoryId ? Number(form.categoryId) : null,
        caption: form.caption || null,
        transactionDate: form.transactionDate,
        notes: form.notes || null,
      })
      setSuccess(true)
      setTimeout(() => {
        setSuccess(false)
        setPreview(null)
        setFile(null)
        setAiSuggestion(null)
        setForm({ amount: '', categoryId: '', caption: '', transactionDate: new Date().toISOString().slice(0, 10), notes: '' })
      }, 2500)
    } catch (err) {
      if (err.code === 'ERR_NETWORK') {
        setError('Không thể kết nối đến server. Vui lòng khởi động backend.')
      } else {
        setError(err.response?.data?.message || 'Đăng bài thất bại. Vui lòng thử lại.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">Đăng bài mới</h1>
        <p className="page-subtitle">Chụp ảnh và ghi nhận chi tiêu của bạn</p>
      </div>

      {success ? (
        <div className="upload-success fade-in">
          <div className="upload-success-icon"><CheckCircle size={48} /></div>
          <h3>Đăng bài thành công! 🎉</h3>
          <p>Bài đăng của bạn đã được chia sẻ lên feed.</p>
        </div>
      ) : (
        <div className="upload-layout">
          {/* Image section */}
          <div className="upload-image-section">
            {!preview ? (
              <div
                className="upload-dropzone"
                onDrop={handleDrop}
                onDragOver={e => e.preventDefault()}
                onClick={() => fileRef.current?.click()}
              >
                <div className="dropzone-icon"><Camera size={40} /></div>
                <div className="dropzone-title">Thả ảnh vào đây</div>
                <div className="dropzone-sub">hoặc click để chọn từ thư mục</div>
                <div className="dropzone-hint">JPG, PNG, WEBP • Tối đa 10MB</div>
                <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }}
                  onChange={e => e.target.files?.[0] && handleFile(e.target.files[0])} />
              </div>
            ) : (
              <div className="upload-preview-wrap">
                <img src={preview} alt="Preview" className="upload-preview-img" />
                <button className="upload-remove-btn"
                  onClick={() => { setPreview(null); setFile(null); setAiSuggestion(null) }}>
                  <X size={16} />
                </button>
                {aiSuggestion && (
                  <div className="ai-suggestion fade-in">
                    <div className="ai-suggestion-header">
                      <Sparkles size={14} />
                      <span>Gemini AI gợi ý</span>
                    </div>
                    <div className="ai-suggestion-body">
                      <span className="ai-suggestion-cat">{aiSuggestion.icon} {aiSuggestion.name}</span>
                      <button className="btn btn-sm ai-accept-btn" onClick={acceptSuggestion} type="button">Chọn</button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Form section */}
          <form className="upload-form" onSubmit={handleSubmit}>
            {error && (
              <div className="auth-error">
                <AlertCircle size={16} /> {error}
              </div>
            )}

            <div className="input-group">
              <label className="input-label">Số tiền *</label>
              <div className="amount-input-wrap">
                <span className="amount-prefix">₫</span>
                <input className="input-field amount-input" type="number" placeholder="0"
                  value={form.amount} onChange={e => setForm(f => ({ ...f, amount: e.target.value }))}
                  required min="0" />
              </div>
              {form.amount && <div className="amount-preview">= {formatCurrency(Number(form.amount))}</div>}
            </div>

            <div className="input-group">
              <label className="input-label">Danh mục</label>
              <div className="category-grid">
                {categories.map(cat => (
                  <button key={cat.id} type="button"
                    className={`category-btn ${form.categoryId === cat.id.toString() ? 'active' : ''}`}
                    onClick={() => setForm(f => ({ ...f, categoryId: cat.id.toString() }))}>
                    <span>{cat.icon}</span>
                    <span>{cat.name}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Caption</label>
              <input className="input-field" type="text" placeholder="Mô tả ngắn..."
                value={form.caption} onChange={e => setForm(f => ({ ...f, caption: e.target.value }))} />
            </div>

            <div className="input-group">
              <label className="input-label">Ngày giao dịch *</label>
              <input className="input-field" type="date" value={form.transactionDate}
                onChange={e => setForm(f => ({ ...f, transactionDate: e.target.value }))} required />
            </div>

            <div className="input-group">
              <label className="input-label">Ghi chú</label>
              <textarea className="input-field" placeholder="Mua ở đâu, với ai..."
                value={form.notes} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
                rows={3} style={{ resize: 'none' }} />
            </div>

            <button type="submit" className="btn btn-primary btn-lg btn-block"
              disabled={loading || !preview}>
              {loading ? <><div className="spinner" /> Đang đăng...</> : <><Upload size={18} /> Đăng bài</>}
            </button>

            {!preview && <p className="upload-hint">* Vui lòng chọn ảnh trước khi đăng</p>}
          </form>
        </div>
      )}
    </div>
  )
}
