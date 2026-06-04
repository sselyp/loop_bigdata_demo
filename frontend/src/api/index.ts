import axios from 'axios'
import { message } from 'ant-design-vue'

// 后端统一响应信封（响应拦截器已将其作为 resolve 值返回）
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// Inject X-API-Key header for backend authentication
request.interceptors.request.use((config) => {
  const apiKey =
    (import.meta as any).env?.VITE_API_KEY ||
    sessionStorage.getItem('apiKey') ||
    localStorage.getItem('apiKey')
  if (apiKey) {
    config.headers['X-API-Key'] = apiKey
  }

  const url = config.url || ''
  if (url.startsWith('/gateway/keys')) {
    const adminKey =
      (import.meta as any).env?.VITE_ADMIN_KEY ||
      sessionStorage.getItem('adminKey') ||
      localStorage.getItem('adminKey')
    if (adminKey) {
      config.headers['X-Admin-Key'] = adminKey
    }
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code !== 200) {
      message.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  (error) => {
    message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
