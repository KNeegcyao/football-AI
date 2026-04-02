<template>
  <view class="schedule-container" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- 固定顶部容器 -->
    <view class="fixed-header">
      <!-- 状态栏占位 -->
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

      <!-- 顶部导航栏 -->
      <view class="nav-bar bg-nav-bar border-b border-theme-main">
        <view class="logo-area">
          <view class="logo-icon">
            <image src="/static/soccer-logo.png" mode="aspectFit" class="logo-img"></image>
          </view>
          <view class="logo-text italic">
            PITCH<text class="highlight text-theme-main">PULSE</text>
          </view>
        </view>

        <view class="nav-actions">
          <view class="action-btn" @click="goToSearch">
            <text class="material-icons" style="font-size: 48rpx; color: var(--text-main);">search</text>
          </view>
          <view class="avatar-box" @click="goToProfile">
            <image class="avatar" :src="userAvatar" mode="aspectFill" @error="handleAvatarError"></image>
          </view>
        </view>
      </view>

      <!-- 日期选择器容器 -->
      <view class="calendar-fixed-area bg-theme-main">
        <!-- 日期选择器标题 -->
        <view class="calendar-header">
          <text class="current-month text-theme-main">{{ currentYearMonth }}</text>
          <text class="view-calendar-btn">查看日历</text>
        </view>
        <!-- 日期选择器滚动条 -->
        <scroll-view scroll-x class="date-selector" show-scrollbar="false">
          <view class="date-list">
            <view 
              v-for="(item, index) in dates" 
              :key="index"
              class="date-item bg-card"
              :class="{ active: activeDateIndex === index }"
              @click="selectDate(index)"
            >
              <text class="date-week" :class="activeDateIndex === index ? 'text-white' : 'text-theme-secondary'">{{ item.week }}</text>
              <text class="date-day" :class="activeDateIndex === index ? 'text-white' : 'text-theme-main'">{{ item.day }}日</text>
              <view class="date-dot" v-if="activeDateIndex === index"></view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 赛事列表内容 -->
    <scroll-view scroll-y class="content-scroll">
      <!-- 顶部占位 -->
      <view class="header-placeholder"></view>
      
      <!-- 正在直播 -->
      <view class="section" v-if="liveMatches.length > 0">
        <view class="match-card live-card bg-card border-theme-main" v-for="match in liveMatches" :key="match.id">
          <view class="live-badge">
            <text class="live-text">LIVE {{ match.liveTime || match.matchMinute || "" }}</text>
          </view>
          <view class="card-body">
            <view class="match-info">
              <text class="competition-text text-theme-secondary">{{ match.competitionName }} · {{ match.round }}</text>
            </view>
            <view class="teams-score">
              <view class="team-side">
                <view class="logo-container">
                  <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
                </view>
                <text class="team-name text-theme-main">{{ match.homeTeam?.name }}</text>
              </view>
              <view class="score-center">
                <view class="score-text text-theme-main">
                  <text class="num">{{ match.homeScore }}</text>
                  <text class="divider">-</text>
                  <text class="num">{{ match.awayScore }}</text>
                </view>
                <view class="status-badge">
                  <text>{{ match.statusName || '进行中' }}</text>
                </view>
              </view>
              <view class="team-side">
                <view class="logo-container">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
              </view>
                <text class="team-name text-theme-main">{{ match.awayTeam?.name }}</text>
              </view>
            </view>
          </view>
          <view class="card-footer border-theme-main">
            <view class="footer-left">
              <text class="material-icons" style="font-size: 28rpx; margin-right: 8rpx; color: #4caf50;">play_circle</text>
              <text class="footer-text text-theme-secondary">视频直播中</text>
            </view>
            <view class="footer-right" @click.stop="openAiAnalysis(match)">
              <text class="ai-label text-theme-secondary">AI 预测:</text>
              <view class="ai-probs">
                <view class="prob-item">
                  <text class="prob-label">主</text>
                  <text class="prob-value">{{ Math.round((match.homeWinProb || 0) * 100) }}%</text>
                </view>
                <view class="prob-item">
                  <text class="prob-label">平</text>
                  <text class="prob-value">{{ Math.round((match.drawProb || 0) * 100) }}%</text>
                </view>
                <view class="prob-item">
                  <text class="prob-label">客</text>
                  <text class="prob-value">{{ Math.round((match.awayWinProb || 0) * 100) }}%</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- AI Analysis Modal -->
      <view v-if="showAiModal" class="ai-modal-overlay">
        <view class="ai-modal-backdrop" @click="showAiModal = false"></view>
        <view class="ai-modal-content bg-card border-theme-main">
          <view class="modal-header">
            <view class="header-title">
              <text class="material-icons ai-icon">psychology</text>
              <text class="title-text text-theme-main">AI 深度预测分析</text>
            </view>
            <view class="close-btn" @click="showAiModal = false">
              <text class="material-icons">close</text>
            </view>
          </view>
          
          <view class="modal-body">
            <view v-if="aiLoading" class="ai-loading">
              <view class="loading-spinner"></view>
              <text class="loading-text text-theme-secondary">AI 正在深度分析对阵数据...</text>
            </view>
            <scroll-view v-else scroll-y class="ai-result-scroll">
              <view class="ai-analysis-text text-theme-main">
                <text>{{ aiAnalysisResult }}</text>
              </view>
            </scroll-view>
          </view>
          
          <view class="modal-footer">
            <button class="modal-close-btn bg-primary" @click="showAiModal = false">完成</button>
          </view>
        </view>
      </view>

      <!-- 即将开始 -->
      <view class="section" v-if="upcomingMatches.length > 0">
        <view class="section-title text-theme-secondary">即将开始</view>
        <view class="match-card-mini bg-card border-theme-main" v-for="match in upcomingMatches" :key="match.id">
          <view class="mini-header">
            <view class="header-left">
              <view class="indicator"></view>
              <text class="match-meta text-theme-secondary">{{ match.competitionName }} · {{ formatMatchTime(match.matchTime) }}</text>
            </view>
            <text class="material-icons" style="font-size: 44rpx; opacity: 0.2; color: var(--text-main);">notifications</text>
        </view>
        <view class="mini-body">
            <view class="mini-team">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
              </view>
              <text class="mini-name text-theme-main">{{ match.homeTeam?.name }}</text>
            </view>
            <text class="vs-text text-theme-secondary">VS</text>
            <view class="mini-team reverse">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
              </view>
              <text class="mini-name text-theme-main">{{ match.awayTeam?.name }}</text>
            </view>
          </view>
          <!-- 新增：AI 预测显示 -->
          <view class="mini-footer" v-if="match.homeWinProb || match.drawProb || match.awayWinProb">
            <view class="footer-right mini" @click.stop="openAiAnalysis(match)">
              <text class="ai-label text-theme-secondary">AI 预测:</text>
              <view class="ai-probs">
                <view class="prob-item">
                  <text class="prob-label">主</text>
                  <text class="prob-value">{{ Math.round((match.homeWinProb || 0) * 100) }}%</text>
                </view>
                <view class="prob-item">
                  <text class="prob-label">平</text>
                  <text class="prob-value">{{ Math.round((match.drawProb || 0) * 100) }}%</text>
                </view>
                <view class="prob-item">
                  <text class="prob-label">客</text>
                  <text class="prob-value">{{ Math.round((match.awayWinProb || 0) * 100) }}%</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 已完赛 -->
      <view class="section" v-if="finishedMatches.length > 0">
        <view class="section-title text-theme-secondary">已完赛</view>
        <view class="match-card-mini finished bg-card border-theme-main" v-for="match in finishedMatches" :key="match.id">
          <view class="mini-header">
            <view class="header-left">
              <view class="indicator"></view>
              <text class="match-meta text-theme-secondary">{{ match.competitionName }} · {{ formatMatchTime(match.matchTime) }}</text>
            </view>
          </view>
          <view class="mini-body">
            <view class="mini-team">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
              </view>
              <view class="team-info">
                <text class="mini-name text-theme-main">{{ match.homeTeam?.name }}</text>
                <text class="finished-score text-theme-main">{{ match.homeScore }}</text>
              </view>
            </view>
            <text class="vs-dash text-theme-secondary">-</text>
            <view class="mini-team reverse">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
              </view>
              <view class="team-info reverse">
                <text class="mini-name text-theme-main">{{ match.awayTeam?.name }}</text>
                <text class="finished-score text-theme-main" :class="{ 'muted': match.awayScore < match.homeScore }">{{ match.awayScore }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 无比赛提示 -->
      <view class="no-match" v-if="liveMatches.length === 0 && upcomingMatches.length === 0 && finishedMatches.length === 0">
        <text class="material-icons" style="font-size: 128rpx; opacity: 0.1; color: var(--text-main);">info</text>
        <text class="no-match-text text-theme-secondary">今日暂无比赛</text>
      </view>
      
      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部导航占位 -->
    <view class="safe-area-bottom"></view>

    <!-- 底部导航栏 -->
    <CustomTabBar :currentTab="1" />
  </view>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { matchApi, userApi } from '@/api'
import { getFullImageUrl } from '@/utils/request'
import { useThemeStore } from '@/store/theme'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)
const liveMatches = ref([])
const upcomingMatches = ref([])
const finishedMatches = ref([])
const userAvatar = ref('/static/soccer-logo.png')
const navbarPaddingRight = ref(16) // 默认 16px

