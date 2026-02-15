<template>
  <view class="search-page bg-[#1a1811] min-h-screen font-display text-gray-200">
    <!-- Header -->
    <header class="fixed top-0 left-0 right-0 z-50 bg-[#1a1811]/80 backdrop-blur-md border-b border-white/5">
      <view class="flex items-center gap-3 px-4 h-16 max-w-2xl mx-auto">
        <button @click="goBack" class="flex items-center justify-center w-10 h-10 -ml-2 text-primary hover:bg-primary/10 rounded-full transition-colors bg-transparent border-none">
          <text class="material-icons text-2xl">chevron_left</text>
        </button>
        <view class="flex-1 relative">
          <input 
            v-model="keyword" 
            type="text" 
            placeholder="搜索资讯或帖子..." 
            class="w-full h-10 bg-white/5 border border-white/10 rounded-full px-10 text-sm focus:outline-none focus:border-primary/50 focus:bg-white/10 transition-all"
            @confirm="handleSearch"
            :focus="true"
          />
          <text class="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 text-lg">search</text>
          <text 
            v-if="keyword" 
            @click="keyword = ''" 
            class="material-icons absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 text-lg cursor-pointer hover:text-white"
          >close</text>
        </view>
        <button @click="handleSearch" class="text-primary font-bold text-sm bg-transparent border-none px-2">搜索</button>
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
    console.log('发送请求参数:', params)
    
    const res = await searchApi.globalSearch(params)
    console.log('收到后端响应 (Raw):', res)
    
    if (!res) {
      console.warn('后端返回结果为空对象')
      return
    }
    
    const combinedResults = []
    
    // 1. 处理资讯
    const newsData = res.news || res.News
    if (newsData && newsData.records) {
      console.log('找到资讯数量:', newsData.records.length)
      newsData.records.forEach(item => {
        combinedResults.push({
          ...item,
          type: 'news',
          displayTime: item.publishTime
        })
      })
    } else {
      console.log('未发现资讯数据或 records 为空')
    }
    
    // 2. 处理帖子
    const postsData = res.posts || res.Posts
    if (postsData && postsData.records) {
      console.log('找到帖子数量:', postsData.records.length)
      postsData.records.forEach(item => {
        combinedResults.push({
          ...item,
          type: 'post',
          displayTime: item.createdAt,
          author: item.username || '社区用户'
        })
      })
    } else {
      console.log('未发现帖子数据或 records 为空')
    }
    
    // 按时间倒序排序
    combinedResults.sort((a, b) => {
      const timeA = new Date(a.displayTime || 0)
      const timeB = new Date(b.displayTime || 0)
      return timeB - timeA
    })
    
    results.value = combinedResults
    console.log('最终展示结果列表:', results.value)
  } catch (e) {
    console.error('搜索请求异常:', e)
    uni.showToast({ title: '搜索失败: ' + (e.message || '网络错误'), icon: 'none' })
  } finally {
    loading.value = false
  }
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
</style>
