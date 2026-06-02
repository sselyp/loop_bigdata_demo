<template>
  <div class="gateway-page">
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="apis" tab="API Management">
        <a-card bordered="false">
          <template #extra>
            <a-space>
              <a-select v-model:value="statusFilter" placeholder="Filter" allow-clear style="width:140px" @change="loadApis">
                <a-select-option value="">All</a-select-option>
                <a-select-option value="DRAFT">Draft</a-select-option>
                <a-select-option value="PUBLISHED">Published</a-select-option>
                <a-select-option value="DEPRECATED">Deprecated</a-select-option>
              </a-select>
              <a-button type="primary" @click="showApiModal=true;editingApi=null"><plus-outlined /> New</a-button>
            </a-space>
          </template>
          <a-table :columns="apiColumns" :data-source="apis" :loading="apiLoading" row-key="id" size="middle" :pagination="{pageSize:10}">
            <template #bodyCell="{column,record}">
              <template v-if="column.key==='method'">
                <a-tag :color="methodColor(record.method)">{{record.method}}</a-tag>
              </template>
              <template v-if="column.key==='status'">
                <a-tag :color="record.status==='PUBLISHED'?'green':record.status==='DRAFT'?'orange':'default'">{{statusLabel(record.status)}}</a-tag>
              </template>
              <template v-if="column.key==='action'">
                <a-space>
                  <a @click="editApi(record)">Edit</a>
                  <a v-if="record.status==='DRAFT'" @click="publishApi(record.id)">Publish</a>
                  <a v-if="record.status==='PUBLISHED'" @click="deprecateApi(record.id)" style="color:#faad14">Deprecate</a>
                  <a-popconfirm title="Delete?" @confirm="deleteApi(record.id)"><a style="color:#ff4d4f">Delete</a></a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
      <a-tab-pane key="keys" tab="API Keys">
        <a-card bordered="false">
          <template #extra><a-button type="primary" @click="showKeyModal=true"><plus-outlined /> Generate</a-button></template>
          <a-table :columns="keyColumns" :data-source="keys" :loading="keyLoading" row-key="id" size="middle" :pagination="{pageSize:10}">
            <template #bodyCell="{column,record}">
              <template v-if="column.key==='status'">
                <a-tag :color="record.status==='ACTIVE'?'green':record.status==='EXPIRED'?'orange':'red'">{{record.status==='ACTIVE'?'Active':record.status==='EXPIRED'?'Expired':record.status}}</a-tag>
              </template>
              <template v-if="column.key==='keyValue'">
                <a-typography-text copyable :code="true" style="font-size:12px">{{record.keyValue}}</a-typography-text>
              </template>
              <template v-if="column.key==='lastUsedAt'">{{record.lastUsedAt?formatTime(record.lastUsedAt):'Never'}}</template>
              <template v-if="column.key==='expiresAt'">{{record.expiresAt?formatTime(record.expiresAt):'Never'}}</template>
              <template v-if="column.key==='action'">
                <a-popconfirm v-if="record.status==='ACTIVE'" title="Revoke?" @confirm="revokeKey(record.id)"><a style="color:#ff4d4f">Revoke</a></a-popconfirm>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
      <a-tab-pane key="logs" tab="Call Logs">
        <a-card bordered="false">
          <template #extra><a-button @click="loadLogs" :loading="logLoading"><reload-outlined /> Refresh</a-button></template>
          <a-table :columns="logColumns" :data-source="logs" :loading="logLoading" row-key="id" size="middle" :pagination="{pageSize:15,showSizeChanger:true}">
            <template #bodyCell="{column,record}">
              <template v-if="column.key==='responseStatus'"><a-tag :color="record.responseStatus===200?'green':'red'">{{record.responseStatus}}</a-tag></template>
              <template v-if="column.key==='responseTimeMs'"><span :style="{color:record.responseTimeMs>1000?'#ff4d4f':'#52c41a'}">{{record.responseTimeMs}}ms</span></template>
              <template v-if="column.key==='createTime'">{{formatTime(record.createTime)}}</template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
    </a-tabs>
    <a-modal v-model:open="showApiModal" :title="editingApi?'Edit API':'New API'" :width="640" @ok="saveApi" @cancel="showApiModal=false" :confirm-loading="apiSaving">
      <a-form :model="apiForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12"><a-form-item label="Name" required><a-input v-model:value="apiForm.name" placeholder="API name"/></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="Method" required><a-select v-model:value="apiForm.method"><a-select-option value="GET">GET</a-select-option><a-select-option value="POST">POST</a-select-option><a-select-option value="PUT">PUT</a-select-option><a-select-option value="DELETE">DELETE</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="Version"><a-select v-model:value="apiForm.version"><a-select-option value="v1">v1</a-select-option><a-select-option value="v2">v2</a-select-option></a-select></a-form-item></a-col>
        </a-row>
        <a-form-item label="Path" required><a-input v-model:value="apiForm.path" placeholder="/api/v1/data/orders"/></a-form-item>
        <a-form-item label="Description"><a-textarea v-model:value="apiForm.description" :rows="2"/></a-form-item>
        <a-form-item label="SQL Template"><a-textarea v-model:value="apiForm.queryTemplate" :rows="3" placeholder="SELECT * FROM t WHERE id=:id"/></a-form-item>
        <a-row :gutter="16">
          <a-col :span="8"><a-form-item label="Rate/min"><a-input-number v-model:value="apiForm.rateLimit" :min="1" style="width:100%"/></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="Cache(s)"><a-input-number v-model:value="apiForm.cacheTtl" :min="0" style="width:100%"/></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="Datasource"><a-input-number v-model:value="apiForm.datasourceId" style="width:100%"/></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>
    <a-modal v-model:open="showKeyModal" title="Generate API Key" @ok="saveKey" @cancel="showKeyModal=false" :confirm-loading="keySaving">
      <a-form :model="keyForm" layout="vertical">
        <a-form-item label="Name" required><a-input v-model:value="keyForm.name" placeholder="App name"/></a-form-item>
        <a-form-item label="Role"><a-input v-model:value="keyForm.roleName" placeholder="Role"/></a-form-item>
        <a-form-item label="Expires"><a-date-picker v-model:value="keyForm.expiresAt" show-time placeholder="Never" style="width:100%"/></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
