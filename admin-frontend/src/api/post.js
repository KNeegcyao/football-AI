import request from './request'

export const getPostList = (params) => request.get('/admin/posts', { params })
export const updatePostStatus = (id, status) => request.put(`/admin/posts/${id}/status`, { status })
export const deletePost = (id) => request.delete(`/admin/posts/${id}`)
