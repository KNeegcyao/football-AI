<template>
  <view class="container" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- 固定顶部容器 -->
    <view class="fixed-header">
      <!-- 状态栏占位 -->
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

      <!-- 顶部导航栏 -->
      <view class="nav-bar bg-nav-bar border-b border-theme-main">
        <view class="logo-area">
          <view class="logo-icon">
            <image src="/static/soccer-logo.png" mode="aspectFit" class="logo-img"></image>
          </view>
          <view class="logo-text italic">
            PITCH<text class="highlight text-theme-main">PULSE</text>
          </view>
        </view>

        <view class="nav-actions">
          <view class="action-btn" @click="goToSearch">
            <text class="material-icons" style="font-size: 48rpx; color: var(--text-main);">search</text>
          </view>
          <view class="avatar-box" @click="goToProfile">
            <image class="avatar" :src="userAvatar" mode="aspectFill" @error="handleAvatarError"></image>
          </view>
        </view>
      </view>

      <!-- 分类滑动条 -->
      <scroll-view scroll-x class="category-scroll bg-nav-bar" :show-scrollbar="false">
        <view class="category-list">
          <view v-for="(item, index) in categories" :key="index" 
                class="category-item" :class="{ active: currentCategory === index }"
                @click="changeCategory(index)">
            <text class="category-text" :class="currentCategory === index ? 'text-theme-main' : 'text-theme-secondary'">{{ item.name }}</text>
            <view v-if="currentCategory === index" class="active-line"></view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="main-content">
      <!-- 顶部占位，防止内容被固定的 header 遮挡 -->
      <view class="header-placeholder"></view>
      
      <!-- 英雄帖子卡片 -->
      <view class="hero-card anim-slide-up" hover-class="btn-active" :hover-start-time="20" :hover-stay-time="70" @click="goToDetail(heroPost.id)">
        <image class="hero-img" :src="heroPost.image" mode="aspectFill"></image>
        <view class="hero-overlay">
          <view class="tag-row">
            <text class="hero-tag">今日要闻</text>
            <text class="hero-subtag" v-if="heroPost.category">{{ heroPost.category }}</text>
            <!-- 英雄帖 AI 勋章 -->
            <view class="ai-badge hero-ai-badge anim-scale-in stagger-2" v-if="heroPost.isAi" @click.stop="toggleHeroAiSummary">
              <text class="material-icons" style="font-size: 20rpx; color: #f9d406;">auto_awesome</text>
              <text class="ai-text">AI</text>
            </view>
          </view>
          <text class="hero-title">{{ heroPost.title }}</text>
          <view class="hero-meta">
              <view class="meta-item">
                <text class="material-icons" style="font-size: 28rpx; color: #fff; opacity: 0.8;">schedule</text>
                <text class="meta-text">{{ heroPost.time }}</text>
              </view>
              <view class="meta-item">
                <text class="material-icons" style="font-size: 28rpx; color: #fff; opacity: 0.8;">star</text>
                <text class="meta-text">{{ heroPost.collections || 0 }}</text>
              </view>
              <view class="meta-item">
                <text class="material-icons" style="font-size: 28rpx; color: #fff; opacity: 0.8;">visibility</text>
                <text class="meta-text">{{ heroPost.views || 0 }}</text>
              </view>
            </view>
        </view>
      </view>

      <!-- 英雄帖 AI 摘要 -->
      <view class="hero-ai-summary-container" v-if="heroPost.isAi" :class="{'visible': heroPost.showSummary}">
        <view class="ai-summary no-margin" v-if="heroPost.aiSummary" :class="{'ai-summary-visible': heroPost.showSummary}">
          <view class="ai-summary-content bg-theme-secondary anim-fade-in">
            <view class="ai-summary-header">
              <text class="material-icons" style="font-size: 24rpx; color: #f9d406;">auto_awesome</text>
              <text class="ai-summary-label">AI 智能摘要</text>
            </view>
            <text class="ai-summary-text text-theme-secondary">
              {{ formatAiSummary(heroPost.aiSummary) }}
            </text>
          </view>
        </view>
      </view>

      <!-- 推荐列表 -->
      <view class="section-header anim-fade-in stagger-1">
        <text class="section-title text-theme-main">为你推荐</text>
        <text class="view-all" hover-class="btn-active" @click="goToSearch">查看全部</text>
      </view>

      <view class="post-list">
        <view v-for="(post, index) in recommendPosts" :key="index" class="post-item bg-card anim-slide-up" :class="'stagger-' + ((index % 5) + 2)" hover-class="btn-active" :hover-start-time="20" :hover-stay-time="70" @click="goToDetail(post.id)">
          <view class="post-main">
            <view class="post-img-box">
              <image class="post-img" :src="post.image" mode="aspectFill"></image>
              <view class="ai-badge" v-if="post.isAi" @click.stop="toggleAiSummary(index)">
                <text class="material-icons" style="font-size: 20rpx; color: #f9d406;">auto_awesome</text>
                <text class="ai-text">AI</text>
              </view>
            </view>
            <view class="post-info">
              <text class="post-title text-theme-main">{{ post.title }}</text>
              <view class="post-footer">
                <view class="post-meta-left">
                  <text class="post-category" v-if="post.category">{{ post.category }}</text>
                  <text class="post-time">{{ post.time }}</text>
                </view>
                <view class="post-stats">
                  <view class="stat-item">
                    <text class="material-icons" style="font-size: 24rpx; color: var(--text-main); opacity: 0.5;">star</text>
                    <text class="stat-num">{{ post.collections || 0 }}</text>
                  </view>
                  <view class="stat-item">
                    <text class="material-icons" style="font-size: 24rpx; color: var(--text-main); opacity: 0.5;">visibility</text>
                    <text class="stat-num">{{ post.views || 0 }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <!-- AI 摘要 -->
          <view class="ai-summary" v-if="post.isAi" :class="{'ai-summary-visible': post.showSummary}">
            <view class="ai-summary-content bg-theme-secondary" v-if="post.aiSummary">
              <view class="ai-summary-header">
                <text class="material-icons" style="font-size: 24rpx; color: #f9d406;">auto_awesome</text>
                <text class="ai-summary-label">AI 智能摘要</text>
              </view>
              <text class="ai-summary-text text-theme-secondary">
                {{ formatAiSummary(post.aiSummary) }}
              </text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部导航栏 -->
    <CustomTabBar :currentTab="0" />
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { postApi, newsApi, userApi, fileApi, aiApi } from '@/api'
import { getFullImageUrl } from '@/utils/request'
import { useThemeStore } from '@/store/theme'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)
const userAvatar = ref('/static/default-avatar.png')
const navbarPaddingRight = ref(16) // 默认 16px
const fontLoaded = ref(false)

