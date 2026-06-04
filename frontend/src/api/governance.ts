import {
  mockLineage,
  mockPermissions,
  mockQualityDimensions,
  mockQualityOverview,
  mockQualityRules,
  mockQualityTrend
} from '@/mock/governance'

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

function ok<T>(data: T): Promise<ApiResponse<T>> {
  return Promise.resolve({ code: 200, message: 'success', data })
}

// 后端数据治理 API 与当前前端演示契约尚未完全对齐，治理页先使用 mock 数据保持演示稳定。
export const governanceApi = {
  qualityOverview: () => ok<QualityOverview>({ ...mockQualityOverview }),
  qualityRules: () => ok<QualityRule[]>(mockQualityRules.map((item) => ({ ...item }))),
  qualityTrend: (_days = 14) => ok<QualityTrendPoint[]>(mockQualityTrend.map((item) => ({ ...item }))),
  qualityDimensions: () =>
    ok<QualityDimensionScore[]>(mockQualityDimensions.map((item) => ({ ...item }))),
  lineage: (_table: string) =>
    ok<LineageGraphData>({
      nodes: mockLineage.nodes.map((item) => ({ ...item })),
      edges: mockLineage.edges.map((item) => ({ ...item }))
    }),
  permissions: () => ok<Permission[]>(mockPermissions.map((item) => ({ ...item, actions: [...item.actions] }))),
  createPermission: (data: Partial<Permission>) =>
    ok<Permission>({
      id: Date.now(),
      role: data.role || '',
      resource: data.resource || '',
      resourceType: data.resourceType || 'table',
      actions: data.actions || ['read'],
      masking: data.masking || 'none',
      updatedAt: data.updatedAt || ''
    }),
  updatePermission: (id: number, data: Partial<Permission>) =>
    ok<Permission>({
      id,
      role: data.role || '',
      resource: data.resource || '',
      resourceType: data.resourceType || 'table',
      actions: data.actions || ['read'],
      masking: data.masking || 'none',
      updatedAt: data.updatedAt || ''
    }),
  deletePermission: (_id: number) => ok<void>(undefined)
}
