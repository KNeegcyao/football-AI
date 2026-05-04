import request from './request'

export const getTopicList = (params) => request.get('/admin/topics', { params })
export const createTopic = (data) => request.post('/admin/topics', data)
export const updateTopic = (id, data) => request.put(`/admin/topics/${id}`, data)
export const deleteTopic = (id) => request.delete(`/admin/topics/${id}`)
