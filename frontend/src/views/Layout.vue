<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible>
      <div class="logo">
        <span v-if="!collapsed">ETL平台</span>
        <span v-else>ETL</span>
      </div>
      <a-menu theme="dark" mode="inline" :selected-keys="[currentRoute]">
        <a-menu-item key="dashboard" @click="router.push('/dashboard')">
          <dashboard-outlined />
          <span>监控看板</span>
        </a-menu-item>
        <a-menu-item key="datasource" @click="router.push('/datasource')">
          <database-outlined />
          <span>数据源管理</span>
        </a-menu-item>
        <a-menu-item key="etl-task" @click="router.push('/etl-task')">
          <sync-outlined />
          <span>ETL任务</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0 16px">
        <a-breadcrumb style="line-height: 64px">
          <a-breadcrumb-item>大数据平台</a-breadcrumb-item>
          <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
        </a-breadcrumb>
      </a-layout-header>
      <a-layout-content style="margin: 16px">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DashboardOutlined, DatabaseOutlined, SyncOutlined } from '@ant-design/icons-vue'

const collapsed = ref(false)
const router = useRouter()
const route = useRoute()

const currentRoute = computed(() => route.name as string)
const currentTitle = computed(() => route.meta.title as string)
</script>

<style scoped>
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
