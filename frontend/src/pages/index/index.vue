<template>
  <view class="container">
    <!-- 状态栏占位 -->
    <view class="status-bar"></view>

    <!-- 顶部导航栏 -->
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
        <view class="avatar-box">
          <image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAIQzgWcNfhgZFvam6mBjP3SQ_yGWwerChZs_eM8_z1o9qWN1QnojbmioWRE4k-7JCMz3wyTItaNxPRS96uepbCycNWyeFd5CG59XlkU87Xapse9FwdEkK2Zx1w8ht2xUwawr5W2_YCj4et1disriTjbEFli9UPYJNPga5CS2cxoieZu8XSCzo0RPa4yG7saKzV_HIw3mDGt75bzRZba9L3kB0HwXM3AyVsiTFG6TPJ51agHPrnr6rJ6i2lggc-umhels7SEKP-34Nv"></image>
        </view>
      </view>
    </view>

    <!-- 分类滑动条 -->
    <scroll-view scroll-x class="category-scroll" show-scrollbar="false">
      <view class="category-list">
        <view v-for="(item, index) in categories" :key="index" 
              class="category-item" :class="{ active: currentCategory === index }"
              @click="changeCategory(index)">
          <text class="category-text">{{ item.name }}</text>
          <view v-if="currentCategory === index" class="active-line"></view>
        </view>
      </view>
    </scroll-view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="main-content">
      <!-- 英雄帖子卡片 -->
      <view class="hero-card" @click="goToDetail(heroPost.id)">
        <image class="hero-img" :src="heroPost.image" mode="aspectFill"></image>
        <view class="hero-overlay">
          <view class="tag-row">
            <text class="hero-tag">今日要闻</text>
            <text class="hero-subtag" v-if="heroPost.category">{{ heroPost.category }}</text>
          </view>
          <text class="hero-title">{{ heroPost.title }}</text>
          <view class="hero-meta">
              <view class="meta-item">
                <u-icon name="clock" size="28rpx" color="rgba(255,255,255,0.6)"></u-icon>
                <text class="meta-text">{{ heroPost.time }}</text>
              </view>
              <view class="meta-item">
                <u-icon name="chat" size="28rpx" color="rgba(255,255,255,0.6)"></u-icon>
                <text class="meta-text">{{ heroPost.comments }} 评论</text>
              </view>
            </view>
        </view>
      </view>

      <!-- 推荐列表 -->
      <view class="section-header">
        <text class="section-title">为你推荐</text>
        <text class="view-all" @click="goToSearch">查看全部</text>
      </view>

      <view class="post-list">
        <view v-for="(post, index) in recommendPosts" :key="index" class="post-item" @click="goToDetail(post.id)">
          <view class="post-main">
            <view class="post-img-box">
              <image class="post-img" :src="post.image" mode="aspectFill"></image>
              <view class="ai-badge" v-if="post.isAi">
                <u-icon name="star-fill" color="#000" size="20rpx"></u-icon>
                <text class="ai-text">AI</text>
              </view>
            </view>
            <view class="post-info">
              <text class="post-title">{{ post.title }}</text>
              <view class="post-footer">
                <text class="post-meta">{{ post.category }} · {{ post.time }}</text>
                <view class="post-stats">
                  <view class="stat-item">
                    <u-icon name="thumb-up" color="#f9d406" size="28rpx"></u-icon>
                    <text class="stat-num">{{ post.likes }}</text>
                  </view>
                  <view class="stat-item">
                    <u-icon name="chat" color="#f9d406" size="28rpx"></u-icon>
                    <text class="stat-num">{{ post.comments }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <!-- AI 摘要 -->
          <view class="ai-summary" v-if="post.aiSummary">
            <text class="ai-summary-text">
              <text class="ai-label">AI 摘要：</text>{{ post.aiSummary }}
            </text>
          </view>
        </view>
      </view>
      
      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: currentTab === index }"
        @tap="handleTabClick(index)">
        <u-icon :name="tab.icon" :color="currentTab === index ? '#f9d406' : '#7A7E83'" size="24"></u-icon>
        <text class="tab-text">{{ tab.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { postApi, newsApi } from '@/api'

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

const currentTab = ref(0)
const tabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar', path: 'pages/schedule/schedule' },
  { text: '社区', icon: 'chat', path: 'pages/index/index' },
  { text: '我的', icon: 'account', path: 'pages/index/index' }
]

const heroPost = ref({
  id: 1,
  title: '正在加载最新资讯...',
  category: '足球',
  image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuB_e4YK349efMWAzc6iiczsM5tSzTnAKKVMkYWXuj_EZTKUOgmCBpnnu9Nbnv6E_FPf3uOXeVMAZeYmf3_CFGfrFh2zP8C9IllzF46IRUKxa5PkZCETfbd_SN_sf9OYPfbrjmRnDvFxK5Zh72kjxUmQdfZmWyAQPUswEsB_JPZ7Lg8vKjR7v9NW95_dy2rxLJT3_kqmFCXRt_rhPduU4AnhbTzNaY_CEkRqdE9LXCnbCiDBBS46kbHrjJNLAf_GrFim5hEkkJELnzex'
})

const recommendPosts = ref([])

const getFullImageUrl = (url) => {
  if (!url) return 'https://lh3.googleusercontent.com/aida-public/AB6AXuB0ptLMlSVayrxqg7fSU6QDrJuEJuUNPhe-FL1oaNUhm5hTHeodIRF-8F-Rhnt-zmJoOquOmsQNkJZBxgkbYOOFqHH1PmXnGA1CbyV6PA9hrFDxkLutFdYm13dkV64EU91kS5OhqpWiTd-1LJLAhJEGACSHdpuxggaRg43262dPjrfJeJh2pEQjQD49zmdB7sz_ar6feJpTa1Fd9ZDHSaAUKmXEic6DkqfyodMJkt3egV4exRVQQbB_9cITUjEiy5btIR-1DrGQbFS1'
  if (url.startsWith('http')) return url
  // 拼接后端根地址，强制使用 8080
  const BASE_URL = 'http://localhost:8080'
  const fullUrl = BASE_URL + (url.startsWith('/') ? url : '/' + url)
  return fullUrl
}

const formatTime = (timeStr) => {
  if (!timeStr) return '刚刚'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = (now - date) / 1000 // 秒
  
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 2592000) return Math.floor(diff / 86400) + '天前'
  
  return timeStr.split('T')[0] // 返回日期部分
}

