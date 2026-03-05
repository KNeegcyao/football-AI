<template>
  <view class="page-container bg-background-dark text-slate-100 min-h-screen font-display">
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
          <view class="cover-overlay"></view>
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

              <view class="avatar-wrapper">
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
              <text class="material-icons text-primary text-xl">verified</text>
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

          <view class="action-buttons">
            <button class="btn-primary" :class="{ 'is-following': isFollowing }" @click="handleFollow">
              {{ isFollowing ? '已关注' : '关注' }}
            </button>
            <button class="btn-secondary" @click="handleMessage">私信</button>
          </view>
        </view>
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
import { userApi, fileApi, postApi, favoriteApi, playerApi, relationshipApi } from '@/api'
import { BASE_URL } from '@/utils/request'

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
  background-color: #12110a;
}

.header-sticky {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(18, 17, 10, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
}

.header-content {
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40rpx;
}

.main-content {
  flex: 1;
  height: 0;
}

.profile-header {
  position: relative;
  padding-bottom: 20rpx;
  background: #0A0A0A;
}

.cover-image-container {
  height: 340rpx;
  position: relative;
  background-color: #1a1a1a;
  
  .cover-image {
    width: 100%;
    height: 100%;
  }
  
  .cover-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(10,10,10,1));
    z-index: 1;
  }
  
  .cover-placeholder {
    width: 100%;
    height: 100%;
    background: linear-gradient(45deg, #1a1a1a, #2a2a2a);
  }
}

.profile-info-card {
  margin-top: -80rpx;
  padding: 0 40rpx;
  position: relative;
  z-index: 10;
}

.avatar-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.progress-ring-box {
  position: relative;
  width: 170rpx;
  height: 170rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  
  .ring-bg {
    fill: none;
    stroke: rgba(255, 255, 255, 0.1);
    stroke-width: 4;
  }
  
  .ring-active {
    fill: none;
    stroke-width: 4;
    stroke-linecap: round;
    transition: stroke-dashoffset 0.6s ease;
    stroke-dasharray: 314.159;
  }
}

.avatar-wrapper {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 4rpx solid #0A0A0A;
  z-index: 2;
  
  .avatar-img {
    width: 100%;
    height: 100%;
  }
}

.avatar-frame {
  position: absolute;
  inset: -10rpx;
  pointer-events: none;
  z-index: 3;
  
  &.frame-lv2 {
    .frame-deco {
      position: absolute;
      font-size: 32rpx;
      color: #B45309;
      text-shadow: 0 0 10rpx rgba(180, 83, 9, 0.5);
    }
    .deco-tl { top: 10rpx; left: 10rpx; }
    .deco-bl { bottom: 10rpx; left: 10rpx; }
  }
  
  &.frame-lv4, &.frame-lv5 {
    border: 4rpx solid #FACC15;
    border-radius: 50%;
    box-shadow: 0 0 20rpx rgba(250, 204, 21, 0.3);
    
    .top-crown {
      position: absolute;
      top: -25rpx;
      left: 50%;
      transform: translateX(-50%);
      font-size: 40rpx;
      color: #FACC15;
      text-shadow: 0 0 15rpx rgba(250, 204, 21, 0.6);
    }
  }
  
  &.frame-lv5 {
    border-color: #f2b90d;
    animation: border-glow 2s infinite;
  }
}

@keyframes border-glow {
  0%, 100% { box-shadow: 0 0 15rpx rgba(242, 185, 13, 0.3); }
  50% { box-shadow: 0 0 30rpx rgba(242, 185, 13, 0.6); }
}

