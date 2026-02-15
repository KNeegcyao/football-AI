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
        <view class="action-btn">
          <u-icon name="search" color="#fff" size="44rpx"></u-icon>
        </view>
        <view class="avatar-box">
          <image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAIQzgWcNfhgZFvam6mBjP3SQ_yGWwerChZs_eM8_z1o9qWN1QnojbmioWRE4k-7JCMz3wyTItaNxPRS96uepbCycNWyeFd5CG59XlkU87Xapse9FwdEkK2Zx1w8ht2xUwawr5W2_YCj4et1disriTjbEFli9UPYJNPga5CS2cxoieZu8XSCzo0RPa4yG7saKzV_HIw3mDGt75bzRZba9L3kB0HwXM3AyVsiTFG6TPJ51agHPrnr6rJ6i2lggc-umhels7SEKP-34Nv" mode="aspectFill"></image>
        </view>
      </view>
    </view>

    <!-- 日期选择器 -->
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
          <text class="date-day">{{ item.day }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 赛事列表内容 -->
    <view class="content-scroll">
      <!-- 正在直播 -->
      <view class="section" v-if="liveMatches.length > 0">
        <view class="match-card live-card" v-for="match in liveMatches" :key="match.id">
          <view class="live-badge">
            <text class="live-text">LIVE {{ match.matchMinute || "67'" }}</text>
          </view>
          <view class="card-body">
            <view class="match-info">
              <text class="competition-text">{{ match.competitionName }} · {{ match.round }}</text>
            </view>
            <view class="teams-score">
              <view class="team">
                <view class="team-logo">
                  <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit"></image>
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
                  <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit"></image>
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
                <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit"></image>
              </view>
              <text class="mini-name">{{ match.homeTeam?.name }}</text>
            </view>
            <text class="vs-text">VS</text>
            <view class="mini-team reverse">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit"></image>
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
                <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit"></image>
              </view>
              <view class="team-info">
                <text class="mini-name">{{ match.homeTeam?.name }}</text>
                <text class="finished-score">{{ match.homeScore }}</text>
              </view>
            </view>
            <text class="vs-dash">-</text>
            <view class="mini-team reverse">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit"></image>
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
import { ref, onMounted } from 'vue'
import { matchApi } from '@/api/index'

const dates = ref([])
const activeDateIndex = ref(0)
const liveMatches = ref([])
const upcomingMatches = ref([])
const finishedMatches = ref([])

const currentTab = ref(1)
const tabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar', path: 'pages/schedule/schedule' },
  { text: '社区', icon: 'chat', path: 'pages/index/index' },
  { text: '我的', icon: 'account', path: 'pages/index/index' }
]

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
})



// 初始化日期列表
const initDates = () => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const today = new Date()
  for (let i = -1; i < 6; i++) {
    const date = new Date()
    date.setDate(today.getDate() + i)
    dates.value.push({
      fullDate: date.toISOString().split('T')[0],
      day: date.getDate(),
      week: i === 0 ? '今天' : (i === -1 ? '昨天' : weekDays[date.getDay()])
    })
  }
  // 默认选中“今天”
  activeDateIndex.value = 1
}

const selectDate = (index) => {
  activeDateIndex.value = index
  fetchMatches()
}

const fetchMatches = async () => {
  const selectedDate = dates.value[activeDateIndex.value].fullDate
  try {
    const res = await matchApi.getByDate(selectedDate)
    // request.js 已经处理了 code === 200 并返回了 data
    if (res && Array.isArray(res)) {
      const allMatches = res
      liveMatches.value = allMatches.filter(m => m.status === 1)
      upcomingMatches.value = allMatches.filter(m => m.status === 0)
      finishedMatches.value = allMatches.filter(m => m.status === 2)
    } else {
      liveMatches.value = []
      upcomingMatches.value = []
      finishedMatches.value = []
    }
  } catch (error) {
    console.error('获取赛事失败:', error)
    liveMatches.value = []
    upcomingMatches.value = []
    finishedMatches.value = []
  }
}

const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const formatMatchTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    return timeStr.split('T')[1].substring(0, 5)
  } catch (e) {
    return timeStr
  }
}

const formatMatchDate = (timeStr) => {
  if (!timeStr) return ''
  try {
    const date = timeStr.split('T')[0]
    const today = new Date().toISOString().split('T')[0]
    if (date === today) return '今天'
    
    const yesterday = new Date()
    yesterday.setDate(yesterday.getDate() - 1)
    if (date === yesterday.toISOString().split('T')[0]) return '昨天'
    
    return date.substring(5) // MM-DD
  } catch (e) {
    return timeStr
  }
}

onMounted(() => {
  initDates()
  fetchMatches()
})
</script>

<style lang="scss">
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

.date-selector {
  background-color: #1a1811;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding: 20rpx 0;

  .date-list {
    display: flex;
    padding: 0 40rpx;
    gap: 24rpx;
  }

  .date-item {
    flex-shrink: 0;
    width: 96rpx;
    height: 112rpx;
    border-radius: 24rpx;
    background-color: rgba(255, 255, 255, 0.05);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    transition: all 0.3s;

    .date-week {
      font-size: 20rpx;
      color: rgba(255, 255, 255, 0.6);
      margin-bottom: 4rpx;
    }

    .date-day {
      font-size: 36rpx;
      font-weight: 800;
      color: rgba(255, 255, 255, 0.6);
    }

    &.active {
      background-color: #f9d406;
      .date-week, .date-day {
        color: #000;
        font-weight: 800;
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
          width: 112rpx;
          height: 112rpx;
          background-color: #fff;
          border-radius: 50%;
          padding: 16rpx;
          display: flex;
          justify-content: center;
          align-items: center;

          image {
            width: 80rpx;
            height: 80rpx;
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
        width: 64rpx;
        height: 64rpx;
        background-color: #fff;
        border-radius: 50%;
        padding: 8rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-shrink: 0;

        image {
          width: 48rpx;
          height: 48rpx;
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