const changeCategory = (index) => {
  currentCategory.value = index
  loadData()
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
    let hasNewsData = false
    if (newsRes && newsRes.records && newsRes.records.length > 0) {
      hasNewsData = true
      const records = newsRes.records.map(item => ({
        id: item.id,
        title: item.title,
        image: getFullImageUrl(item.coverUrl),
        category: item.category || '足球',
        time: formatTime(item.publishTime),
        likes: item.likeCount || 0,
        comments: item.commentCount || 0,
        isAi: true,
        aiSummary: item.summary
      }))
      
      // 设置第一条为英雄贴
      heroPost.value = records[0]
      // 其余为推荐列表
      recommendPosts.value = records.slice(1)
    } else {
      // 如果当前分类没有数据，清空英雄贴
      heroPost.value = {
        id: 0,
        title: '暂无相关资讯',
        image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuB_e4YK349efMWAzc6iiczsM5tSzTnAKKVMkYWXuj_EZTKUOgmCBpnnu9Nbnv6E_FPf3uOXeVMAZeYmf3_CFGfrFh2zP8C9IllzF46IRUKxa5PkZCETfbd_SN_sf9OYPfbrjmRnDvFxK5Zh72kjxUmQdfZmWyAQPUswEsB_JPZ7Lg8vKjR7v9NW95_dy2rxLJT3_kqmFCXRt_rhPduU4AnhbTzNaY_CEkRqdE9LXCnbCiDBBS46kbHrjJNLAf_GrFim5hEkkJELnzex'
      }
    }

    // 2. 只有在完全没有资讯数据时，才回退加载社区帖子
    if (!hasNewsData) {
      const postRes = await postApi.getList({ page: 1, size: 10 })
      if (postRes && postRes.records) {
        recommendPosts.value = postRes.records.map(item => ({
          id: item.id,
          title: item.title,
          image: item.images ? getFullImageUrl(JSON.parse(item.images)[0]) : 'https://lh3.googleusercontent.com/aida-public/AB6AXuA3a1YaN0N9eGVEJt27x1pvl9bmA_s1bXVgR5SdN9DFsrcqh6uzFlCKQvzphdGraw6VAXJOL5xegk1zZJscCAfDzZfGT4Fejcexhqu_xRMaGLwAyC_dwx8QT7G51wSlpvwCfM0s_1gUg5P-ukoYTMarx8NlDRmwAi91_C2xGc72pSqbXHKu8-PoTd87oU497A_XdyMvzv_I_h0RJiCgkPZHjNdFRYVe0WX4_DdpS88_1KhVnI26khp2KS9dOKUy089md8x3oQERrucF',
          category: '社区',
          time: formatTime(item.createdAt),
          likes: item.likeCount,
          comments: item.commentCount,
          isAi: false
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
  }
}

onMounted(() => {
  loadData()
})

const goToDetail = (id) => {
  if (!id) return
  uni.navigateTo({
    url: `/pages/news/detail?id=${id}`
  })
}

const goToSearch = () => {
  uni.navigateTo({
    url: '/pages/search/search'
  })
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
  // 保持向后兼容性
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

</script>

<style lang="scss">
.container {
    min-height: 100vh;
    background-color: $pitch-pulse-bg-dark;
    color: #fff;
    display: flex;
    flex-direction: column;
    position: relative;
    width: 100%;
    margin: 0 auto;
    overflow-x: hidden;
    box-sizing: border-box;
  }

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
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
  background-color: transparent; /* 移除原有背景色 */
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10rpx; /* 增加留白 */
  
  .logo-img {
    width: 100%; /* 配合 padding 自动缩放 */
    height: 100%;
    /* 应用色彩滤镜，使其偏向金黄色并增加立体感 */
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
}

.avatar {
  width: 100%;
  height: 100%;
}

.category-scroll {
    width: 100%;
    background-color: $pitch-pulse-bg-dark;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
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
    color: rgba(255, 255, 255, 0.4);
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
  width: 100%;
  padding: 30rpx 40rpx;
  box-sizing: border-box;
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
  color: rgba(255, 255, 255, 0.6);
  font-size: 20rpx;
  font-weight: 500;
  text-transform: uppercase;
}

.hero-title {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 20rpx;
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
  color: rgba(255, 255, 255, 0.5);
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
}

.post-main {
  display: flex;
  gap: 30rpx;
}

.post-img-box {
  position: relative;
  width: 240rpx;
  height: 180rpx;
  flex-shrink: 0;
}

.post-img {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
}

.ai-badge {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  background-color: $pitch-pulse-primary;
  color: #000;
  font-size: 18rpx;
  font-weight: 800;
  padding: 2rpx 10rpx;
  border-radius: 4rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.post-title {
  font-size: 28rpx;
  font-weight: 700;
  line-height: 1.4;
  color: rgba(255, 255, 255, 0.9);
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-meta {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
}

.post-stats {
  display: flex;
  gap: 20rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.stat-num {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
}

.ai-summary {
  margin-top: 20rpx;
  background-color: rgba($pitch-pulse-primary, 0.05);
  border: 1rpx solid rgba($pitch-pulse-primary, 0.1);
  padding: 15rpx;
  border-radius: 12rpx;
}

.ai-summary-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
  font-style: italic;
  line-height: 1.5;
}

.ai-label {
  color: $pitch-pulse-primary;
  font-weight: 700;
}

/* 2. 修正悬浮按钮：让它相对于 tab-bar 稳定悬浮 */ 
 .fab-btn { 
   position: fixed; 
   bottom: 180rpx; /* 抬高位置，避免遮挡 tab-bar */ 
   
   /* 重点：在 H5 居中模式下，右边距需要特殊处理 */ 
   /* #ifdef H5 */ 
   left: 50%; 
   transform: translateX(140rpx); /* 通过位移精确控制它在容器右侧 */ 
   /* #endif */ 
   
   /* 非 H5 环境（如真机）使用常规右定位 */ 
   /* #ifndef H5 */ 
   right: 40rpx; 
   /* #endif */ 
   
   width: 110rpx; 
   height: 110rpx; 
   background-color: $pitch-pulse-primary; 
   border-radius: 50%; 
   display: flex; 
   justify-content: center; 
   align-items: center; 
   box-shadow: 0 10rpx 30rpx rgba($pitch-pulse-primary, 0.4); 
   z-index: 999; 
 } 
 
 /* 1. 修正底部导航栏：确保在居中模式下也能对齐 */ 
 .tab-bar { 
   position: fixed; 
   bottom: 0; 
   /* 取消之前的 transform 居中，改用 auto 以适配外层容器 */ 
   left: 0; 
   right: 0; 
   margin: 0 auto; 
   width: 100%; 
   
   /* #ifdef H5 */ 
   max-width: 500px; /* 必须与你的模拟器宽度一致 */ 
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
 
 /* 3. 修正 tab-item：确保宽度平分 */ 
 .tab-item { 
   flex: 1; /* 强制平分布局 */ 
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
      color: $pitch-pulse-primary;
      font-weight: 700;
    }
  }
}

.bottom-placeholder {
  height: 160rpx;
}
</style>
