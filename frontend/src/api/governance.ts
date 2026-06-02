import request from './index'

// ---- 质量看板 ----
export interface QualityOverview {
  totalTables: number
  monitoredTables: number
  passRate: number // 0-100
  ruleCount: number
  failedRuleCount: number
}

export interface QualityRule {
  id: number
  name: string
  table: string
  dimension: string // 完整性/准确性/一致性/唯一性/及时性
  passRate: number
  status: 'pass' | 'warn' | 'fail'
  lastRun: string
}

export interface QualityTrendPoint {
  date: string
  passRate: number
}

export interface QualityDimensionScore {
  dimension: string
  score: number
}

// ---- 血缘图 ----
export interface LineageNode {
  id: string
  label: string
  nodeType: 'source' | 'table' | 'job' | 'report'
}

export interface LineageEdge {
  source: string
  target: string
  label?: string
}

export interface LineageGraphData {
  nodes: LineageNode[]
  edges: LineageEdge[]
}

// ---- 权限配置 ----
export type MaskingLevel = 'none' | 'partial' | 'full'

export interface Permission {
  id: number
  role: string
  resource: string
  resourceType: 'table' | 'api' | 'column'
  actions: string[] // read / write / export / delete
  masking: MaskingLevel
  updatedAt: string
}

// 后端数据治理 API 尚未实现，接口契约先在此预留；视图在请求失败时回退到 mock 数据。
export const governanceApi = {
  qualityOverview: () => request.get('/governance/quality/overview'),
  qualityRules: () => request.get('/governance/quality/rules'),
  qualityTrend: (days = 14) => request.get('/governance/quality/trend', { params: { days } }),
  qualityDimensions: () => request.get('/governance/quality/dimensions'),
  lineage: (table: string) => request.get('/governance/lineage', { params: { table } }),
  permissions: () => request.get('/governance/permissions'),
  createPermission: (data: Partial<Permission>) => request.post('/governance/permissions', data),
  updatePermission: (id: number, data: Partial<Permission>) =>
    request.put(`/governance/permissions/${id}`, data),
  deletePermission: (id: number) => request.delete(`/governance/permissions/${id}`)
}
