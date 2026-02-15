<template>
  <view class="search-page bg-[#1a1811] min-h-screen font-display text-gray-200">
    <!-- Header -->
    <header class="fixed top-0 left-0 right-0 z-50 bg-[#1a1811]/80 backdrop-blur-md border-b border-white/5">
      <view class="flex items-center gap-3 px-4 h-16 max-w-2xl mx-auto">
        <button @click="goBack" class="flex items-center justify-center w-10 h-10 -ml-2 text-primary hover:bg-primary/10 rounded-full transition-colors bg-transparent border-none">
          <text class="material-icons text-2xl">chevron_left</text>
        </button>
        
        <!-- 搜索框容器 -->
        <view 
          class="flex-1 relative flex items-center h-10 bg-white/5 border rounded-full px-3 transition-all duration-300"
          :class="[isFocus ? 'border-primary shadow-[0_0_10px_rgba(235,60,60,0.2)] bg-white/10' : 'border-white/10']"
        >
          <text class="material-icons text-gray-500 text-lg mr-2">search</text>
          <input 
            v-model="keyword" 
            type="text" 
            placeholder="搜索资讯或帖子..." 
            class="flex-1 h-full bg-transparent text-sm focus:outline-none"
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
    <main class="pt-20 pb-10 px-4 max-w-2xl mx-auto">
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
          <view class="flex gap-4">
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
              <view class="flex items-center gap-3 text-xs text-gray-500">
                <text>{{ item.author }}</text>
                <view class="flex items-center gap-1">
                  <text class="material-icons text-sm">visibility</text>
                  <text>{{ item.viewCount || 0 }}</text>
                </view>
              </view>
            </view>
            <image 
              v-if="item.coverUrl || item.image" 
              :src="getFullImageUrl(item.coverUrl || item.image)" 
              mode="aspectFill" 
              class="w-24 h-24 rounded-lg object-cover flex-shrink-0"
            />
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
    
    // 1. 处理资讯
    const newsData = res.news || res.News
    if (newsData && newsData.records) {
      newsData.records.forEach(item => {
        combinedResults.push({
          ...item,
          type: 'news',
          displayTime: item.publishTime
        })
      })
    }
    
    // 2. 处理帖子
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

const goToDetail = (item) => {
  const url = item.type === 'news' 
    ? `/pages/news/detail?id=${item.id}` 
    : `/pages/community/detail?id=${item.id}`
  uni.navigateTo({ url })
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
