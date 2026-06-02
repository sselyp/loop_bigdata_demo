<template>
  <div class="page-container">
    <a-card title="权限与脱敏配置">
      <template #extra>
        <a-button type="primary" @click="openCreate">新增授权</a-button>
      </template>

      <a-table
        :columns="columns"
        :data-source="permissions"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        row-key="id"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'resourceType'">
            <a-tag>{{ resourceTypeText(record.resourceType) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-tag v-for="a in record.actions" :key="a" color="blue">{{ actionText(a) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'masking'">
            <a-tag :color="maskingColor(record.masking)">{{ maskingText(record.masking) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'op'">
            <a-space>
              <a @click="openEdit(record)">编辑</a>
              <a-popconfirm title="确认删除该授权？" @confirm="remove(record)">
                <a class="danger">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalOpen"
      :title="editingId ? '编辑授权' : '新增授权'"
      :confirm-loading="saving"
      @ok="save"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="角色" name="role">
          <a-input v-model:value="form.role" placeholder="如：数据分析师" />
        </a-form-item>
        <a-form-item label="资源" name="resource">
          <a-input v-model:value="form.resource" placeholder="表名 / API 路径 / 字段" />
        </a-form-item>
        <a-form-item label="资源类型" name="resourceType">
          <a-select v-model:value="form.resourceType" :options="resourceTypeOptions" />
        </a-form-item>
        <a-form-item label="操作权限" name="actions">
          <a-checkbox-group v-model:value="form.actions" :options="actionOptions" />
        </a-form-item>
        <a-form-item label="脱敏级别" name="masking">
          <a-radio-group v-model:value="form.masking" :options="maskingOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message, type FormInstance } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  governanceApi,
  type MaskingLevel,
  type Permission
} from '@/api/governance'
import { mockPermissions } from '@/mock/governance'

const loading = ref(false)
const saving = ref(false)
const modalOpen = ref(false)
const editingId = ref<number | null>(null)
const permissions = ref<Permission[]>([])
const formRef = ref<FormInstance>()

const columns = [
  { title: '角色', dataIndex: 'role', key: 'role' },
  { title: '资源', dataIndex: 'resource', key: 'resource' },
  { title: '类型', dataIndex: 'resourceType', key: 'resourceType' },
  { title: '操作权限', dataIndex: 'actions', key: 'actions' },
  { title: '脱敏', dataIndex: 'masking', key: 'masking' },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt' },
  { title: '操作', key: 'op', width: 120 }
]

const resourceTypeOptions = [
  { label: '数据表', value: 'table' },
  { label: 'API', value: 'api' },
  { label: '字段', value: 'column' }
]
const actionOptions = [
  { label: '读', value: 'read' },
  { label: '写', value: 'write' },
  { label: '导出', value: 'export' },
  { label: '删除', value: 'delete' }
]
const maskingOptions = [
  { label: '不脱敏', value: 'none' },
  { label: '部分脱敏', value: 'partial' },
  { label: '完全脱敏', value: 'full' }
]

const rules = {
  role: [{ required: true, message: '请输入角色' }],
  resource: [{ required: true, message: '请输入资源' }],
  resourceType: [{ required: true, message: '请选择资源类型' }],
  actions: [{ required: true, type: 'array' as const, min: 1, message: '请至少选择一个操作权限' }]
}

const emptyForm = (): Omit<Permission, 'id' | 'updatedAt'> => ({
  role: '',
  resource: '',
  resourceType: 'table',
  actions: ['read'],
  masking: 'none'
})
const form = reactive(emptyForm())

function resourceTypeText(t: Permission['resourceType']) {
  return resourceTypeOptions.find((o) => o.value === t)?.label ?? t
}
function actionText(a: string) {
  return actionOptions.find((o) => o.value === a)?.label ?? a
}
function maskingText(m: MaskingLevel) {
  return maskingOptions.find((o) => o.value === m)?.label ?? m
}
function maskingColor(m: MaskingLevel) {
  return m === 'none' ? 'default' : m === 'partial' ? 'orange' : 'red'
}

async function load() {
  loading.value = true
  try {
    const res = await governanceApi.permissions()
    permissions.value = (res as any).data
  } catch {
    // 后端权限 API 尚未实现，回退到 mock 数据
    permissions.value = mockPermissions.map((p) => ({ ...p }))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  modalOpen.value = true
}

function openEdit(record: Permission) {
  editingId.value = record.id
  Object.assign(form, {
    role: record.role,
    resource: record.resource,
    resourceType: record.resourceType,
    actions: [...record.actions],
    masking: record.masking
  })
  modalOpen.value = true
}

async function save() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  const now = dayjs().format('YYYY-MM-DD HH:mm')
  try {
    if (editingId.value) {
      try {
        await governanceApi.updatePermission(editingId.value, { ...form })
      } catch {
        // 后端未就绪，本地更新用于演示
      }
      const idx = permissions.value.findIndex((p) => p.id === editingId.value)
      if (idx >= 0) permissions.value[idx] = { ...permissions.value[idx], ...form, updatedAt: now }
      message.success('已更新授权')
    } else {
      try {
        await governanceApi.createPermission({ ...form })
      } catch {
        // 后端未就绪，本地新增用于演示
      }
      const nextId = Math.max(0, ...permissions.value.map((p) => p.id)) + 1
      permissions.value.unshift({ id: nextId, ...form, actions: [...form.actions], updatedAt: now })
      message.success('已新增授权')
    }
    modalOpen.value = false
  } finally {
    saving.value = false
  }
}

async function remove(record: Permission) {
  try {
    await governanceApi.deletePermission(record.id)
  } catch {
    // 后端未就绪，本地删除用于演示
  }
  permissions.value = permissions.value.filter((p) => p.id !== record.id)
  message.success('已删除授权')
}

load()
</script>

<style scoped>
.page-container { padding: 8px; }
.danger { color: #cf1322; }
</style>
