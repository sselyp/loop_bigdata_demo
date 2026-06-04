import { defineStore } from 'pinia'

const AUTH_KEY = 'etlPlatformSession'
const API_KEY = 'test-etl-key-12345'
const ADMIN_KEY = 'admin-secret-2024'

interface AuthState {
  username: string
  token: string
}

function readSessionAuth(): AuthState {
  try {
    const raw = sessionStorage.getItem(AUTH_KEY)
    if (!raw) {
      return { username: '', token: '' }
    }

    const parsed = JSON.parse(raw) as Partial<AuthState>
    return {
      username: parsed.username || '',
      token: parsed.token || ''
    }
  } catch {
    sessionStorage.removeItem(AUTH_KEY)
    return { username: '', token: '' }
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => readSessionAuth(),
  getters: {
    isAuthenticated: (state) => Boolean(state.token)
  },
  actions: {
    login(username: string, password: string) {
      if (username !== 'admin' || password !== 'admin') {
        return false
      }

      this.username = username
      this.token = 'demo-admin-session'
      sessionStorage.setItem(AUTH_KEY, JSON.stringify({ username: this.username, token: this.token }))
      sessionStorage.setItem('apiKey', API_KEY)
      sessionStorage.setItem('adminKey', ADMIN_KEY)
      return true
    },
    logout() {
      this.username = ''
      this.token = ''
      sessionStorage.removeItem(AUTH_KEY)
      sessionStorage.removeItem('apiKey')
      sessionStorage.removeItem('adminKey')
    }
  }
})
