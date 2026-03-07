<template>
  <view class="container" :class="themeClass">
    <!-- Navbar with Search -->
    <view class="navbar bg-nav-bar border-b border-theme-main" :style="{ paddingTop: statusBarHeight + 'px', paddingRight: navbarPaddingRight + 'px' }">
      <view class="nav-left" @click="goBack">
        <view class="nav-btn-glass">
          <u-icon name="arrow-left" :color="themeStore.theme === 'dark' ? '#fff' : '#000'" size="20"></u-icon>
        </view>
      </view>
      <view class="search-box bg-theme-secondary border-theme-main">
        <u-icon name="search" :color="themeStore.theme === 'dark' ? 'rgba(156, 163, 175, 0.4)' : 'rgba(0, 0, 0, 0.3)'" size="18"></u-icon>
        <input 
          class="search-input" 
          type="text" 
          placeholder="搜索资讯或帖子..." 
          :placeholder-style="themeStore.theme === 'dark' ? 'color: rgba(255, 255, 255, 0.2)' : 'color: rgba(0, 0, 0, 0.3)'"
          v-model="keyword"
          confirm-type="search"
          @confirm="handleSearch"
          :focus="autoFocus"
        />
        <view v-if="keyword" class="clear-btn" @click="clearSearch">
          <u-icon name="close" :color="themeStore.theme === 'dark' ? '#9ca3af' : '#666'" size="16"></u-icon>
        </view>
      </view>
      <view class="nav-right" @click="handleSearch">
        <text class="search-btn-text">搜索</text>
      </view>
    </view>

    <!-- Content -->
    <scroll-view scroll-y class="content-scroll">
      <main class="content-area">
        <!-- Loading State -->
        <view v-if="loading" class="loading-state">
          <view class="loading-spinner"></view>
          <text class="loading-text">正在搜寻精彩内容...</text>
        </view>

        <!-- Empty State -->
        <view v-else-if="searched && results.length === 0" class="empty-state">
          <u-icon name="search" :color="themeStore.theme === 'dark' ? '#374151' : '#e2e8f0'" size="60"></u-icon>
          <text class="empty-text">未找到相关内容</text>
          <text class="empty-subtext">换个关键词试试吧</text>
        </view>

        <!-- Results List -->
        <view v-else class="results-list">
          <view 
            v-for="(item, index) in results" 
            :key="index" 
            class="result-card bg-theme-secondary border-theme-main"
            @click="goToDetail(item)"
          >
            <!-- 赛事卡片 -->
            <view v-if="item.type === 'match'" class="match-card">
               <view class="card-header">
                  <view class="badge-row">
                    <text class="badge match-badge">赛事</text>
                    <text class="competition-name text-secondary">{{ item.competitionName }}</text>
                  </view>
                  <text class="time-text text-secondary">{{ formatTime(item.matchTime) }}</text>
               </view>
               <view class="match-teams team-bg-box">
                  <view class="team">
                     <image :src="getFullImageUrl(item.homeTeam?.logoUrl)" class="team-logo" mode="aspectFit" @error="handleImageError(item.homeTeam)"></image>
                     <text class="team-name">{{ item.homeTeam?.name }}</text>
                  </view>
                  <view class="score-area">
                     <text class="score" v-if="item.status > 0">{{ item.homeScore }} - {{ item.awayScore }}</text>
                     <text class="vs" v-else>VS</text>
                     <text class="status-badge">
                      {{ item.status === 1 && item.liveTime ? item.liveTime : getStatusText(item.status) }}
                    </text>
                  </view>
                  <view class="team">
                     <image :src="getFullImageUrl(item.awayTeam?.logoUrl)" class="team-logo" mode="aspectFit" @error="handleImageError(item.awayTeam)"></image>
                     <text class="team-name">{{ item.awayTeam?.name }}</text>
                  </view>
               </view>
            </view>

            <!-- 帖子卡片 -->
            <view v-else-if="item.type === 'post'" class="post-card-style">
              <view class="post-header">
                <view class="user-info">
                  <image class="user-avatar bg-theme-secondary" :src="getFullImageUrl(item.userAvatar) || '/static/default-avatar.png'" mode="aspectFill"></image>
                  <text class="user-name">{{ item.author || '社区用户' }}</text>
                </view>
                <text class="post-time text-secondary">{{ formatTime(item.displayTime) }}</text>
              </view>
              
              <view class="post-content">
                <text class="highlight-title" v-if="item.title">【{{ item.title }}】</text>
                <text class="post-text">{{ item.summary || item.content }}</text>
              </view>
              
              <image v-if="item.cover" class="post-main-img bg-theme-secondary" :src="getFullImageUrl(item.cover)" mode="aspectFill"></image>
              
              <view class="post-footer-stats">
                <view class="interaction-item">
                  <u-icon name="heart" size="18" :color="themeStore.theme === 'dark' ? '#9ca3af' : '#64748b'"></u-icon>
                  <text>{{ item.likes || 0 }}</text>
                </view>
                <view class="interaction-item">
                  <u-icon name="chat" size="18" :color="themeStore.theme === 'dark' ? '#9ca3af' : '#64748b'"></u-icon>
                  <text>{{ item.commentCount || 0 }}</text>
                </view>
                <view class="interaction-item">
                  <u-icon name="share-square" size="18" :color="themeStore.theme === 'dark' ? '#9ca3af' : '#64748b'"></u-icon>
                  <text>{{ item.shares || 0 }}</text>
                </view>
              </view>
            </view>

            <!-- 资讯卡片 -->
            <view v-else class="content-card">
              <view class="card-main">
                <view class="card-header">
                  <text class="badge news-badge">资讯</text>
                  <text class="time-text text-secondary">{{ formatTime(item.displayTime) }}</text>
                </view>
                <text class="title-text">{{ item.title }}</text>
                <text class="summary-text text-secondary">{{ item.summary || item.content }}</text>
                <view class="card-footer">
                  <view class="stat-item" v-if="item.author">
                    <u-icon name="account" size="14" :color="themeStore.theme === 'dark' ? '#6b7280' : '#64748b'"></u-icon>
                    <text>{{ item.author }}</text>
                  </view>
                  <view class="stat-item">
                    <u-icon name="eye" size="14" :color="themeStore.theme === 'dark' ? '#6b7280' : '#64748b'"></u-icon>
                    <text>{{ item.viewCount || item.views || 0 }}</text>
                  </view>
                </view>
              </view>
              
              <view v-if="item.cover" class="cover-area">
                <image :src="getFullImageUrl(item.cover)" class="cover-img bg-theme-secondary" mode="aspectFill" @error="item.cover = null"></image>
              </view>
            </view>
          </view>
        </view>
      </main>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { searchApi } from '@/api'
