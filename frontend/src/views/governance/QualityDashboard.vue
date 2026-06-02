<template>
  <div class="page-container">
    <a-spin :spinning="loading">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card>
            <a-statistic title="监控表数" :value="overview.monitoredTables" :suffix="`/ ${overview.totalTables}`" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="整体通过率" :value="overview.passRate" suffix="%" :value-style="{ color: passRateColor }" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="质量规则" :value="overview.ruleCount" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="未通过规则" :value="overview.failedRuleCount" :value-style="{ color: '#cf1322' }" />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px">
        <a-col :span="10">
          <a-card title="各维度质量评分">
            <div ref="dimRef" class="chart" />
          </a-card>
        </a-col>
        <a-col :span="14">
          <a-card title="近 14 天质量通过率趋势">
            <div ref="trendRef" class="chart" />
          </a-card>
        </a-col>
      </a-row>

      <a-card title="质量规则明细" style="margin-top: 16px">
        <a-table
          :columns="ruleColumns"
          :data-source="rules"
          :pagination="{ pageSize: 10 }"
          row-key="id"
          size="middle"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'passRate'">{{ record.passRate }}%</template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
          </template>
        </a-table>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import {
  governanceApi,
  type QualityDimensionScore,
  type QualityOverview,
  type QualityRule,
  type QualityTrendPoint
} from '@/api/governance'
import {
  mockQualityDimensions,
  mockQualityOverview,
  mockQualityRules,
  mockQualityTrend
} from '@/mock/governance'

const loading = ref(false)
const overview = ref<QualityOverview>({ ...mockQualityOverview })
const dimensions = ref<QualityDimensionScore[]>([])
const trend = ref<QualityTrendPoint[]>([])
const rules = ref<QualityRule[]>([])

const dimRef = ref<HTMLElement>()
const trendRef = ref<HTMLElement>()
let dimChart: echarts.ECharts | undefined
let trendChart: echarts.ECharts | undefined

const passRateColor = computed(() => (overview.value.passRate >= 95 ? '#3f8600' : '#faad14'))

const ruleColumns = [
  { title: '规则', dataIndex: 'name', key: 'name' },
  { title: '表', dataIndex: 'table', key: 'table' },
  { title: '维度', dataIndex: 'dimension', key: 'dimension' },
  { title: '通过率', dataIndex: 'passRate', key: 'passRate' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '最近执行', dataIndex: 'lastRun', key: 'lastRun' }
]

function statusColor(status: QualityRule['status']) {
  return status === 'pass' ? 'green' : status === 'warn' ? 'orange' : 'red'
}
function statusText(status: QualityRule['status']) {
  return status === 'pass' ? '通过' : status === 'warn' ? '告警' : '失败'
}

async function load() {
  loading.value = true
  try {
    const [o, d, t, r] = await Promise.all([
      governanceApi.qualityOverview(),
      governanceApi.qualityDimensions(),
      governanceApi.qualityTrend(),
      governanceApi.qualityRules()
    ])
    overview.value = (o as any).data
    dimensions.value = (d as any).data
    trend.value = (t as any).data
    rules.value = (r as any).data
  } catch {
    // 后端数据治理 API 尚未实现，回退到 mock 数据
    overview.value = { ...mockQualityOverview }
    dimensions.value = mockQualityDimensions
    trend.value = mockQualityTrend
    rules.value = mockQualityRules
  } finally {
    loading.value = false
    await nextTick()
    renderCharts()
  }
}

function renderCharts() {
  if (dimRef.value) {
    dimChart = dimChart ?? echarts.init(dimRef.value)
    dimChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 60, right: 24, top: 16, bottom: 24 },
      xAxis: { type: 'value', max: 100 },
      yAxis: { type: 'category', data: dimensions.value.map((i) => i.dimension) },
      series: [
        {
          type: 'bar',
          data: dimensions.value.map((i) => i.score),
          itemStyle: { color: '#1677ff', borderRadius: [0, 4, 4, 0] },
          label: { show: true, position: 'right', formatter: '{c}' }
        }
      ]
    })
  }
  if (trendRef.value) {
    trendChart = trendChart ?? echarts.init(trendRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 48, right: 24, top: 24, bottom: 32 },
      xAxis: { type: 'category', boundaryGap: false, data: trend.value.map((i) => i.date) },
      yAxis: { type: 'value', min: 80, max: 100, axisLabel: { formatter: '{value}%' } },
      series: [
        {
          type: 'line',
          smooth: true,
          data: trend.value.map((i) => i.passRate),
          areaStyle: { opacity: 0.15 },
          itemStyle: { color: '#52c41a' }
        }
      ]
    })
  }
}

function handleResize() {
  dimChart?.resize()
  trendChart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  dimChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.page-container { padding: 8px; }
.chart { width: 100%; height: 300px; }
</style>
