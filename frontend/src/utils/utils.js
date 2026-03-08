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

/**
 * 格式化聊天时间显示
 * @param {string|Date} time 
 * @returns {string}
 */
export const formatChatMessageTime = (time) => {
  if (!time) return ''
  
  // 兼容不同格式的时间字符串
  const date = typeof time === 'string' ? new Date(time.replace(/-/g, '/').replace('T', ' ')) : new Date(time)
  const now = new Date()
  
  const isSameYear = date.getFullYear() === now.getFullYear()
  const isToday = date.toDateString() === now.toDateString()
  
  // 计算是否是昨天
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  const isYesterday = date.toDateString() === yesterday.toDateString()
  
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const timeStr = `${hours}:${minutes}`
  
  if (isToday) {
    return timeStr
  }
  
  if (isYesterday) {
    return `昨天 ${timeStr}`
  }
  
  if (isSameYear) {
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日 ${timeStr}`
  }
  
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}年${month}月${day}日 ${timeStr}`
}
