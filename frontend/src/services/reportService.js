import api from './api'

// GET /api/reports/monthly?year=YYYY&month=MM
export const getMonthlyReport = async (year, month) => {
  const { data } = await api.get('/reports/monthly', { params: { year, month } })
  return data
}
