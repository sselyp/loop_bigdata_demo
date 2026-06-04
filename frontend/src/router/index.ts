import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true, title: '用户登录' }
    },
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/monitor/Dashboard.vue'),
          meta: { title: '监控看板' }
        },
        {
          path: 'datasource',
          name: 'Datasource',
          component: () => import('@/views/datasource/DatasourceList.vue'),
          meta: { title: '数据源管理' }
        },
        {
          path: 'etl-task',
          name: 'EtlTask',
          component: () => import('@/views/etl-task/EtlTaskList.vue'),
          meta: { title: 'ETL任务' }
        },
        {
          path: 'etl-task/:id/logs',
          name: 'EtlTaskLogs',
          component: () => import('@/views/monitor/EtlTaskLogs.vue'),
          meta: { title: '执行日志' }
        },
        {
          path: 'gateway',
          name: 'ApiGateway',
          component: () => import('@/views/gateway/ApiGateway.vue'),
          meta: { title: 'API管理' }
        },
        {
          path: 'governance/quality',
          name: 'GovernanceQuality',
          component: () => import('@/views/governance/QualityDashboard.vue'),
          meta: { title: '质量看板' }
        },
        {
          path: 'governance/lineage',
          name: 'GovernanceLineage',
          component: () => import('@/views/governance/LineageGraph.vue'),
          meta: { title: '数据血缘' }
        },
        {
          path: 'governance/permission',
          name: 'GovernancePermission',
          component: () => import('@/views/governance/PermissionConfig.vue'),
          meta: { title: '权限配置' }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    if (to.name === 'Login' && auth.isAuthenticated) {
      return '/dashboard'
    }
    return true
  }

  if (!auth.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  return true
})

export default router
