<template>
  <view class="schedule-container" :class="themeClass">
    <!-- 状态栏占位 -->
    <view class="status-bar"></view>

    <!-- 顶部导航栏 -->
    <view class="nav-bar bg-nav-bar" :style="{ paddingRight: navbarPaddingRight + 'px' }">
      <view class="logo-area">
        <view class="logo-icon">
          <image class="logo-img" src="https://ai-football-kneeg.oss-cn-beijing.aliyuncs.com/logo/logo.png" mode="aspectFit"></image>
        </view>
        <text class="logo-text italic">Socca<text class="highlight">Hub</text></text>
      </view>

      <view class="nav-actions">
        <view class="action-btn bg-theme-secondary" @click="goToSearch">
          <image :src="getFullImageUrl('/static/icons/actions/search.svg')" style="width: 44rpx; height: 44rpx; filter: invert(var(--is-dark)); opacity: 0.8;"></image>
        </view>
        <view class="avatar-box bg-theme-secondary" @click="goToProfile">
          <image class="avatar" :src="userAvatar" mode="aspectFill" @error="handleAvatarError"></image>
        </view>
      </view>
    </view>

    <!-- 日期选择器 -->
    <view class="calendar-header bg-theme-main">
      <text class="current-month text-theme-main">{{ currentYearMonth }}</text>
      <text class="view-calendar-btn">查看日历</text>
    </view>
    <scroll-view scroll-x class="date-selector bg-theme-main" show-scrollbar="false">
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

    <!-- 赛事列表内容 -->
    <view class="content-scroll">
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
              <image :src="getFullImageUrl('/static/icons/actions/play_circle.svg')" style="width: 28rpx; height: 28rpx; margin-right: 8rpx;"></image>
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
            <image :src="getFullImageUrl('/static/icons/actions/bell.svg')" style="width: 36rpx; height: 36rpx; opacity: 0.2; filter: invert(var(--is-dark));"></image>
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
        <image :src="getFullImageUrl('/static/icons/actions/info.svg')" style="width: 128rpx; height: 128rpx; opacity: 0.1; filter: invert(var(--is-dark));"></image>
        <text class="no-match-text text-theme-secondary">今日暂无比赛</text>
      </view>
    </view>

    <!-- 底部导航占位 -->
    <view class="safe-area-bottom"></view>

    <!-- 底部导航栏 -->
    <view class="tab-bar bg-tab-bar border-theme-main">
      <view v-for="(tab, index) in tabs" :key="index" 
            :class="['tab-item', tab.isCenter ? 'center-item' : '', currentTab === index ? 'active' : '']"
            @tap="handleTabClick(index)">
        <view v-if="tab.isCenter" class="center-icon bg-primary pulse-glow">
          <text class="material-symbols-outlined text-accent animate-pulse" style="font-size: 56rpx;">{{ tab.icon }}</text>
        </view>
        <template v-else>
          <text class="material-symbols-outlined" :style="{ color: currentTab === index ? '#f9d406' : 'rgba(255, 255, 255, 0.4)', fontSize: '48rpx' }">{{ tab.icon }}</text>
          <text class="tab-text" :class="currentTab === index ? 'text-[#f9d406]' : 'text-theme-secondary'">{{ tab.text }}</text>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { matchApi, userApi, fileApi } from '@/api'