const showAiModal = ref(false)
const aiLoading = ref(false)
const aiAnalysisResult = ref('')

const openAiAnalysis = async (match) => {
  showAiModal.value = true
  aiLoading.value = true
  aiAnalysisResult.value = ''
  
  try {
    const res = await matchApi.predict({
      teamA: match.homeTeam?.name || '主队',
      teamB: match.awayTeam?.name || '客队',
      recentForm: `${match.homeTeam?.name} 近期状态良好，${match.awayTeam?.name} 略有起伏。`
    })
    
    if (res) {
      aiAnalysisResult.value = res
    } else {
      aiAnalysisResult.value = 'AI 分析暂时不可用，请稍后再试。'
    }
  } catch (e) {
    console.error('AI Prediction failed:', e)
    aiAnalysisResult.value = '网络请求失败，请检查网络连接。'
  } finally {
    aiLoading.value = false
  }
}

const dates = ref([])
  const activeDateIndex = ref(0)
  const currentYearMonth = ref('')
  let refreshTimer = null

const handleAvatarError = () => {
  userAvatar.value = '/static/soccer-logo.png'
}

const getUserProfile = async () => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) {
      userAvatar.value = '/static/soccer-logo.png'
      return
    }
    const res = await userApi.getProfile()
    // 兼容：res 直接是数据，或者是包含 data 属性的对象
    const userData = res?.data || res
    if (userData && userData.avatar) {
      userAvatar.value = getFullImageUrl(userData.avatar)
    } else {
      userAvatar.value = '/static/soccer-logo.png'
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
    userAvatar.value = '/static/soccer-logo.png'
  }
}

