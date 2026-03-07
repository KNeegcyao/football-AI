<template>
  <view class="page-container min-h-screen font-display" :class="themeClass">
    <!-- Top Navigation Header -->
    <view class="header-sticky" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="header-left" @click="goBack">
          <text class="material-icons text-2xl">arrow_back</text>
        </view>
        <view class="header-center">
          <text class="text-lg font-bold">{{ userInfo.nickname }}的战术板</text>
        </view>
        <view class="header-right">
          <!-- 占位，保持中间标题居中 -->
          <view style="width: 48rpx;"></view>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main-content" @scrolltolower="loadMore">
      <!-- Profile Header Section -->
      <view class="profile-header">
        <view class="cover-image-container">
          <image v-if="userInfo.cover" class="cover-image" :src="userInfo.cover" mode="aspectFill"></image>
          <view v-else class="cover-placeholder"></view>
        </view>
        
        <view class="profile-info-card">
          <view class="avatar-section">
            <view class="progress-ring-box">
              <svg class="progress-ring" viewBox="0 0 112 112">
                <circle class="ring-bg" cx="56" cy="56" r="50"></circle>
                <circle class="ring-active" cx="56" cy="56" r="50" 
                  :style="{ strokeDashoffset: 314.159 * (1 - progress), stroke: levelColor }"></circle>
              </svg>
              
              <!-- Lv.5 流光背景层 (必须在最底层) -->
              <view v-if="Number(userInfo.level) === 5" class="frame-glow-lv5-bg"></view>

              <view class="avatar-wrapper border-2 border-white/20">
                <image class="avatar-img" :src="userInfo.avatar" mode="aspectFill"></image>
              </view>

              <!-- 头像框装饰层 -->
              <view class="avatar-frame" :class="'frame-lv' + userInfo.level">
                <!-- Lv.2 足球装饰 (左上+左下) -->
                <template v-if="Number(userInfo.level) === 2">
                  <text class="material-icons frame-deco deco-tl">sports_soccer</text>
                  <text class="material-icons frame-deco deco-bl">sports_soccer</text>
                </template>
                
                <!-- Lv.4 皇冠装饰 (正上方) -->
                <text v-if="Number(userInfo.level) === 4" class="material-icons top-crown">workspace_premium</text>
                
                <!-- Lv.5 皇冠装饰 (正上方) -->
                <text v-if="Number(userInfo.level) === 5" class="material-icons top-crown">workspace_premium</text>
              </view>
              
              <view class="level-badge" :class="'lv-badge-' + userInfo.level">
                <text class="material-icons badge-icon">workspace_premium</text>
                <text class="badge-text">LV.{{ userInfo.level }}</text>
              </view>
            </view>
          </view>
          
          <view class="identity-info">
            <view class="name-row">
              <text class="nickname">{{ userInfo.nickname }}</text>
              <text class="material-icons text-white/90 text-xl">verified</text>
            </view>
            
            <!-- 层次感称号标签 -->
            <view class="title-container" v-if="userInfo.levelTitle">
              <text class="primary-title">{{ userInfo.levelTitle.split('|')[0] }}</text>
              <text class="divider">|</text>
              <text class="secondary-title">{{ userInfo.levelTitle.split('|')[1] }}</text>
            </view>
            <text v-else class="user-handle">{{ userInfo.nickname }} | 绿茵观察员</text>
            
            <text class="user-bio">{{ userInfo.bio || '此人没有留下任何足迹...' }}</text>
          </view>
        </view>
      </view>

      <!-- Action Buttons Row -->
      <view class="action-buttons-row">
        <button class="btn-primary" :class="{ 'is-following': isFollowing }" @click="handleFollow">
          {{ isFollowing ? '已关注' : '关注' }}
        </button>
        <button class="btn-secondary" @click="handleMessage">
          <text class="material-icons" style="font-size: 32rpx; margin-right: 8rpx;">chat_bubble_outline</text>
          <text>私信</text>
        </button>
      </view>

      <!-- Stats Section -->
      <view class="stats-container">
        <view class="stat-item" @click="goToFollow('following')">
          <text class="stat-value">{{ formatStats(userInfo.stats.following) }}</text>
          <text class="stat-label">关注</text>
        </view>
        <view class="stat-item" @click="goToFollow('followers')">
          <text class="stat-value">{{ formatStats(userInfo.stats.followers) }}</text>
          <text class="stat-label">粉丝</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ formatStats(userInfo.stats.likesReceived) }}</text>
          <text class="stat-label">获赞</text>
        </view>
      </view>

      <!-- Tabs Navigation -->
      <view class="tabs-sticky">
        <view class="tabs-content">
          <view v-for="(tab, index) in profileTabs" :key="index" 
                class="tab-item" 
                :class="{ active: currentProfileTab === index }"
                @click="currentProfileTab = index">
            <text class="tab-text">{{ tab.name }} ({{ tab.count }})</text>
            <view class="tab-indicator" v-if="currentProfileTab === index"></view>
          </view>
        </view>
      </view>

      <!-- Content Feed -->
      <view class="content-feed">
        <!-- 动态列表 (Tab 0) -->
        <view v-if="currentProfileTab === 0">
          <view v-for="(post, index) in posts" :key="post.id" class="post-card" @click="goToPostDetail(post.id)">
            <view class="post-content-row">
              <view class="post-text-side">
                <text class="post-title">{{ post.title }}</text>
                <text class="post-excerpt">{{ post.content }}</text>
                <view class="post-meta">
                  <view class="meta-item">
                    <text class="material-icons text-sm">favorite</text>
                    <text>{{ formatStats(post.likes) }}</text>
                  </view>
                  <view class="meta-item">
                    <text class="material-icons text-sm">chat_bubble</text>
                    <text>{{ post.commentCount }}</text>
                  </view>
                  <text class="post-time">{{ formatTime(post.createTime) }}</text>
                </view>
              </view>
              <image v-if="post.image" class="post-image" :src="post.image" mode="aspectFill"></image>
            </view>
          </view>
          
          <u-loadmore :status="loadStatus" color="#64748b" v-if="posts.length > 0" />
          <view class="empty-state" v-if="posts.length === 0 && !loading">
            <text class="material-icons empty-icon">description</text>
            <text>暂无动态</text>
          </view>
        </view>
        
        <!-- 相册 (Tab 2) -->
        <view v-else-if="currentProfileTab === 2" class="empty-state">
          <text class="material-icons empty-icon">photo_library</text>
          <text>暂无相册</text>
        </view>
      </view>
      
      <view class="footer-info">
        <text class="version-text">PitchPulse v2.4.0 (Build 503)</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useThemeStore } from '@/store/theme'
