<template>
  <div class="logs-page">
    <a-card :bordered="false">
      <template #title>
        <a-space>
          <a-button type="text" @click="$router.back()">
            <arrow-left-outlined />
          </a-button>
          <span>ETL 任务执行日志 — 任务 #{{ taskId }}</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button @click="loadLogs" :loading="loading">
            <reload-outlined /> 刷新
          </a-button>
          <a-button type="primary" @click="runTask" :loading="running">
            <caret-right-outlined /> 手动执行
          </a-button>
        </a-space>
      </template>

      <!-- Task Info -->
      <a-descriptions v-if="taskInfo" bordered size="small" :column="3" style="margin-bottom: 16px">
        <a-descriptions-item label="任务名称">{{ taskInfo.name }}</a-descriptions-item>
        <a-descriptions-item label="源表">{{ taskInfo.sourceTable }}</a-descriptions-item>
        <a-descriptions-item label="目标表">{{ taskInfo.targetTable }}</a-descriptions-item>
        <a-descriptions-item label="同步模式">{{ taskInfo.syncMode }}</a-descriptions-item>
        <a-descriptions-item label="调度方式">{{ taskInfo.scheduleType }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="taskInfo.status === 'ENABLED' ? 'blue' : 'default'">
            {{ taskInfo.status === 'ENABLED' ? '启用' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <!-- Execution Logs Table -->
      <a-table
        :columns="columns"
        :data-source="executions"
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total: number) => `共 ${total} 条记录` }"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag v-if="record.status === 'SUCCESS'" color="success">
              <check-circle-outlined /> 成功
            </a-tag>
            <a-tag v-else-if="record.status === 'FAILED'" color="error">
              <close-circle-outlined /> 失败
            </a-tag>
            <a-tag v-else-if="record.status === 'RUNNING'" color="processing">
              <sync-outlined :spin="true" /> 运行中
            </a-tag>
            <a-tag v-else>{{ record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'rowsProcessed'">
            <span v-if="record.rowsProcessed > 0">{{ record.rowsProcessed.toLocaleString() }} 行</span>
            <span v-else-if="record.status === 'RUNNING'" style="color: #1677ff">处理中...</span>
            <span v-else style="color: #999">-</span>
          </template>
          <template v-if="column.key === 'duration'">
            {{ calcDuration(record.startTime, record.endTime) }}
          </template>
          <template v-if="column.key === 'startTime'">
            {{ formatTime(record.startTime) }}
          </template>
          <template v-if="column.key === 'endTime'">
            {{ formatTime(record.endTime) || (record.status === 'RUNNING' ? '运行中' : '-') }}
          </template>
          <template v-if="column.key === 'errorMessage'">
            <template v-if="record.errorMessage">
              <a-popover title="错误详情" trigger="click">
                <template #content>
                  <pre style="max-width: 500px; max-height: 300px; overflow: auto; white-space: pre-wrap; font-size: 12px">{{ record.errorMessage }}</pre>
                </template>
                <a-tag color="error" style="cursor: pointer">查看错误</a-tag>
              </a-popover>
            </template>
            <span v-else style="color: #999">-</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined, ReloadOutlined, CaretRightOutlined,
  CheckCircleOutlined, CloseCircleOutlined, SyncOutlined
} from '@ant-design/icons-vue'
import { etlTaskApi, type EtlTask } from '@/api/etlTask'

const route = useRoute()
const router = useRouter()
const taskId = ref(Number(route.params.id))
const taskInfo = ref<EtlTask | null>(null)
const executions = ref<any[]>([])
const loading = ref(false)
const running = ref(false)

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '处理行数', dataIndex: 'rowsProcessed', key: 'rowsProcessed', width: 110 },
  { title: '耗时', key: 'duration', width: 100 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 170 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 170 },
  { title: '错误信息', dataIndex: 'errorMessage', key: 'errorMessage', width: 120 }
]

function formatTime(time: string | undefined) {
  if (!time) return ''
  try {
    return new Date(time).toLocaleString('zh-CN')
  } catch {
    return time
  }
}

function calcDuration(start: string | undefined, end: string | undefined) {
  if (!start) return '-'
  const endTime = end ? new Date(end).getTime() : Date.now()
  const startTime = new Date(start).getTime()
  const diff = endTime - startTime
  if (diff < 0) return '-'
  if (diff < 1000) return diff + 'ms'
  if (diff < 60000) return (diff / 1000).toFixed(1) + 's'
  return (diff / 60000).toFixed(1) + 'm'
}

async function loadLogs() {
  loading.value = true
  try {
    const res = await etlTaskApi.logs(taskId.value)
    executions.value = res.data || []
  } catch (e: any) {
    message.error('加载日志失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function loadTaskInfo() {
  try {
    const res = await etlTaskApi.list()
    const tasks = res.data || []
    taskInfo.value = tasks.find((t: EtlTask) => t.id === taskId.value) || null
  } catch (e) {
    // Task info is supplementary
  }
}

async function runTask() {
  running.value = true
  try {
    await etlTaskApi.run(taskId.value)
    message.success('任务执行已触发')
    setTimeout(loadLogs, 2000)
  } catch (e: any) {
    message.error('执行失败: ' + (e.message || '未知错误'))
  } finally {
    running.value = false
  }
}

onMounted(() => {
  loadLogs()
  loadTaskInfo()
})
</script>

<style scoped>
.logs-page { padding: 0; }
</style>