import { BASE_URL } from '@/utils/request'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const autoFocus = ref(true)
const statusBarHeight = ref(20)
const navbarPaddingRight = ref(0)

onLoad(() => {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 20

  // #ifdef MP-WEIXIN
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect()
    navbarPaddingRight.value = (sysInfo.screenWidth - menuButton.right) + menuButton.width + 8
  } catch (e) {
    navbarPaddingRight.value = 94
  }
  // #endif
})

const handleSearch = async () => {
  if (!keyword.value.trim()) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }

  loading.value = true
  searched.value = true
  results.value = []
  
  try {
    const params = { 
      keyword: keyword.value,
      page: 1,
      size: 50
    }
    
    const res = await searchApi.globalSearch(params)
    if (!res) return
    
    const combinedResults = []
    
    const matchesData = res.matches || res.Matches
    if (matchesData && matchesData.records) {
      matchesData.records.forEach(item => {
        combinedResults.push({
          ...item,
          type: 'match',
          displayTime: item.matchTime
        })
      })
    }

    const newsData = res.news || res.News
    if (newsData && newsData.records) {
      newsData.records.forEach(item => {
        let cover = item.coverUrl || item.cover
        if (!cover && item.content) {
          const imgMatch = item.content.match(/<img[^>]+src="([^">]+)"/)
          if (imgMatch) {
            cover = imgMatch[1]
          }
        }
        
        let summary = item.summary
        if (!summary && item.content) {
          const textContent = item.content.replace(/<[^>]+>/g, '')
          summary = textContent.replace(/\s+/g, ' ').trim().substring(0, 60) + '...'
        }

        combinedResults.push({
          ...item,
          type: 'news',
          displayTime: item.publishTime,
          cover: cover,
          summary: summary
        })
      })
    }
    
    const postsData = res.posts || res.Posts
    if (postsData && postsData.records) {
      postsData.records.forEach(item => {
        let postCover = null
        if (item.images && Array.isArray(item.images) && item.images.length > 0) {
          postCover = item.images[0]
        } else if (item.images && typeof item.images === 'string') {
          try {
            const parsedImages = JSON.parse(item.images)
            if (Array.isArray(parsedImages) && parsedImages.length > 0) {
              postCover = parsedImages[0]
            }
          } catch (e) {
            postCover = item.images
          }
        }

        combinedResults.push({
          ...item,
          type: 'post',
          displayTime: item.createdAt,
          author: item.userName || '社区用户',
          userAvatar: item.userAvatar,
          cover: postCover,
          shares: 0
        })
      })
    }
    
    combinedResults.sort((a, b) => {
      const timeA = new Date(a.displayTime || 0)
      const timeB = new Date(b.displayTime || 0)
      return timeB - timeA
    })
    
    results.value = combinedResults
  } catch (e) {
    console.error('搜索失败:', e)
    uni.showToast({ title: '搜索失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const clearSearch = () => {
  keyword.value = ''
  results.value = []
  searched.value = false
}

const goBack = () => {
  uni.navigateBack()
}

const getStatusText = (status) => {
  const statusMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束',
    3: '推迟',
    4: '取消'
  }
  return statusMap[status] || '未开始'
}

const handleImageError = (team) => {
  if (team) {
    team.logoUrl = '/static/soccer-logo.png'
  }
}

const goToDetail = (item) => {
  if (item.type === 'match') {
    uni.switchTab({
      url: '/pages/schedule/schedule'
    })
    return
  }
  
  const url = item.type === 'news' 
    ? `/pages/news/detail?id=${item.id}`
    : `/pages/post/detail?id=${item.id}`
    
  uni.navigateTo({
    url
  })
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 24 && hours > 0) return `${hours}小时前`
  if (hours <= 0) return '刚刚'
  return timeStr.split(' ')[0]
}