import { userApi, fileApi, postApi, favoriteApi, playerApi, relationshipApi } from '@/api'
import { BASE_URL } from '@/utils/request'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight
const currentProfileTab = ref(0)
const profileTabs = ref([
  { name: '动态', count: 0 },
  { name: '相册', count: 0 }
])

const userInfo = ref({
  id: '',
  nickname: '加载中...',
  username: '',
  avatar: '/static/soccer-logo.png',
  cover: '',
  level: 1,
  levelTitle: '新秀观察员 | 足球爱好者',
  bio: '',
  description: '',
  stats: {
    following: 0,
    followers: 0,
    likesReceived: 0
  }
})

const isFollowing = ref(false)
const targetUserId = ref('')

// 等级体系称号映射
const levelTitles = {
  1: '新秀观察员 | 足球爱好者',
  2: '战术研究员 | 比赛分析者',
  3: '资深分析师 | 绿茵智囊',
  4: '战术大师 | 绿茵战略家',
  5: '战术宗师 | 足球哲学家'
}

const levelColor = computed(() => {
  const level = userInfo.value.level
  if (level === 1) return '#4B5563' // 哑光石墨
  if (level === 2) return '#B45309' // 古铜
  if (level === 3) return '#CBD5E1' // 银白
  if (level === 4) return '#FACC15' // 亮金
  if (level === 5) return '#f2b90d' // 黑金
  return '#4B5563'
})

