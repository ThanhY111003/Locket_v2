import React, { useEffect, useState } from 'react'
import { BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import { getMonthlyReport } from '../services/reportService'
import { categories, formatCurrency, getCategoryByName } from '../data/mockData'
import './ReportPage.css'

const RADIAN = Math.PI / 180
const renderCustomLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }) => {
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5
  const x = cx + radius * Math.cos(-midAngle * RADIAN)
  const y = cy + radius * Math.sin(-midAngle * RADIAN)
  return percent > 0.05 ? (
    <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontSize={12} fontWeight={700}>
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  ) : null
}

const CustomTooltip = ({ active, payload }) => {
  if (active && payload?.length) {
    return (
      <div className="chart-tooltip">
        <div className="chart-tooltip-label">{payload[0].name || payload[0].payload?.day}</div>
        <div className="chart-tooltip-value">{formatCurrency(Number(payload[0].value))}</div>
      </div>
    )
  }
  return null
}

export default function ReportPage() {
  const now = new Date()
  const [year, setYear] = useState(now.getFullYear())
  const [month, setMonth] = useState(now.getMonth() + 1)
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)

  const months = [
    { label: `Th ${month - 2 > 0 ? month - 2 : month + 10}/${month - 2 > 0 ? year : year - 1}`, m: month - 2 > 0 ? month - 2 : month + 10, y: month - 2 > 0 ? year : year - 1 },
    { label: `Th ${month - 1 > 0 ? month - 1 : month + 11}/${month - 1 > 0 ? year : year - 1}`, m: month - 1 > 0 ? month - 1 : month + 11, y: month - 1 > 0 ? year : year - 1 },
    { label: `Th ${month}/${year}`, m: month, y: year },
  ]

  useEffect(() => {
    setLoading(true)
    getMonthlyReport(year, month)
      .then(data => { setReport(data); setLoading(false) })
      .catch(() => setLoading(false))
  }, [year, month])

  if (loading) {
    return (
      <div className="page-container">
        <div className="page-header"><h1 className="page-title">Báo cáo tài chính</h1></div>
        <div className="stat-grid">{[1,2,3,4].map(i => <div key={i} className="stat-card skeleton" style={{ height: 100 }} />)}</div>
        <div className="charts-grid">
          <div className="chart-card card skeleton" style={{ height: 360 }} />
          <div className="chart-card card skeleton" style={{ height: 360 }} />
        </div>
      </div>
    )
  }

  if (!report) {
    return (
      <div className="page-container">
        <div className="page-header"><h1 className="page-title">Báo cáo tài chính</h1></div>
        <div className="empty-state">
          <div className="empty-icon">❌</div>
          <div className="empty-title">Không thể tải báo cáo</div>
          <div className="empty-desc">Vui lòng kiểm tra kết nối và thử lại</div>
        </div>
      </div>
    )
  }

  const pieData = Object.entries(report.spendingByCategory ?? {}).map(([name, value]) => ({
    name, value: Number(value), color: getCategoryByName(name)?.color || '#7c3aed'
  }))
  const topCategories = [...pieData].sort((a, b) => b.value - a.value)
  // weeklyData từ backend (thực), fallback empty array
  const weeklyData = (report.weeklyData || []).map(d => ({ ...d, amount: Number(d.amount || 0) }))

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Báo cáo tài chính</h1>
          <p className="page-subtitle">Tổng quan chi tiêu của bạn</p>
        </div>
        <div className="report-month-tabs">
          {months.map(({ label, m, y }) => (
            <button key={label}
              className={`month-tab ${month === m && year === y ? 'active' : ''}`}
              onClick={() => { setMonth(m); setYear(y) }}>
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* Summary */}
      <div className="stat-grid">
        {[
          { label: 'Tổng chi tiêu', value: formatCurrency(report.totalSpending), sub: `Tháng ${month}/${year}` },
          { label: 'Trung bình/ngày', value: formatCurrency(Math.round(Number(report.totalSpending) / now.getDate())), sub: `${now.getDate()} ngày đã qua` },
          { label: 'Giao dịch', value: `${report.transactions?.length ?? 0}`, sub: 'lần chi tiêu' },
          { label: 'Danh mục', value: `${Object.keys(report.spendingByCategory ?? {}).length}`, sub: 'loại chi tiêu' },
        ].map((s, i) => (
          <div key={i} className="stat-card fade-in" style={{ animationDelay: `${i * 60}ms` }}>
            <div className="stat-label">{s.label}</div>
            <div className="stat-value" style={{ fontSize: 20 }}>{s.value}</div>
            <div className="stat-sub">{s.sub}</div>
          </div>
        ))}
      </div>

      {/* Charts */}
      <div className="charts-grid">
        <div className="chart-card card">
          <div className="chart-title">Chi tiêu theo danh mục</div>
          {pieData.length > 0 ? (
            <>
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100}
                    dataKey="value" labelLine={false} label={renderCustomLabel}>
                    {pieData.map((entry, i) => <Cell key={i} fill={entry.color} />)}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                </PieChart>
              </ResponsiveContainer>
              <div className="chart-legend">
                {topCategories.map((cat, i) => (
                  <div key={i} className="legend-item">
                    <div className="legend-dot" style={{ background: cat.color }} />
                    <span className="legend-name">{cat.name}</span>
                    <span className="legend-value">{formatCurrency(cat.value)}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="empty-state">
              <div className="empty-icon">📊</div>
              <div className="empty-title">Chưa có dữ liệu tháng này</div>
              <div className="empty-desc">Hãy đăng bài và ghi nhận chi tiêu!</div>
            </div>
          )}
        </div>

        <div className="chart-card card">
          <div className="chart-title">Chi tiêu 7 ngày gần nhất</div>
          {weeklyData.length > 0 ? (
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={weeklyData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="day" tick={{ fill: '#a1a1c0', fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#6b6b8a', fontSize: 10 }} axisLine={false} tickLine={false}
                  tickFormatter={v => v >= 1000000 ? `${(v/1000000).toFixed(1)}M` : v >= 1000 ? `${(v/1000).toFixed(0)}k` : v} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="amount" radius={[6,6,0,0]} fill="url(#barGradient)" />
                <defs>
                  <linearGradient id="barGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#7c3aed" />
                    <stop offset="100%" stopColor="#ec4899" />
                  </linearGradient>
                </defs>
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state" style={{ height: 240, justifyContent: 'center' }}>
              <div className="empty-icon">📈</div>
              <div className="empty-title">Chưa có giao dịch 7 ngày qua</div>
            </div>
          )}
        </div>
      </div>

      {/* Transaction history */}
      {report.transactions?.length > 0 && (
        <div className="report-section card">
          <div className="report-section-header">
            <div className="chart-title">Lịch sử giao dịch</div>
            <span className="badge badge-purple">{report.transactions.length} giao dịch</span>
          </div>
          <div className="tx-list">
            {report.transactions.map((tx, i) => {
              const cat = getCategoryByName(tx.categoryName)
              return (
                <div key={tx.id} className="tx-item fade-in" style={{ animationDelay: `${i * 30}ms` }}>
                  <div className="tx-icon" style={{ background: `${cat.color}22`, color: cat.color }}>{cat.icon}</div>
                  <div className="tx-info">
                    <div className="tx-note">{tx.notes || tx.categoryName}</div>
                    <div className="tx-date">{new Date(tx.transactionDate).toLocaleDateString('vi-VN')}</div>
                  </div>
                  <div className="tx-meta">
                    <div className="tx-amount">-{formatCurrency(tx.amount)}</div>
                    <span className={`badge ${cat.badge}`}>{tx.categoryName}</span>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
