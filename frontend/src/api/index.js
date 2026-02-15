import request from '@/utils/request'

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
