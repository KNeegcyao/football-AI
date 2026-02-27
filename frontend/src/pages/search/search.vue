<template>
  <view class="container">
    <!-- Navbar with Search -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px', paddingRight: navbarPaddingRight + 'px' }">
      <view class="nav-left" @click="goBack">
        <view class="nav-btn-glass">
          <u-icon name="arrow-left" color="#fff" size="20"></u-icon>
        </view>
      </view>
      <view class="search-box">
        <u-icon name="search" color="rgba(156, 163, 175, 0.4)" size="18"></u-icon>
        <input 
          class="search-input" 
          type="text" 
          placeholder="搜索资讯或帖子..." 
          placeholder-style="color: rgba(255, 255, 255, 0.2)"
          v-model="keyword"
          confirm-type="search"
          @confirm="handleSearch"
          :focus="autoFocus"
        />
        <view v-if="keyword" class="clear-btn" @click="clearSearch">
          <u-icon name="close" color="#9ca3af" size="16"></u-icon>
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
          <u-icon name="search" color="#374151" size="60"></u-icon>
          <text class="empty-text">未找到相关内容</text>
          <text class="empty-subtext">换个关键词试试吧</text>
        </view>

        <!-- Results List -->
        <view v-else class="results-list">
          <view 
            v-for="(item, index) in results" 
            :key="index" 
            class="result-card"
            @click="goToDetail(item)"
          >
            <!-- 赛事卡片 -->
            <view v-if="item.type === 'match'" class="match-card">
               <view class="card-header">
                  <view class="badge-row">
                    <text class="badge match-badge">赛事</text>
                    <text class="competition-name">{{ item.competitionName }}</text>
                  </view>
                  <text class="time-text">{{ formatTime(item.matchTime) }}</text>
               </view>
               <view class="match-teams">
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

            <!-- 帖子卡片 (同步圈子搜索样式) -->
            <view v-else-if="item.type === 'post'" class="post-card-style">
              <view class="post-header">
                <view class="user-info">
                  <image class="user-avatar" :src="getFullImageUrl(item.userAvatar) || '/static/default-avatar.png'" mode="aspectFill"></image>
                  <text class="user-name">{{ item.author || '社区用户' }}</text>
                </view>
                <text class="post-time">{{ formatTime(item.displayTime) }}</text>
              </view>
              
              <view class="post-content">
                <text class="highlight-title" v-if="item.title">【{{ item.title }}】</text>
                <text class="post-text">{{ item.summary || item.content }}</text>
              </view>
              
              <image v-if="item.cover" class="post-main-img" :src="getFullImageUrl(item.cover)" mode="aspectFill"></image>
              
              <view class="post-footer-stats">
                <view class="interaction-item">
                  <u-icon name="heart" size="18" color="#9ca3af"></u-icon>
                  <text>{{ item.likes || 0 }}</text>
                </view>
                <view class="interaction-item">
                  <u-icon name="chat" size="18" color="#9ca3af"></u-icon>
                  <text>{{ item.commentCount || 0 }}</text>
                </view>
                <view class="interaction-item">
                  <u-icon name="share-square" size="18" color="#9ca3af"></u-icon>
                  <text>{{ item.shares || 0 }}</text>
                </view>
              </view>
            </view>

            <!-- 资讯卡片 -->
            <view v-else class="content-card">
              <view class="card-main">
                <view class="card-header">
                  <text class="badge news-badge">资讯</text>
                  <text class="time-text">{{ formatTime(item.displayTime) }}</text>
                </view>
                <text class="title-text">{{ item.title }}</text>
                <text class="summary-text">{{ item.summary || item.content }}</text>
                <view class="card-footer">
                  <view class="stat-item" v-if="item.author">
                    <u-icon name="account" size="14" color="#6b7280"></u-icon>
                    <text>{{ item.author }}</text>
                  </view>
                  <view class="stat-item">
                    <u-icon name="eye" size="14" color="#6b7280"></u-icon>
                    <text>{{ item.viewCount || item.views || 0 }}</text>
                  </view>
                </view>
              </view>
              
              <view v-if="item.cover" class="cover-area">
                <image :src="getFullImageUrl(item.cover)" class="cover-img" mode="aspectFill" @error="item.cover = null"></image>
              </view>
            </view>
          </view>
        </view>
      </main>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { searchApi } from '@/api'