const goToProfile = () => {
  uni.switchTab({
    url: '/pages/my/my'
  })
}

onShow(() => {
  uni.hideTabBar()
  getUserProfile()
})

const leagues = ref([
  { id: 0, name: '全部', icon: 'sports_soccer' },
  { id: 1, name: '中超', icon: 'flag' },
  { id: 2, name: '英超', icon: 'military_tech' },
  { id: 3, name: '西甲', icon: 'emoji_events' },
  { id: 4, name: '德甲', icon: 'workspace_premium' },
  { id: 5, name: '意甲', icon: 'stars' },
  { id: 6, name: '法甲', icon: 'workspace_premium' },
  { id: 7, name: '欧冠', icon: 'trophy' }
])

const currentLeague = ref(0)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const matches = ref([])
const loading = ref(false)

// 初始化日期列表（前后3天）
const initDates = () => {
  const list = []
  const today = new Date()
  
  // 前3天
  for (let i = 3; i >= 1; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    list.push({
      week: getWeekName(d),
      day: d.getDate(),
      fullDate: formatFullDate(d),
      isToday: false
    })
  }
  
  // 今天
  list.push({
    week: '今天',
    day: today.getDate(),
    fullDate: formatFullDate(today),
    isToday: true
  })
  
  // 后3天
  for (let i = 1; i <= 3; i++) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    list.push({
      week: getWeekName(d),
      day: d.getDate(),
      fullDate: formatFullDate(d),
      isToday: false
    })
  }
  
  dates.value = list
  // 默认选中今天 (索引为3)
  activeDateIndex.value = 3
  updateCurrentYearMonth(today)
}

