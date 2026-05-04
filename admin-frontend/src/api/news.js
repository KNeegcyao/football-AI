import request from './request'

export const getNewsList = (params) => request.get('/admin/news', { params })
export const createNews = (data) => request.post('/admin/news', data)
export const updateNews = (id, data) => request.put(`/admin/news/${id}`, data)
export const deleteNews = (id) => request.delete(`/admin/news/${id}`)
