import request from './index'

// 响应拦截器返回的统一信封结构（见 api/index.ts）
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

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
// 第二个泛型参数覆盖 axios 默认返回类型，使 await 结果为 ApiResponse<T>（拦截器已剥离到信封层）。
export const governanceApi = {
  qualityOverview: () =>
    request.get<unknown, ApiResponse<QualityOverview>>('/governance/quality/overview'),
  qualityRules: () => request.get<unknown, ApiResponse<QualityRule[]>>('/governance/quality/rules'),
  qualityTrend: (days = 14) =>
    request.get<unknown, ApiResponse<QualityTrendPoint[]>>('/governance/quality/trend', {
      params: { days }
    }),
  qualityDimensions: () =>
    request.get<unknown, ApiResponse<QualityDimensionScore[]>>('/governance/quality/dimensions'),
  lineage: (table: string) =>
    request.get<unknown, ApiResponse<LineageGraphData>>('/governance/lineage', {
      params: { table }
    }),
  permissions: () => request.get<unknown, ApiResponse<Permission[]>>('/governance/permissions'),
  createPermission: (data: Partial<Permission>) =>
    request.post<unknown, ApiResponse<Permission>>('/governance/permissions', data),
  updatePermission: (id: number, data: Partial<Permission>) =>
    request.put<unknown, ApiResponse<Permission>>(`/governance/permissions/${id}`, data),
  deletePermission: (id: number) =>
    request.delete<unknown, ApiResponse<void>>(`/governance/permissions/${id}`)
}
