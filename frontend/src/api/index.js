import request from '@/utils/request'

/**
 * 认证相关接口
 */
export const authApi = {
  /**
   * 登录
   * @param {Object} data { username, password }
   */
  login: (data) => request.post('/api/auth/login', data),

  /**
   * 注册
   * @param {Object} data { username, password, nickname }
   */
  register: (data) => request.post('/api/auth/register', data)
}

/**
 * 帖子相关接口
 */
export const postApi = {
  /**
   * 分页获取帖子列表
   */
  getList: (params) => request.get('/api/posts/list', params),

  /**
   * 获取帖子详情
   */
  getDetail: (id) => request.get(`/api/posts/${id}`),

  /**
   * 发布帖子
   */
  create: (data) => request.post('/api/posts', data)
}

/**
 * 资讯相关接口
 */
export const newsApi = {
  /**
   * 分页获取资讯列表
   */
  getList: (params) => request.get('/api/news/list', params),

  /**
   * 获取资讯详情
   */
  getDetail: (id) => request.get(`/api/news/${id}`)
}

/**
 * 搜索相关接口
 */
export const searchApi = {
  /**
   * 全局搜索
   */
  globalSearch: (params) => request.get('/api/search', params)
}

/**
 * 赛事相关接口
 */
export const matchApi = {
  /**
   * 获取指定日期的赛事
   */
  getByDate: (date) => request.get('/api/matches/date', { date }),

  /**
   * 分页获取赛事列表
   */
  getList: (params) => request.get('/api/matches/list', params)
}

/**
 * 用户相关接口
 */
export const userApi = {
  /**
   * 获取个人资料
   */
  getProfile: () => request.get('/api/users/profile'),

  /**
   * 获取用户统计数据
   */
  getStats: () => request.get('/api/users/stats'),

  /**
   * 更新个人资料
   */
  updateProfile: (data) => request.put('/api/users/profile', data)
}

/**
 * 文件上传相关接口
 */
export const fileApi = {
  /**
   * 上传文件接口地址
   */
  uploadUrl: '/api/files/upload',
  
  /**
   * 获取完整的文件访问路径
   */
  getFileUrl: (url) => {
    if (!url) return ''
    if (url.startsWith('http')) return url
    return 'http://localhost:8080' + (url.startsWith('/') ? url : '/' + url)
  }
}
