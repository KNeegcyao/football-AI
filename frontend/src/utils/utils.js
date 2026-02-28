import { BASE_URL } from './request'

/**
 * 获取完整图片 URL
 * @param {string} url 
 * @returns {string}
 */
export const getFullImageUrl = (url) => {
  if (!url) return 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=1200&auto=format&fit=crop'
  if (url.startsWith('http')) return url
  return BASE_URL + (url.startsWith('/') ? url : '/' + url)
}

/**
 * 格式化时间
 * @param {string} time 
 * @returns {string}
 */
export const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time.replace('T', ' '))
  const now = new Date()
  const diff = now - date
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) return '刚刚'
  if (diff < hour) return Math.floor(diff / minute) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'
  if (diff < 7 * day) return Math.floor(diff / day) + '天前'
  
  return time.substring(0, 10)
}
