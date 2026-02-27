/**
 * 通用请求工具类
 * 封装 uni.request，支持拦截器、BaseURL 及 Promise 异步处理
 */

const BASE_URL = 'http://192.168.5.6:8080' // 模拟器测试使用 192.168.5.6，真机测试改回 192.168.5.6
export { BASE_URL }

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
        console.log('API Response Status:', res.statusCode)
        console.log('API Response Data Type:', typeof res.data)
        console.log('API Response Data:', res.data)
        
        // 处理 HTTP 状态码为 401 的情况 (Spring Security 默认行为)
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.showToast({ title: '登录已过期', icon: 'none' })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' })
          }, 1500)
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
          uni.showToast({ title: '登录已过期', icon: 'none' })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' })
          }, 1500)
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
request.get = (url, data, header) => request({ url, method: 'GET', data, header })
request.post = (url, data, header) => request({ url, method: 'POST', data, header })
request.put = (url, data, header) => request({ url, method: 'PUT', data, header })
request.delete = (url, data, header) => request({ url, method: 'DELETE', data, header })

export default request

