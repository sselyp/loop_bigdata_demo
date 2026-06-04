import type {
  LineageGraphData,
  Permission,
  QualityDimensionScore,
  QualityOverview,
  QualityRule,
  QualityTrendPoint
} from '@/api/governance'

// 演示用 mock 数据。后端数据治理 API 就绪后，视图会优先使用真实接口，请求失败时回退到这里。

export const mockQualityOverview: QualityOverview = {
  totalTables: 326,
  monitoredTables: 248,
  passRate: 94.2,
  ruleCount: 512,
  failedRuleCount: 23
}

export const mockQualityDimensions: QualityDimensionScore[] = [
  { dimension: '完整性', score: 96 },
  { dimension: '准确性', score: 92 },
  { dimension: '一致性', score: 89 },
  { dimension: '唯一性', score: 98 },
  { dimension: '及时性', score: 90 }
]

export const mockQualityTrend: QualityTrendPoint[] = Array.from({ length: 14 }).map((_, i) => {
  const d = new Date()
  d.setDate(d.getDate() - (13 - i))
  const date = `${d.getMonth() + 1}/${d.getDate()}`
  const passRate = Number((90 + Math.sin(i / 2) * 4 + (i % 3)).toFixed(1))
  return { date, passRate }
})

export const mockQualityRules: QualityRule[] = [
  { id: 1, name: '订单金额非空', table: 'dwd_order', dimension: '完整性', passRate: 99.8, status: 'pass', lastRun: '2026-06-02 08:00' },
  { id: 2, name: '用户ID唯一', table: 'dim_user', dimension: '唯一性', passRate: 100, status: 'pass', lastRun: '2026-06-02 08:00' },
  { id: 3, name: '支付状态枚举校验', table: 'dwd_payment', dimension: '准确性', passRate: 97.1, status: 'warn', lastRun: '2026-06-02 08:00' },
  { id: 4, name: '日分区数据及时到达', table: 'dws_sales_daily', dimension: '及时性', passRate: 85.3, status: 'fail', lastRun: '2026-06-02 08:00' },
  { id: 5, name: '币种一致性', table: 'dwd_order', dimension: '一致性', passRate: 96.4, status: 'warn', lastRun: '2026-06-02 08:00' }
]

export const mockLineage: LineageGraphData = {
  nodes: [
    { id: 'src_mysql_order', label: 'MySQL: 订单库', nodeType: 'source' },
    { id: 'src_kafka_log', label: 'Kafka: 行为日志', nodeType: 'source' },
    { id: 'ods_order', label: 'ods_order', nodeType: 'table' },
    { id: 'ods_user_log', label: 'ods_user_log', nodeType: 'table' },
    { id: 'job_clean', label: 'ETL: 清洗作业', nodeType: 'job' },
    { id: 'dwd_order', label: 'dwd_order', nodeType: 'table' },
    { id: 'job_agg', label: 'ETL: 聚合作业', nodeType: 'job' },
    { id: 'dws_sales_daily', label: 'dws_sales_daily', nodeType: 'table' },
    { id: 'report_sales', label: '销售日报', nodeType: 'report' }
  ],
  edges: [
    { source: 'src_mysql_order', target: 'ods_order', label: '接入' },
    { source: 'src_kafka_log', target: 'ods_user_log', label: '接入' },
    { source: 'ods_order', target: 'job_clean' },
    { source: 'ods_user_log', target: 'job_clean' },
    { source: 'job_clean', target: 'dwd_order', label: '产出' },
    { source: 'dwd_order', target: 'job_agg' },
    { source: 'job_agg', target: 'dws_sales_daily', label: '产出' },
    { source: 'dws_sales_daily', target: 'report_sales', label: '消费' }
  ]
}

export const mockLineageTables = ['dwd_order', 'dws_sales_daily', 'ods_order', 'dim_user']

export const mockPermissions: Permission[] = [
  { id: 1, role: '数据分析师', resource: 'dws_sales_daily', resourceType: 'table', actions: ['read', 'export'], masking: 'none', updatedAt: '2026-05-30 14:21' },
  { id: 2, role: '运营', resource: 'dim_user', resourceType: 'table', actions: ['read'], masking: 'partial', updatedAt: '2026-05-29 10:05' },
  { id: 3, role: '外部合作方', resource: 'dwd_order', resourceType: 'table', actions: ['read'], masking: 'full', updatedAt: '2026-05-28 09:40' },
  { id: 4, role: '数据工程师', resource: '/api/governance/lineage', resourceType: 'api', actions: ['read', 'write'], masking: 'none', updatedAt: '2026-06-01 16:12' },
  { id: 5, role: '风控', resource: 'dim_user.phone', resourceType: 'column', actions: ['read'], masking: 'partial', updatedAt: '2026-05-27 11:33' }
]