// 检查字体加载状态
const checkFontLoaded = () => {
  // #ifdef MP-WEIXIN
  // 微信小程序可以通过一些 hack 方式或者定时器，但最稳妥是直接给一个极短的延迟
  setTimeout(() => {
    fontLoaded.value = true
  }, 1000)
  // #endif
  
  // #ifdef H5 || APP-PLUS
  if (document.fonts) {
    document.fonts.ready.then(() => {
      fontLoaded.value = true
    })
  } else {
    setTimeout(() => {
      fontLoaded.value = true
    }, 1000)
  }
  // #endif
}

const handleAvatarError = () => {
  userAvatar.value = '/static/default-avatar.png'
}

const getUserProfile = async () => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) {
      userAvatar.value = '/static/default-avatar.png'
      return
    }
    const res = await userApi.getProfile()
    if (res && res.avatar) {
      userAvatar.value = getFullImageUrl(res.avatar)
    } else {
      userAvatar.value = '/static/default-avatar.png'
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
    userAvatar.value = '/static/default-avatar.png'
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

const currentCategory = ref(0)
const categories = [
  { name: '推荐', id: 0 },
  { name: '中超', id: 1 },
  { name: '英超', id: 2 },
  { name: '德甲', id: 3 },
  { name: '意甲', id: 4 },
  { name: '欧冠', id: 5 },
  { name: '西甲', id: 6 }
]

const heroPost = ref({
  id: 1,
  title: '正在加载最新资讯...',
  category: '足球',
  image: 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=1200&auto=format&fit=crop'
})

const recommendPosts = ref([])

const formatTime = (timeStr) => {
  if (!timeStr) return '刚刚'
  const date = new Date(timeStr.replace(/-/g, '/')) // 兼容 iOS
  const now = new Date()
  const diff = (now - date) / 1000 // 秒
  
  if (diff < 0) return '刚刚'
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  
  const days = Math.floor(diff / 86400)
  if (days < 30) return days + '天前'
  if (days < 365) return Math.floor(days / 30) + '个月前'
  return Math.floor(days / 365) + '年前'
}

const formatAiSummary = (text) => {
  if (!text) return ''
  // 1. 强制前端截断：无论后端存了多少，前端展示绝对不超过 20 字
  let cleaned = text.replace(/#+\s*(摘要|总结)[:：]?\s*/g, '')
    .replace(/^(摘要|总结)[:：]?\s*/g, '')
    .replace(/【深度点评】/g, '')
    .trim()
  
  return cleaned.length > 20 ? cleaned.substring(0, 20) : cleaned
}

const changeCategory = (index) => {
  currentCategory.value = index
  loadData()
}

const toggleAiSummary = async (index) => {
  const post = recommendPosts.value[index]
  if (!post) return
  
  // 切换显示状态
  post.showSummary = !post.showSummary
  
  // 如果打开摘要且当前没有摘要内容，则触发生成
  if (post.showSummary && !post.aiSummary) {
    try {
      // 标记正在加载
      post.aiSummary = 'AI 正在生成精炼摘要...'
      const summary = await aiApi.getNewsSummary(post.id)
      if (summary) {
        post.aiSummary = summary
      } else {
        post.aiSummary = '暂无摘要'
      }
    } catch (e) {
      console.error('AI 摘要生成失败:', e)
      post.aiSummary = '生成失败，请重试'
    }
  }
}

const toggleHeroAiSummary = async () => {
  if (!heroPost.value) return
  
  // 切换显示状态
  heroPost.value.showSummary = !heroPost.value.showSummary
  
  // 如果打开摘要且当前没有摘要内容，则触发生成
  if (heroPost.value.showSummary && !heroPost.value.aiSummary) {
    try {
      // 标记正在加载
      heroPost.value.aiSummary = 'AI 正在生成精炼摘要...'
      const summary = await aiApi.getNewsSummary(heroPost.value.id)
      if (summary) {
        heroPost.value.aiSummary = summary
      } else {
        heroPost.value.aiSummary = '暂无摘要'
      }
    } catch (e) {
      console.error('AI 摘要生成失败:', e)
      heroPost.value.aiSummary = '生成失败，请重试'
    }
  }
}

// 加载数据
const loadData = async () => {
  try {
    recommendPosts.value = [] // 清空旧数据
    
    // 1. 加载资讯列表
    const params = { page: 1, size: 20 }
    // 如果当前选中的不是“推荐”（index 0），则传递 categoryId 参数进行过滤
    if (currentCategory.value > 0) {
      params.categoryId = categories[currentCategory.value].id
    }
    
    const newsRes = await newsApi.getList(params)
    console.log('News list API response:', newsRes)
    let hasNewsData = false
    if (newsRes && newsRes.records && newsRes.records.length > 0) {
      hasNewsData = true
      const records = newsRes.records.map(item => {
        let categoryName = ''
        
        // 优先使用 categoryId 映射
        if (item.categoryId) {
          const cat = categories.find(c => c.id === item.categoryId)
          if (cat) categoryName = cat.name
        }
        
        // 如果没有 categoryId，尝试从 item.category 中提取
        if (!categoryName && item.category) {
          const catStr = item.category
          const matchedCat = categories.find(c => c.id !== 0 && catStr.includes(c.name))
          if (matchedCat) {
            categoryName = matchedCat.name
          }
        }
        
        // 如果还是没有，且 item.category 比较短，则使用它
        if (!categoryName && item.category && item.category.length < 5) {
          categoryName = item.category
        }
        
        // 最终兜底
        categoryName = categoryName || '足球'

        // 处理作者显示，屏蔽“直播吧”
        let authorName = item.author || 'SoccaHub'
        if (authorName.includes('直播吧')) {
          authorName = 'SoccaHub'
        }

        return {
          id: item.id,
          title: item.title,
          image: getFullImageUrl(item.coverUrl),
          category: categoryName,
          time: formatTime(item.publishTime),
          likes: item.likeCount || 0,
          comments: item.commentCount || 0,
          views: item.viewCount || 0,
          collections: item.collectCount !== undefined ? item.collectCount : 0,
          isAi: true,
          aiSummary: item.summary,
          showSummary: false,
          userName: authorName,
          userAvatar: getFullImageUrl('/static/default-avatar.png')
        }
      })
      
      // 设置第一条为英雄贴
      heroPost.value = records[0]
      // 其余为推荐列表
      recommendPosts.value = records.slice(1)
    } else {
      // 如果当前分类没有数据，清空英雄贴
      heroPost.value = {
        id: 0,
        title: '暂无相关资讯',
        image: '/static/teams/generic_stadium.jpg'
      }
    }

    // 2. 只有在完全没有资讯数据时，才回退加载社区帖子
    if (!hasNewsData) {
      const postRes = await postApi.getList({ page: 1, size: 10 })
      if (postRes && postRes.records) {
        recommendPosts.value = postRes.records.map(item => ({
          id: item.id,
          title: item.title,
          image: item.images ? getFullImageUrl(JSON.parse(item.images)[0]) : '/static/teams/generic_stadium.jpg',
          category: '社区',
          time: formatTime(item.createdAt),
          likes: item.likes,
          comments: item.commentCount,
          views: item.views || Math.floor(Math.random() * 500) + 50,
          collections: Math.floor((item.likes || 0) / 3) + (item.commentCount || 0),
          isAi: false,
          userName: item.userName || '未知用户',
          userAvatar: item.userAvatar ? getFullImageUrl(item.userAvatar) : '/static/default-avatar.png'
        }))
      }
    }
  } catch (e) {
    console.error('加载数据失败:', e)
    // 打印更详细的错误堆栈或属性
    if (e instanceof Error) {
      console.error('Error Name:', e.name)
      console.error('Error Message:', e.message)
      console.error('Error Stack:', e.stack)
    }
    
    // 加载失败时的 UI 提示
    heroPost.value = {
      id: 0,
      title: '网络连接失败，请检查后端服务是否启动',
      image: '/static/teams/generic_stadium.jpg'
    }
  }
}

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

  getUserProfile()
  loadData()
})