const progress = computed(() => {
  if (!userInfo.value.experience) return 0
  const level = userInfo.value.level || 1
  const levelExpMap = {
    1: 100,
    2: 200,
    3: 300,
    4: 400,
    5: 99999
  }
  const nextExp = levelExpMap[level] || 100
  return Math.min(1, userInfo.value.experience / nextExp)
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = (now.getTime() - date.getTime()) / 1000
  
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 2592000) return Math.floor(diff / 86400) + '天前'
  
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const posts = ref([])
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const loadStatus = ref('loadmore')

onLoad((options) => {
  if (options.userId) {
    targetUserId.value = options.userId
    loadUserProfile(options.userId)
    checkFollowStatus(options.userId)
  }
})

const getAvatarUrl = (avatar, type = 'avatar') => {
  if (!avatar) {
    return type === 'avatar' ? '/static/soccer-logo.png' : ''
  }
  if (avatar.includes('/uploads/')) {
    const relativePath = avatar.substring(avatar.indexOf('/uploads/'))
    return fileApi.getFileUrl(relativePath)
  }
  if (avatar.startsWith('http')) {
    return avatar
  }
  return fileApi.getFileUrl(avatar)
}

const loadUserProfile = async (userId) => {
  try {
    const profileRes = await userApi.getProfile(userId)
    if (profileRes) {
      const avatarUrl = getAvatarUrl(profileRes.avatar, 'avatar')
      const coverUrlFromRes = getAvatarUrl(profileRes.cover, 'cover')
      
      const level = profileRes.level || 1
      userInfo.value = {
        ...userInfo.value,
        id: profileRes.id,
        nickname: profileRes.nickname || profileRes.username,
        username: '@' + profileRes.username,
        avatar: avatarUrl,
        cover: coverUrlFromRes,
        level: level,
        levelTitle: levelTitles[level] || '新秀观察员 | 足球爱好者',
        bio: profileRes.bio || '此人没有留下任何足迹...',
        experience: profileRes.experience || 0
      }
      
      page.value = 1
      posts.value = []
      loadStatus.value = 'loadmore'
      loadPosts()
    }

    const statsRes = await userApi.getStats(userId)
    if (statsRes) {
      userInfo.value.stats = {
        following: statsRes.followingCount || 0,
        followers: statsRes.followerCount || 0,
        likesReceived: statsRes.likeReceivedCount || 0
      }
      profileTabs.value[0].count = statsRes.postCount || 0
    }
  } catch (e) {
    console.error('加载用户信息失败:', e)
  }
}

const checkFollowStatus = async (userId) => {
  try {
    console.log('Checking follow status for userId:', userId)
    const res = await postApi.checkFollowStatus(userId)
    console.log('Follow status result:', res)
    isFollowing.value = !!res
  } catch (e) {
    console.error('检查关注状态失败:', e)
  }
}

const loadPosts = async () => {
  if (loading.value || loadStatus.value === 'nomore') return
  
  loading.value = true
  loadStatus.value = 'loading'
  
  try {
    const res = await postApi.getList({
      page: page.value,
      size: pageSize.value,
      userId: targetUserId.value
    })
    
    if (res && res.records) {
      const newPosts = res.records.map(item => ({
        id: item.id,
        title: item.title,
        content: item.content,
        image: item.images && item.images.length > 0 ? fileApi.getFileUrl(item.images[0]) : '',
        createTime: item.createdAt,
        likes: item.likes || 0,
        commentCount: item.commentCount || 0
      }))
      
      if (page.value === 1) {
        posts.value = newPosts
      } else {
        posts.value = [...posts.value, ...newPosts]
      }
      
      if (newPosts.length < pageSize.value) {
        loadStatus.value = 'nomore'
      } else {
        loadStatus.value = 'loadmore'
        page.value++
      }
    }
  } catch (e) {
    console.error('加载动态失败:', e)
    loadStatus.value = 'loadmore'
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (currentProfileTab.value === 0) {
    loadPosts()
  }
}

const handleFollow = async () => {
  try {
    if (isFollowing.value) {
      await relationshipApi.unfollow(targetUserId.value)
      isFollowing.value = false
      uni.showToast({ title: '已取消关注', icon: 'none' })
      if (userInfo.value.stats) {
        userInfo.value.stats.followers = Math.max(0, (userInfo.value.stats.followers || 0) - 1)
      }
    } else {
      await relationshipApi.follow(targetUserId.value)
      isFollowing.value = true
      uni.showToast({ title: '关注成功', icon: 'success' })
      if (userInfo.value.stats) {
        userInfo.value.stats.followers = (userInfo.value.stats.followers || 0) + 1
      }
    }
  } catch (e) {
    console.error('关注操作失败:', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const handleMessage = () => {
  uni.navigateTo({
    url: `/pages/message/chat?otherUserId=${targetUserId.value}&otherNickname=${userInfo.value.nickname}&otherAvatar=${userInfo.value.avatar}`
  })
}

const goToFollow = (type) => {
  const tab = type === 'following' ? 0 : 1
  uni.navigateTo({ 
    url: `/pages/my/follow?tab=${tab}&userId=${targetUserId.value}` 
  })
}

const goToPostDetail = (id) => {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

const goBack = () => {
  uni.navigateBack()
}

const formatStats = (num) => {
  if (num >= 10000) return (num / 1000).toFixed(1) + 'K'
  return num || 0
}
</script>

<style lang="scss" scoped>
.page-container {
  display: flex;
  flex-direction: column;
  background-color: var(--bg-main);
}

.header-sticky {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: var(--nav-bar-bg);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid var(--border-main);
}

.header-content {
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40rpx;
  color: var(--text-main);
}

.main-content {
  flex: 1;
  height: 0;
}

  .profile-header {
    position: relative;
    overflow: hidden;

    .cover-image-container {
      height: 520rpx;
      position: relative;
      background-color: var(--bg-main);
      
      &::after {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 60%;
        background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, transparent 100%);
        z-index: 2;
      }

      .cover-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .cover-placeholder {
        width: 100%;
        height: 100%;
        background: linear-gradient(45deg, var(--bg-main), var(--bg-secondary));
      }
    }

    .profile-info-card {
      position: absolute;
      bottom: 40rpx;
      left: 0;
      right: 0;
      z-index: 10;
      background: transparent !important;
      box-shadow: none !important;
      padding: 0 40rpx;
      display: flex;
      align-items: flex-end;
      gap: 30rpx;

      .avatar-section {
        flex-shrink: 0;
        display: flex;
        justify-content: flex-start;
        align-items: flex-end;
        width: 224rpx;
        height: 224rpx;
      }

      .identity-info {
        flex: 1;
        margin-top: 0;
        padding-bottom: 10rpx;

        .name-row {
          display: flex;
          align-items: center;
          gap: 16rpx;
          margin-bottom: 12rpx;
          
          .nickname {
            font-size: 44rpx;
            font-weight: 800;
            color: #ffffff !important;
            text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.3);
          }
        }

        .title-container {
          display: inline-flex;
          align-items: center;
          background: rgba(255, 255, 255, 0.15);
          backdrop-filter: blur(4rpx);
          padding: 6rpx 16rpx;
          border-radius: 8rpx;
          margin-top: 12rpx;
          border: 1rpx solid rgba(255, 255, 255, 0.2);
          gap: 12rpx;

          .primary-title {
            font-size: 24rpx;
            font-weight: 600;
            color: #f2b90d;
          }

          .divider {
            color: rgba(255, 255, 255, 0.5);
            margin: 0 10rpx;
            font-size: 20rpx;
          }

          .secondary-title {
            font-size: 22rpx;
            color: rgba(255, 255, 255, 0.9);
          }
        }

        .user-bio {
          font-size: 26rpx;
          color: rgba(255, 255, 255, 0.8) !important;
          margin-top: 10rpx;
          line-height: 1.4;
          display: block;
          text-shadow: 0 1rpx 4rpx rgba(0,0,0,0.3);
        }
      }
    }
  }

  .action-buttons-row {
    display: flex;
    gap: 20rpx;
    margin: 30rpx 40rpx 0;
    
    button {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      border-radius: 44rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30rpx;
      font-weight: 700;
      transition: all 0.3s;
      
      &::after { border: none; }
      
      &:active { transform: scale(0.97); }
    }
    
    .btn-primary {
      background: #f2b90d;
      color: #000;
      box-shadow: 0 8rpx 20rpx rgba(242, 185, 13, 0.3);
      
      &.is-following {
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(4rpx);
        color: #ffffff;
        border: 1rpx solid rgba(255, 255, 255, 0.2);
        box-shadow: none;
      }
    }
    
    .btn-secondary {
      background: rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(4rpx);
      color: #fff;
      border: 1rpx solid rgba(255, 255, 255, 0.2);
    }
  }

  .progress-ring-box {
    position: relative;
    width: 224rpx;
    height: 224rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: visible;
  }

  .progress-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);
    z-index: 10;
    
    circle {
      fill: transparent;
      stroke-width: 2;
      stroke-linecap: round;
    }
    
    .ring-bg {
      stroke: rgba(255, 255, 255, 0.1);
    }
    
    .ring-active {
      transition: stroke-dashoffset 0.35s;
      stroke-dasharray: 314.159;
    }
  }

  .avatar-wrapper {
    position: absolute;
    width: 200rpx;
    height: 200rpx;
    border-radius: 50%;
    overflow: hidden;
    z-index: 20;
    background: #1e293b;
    border: 4rpx solid #1e293b;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;

    .avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      display: block;
      background-color: #2d3748;
    }
  }

  /* 头像框通用样式 */
  .avatar-frame {
    position: absolute;
    inset: -10rpx;
    border-radius: 50%;
    z-index: 30;
    pointer-events: none;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;

    /* Lv.1: 哑光石墨 */
    &.frame-lv1 {
      border: 2rpx solid rgba(156, 163, 175, 0.4) !important;
    }

    /* Lv.2: 青铜足球 */
    &.frame-lv2 {
      border: 4rpx dashed #B45309 !important;
      
      .frame-deco {
        position: absolute;
        font-size: 32rpx;
        color: #B45309;
        text-shadow: 0 0 6rpx rgba(0, 0, 0, 0.5);
        z-index: 40;
        
        &.deco-tl { top: 0; left: 0; transform: translate(-20%, -20%); }
        &.deco-bl { bottom: 40rpx; left: -10rpx; }
      }
    }

    /* Lv.3: 白金银色 */
    &.frame-lv3 {
      border: 5rpx double #CBD5E1 !important;
      background: none;
      box-shadow: 0 0 15rpx rgba(203, 213, 225, 0.5);
      
      &::after {
        content: "";
        position: absolute;
        inset: 4rpx;
        border-radius: 50%;
        border: 1rpx dashed rgba(255, 255, 255, 0.3);
      }
    }

    /* Lv.4: 黄金至尊 (呼吸灯) */
    &.frame-lv4 {
      border: 6rpx solid #FACC15 !important;
      animation: gold-pulse 2s infinite ease-in-out;
      
      .top-crown {
        position: absolute;
        top: -30rpx;
        left: 50%;
        transform: translateX(-50%);
        font-size: 44rpx;
        color: #FACC15;
        text-shadow: 0 0 10rpx rgba(250, 204, 21, 0.5);
        z-index: 40;
      }
    }

    /* Lv.5: 黑金至尊 (旋转流光) */
    &.frame-lv5 {
      background: none !important;
      border: none !important;
      
      .top-crown {
        position: absolute;
        top: -34rpx;
        left: 50%;
        transform: translateX(-50%);
        font-size: 56rpx;
        color: #f2b90d;
        text-shadow: 0 0 20rpx rgba(242, 185, 13, 0.8), 0 0 40rpx rgba(0, 0, 0, 0.5);
        z-index: 40;
      }
    }
  }

  .frame-glow-lv5-bg {
    position: absolute;
    inset: -8rpx;
    border-radius: 50%;
    /* 黑金交替渐变 */
    background: conic-gradient(
      from 0deg, 
      #000000 0%, 
      #f2b90d 25%, 
      #000000 50%, 
      #f2b90d 75%, 
      #000000 100%
    );
    animation: rotate 1.5s linear infinite;
    z-index: 4;
    box-shadow: 0 0 25rpx rgba(242, 185, 13, 0.4);
  }

  .level-badge {
    position: absolute;
    bottom: -4rpx;
    left: 50%;
    transform: translateX(-50%);
    background: linear-gradient(135deg, #FDE047 0%, #EAB308 100%);
    color: #000000;
    padding: 4rpx 16rpx;
    border-radius: 999rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.4);
    z-index: 40;
    white-space: nowrap;
    border: 2rpx solid #1e293b;

    .badge-icon {
      font-size: 22rpx !important;
      font-weight: bold;
    }

    .badge-text {
      font-size: 20rpx;
      font-weight: 800;
      line-height: 1;
    }

    &.lv-badge-5 {
      background: linear-gradient(90deg, #f2b90d, #fff);
      box-shadow: 0 0 15rpx rgba(242, 185, 13, 0.6);
    }
  }

  .stats-container {
    display: flex;
    justify-content: space-around;
    padding: 30rpx;
    margin: 30rpx 40rpx 0;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(15px);
    border: 1rpx solid rgba(255, 255, 255, 0.1);
    border-radius: 24rpx;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      transition: all 0.3s;
      
      .stat-value {
        font-size: 36rpx;
        font-weight: 800;
        color: var(--text-main);
      }
      
      .stat-label {
        font-size: 24rpx;
        color: var(--text-secondary);
        margin-top: 4rpx;
      }

      &:active {
        transform: scale(0.92);
      }
    }
  }

  /* 针对浅色模式的覆盖样式 */
  .theme-light {
    .stats-container {
      background: rgba(0, 0, 0, 0.04);
      border-color: rgba(0, 0, 0, 0.08);
      
      .stat-item {
        .stat-value {
          color: #1a1a1a;
        }
        .stat-label {
          color: #666666;
        }
      }
    }

    .action-buttons-row {
      .btn-primary.is-following {
        background: rgba(0, 0, 0, 0.05);
        color: #333333;
        border: 1rpx solid rgba(0, 0, 0, 0.1);
      }
      
      .btn-secondary {
        background: rgba(0, 0, 0, 0.05);
        color: #333333;
        border: 1rpx solid rgba(0, 0, 0, 0.1);
      }
    }
  }

  @keyframes rotate {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  @keyframes gold-pulse {
    0% { transform: scale(1); border-color: #FACC15; }
    50% { transform: scale(1.02); border-color: #FFF176; box-shadow: 0 0 20rpx #FACC15; }
    100% { transform: scale(1); border-color: #FACC15; }
  }

.tabs-sticky {
  position: sticky;
  top: 120rpx;
  z-index: 90;
  background-color: var(--nav-bar-bg);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid var(--border-main);
}

.tabs-content {
  display: flex;
  height: 100rpx;
  padding: 0 40rpx;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  margin-right: 60rpx;
  
  .tab-text {
    font-size: 30rpx;
    color: var(--text-secondary);
    font-weight: 500;
    transition: all 0.3s;
  }
  
  &.active {
    .tab-text {
      color: var(--text-main);
      font-weight: 700;
    }
  }
  
  .tab-indicator {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 6rpx;
    background: #f2b90d;
    border-radius: 3rpx;
    box-shadow: 0 0 10rpx rgba(242, 185, 13, 0.4);
  }
}

.content-feed {
  padding: 20rpx 0;
}

.post-card {
  background: var(--card-bg);
  margin: 0 20rpx 20rpx;
  padding: 30rpx;
  border-radius: 24rpx;
  border: 1rpx solid var(--border-main);
}

.post-content-row {
  display: flex;
  gap: 20rpx;
}

.post-text-side {
  flex: 1;
}

.post-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.post-excerpt {
  font-size: 28rpx;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 30rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
  
  .meta-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }
  
  .post-time {
    margin-left: auto;
  }
}

.post-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  background-color: var(--bg-main);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  color: var(--text-secondary);
  
  .empty-icon {
    font-size: 100rpx;
    margin-bottom: 20rpx;
    opacity: 0.3;
  }
  
  text {
    font-size: 28rpx;
  }
}

.footer-info {
  padding: 60rpx 0 100rpx;
  display: flex;
  justify-content: center;
  
  .version-text {
    font-size: 22rpx;
    color: var(--text-secondary);
    opacity: 0.5;
  }
}

/* 浅色模式适配 */
.theme-light {
  background-color: var(--bg-main);
  
  .header-sticky {
    background-color: var(--bg-main);
    border-bottom: 1px solid var(--border-main);
    .header-center text, .header-left text {
      color: var(--text-main);
    }
  }
  
  .profile-info-card {
    background-color: #FFFFFF;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    .nickname {
      color: var(--text-main);
    }
    .user-bio {
      color: var(--text-secondary);
    }
  }
  
  .stats-container {
    background-color: #FFFFFF;
    .stat-value {
      color: var(--text-main);
    }
    .stat-label {
      color: var(--text-secondary);
    }
  }
  
  .tabs-wrapper {
    background-color: var(--bg-main);
    border-bottom: 1px solid var(--border-main);
    .tab-text {
      color: var(--text-secondary);
      &.active {
        color: var(--accent-color);
      }
    }
  }
  
  .post-card {
    background-color: #FFFFFF;
    border: 1px solid #F3F4F6;
    .post-title {
      color: var(--text-main);
    }
  }
}
</style>