const updateCurrentYearMonth = (date) => {
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  currentYearMonth.value = `${y}年${m}月`
}

const getWeekName = (date) => {
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weeks[date.getDay()]
}

const formatDate = (date) => {
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${m < 10 ? '0' + m : m}-${d < 10 ? '0' + d : d}`
}

const formatFullDate = (date) => {
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${y}-${m < 10 ? '0' + m : m}-${d < 10 ? '0' + d : d}`
}

const formatMatchTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const h = date.getHours()
  const m = date.getMinutes()
  return `${h < 10 ? '0' + h : h}:${m < 10 ? '0' + m : m}`
}

const formatMatchDate = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${m < 10 ? '0' + m : m}-${d < 10 ? '0' + d : d}`
}

const handleImageError = (team) => {
  if (team) {
    team.logoUrl = '/static/teams/generic_logo.png'
  }
}

const goToSearch = () => {
  uni.navigateTo({
    url: '/pages/search/search'
  })
}

// 获取比赛数据
const fetchMatches = async () => {
  try {
    const dateItem = dates.value[activeDateIndex.value]
    if (!dateItem) return
    
    console.log('Fetching matches for date:', dateItem.fullDate)
    const res = await matchApi.getByDate(dateItem.fullDate)
    
    // 兼容：res 直接是数组，或者是包含 records/data 的对象
    const actualMatches = Array.isArray(res) 
      ? res 
      : (res?.records || res?.data || [])
    
    console.log('Fetched matches:', actualMatches.length)
    
    if (Array.isArray(actualMatches) && actualMatches.length > 0) {
      // 状态码兼容：0:未开始, 1:进行中, 2:已结束 (请根据后端实际情况调整)
      liveMatches.value = actualMatches.filter(m => m.status === 1 || m.status === '1')
      upcomingMatches.value = actualMatches.filter(m => m.status === 0 || m.status === '0')
      finishedMatches.value = actualMatches.filter(m => m.status === 2 || m.status === '2')
    } else {
      liveMatches.value = []
      upcomingMatches.value = []
      finishedMatches.value = []
    }
  } catch (e) {
    console.error('获取赛程失败:', e)
    // 只有在非自动刷新时才弹出 Toast，避免干扰
    if (!refreshTimer) {
      uni.showToast({ title: '赛程加载失败', icon: 'none' })
    }
  }
}

const selectDate = (index) => {
  activeDateIndex.value = index
  const selectedDate = new Date(dates.value[index].fullDate)
  updateCurrentYearMonth(selectedDate)
  fetchMatches()
  
  // 切换日期后也开启自动刷新，以便在非“今天”页面停留时也能及时看到数据同步
  startAutoRefresh()
}

// 自动刷新逻辑
const startAutoRefresh = () => {
  stopAutoRefresh()
  // 保持后台定时刷新，获取最新数据（无论是否在“今天”标签）
  refreshTimer = setInterval(fetchMatches, 30000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 页面加载
onMounted(() => {
  initDates()
  fetchMatches()
})
</script>

<style lang="scss" scoped>
.schedule-container {
  height: 100vh; /* 固定高度为视口高度 */
  background-color: var(--bg-main);
  color: var(--text-main);
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  margin: 0 auto;
  overflow: hidden; /* 防止容器本身滚动 */
  box-sizing: border-box;
  transition: background-color 0.3s, color 0.3s;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
}

.content-scroll {
  flex: 1;
  height: 0; /* 必须：强制 flex 计算生效 */
  width: 100%;
  box-sizing: border-box;
}

.bottom-placeholder {
  height: calc(120rpx + env(safe-area-inset-bottom));
  width: 100%;
}

.status-bar {
  width: 100%;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 999;
  background-color: #1a1811;
  
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
}

.header-placeholder {
  height: calc(var(--status-bar-height) + 100rpx + 260rpx); /* statusBarHeight + nav-bar(100rpx) + calendar(260rpx) */
  width: 100%;
}

.no-match {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  
  .no-match-text {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: var(--text-secondary);
  }
}

/* AI Modal Styles */
.ai-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
}

.ai-modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
}

.ai-modal-content {
  position: relative;
  width: 100%;
  max-width: 600rpx;
  background-color: var(--card-bg);
  border-radius: 40rpx;
  border: 1px solid var(--border-main);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalFadeIn 0.3s ease-out;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.modal-header {
  padding: 30rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-main);

  .header-title {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .ai-icon {
      font-size: 48rpx;
      color: #f9d406;
    }

    .title-text {
      font-size: 32rpx;
      font-weight: 700;
      color: var(--text-main);
    }
  }

  .close-btn {
    color: var(--text-secondary);
    padding: 10rpx;
  }
}

.modal-body {
  padding: 40rpx;
  min-height: 300rpx;
  max-height: 60vh;

  .ai-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60rpx 0;

    .loading-spinner {
      width: 64rpx;
      height: 64rpx;
      border: 6rpx solid rgba(249, 212, 6, 0.1);
      border-top-color: #f9d406;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 24rpx;
    }

    .loading-text {
      font-size: 26rpx;
      color: var(--text-secondary);
      text-align: center;
    }
  }

  .ai-result-scroll {
    height: 100%;
  }

  .ai-analysis-text {
    font-size: 28rpx;
    line-height: 1.6;
    color: var(--text-main);
    white-space: pre-wrap;
  }
}

.modal-footer {
  padding: 30rpx 40rpx;
  border-top: 1px solid var(--border-main);

  .modal-close-btn {
    width: 100%;
    height: 88rpx;
    background-color: #f9d406;
    color: #000;
    border-radius: 20rpx;
    font-size: 30rpx;
    font-weight: 700;
    display: flex;
    justify-content: center;
    align-items: center;
    border: none;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.nav-bar {
  width: 100%;
  height: 100rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  box-sizing: border-box;
  background-color: #1a1811;
  border-bottom: 1rpx solid var(--border-main);
}

.calendar-fixed-area {
  width: 100%;
  border-bottom: 1rpx solid var(--border-main);
  background-color: #1a1811;
}

.logo-area {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;

  .logo-icon {
    width: 56rpx;
    height: 56rpx;
    margin-right: 12rpx;
    display: flex;
    align-items: center;

    .logo-img {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    font-size: 32rpx;
    font-weight: 800;
    letter-spacing: 2rpx;
    line-height: 1;
    color: #d4af37;
    
    &.italic {
      font-style: italic;
    }
    
    .highlight {
      color: #fff;
      margin-left: 6rpx;
    }
  }
}

.nav-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
  flex-shrink: 0;

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60rpx;
    height: 60rpx;
  }

  .avatar-box {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    border: 2rpx solid rgba(249, 212, 6, 0.3);
    overflow: hidden;

    .avatar {
      width: 100%;
      height: 100%;
    }
  }
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx 20rpx;
  background-color: var(--bg-main);

  .current-month {
    font-size: 36rpx;
    font-weight: 800;
    color: var(--text-main);
    letter-spacing: 1rpx;
  }

  .view-calendar-btn {
    font-size: 26rpx;
    font-weight: 600;
    color: #ff2e63;
  }
}

.date-selector {
  background-color: var(--bg-main);
  padding-bottom: 30rpx;
  margin-bottom: 20rpx;

  .date-list {
    display: flex;
    padding: 0 32rpx;
    gap: 24rpx;
  }

  .date-item {
    flex-shrink: 0;
    width: 100rpx;
    height: 140rpx;
    border-radius: 28rpx;
    background-color: var(--card-bg);
    border: 1px solid var(--border-main);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    position: relative;

    .date-week {
      font-size: 22rpx;
      color: var(--text-secondary);
      margin-bottom: 12rpx;
      font-weight: 500;
    }

    .date-day {
      font-size: 32rpx;
      font-weight: 800;
      color: var(--text-main);
      margin-bottom: 4rpx;
    }

    .date-dot {
      width: 8rpx;
      height: 8rpx;
      background-color: #fff;
      border-radius: 50%;
      position: absolute;
      bottom: 20rpx;
    }

    &.active {
      background: linear-gradient(180deg, #ff4b5c 0%, #ff2e63 100%);
      border-color: transparent;
      box-shadow: 0 8rpx 20rpx rgba(255, 46, 99, 0.3);
      transform: translateY(-4rpx);

      .date-week {
        color: rgba(255, 255, 255, 0.9);
      }
      
      .date-day {
        font-size: 36rpx;
        color: #fff;
      }
    }
  }
}

.content-scroll {
  padding: 40rpx 32rpx 0;
}

.section {
  margin-bottom: 48rpx;

  .section-title {
    font-size: 28rpx;
    font-weight: 700;
    color: var(--text-secondary);
    margin-bottom: 24rpx;
    text-transform: uppercase;
    letter-spacing: 2rpx;
  }
}

.match-card {
  background-color: var(--card-bg);
  border-radius: 32rpx;
  border: 1px solid var(--border-main);
  overflow: hidden;
  position: relative;
  margin-bottom: 24rpx;

  .live-badge {
    position: absolute;
    top: 0;
    right: 0;
    background-color: #4a0404;
    padding: 8rpx 24rpx;
    border-bottom-left-radius: 24rpx;

    .live-text {
      font-size: 20rpx;
      font-weight: 700;
      color: #fff;
      text-transform: uppercase;
      animation: pulse 2s infinite;
    }
  }

  .card-body {
    padding: 40rpx;

    .match-info {
      margin-bottom: 32rpx;
      .competition-text {
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }

    .teams-score {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 40rpx 0;

      .team-side {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        overflow: hidden;

        .logo-container {
          width: 90rpx;
          height: 90rpx;
          background-color: #fff;
          border-radius: 50%;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-bottom: 16rpx;
          box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
          flex-shrink: 0;

          image {
            width: 64rpx;
            height: 64rpx;
          }
        }

        .team-name {
          font-size: 28rpx;
          font-weight: 800;
          color: var(--text-main);
          text-align: center;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          width: 100%;
          padding: 0 10rpx;
          box-sizing: border-box;
        }
      }

      .score-center {
        flex: 0 0 160rpx; /* 固定宽度，不参与 flex 缩放 */
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .score-text {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 12rpx; /* 缩小间距 */
          margin-bottom: 12rpx;

          .num {
            font-size: 64rpx; /* 稍微缩小字号 */
            font-weight: 900;
            color: var(--text-main);
            width: 60rpx; /* 缩小容器宽度 */
            text-align: center;
            font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
          }

          .divider {
            font-size: 32rpx; /* 缩小分隔符 */
            color: var(--text-secondary);
            font-weight: 300;
          }
        }

        .status-badge {
          background: linear-gradient(90deg, #ff2e63 0%, #ff4b5c 100%);
          color: #fff;
          font-size: 20rpx; /* 缩小字号 */
          padding: 4rpx 16rpx; /* 缩小内边距 */
          border-radius: 24rpx;
          font-weight: 700;
          box-shadow: 0 4rpx 12rpx rgba(255, 46, 99, 0.4);
          white-space: nowrap;
        }
      }
    }
  }

  .footer-right {
    display: flex;
    align-items: center;
    gap: 12rpx;

    &.mini {
      justify-content: flex-end;
    }

    .ai-label {
      font-size: 22rpx;
      font-weight: 500;
      color: var(--text-secondary);
      opacity: 0.8;
    }

    .ai-probs {
      display: flex;
      align-items: center;
      gap: 16rpx;
      background: rgba(255, 255, 255, 0.05);
      padding: 4rpx 16rpx;
      border-radius: 20rpx;
      border: 1rpx solid rgba(255, 255, 255, 0.1);
    }

    .prob-item {
      display: flex;
      align-items: center;
      gap: 4rpx;

      .prob-label {
        font-size: 20rpx;
        color: var(--text-secondary);
        font-weight: 400;
      }

      .prob-value {
        font-size: 24rpx;
        color: #f2b90d;
        font-weight: 700;
        min-width: 50rpx;
        text-align: right;
      }
    }
  }

  .card-footer {
    background-color: rgba(0, 0, 0, 0.05);
    padding: 24rpx 40rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 24rpx;
    border-top: 1rpx solid var(--border-main);

    .footer-left {
      display: flex;
      align-items: center;
      gap: 16rpx;
      .footer-text {
        color: var(--text-secondary);
      }
    }
  }
}

.mini-footer {
    padding: 0 40rpx 24rpx;
    display: flex;
    justify-content: flex-end;
  }

  .match-card-mini {
  background-color: var(--card-bg);
  border-radius: 24rpx;
  padding: 32rpx;
  border: 1px solid var(--border-main);
  margin-bottom: 24rpx;

  .mini-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .indicator {
        width: 8rpx;
        height: 32rpx;
        background-color: var(--accent-color);
        border-radius: 999rpx;
      }

      .match-meta {
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }
  }

  .mini-body {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .mini-team {
      display: flex;
      align-items: center;
      gap: 16rpx;
      flex: 1;
      overflow: hidden;

      .mini-logo {
        width: 80rpx;
        height: 80rpx;
        background-color: #fff;
        border-radius: 50%;
        padding: 6rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-shrink: 0;

        image {
          width: 60rpx;
          height: 60rpx;
        }
      }

      .mini-name {
        font-size: 26rpx;
        font-weight: 600;
        color: var(--text-main);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        flex: 1;
      }

      &.reverse {
        flex-direction: row-reverse;
        text-align: right;
      }
    }

    .vs-text {
      font-size: 24rpx;
      color: var(--text-secondary);
      font-family: monospace;
      margin: 0 20rpx;
      flex-shrink: 0;
    }
  }

  &.finished {
    opacity: 1;
    .mini-header .header-left .indicator {
      background-color: var(--border-main);
    }
    
    .finished-score {
      font-size: 32rpx;
      font-weight: 800;
      color: var(--accent-color);
      
      &.muted {
        color: var(--text-secondary);
      }
    }

    .vs-dash {
      font-size: 24rpx;
      color: var(--text-secondary);
      margin: 0 20rpx;
      flex-shrink: 0;
    }

    .team-info {
      display: flex;
      flex-direction: column;
      gap: 4rpx;
      flex: 1;
      overflow: hidden;

      &.reverse {
        align-items: flex-end;
      }
    }
  }
}

/* 底部导航占位 */
.safe-area-bottom {
  height: 160rpx;
}
</style>
