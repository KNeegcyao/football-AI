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
  register: (data) => request.post('/api/auth/register', data),

  /**
   * 重置密码
   * @param {Object} data { username, password, code }
   */
  resetPassword: (data) => request.post('/api/auth/reset-password', data),

  /**
   * 发送验证码
   * @param {Object} data { phone }
   */
  sendCode: (data) => request.post('/api/auth/send-code', data),

  /**
   * 验证码登录
   * @param {Object} data { phone, code }
   */
  loginByCode: (data) => request.post('/api/auth/login-code', data)
}

/**
 * 社区相关接口
 */
export const communityApi = {
  /**
   * 获取热门圈子
   */
  getHotCircles: () => request.get('/api/community/circles/hot'),

  /**
   * 获取趋势话题
   */
  getTrendTopics: () => request.get('/api/community/topics/trending'),

  /**
   * 获取圈子详情
   */
  getCircleDetail: (id) => request.get(`/api/teams/${id}`),

  /**
   * 获取话题详情
   */
  getTopicDetail: (id) => request.get(`/api/community/topics/${id}`),

  /**
   * 获取圈子帖子列表
   */
  getCirclePosts: (name, params) => request.get(`/api/community/circles/${encodeURIComponent(name)}/posts`, params),

  /**
   * 获取话题帖子列表
   */
  getTopicPosts: (title, params) => request.get('/api/community/topics/posts', { title, ...params })
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
   * 获取帖子评论
   * @param {String} id 帖子ID
   * @param {Object} params { page, size, sort } sort: newest|hottest
   */
  getComments: (id, params) => request.get('/api/comments/list', { postId: id, ...params }),

  /**
   * 发布评论
   * @param {Object} data { postId, content, parentId }
   */
  createComment: (data) => request.post('/api/comments', data),

  /**
   * 发布帖子
   */
  create: (data) => request.post('/api/posts', data),

  /**
   * 点赞/取消点赞
   * @param {Object} data { targetId, targetType } type: 1帖子, 2评论
   */
  like: (data) => request.post('/api/likes/toggle', data),

  /**
   * 关注用户
   */
  followUser: (userId) => request.post(`/api/relationships/follow/${userId}`),

  /**
   * 取消关注
   */
  unfollowUser: (userId) => request.post(`/api/relationships/unfollow/${userId}`),

  /**
   * 检查关注状态
   */
  checkFollowStatus: (userId) => request.get(`/api/relationships/isFollowing/${userId}`),
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

/**
 * 收藏相关接口
 */
export const favoriteApi = {
  /**
   * 收藏/取消收藏
   * @param {Object} data { postId, newsId }
   */
  toggle: (data) => request.post('/api/favorites/toggle', data),

  /**
   * 获取我的收藏帖子列表
   * @param {Object} params { page, size }
   */
  list: (params) => request.get('/api/favorites', params),

  /**
   * 获取我的收藏新闻列表
   * @param {Object} params { page, size }
   */
  listNews: (params) => request.get('/api/favorites/news', params),

  /**
   * 检查收藏状态
   * @param {Object} params { postId, newsId }
   */
  check: (params) => request.get('/api/favorites/check', params)
}

/**
 * 通知相关接口
 */
export const notificationApi = {
  /**
   * 获取未读消息数
   */
  getUnreadCount: () => request.get('/api/notifications/unread-count'),

  /**
   * 获取通知列表
   */
  getList: (params) => request.get('/api/notifications', params),

  /**
   * 标记已读
   */
  read: (id) => request.put(`/api/notifications/${id}/read`),

  /**
   * 全部已读
   */
  readAll: () => request.put('/api/notifications/read-all')
}
