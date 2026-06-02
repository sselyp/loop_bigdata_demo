import axios from 'axios'
import { message } from 'ant-design-vue'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// Inject X-API-Key header for backend authentication
request.interceptors.request.use((config) => {
  const apiKey = (import.meta as any).env?.VITE_API_KEY || localStorage.getItem('apiKey')
  if (apiKey) {
    config.headers['X-API-Key'] = apiKey
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
