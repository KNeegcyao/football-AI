<template>
  <view class="page-container min-h-screen font-display antialiased" :class="themeClass">
    <!-- Top Navigation Bar -->
    <view class="header-sticky" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="header-left" @click="goBack">
          <text class="material-symbols-outlined text-slate-100">arrow_back_ios_new</text>
        </view>
        <view class="header-center">
          <text class="text-lg font-bold tracking-tight">关注与粉丝</text>
        </view>
        <view class="header-right">
          <text class="material-symbols-outlined text-slate-100">search</text>
        </view>
      </view>
    </view>

    <!-- Tabs Component -->
    <view class="tabs-container">
      <view class="tabs-wrapper">
        <view 
          v-for="(tab, index) in tabs" 
          :key="index"
          class="tab-item"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          <text class="tab-text" :class="{ 'text-primary': currentTab === index }">
            {{ tab.name }} {{ tab.count }}
          </text>
          <view class="tab-indicator" v-if="currentTab === index"></view>
        </view>
      </view>
    </view>

    <!-- Scrollable List Area -->
    <scroll-view 
      scroll-y 
      class="list-content" 
      @scrolltolower="loadMore"
      enhanced
      :show-scrollbar="false"
    >
      <view class="user-list">
        <view 
          v-for="(user, index) in userList" 
          :key="user.id" 
          class="user-item"
          @click="goToUserProfile(user.id)"
        >
          <view class="avatar-container">
            <image 
              class="avatar-image" 
              :src="user.avatar || '/static/soccer-logo.png'" 
              mode="aspectFill"
            ></image>
            <view v-if="user.isVerified" class="verified-badge">
              <text class="material-symbols-outlined verified-icon">check</text>
            </view>
          </view>
          
          <view class="user-info">
            <view class="name-row">
              <text class="nickname">{{ user.nickname }}</text>
              <text v-if="user.isVerified" class="material-symbols-outlined verified-tag">verified</text>
            </view>
            <text class="bio">{{ user.bio || '暂无简介' }}</text>
          </view>
          
          <button 
            v-if="!user.isSelf"
            class="action-btn"
            :class="user.isFollowing ? 'btn-following' : 'btn-follow'"
            @click.stop="toggleFollow(user, index)"
          >
            {{ user.isFollowing ? '已关注' : (user.isFollower ? '回关' : '关注') }}
          </button>
        </view>

        <!-- Loading & Empty States -->
        <u-loadmore :status="loadStatus" color="#64748b" margin-top="40" margin-bottom="40" />
        <view class="empty-state" v-if="userList.length === 0 && loadStatus !== 'loading'">
          <text class="material-symbols-outlined empty-icon">group</text>
          <text>暂无{{ currentTab === 0 ? '关注' : '粉丝' }}</text>
        </view>
      </view>
      
      <!-- Bottom Spacing -->
      <view class="h-24"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useThemeStore } from '@/store/theme'
import { userApi, postApi, fileApi, relationshipApi } from '@/api'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight
const currentTab = ref(0)
const tabs = ref([
  { name: '关注', count: 0 },
  { name: '粉丝', count: 0 }
])

const userId = ref('')
const userList = ref([])
const page = ref(1)
const pageSize = ref(20)
const loadStatus = ref('loadmore')
const isSelf = ref(true)

onLoad((options) => {
  if (options.tab) {
    currentTab.value = parseInt(options.tab)
  }
  if (options.userId) {
    userId.value = options.userId
    const userInfo = uni.getStorageSync('userInfo')
    isSelf.value = !userId.value || userId.value === userInfo?.id
  }
  
  loadStats()
  refreshList()
})

