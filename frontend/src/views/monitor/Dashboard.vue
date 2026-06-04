<template>
  <div class="dashboard">
    <!-- Stats Row -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic title="总任务数" :value="stats.totalTasks">
            <template #prefix><sync-outlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic title="执行成功" :value="stats.successCount" value-style="color: #52c41a">
            <template #prefix><check-circle-outlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic title="执行失败" :value="stats.failedCount" value-style="color: #ff4d4f">
            <template #prefix><close-circle-outlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card">
          <a-statistic title="最近执行" :value="stats.recentExecutions">
            <template #prefix><history-outlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- Task List -->
    <a-card title="ETL 任务概览" style="margin-bottom: 16px">
      <template #extra>
        <a-button type="primary" size="small" @click="router.push('/etl-task')">
          管理任务
        </a-button>
      </template>
      <a-table
        :columns="taskColumns"
        :data-source="tasks"
        :loading="taskLoading"
        row-key="id"
        :pagination="false"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'lastRunStatus'">
            <a-tag v-if="record.lastRunStatus === 'SUCCESS'" color="success">成功</a-tag>
            <a-tag v-else-if="record.lastRunStatus === 'FAILED'" color="error">失败</a-tag>
            <a-tag v-else-if="record.lastRunStatus === 'RUNNING'" color="processing">运行中</a-tag>
            <span v-else style="color: #999">未执行</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ENABLED' ? 'blue' : 'default'">
              {{ record.status === 'ENABLED' ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'lastRunTime'">
            {{ record.lastRunTime ? formatTime(record.lastRunTime) : '-' }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="primary" size="small" @click="runTask(record)">执行</a-button>
              <a-button size="small" @click="viewLogs(record.id)">日志</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- Recent Executions -->
    <a-card title="最近执行记录">
      <a-table
        :columns="execColumns"
        :data-source="recentExecutions"
        :loading="execLoading"
        row-key="id"
        :pagination="{ pageSize: 10, showSizeChanger: false }"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag v-if="record.status === 'SUCCESS'" color="success">成功</a-tag>
            <a-tag v-else-if="record.status === 'FAILED'" color="error">失败</a-tag>
            <a-tag v-else-if="record.status === 'RUNNING'" color="processing">运行中</a-tag>
            <a-tag v-else>{{ record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'errorMessage'">
            <a-tooltip v-if="record.errorMessage" :title="record.errorMessage">
              <span style="color: #ff4d4f; cursor: pointer">查看详情</span>
            </a-tooltip>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SyncOutlined, CheckCircleOutlined, CloseCircleOutlined, HistoryOutlined } from '@ant-design/icons-vue'
import { etlTaskApi, type EtlTask, type EtlExecution } from '@/api/etlTask'

const router = useRouter()

const stats = reactive({
  totalTasks: 0,
  successCount: 0,
  failedCount: 0,
  recentExecutions: 0
})

const tasks = ref<EtlTask[]>([])
const taskLoading = ref(false)
const recentExecutions = ref<EtlExecution[]>([])
const execLoading = ref(false)

const taskColumns = [
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '同步模式', dataIndex: 'syncMode', key: 'syncMode' },
  { title: '调度方式', dataIndex: 'scheduleType', key: 'scheduleType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '最近执行', dataIndex: 'lastRunStatus', key: 'lastRunStatus' },
  { title: '执行时间', dataIndex: 'lastRunTime', key: 'lastRunTime' },
  { title: '操作', key: 'action', width: 160 }
]

const execColumns = [
  { title: '任务ID', dataIndex: 'taskId', key: 'taskId', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '处理行数', dataIndex: 'rowsProcessed', key: 'rowsProcessed', width: 100 },
  { title: '错误信息', dataIndex: 'errorMessage', key: 'errorMessage' },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime' },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime' }
]

function formatTime(time: string) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

async function loadData() {
  taskLoading.value = true
  execLoading.value = true
  try {
    const taskList = await etlTaskApi.list()
    tasks.value = taskList.data || []

    stats.totalTasks = tasks.value.length
    stats.successCount = tasks.value.filter((t: EtlTask) => t.lastRunStatus === 'SUCCESS').length
    stats.failedCount = tasks.value.filter((t: EtlTask) => t.lastRunStatus === 'FAILED').length

    // Collect recent executions from all tasks in parallel
    const logPromises = tasks.value
      .filter(t => t.id)
      .map(t => etlTaskApi.logs(t.id!)
        .then(r => (r.data || []) as EtlExecution[])
        .catch(() => [] as EtlExecution[])
      )
    const allLogs = await Promise.all(logPromises)
    const allExecs: EtlExecution[] = allLogs.flat()
    allExecs.sort((a, b) => {
      const ta = a.startTime ? new Date(a.startTime).getTime() : 0
      const tb = b.startTime ? new Date(b.startTime).getTime() : 0
      return tb - ta
    })
    recentExecutions.value = allExecs
    stats.recentExecutions = allExecs.length
  } catch (e: any) {
    message.error('加载数据失败: ' + (e.message || '未知错误'))
  } finally {
    taskLoading.value = false
    execLoading.value = false
  }
}

async function runTask(task: EtlTask) {
  if (!task.id) return
  try {
    await etlTaskApi.run(task.id)
    message.success(`任务 "${task.name}" 执行已触发`)
    setTimeout(loadData, 2000)
  } catch (e: any) {
    message.error('执行失败: ' + (e.message || '未知错误'))
  }
}

function viewLogs(taskId: number | undefined) {
  if (taskId) {
    router.push(`/etl-task/${taskId}/logs`)
  }
}

onMounted(loadData)
</script>

<style scoped>
.dashboard { padding: 0; }
.stat-card { text-align: center; }
.stat-card .ant-statistic { font-size: 16px; }
</style>
