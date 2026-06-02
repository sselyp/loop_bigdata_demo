import request from './index'

export interface ApiDefinition {
  id?: number
  name: string
  path: string
  method: string
  description?: string
  version?: string
  datasourceId?: number
  queryTemplate?: string
  responseType?: string
  rateLimit?: number
  cacheTtl?: number
  status?: string
  publishedAt?: string
  deprecatedAt?: string
  createTime?: string
}

export interface ApiKey {
  id?: number
  name: string
  keyValue?: string
  secretValue?: string
  roleName?: string
  rateLimitOverride?: number
  expiresAt?: string
  lastUsedAt?: string
  status?: string
  createTime?: string
}

export interface ApiCallLog {
  id?: number
  apiId?: number
  apiKeyId?: number
  callerIp?: string
  requestMethod?: string
  requestPath?: string
  requestParams?: string
  responseStatus?: number
  responseTimeMs?: number
  errorMessage?: string
  createTime?: string
}

export const gatewayApi = {
  // API Definitions
  listApis: (status?: string) =>
    request.get('/gateway/apis' + (status ? '?status=' + status : '')),
  getApi: (id: number) => request.get('/gateway/apis/' + id),
  createApi: (data: ApiDefinition) => request.post('/gateway/apis', data),
  updateApi: (id: number, data: ApiDefinition) => request.put('/gateway/apis/' + id, data),
  deleteApi: (id: number) => request.delete('/gateway/apis/' + id),
  publishApi: (id: number) => request.post('/gateway/apis/' + id + '/publish'),
  deprecateApi: (id: number) => request.post('/gateway/apis/' + id + '/deprecate'),

  // API Keys
  listKeys: () => request.get('/gateway/keys'),
  createKey: (data: ApiKey) => request.post('/gateway/keys', data),
  revokeKey: (id: number) => request.post('/gateway/keys/' + id + '/revoke'),
  validateKey: (keyValue: string) => request.post('/gateway/keys/validate', keyValue),

  // Call Logs
  listCallLogs: (apiId?: number, limit?: number) =>
    request.get('/gateway/logs' + (apiId ? '?apiId=' + apiId + '&limit=' + (limit || 100) : '?limit=' + (limit || 100))),
}
