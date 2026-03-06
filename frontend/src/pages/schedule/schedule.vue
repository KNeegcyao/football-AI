<template>
  <view class="schedule-container">
    <!-- 状态栏占位 -->
    <view class="status-bar"></view>

    <!-- 顶部导航栏 -->
    <view class="nav-bar" :style="{ paddingRight: navbarPaddingRight + 'px' }">
      <view class="logo-area">
        <view class="logo-icon">
          <image class="logo-img" src="/static/soccer-logo.png" mode="aspectFit"></image>
        </view>
        <text class="logo-text">PITCH<text class="primary">PULSE</text></text>
      </view>

      <view class="nav-actions">
        <view class="action-btn" @click="goToSearch">
          <u-icon name="search" color="#fff" size="44rpx"></u-icon>
        </view>
        <view class="avatar-box" @click="goToProfile">
          <image class="avatar" :src="userAvatar" mode="aspectFill" @error="handleAvatarError"></image>
        </view>
      </view>
    </view>

    <!-- 日期选择器 -->
    <view class="calendar-header">
      <text class="current-month">{{ currentYearMonth }}</text>
      <view class="ai-predict-entry" @click="openAIPredict">
        <u-icon name="star-fill" color="#f9d406" size="28rpx"></u-icon>
        <text class="ai-predict-text">AI 赛前预测</text>
      </view>
    </view>
    <scroll-view scroll-x class="date-selector" show-scrollbar="false">
      <view class="date-list">
        <view 
          v-for="(item, index) in dates" 
          :key="index"
          class="date-item"
          :class="{ active: activeDateIndex === index }"
          @click="selectDate(index)"
        >
          <text class="date-week">{{ item.week }}</text>
          <text class="date-day">{{ item.day }}日</text>
          <view class="date-dot" v-if="activeDateIndex === index"></view>
        </view>
      </view>
    </scroll-view>

    <!-- 赛事列表内容 -->
    <view class="content-scroll">
      <!-- 正在直播 -->
      <view class="section" v-if="liveMatches.length > 0">
        <view class="match-card live-card" v-for="match in liveMatches" :key="match.id">
          <view class="live-badge">
            <text class="live-text">LIVE {{ match.liveTime || match.matchMinute || "" }}</text>
          </view>
          <view class="card-body">
            <view class="match-info">
              <text class="competition-text">{{ match.competitionName }} · {{ match.round }}</text>
            </view>
            <view class="teams-score">
              <view class="team-side">
                <view class="logo-container">
                  <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
                </view>
                <text class="team-name">{{ match.homeTeam?.name }}</text>
              </view>
              <view class="score-center">
                <view class="score-text">
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
                <text class="team-name">{{ match.awayTeam?.name }}</text>
              </view>
            </view>
          </view>
          <view class="card-footer">
            <view class="footer-left">
              <u-icon name="play-circle" color="#f9d406" size="14"></u-icon>
              <text class="footer-text">视频直播中</text>
            </view>
            <view class="footer-right">
              <text class="ai-label">AI 预测:</text>
              <text class="ai-value">主队胜率 65%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 即将开始 -->
      <view class="section" v-if="upcomingMatches.length > 0">
        <view class="section-title">即将开始</view>
        <view class="match-card-mini" v-for="match in upcomingMatches" :key="match.id">
          <view class="mini-header">
            <view class="header-left">
              <view class="indicator"></view>
              <text class="match-meta">{{ match.competitionName }} · {{ formatMatchTime(match.matchTime) }}</text>
            </view>
            <u-icon name="bell" color="rgba(255,255,255,0.2)" size="18"></u-icon>
          </view>
          <view class="mini-body">
            <view class="mini-team">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
              </view>
              <text class="mini-name">{{ match.homeTeam?.name }}</text>
            </view>
            <text class="vs-text">VS</text>
            <view class="mini-team reverse">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
              </view>
              <text class="mini-name">{{ match.awayTeam?.name }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 已完赛 -->
      <view class="section" v-if="finishedMatches.length > 0">
        <view class="section-title">已完赛</view>
        <view class="match-card-mini finished" v-for="match in finishedMatches" :key="match.id">
          <view class="mini-header">
            <view class="header-left">
              <view class="indicator"></view>
              <text class="match-meta">{{ match.competitionName }} · {{ formatMatchTime(match.matchTime) }}</text>
            </view>
          </view>
          <view class="mini-body">
            <view class="mini-team">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
              </view>
              <view class="team-info">
                <text class="mini-name">{{ match.homeTeam?.name }}</text>
                <text class="finished-score">{{ match.homeScore }}</text>
              </view>
            </view>
            <text class="vs-dash">-</text>
            <view class="mini-team reverse">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
              </view>
              <view class="team-info reverse">
                <text class="mini-name">{{ match.awayTeam?.name }}</text>
                <text class="finished-score" :class="{ 'muted': match.awayScore < match.homeScore }">{{ match.awayScore }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 无比赛提示 -->
      <view class="no-match" v-if="liveMatches.length === 0 && upcomingMatches.length === 0 && finishedMatches.length === 0">
        <u-icon name="info-circle" color="rgba(255,255,255,0.3)" size="64"></u-icon>
        <text class="no-match-text">今日暂无比赛</text>
      </view>
    </view>

    <!-- 底部导航占位 -->
    <view class="safe-area-bottom"></view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @tap="handleTabClick(index)"
      >
        <u-icon :name="tab.icon" size="24" :color="currentTab === index ? '#f9d406' : 'rgba(255, 255, 255, 0.4)'"></u-icon>
        <text class="tab-text">{{ tab.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { matchApi, userApi, fileApi } from '@/api/index'
import { BASE_URL } from '@/utils/request'

const liveMatches = ref([])
const upcomingMatches = ref([])
const finishedMatches = ref([])
  const userAvatar = ref('/static/soccer-logo.png')
  const navbarPaddingRight = ref(16) // 默认 16px
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
      userAvatar.value = fileApi.getFileUrl(res.avatar)
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
  { text: '赛程', icon: 'calendar', path: 'pages/schedule/schedule' },
  { text: '社区', icon: 'chat', path: 'pages/community/community' },
  { text: '我的', icon: 'account', path: 'pages/my/my' }
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

const getFullImageUrl = (url) => {
  if (!url) return '/static/soccer-logo.png'
  
  // 外部链接处理 (包含 http, //, 或特定域名)
  if (url.startsWith('http') || url.startsWith('//') || 
      url.includes('pstatp.com') || url.includes('zhibo8.cc') ||
      url.includes('wikimedia.org') || url.includes('premierleague.com') ||
      url.includes('football-data.org')) {
    return url
  }
  // 本地静态资源 (排除球队logo)
  if (url.startsWith('/static/') && !url.startsWith('/static/teams/')) return url
  // 后端资源
  return BASE_URL + (url.startsWith('/') ? url : '/' + url)
}

const handleImageError = (team) => {
  if (team) {
    team.logoUrl = '/static/soccer-logo.png'
  }
}

const openAIPredict = () => {
  uni.showLoading({
    title: 'AI 正在分析中...',
    mask: true
  })
  
  // 模拟 AI 分析过程
  setTimeout(() => {
    uni.hideLoading()
    uni.showModal({
      title: 'AI 赛前深度分析',
      content: '基于大数据模型分析，今日焦点赛事主队胜率较高。系统正在集成更详细的 Agent 实时预测功能，敬请期待！',
      showCancel: false,
      confirmText: '了解',
      confirmColor: '#f9d406'
    })
  }, 1500)
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
  const tab = tabs[index]
  if (!tab || !tab.path) return
  
  if (currentTab.value === index) return

  const url = tab.path.startsWith('/') ? tab.path : '/' + tab.path
  
  uni.switchTab({
    url: url,
    fail: () => {
      uni.reLaunch({ url })
    }
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
  background-color: #1a1811;
  color: #fff;
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  margin: 0 auto;
  overflow-x: hidden;
  box-sizing: border-box;
  background-image: radial-gradient(circle at top right, rgba(74, 4, 4, 0.4) 0%, transparent 70%);
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
    color: rgba(255, 255, 255, 0.4);
  }
}

.nav-bar {
  display: flex;
  width: 100%;
  box-sizing: border-box;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 40rpx;
  background-color: rgba($pitch-pulse-bg-dark, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
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
  width: 60rpx;
  height: 60rpx;
  background-color: transparent;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10rpx;
  
  .logo-img {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 2rpx 4rpx rgba(0,0,0,0.3));
  }
}

.logo-text {
  font-size: 36rpx;
  font-weight: 800;
  letter-spacing: -1rpx;
  .primary {
    color: $pitch-pulse-primary;
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
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 20rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.05);
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx 20rpx;
  background-color: #1a1811;

  .current-month {
    font-size: 36rpx;
    font-weight: 800;
    color: #fff;
    letter-spacing: 1rpx;
  }

  .ai-predict-entry {
    display: flex;
    align-items: center;
    gap: 8rpx;
    background: rgba($pitch-pulse-primary, 0.1);
    padding: 10rpx 20rpx;
    border-radius: 30rpx;
    border: 1rpx solid rgba($pitch-pulse-primary, 0.2);
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.95);
      background: rgba($pitch-pulse-primary, 0.2);
    }

    .ai-predict-text {
      font-size: 24rpx;
      font-weight: 700;
      color: $pitch-pulse-primary;
    }
  }
}

.date-selector {
  background-color: #1a1811;
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
    background-color: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.05);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    position: relative;

    .date-week {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.4);
      margin-bottom: 12rpx;
      font-weight: 500;
    }

    .date-day {
      font-size: 32rpx;
      font-weight: 800;
      color: #fff;
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
    color: rgba(255, 255, 255, 0.5);
    margin-bottom: 24rpx;
    text-transform: uppercase;
    letter-spacing: 2rpx;
  }
}

.match-card {
  background-color: #2a2820;
  border-radius: 32rpx;
  border: 1px solid rgba(249, 212, 6, 0.2);
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
        color: rgba(255, 255, 255, 0.4);
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
          color: #fff;
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
            color: #fff;
            width: 60rpx; /* 缩小容器宽度 */
            text-align: center;
            font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
          }

          .divider {
            font-size: 32rpx; /* 缩小分隔符 */
            color: rgba(255, 255, 255, 0.4);
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

  .card-footer {
    background-color: rgba(0, 0, 0, 0.2);
    padding: 24rpx 40rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 24rpx;

    .footer-left {
      display: flex;
      align-items: center;
      gap: 16rpx;
      .footer-text {
        color: rgba(255, 255, 255, 0.6);
      }
    }

    .footer-right {
      display: flex;
      align-items: center;
      gap: 8rpx;
      .ai-label {
        color: #f9d406;
        font-weight: 700;
      }
      .ai-value {
        color: rgba(255, 255, 255, 0.6);
      }
    }
  }
}

.match-card-mini {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 24rpx;
  padding: 32rpx;
  border: 1px solid rgba(255, 255, 255, 0.05);
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
        background-color: #f9d406;
        border-radius: 999rpx;
      }

      .match-meta {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.6);
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
          color: #fff;
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
        color: rgba(255, 255, 255, 0.4);
        font-family: monospace;
        margin: 0 20rpx; /* 增加左右边距 */
        flex-shrink: 0;
      }
  }

  &.finished {
    opacity: 1;
    .mini-header .header-left .indicator {
      background-color: rgba(255, 255, 255, 0.2);
    }
    
    .finished-score {
      font-size: 32rpx;
      font-weight: 800;
      color: #f9d406;
      
      &.muted {
        color: rgba(255, 255, 255, 0.4);
      }
    }

    .vs-dash {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.2);
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

/* 底部导航栏 */
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
  padding-bottom: constant(safe-area-inset-bottom);
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

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
</style>