const isNavigating = ref(false)

const goToDetail = (id) => {
  if (!id) return
  if (isNavigating.value) return
  isNavigating.value = true
  
  uni.navigateTo({
    url: `/pages/news/detail?id=${id}`,
    complete: () => {
      setTimeout(() => {
        isNavigating.value = false
      }, 500)
    },
    fail: () => {
      isNavigating.value = false
    }
  })
}

const goToSearch = () => {
  uni.navigateTo({
    url: '/pages/search/search'
  })
}

// 页面挂载时
onMounted(() => {
  checkFontLoaded()
})

</script>

<style lang="scss">
.container {
    height: 100vh; /* 固定高度为视口高度 */
    background-color: var(--bg-main);
    color: var(--text-main);
    display: flex;
    flex-direction: column;
    position: relative;
    width: 100%;
    margin: 0 auto;
    overflow: hidden; /* 防止容器本身滚动 */
    box-sizing: border-box;
    transition: background-color 0.3s, color 0.3s;
    
    /* #ifdef H5 */
    max-width: 500px;
    /* #endif */
  }

.status-bar {
  width: 100%;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 999;
  background-color: #1a1811;
  
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
}

.nav-bar {
  width: 100%;
  height: 100rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  box-sizing: border-box;
  background-color: #1a1811;
  border-bottom: 1rpx solid var(--border-main);
}

