<template>
  <view class="search-page bg-[#1a1811] min-h-screen font-display text-gray-200">
    <!-- Header -->
    <header class="fixed top-0 left-0 right-0 z-50 bg-[#1a1811]/80 backdrop-blur-md border-b border-white/5 h5-header-fix">
      <view class="flex items-center gap-3 px-4 h-16 max-w-2xl mx-auto">
        <button @click="goBack" class="flex items-center justify-center w-10 h-10 -ml-2 text-primary hover:bg-primary/10 rounded-full transition-colors bg-transparent border-none">
          <text class="material-icons text-2xl">chevron_left</text>
        </button>
        
        <!-- 搜索框容器 -->
        <view
          class="flex-1 relative flex items-center h-10 bg-white/5 border rounded-full px-3 transition-all duration-300"
          :class="[isFocus ? 'border-white/20 bg-white/10' : 'border-white/10']"
        >
          <text class="material-icons text-gray-500 text-lg mr-2">search</text>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索资讯或帖子..."
            class="flex-1 h-full bg-transparent text-sm focus:outline-none border-none"
            style="outline: none; border-color: transparent;"
            @confirm="handleSearch"
            @focus="isFocus = true"
            @blur="isFocus = false"
            :focus="autoFocus"
          />
          <text 
            v-if="keyword" 
            @click="clearSearch" 
            class="material-icons text-gray-500 text-lg cursor-pointer hover:text-white ml-2"
          >close</text>
        </view>
        
        <button @click="handleSearch" class="text-primary font-bold text-sm bg-transparent border-none px-2 active:opacity-70 transition-opacity">搜索</button>
      </view>
    </header>

    <!-- Content -->
    <main class="pt-20 pb-10 px-4 w-full mx-auto">
      <!-- Loading State -->
      <view v-if="loading" class="flex flex-col items-center justify-center py-20">
        <view class="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin mb-4"></view>
        <text class="text-gray-500 text-sm">正在搜寻精彩内容...</text>
      </view>

      <!-- Empty State -->
      <view v-else-if="searched && results.length === 0" class="flex flex-col items-center justify-center py-20">
        <text class="material-icons text-6xl text-gray-700 mb-4">search_off</text>
        <text class="text-gray-400">未找到相关内容</text>
        <text class="text-gray-600 text-xs mt-2">换个关键词试试吧</text>
      </view>

      <!-- Results List -->
      <view v-else class="space-y-4">
        <view 
        v-for="(item, index) in results" 
        :key="index" 
        class="bg-white/5 rounded-xl p-4 border border-white/5 hover:bg-white/10 transition-colors cursor-pointer"
        @click="goToDetail(item)"
      >
        <!-- 赛事卡片 -->
        <view v-if="item.type === 'match'" class="flex flex-col gap-2">
           <view class="flex items-center justify-between mb-2">
              <view class="flex items-center gap-2">
                <text class="text-[10px] font-bold uppercase tracking-wider text-[#34d399] px-1.5 py-0.5 bg-[#34d399]/10 rounded">
                  赛事
                </text>
                <text class="text-xs text-gray-400">{{ item.competitionName }}</text>
              </view>
              <text class="text-xs text-gray-500">{{ formatTime(item.matchTime) }}</text>
           </view>
           <view class="flex items-center justify-between px-2 py-2 bg-black/20 rounded-lg">
              <view class="flex flex-col items-center gap-1 w-1/3">
                 <image :src="getFullImageUrl(item.homeTeam?.logoUrl)" class="w-10 h-10 rounded-full bg-white/5 p-1" mode="aspectFit" @error="handleImageError(item.homeTeam)"></image>
                 <text class="text-xs font-bold text-white text-center line-clamp-1">{{ item.homeTeam?.name }}</text>
              </view>
              <view class="flex flex-col items-center justify-center w-1/3">
                 <text class="text-2xl font-bold text-white leading-none mb-1" v-if="item.status > 0">{{ item.homeScore }} - {{ item.awayScore }}</text>
                 <text class="text-xl font-bold text-[#f9d406]" v-else>VS</text>
                 <text class="text-[10px] text-gray-400 mt-1 px-2 py-0.5 bg-white/5 rounded-full">
                  {{ item.status === 1 && item.liveTime ? item.liveTime : getStatusText(item.status) }}
                </text>
              </view>
              <view class="flex flex-col items-center gap-1 w-1/3">
                 <image :src="getFullImageUrl(item.awayTeam?.logoUrl)" class="w-10 h-10 rounded-full bg-white/5 p-1" mode="aspectFit" @error="handleImageError(item.awayTeam)"></image>
                 <text class="text-xs font-bold text-white text-center line-clamp-1">{{ item.awayTeam?.name }}</text>
              </view>
           </view>
        </view>

        <!-- 资讯/帖子卡片 -->
        <view v-else class="flex gap-4">
          <view class="flex-1 min-w-0">
            <view class="flex items-center gap-2 mb-2">
              <text class="text-[10px] font-bold uppercase tracking-wider text-primary px-1.5 py-0.5 bg-primary/10 rounded">
                {{ item.type === 'news' ? '资讯' : '社区' }}
              </text>
              <text class="text-xs text-gray-500">{{ formatTime(item.displayTime) }}</text>
            </view>
            <text class="text-base font-bold text-white leading-snug line-clamp-2 mb-2">
              {{ item.title }}
            </text>
            <text class="text-xs text-gray-400 line-clamp-2 mb-3 leading-relaxed">
              {{ item.summary || item.content }}
            </text>
            <view class="flex items-center gap-4 text-xs text-gray-500">
              <text v-if="item.author" class="flex items-center gap-1">
                <u-icon name="account" size="14"></u-icon>
                {{ item.author }}
              </text>
              <text class="flex items-center gap-1">
                <u-icon name="eye" size="14"></u-icon>
                {{ item.views || 0 }}
              </text>
              <text v-if="item.comments !== undefined" class="flex items-center gap-1">
                <u-icon name="chat" size="14"></u-icon>
                {{ item.comments || 0 }}
              </text>
            </view>
          </view>
          
          <view 
            v-if="item.cover" 
            class="w-24 h-24 rounded-lg overflow-hidden bg-white/5 flex-shrink-0"
          >
            <image 
              :src="getFullImageUrl(item.cover)" 
              class="w-full h-full object-cover"
              mode="aspectFill"
              @error="item.cover = null"
            ></image>
          </view>
        </view>
      </view>
      </view>
    </main>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { searchApi } from '@/api'

const keyword = ref('')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const isFocus = ref(false)
const autoFocus = ref(true)

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
        let cover = item.cover
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
        combinedResults.push({
          ...item,
          type: 'post',
          displayTime: item.createdAt,
          author: item.username || '社区用户'
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
    : `/pages/community/detail?id=${item.id}`
    
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
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}
</script>

<style>
/* #ifdef H5 */
.h5-header-fix {
  max-width: 500px;
  margin: 0 auto;
}
/* #endif */
</style>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* 隐藏 H5 默认 header */
/* #ifdef H5 */
uni-page-head {
  display: none;
}
/* #endif */

input {
  /* 确保在所有平台上移除默认样式 */
  -webkit-appearance: none;
  outline: none;
  border: none;
}
</style>