const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('/static/') && !url.startsWith('/static/teams/')) {
      return url
  }
  if (url.startsWith('http')) return url
  return BASE_URL + (url.startsWith('/') ? url : '/' + url)
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
  transition: all 0.3s;
}

/* Navbar */
.navbar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  margin-right: 12px;
}

.nav-btn-glass {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: var(--bg-secondary);
  display: flex;
  justify-content: center;
  align-items: center;
}

.search-box {
  flex: 1;
  height: 38px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  margin-right: 12px;
  transition: all 0.3s ease;
  background-color: var(--bg-secondary);
  border: 1rpx solid var(--border-main);

  &:focus-within {
    border-color: rgba($pitch-pulse-primary, 0.5);
    box-shadow: 0 0 10px rgba($pitch-pulse-primary, 0.1);
  }
}

.search-input {
  flex: 1;
  font-size: 14px;
  color: var(--text-main);
  margin-left: 8px;
  background: transparent;
  border: none;
  outline: none;
}

.clear-btn {
  padding: 4px;
}

.nav-right {
  padding: 4px 8px;
}

.search-btn-text {
  color: $pitch-pulse-primary;
  font-size: 14px;
  font-weight: bold;
}

/* Content */
.content-scroll {
  flex: 1;
  overflow: hidden;
}

.content-area {
  padding: 16px;
}

/* Loading & Empty States */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba($pitch-pulse-primary, 0.2);
  border-top-color: $pitch-pulse-primary;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  color: var(--text-secondary);
  font-size: 14px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
}

.empty-text {
  color: var(--text-secondary);
  font-size: 16px;
  margin-top: 16px;
}

.empty-subtext {
  color: var(--text-secondary);
  opacity: 0.6;
  font-size: 12px;
  margin-top: 8px;
}

/* Results */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-card {
  border-radius: 12px;
  padding: 16px;
}

.badge {
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 8px;
}

.match-badge {
  color: #34d399;
  background-color: rgba(52, 211, 153, 0.1);
}

.news-badge {
  color: $pitch-pulse-primary;
  background-color: rgba($pitch-pulse-primary, 0.1);
}

/* Post Card Style */
.post-card-style {
  display: flex;
  flex-direction: column;
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-main);
}

.post-time {
  font-size: 12px;
}

.post-content {
  margin-bottom: 12px;
}

.highlight-title {
  color: $pitch-pulse-primary;
  font-weight: bold;
  font-size: 15px;
  margin-right: 4px;
}

.post-text {
  font-size: 14px;
  color: var(--text-main);
  opacity: 0.9;
  line-height: 1.6;
}

.post-main-img {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.post-footer-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.interaction-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.time-text {
  font-size: 12px;
}

/* Match Card */
.match-teams {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
}

.team {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 30%;
}

.team-logo {
  width: 40px;
  height: 40px;
}

.team-name {
  font-size: 12px;
  font-weight: bold;
  color: var(--text-main);
  text-align: center;
}

.score-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40%;
}

.score {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-main);
}

.vs {
  font-size: 20px;
  font-weight: bold;
  color: $pitch-pulse-primary;
}

.status-badge {
  font-size: 10px;
  color: var(--text-secondary);
  margin-top: 4px;
  padding: 2px 8px;
  background-color: var(--bg-secondary);
  border-radius: 10px;
}

/* Content Card */
.content-card {
  display: flex;
  gap: 16px;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.badge-row {
  display: flex;
  align-items: center;
}

.competition-name {
  font-size: 12px;
}

.title-text {
  font-size: 16px;
  font-weight: bold;
  color: var(--text-main);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.summary-text {
  font-size: 12px;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.cover-area {
  width: 96px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.theme-light {
  .team-bg-box {
    background-color: #f8fafc;
  }
}

.theme-dark {
  .team-bg-box {
    background-color: rgba(0, 0, 0, 0.2);
  }
}
</style>