.frame-glow-lv5-bg {
  position: absolute;
  inset: -15rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(242, 185, 13, 0.15) 0%, transparent 70%);
  animation: pulse-glow 3s infinite;
  z-index: 1;
}

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.level-badge {
  position: absolute;
  bottom: -5rpx;
  right: -5rpx;
  display: flex;
  align-items: center;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  background: #4B5563;
  z-index: 10;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.3);
  
  .badge-icon {
    font-size: 24rpx;
    margin-right: 4rpx;
  }
  
  .badge-text {
    font-size: 20rpx;
    font-weight: bold;
  }
  
  &.lv-badge-2 { background: linear-gradient(135deg, #B45309, #78350F); }
  &.lv-badge-3 { background: linear-gradient(135deg, #94A3B8, #475569); }
  &.lv-badge-4 { background: linear-gradient(135deg, #FACC15, #CA8A04); }
  &.lv-badge-5 { background: linear-gradient(135deg, #f2b90d, #b48a0a); }
}

.identity-info {
  margin-top: 24rpx;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  
  .nickname {
    font-size: 44rpx;
    font-weight: 800;
    color: #fff;
  }
}

.title-container {
  display: inline-flex;
  align-items: center;
  background: rgba(242, 185, 13, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  margin-top: 12rpx;
  border: 1rpx solid rgba(242, 185, 13, 0.2);
  
  .primary-title {
    font-size: 24rpx;
    color: #f2b90d;
    font-weight: 600;
  }
  
  .divider {
    color: rgba(242, 185, 13, 0.3);
    margin: 0 10rpx;
    font-size: 20rpx;
  }
  
  .secondary-title {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.6);
  }
}

.user-handle {
  font-size: 26rpx;
  color: #f2b90d;
  margin-top: 8rpx;
  display: block;
}

.user-bio {
  font-size: 28rpx;
  color: #94A3B8;
  margin-top: 20rpx;
  line-height: 1.5;
  display: block;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  
  button {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;
    font-weight: 600;
    transition: all 0.3s;
    
    &::after { border: none; }
    
    &:active { transform: scale(0.97); }
  }
  
  .btn-primary {
    background: #f2b90d;
    color: #000;
    
    &.is-following {
      background: rgba(255, 255, 255, 0.1);
      color: #94A3B8;
      border: 1rpx solid rgba(255, 255, 255, 0.1);
    }
  }
  
  .btn-secondary {
    background: rgba(255, 255, 255, 0.05);
    color: #fff;
    border: 1rpx solid rgba(255, 255, 255, 0.1);
  }
}

.stats-container {
  display: flex;
  padding: 40rpx;
  background: #0A0A0A;
  border-top: 1rpx solid rgba(255, 255, 255, 0.05);
  
  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .stat-value {
      font-size: 36rpx;
      font-weight: 700;
      color: #fff;
    }
    
    .stat-label {
      font-size: 24rpx;
      color: #64748B;
      margin-top: 4rpx;
    }
  }
}

.tabs-sticky {
  position: sticky;
  top: 120rpx;
  z-index: 90;
  background: #0A0A0A;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
}

.tabs-content {
  display: flex;
  height: 100rpx;
  
  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    
    .tab-text {
      font-size: 30rpx;
      color: #64748B;
      transition: all 0.3s;
    }
    
    &.active .tab-text {
      color: #f2b90d;
      font-weight: 700;
    }
    
    .tab-indicator {
      position: absolute;
      bottom: 0;
      width: 40rpx;
      height: 6rpx;
      background: #f2b90d;
      border-radius: 3rpx;
      box-shadow: 0 -2rpx 10rpx rgba(242, 185, 13, 0.5);
    }
  }
}

.content-feed {
  padding: 30rpx;
}

.post-card {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  
  .post-content-row {
    display: flex;
    gap: 20rpx;
  }
  
  .post-text-side {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  .post-title {
    font-size: 32rpx;
    font-weight: 700;
    color: #fff;
    margin-bottom: 12rpx;
  }
  
  .post-excerpt {
    font-size: 28rpx;
    color: #94A3B8;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    margin-bottom: 20rpx;
  }
  
  .post-image {
    width: 180rpx;
    height: 180rpx;
    border-radius: 16rpx;
  }
  
  .post-meta {
    display: flex;
    align-items: center;
    gap: 24rpx;
    
    .meta-item {
      display: flex;
      align-items: center;
      gap: 6rpx;
      color: #64748B;
      font-size: 24rpx;
    }
    
    .post-time {
      font-size: 24rpx;
      color: #475569;
      margin-left: auto;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
  color: #475569;
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
    opacity: 0.5;
  }
  
  text {
    font-size: 28rpx;
  }
}

.footer-info {
  padding: 60rpx 0;
  display: flex;
  justify-content: center;
  
  .version-text {
    font-size: 22rpx;
    color: #334155;
    letter-spacing: 2rpx;
  }
}
</style>