.logo-area {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;

  .logo-icon {
    width: 56rpx;
    height: 56rpx;
    margin-right: 12rpx;
    display: flex;
    align-items: center;

    .logo-img {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    font-size: 32rpx;
    font-weight: 800;
    letter-spacing: 2rpx;
    line-height: 1;
    color: #d4af37;
    
    &.italic {
      font-style: italic;
    }
    
    .highlight {
      color: #fff;
      margin-left: 6rpx;
    }
  }
}

.nav-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
  flex-shrink: 0;

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60rpx;
    height: 60rpx;
  }

  .avatar-box {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    border: 2rpx solid rgba(249, 212, 6, 0.3);
    overflow: hidden;

    .avatar {
      width: 100%;
      height: 100%;
    }
  }
}

.category-scroll {
  width: 100%;
  background-color: var(--nav-bar-bg);
  border-bottom: 1rpx solid var(--border-main);
  white-space: nowrap;
}

.category-list {
  display: flex;
  white-space: nowrap;
  padding: 0 32rpx;
  height: 80rpx;
  align-items: center;
  width: max-content;
}

.category-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 48rpx;
  position: relative;
  flex-shrink: 0;
  
  .category-text {
    font-size: 28rpx;
    color: var(--text-secondary);
    font-weight: 500;
  }
  
  &.active {
    .category-text {
      color: $pitch-pulse-primary;
      font-weight: 700;
    }
  }
}

.active-line {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background-color: $pitch-pulse-primary;
  border-radius: 4rpx;
}

.main-content {
  flex: 1;
  height: 0; /* 必须：强制 flex 计算生效，否则 scroll-view 会自适应高度导致滚动失效 */
  width: 100%;
  padding: 0 32rpx 30rpx; /* 顶部 padding 移到 placeholder */
  box-sizing: border-box;
}

.header-placeholder {
  height: calc(var(--status-bar-height) + 100rpx + 80rpx); /* statusBarHeight + nav-bar(100rpx) + category-scroll(80rpx) */
  width: 100%;
}

