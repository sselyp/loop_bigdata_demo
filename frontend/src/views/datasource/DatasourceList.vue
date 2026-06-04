<template>
  <div class="page-container">
    <a-card title="数据源管理">
      <template #extra>
        <a-space>
          <a-button @click="load">刷新</a-button>
          <a-button type="primary" @click="openCreate">新增数据源</a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="datasources"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        row-key="id"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag color="blue">{{ record.type }}</a-tag>
          </template>
          <template v-else-if="column.key === 'address'">{{ record.host }}:{{ record.port }}</template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">
              {{ record.status === 'ACTIVE' ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'op'">
            <a-space>
              <a @click="openEdit(record)">编辑</a>
              <a :class="{ disabled: testingId === record.id }" @click="testConnection(record)">
                {{ testingId === record.id ? '测试中...' : '测试连接' }}
              </a>
              <a-popconfirm title="确认删除该数据源？" @confirm="remove(record)">
                <a class="danger">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalOpen"
      :title="editingId ? '编辑数据源' : '新增数据源'"
      :confirm-loading="saving"
      width="560px"
      @ok="save"
    >
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="名称" name="name">
          <a-input v-model:value="form.name" placeholder="数据源名称" />
        </a-form-item>
        <a-form-item label="类型" name="type">
          <a-select v-model:value="form.type" :options="typeOptions" @change="onTypeChange" />
        </a-form-item>
        <a-form-item label="主机" name="host">
          <a-input v-model:value="form.host" placeholder="如 127.0.0.1" />
        </a-form-item>
        <a-form-item label="端口" name="port">
          <a-input-number v-model:value="form.port" :min="1" :max="65535" style="width: 100%" />
        </a-form-item>
        <a-form-item label="数据库" name="database">
          <a-input v-model:value="form.database" />
        </a-form-item>
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password
            v-model:value="form.password"
            :placeholder="editingId ? '留空则不修改密码' : '请输入密码'"
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
import { onMounted, reactive, ref } from 'vue'
import { message, type FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { datasourceApi, type Datasource } from '@/api/datasource'

const loading = ref(false)
const saving = ref(false)
const testingId = ref<number | null>(null)
const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const datasources = ref<Datasource[]>([])
const formRef = ref<FormInstance>()

const DEFAULT_PORTS: Record<string, number> = {
  MYSQL: 3306,
  POSTGRESQL: 5432,
  ORACLE: 1521,
  SQLSERVER: 1433
}
const typeOptions = Object.keys(DEFAULT_PORTS).map((t) => ({ label: t, value: t }))
const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' }
]

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '地址', key: 'address' },
  { title: '数据库', dataIndex: 'database', key: 'database' },
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '备注', dataIndex: 'remark', key: 'remark', ellipsis: true },
  { title: '操作', key: 'op', width: 200 }
]

const emptyForm = (): Datasource => ({
  name: '',
  type: 'MYSQL',
  host: '',
  port: 3306,
  database: '',
  username: '',
  password: '',
  status: 'ACTIVE',
  remark: ''
})
const form = reactive<Datasource>(emptyForm())

const rules: Record<string, Rule[]> = {
  name: [{ required: true, message: '请输入名称' }],
  type: [{ required: true, message: '请选择类型' }],
  host: [{ required: true, message: '请输入主机' }],
  port: [{ required: true, type: 'number', message: '请输入端口' }],
  database: [{ required: true, message: '请输入数据库' }],
  username: [{ required: true, message: '请输入用户名' }],
  password: [
    {
      validator: (_rule, value) =>
        editingId.value || value ? Promise.resolve() : Promise.reject('请输入密码')
    }
  ]
}

function onTypeChange(value: unknown) {
  const port = DEFAULT_PORTS[value as string]
  if (port) form.port = port
}

async function load() {
  loading.value = true
  try {
    const res = await datasourceApi.list()
    datasources.value = res.data
  } catch {
    // 错误信息由 axios 响应拦截器统一弹出
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  modalOpen.value = true
}

function openEdit(record: Datasource) {
  editingId.value = record.id ?? null
  Object.assign(form, { ...emptyForm(), ...record, password: '' })
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
    const payload: Datasource = { ...form }
    // 编辑时留空密码表示不修改，避免覆盖后端已存密码
    if (editingId.value && !payload.password) delete payload.password
    if (editingId.value) {
      await datasourceApi.update(editingId.value, payload)
      message.success('已更新数据源')
    } else {
      await datasourceApi.create(payload)
      message.success('已新增数据源')
    }
    modalOpen.value = false
    await load()
  } catch {
    // 拦截器已提示错误
  } finally {
    saving.value = false
  }
}

async function testConnection(record: Datasource) {
  if (!record.id) return
  testingId.value = record.id
  try {
    const res = await datasourceApi.test(record.id)
    message.success(res.data || '连接成功')
  } catch {
    // 拦截器已提示错误
  } finally {
    testingId.value = null
  }
}

async function remove(record: Datasource) {
  if (!record.id) return
  try {
    await datasourceApi.delete(record.id)
    message.success('已删除数据源')
    await load()
  } catch {
    // 拦截器已提示错误
  }
}

onMounted(load)
</script>

<style scoped>
.page-container { padding: 8px; }
.danger { color: #cf1322; }
.disabled { color: #bfbfbf; pointer-events: none; }
</style>
