<template>
  <div class="gateway-page">
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="apis" tab="API Mgmt">
        <a-card bordered="false">
          <template #extra>
            <a-space>
              <a-select v-model:value="statusFilter" placeholder="Filter" allow-clear style="width: 140px" @change="loadApis">
                <a-select-option value="">All</a-select-option>
                <a-select-option value="DRAFT">Draft</a-select-option>
                <a-select-option value="PUBLISHED">Published</a-select-option>
                <a-select-option value="DEPRECATED">Deprecated</a-select-option>
              </a-select>
              <a-button type="primary" @click="showApiModal = true; editingApi = null">
                <plus-outlined /> New API
              </a-button>
            </a-space>
          </template>
          <p>API management interface - create, edit, publish and deprecate data APIs.</p>
        </a-card>
      </a-tab-pane>
      <a-tab-pane key="keys" tab="API Keys">
        <a-card bordered="false">
          <template #extra>
            <a-button type="primary" @click="showKeyModal = true"><plus-outlined /> Generate Key</a-button>
          </template>
          <p>API key management - generate, view and revoke access keys.</p>
        </a-card>
      </a-tab-pane>
      <a-tab-pane key="logs" tab="Call Logs">
        <a-card bordered="false">
          <template #extra>
            <a-button @click="loadLogs" :loading="logLoading"><reload-outlined /> Refresh</a-button>
          </template>
          <p>API call logs - monitor request volume, latency and error rates.</p>
        </a-card>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { gatewayApi, type ApiDefinition, type ApiKey, type ApiCallLog } from '@/api/gateway'

const activeTab = ref("apis")
const statusFilter = ref("")
const apis = ref<ApiDefinition[]>([])
const apiLoading = ref(false)
const showApiModal = ref(false)
const keys = ref<ApiKey[]>([])
const keyLoading = ref(false)
const showKeyModal = ref(false)
const logs = ref<ApiCallLog[]>([])
const logLoading = ref(false)

function methodColor(m: string | undefined) {
  if (m === "GET") return "blue"
  if (m === "POST") return "green"
  if (m === "PUT") return "orange"
  if (m === "DELETE") return "red"
  return "default"
}
function formatTime(time: string | undefined) {
  if (!time) return "-"
  try { return new Date(time).toLocaleString("zh-CN") }
  catch { return time }
}
async function loadApis() {
  apiLoading.value = true
  try {
    const r = await gatewayApi.listApis(statusFilter.value || undefined)
    apis.value = r.data || []
  } catch(e: any) { message.error("Failed to load APIs")
  } finally { apiLoading.value = false }
}
async function loadKeys() {
  keyLoading.value = true
  try { const r = await gatewayApi.listKeys(); keys.value = r.data || [] }
  catch(e: any) { message.error("Failed to load keys") }
  finally { keyLoading.value = false }
}
async function loadLogs() {
  logLoading.value = true
  try { const r = await gatewayApi.listCallLogs(); logs.value = r.data || [] }
  catch(e: any) { message.error("Failed to load logs") }
  finally { logLoading.value = false }
}
onMounted(() => { loadApis(); loadKeys(); loadLogs() })
</script>

<style scoped>
.gateway-page { padding: 0; }
</style>
