<template>
  <view class="container">
    <!-- Header Image & Overlay -->
    <view class="header-image-container">
      <image 
        class="header-image" 
        :src="topicInfo.image || topicInfo.cover || 'https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/image-20260219223955621.png'" 
        mode="aspectFill"
      ></image>
      <view class="header-overlay"></view>
      
      <!-- Top Navigation Bar -->
      <view class="nav-bar" :style="{ paddingRight: navbarPaddingRight + 'px' }">
        <view class="icon-btn" @click="goBack">
          <u-icon name="arrow-left" color="#fff" size="24"></u-icon>
        </view>
        <view class="nav-actions">
          <view class="icon-btn" @click="handleSearch">
            <u-icon name="search" color="#fff" size="24"></u-icon>
          </view>
          <view class="icon-btn" @click="handleShare">
            <u-icon name="share" color="#fff" size="24"></u-icon>
          </view>
        </view>
      </view>

      <!-- Topic Title -->
      <view class="topic-info">
        <view class="topic-badge-row">
          <view class="badge">Trending</view>
          <view class="ai-label">
            <u-icon name="star-fill" color="#fff" size="14"></u-icon>
            <text class="ai-text">PitchPulse AI 热门话题</text>
          </view>
        </view>
        <text class="topic-title">#{{ topicTitle }}#</text>
      </view>
    </view>

    <!-- Stats & Description -->
    <view class="content-body">
      <view class="stats-row">
        <view class="stat-card">
          <text class="stat-label">讨论</text>
          <text class="stat-value">{{ formatCount(topicInfo.postCount || 0) }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">阅读</text>
          <text class="stat-value">{{ formatCount(topicInfo.viewCount || 0) }}</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">预测</text>
          <text class="stat-value highlight">89%</text>
        </view>
      </view>

      <view class="description-box" v-if="topicInfo.description" @click="toggleExpand">
        <text class="description-text" :class="{ 'expanded': isExpanded }">
          {{ topicInfo.description }}
        </text>
        <view class="expand-btn">
          <text>{{ isExpanded ? '收起背景' : '查看完整背景' }}</text>
          <u-icon :name="isExpanded ? 'arrow-up' : 'arrow-down'" color="#f20d33" size="12"></u-icon>
        </view>
      </view>
    </view>

    <!-- Feed Tabs -->
    <view class="tabs-header sticky-header">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'hot' }" 
        @click="switchTab('hot')"
      >最热</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'new' }" 
        @click="switchTab('new')"
      >最新</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'media' }" 
        @click="switchTab('media')"
      >媒体</view>
    </view>

    <!-- Discussion Feed -->
    <view class="feed-list">
      <!-- Real User Post Cards -->
      <view class="post-card" v-for="(post, index) in postList" :key="post.id || index" @click="navigateToPost(post)">
        <view class="post-header">
          <view class="user-info">
            <image class="user-avatar" :src="post.userAvatar || '/static/soccer-logo.png'" mode="aspectFill"></image>
            <view class="user-meta">
              <text class="user-name">{{ post.userName || '球迷用户' }}</text>
              <text class="post-time">{{ formatTime(post.createTime) }}</text>
            </view>
          </view>
        </view>

        <text class="post-content">{{ post.content }}</text>
        
        <!-- Post Images -->
        <view class="post-images-grid" v-if="post.images && post.images.length > 0">
          <image 
            class="post-grid-img" 
            v-for="(img, i) in post.images" 
            :key="i" 
            :src="img" 
            mode="aspectFill"
            @click.stop="previewImage(post.images, i)"
          ></image>
        </view>
        
        <view class="post-footer">
          <view class="interaction-item" @click.stop="handleLike(post)">
            <text class="material-icons footer-icon" :style="{ color: post.isLiked ? '#f2b90d' : '#9ca3af' }">
              {{ post.isLiked ? 'favorite' : 'favorite_border' }}
            </text>
            <text :style="{ color: post.isLiked ? '#f2b90d' : '#9ca3af' }">{{ post.likes || 0 }}</text>
          </view>
          <view class="interaction-item">
            <text class="material-icons footer-icon">chat_bubble_outline</text>
            <text>{{ post.comments || 0 }}</text>
          </view>
          <view class="interaction-item">
            <text class="material-icons footer-icon">share</text>
            <text>{{ post.shares || 0 }}</text>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view v-if="postList.length === 0 && !loading" class="empty-state">
        <text>暂无更多话题讨论</text>
      </view>
      
      <view v-if="loading" class="loading-status">
        <text>加载中...</text>
      </view>
    </view>

    <!-- Floating Action Button -->
    <view class="fab-container">
      <button class="fab-btn" @click="handleParticipate">
        <u-icon name="plus" color="#fff" size="20"></u-icon>
        <text class="fab-text">参与讨论</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app';
