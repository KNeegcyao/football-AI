/**
 * 通用请求工具类
 * 封装 uni.request，支持拦截器、BaseURL 及 Promise 异步处理
 */

// 开发环境服务器地址 (请根据实际 IP 或域名修改)
const BASE_URL = import.meta.env.VITE_APP_BASE_URL || 'http://192.168.5.29:8080' 

// 阿里云 OSS 基础地址 (请根据实际 Bucket 域名修改)
const OSS_BASE_URL = import.meta.env.VITE_APP_OSS_BASE_URL || 'https://ai-football-kneeg.oss-cn-beijing.aliyuncs.com'

export { BASE_URL, OSS_BASE_URL }

/**
 * 获取完整图片 URL
 * 增强版：优先处理 OSS 路径，兼容本地 /uploads/ 路径
 */
export const getFullImageUrl = (path) => {
  if (!path) return '';
  
  // 1. 如果已经是完整 HTTP 路径，直接返回
  if (path.startsWith('http')) {
    return path;
  }

  // 规范化路径：确保 path 不以 / 开头，用于拼接 OSS_BASE_URL
  const cleanPath = path.startsWith('/') ? path.substring(1) : path;

  // 2. 处理静态资源：如果以 static/uploads/ 开头，走 OSS
  if (path.startsWith('static/uploads/') || path.startsWith('/static/uploads/')) {
    // 提取业务目录和文件名，例如 static/uploads/posts/xxx.jpg -> posts/xxx.jpg
    const parts = path.split('/');
    // 找到 uploads 后的部分
    const uploadsIndex = parts.indexOf('uploads');
    if (uploadsIndex !== -1 && uploadsIndex < parts.length - 1) {
      const subPath = parts.slice(uploadsIndex + 1).join('/');
      return `${OSS_BASE_URL}/${subPath}`;
    }
  }

  // 3. 处理以 /static/ 开头的其他静态资源（如图标、Logo）
  if (path.startsWith('/static/')) {
    // 特殊处理 Logo，确保映射到正确的 OSS 路径
    if (path === '/static/soccer-logo.png' || path === '/static/logo.png') {
      return `${OSS_BASE_URL}/logo/logo.png`;
    }
    const subPath = path.substring('/static/'.length);
    const url = `${OSS_BASE_URL}/static/${subPath}`;
    return url;
  }
  
  // 4. 处理直接以 static/ 开头的非上传路径
  if (path.startsWith('static/') && !path.startsWith('static/uploads/')) {
    const url = `${OSS_BASE_URL}/${path}`;
    return url;
  }
  
  // 4. 处理旧的上传资源：如果以 /uploads/ 开头，将其映射到 OSS 对应的业务目录
  if (path.startsWith('/uploads/')) {
    const subPath = path.substring('/uploads/'.length);
    const url = `${OSS_BASE_URL}/${subPath}`;
    return url;
  }

  // 5. 处理 OSS 中的业务目录 (avatar/, posts/, covers/ 等)
  const ossDirectories = ['avatar/', 'posts/', 'covers/'];
  if (ossDirectories.some(dir => path.startsWith(dir))) {
    const url = `${OSS_BASE_URL}/${path}`;
    return url;
  }
  
  // 6. 兼容处理包含 /uploads/ 的路径
  if (path.includes('/uploads/')) {
    const relativePath = path.substring(path.indexOf('/uploads/') + '/uploads/'.length)
    const url = `${OSS_BASE_URL}/${relativePath}`;
    return url;
  }

  // 7. 其他相对路径拼接 BASE_URL
  const finalUrl = BASE_URL + (path.startsWith('/') ? path : '/' + path);
  return finalUrl;
};

const request = (options = {}) => {
  // 补全 URL
  options.url = BASE_URL + (options.url.startsWith('/') ? options.url : '/' + options.url)
  options.method = options.method || 'GET'
  
  // 增加默认超时时间，uni.request 默认是 60000 (60秒)
  // 如果 options 中没有设置 timeout，则默认设置为 60秒
  options.timeout = options.timeout || 60000;

  // 对于 GET 请求，手动将 data 拼接到 URL 后面，确保参数传递
  if (options.method.toUpperCase() === 'GET' && options.data) {
    const params = Object.keys(options.data)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(options.data[key])}`)
      .join('&');
    if (params) {
      options.url += (options.url.indexOf('?') > -1 ? '&' : '?') + params;
    }
    // 拼接完后清除 data，避免某些环境重复发送
    delete options.data;
  }
  
  // 对于 GET 请求，通常不需要 Content-Type，或者使用 application/x-www-form-urlencoded
  const defaultHeader = options.method.toUpperCase() === 'GET' 
    ? {} 
    : { 'Content-Type': 'application/json' }

  options.header = {
    ...defaultHeader,
    ...options.header
  }

  // 模拟从存储中获取 token
  const token = uni.getStorageSync('token')
  if (token) {
    options.header['Authorization'] = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      success: (res) => {
        // 打印原始响应，方便调试
        console.log('API Response Status:', res.statusCode)
        console.log('API Response Data Type:', typeof res.data)
        console.log('API Response Data:', res.data)
        
        // 处理 HTTP 状态码为 401 的情况 (Spring Security 默认行为)
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          // 获取当前页面路径，如果是登录页则不提示和跳转
          const pages = getCurrentPages()
          const currentPage = pages[pages.length - 1]
          if (currentPage && currentPage.route !== 'pages/login/login') {
            uni.showToast({ title: '登录已过期', icon: 'none' })
            setTimeout(() => {
              uni.navigateTo({ url: '/pages/login/login' })
            }, 1500)
          }
          reject(new Error('未登录或登录已过期'))
          return
        }

        if (res.data === undefined || res.data === null || res.data === '') {
          console.warn('API Warning: Response data is empty.')
          reject(new Error('服务器返回了空内容'))
          return
        }

        let responseData = res.data
        if (typeof res.data === 'string') {
          try {
            // 尝试解析字符串 JSON (部分环境 uni.request 不会自动解析)
            responseData = JSON.parse(res.data)
          } catch (e) {
            console.error('API Error: Failed to parse JSON response.', res.data)
            reject(new Error('服务器返回数据格式错误'))
            return
          }
        }

        const { code, msg, data } = responseData
        if (code === 200) {
          resolve(data)
        } else if (code === 401) {
          // 业务逻辑返回的 401
          uni.removeStorageSync('token')
          const pages = getCurrentPages()
          const currentPage = pages[pages.length - 1]
          if (currentPage && currentPage.route !== 'pages/login/login') {
            uni.showToast({ title: '登录已过期', icon: 'none' })
            setTimeout(() => {
              uni.navigateTo({ url: '/pages/login/login' })
            }, 1500)
          }
          reject(new Error(msg || '未登录'))
        } else {
          uni.showToast({ title: msg || '请求失败', icon: 'none' })
          reject(new Error(msg || '服务器错误'))
        }
      },
      fail: (err) => {
        console.error('Request Fail URL:', options.url)
        console.error('Request Fail Error:', JSON.stringify(err))
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 挂载常用的请求方法
request.get = (url, data, header, options = {}) => request({ url, method: 'GET', data, header, ...options })
request.post = (url, data, header, options = {}) => request({ url, method: 'POST', data, header, ...options })
request.put = (url, data, header, options = {}) => request({ url, method: 'PUT', data, header, ...options })
request.delete = (url, data, header, options = {}) => request({ url, method: 'DELETE', data, header, ...options })

export default request