const loadStats = async () => {
  try {
    const statsRes = await userApi.getStats(userId.value)
    if (statsRes) {
      tabs.value[0].count = statsRes.followingCount || 0
      tabs.value[1].count = statsRes.followerCount || 0
    }
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

const refreshList = () => {
  page.value = 1
  userList.value = []
  loadStatus.value = 'loadmore'
  loadData()
}

const loadData = async () => {
  if (loadStatus.value === 'loading' || loadStatus.value === 'nomore') return
  
  loadStatus.value = 'loading'
  try {
    let res
    const params = { page: page.value, size: pageSize.value }
    
    if (currentTab.value === 0) {
      // 获取关注列表
      res = isSelf.value 
        ? await userApi.getFollowing(params) 
        : await userApi.getUserFollowing(userId.value, params)
    } else {
      // 获取粉丝列表
      res = isSelf.value 
        ? await userApi.getFollowers(params) 
        : await userApi.getUserFollowers(userId.value, params)
    }

    if (res && res.records) {
      const userInfo = uni.getStorageSync('userInfo')
      const newList = res.records.map(item => ({
        id: item.id,
        nickname: item.nickname || item.username,
        avatar: fileApi.getFileUrl(item.avatar),
        bio: item.bio,
        isVerified: item.isVerified || false,
        isFollowing: item.isFollowing || false,
        isFollower: item.isFollower || false,
        isSelf: item.id === userInfo?.id
      }))

      if (page.value === 1) {
        userList.value = newList
      } else {
        userList.value = [...userList.value, ...newList]
      }

      if (newList.length < pageSize.value) {
        loadStatus.value = 'nomore'
      } else {
        loadStatus.value = 'loadmore'
        page.value++
      }
    } else {
      loadStatus.value = 'nomore'
    }
  } catch (e) {
    console.error('加载列表失败:', e)
    loadStatus.value = 'loadmore'
  }
}

const switchTab = (index) => {
  if (currentTab.value === index) return
  currentTab.value = index
  refreshList()
}

const loadMore = () => {
  loadData()
}

const toggleFollow = async (user, index) => {
  try {
    if (user.isFollowing) {
      await relationshipApi.unfollow(user.id)
      user.isFollowing = false
      if (isSelf.value && currentTab.value === 0) {
        // 如果是在自己的关注列表取消关注，可以直接移除
        // userList.value.splice(index, 1)
        // tabs.value[0].count--
      }
    } else {
      await relationshipApi.follow(user.id)
      user.isFollowing = true
    }
  } catch (e) {
    console.error('关注操作失败:', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const goToUserProfile = (id) => {
  const userInfo = uni.getStorageSync('userInfo')
  if (id === userInfo?.id) {
    uni.switchTab({ url: '/pages/my/my' })
  } else {
    uni.navigateTo({ url: `/pages/my/user-profile?userId=${id}` })
  }
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.page-container {
  display: flex;
  flex-direction: column;
  background-color: #12080a;
}

.header-sticky {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(18, 8, 10, 0.8);
  backdrop-filter: blur(10px);
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.tabs-container {
  padding: 0 32rpx 16rpx;
  background-color: #12080a;
}

.tabs-wrapper {
  display: flex;
  border-bottom: 1px solid rgba(61, 38, 42, 0.4);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  position: relative;
  
  &.active {
    .tab-text {
      color: #db143c;
    }
  }
}

.tab-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #64748b;
  transition: all 0.3s;
}

.text-primary {
  color: #db143c !important;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 64rpx;
  height: 6rpx;
  background-color: #db143c;
  border-radius: 999rpx;
}

.list-content {
  flex: 1;
  height: 0;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 32rpx;
  border-bottom: 1px solid rgba(61, 38, 42, 0.1);
  transition: background-color 0.2s;
  
  &:active {
    background-color: rgba(219, 20, 60, 0.05);
  }
}

.avatar-container {
  position: relative;
  flex-shrink: 0;
}

.avatar-image {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(219, 20, 60, 0.2);
}

.verified-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32rpx;
  height: 32rpx;
  background-color: #db143c;
  border-radius: 50%;
  border: 4rpx solid #12080a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.verified-icon {
  font-size: 20rpx;
  color: white;
  font-weight: bold;
}

.user-info {
  flex: 1;
  margin: 0 24rpx;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 4rpx;
}

.nickname {
  font-size: 32rpx;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.verified-tag {
  color: #db143c;
  font-size: 28rpx;
}

.bio {
  font-size: 24rpx;
  color: #94a3b8;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.action-btn {
  height: 64rpx;
  padding: 0 32rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  margin: 0;
  
  &::after { border: none; }
}

.btn-follow {
  background-color: #db143c;
  color: white;
  box-shadow: 0 8rpx 16rpx rgba(219, 20, 60, 0.2);
  
  &:active { transform: scale(0.95); }
}

.btn-following {
  background-color: #1f1416;
  color: #cbd5e1;
  border: 1px solid #3d262a;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  color: #64748b;
  gap: 16rpx;
}

.empty-icon {
  font-size: 120rpx;
  opacity: 0.1;
}

.h-24 { height: 192rpx; }

/* Material Icons Support */
.material-symbols-outlined {
  font-family: 'MaterialIcons' !important;
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}

/* 浅色模式适配 */
.theme-light {
  background-color: var(--bg-main);
  
  .header-sticky {
    background-color: var(--bg-main);
    border-bottom: 1px solid var(--border-main);
    .text-slate-100 {
      color: var(--text-main) !important;
    }
  }
  
  .tabs-container {
    background-color: var(--bg-main);
    border-bottom: 1px solid var(--border-main);
    .tab-text {
      color: var(--text-secondary);
      &.text-primary {
        color: var(--accent-color) !important;
      }
    }
  }
  
  .user-item {
    background-color: #FFFFFF;
    border-bottom: 1px solid #F3F4F6;
    .nickname {
      color: var(--text-main);
    }
    .bio {
      color: var(--text-secondary);
    }
  }
  
  .btn-following {
    background-color: #F3F4F6 !important;
    color: #6B7280 !important;
  }
}
</style>
