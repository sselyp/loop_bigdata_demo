<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible>
      <div class="logo">
        <span v-if="!collapsed">ETL平台</span>
        <span v-else>ETL</span>
      </div>
      <a-menu theme="dark" mode="inline" :selected-keys="[currentRoute]" v-model:open-keys="openKeys">
        <a-menu-item key="Dashboard" @click="router.push('/dashboard')">
          <dashboard-outlined />
          <span>监控看板</span>
        </a-menu-item>
        <a-menu-item key="Datasource" @click="router.push('/datasource')">
          <database-outlined />
          <span>数据源管理</span>
        </a-menu-item>
        <a-menu-item key="EtlTask" @click="router.push('/etl-task')">
          <sync-outlined />
          <span>ETL任务</span>
        </a-menu-item>
        <a-menu-item key="ApiGateway" @click="router.push('/gateway')">
          <api-outlined />
          <span>API管理</span>
        </a-menu-item>
        <a-sub-menu key="governance">
          <template #icon><safety-certificate-outlined /></template>
          <template #title>数据治理</template>
          <a-menu-item key="GovernanceQuality" @click="router.push('/governance/quality')">质量看板</a-menu-item>
          <a-menu-item key="GovernanceLineage" @click="router.push('/governance/lineage')">数据血缘</a-menu-item>
          <a-menu-item key="GovernancePermission" @click="router.push('/governance/permission')">权限配置</a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="app-header">
        <a-breadcrumb style="line-height: 64px">
          <a-breadcrumb-item>大数据平台</a-breadcrumb-item>
          <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
        </a-breadcrumb>
        <div class="header-actions">
          <a-tag color="blue">{{ auth.username || 'admin' }}</a-tag>
          <a-button type="text" @click="handleLogout">
            <template #icon><logout-outlined /></template>
            退出
          </a-button>
        </div>
      </a-layout-header>
      <a-layout-content style="margin: 16px">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  DashboardOutlined,
  DatabaseOutlined,
  SyncOutlined,
  ApiOutlined,
  SafetyCertificateOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'

const collapsed = ref(false)
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const currentRoute = computed(() => route.name as string)
const currentTitle = computed(() => route.meta.title as string)

const openKeys = ref<string[]>([])
watch(
  currentRoute,
  (name) => {
    if (name?.startsWith('Governance') && !openKeys.value.includes('governance')) {
      openKeys.value = ['governance']
    }
  },
  { immediate: true }
)

function handleLogout() {
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 0 16px;
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
  background: rgba(255,255,255,0.1);
  margin-bottom: 4px;
}
</style>
