import request from './request'

export const getUserList = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })
export const updateUserRole = (id, role) => request.put(`/admin/users/${id}/role`, { role })
export const deleteUser = (id) => request.delete(`/admin/users/${id}`)
