import request, { BASE_URL } from '@/utils/request'

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
  sendCode: (data) => request.post('/api/auth/code', data),

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
  getTrendTopics: (params) => request.get('/api/community/topics/trending', params),

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
  getTopicPosts: (title, params) => request.get('/api/community/topics/posts', { title, ...params }),

  /**
   * 加入圈子
   */
  joinCircle: (id) => request.post(`/api/teams/${id}/follow`),

  /**
   * 退出圈子
   */
  leaveCircle: (id) => request.post(`/api/teams/${id}/unfollow`),

  /**
   * 检查圈子加入状态
   */
  checkJoinStatus: (id) => request.get(`/api/teams/${id}/isFollowing`)
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
   * 删除评论
   */
  deleteComment: (id) => request.delete(`/api/comments/${id}`),

  /**
   * 发布帖子
   */
  create: (data) => request.post('/api/posts', data),

  /**
   * 删除帖子
   */
  deletePost: (id) => request.delete(`/api/posts/${id}`),

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

  /**
   * 检查关注状态 (别名)
   */
  isFollowing: (userId) => request.get(`/api/relationships/isFollowing/${userId}`),
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
 * AI 相关接口
 */
export const aiApi = {
  /**
   * 获取新闻摘要 (自动生成并持久化)
   */
  getNewsSummary: (id) => request.post(`/api/ai/news/${id}/summary`),
  
  /**
   * 获取新闻深度点评 (实时生成)
   */
  getNewsImpact: (id) => request.post(`/api/ai/news/${id}/impact`),

  /**
   * 足球规则问答 (RAG)
   */
  askRule: (data) => request.post('/api/ai/qa/rule', data),

  /**
   * 通用智能对话 (RAG)
   */
  chat: (data) => request.post('/api/ai/chat', data),

  /**
   * 生成机智回复
   */
  generateComment: (data) => request.post('/api/ai/comment/generate', data),

  /**
   * 战术深度分析
   */
  analyzeTactics: (data) => request.post('/api/ai/tactics/analyze', data),

  /**
   * 球队/球星数据智能查询 (Tool)
   */
  queryData: (data) => request.post('/api/ai/query/data', data),

  /**
   * 胜率预测
   */
  predictMatch: (data) => request.post('/api/ai/match/predict', data),

  /**
   * 生成赛后深度战报
   */
  generateMatchReport: (data) => request.post('/api/ai/match/report', data),

  /**
   * 评论情感分析/总结
   */
  analyzeComments: (data) => request.post('/api/ai/comment/analyze', data)
}

/**
 * Dify AI 原生接口 (文件/任务管理)
 */
