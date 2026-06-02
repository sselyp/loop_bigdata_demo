<template>
  <div class="page-container">
    <a-card title="数据血缘">
      <template #extra>
        <a-space>
          <span>目标表：</span>
          <a-select v-model:value="selectedTable" style="width: 200px" :options="tableOptions" @change="load" />
          <a-button @click="fitView">重置视图</a-button>
        </a-space>
      </template>

      <a-spin :spinning="loading">
        <a-row :gutter="16">
          <a-col :span="18">
            <div class="legend">
              <span v-for="l in legends" :key="l.type" class="legend-item">
                <i class="dot" :style="{ background: l.color }" />{{ l.text }}
              </span>
            </div>
            <div ref="graphRef" class="graph">
              <a-empty v-if="!loading && empty" description="暂无血缘数据" />
            </div>
          </a-col>
          <a-col :span="6">
            <a-card size="small" title="节点详情">
              <a-empty v-if="!selectedNode" description="点击节点查看详情" :image="emptyImage" />
              <a-descriptions v-else :column="1" size="small">
                <a-descriptions-item label="名称">{{ selectedNode.label }}</a-descriptions-item>
                <a-descriptions-item label="类型">
                  <a-tag :color="typeColor(selectedNode.nodeType)">{{ typeText(selectedNode.nodeType) }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="标识">{{ selectedNode.id }}</a-descriptions-item>
                <a-descriptions-item label="上游">{{ upstream.join('、') || '无' }}</a-descriptions-item>
                <a-descriptions-item label="下游">{{ downstream.join('、') || '无' }}</a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-col>
        </a-row>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Empty } from 'ant-design-vue'
import G6, { type Graph } from '@antv/g6'
import { governanceApi, type LineageGraphData, type LineageNode } from '@/api/governance'
import { mockLineage, mockLineageTables } from '@/mock/governance'

const emptyImage = Empty.PRESENTED_IMAGE_SIMPLE

const loading = ref(false)
const empty = ref(false)
const selectedTable = ref(mockLineageTables[0])
const tableOptions = mockLineageTables.map((t) => ({ label: t, value: t }))
const graphData = ref<LineageGraphData>({ nodes: [], edges: [] })
const selectedNode = ref<LineageNode | null>(null)

const graphRef = ref<HTMLElement>()
let graph: Graph | undefined

const legends = [
  { type: 'source', text: '数据源', color: '#722ed1' },
  { type: 'table', text: '数据表', color: '#1677ff' },
  { type: 'job', text: 'ETL作业', color: '#fa8c16' },
  { type: 'report', text: '报表/应用', color: '#13c2c2' }
]

const typeColorMap: Record<LineageNode['nodeType'], string> = {
  source: '#722ed1',
  table: '#1677ff',
  job: '#fa8c16',
  report: '#13c2c2'
}
const typeTextMap: Record<LineageNode['nodeType'], string> = {
  source: '数据源',
  table: '数据表',
  job: 'ETL作业',
  report: '报表/应用'
}
function typeColor(t: LineageNode['nodeType']) {
  return typeColorMap[t]
}
function typeText(t: LineageNode['nodeType']) {
  return typeTextMap[t]
}

const upstream = computed(() =>
  selectedNode.value
    ? graphData.value.edges.filter((e) => e.target === selectedNode.value!.id).map((e) => e.source)
    : []
)
const downstream = computed(() =>
  selectedNode.value
    ? graphData.value.edges.filter((e) => e.source === selectedNode.value!.id).map((e) => e.target)
    : []
)

function toG6Data(data: LineageGraphData) {
  return {
    nodes: data.nodes.map((n) => ({
      id: n.id,
      label: n.label,
      nodeType: n.nodeType,
      style: { fill: typeColorMap[n.nodeType], stroke: typeColorMap[n.nodeType], radius: 4 },
      labelCfg: { style: { fill: '#fff', fontSize: 12 } }
    })),
    edges: data.edges.map((e) => ({
      source: e.source,
      target: e.target,
      label: e.label,
      labelCfg: { autoRotate: true, style: { fill: '#888', fontSize: 10 } }
    }))
  }
}

function ensureGraph() {
  if (graph || !graphRef.value) return
  const width = graphRef.value.clientWidth || 800
  graph = new G6.Graph({
    container: graphRef.value,
    width,
    height: 460,
    fitView: true,
    fitViewPadding: 24,
    layout: { type: 'dagre', rankdir: 'LR', nodesep: 24, ranksep: 60 },
    defaultNode: { type: 'rect', size: [130, 38] },
    defaultEdge: { type: 'polyline', style: { stroke: '#bfbfbf', endArrow: true }, routeCfg: { radius: 8 } },
    modes: { default: ['drag-canvas', 'zoom-canvas', 'drag-node'] }
  })
  graph.on('node:click', (evt) => {
    const model = evt.item?.getModel() as { id: string; label: string; nodeType: LineageNode['nodeType'] } | undefined
    if (model) {
      selectedNode.value = { id: model.id, label: model.label, nodeType: model.nodeType }
    }
  })
}

async function load() {
  loading.value = true
  selectedNode.value = null
  try {
    const res = await governanceApi.lineage(selectedTable.value)
    graphData.value = (res as any).data
  } catch {
    // 后端血缘 API 尚未实现，回退到 mock 数据
    graphData.value = mockLineage
  } finally {
    loading.value = false
    empty.value = graphData.value.nodes.length === 0
    ensureGraph()
    if (graph && !empty.value) {
      graph.changeData(toG6Data(graphData.value))
      graph.fitView(24)
    }
  }
}

function fitView() {
  graph?.fitView(24)
}

function handleResize() {
  if (graph && graphRef.value) {
    graph.changeSize(graphRef.value.clientWidth || 800, 460)
    graph.fitView(24)
  }
}

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  graph?.destroy()
})
</script>

<style scoped>
.page-container { padding: 8px; }
.graph { width: 100%; height: 460px; border: 1px solid #f0f0f0; border-radius: 6px; }
.legend { margin-bottom: 8px; }
.legend-item { margin-right: 16px; font-size: 12px; color: #666; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 4px; vertical-align: middle; }
</style>