import { getFullImageUrl } from '@/utils/request'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)
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
    
    if (res.code === 200) {
      aiAnalysisResult.value = res.data
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
    if (res && res.avatar) {
      userAvatar.value = getFullImageUrl(res.avatar)
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

const currentTab = ref(1)
const tabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar_month', path: 'pages/schedule/schedule' },
  { text: 'AI助手', icon: 'psychology', path: 'pages/ai/ai', isCenter: true },
  { text: '社区', icon: 'forum', path: 'pages/community/community' },
  { text: '我的', icon: 'person', path: 'pages/my/my' }
]

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
    
    const matches = await matchApi.getByDate(dateItem.fullDate)
    if (matches) {
      liveMatches.value = matches.filter(m => m.status === 1)
      upcomingMatches.value = matches.filter(m => m.status === 0)
      finishedMatches.value = matches.filter(m => m.status === 2)
    } else {
      liveMatches.value = []
      upcomingMatches.value = []
      finishedMatches.value = []
    }
  } catch (e) {
    console.error('获取赛程失败:', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
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

const handleTabClick = (index) => {
  if (index === currentTab.value) return
  uni.switchTab({
    url: '/' + tabs[index].path
  })
}

const switchTab = (index) => {
  handleTabClick(index)
}

// 页面挂载时修正当前 Tab 索引
onMounted(() => {
  // #ifdef MP-WEIXIN
  // 适配小程序胶囊按钮，防止遮挡右上角功能键
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect();
    const systemInfo = uni.getSystemInfoSync();
    // 胶囊到右边的距离 + 胶囊宽度 + 额外间距 (8px)
    navbarPaddingRight.value = (systemInfo.screenWidth - menuButton.right) + menuButton.width + 8;
  } catch (e) {
    console.error('获取胶囊按钮信息失败:', e);
    navbarPaddingRight.value = 94; // 微信小程序默认胶囊区域宽度约为 94px
  }
  // #endif

  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  if (currentPage) {
    const route = '/' + currentPage.route
    // 兼容路径前缀比较
    const index = tabs.findIndex(tab => ('/' + tab.path) === route)
    if (index !== -1) {
      currentTab.value = index
    }
  }
  
  initDates()
  fetchMatches()
  getUserProfile()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style lang="scss" scoped>
.schedule-container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  margin: 0 auto;
  overflow-x: hidden;
  box-sizing: border-box;
  background-image: radial-gradient(circle at top right, rgba(74, 4, 4, 0.4) 0%, transparent 70%);
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
}

.status-bar {
  height: var(--status-bar-height);
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
  background-color: var(--bg-card);
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
  display: flex;
  width: 100%;
  box-sizing: border-box;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 40rpx;
  background-color: var(--nav-bar-bg);
  backdrop-filter: blur(10px);
  border-bottom: 1rpx solid var(--border-main);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.logo-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  
  .logo-img {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 4rpx 8rpx rgba(0,0,0,0.6));
  }
}

.logo-text {
  font-size: 38rpx;
  font-weight: 900;
  letter-spacing: -1rpx;
  color: #d4af37;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.3);
  margin-left: -5rpx;
  
  &.italic {
    font-style: italic;
  }
  
  .highlight {
    color: #fff;
    margin-left: 6rpx;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -2rpx;
      left: 0;
      width: 100%;
      height: 2rpx;
      background: linear-gradient(90deg, transparent, #d4af37, transparent);
    }
  }

  .primary {
    color: #fff;
    margin-left: 6rpx;
  }
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.action-btn {
  width: 80rpx;
  height: 80rpx;
  background-color: var(--bg-secondary);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--bg-secondary);
  
  .avatar {
    width: 100%;
    height: 100%;
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
    padding: 0 40rpx;
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
  padding: 40rpx 40rpx 0;
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
        gap: 16rpx; /* 减小间距 */
        flex: 1;
        overflow: hidden; /* 必须溢出隐藏 */

        .mini-logo {
          width: 80rpx; /* 减小尺寸 */
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
          font-size: 26rpx; /* 减小字号 */
          font-weight: 600;
          color: var(--text-main);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          flex: 1; /* 让名字占据剩余空间 */
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
        margin: 0 20rpx; /* 增加左右边距 */
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

    /* 在完赛卡片中，mini-team 内包含 team-info 容器 */
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

/* 底部导航栏样式同步自 index.vue */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  margin: 0 auto;
  width: 100%;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
  
  height: 120rpx; 
  background-color: rgba(26, 24, 17, 0.98); 
  backdrop-filter: blur(20px); 
  border-top: 1rpx solid rgba(255, 255, 255, 0.1); 
  display: flex; 
  justify-content: space-around; 
  align-items: center; 
  padding-bottom: env(safe-area-inset-bottom); 
  z-index: 9999; 
  box-sizing: border-box; 
  pointer-events: auto;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 100%;
  transition: all 0.3s ease;
  
  &.center-item {
    position: relative;
    overflow: visible;
  }
  
  .center-icon {
    position: absolute;
    top: -40rpx;
    width: 110rpx;
    height: 110rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 8rpx solid #1a1811;
    z-index: 10001;
    background-color: #8B0000;
    
    &.pulse-glow {
      box-shadow: 0 0 20rpx rgba(139, 0, 0, 0.6);
    }
  }
  
  .tab-text {
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.4);
    font-weight: 500;
  }
  
  &.active {
    .tab-text {
      color: #f9d406;
      font-weight: 700;
    }
  }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: .7; transform: scale(0.95); }
}
</style>