import { communityApi, fileApi, postApi } from '@/api';

const topicTitle = ref('梅西中国行');
const topicId = ref(null);
const topicInfo = ref({});
const loading = ref(false);
const postList = ref([]);
const navbarPaddingRight = ref(16); // 默认 16px
const isExpanded = ref(false);
const currentTab = ref('hot'); // hot, new, media

const isNavigating = ref(false);

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value;
};

const switchTab = (tab) => {
  if (currentTab.value === tab) return;
  currentTab.value = tab;
  loadPosts(topicTitle.value);
};

const formatCount = (num) => {
  if (!num) return '0';
  if (num > 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  return num;
};

const handleParticipate = () => {
  if (isNavigating.value) return;
  isNavigating.value = true;
  
  // 携带话题信息跳转到发布页
  uni.navigateTo({
    url: `/pages/post/publish?topic=${encodeURIComponent(topicTitle.value)}&topicId=${topicId.value || ''}`,
    complete: () => {
      setTimeout(() => {
        isNavigating.value = false;
      }, 500);
    }
  });
};

const navigateToPost = (post) => {
  if (post && post.id) {
    uni.navigateTo({
      url: `/pages/post/detail?id=${post.id}`
    });
  }
};

const goBack = () => {
  uni.navigateBack();
};

const handleSearch = () => {
  uni.navigateTo({
    url: '/pages/search/search'
  });
};

const handleShare = () => {
  // #ifdef MP-WEIXIN
  // 小程序端唤起分享菜单
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline']
  });
  // #endif

  // #ifndef MP-WEIXIN
  // H5/App端 模拟分享逻辑
  uni.setClipboardData({
    data: window.location.href || `话题：#${topicTitle.value}#`,
    success: () => {
      uni.showToast({
        title: '链接已复制到剪贴板',
        icon: 'success'
      });
    }
  });
  // #endif
};

