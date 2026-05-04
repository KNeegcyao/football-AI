<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="(item, index) in statCards" :key="item.title">
        <div class="stat-card" :class="`card-${index}`" :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="stat-icon-wrapper" :style="{ background: item.iconBg }">
            <el-icon size="24" :color="item.iconColor">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">{{ item.title }}</div>
            <div class="stat-value">{{ formatNumber(item.value) }}</div>
            <div class="stat-trend" v-if="item.trend !== undefined">
              <el-icon size="14" :color="item.trend >= 0 ? '#22c55e' : '#ef4444'">
                <ArrowUp v-if="item.trend >= 0" />
                <ArrowDown v-else />
              </el-icon>
              <span :style="{ color: item.trend >= 0 ? '#22c55e' : '#ef4444' }">
                {{ Math.abs(item.trend) }}%
              </span>
              <span class="trend-label">较昨日</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon size="18" color="#10b981"><TrendCharts /></el-icon>
                <span>数据趋势</span>
              </div>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon size="18" color="#10b981"><PieChart /></el-icon>
                <span>内容分布</span>
              </div>
            </div>
          </template>
          <div ref="pieRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="action-row">
      <el-col :xs="24">
        <el-card class="action-card" shadow="never">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <el-icon size="18" color="#10b981"><Operation /></el-icon>
                <span>快捷操作</span>
              </div>
            </div>
          </template>
          <div class="action-grid">
            <div 
              v-for="action in quickActions" 
              :key="action.path"
              class="action-item"
              @click="$router.push(action.path)"
            >
              <div class="action-icon" :style="{ background: action.bg }">
                <el-icon size="24" :color="action.color">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <div class="action-title">{{ action.title }}</div>
              <div class="action-desc">{{ action.desc }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { getDashboardStats, getDashboardTrend } from '../../api/dashboard'
import { formatNumber } from '../../utils/format'
import { 
  User, Document, ChatDotRound, PriceTag,
  ArrowUp, ArrowDown, TrendCharts, PieChart, Operation,
  Plus, EditPen, Delete, Search
} from '@element-plus/icons-vue'

const router = useRouter()
const stats = ref({})
const trend = ref([])
const trendDays = ref(7)
const chartRef = ref()
const pieRef = ref()

const statCards = computed(() => [
  { 
    title: '总用户数', 
    value: stats.value.totalUsers || 0, 
    trend: 12.5,
    icon: 'User',
    iconBg: 'rgba(59, 130, 246, 0.15)',
    iconColor: '#3b82f6'
  },
  { 
    title: '总帖子数', 
    value: stats.value.totalPosts || 0, 
    trend: 8.3,
    icon: 'Document',
    iconBg: 'rgba(16, 185, 129, 0.15)',
    iconColor: '#10b981'
  },
  { 
    title: '总评论数', 
    value: stats.value.totalComments || 0, 
    trend: -2.1,
    icon: 'ChatDotRound',
    iconBg: 'rgba(245, 158, 11, 0.15)',
    iconColor: '#f59e0b'
  },
  { 
    title: '总话题数', 
    value: stats.value.totalTopics || 0, 
    trend: 5.7,
    icon: 'PriceTag',
    iconBg: 'rgba(139, 92, 246, 0.15)',
    iconColor: '#8b5cf6'
  }
])

const quickActions = [
  { title: '新增话题', desc: '创建新的讨论话题', icon: 'Plus', path: '/topics', bg: 'rgba(16, 185, 129, 0.15)', color: '#10b981' },
  { title: '发布资讯', desc: '发布最新足球资讯', icon: 'EditPen', path: '/news', bg: 'rgba(59, 130, 246, 0.15)', color: '#3b82f6' },
  { title: '审核帖子', desc: '管理用户发布内容', icon: 'Search', path: '/posts', bg: 'rgba(245, 158, 11, 0.15)', color: '#f59e0b' },
  { title: '清理评论', desc: '删除违规评论内容', icon: 'Delete', path: '/comments', bg: 'rgba(239, 68, 68, 0.15)', color: '#ef4444' }
]

const initLineChart = () => {
  if (!chartRef.value || trend.value.length === 0) return
  
  const chart = echarts.init(chartRef.value)
  const dates = trend.value.map(item => item.date)
  const newUsers = trend.value.map(item => item.newUsers)
  const newPosts = trend.value.map(item => item.newPosts)
  const newComments = trend.value.map(item => item.newComments)
  
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 41, 59, 0.95)',
      borderColor: '#334155',
      textStyle: { color: '#f1f5f9' },
      axisPointer: {
        lineStyle: { color: '#10b981', type: 'dashed' }
      }
    },
    legend: {
      data: ['新增用户', '新增帖子', '新增评论'],
      textStyle: { color: '#94a3b8' },
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: '#334155' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#1e293b' } },
      axisLabel: { color: '#64748b' }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: newUsers,
        lineStyle: { color: '#3b82f6', width: 3 },
        itemStyle: { color: '#3b82f6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.02)' }
          ])
        }
      },
      {
        name: '新增帖子',
        type: 'line',
        smooth: true,
        data: newPosts,
        lineStyle: { color: '#10b981', width: 3 },
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.02)' }
          ])
        }
      },
      {
        name: '新增评论',
        type: 'line',
        smooth: true,
        data: newComments,
        lineStyle: { color: '#f59e0b', width: 3 },
        itemStyle: { color: '#f59e0b' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0.02)' }
          ])
        }
      }
    ]
  })
  
  window.addEventListener('resize', () => chart.resize())
}

const initPieChart = () => {
  if (!pieRef.value) return
  
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(30, 41, 59, 0.95)',
      borderColor: '#334155',
      textStyle: { color: '#f1f5f9' }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#94a3b8' }
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#0f172a',
          borderWidth: 3
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#f1f5f9'
          }
        },
        data: [
          { value: stats.value.totalPosts || 0, name: '帖子', itemStyle: { color: '#10b981' } },
          { value: stats.value.totalComments || 0, name: '评论', itemStyle: { color: '#3b82f6' } },
          { value: stats.value.totalTopics || 0, name: '话题', itemStyle: { color: '#f59e0b' } },
          { value: stats.value.totalUsers || 0, name: '用户', itemStyle: { color: '#8b5cf6' } }
        ]
      }
    ]
  })
  
  window.addEventListener('resize', () => chart.resize())
}

const loadTrend = async () => {
  try {
    trend.value = await getDashboardTrend(trendDays.value)
    initLineChart()
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  try {
    stats.value = await getDashboardStats()
    await loadTrend()
    initPieChart()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  background: linear-gradient(135deg, var(--bg-secondary), rgba(30, 41, 59, 0.8));
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: all 0.3s ease;
  animation: slideUp 0.6s ease both;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(16, 185, 129, 0.3);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-icon-wrapper {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #e2e8f0;
  margin-bottom: 8px;
  line-height: 1;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
}

.trend-label {
  color: var(--text-tertiary);
  font-weight: 500;
  margin-left: 4px;
}

.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  height: 420px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  color: #e2e8f0;
  letter-spacing: 0.3px;
}

.chart-container {
  height: 340px;
}

.action-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.action-item {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8), rgba(15, 23, 42, 0.6));
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  transform: translateY(-4px);
  border-color: rgba(16, 185, 129, 0.3);
  box-shadow: 0 8px 32px rgba(16, 185, 129, 0.15);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.action-desc {
  font-size: 13px;
  color: var(--text-tertiary);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: 16px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-radio-button__inner) {
  background: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, var(--primary-600), var(--primary-500));
  border-color: var(--primary-500);
  box-shadow: none;
}
</style>
