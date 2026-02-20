<script setup>
import { ref, onMounted } from 'vue'
import { onLoad, onPageScroll } from '@dcloudio/uni-app'
import { newsApi, favoriteApi } from '@/api/index'

const newsId = ref(null)
const news = ref({
  title: '正在加载...',
  coverUrl: '',
  publishTime: '',
  categoryId: 0,
  content: '',
  author: 'PitchPulse 编辑部',
  authorSub: '深度足球组'
})

const scrollProgress = ref(0)
const isFavorited = ref(false)
const fontSizeLevel = ref(0)
const isEyeProtection = ref(false)
const fontClasses = ['text-base', 'text-lg', 'text-xl']

onLoad((options) => {
  if (options.id) {
    newsId.value = options.id
    fetchNewsDetail(options.id)
    checkFavoriteStatus(options.id)
  }
})

const checkFavoriteStatus = async (id) => {
  try {
    const res = await favoriteApi.check({ newsId: id })
    if (res !== undefined) {
      isFavorited.value = res
    }
  } catch (error) {
    console.error('Check favorite status failed:', error)
  }
}

const toggleFavorite = async () => {
  try {
    const res = await favoriteApi.toggle({ newsId: newsId.value })
    if (res && res.favorited !== undefined) {
      isFavorited.value = res.favorited
      uni.showToast({ title: res.favorited ? '已收藏' : '已取消收藏', icon: 'success' })
    }
  } catch (error) {
    console.error('Toggle favorite failed:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const toggleFontSize = () => {
  fontSizeLevel.value = (fontSizeLevel.value + 1) % fontClasses.length
  const sizes = ['标准', '大', '超大']
  uni.showToast({ title: `字号：${sizes[fontSizeLevel.value]}`, icon: 'none' })
}

const toggleEyeProtection = () => {
  isEyeProtection.value = !isEyeProtection.value
  uni.showToast({ title: isEyeProtection.value ? '已开启护眼模式' : '已关闭护眼模式', icon: 'none' })
}

const handleShare = () => {
  // #ifdef H5
  uni.setClipboardData({
    data: window.location.href,
    success: () => {
      uni.showToast({ title: '链接已复制', icon: 'success' })
    }
  })
  // #endif
  // #ifndef H5
  uni.share({
    provider: "weixin",
    scene: "WXSceneSession",
    type: 0,
    href: "http://localhost:8080/#/pages/news/detail?id=" + newsId.value,
    title: news.value.title,
    summary: news.value.content.substring(0, 50),
    imageUrl: news.value.coverUrl,
    success: function (res) {
        console.log("success:" + JSON.stringify(res));
    },
    fail: function (err) {
        console.log("fail:" + JSON.stringify(err));
    }
  });
  // #endif
}


const fetchNewsDetail = async (id) => {
  try {
    const res = await newsApi.getDetail(id)
    if (res) {
      news.value = {
        ...res,
        author: res.author || 'PitchPulse 编辑部',
        authorSub: res.authorSub || '深度足球组'
      }
    }
  } catch (e) {
    console.error('获取新闻详情失败:', e)
    uni.showToast({
      title: '获取详情失败',
      icon: 'none'
    })
  }
}

const getCategoryName = (categoryId) => {
  const maps = {
    1: '中超',
    2: '英超',
    3: '德甲',
    4: '意甲',
    5: '欧战',
    6: '西甲'
  }
  return maps[categoryId] || '足球'
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

const goBack = () => {
  uni.navigateBack()
}

onPageScroll((e) => {
  const scrollTop = e.scrollTop
  // 获取页面总高度
  uni.getSystemInfo({
    success: (res) => {
      const windowHeight = res.windowHeight
      const query = uni.createSelectorQuery()
      query.select('.news-detail-container').boundingClientRect((rect) => {
        if (rect) {
          const totalHeight = rect.height
          const progress = (scrollTop / (totalHeight - windowHeight)) * 100
          scrollProgress.value = Math.min(Math.max(progress, 0), 100)
        }
      }).exec()
    }
  })
})

const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}
</script>

<template>
  <view :class="['news-detail-container font-display min-h-screen transition-colors duration-300', isEyeProtection ? 'bg-[#f0ecd6] text-gray-800' : 'bg-[#1a1811] text-gray-200']">
    <!-- Header / Navigation -->
    <header :class="['fixed top-0 left-0 right-0 z-50 backdrop-blur-md border-b h5-header-fix transition-colors duration-300', isEyeProtection ? 'bg-[#f0ecd6]/80 border-black/5' : 'bg-[#1a1811]/80 border-white/5']">
      <view class="flex items-center justify-between px-4 h-14 max-w-2xl mx-auto">
        <button @click="goBack" class="flex items-center justify-center w-10 h-10 -ml-2 text-primary hover:bg-primary/10 rounded-full transition-colors bg-transparent border-none">
          <text class="material-icons">chevron_left</text>
        </button>
        <view class="flex items-center gap-1">
          <view class="w-6 h-6 bg-primary rounded-sm flex items-center justify-center">
            <text class="text-white font-bold text-xs">P</text>
          </view>
          <text :class="['font-bold tracking-tight transition-colors', isEyeProtection ? 'text-gray-900' : 'text-white']">PITCHPULSE</text>
        </view>
        <view class="w-10"></view> <!-- Spacer for balance -->
      </view>
      <!-- Reading Progress Indicator -->
      <view class="h-[2px] w-full bg-white/5">
        <view class="h-full bg-primary transition-all duration-100" :style="{ width: scrollProgress + '%' }"></view>
      </view>
    </header>

    <!-- Main Content -->
    <main class="pt-20 pb-24 max-w-2xl mx-auto px-5">
      <!-- Metadata & Title -->
      <view class="mb-8">
        <view class="flex items-center gap-3 mb-4">
          <text class="text-xs font-semibold uppercase tracking-wider text-primary py-1 px-2 bg-primary/10 rounded">
            {{ getCategoryName(news.categoryId) }}
          </text>
          <text :class="['text-xs transition-colors', isEyeProtection ? 'text-gray-500' : 'text-gray-400']">{{ formatTime(news.publishTime) }}</text>
        </view>
        <h1 :class="['text-3xl md:text-4xl font-bold leading-tight mb-6 transition-colors', isEyeProtection ? 'text-gray-900' : 'text-white']">
          {{ news.title }}
        </h1>
        <view :class="['flex items-center justify-between border-y py-4 transition-colors', isEyeProtection ? 'border-black/5' : 'border-white/5']">
          <view class="flex items-center gap-3">
            <view class="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center">
              <text class="material-icons text-primary text-sm">edit</text>
            </view>
            <view>
              <text :class="['text-sm font-semibold block transition-colors', isEyeProtection ? 'text-gray-900' : 'text-white']">{{ news.author }}</text>
              <text :class="['text-xs block transition-colors', isEyeProtection ? 'text-gray-500' : 'text-gray-500']">{{ news.authorSub }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Article Body -->
      <article :class="['article-content prose max-w-none transition-colors duration-300', isEyeProtection ? 'prose-stone text-gray-800' : 'prose-invert text-gray-300', fontClasses[fontSizeLevel]]">
        <rich-text :nodes="news.content"></rich-text>
      </article>

      <!-- End of Article Decorator -->
      <view class="mt-20 flex flex-col items-center">
        <view :class="['w-12 h-px mb-6 transition-colors', isEyeProtection ? 'bg-black/10' : 'bg-white/20']"></view>
        <view class="w-10 h-10 bg-primary/20 rounded-full flex items-center justify-center mb-2">
          <text class="text-primary font-bold text-sm tracking-tighter">PP</text>
        </view>
        <text :class="['text-xs tracking-widest uppercase transition-colors', isEyeProtection ? 'text-gray-500' : 'text-gray-600']">END OF ARTICLE</text>
      </view>
    </main>

    <!-- Bottom Action Bar (Floating) -->
    <view :class="['fixed bottom-6 left-1/2 -translate-x-1/2 w-[90%] max-w-md h-14 backdrop-blur-xl rounded-full border shadow-2xl flex items-center justify-around px-6 z-50 transition-colors duration-300', isEyeProtection ? 'bg-[#f0ecd6]/90 border-black/10 shadow-gray-200/50' : 'bg-[#1a1811]/90 border-white/10']">
      
      <!-- Favorite -->
      <view @click="toggleFavorite" class="flex flex-col items-center gap-0.5 transition-colors cursor-pointer" :class="isFavorited ? 'text-red-500' : (isEyeProtection ? 'text-gray-600 hover:text-primary' : 'text-gray-400 hover:text-primary')">
        <text class="material-icons text-xl">{{ isFavorited ? 'favorite' : 'favorite_border' }}</text>
        <text class="text-[10px] font-medium">{{ isFavorited ? '已收藏' : '收藏' }}</text>
      </view>

      <view :class="['w-px h-6', isEyeProtection ? 'bg-black/10' : 'bg-white/10']"></view>

      <!-- Font Size -->
      <view @click="toggleFontSize" :class="['flex flex-col items-center gap-0.5 transition-colors cursor-pointer', isEyeProtection ? 'text-gray-600 hover:text-primary' : 'text-gray-400 hover:text-primary']">
        <text class="material-icons text-xl">text_fields</text>
        <text class="text-[10px] font-medium">字号</text>
      </view>

      <view :class="['w-px h-6', isEyeProtection ? 'bg-black/10' : 'bg-white/10']"></view>

      <!-- Eye Protection -->
      <view @click="toggleEyeProtection" :class="['flex flex-col items-center gap-0.5 transition-colors cursor-pointer', isEyeProtection ? 'text-primary' : 'text-gray-400 hover:text-primary']">
        <text class="material-icons text-xl">{{ isEyeProtection ? 'brightness_7' : 'nightlight_round' }}</text>
        <text class="text-[10px] font-medium">{{ isEyeProtection ? '日间' : '护眼' }}</text>
      </view>

      <view :class="['w-px h-6', isEyeProtection ? 'bg-black/10' : 'bg-white/10']"></view>

      <!-- Share -->
      <view @click="handleShare" :class="['flex flex-col items-center gap-0.5 transition-colors cursor-pointer', isEyeProtection ? 'text-gray-600 hover:text-primary' : 'text-gray-400 hover:text-primary']">
        <text class="material-icons text-xl">share</text>
        <text class="text-[10px] font-medium">转发</text>
      </view>
    </view>
  </view>
</template>

<style>
/* #ifdef H5 */
.h5-header-fix {
  max-width: 500px;
  margin: 0 auto;
}
/* #endif */
</style>

<style scoped>
.article-content {
  line-height: 1.8;
  letter-spacing: 0.01em;
}

.article-content :deep(p) {
  margin-bottom: 1.5rem;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0.5rem;
  margin: 1rem 0;
}

/* 隐藏 H5 默认 header */
/* #ifdef H5 */
uni-page-head {
  display: none;
}
/* #endif */
</style>