.hero-card {
  position: relative;
  width: 100%;
  height: 400rpx;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 40rpx;
}

.hero-img {
  width: 100%;
  height: 100%;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.9), rgba(0,0,0,0.4), transparent);
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.tag-row {
  display: flex;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.hero-tag {
  background-color: $pitch-pulse-primary;
  color: #000;
  font-size: 20rpx;
  font-weight: 800;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  text-transform: uppercase;
}

.hero-subtag {
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
  text-transform: uppercase;
}

.hero-title {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 20rpx;
  color: #FFFFFF; /* 强制白色，不受主题影响，因为背景有黑色渐变 */
}

.hero-meta {
  display: flex;
  gap: 30rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7); /* 增强对比度 */
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 700;
}

.view-all {
  font-size: 24rpx;
  color: $pitch-pulse-primary;
  font-weight: 600;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.post-item {
  display: flex;
  flex-direction: column;
  background-color: rgba(255, 255, 255, 0.03);
  border-radius: 24rpx;
  padding: 20rpx;
  transition: all 0.3s ease;
  border: 1rpx solid rgba(255, 255, 255, 0.05);

  &:active {
    background-color: rgba(255, 255, 255, 0.06);
    transform: scale(0.98);
  }
}

.post-main {
  display: flex;
  gap: 24rpx;
}

.post-img-box {
  position: relative;
  width: 220rpx;
  height: 160rpx;
  flex-shrink: 0;
  border-radius: 16rpx;
  overflow: hidden;
}

.post-img {
  width: 100%;
  height: 100%;
}

.ai-badge {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  background: linear-gradient(135deg, $pitch-pulse-primary, #ffeb3b);
  color: #000;
  font-size: 18rpx;
  font-weight: 900;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
  z-index: 10;
  box-shadow: 0 4rpx 8rpx rgba(0,0,0,0.3);

  &.hero-ai-badge {
    position: relative;
    top: 0;
    left: 0;
    margin-left: 12rpx;
  }

  .ai-text {
    font-size: 18rpx;
    font-weight: 900;
  }
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 4rpx 0;
}

.post-title {
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.95);
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
}

.post-meta-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.post-category {
  font-size: 20rpx;
  color: #000;
  font-weight: 800;
  background-color: $pitch-pulse-primary;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  text-transform: uppercase;
}

.post-time {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.5);
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-num {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 500;
}

/* AI Summary Styles */
.ai-summary {
  height: 0;
  opacity: 0;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateY(-10rpx);
  margin-top: 0;
  padding: 0 4rpx;
  
  &.ai-summary-visible {
    height: auto;
    opacity: 1;
    margin-top: 20rpx;
    transform: translateY(0);
    padding-bottom: 10rpx;
  }
  
  &.no-margin {
    margin-top: 0 !important;
  }
}

.hero-ai-summary-container {
  padding: 0 30rpx;
  height: 0;
  opacity: 0;
  overflow: hidden;
  transition: all 0.4s ease;
  
  &.visible {
    height: auto;
    opacity: 1;
    margin-top: 20rpx;
    margin-bottom: 10rpx;
  }
}

.ai-summary-content {
  border-radius: 8rpx;
  padding: 12rpx 16rpx;
  background-color: rgba(212, 175, 55, 0.04) !important;
  border: 1rpx solid rgba(212, 175, 55, 0.12);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4rpx;
    background-color: #D4AF37;
    opacity: 0.5;
  }
}

.ai-summary-header {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 6rpx;
}

.ai-summary-label {
  font-size: 20rpx;
  font-weight: 800;
  color: #D4AF37;
  text-transform: uppercase;
  letter-spacing: 0.5rpx;
}

.ai-summary-text {
  font-size: 24rpx;
  line-height: 1.2;
  font-style: italic;
  display: block;
  white-space: nowrap; /* 强制单行显示 */
  overflow: hidden;
  text-overflow: ellipsis; /* 溢出显示省略号 */
  color: rgba(255, 255, 255, 0.85);
}

.theme-dark .ai-summary-content {
  background-color: rgba(212, 175, 55, 0.08) !important;
  border-color: rgba(212, 175, 55, 0.2);
}

.theme-light .ai-summary-content {
  background-color: rgba(184, 134, 11, 0.03) !important;
  border-color: rgba(184, 134, 11, 0.1);
  
  .ai-summary-label {
    color: #B8860B;
  }
  
  &::before {
    background-color: #B8860B;
  }
}

.bottom-placeholder {
  height: calc(110rpx + env(safe-area-inset-bottom));
}
</style>