export const difyApi = {
  /**
   * 上传文件 (目前仅支持图片)
   * @param {String} filePath 文件临时路径
   * @param {String} user 用户标识
   */
  uploadFile: (filePath, user) => {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      uni.uploadFile({
        url: `${BASE_URL}/api/ai/files/upload`,
        filePath: filePath,
        name: 'file',
        formData: { user },
        header: {
          'Authorization': token ? (token.startsWith('Bearer ') ? token : `Bearer ${token}`) : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data)
              resolve(data.code === 200 || data.code === 0 ? data.data : data)
            } catch (e) {
              resolve(res.data)
            }
          } else {
            reject(new Error('上传失败'))
          }
        },
        fail: reject
      })
    })
  },

  /**
   * 获取文件预览 URL
   * @param {String} fileId 文件 ID
   */
  getFilePreviewUrl: (fileId) => `${BASE_URL}/api/ai/files/${fileId}/preview`,

  /**
   * 停止响应
   * @param {String} taskId 任务 ID
   * @param {String} user 用户标识
   */
  stopResponse: (taskId, user) => request.post(`/api/ai/chat-messages/${taskId}/stop`, { user }),

  /**
   * 获取会话历史消息
   * @param {String} conversationId 会话 ID
   * @param {String} user 用户标识
   * @param {String} firstId 当前页第一条聊天记录的 ID
   * @param {Number} limit 一次请求返回多少条聊天记录
   */
  getMessages: (conversationId, user, firstId = null, limit = 20) => {
    const params = {
      conversation_id: conversationId,
      user,
      limit
    }
    if (firstId && firstId !== 'null') params.first_id = firstId
    return request.get('/api/ai/messages', params)
  },

  /**
   * 获取会话列表
   * @param {String} user 用户标识
   * @param {String} lastId 当前页最后面一条记录的 ID
   * @param {Number} limit 一次请求返回多少条记录
   */
  getConversations: (user, lastId = null, limit = 20) => {
    return request.get('/api/ai/conversations', {
      user,
      last_id: lastId,
      limit
    })
  },

  /**
   * 删除会话
   * @param {String} conversationId 会话 ID
   * @param {String} user 用户标识
   */
  deleteConversation: (conversationId, user) => {
    return request.delete(`/api/ai/conversations/${conversationId}`, { user })
  }
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
  getList: (params) => request.get('/api/matches/list', params),

  /**
   * 获取指定球队的赛事
   */
  getByTeam: (teamId) => request.get(`/api/matches/team/${teamId}`),

  /**
   * AI 胜率预测
   */
  predict: (data) => request.post('/api/ai/match/predict', data)
}

/**
 * 球员相关接口
 */
export const playerApi = {
  /**
   * 获取指定球队的球员列表
   */
  listByTeam: (teamId) => request.get(`/api/players/team/${teamId}`),

  /**
   * 获取指定球队的球员列表 (SportAPI)
   */
  listByTeamSportApi: (teamId) => request.get(`/api/players/team/${teamId}/sportapi`),

  /**
   * 获取球员详情 (SportAPI)
   */
  getSportApiDetail: (id) => request.get(`/api/players/sportapi/${id}`),

  create: (data) => request.post('/api/players', data),

  /**
   * 分页获取球员列表
   */
  getList: (params) => request.get('/api/players/list', params),

  /**
   * 获取球员详情
   */
  getDetail: (id) => request.get(`/api/players/${id}`)
}

/**
 * 用户相关接口
 */
export const userApi = {
  /**
   * 获取个人资料
   */
  getProfile: (id) => id ? request.get(`/api/users/${id}/profile`) : request.get('/api/users/profile'),

  /**
   * 获取用户统计数据
   */
  getStats: (id) => id ? request.get(`/api/users/${id}/stats`) : request.get('/api/users/stats'),

  /**
   * 获取当前用户的经验流水
   */
  getExperienceRecords: () => request.get('/api/experience/records'),

  /**
   * 分页获取当前用户的经验流水
   * @param {Object} params { page, size }
   */
  getExperienceRecordsPage: (params) => request.get('/api/experience/records/page', params),

  /**
   * 获取关注列表
   */
  getFollowing: (params) => request.get('/api/relationships/following', params),

  /**
   * 获取粉丝列表
   */
  getFollowers: (params) => request.get('/api/relationships/followers', params),

  /**
   * 获取指定用户的关注列表
   */
  getUserFollowing: (userId, params) => request.get(`/api/relationships/following/${userId}`, params),

  /**
   * 获取指定用户的粉丝列表
   */
  getUserFollowers: (userId, params) => request.get(`/api/relationships/followers/${userId}`, params),

  /**
   * 更新个人资料
   */
  updateProfile: (data) => request.put('/api/users/profile', data),

  /**
   * 上传头像 (直接更新到数据库)
   */
  uploadAvatarUrl: '/api/user/avatar/upload',

  /**
   * 修改密码
   * @param {Object} data { oldPassword, newPassword }
   */
  updatePassword: (data) => request.put('/api/users/password', data),

  /**
   * 注销账号
   */
  deleteAccount: () => request.delete('/api/users/account'),

  /**
   * 更新回复提醒设置
   * @param {String} replyType all|following|none
   */
  updateNotificationSetting: (replyType) => request.put('/api/users/notification-setting', { replyType }),

  /**
   * 更新粉丝提醒设置
   * @param {String} fanType receive|never
   */
  updateFanNotificationSetting: (fanType) => request.put('/api/users/fan-notification-setting', { fanType })
}

