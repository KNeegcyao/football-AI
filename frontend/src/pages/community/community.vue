<template>
  <view class="container">
    <!-- Status Bar Placeholder -->
    <view class="status-bar"></view>

    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <view>
          <text class="subtitle">PULSE DISCOVERY</text>
          <text class="title">社区发现</text>
        </view>
        <view class="notification-btn">
          <text class="material-icons" style="font-size: 20px; color: #f2b90d;">notifications_none</text>
        </view>
      </view>
      
      <!-- Search Bar -->
      <view class="search-bar">
        <text class="material-icons search-icon">search</text>
        <input class="search-input" placeholder="搜索圈子、话题" placeholder-style="color: rgba(242, 185, 13, 0.3)" />
      </view>
    </view>

    <!-- Main Content -->
    <scroll-view scroll-y class="main-scroll" :style="{height: scrollHeight + 'px'}">
      <!-- Hot Circles -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">热门圈子</text>
          <text class="view-all">查看全部</text>
        </view>
        
        <scroll-view scroll-x class="circles-scroll" show-scrollbar="false">
          <view class="circles-container">
            <view class="circle-item" v-for="(circle, index) in hotCircles" :key="index" @click="navigateToCircle(circle)">
              <view class="circle-avatar-wrapper" :class="{'highlight': index === 0}">
                <view class="circle-avatar-inner">
                  <image class="circle-avatar" :src="circle.image" mode="aspectFill"></image>
                </view>
              </view>
              <text class="circle-name">{{ circle.name }}</text>
              <text class="circle-members">{{ circle.members }} 成员</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- Trend Topics -->
      <view class="section">
        <view class="section-header">
          <text class="section-title">趋势话题</text>
          <view class="trending-badge">
            <text class="material-icons" style="font-size: 14px;">trending_up</text>
            <text class="trending-text">正在热议</text>
          </view>
        </view>

        <view class="trends-list">
          <view class="trend-item glass-panel" v-for="(topic, index) in trendTopics" :key="index" @click="navigateToPost(topic)">
            <view class="trend-bg-icon">
              <text class="material-icons" style="font-size: 60px; color: #f2b90d; opacity: 0.1;">tag</text>
            </view>
            <view class="trend-content">
              <text class="trend-title">{{ topic.title }}</text>
              <text class="trend-stats">{{ topic.stats }}</text>
              
              <view class="trend-avatars" v-if="topic.avatars">
                <view class="avatar-group">
                  <image v-for="(avatar, i) in topic.avatars" :key="i" :src="avatar" class="mini-avatar" mode="aspectFill"></image>
                  <view class="mini-avatar-count" v-if="topic.extraCount">
                    <text class="count-text">+{{ topic.extraCount }}</text>
                  </view>
                </view>
              </view>
              
              <view class="trend-meta" v-if="topic.tags">
                <text class="meta-tag">{{ topic.tags[0] }}</text>
                <text class="meta-time">{{ topic.time }}</text>
              </view>
            </view>
            
            <button class="action-btn" :class="{'btn-join': topic.action === '加入', 'btn-explore': topic.action === '探索'}">
              {{ topic.action }}
            </button>
          </view>
        </view>
      </view>
      
      <!-- Bottom Padding for TabBar -->
      <view class="safe-area-bottom"></view>
    </scroll-view>

    <!-- FAB -->
    <view class="fab-btn" @click="handlePostClick">
      <text class="material-icons" style="font-size: 60rpx;">add</text>
    </view>

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
import { ref, onMounted } from 'vue';
import { communityApi } from '@/api';

const scrollHeight = ref(800); // Should be calculated based on window height

const currentTab = ref(2); // 社区是第3个，索引为2
const tabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar', path: 'pages/schedule/schedule' },
  { text: '社区', icon: 'chat', path: 'pages/community/community' },
  { text: '我的', icon: 'account', path: 'pages/my/my' }
];

const handleTabClick = (index) => {
  const tab = tabs[index]
  if (!tab || !tab.path) return
  
  if (currentTab.value === index) return

  const url = tab.path.startsWith('/') ? tab.path : '/' + tab.path
  
  uni.switchTab({
    url: url,
    fail: () => {
      uni.reLaunch({ url })
    }
  })
}

const handlePostClick = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
};

