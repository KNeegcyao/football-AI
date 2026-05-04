import request from './request'

export const getCommentList = (params) => request.get('/admin/comments', { params })
export const deleteComment = (id) => request.delete(`/admin/comments/${id}`)