/**
 * 关注/关系相关接口
 */
export const relationshipApi = {
  follow: (userId) => request.post(`/api/relationships/follow/${userId}`),
  unfollow: (userId) => request.post(`/api/relationships/unfollow/${userId}`),
  getFollowing: (params) => request.get('/api/relationships/following', params),
  getFollowers: (params) => request.get('/api/relationships/followers', params),
  getBlacklist: (params) => request.get('/api/relationships/blacklist', params)
}

/**
 * 聊天相关接口
 */
export const chatApi = {
  /**
   * 获取会话列表
   */
  getSessions: () => request.get('/api/chat/sessions'),

  /**
   * 获取会话消息列表
   */
  getMessages: (sessionId, params) => request.get(`/api/chat/messages/${sessionId}`, params),

  /**
   * 通过对方用户ID获取会话ID
   */
  getSessionByUserId: (otherUserId) => request.get(`/api/chat/session/user/${otherUserId}`),

  /**
   * 获取 WebSocket 连接地址
   */
  getWsUrl: () => {
    const token = uni.getStorageSync('token')
    return `${BASE_URL.replace('http', 'ws')}/ws/chat?token=${token}`
  },

  /**
   * 获取会话设置
   */
  getSessionSettings: (otherUserId) => request.get(`/api/chat/settings/${otherUserId}`),

  /**
     * 设置置顶
     */
    setTop: (sessionId, isTop) => request.post(`/api/chat/settings/top/${sessionId}?isTop=${isTop}`),

    /**
     * 设置免打扰
     */
    setMute: (sessionId, isMute) => request.post(`/api/chat/settings/mute/${sessionId}?isMute=${isMute}`),

    /**
     * 设置黑名单
     */
    setBlacklist: (otherUserId, isBlacklist) => request.post(`/api/chat/settings/blacklist/${otherUserId}?isBlacklist=${isBlacklist}`)
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
  getFileUrl: (url) => getFullImageUrl(url)
}

/**
 * 收藏相关接口
 */
export const favoriteApi = {
  /**
   * 收藏/取消收藏
   * @param {Object} data { postId, newsId, playerId }
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
   * 获取我的收藏球员列表
   * @param {Object} params { page, size }
   */
  listPlayers: (params) => request.get('/api/favorites/players', params),

  /**
   * 检查收藏状态
   * @param {Object} params { postId, newsId, playerId }
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
   * @param {Object} params { page, size, types } types: 1系统, 2点赞, 3评论, 4回复, 5收藏, 6@
   */
  getList: (params) => request.get('/api/notifications', params),

  /**
   * 标记已读
   * @param {String} id 通知ID
   */
  read: (id) => request.put(`/api/notifications/${id}/read`),

  /**
   * 全部已读
   */
  readAll: (types) => request.put('/api/notifications/read-all', { types }),

  /**
   * 删除通知
   */
  delete: (id) => request.delete(`/api/notifications/${id}`)
}

/**
 * 私信相关接口
 */
export const messageApi = {
  /**
   * 发送私信
   * @param {Object} data { receiverId, content }
   */
  send: (data) => request.post('/api/messages/send', data),

  /**
   * 获取对话列表
   */
  getConversations: (params) => request.get('/api/messages/conversations', params),

  /**
   * 获取消息记录
   * @param {Long} otherUserId 对方用户ID
   */
  getMessages: (otherUserId, params) => request.get(`/api/messages/history/${otherUserId}`, params)
}

