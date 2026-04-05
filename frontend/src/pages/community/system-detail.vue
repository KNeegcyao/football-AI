<template>
  <view class="container" :class="themeClass">
    <view class="navbar bg-nav-bar border-b border-theme-main" :style="{ paddingTop: statusBarHeight + 'px', paddingRight: navbarPaddingRight + 'px' }">
      <view class="nav-left" @click="goBack">
        <text class="material-icons" :style="{ color: themeStore.theme === 'dark' ? '#fff' : '#000', fontSize: '48rpx' }">arrow_back</text>
      </view>
      <view class="nav-center">
        <text class="nav-title text-theme-main">系统通知</text>
      </view>
      <view class="nav-right">
      </view>
    </view>

    <view class="list-content">
      <view class="item border-b border-theme-main" v-for="(item, index) in list" :key="index" @click="handleClick(item)">
        <view class="avatar-box">
          <view class="system-icon-bg bg-purple">
            <text class="material-icons" style="color: #fff; font-size: 48rpx;">campaign</text>
          </view>
        </view>
        <view class="main-box">
          <view class="header">
            <text class="nickname text-theme-main">官方助手</text>
            <text class="time text-theme-secondary">{{ formatTime(item.createdAt) }}</text>
          </view>
          <view class="action">
            <text class="action-text text-theme-secondary">{{ item.content }}</text>
          </view>
        </view>
        <view class="unread-dot" v-if="!item.isRead"></view>
      </view>
      
      <view class="empty-tip text-theme-secondary" v-if="list.length === 0 && !isLoading">
        <text>暂无系统通知</text>
      </view>
      
      <u-loadmore v-if="list.length > 0" :status="loadStatus" @loadmore="loadData(false)" marginTop="30" color="var(--text-secondary)" />
    </view>
  </view>
</template>

<script setup>
import { ref, computed, getCurrentInstance } from 'vue';
import { notificationApi } from '@/api';
import { useThemeStore } from '@/store/theme';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';

const { proxy } = getCurrentInstance();
const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const list = ref([]);
const page = ref(1);
const size = ref(10);
const loadStatus = ref('loadmore');
const isLoading = ref(false);
const statusBarHeight = ref(0);
const navbarPaddingRight = ref(16);

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  
  // #ifdef MP-WEIXIN
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect();
    navbarPaddingRight.value = (systemInfo.screenWidth - menuButton.left) + 8;
  } catch (e) {
    navbarPaddingRight.value = 94;
  }
  // #endif
  
  loadData(true);
});

onPullDownRefresh(() => {
  loadData(true);
});

const loadData = async (refresh = false) => {
  if (isLoading.value) return;
  if (refresh) {
    page.value = 1;
    list.value = [];
    loadStatus.value = 'loading';
  }
  isLoading.value = true;
  try {
    const res = await notificationApi.getList({
      page: page.value,
      size: size.value,
      types: '8' // 系统通知
    });
    const records = res.records || [];
    
    if (refresh) {
      list.value = records;
    } else {
      list.value = [...list.value, ...records];
    }
    
    if (records.length < size.value) {
      loadStatus.value = 'nomore';
    } else {
      loadStatus.value = 'loadmore';
      page.value++;
    }
  } catch (e) {
    console.error(e);
  } finally {
    isLoading.value = false;
    if (refresh) uni.stopPullDownRefresh();
  }
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time.replace('T', ' '));
  const now = new Date();
  const diff = now - date;
  const minute = 60 * 1000, hour = 60 * minute, day = 24 * hour;
  if (diff < minute) return '刚刚';
  if (diff < hour) return Math.floor(diff / minute) + '分钟前';
  if (diff < day) return Math.floor(diff / hour) + '小时前';
  return time.substring(0, 10);
};

const handleClick = (item) => {
  if (!item.isRead) {
    notificationApi.read(item.id).then(() => { 
      item.isRead = 1;
      // 通知 CustomTabBar 刷新系统未读数
      uni.$emit('refreshSystemUnread');
    });
  }
};

const goBack = () => {
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
$pitch-pulse-primary: #f2b90d;

.container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
  transition: all 0.3s;
}

.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  min-height: 100rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 32rpx;
  backdrop-filter: blur(10px);
  background-color: var(--bg-nav-bar);
  
  .nav-left, .nav-right {
    flex: 1;
    display: flex;
    align-items: center;
  }
  
  .nav-right {
    justify-content: flex-end;
  }
  
  .nav-center {
    flex: 2;
    display: flex;
    justify-content: center;
    .nav-title {
      font-size: 34rpx;
      font-weight: 600;
    }
  }
}

.list-content {
  padding: 0 32rpx;
  
  .item {
    display: flex;
    align-items: flex-start;
    padding: 30rpx 0;
    position: relative;
    
    .avatar-box {
      margin-right: 24rpx;
      .system-icon-bg {
        width: 90rpx;
        height: 90rpx;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #a855f7;
      }
    }
    
    .main-box {
      flex: 1;
      min-width: 0;
      
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12rpx;
        
        .nickname {
          font-size: 30rpx;
          font-weight: 600;
        }
        
        .time {
          font-size: 24rpx;
        }
      }
      
      .action {
        .action-text {
          font-size: 28rpx;
          line-height: 1.5;
        }
      }
    }
    
    .unread-dot {
      position: absolute;
      top: 30rpx;
      right: 0;
      width: 16rpx;
      height: 16rpx;
      background-color: #ff4d4f;
      border-radius: 50%;
    }
  }
}

.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  font-size: 28rpx;
}
</style>