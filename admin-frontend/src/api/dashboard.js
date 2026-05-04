import request from './request'

export const getDashboardStats = () => request.get('/admin/dashboard/stats')
export const getDashboardTrend = (days = 7) => request.get('/admin/dashboard/trend', { params: { days } })