const navigateToCircle = (circle) => {
  console.log('Navigate to circle:', circle);
  uni.navigateTo({
    url: `/pages/community/circle-detail?name=${encodeURIComponent(circle.name)}&members=${encodeURIComponent(circle.members)}&image=${encodeURIComponent(circle.image)}`,
    fail: () => {
      uni.showToast({
        title: '圈子详情页加载失败',
        icon: 'none'
      });
    }
  });
};

const navigateToPost = (topic) => {
  console.log('Navigate to topic:', topic);
  uni.navigateTo({
    url: `/pages/community/topic-detail?id=${topic.id || 0}&title=${encodeURIComponent(topic.title)}`,
    fail: () => {
      uni.showToast({
        title: '话题详情页加载失败',
        icon: 'none'
      });
    }
  });
};

// Mock Data as Fallback
const defaultHotCircles = [
  { name: '皇家马德里', members: '240万', image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDVNO1DpjWhPQCmDKg01xb5yAMwrIDN-8i_4q50S47genicKUfZQsx0u64atjcu_w2D88dyPoW1vStbL_XEappjN6xfxsvGY1Yp5yWml1bRHZLQeVuW1BuQ8rEO4YpZlIdqV2bnvYm-rtCCR1FkX_JHG1j2RqNJacKVq4pzVshDKCTMJGPntSn5CC6R44xAJzY3nZUBxkAATRoTcfNIzMSrt6Ae7iXSEY0Q3sI1gEpm7cNTo3wGk-qZusFIOpI9GYstGHpDXEjKDW9p' },
  { name: '曼联', members: '180万', image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDKhJshhyD3-gYgPHCiTdXJYVKaTZAP6S1WDBxDEEzPv-Kte1XRlZViGK1OeIBiA-kTJ4IRN1WOUCZc6n0onlYH1fspyLYfwgNyzy8b1qo62r5W3WxHiO3VoIpqMAfz06BakxfCieqW1tIEv5CZqHnE1OsHPeejvEPQC0rmwg7YywZ2Dq0F6-GFfop8hSdmiKw_VVMlaz8D98KNQVKUDMxUud5VzYtVjZQHYH_XoLieaUTWyGOtEWjfKEOesuufbnHKkl0fiVX9obVK' },
  { name: '利物浦', members: '150万', image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCcrGz0sfXaED5Xq0eMoDHjivd0idgIGpK2sMp66mRmG_igyA3gU7DCnIggN-wrA95P-SI8xm9F6-khvkPCHa76ESZFWCuWdbBTUsuEnwTuc7EKO6REi8eYw1AkLLBoZ5mU0vEyAzufWOUDUWeu7PoTfYbPvhqlvz0Et5DgzSJ6a4UoCIb9fJZifzNckrmdGeCHKLUtZSFklVKnHvrQ_uNGZ3iB71Fd5YYI4beqxqUS1XOfUmykhcDASnEnnxPTLW7eNtqj4jXwMozs' },
  { name: '拜仁慕尼黑', members: '98万', image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAIjqoKGelhhsIODjdGSO3CDhFsVv7j3h50hmxGGLpcCHbcNTc8FxKcm9S50xwqMcn3aWUdDaIPZ4TcQ36y819Lmo_92CmXGw0vgx5oLhh-IM5h0UtnTFT2-N3Of-x-id1X8Y34H09_ICTh4wlKfLzzIi8Xpi_20Siy7N0YfzvdANO6IBqeAR5Vx-z6gNH0q9azRcKlIqgynr66g2Yt2ui9D8tVcTPVJQoFqao3K6q2fDZr4iztmkEKPfBBGWn2YyR9aGExVBGL_1r_' }
];

const defaultTrendTopics = [
  { 
    title: '#转会窗口动态', 
    stats: '每小时 1.25万 帖子', 
    action: '加入',
    avatars: [
      'https://lh3.googleusercontent.com/aida-public/AB6AXuATdmAqFDBHDLFSrvkOdXszRsl-foBm99iFZSMA9k6U27dxMDGrqDj5wePvJLzco3U5kAjEOwI-fQDTlajpB7soBEt7a_4Z6opAeidctON_JZHOJjeh0tsXauMNDRva4qkpcopzq7n21O_VhPzgk9iWKQLpZ85_jn19oxUVXc-sZIv_RLuyqhh96Bu8CJYZMSb-OoIXK2P56I8ezO94Yyp45f9kTA-5CfGIz-_RQ6bHqh9NjO_aRu04vFk8nRdjoaV_oPVhj81ZaQeH',
      'https://lh3.googleusercontent.com/aida-public/AB6AXuBOdkyV91FluVNjl50vbosguFH1qM1oPIBS-OMN7hr8Vj8JdAUFctKMtJl_kp8yd107Ryg5-dSmtu6RBCtjuFgLdngUnL2W7TvkrcFEPQn6Tnu8mB6R6cdicAPwUWwzYqRmIwkIDfjZ9X0R2WbVYqoaN7hXRr7h81wUHIj1P75-GtWWQ1EYG4mdU6isbKdWxmGlB8lLpfmFaCX8bKvfYhZgjaDsppxWt8ei5e2H4eYcqGGbjEQIHWItSc3Ic6SIePIalo9FsNsViQ6R',
      'https://lh3.googleusercontent.com/aida-public/AB6AXuB5RVAFby5yx4vJooY_hLKHj39lXJr7XaPyb2gxNTedCulZD3WGJR3Wf4gx3f5bLJadkMUN7djSieqTOC29ZpAAYyJjE7IFHAE_phuCzp52J5O3HRxe0TI4CCiOgT0iR2TDligRPLCJcSLROCyYpPkN2ZPsDk8NBMBp16Oayk1sgpgv4uCG44uy0c0llRWJt_DnK0vxAreEwoaWPwemdUVUnQ0qrNF6wUst9EyaTlrgUc9v41Bl6ZMU3Eh4z3FGTj98TOWBiDD7e7jP'
    ],
    extraCount: 82
  },
  { 
    title: '#欧冠抽签结果', 
    stats: '每小时 8.2k 帖子', 
    action: '探索',
    tags: ['速报'],
    time: '2分钟前活跃'
  },
  { 
    title: '#德比日焦点', 
    stats: '每小时 5.1k 帖子', 
    action: '探索',
    tags: ['比赛日'],
    time: '5分钟前活跃'
  }
];

const hotCircles = ref(defaultHotCircles);
const trendTopics = ref(defaultTrendTopics);

const loadData = async () => {
  try {
    // 尝试从 API 获取数据
    const [circlesRes, topicsRes] = await Promise.allSettled([
      communityApi.getHotCircles(),
      communityApi.getTrendTopics()
    ]);

    if (circlesRes.status === 'fulfilled' && circlesRes.value) {
      hotCircles.value = circlesRes.value;
    } else {
      console.warn('Failed to load hot circles, using default data');
    }

    if (topicsRes.status === 'fulfilled' && topicsRes.value) {
      trendTopics.value = topicsRes.value;
    } else {
      console.warn('Failed to load trend topics, using default data');
    }
  } catch (e) {
    console.error('Error loading community data:', e);
    // 保持默认数据
  }
};

onMounted(() => {
  loadData();
  
  // Calculate scroll height
  uni.getSystemInfo({
    success: (res) => {
      // 减去头部高度、底部导航栏高度等
      scrollHeight.value = res.windowHeight; 
    }
  });
});
</script>

<style scoped>
/* FAB Button Style */
.fab-btn {
  position: fixed;
  bottom: calc(160rpx + env(safe-area-inset-bottom));
  right: 40rpx;
  width: 110rpx;
  height: 110rpx;
  background: linear-gradient(135deg, #f2b90d 0%, #e0a800 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(242, 185, 13, 0.4);
  z-index: 9990;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.fab-btn:active {
  transform: scale(0.9);
  box-shadow: 0 4rpx 10rpx rgba(242, 185, 13, 0.4);
}

/* TabBar Styles aligned with schedule.vue */
/* 底部导航占位 */
.safe-area-bottom {
  height: 160rpx;
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
}

.tab-text {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 500;
}

.tab-item.active .tab-text {
  color: #f9d406;
  font-weight: 700;
}

/* Colors */
.container {
  background-color: #1a1814;
  min-height: 100vh;
  color: #fff;
  /* #ifndef H5 */
  overflow-x: hidden;
  /* #endif */
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
}

.header {
  padding: 10px 24px 24px;
  background-color: rgba(26, 24, 20, 0.8);
  position: sticky;
  top: 0;
  z-index: 40;
  backdrop-filter: blur(12px);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.subtitle {
  color: #f2b90d;
  font-size: 12px;
  font-weight: bold;
  letter-spacing: 2px;
  margin-bottom: 4px;
  display: block;
}

.title {
  font-size: 30px;
  font-weight: bold;
  color: #fff;
  display: block;
}

.notification-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(242, 185, 13, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(242, 185, 13, 0.3);
}

.search-bar {
  position: relative;
  background-color: rgba(42, 10, 10, 0.4);
  border-radius: 9999px;
  border: 1px solid rgba(242, 185, 13, 0.2);
  display: flex;
  align-items: center;
  padding: 12px 24px;
}

.search-icon {
  color: rgba(242, 185, 13, 0.6);
  margin-right: 12px;
}

.search-input {
  flex: 1;
  color: #fff;
  font-size: 14px;
}

/* Sections */
.section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}

.view-all {
  color: #f2b90d;
  font-size: 14px;
  font-weight: 500;
}

/* Hot Circles */
.circles-scroll {
  white-space: nowrap;
  width: 100%;
}

.circles-container {
  display: flex;
  padding: 0 24px;
  gap: 20px;
}

.circle-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
}

.circle-avatar-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  padding: 3px;
  border: 1px solid rgba(242, 185, 13, 0.2);
}

.circle-avatar-wrapper.highlight {
  background: linear-gradient(to top right, #f2b90d, #fde047);
  box-shadow: 0 10px 15px -3px rgba(242, 185, 13, 0.1);
  border: none;
}

.circle-avatar-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #1a1814;
  overflow: hidden;
  border: 2px solid #1a1814;
}

.circle-avatar {
  width: 100%;
  height: 100%;
}

.circle-name {
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  margin-top: 8px;
}

.circle-members {
  font-size: 10px;
  color: rgba(242, 185, 13, 0.6);
  font-weight: 500;
}

/* Trending */
.trending-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f2b90d;
}

.trending-text {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.trends-list {
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.glass-panel {
  background: rgba(42, 10, 10, 0.4);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(242, 185, 13, 0.1);
  padding: 20px;
  border-radius: 12px;
  border-left: 4px solid #f2b90d;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.trend-bg-icon {
  position: absolute;
  top: 0;
  right: 0;
  padding: 16px;
  z-index: 0;
}

.trend-content {
  position: relative;
  z-index: 10;
  flex: 1;
}

.trend-title {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 4px;
  display: block;
}

.trend-stats {
  font-size: 12px;
  color: rgba(242, 185, 13, 0.7);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  display: block;
  margin-bottom: 16px;
}

.trend-avatars {
  display: flex;
  margin-top: 16px;
}

.avatar-group {
  display: flex;
  margin-left: 10px;
}

.mini-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #2a0a0a;
  margin-left: -8px;
}

.mini-avatar-count {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(242, 185, 13, 0.2);
  border: 2px solid #2a0a0a;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -8px;
}

.count-text {
  font-size: 8px;
  font-weight: bold;
  color: #fff;
}

.trend-meta {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  align-items: center;
}

.meta-tag {
  background-color: rgba(242, 185, 13, 0.1);
  color: #f2b90d;
  font-size: 10px;
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 4px;
}

.meta-time {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
}

.action-btn {
  position: relative;
  z-index: 10;
  font-size: 12px;
  font-weight: bold;
  padding: 8px 16px;
  border-radius: 8px;
  line-height: 1.5;
  margin: 0;
}

.btn-join {
  background-color: #f2b90d;
  color: #1a1814;
}

.btn-explore {
  background-color: rgba(242, 185, 13, 0.1);
  border: 1px solid rgba(242, 185, 13, 0.2);
  color: #f2b90d;
}

/* FAB */
.fab-btn {
  position: fixed;
  bottom: 160rpx;
  /* 默认靠右 */
  right: 32rpx;
  
  /* #ifdef H5 */
  left: 50%;
  right: auto;
  /* 500px / 2 = 250px. 按钮宽 110rpx ≈ 55px. 边距 16px.
     偏移量 = 250 - 55 - 16 = 179px */
  margin-left: 179px;
  /* #endif */

  width: 110rpx;
  height: 110rpx;
  background-color: #f2b90d;
  color: #1a1814;
  border-radius: 50%;
  box-shadow: 0 12rpx 24rpx -4rpx rgba(242, 185, 13, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.fab-text {
  font-weight: 800;
  font-size: 28rpx;
}
</style>