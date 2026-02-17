<template>
  <view class="schedule-container">
    <!-- 状态栏占位 -->
    <view class="status-bar"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
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
      <text class="view-calendar-btn">查看日历</text>
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
              <view class="team">
                <view class="team-logo">
                  <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
                </view>
                <text class="team-name">{{ match.homeTeam?.name }}</text>
              </view>
              <view class="score-box">
                <text class="score-text">{{ match.homeScore }} - {{ match.awayScore }}</text>
                <view class="status-tag">
                  <text>下半场</text>
                </view>
              </view>
              <view class="team">
                <view class="team-logo">
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
              <text class="match-meta">{{ match.competitionName }} · {{ formatMatchDate(match.matchTime) }}</text>
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
        <u-icon :name="tab.icon" size="24" :color="currentTab === index ? '#f9d406' : '#7A7E83'"></u-icon>
        <text class="tab-text">{{ tab.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { matchApi, userApi, fileApi } from '@/api/index'

const dates = ref([])
const activeDateIndex = ref(0)
const currentYearMonth = ref('')
const liveMatches = ref([])
const upcomingMatches = ref([])
const finishedMatches = ref([])
const userAvatar = ref('/static/soccer-logo.png')
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
      url.includes('wikimedia.org')) {
    return url
  }
  // 本地静态资源
  if (url.startsWith('/static/')) return url
  // 后端资源
  return 'http://localhost:8080' + url
}

const handleImageError = (team) => {
  if (team) {
    team.logoUrl = '/static/soccer-logo.png'
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
  
  // 如果选中今天，重启自动刷新
  if (dates.value[index].isToday) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
}

// 自动刷新逻辑
const startAutoRefresh = () => {
  stopAutoRefresh()
  // 只有选中“今天”时才刷新
  if (dates.value[activeDateIndex.value]?.isToday) {
    refreshTimer = setInterval(fetchMatches, 30000)
  }
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const handleTabClick = (index) => {
  try {
    const tab = tabs[index]
    if (!tab || !tab.path) return

    currentTab.value = index
    
    // 统一路径格式：pages/schedule/schedule
    const path = tab.path.startsWith('/') ? tab.path.substring(1) : tab.path

    // #ifdef H5
    // 检查是否已经是当前路径，避免重复刷新
    const currentHash = window.location.hash.replace('#/', '')
    if (currentHash === path) {
      console.log('已经在当前页面，无需跳转')
      return
    }

    console.log('执行强制重载跳转:', path)
    
    // 彻底放弃框架路由，改用物理重载
    const newUrl = window.location.origin + window.location.pathname + '#/' + path
    window.location.href = newUrl
    
    // 增加延迟，确保 hash 变更已被浏览器记录，减少 ERR_ABORTED 风险
    setTimeout(() => {
      window.location.reload()
    }, 100)
    // #endif

    // #ifndef H5
    uni.reLaunch({
      url: '/' + path
    })
    // #endif
  } catch (e) {
    console.error('跳转异常:', e)
  }
}

const switchTab = (index) => {
  handleTabClick(index)
}

// 页面挂载时修正当前 Tab 索引
onMounted(() => {
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
  padding-bottom: 120rpx;
  background-image: radial-gradient(circle at top right, rgba(74, 4, 4, 0.4) 0%, transparent 70%);
  display: flex;
  flex-direction: column;
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
  background-color: rgba(26, 24, 17, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  
  /* #ifdef H5 */
  max-width: 440px;
  left: 50%;
  transform: translateX(-50%);
  /* #endif */
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.logo-icon {
  width: 60rpx;
  height: 60rpx;
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
    color: #f9d406;
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
  background-color: rgba(255, 255, 255, 0.05);
  
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
  background-color: #1a1811;

  .current-month {
    font-size: 36rpx;
    font-weight: 800;
    color: #fff;
    letter-spacing: 1rpx;
  }

  .view-calendar-btn {
    font-size: 26rpx;
    font-weight: 600;
    color: #ff2e63;
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

      .team {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 16rpx;

        .team-logo {
          width: 200rpx;
          height: 200rpx;
          background-color: #fff;
          border-radius: 50%;
          padding: 8rpx;
          display: flex;
          justify-content: center;
          align-items: center;

          image {
            width: 184rpx;
            height: 184rpx;
          }
        }

        .team-name {
        font-size: 28rpx;
        font-weight: 700;
        text-align: center;
        color: #fff;
      }
      }

      .score-box {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8rpx;
        margin: 0 32rpx;

        .score-text {
          font-size: 60rpx;
          font-weight: 800;
          letter-spacing: 8rpx;
        }

        .status-tag {
          font-size: 20rpx;
          color: #f9d406;
          background-color: rgba(249, 212, 6, 0.1);
          padding: 4rpx 16rpx;
          border-radius: 999rpx;
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
      gap: 24rpx;
      flex: 1;

      .mini-logo {
        width: 120rpx;
        height: 120rpx;
        background-color: #fff;
        border-radius: 50%;
        padding: 6rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-shrink: 0;

        image {
          width: 108rpx;
          height: 108rpx;
        }
      }

      .mini-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #fff;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      &.reverse {
        flex-direction: row-reverse;
        text-align: right;
      }
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

    .vs-text {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.4);
      font-family: monospace;
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
