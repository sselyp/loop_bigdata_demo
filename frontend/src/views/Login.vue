<template>
  <main class="login-page" :style="pageStyle">
    <section class="brand-panel">
      <div class="brand-kicker">Big Data Operations</div>
      <h1>重庆机电工程大数据平台</h1>
      <div class="brand-metrics">
        <div>
          <strong>ETL</strong>
          <span>数据接入</span>
        </div>
        <div>
          <strong>API</strong>
          <span>统一服务</span>
        </div>
        <div>
          <strong>Gov</strong>
          <span>数据治理</span>
        </div>
      </div>
    </section>

    <a-card class="login-card" :bordered="false">
      <div class="card-title">
        <span>用户登录</span>
        <small>Platform Console</small>
      </div>
      <a-form layout="vertical" :model="form" @finish="handleLogin">
        <a-form-item
          label="用户登录"
          name="username"
          :rules="[{ required: true, message: '请输入用户登录' }]"
        >
          <a-input v-model:value="form.username" size="large" autocomplete="username">
            <template #prefix><user-outlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item
          label="用户密码"
          name="password"
          :rules="[{ required: true, message: '请输入用户密码' }]"
        >
          <a-input-password
            v-model:value="form.password"
            size="large"
            autocomplete="off"
          >
            <template #prefix><lock-outlined /></template>
          </a-input-password>
        </a-form-item>
        <a-button class="login-button" type="primary" html-type="submit" size="large" block>
          <template #icon><login-outlined /></template>
          登录
        </a-button>
      </a-form>
    </a-card>
  </main>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { LockOutlined, LoginOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'
import loginBg from '@/assets/login-bg.png'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const form = reactive({
  username: 'admin',
  password: ''
})

const pageStyle = {
  backgroundImage: `linear-gradient(90deg, rgba(5, 18, 32, 0.72), rgba(5, 18, 32, 0.28)), url(${loginBg})`
}

function handleLogin() {
  if (!auth.login(form.username.trim(), form.password)) {
    message.error('用户名或密码错误')
    form.password = ''
    return
  }

  message.success('登录成功')
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
  router.replace(redirect)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 392px;
  align-items: center;
  gap: 48px;
  padding: 64px clamp(32px, 8vw, 112px);
  background-color: #071425;
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  color: #f8fbff;
  overflow: hidden;
}

.brand-panel {
  max-width: 720px;
}

.brand-kicker {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border: 1px solid rgba(120, 210, 255, 0.45);
  border-radius: 4px;
  background: rgba(10, 30, 48, 0.42);
  color: #a8defe;
  font-size: 13px;
}

h1 {
  margin: 28px 0 32px;
  font-size: 48px;
  line-height: 1.16;
  font-weight: 700;
  letter-spacing: 0;
  text-shadow: 0 8px 28px rgba(0, 0, 0, 0.35);
}

.brand-metrics {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.brand-metrics div {
  min-width: 128px;
  padding: 14px 16px;
  border: 1px solid rgba(143, 208, 255, 0.24);
  border-radius: 6px;
  background: rgba(5, 18, 32, 0.42);
  backdrop-filter: blur(8px);
}

.brand-metrics strong {
  display: block;
  color: #ffffff;
  font-size: 22px;
  line-height: 1.2;
}

.brand-metrics span {
  display: block;
  margin-top: 6px;
  color: #b8d6e9;
  font-size: 13px;
}

.login-card {
  width: 100%;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(12px);
}

.card-title {
  margin-bottom: 28px;
}

.card-title span {
  display: block;
  color: #122238;
  font-size: 24px;
  font-weight: 700;
}

.card-title small {
  display: block;
  margin-top: 6px;
  color: #6d7b8c;
  font-size: 13px;
}

.login-button {
  height: 44px;
  border-radius: 4px;
  background: #1677ff;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
    align-content: center;
    gap: 28px;
    padding: 32px 20px;
  }

  h1 {
    font-size: 34px;
    margin: 20px 0 22px;
  }

  .login-card {
    max-width: 420px;
  }
}
</style>
