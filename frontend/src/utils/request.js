/**
 * 通用请求工具类
 * 封装 uni.request，支持拦截器、BaseURL 及 Promise 异步处理
 */

const BASE_URL = 'http://localhost:8080' // 后端服务地址

const request = (options = {}) => {
  // 补全 URL
  options.url = BASE_URL + (options.url.startsWith('/') ? options.url : '/' + options.url)
  options.method = options.method || 'GET'
  options.header = {
    'Content-Type': 'application/json',
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
        console.log('API Response:', res)
        
        if (!res.data) {
          reject(new Error('服务器未返回数据'))
          return
        }

        const { code, msg, data } = res.data
        if (code === 200) {
          resolve(data)
        } else if (code === 401) {
          // Token 过期或未登录
          uni.showToast({ title: '登录已过期', icon: 'none' })
          reject(new Error(msg || '未登录'))
        } else {
          uni.showToast({ title: msg || '请求失败', icon: 'none' })
          reject(new Error(msg || '服务器错误'))
        }
      },
      fail: (err) => {
        console.error('Request Fail:', err)
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 导出常用的请求方法
export default {
  get: (url, data, header) => request({ url, method: 'GET', data, header }),
  post: (url, data, header) => request({ url, method: 'POST', data, header }),
  put: (url, data, header) => request({ url, method: 'PUT', data, header }),
  delete: (url, data, header) => request({ url, method: 'DELETE', data, header })
}
