import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
        }
      ]
    }
  ]
})

export default router