onLoad((options) => {
  if (options.id) {
    topicId.value = options.id;
    loadTopicDetail(options.id);
  } else if (options.title) {
    topicTitle.value = decodeURIComponent(options.title).replace(/^#|#$/g, '');
    // 如果只有标题没有 ID，先通过标题加载帖子，loadPosts 会尝试从帖子中反向获取 topicId
    loadPosts(topicTitle.value);
  }
  
  topicInfo.value = {
    image: 'https://images.unsplash.com/photo-1522778119026-d647f0596c20?q=80&w=1200&auto=format&fit=crop',
    title: topicTitle.value,
    postCount: 0,
    viewCount: 0,
    description: '加载中...'
  };

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

  // onShow 会处理初次加载，这里不需要重复调用
});

onShareAppMessage(() => {
  return {
    title: `#${topicTitle.value}#`,
    path: `/pages/community/topic-detail?title=${encodeURIComponent(topicTitle.value)}&id=${topicId.value || ''}`,
    imageUrl: topicInfo.value.image || ''
  };
});

onShareTimeline(() => {
  return {
    title: `#${topicTitle.value}#`,
    query: `title=${encodeURIComponent(topicTitle.value)}&id=${topicId.value || ''}`,
    imageUrl: topicInfo.value.image || ''
  };
});

onShow(() => {
  if (topicTitle.value) {
    loadPosts(topicTitle.value);
  }
});

const loadTopicDetail = async (id) => {
  try {
    const data = await communityApi.getTopicDetail(id)
    if (data) {
      topicInfo.value = {
        ...data,
        description: data.description || '暂无话题介绍',
        viewCount: data.viewCount || 0,
        postCount: data.postCount || 0
      }
    }
  } catch (error) {
    console.error('获取话题详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

const loadPosts = async (title) => {
  try {
    loading.value = true
    const params = { 
      page: 1, 
      size: 20,
      topic: title 
    }
    
    // Add sorting/filter based on currentTab
    if (currentTab.value === 'hot') {
      params.sort = 'likes' // Assume backend supports sorting by likes for 'hot'
    } else if (currentTab.value === 'new') {
      params.sort = 'create_time'
    } else if (currentTab.value === 'media') {
      params.hasImage = true // Assume backend supports filtering by images
    }

    const res = await communityApi.getTopicPosts(title, params)
    if (res) {
      const data = res.posts || res; // 兼容旧版或新版返回格式
      const records = data.records || []
      
      // 自动获取并设置话题信息
      if (res.topic) {
        topicId.value = res.topic.id;
        topicInfo.value = {
          ...res.topic,
          description: res.topic.description || '暂无话题介绍',
          viewCount: res.topic.viewCount || 0,
          postCount: res.topic.postCount || 0
        };
      } else if (!topicId.value && records.length > 0) {
        // 后退方案：从帖子列表中找 ID
        const firstPostWithTopic = records.find(p => p.topicId);
        if (firstPostWithTopic) {
          topicId.value = firstPostWithTopic.topicId;
          if (!topicInfo.value || !topicInfo.value.description || topicInfo.value.description === '加载中...') {
            loadTopicDetail(topicId.value);
          }
        }
      }

      postList.value = records.map(post => {
        let postImages = [];
        try {
          if (post.images) {
            if (Array.isArray(post.images)) {
                postImages = post.images;
            } else if (typeof post.images === 'string') {
                postImages = JSON.parse(post.images);
            }
          }
        } catch (e) {
          console.error('解析图片失败:', e);
        }

        return {
          id: post.id,
          userId: post.userId,
          title: post.title,
          content: post.content,
          images: postImages.map(img => fileApi.getFileUrl(img)),
          likes: post.likes || 0,
          isLiked: post.isLiked || false,
          comments: post.commentCount || 0,
          shares: 0,
          userName: post.userName || '未知用户',
          userAvatar: post.userAvatar ? fileApi.getFileUrl(post.userAvatar) : '/static/default-avatar.png',
          createTime: post.createdAt || post.createTime
        }
      })
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleLike = async (post) => {
  try {
    const res = await postApi.like({
      targetId: post.id,
      targetType: 1
    });
    
    if (res && res.liked !== undefined) {
      post.isLiked = res.liked;
      if (res.liked) {
        post.likes++;
        uni.showToast({ title: '已点赞', icon: 'none' });
      } else {
        post.likes--;
        uni.showToast({ title: '已取消', icon: 'none' });
      }
    }
  } catch (error) {
    console.error('Like failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const getRandomAvatar = () => {
  const avatars = [
    '/static/soccer-logo.png',
    '/static/teams/generic_stadium.jpg',
    '/static/default-team.png'
  ];
  return avatars[Math.floor(Math.random() * avatars.length)];
};

const getRandomName = () => {
  const names = ['伯纳乌之王', '梅西死忠', 'C罗粉丝', '曼联红魔', '阿森纳枪手', '利物浦KOP', '拜仁慕尼黑', '尤文蒂尼'];
  return names[Math.floor(Math.random() * names.length)] + Math.floor(Math.random() * 1000);
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return date.toLocaleDateString();
};

const previewImage = (images, current) => {
  uni.previewImage({
    urls: images,
    current: current
  });
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #12110a;
  color: #fff;
  padding-bottom: 100rpx;
}

/* Header */
.header-image-container {
  position: relative;
  height: 480rpx;
  width: 100%;
}

.header-image {
  width: 100%;
  height: 100%;
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3) 0%, rgba(18,17,10,1) 100%);
}

.nav-bar {
  position: absolute;
  top: 40rpx;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx;
  z-index: 10;
  
  /* #ifdef MP-WEIXIN */
  padding-top: var(--status-bar-height);
  /* #endif */
}

.nav-actions {
  display: flex;
  align-items: center;
}

.icon-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(255,255,255,0.1);
  border-radius: 50%;
  margin-left: 20rpx;
}

.topic-info {
  position: absolute;
  bottom: 40rpx;
  left: 32rpx;
  right: 32rpx;
}

.topic-badge-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.badge {
  background-color: #f20d33;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  margin-right: 16rpx;
  font-weight: bold;
}

.ai-label {
  display: flex;
  align-items: center;
  background-color: rgba(255,255,255,0.1);
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}

.ai-text {
  color: #fff;
  font-size: 20rpx;
  margin-left: 8rpx;
}

.topic-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

/* Content Body */
.content-body {
  padding: 0 32rpx;
  margin-top: -20rpx;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 32rpx;
}

.stat-card {
  flex: 1;
  background-color: #1c1a11;
  padding: 24rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 16rpx;
  
  &:last-child {
    margin-right: 0;
  }
}

.stat-label {
  color: #9ca3af;
  font-size: 24rpx;
  margin-bottom: 8rpx;
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  
  &.highlight {
    color: #f20d33;
  }
}

.description-box {
  background-color: #1c1a11;
  padding: 24rpx;
  border-radius: 16rpx;
  margin-bottom: 40rpx;
}

.description-text {
  font-size: 26rpx;
  color: #d1d5db;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  
  &.expanded {
    -webkit-line-clamp: unset;
  }
}

.expand-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #f20d33;
}

/* Tabs */
.sticky-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #12110a;
}

.tabs-header {
  display: flex;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #2d2a1d;
  margin-bottom: 32rpx;
}

.tab-item {
  padding: 24rpx 0;
  margin-right: 48rpx;
  font-size: 28rpx;
  color: #9ca3af;
  position: relative;
  
  &.active {
    color: #f2b90d;
    font-weight: bold;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 4rpx;
      background-color: #f2b90d;
    }
  }
}

/* Feed List */
.feed-list {
  padding: 0 32rpx;
}

/* Post Card Styles from circle-detail.vue */
.post-card {
  background-color: #1c1a11;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #2d2a1d;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
}

.post-time {
  font-size: 12px;
  color: #6b7280;
}

.post-content {
  font-size: 14px;
  color: #d1d5db;
  line-height: 1.6;
  display: block;
  margin-bottom: 16px;
}

.post-images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 12px;
}

.post-grid-img {
  width: calc(33.33% - 3px);
  height: 220rpx;
  border-radius: 4px;
  background-color: #2d2a1d;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 24px;
}

.interaction-item {
  display: flex;
  align-items: center;
  color: #9ca3af;
  font-size: 12px;
}

.footer-icon {
  font-size: 16px;
  margin-right: 6px;
}

.loading-status, .empty-state {
  text-align: center;
  padding: 40rpx;
  color: #6b7280;
  font-size: 26rpx;
}

/* FAB */
.fab-container {
  position: fixed;
  bottom: 60rpx;
  left: 0;
  width: 100%;
  display: flex; 
  justify-content: center;
  pointer-events: none;
  z-index: 100;
}

.fab-btn {
  background-color: #f2b90d;
  color: #12110a;
  display: flex;
  align-items: center;
  padding: 0 48rpx;
  height: 96rpx;
  border-radius: 48rpx;
  box-shadow: 0 8rpx 32rpx rgba(242, 185, 13, 0.4);
  pointer-events: auto;
  border: none;
}

.fab-text {
  font-weight: bold;
  font-size: 28rpx;
  margin-left: 12rpx;
}
</style>