<template>
  <view class="container" :class="themeClass">
    <view class="custom-navbar bg-nav-bar border-b border-theme-main" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-inner">
        <view class="left" @click="goBack">
          <u-icon name="arrow-left" color="var(--text-main)" size="22"></u-icon>
        </view>
        <view class="center">
          <text class="title text-theme-main">新增粉丝</text>
        </view>
        <view class="right" :style="{ paddingRight: navbarPaddingRight + 'px' }">
          <view class="icon-btn bg-theme-secondary" @click="showSettings = true" title="设置">
            <u-icon name="setting" :color="themeStore.theme === 'dark' ? '#fff' : '#000'" size="24"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <u-popup :show="showSettings" mode="bottom" round="24" @close="showSettings = false">
      <view class="settings-panel bg-theme-main">
        <view class="settings-header border-b border-theme-main">
          <text class="settings-title text-theme-main">新增粉丝消息提醒</text>
          <view class="close-btn" @click="showSettings = false">
            <u-icon name="close" color="var(--text-secondary)" size="20"></u-icon>
          </view>
        </view>
        <view class="settings-list">
          <view class="settings-item border-b border-theme-main" @click="handleSettingSelect('receive')">
            <text class="item-label text-theme-main">接收提醒</text>
            <view class="radio-box" :class="{ checked: notificationSetting === 'receive' }"></view>
          </view>
          <view class="settings-item" @click="handleSettingSelect('never')">
            <text class="item-label text-theme-main">永不提醒</text>
            <view class="radio-box" :class="{ checked: notificationSetting === 'never' }"></view>
          </view>
        </view>
      </view>
    </u-popup>

    <view class="list-content">
      <view class="item border-b border-theme-main" v-for="(item, index) in list" :key="index" @click="handleClick(item)">
        <view class="avatar-box">
          <image :src="item.fromUser.avatar || '/static/default-avatar.png'" class="avatar-img bg-theme-secondary" mode="aspectFill"></image>
        </view>
        <view class="main-box">
          <view class="header">
            <text class="nickname">{{ item.fromUser.nickname || '用户' }}</text>
            <text class="time">{{ formatTime(item.createdAt) }}</text>
          </view>
          <view class="action">
            <text class="action-text">关注了你</text>
          </view>
        </view>
        <view class="unread-dot" v-if="!item.isRead"></view>
      </view>
      <u-loadmore :status="loadStatus" @loadmore="loadData(false)" marginTop="30" color="var(--text-secondary)" />
    </view>
  </view>
</template>

<script setup>
import { ref, computed, getCurrentInstance } from 'vue';
import { notificationApi, userApi } from '@/api';
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
const showSettings = ref(false);
const notificationSetting = ref('receive'); // 默认接收提醒

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  
  // 获取当前设置
  userApi.getProfile().then(res => {
    if (res && res.fanNotificationType) {
      notificationSetting.value = res.fanNotificationType;
    }
  });
  
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

const handleSettingSelect = async (type) => {
  try {
    await userApi.updateFanNotificationSetting(type);
    notificationSetting.value = type;
    uni.showToast({
      title: '设置成功',
      icon: 'success'
    });
    setTimeout(() => {
      showSettings.value = false;
    }, 500);
  } catch (e) {
    uni.showToast({
      title: '设置失败',
      icon: 'none'
    });
  }
};

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
      types: '5' // 关注
    });
    const records = (res.records || []).map(item => {
      if (item.fromUser && item.fromUser.avatar) {
        item.fromUser.avatar = proxy.$utils.getFullImageUrl(item.fromUser.avatar);
      }
      return item;
    });
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
    notificationApi.read(item.id).then(() => { item.isRead = 1; });
  }
  uni.navigateTo({ url: `/pages/my/profile?id=${item.fromUser.id}` });
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

.custom-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);

  .navbar-inner {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 24rpx 0 32rpx;

    .left {
      display: flex;
      align-items: center;
      min-width: 80rpx;
    }

    .center {
      flex: 1;
      display: flex;
      justify-content: center;
      
      .title {
        font-size: 34rpx;
        font-weight: 800;
        letter-spacing: 2rpx;
      }
    }

    .right {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 12rpx;
      min-width: 180rpx;
    }
    
    .icon-btn {
      width: 80rpx;
      height: 80rpx;
      border-radius: 20rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      &:active {
        opacity: 0.7;
      }
    }
  }
}

.settings-panel {
  padding-bottom: env(safe-area-inset-bottom);
  
  .settings-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 40rpx;
    
    .settings-title {
      font-size: 32rpx;
      font-weight: 700;
    }
    
    .close-btn {
      padding: 10rpx;
    }
  }
  
  .settings-list {
    padding: 0 40rpx;
    
    .settings-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 40rpx 0;
      
      .item-label {
        font-size: 30rpx;
        font-weight: 500;
      }
      
      .radio-box {
        width: 40rpx;
        height: 40rpx;
        border: 4rpx solid var(--text-secondary);
        border-radius: 50%;
        position: relative;
        transition: all 0.2s;
        
        &.checked {
          border-color: $pitch-pulse-primary;
          &::after {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 20rpx;
            height: 20rpx;
            background: $pitch-pulse-primary;
            border-radius: 50%;
          }
        }
      }
      
      &:active {
        opacity: 0.7;
      }
    }
  }
}

.list-content {
  padding: 0 30rpx;
  
  .item {
    display: flex;
    align-items: center;
    padding: 30rpx 0;
    position: relative;
    
    .avatar-box {
      margin-right: 20rpx;
      
      .avatar-img {
        width: 80rpx;
        height: 80rpx;
        border-radius: 40rpx;
      }
    }
    
    .main-box {
      flex: 1;
      
      .header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6rpx;
        
        .nickname {
          font-size: 28rpx;
          font-weight: bold;
          color: var(--text-main);
        }
        
        .time {
          font-size: 24rpx;
          color: var(--text-secondary);
        }
      }
      
      .action {
        font-size: 26rpx;
        color: var(--text-secondary);
      }
    }
    
    .unread-dot {
      width: 12rpx;
      height: 12rpx;
      background: $pitch-pulse-primary;
      border-radius: 6rpx;
      margin-left: 20rpx;
    }
  }
}
</style>