<template>
  <div class="page-container">
    <a-card title="ETL任务">
      <template #extra>
        <a-space>
          <a-button @click="load">刷新</a-button>
          <a-button type="primary" @click="openCreate">新建任务</a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tasks"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        row-key="id"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'source'">{{ datasourceName(record.sourceDatasourceId) }}</template>
          <template v-else-if="column.key === 'syncMode'">
            <a-tag :color="record.syncMode === 'INCREMENTAL' ? 'purple' : 'blue'">
              {{ record.syncMode === 'INCREMENTAL' ? '增量' : '全量' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'schedule'">
            <span v-if="record.scheduleType === 'CRON'">CRON: {{ record.cronExpression }}</span>
            <span v-else>手动</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'ENABLED' ? 'green' : 'default'">
              {{ record.status === 'ENABLED' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'lastRun'">
            <a-tag v-if="record.lastRunStatus" :color="runStatusColor(record.lastRunStatus)">
              {{ runStatusText(record.lastRunStatus) }}
            </a-tag>
            <span v-else>-</span>
            <span v-if="record.lastRunTime" class="run-time">{{ record.lastRunTime }}</span>
          </template>
          <template v-else-if="column.key === 'op'">
            <a-space>
              <a :class="{ disabled: runningId === record.id }" @click="run(record)">
                {{ runningId === record.id ? '执行中...' : '执行' }}
              </a>
              <a @click="goLogs(record)">日志</a>
              <a @click="openEdit(record)">编辑</a>
              <a-popconfirm title="确认删除该任务？" @confirm="remove(record)">
                <a class="danger">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalOpen"
      :title="editingId ? '编辑任务' : '新建任务'"
      :confirm-loading="saving"
      width="600px"
      @ok="save"
    >
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 6 }">
        <a-form-item label="任务名称" name="name">
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="源数据源" name="sourceDatasourceId">
          <a-select
            v-model:value="form.sourceDatasourceId"
            :options="datasourceOptions"
            placeholder="选择数据源"
            :loading="dsLoading"
          />
        </a-form-item>
        <a-form-item label="源表" name="sourceTable">
          <a-input v-model:value="form.sourceTable" />
        </a-form-item>
        <a-form-item label="目标表" name="targetTable">
          <a-input v-model:value="form.targetTable" />
        </a-form-item>
        <a-form-item label="同步模式" name="syncMode">
          <a-radio-group v-model:value="form.syncMode" :options="syncModeOptions" />
        </a-form-item>
        <a-form-item v-if="form.syncMode === 'INCREMENTAL'" label="增量字段" name="incrementalColumn">
          <a-input v-model:value="form.incrementalColumn" placeholder="如 update_time" />
        </a-form-item>
        <a-form-item label="调度方式" name="scheduleType">
          <a-radio-group v-model:value="form.scheduleType" :options="scheduleOptions" />
        </a-form-item>
        <a-form-item v-if="form.scheduleType === 'CRON'" label="Cron表达式" name="cronExpression">
          <a-input v-model:value="form.cronExpression" placeholder="如 0 0 2 * * ?" />
        </a-form-item>
        <a-form-item label="字段映射" name="fieldMapping">
          <a-textarea
            v-model:value="form.fieldMapping"
            :rows="3"
            placeholder='可选，JSON 数组：[{"source":"a","target":"b"}]'
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message, type FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import type { ApiResponse } from '@/api'
import { etlTaskApi, type EtlTask } from '@/api/etlTask'
import { datasourceApi, type Datasource } from '@/api/datasource'

const router = useRouter()
const loading = ref(false)
const dsLoading = ref(false)
const saving = ref(false)
const runningId = ref<number | null>(null)
const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const tasks = ref<EtlTask[]>([])
const datasources = ref<Datasource[]>([])
const formRef = ref<FormInstance>()

// etlTaskApi 为共享模块（未泛型化），在此统一解包响应信封
function unwrap<T>(res: unknown): T {
  return (res as ApiResponse<T>).data
}

const syncModeOptions = [
  { label: '全量', value: 'FULL' },
  { label: '增量', value: 'INCREMENTAL' }
]
const scheduleOptions = [
  { label: '手动', value: 'MANUAL' },
  { label: '定时(CRON)', value: 'CRON' }
]
const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' }
]

const datasourceOptions = computed(() =>
  datasources.value.map((d) => ({ label: `${d.name} (${d.type})`, value: d.id }))
)

const columns = [
  { title: '任务名称', dataIndex: 'name', key: 'name' },
  { title: '源数据源', key: 'source' },
  { title: '源表', dataIndex: 'sourceTable', key: 'sourceTable' },
  { title: '目标表', dataIndex: 'targetTable', key: 'targetTable' },
  { title: '同步模式', key: 'syncMode' },
  { title: '调度', key: 'schedule' },
  { title: '状态', key: 'status' },
  { title: '最近执行', key: 'lastRun' },
  { title: '操作', key: 'op', width: 220 }
]

function datasourceName(id: number) {
  return datasources.value.find((d) => d.id === id)?.name ?? `#${id}`
}
function runStatusColor(s: string) {
  return s === 'SUCCESS' ? 'green' : s === 'FAILED' ? 'red' : 'blue'
}
function runStatusText(s: string) {
  return s === 'SUCCESS' ? '成功' : s === 'FAILED' ? '失败' : s === 'RUNNING' ? '运行中' : s
}

const emptyForm = (): EtlTask => ({
  name: '',
  sourceDatasourceId: undefined as unknown as number,
  sourceTable: '',
  targetTable: '',
  syncMode: 'FULL',
  incrementalColumn: '',
  fieldMapping: '',
  scheduleType: 'MANUAL',
  cronExpression: '',
  status: 'ENABLED',
  remark: ''
})
const form = reactive<EtlTask>(emptyForm())

const rules: Record<string, Rule[]> = {
  name: [{ required: true, message: '请输入任务名称' }],
  sourceDatasourceId: [{ required: true, type: 'number', message: '请选择源数据源' }],
  sourceTable: [{ required: true, message: '请输入源表' }],
  targetTable: [{ required: true, message: '请输入目标表' }],
  syncMode: [{ required: true, message: '请选择同步模式' }],
  scheduleType: [{ required: true, message: '请选择调度方式' }],
  incrementalColumn: [
    {
      validator: (_rule, value) =>
        form.syncMode === 'INCREMENTAL' && !value
          ? Promise.reject('增量模式需填写增量字段')
          : Promise.resolve()
    }
  ],
  cronExpression: [
    {
      validator: (_rule, value) =>
        form.scheduleType === 'CRON' && !value
          ? Promise.reject('请填写 Cron 表达式')
          : Promise.resolve()
    }
  ],
  fieldMapping: [
    {
      validator: (_rule, value) => {
        if (!value) return Promise.resolve()
        try {
          JSON.parse(value)
          return Promise.resolve()
        } catch {
          return Promise.reject('字段映射需为合法 JSON')
        }
      }
    }
  ]
}

async function loadDatasources() {
  dsLoading.value = true
  try {
    const res = await datasourceApi.list()
    datasources.value = res.data
  } catch {
    // 拦截器已提示错误
  } finally {
    dsLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    tasks.value = unwrap<EtlTask[]>(await etlTaskApi.list())
  } catch {
    // 拦截器已提示错误
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  modalOpen.value = true
}

function openEdit(record: EtlTask) {
  editingId.value = record.id ?? null
  Object.assign(form, { ...emptyForm(), ...record })
  modalOpen.value = true
}

async function save() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    const payload: EtlTask = { ...form }
    if (editingId.value) {
      await etlTaskApi.update(editingId.value, payload)
      message.success('已更新任务')
    } else {
      await etlTaskApi.create(payload)
      message.success('已创建任务')
    }
    modalOpen.value = false
    await load()
  } catch {
    // 拦截器已提示错误
  } finally {
    saving.value = false
  }
}

async function run(record: EtlTask) {
  if (!record.id) return
  runningId.value = record.id
  try {
    await etlTaskApi.run(record.id)
    message.success('已触发执行')
    await load()
  } catch {
    // 拦截器已提示错误
  } finally {
    runningId.value = null
  }
}

function goLogs(record: EtlTask) {
  if (record.id) router.push(`/etl-task/${record.id}/logs`)
}

async function remove(record: EtlTask) {
  if (!record.id) return
  try {
    await etlTaskApi.delete(record.id)
    message.success('已删除任务')
    await load()
  } catch {
    // 拦截器已提示错误
  }
}

onMounted(() => {
  loadDatasources()
  load()
})
</script>

<style scoped>
.page-container { padding: 8px; }
.danger { color: #cf1322; }
.disabled { color: #bfbfbf; pointer-events: none; }
.run-time { margin-left: 6px; color: #999; font-size: 12px; }
</style>