// @ts-nocheck - template functions used in Vue SFC, vue-tsc may not detect
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { gatewayApi, type ApiDefinition, type ApiKey, type ApiCallLog } from '@/api/gateway'

const activeTab = ref("apis")
const statusFilter = ref("")
const apis = ref<ApiDefinition[]>([])
const apiLoading = ref(false)
const apiSaving = ref(false)
const showApiModal = ref(false)
const editingApi = ref<ApiDefinition | null>(null)
const apiForm = reactive<ApiDefinition>({name:"",path:"/api/v1/",method:"GET",version:"v1",rateLimit:100,cacheTtl:0})
const keys = ref<ApiKey[]>([])
const keyLoading = ref(false)
const keySaving = ref(false)
const showKeyModal = ref(false)
const keyForm = reactive<ApiKey>({name:"",roleName:""})
const logs = ref<ApiCallLog[]>([])
const logLoading = ref(false)

const apiColumns=[{title:"Name",dataIndex:"name",key:"name"},{title:"Method",dataIndex:"method",key:"method",width:80},{title:"Path",dataIndex:"path",key:"path"},{title:"Version",dataIndex:"version",key:"version",width:60},{title:"Rate",dataIndex:"rateLimit",key:"rateLimit",width:80},{title:"Status",dataIndex:"status",key:"status",width:90},{title:"Action",key:"action",width:200}]
const keyColumns=[{title:"Name",dataIndex:"name",key:"name"},{title:"Key",dataIndex:"keyValue",key:"keyValue",width:220},{title:"Role",dataIndex:"roleName",key:"roleName",width:100},{title:"LastUsed",dataIndex:"lastUsedAt",key:"lastUsedAt"},{title:"Expires",dataIndex:"expiresAt",key:"expiresAt"},{title:"Status",dataIndex:"status",key:"status",width:90},{title:"Action",key:"action",width:80}]
const logColumns=[{title:"API",dataIndex:"apiId",key:"apiId",width:70},{title:"Method",dataIndex:"requestMethod",key:"requestMethod",width:80},{title:"Path",dataIndex:"requestPath",key:"requestPath"},{title:"Status",dataIndex:"responseStatus",key:"responseStatus",width:90},{title:"Latency",dataIndex:"responseTimeMs",key:"responseTimeMs",width:100},{title:"IP",dataIndex:"callerIp",key:"callerIp",width:130},{title:"Time",dataIndex:"createTime",key:"createTime",width:170}]

function methodColor(m:string|undefined){if(m==="GET")return"blue";if(m==="POST")return"green";if(m==="PUT")return"orange";if(m==="DELETE")return"red";return"default"}
function statusLabel(s:string|undefined){if(s==="PUBLISHED")return"Published";if(s==="DRAFT")return"Draft";return s||"-"}
function formatTime(t:string|undefined){if(!t)return"-";try{return new Date(t).toLocaleString("zh-CN")}catch{return t}}

async function loadApis(){apiLoading.value=true;try{const r=await gatewayApi.listApis(statusFilter.value||undefined);apis.value=r.data||[]}catch(e:any){message.error("Failed")}finally{apiLoading.value=false}}
function editApi(rec:ApiDefinition){editingApi.value=rec;Object.assign(apiForm,rec);showApiModal.value=true}
async function saveApi(){apiSaving.value=true;try{if(editingApi.value?.id){await gatewayApi.updateApi(editingApi.value.id,{...apiForm})}else{await gatewayApi.createApi({...apiForm})};showApiModal.value=false;resetForm();await loadApis()}catch(e:any){message.error("Failed:"+(e.message||""))}finally{apiSaving.value=false}}
function resetForm(){editingApi.value=null;Object.assign(apiForm,{name:"",path:"/api/v1/",method:"GET",version:"v1",queryTemplate:"",description:"",datasourceId:undefined,rateLimit:100,cacheTtl:0})}
async function deleteApi(id:number|undefined){if(!id)return;try{await gatewayApi.deleteApi(id);await loadApis()}catch(e:any){}}
async function publishApi(id:number|undefined){if(!id)return;try{await gatewayApi.publishApi(id);await loadApis()}catch(e:any){}}
async function deprecateApi(id:number|undefined){if(!id)return;try{await gatewayApi.deprecateApi(id);await loadApis()}catch(e:any){}}
async function loadKeys(){keyLoading.value=true;try{const r=await gatewayApi.listKeys();keys.value=r.data||[]}catch(e:any){}finally{keyLoading.value=false}}
async function saveKey(){keySaving.value=true;try{await gatewayApi.createKey({...keyForm});showKeyModal.value=false;Object.assign(keyForm,{name:"",roleName:""});await loadKeys()}catch(e:any){}finally{keySaving.value=false}}
async function revokeKey(id:number|undefined){if(!id)return;try{await gatewayApi.revokeKey(id);await loadKeys()}catch(e:any){}}
async function loadLogs(){logLoading.value=true;try{const r=await gatewayApi.listCallLogs();logs.value=r.data||[]}catch(e:any){}finally{logLoading.value=false}}
onMounted(()=>{loadApis();loadKeys();loadLogs()})
</script>

<style scoped>
.gateway-page{padding:0}
</style>