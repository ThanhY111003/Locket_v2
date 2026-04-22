// ===== Utility functions (KHÔNG phải mock data) =====

export const categories = [
  { id: 1, name: 'Ăn uống',   icon: '🍜', color: '#f59e0b', badge: 'badge-yellow' },
  { id: 2, name: 'Mua sắm',   icon: '🛍️', color: '#ec4899', badge: 'badge-pink' },
  { id: 3, name: 'Di chuyển', icon: '🚗', color: '#3b82f6', badge: 'badge-blue' },
  { id: 4, name: 'Giải trí',  icon: '🎬', color: '#7c3aed', badge: 'badge-purple' },
  { id: 5, name: 'Hóa đơn',   icon: '📄', color: '#10b981', badge: 'badge-green' },
  { id: 6, name: 'Khác',      icon: '📦', color: '#6b7280', badge: 'badge-purple' },
]

export const getCategoryById  = (id)   => categories.find(c => c.id === id)   || categories[5]
export const getCategoryByName = (name) => categories.find(c => c.name === name) || categories[5]

export const formatCurrency = (amount) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount ?? 0)

export const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const diff = (Date.now() - date) / 1000
  if (diff < 60)     return 'Vừa xong'
  if (diff < 3600)   return `${Math.floor(diff / 60)} phút trước`
  if (diff < 86400)  return `${Math.floor(diff / 3600)} giờ trước`
  if (diff < 172800) return 'Hôm qua'
  return date.toLocaleDateString('vi-VN')
}

export const getInitials = (name) => {
  if (!name) return '?'
  return name.split(' ').map(n => n[0]).slice(-2).join('').toUpperCase()
}
