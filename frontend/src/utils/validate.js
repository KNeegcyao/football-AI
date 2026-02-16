
/**
 * 校验手机号
 * @param {string} phone 
 * @returns {boolean}
 */
export function isMobile(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 校验邮箱
 * @param {string} email 
 * @returns {boolean}
 */
export function isEmail(email) {
  // 基础格式校验
  const reg = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  if (!reg.test(email)) return false
  
  // 增加常用顶级域名白名单校验，避免类似 .comk 这种拼写错误
  // 这里列出常见的顶级域名，可以根据需要扩展
  const commonTLDs = [
    'com', 'cn', 'net', 'org', 'edu', 'gov', 
    'hk', 'tw', 'us', 'uk', 'jp', 'kr', 'de', 'fr', 
    'io', 'me', 'info', 'biz', 'top', 'xyz', 'vip', 
    'club', 'online', 'site', 'work', 'tech', 'pub', 
    'live', 'news', 'cc', 'cloud', 'design', 'blog'
  ]
  const tld = email.split('.').pop().toLowerCase()
  return commonTLDs.includes(tld)
}