import { BASE_URL } from '@/utils/request'

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

  console.log('开始搜索, 关键词:', keyword.value)
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
    
    // 1. 处理赛事 (优先显示)
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

    // 2. 处理资讯
    const newsData = res.news || res.News
    if (newsData && newsData.records) {
      newsData.records.forEach(item => {
        // 提取封面图 (如果没封面，尝试从内容提取第一张图)
        let cover = item.coverUrl || item.cover
        if (!cover && item.content) {
          const imgMatch = item.content.match(/<img[^>]+src="([^">]+)"/)
          if (imgMatch) {
            cover = imgMatch[1]
          }
        }
        
        // 处理摘要 (去除 HTML 标签)
        let summary = item.summary
        if (!summary && item.content) {
          // 移除 HTML 标签
          const textContent = item.content.replace(/<[^>]+>/g, '')
          // 移除多余空白
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
    
    // 3. 处理帖子
    const postsData = res.posts || res.Posts
    if (postsData && postsData.records) {
      postsData.records.forEach(item => {
        // 提取封面图 (帖子首图)
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
    
    // 按时间倒序排序
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
    // 赛事没有详情页，跳转到赛程页面
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
  // Handle frontend static assets (except team logos which are on backend)
  if (url.startsWith('/static/') && !url.startsWith('/static/teams/')) {
      return url
  }
  if (url.startsWith('http')) return url
  return BASE_URL + (url.startsWith('/') ? url : '/' + url)
}
</script>

<style lang="scss" scoped>
$bg-dark: #1a1811;
$surface-dark: #24211a;
$border-dark: rgba(255, 255, 255, 0.05);
$primary: #f2b90d;
$text-gray: #9ca3af;
$text-white: #ffffff;

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: $bg-dark;
  color: $text-white;
}

/* Navbar */
.navbar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background-color: $surface-dark;
  border-bottom: 1px solid $border-dark;
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
  background-color: rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
}

.search-box {
  flex: 1;
  height: 38px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  padding: 0 16px;
  margin-right: 12px;
  transition: all 0.3s ease;

  &:focus-within {
    background-color: rgba(255, 255, 255, 0.08);
    border-color: rgba($primary, 0.5);
    box-shadow: 0 0 10px rgba($primary, 0.1);
  }
}

.search-input {
  flex: 1;
  font-size: 14px;
  color: #fff;
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
  color: $primary;
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
  border: 3px solid rgba($primary, 0.2);
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  color: $text-gray;
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
  color: #9ca3af;
  font-size: 16px;
  margin-top: 16px;
}

.empty-subtext {
  color: #4b5563;
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
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
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
  color: $primary;
  background-color: rgba($primary, 0.1);
}

/* Post Card Style (Sync with circle-search) */
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
  background-color: rgba(255, 255, 255, 0.1);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
}

.post-time {
  font-size: 12px;
  color: #6b7280;
}

.post-content {
  margin-bottom: 12px;
}

.highlight-title {
  color: $primary;
  font-weight: bold;
  font-size: 15px;
  margin-right: 4px;
}

.post-text {
  font-size: 14px;
  color: #d1d5db;
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
  color: #9ca3af;
}

.time-text {
  font-size: 12px;
  color: #6b7280;
}

/* Match Card */
.match-teams {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  background-color: rgba(0, 0, 0, 0.2);
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
  color: #fff;
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
  color: #fff;
}

.vs {
  font-size: 20px;
  font-weight: bold;
  color: $primary;
}

.status-badge {
  font-size: 10px;
  color: #9ca3af;
  margin-top: 4px;
  padding: 2px 8px;
  background-color: rgba(255, 255, 255, 0.05);
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
  color: #9ca3af;
}

.title-text {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.summary-text {
  font-size: 12px;
  color: #9ca3af;
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
  color: #6b7280;
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
</style>
