<template>
  <view class="container" :class="themeClass">
    <view class="custom-navbar bg-nav-bar border-b border-theme-main" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-inner">
        <view class="left" @click="goBack">
          <u-icon name="arrow-left" color="var(--text-main)" size="22"></u-icon>
        </view>
        <view class="center">
          <text class="title text-theme-main">收到喜欢</text>
        </view>
        <view class="right" :style="{ paddingRight: navbarPaddingRight + 'px' }">
          <view class="icon-btn bg-theme-secondary" @click="showSettings = true" title="设置">
            <u-icon name="setting" color="var(--text-main)" size="24"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <u-popup :show="showSettings" mode="bottom" round="24" @close="showSettings = false">
      <view class="settings-panel bg-theme-main">
        <view class="settings-header border-b border-theme-main">
          <text class="settings-title text-theme-main">收到喜欢消息提醒</text>
          <view class="close-btn" @click="showSettings = false">
            <u-icon name="close" color="var(--text-secondary)" size="20"></u-icon>
          </view>
        </view>
        <view class="settings-list">
          <view class="settings-item border-b border-theme-main" @click="handleSettingSelect('all')">
            <text class="item-label text-theme-main">所有人</text>
            <u-icon v-if="notificationSetting === 'all'" name="checkbox-mark" color="#f43f5e" size="18"></u-icon>
          </view>
          <view class="settings-item border-b border-theme-main" @click="handleSettingSelect('follow')">
            <text class="item-label text-theme-main">关注的人</text>
            <u-icon v-if="notificationSetting === 'follow'" name="checkbox-mark" color="#f43f5e" size="18"></u-icon>
          </view>
          <view class="settings-item" @click="handleSettingSelect('none')">
            <text class="item-label text-theme-main">不接收任何消息提醒</text>
            <u-icon v-if="notificationSetting === 'none'" name="checkbox-mark" color="#f43f5e" size="18"></u-icon>
          </view>
        </view>
      </view>
    </u-popup>

    <view class="list-content">
      <view class="item border-b border-theme-main" v-for="(item, index) in list" :key="index" @click="handleClick(item)">
        <view class="avatar-box">
          <image :src="item.fromUser.avatar || '/static/default-avatar.png'" class="avatar-img bg-theme-secondary" mode="aspectFill"></image>
          <view class="badge-icon border-theme-main">
            <u-icon name="thumb-up-fill" size="10" color="#000"></u-icon>
          </view>
        </view>
        <view class="main-box">
          <view class="header">
            <text class="nickname">{{ item.fromUser.nickname || '用户' }}</text>
            <text class="time">{{ formatTime(item.createdAt) }}</text>
          </view>
          <view class="action">
            <text class="action-text">{{ getActionDesc(item) }}</text>
            <text v-if="item.postTitle" class="post-title">"{{ item.postTitle }}"</text>
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
const notificationSetting = ref('all'); // 默认所有人

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  
  // 获取当前设置
  userApi.getProfile().then(res => {
    if (res && res.replyNotificationType) {
      notificationSetting.value = res.replyNotificationType === 'following' ? 'follow' : res.replyNotificationType;
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
    const apiType = type === 'all' ? 'all' : (type === 'follow' ? 'following' : 'none');
    await userApi.updateNotificationSetting(apiType);
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
      types: '1,2' // 帖子点赞、评论点赞
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

const getActionDesc = (item) => {
  return item.type === 1 ? '赞了你的帖子' : '赞了你的评论';
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
  if (item.postId) {
    uni.navigateTo({ url: `/pages/post/detail?id=${item.postId}` });
  }
};

const goBack = () => {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.switchTab({
      url: '/pages/community/index'
    });
  }
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
      position: relative;
      
      .avatar-img {
        width: 80rpx;
        height: 80rpx;
        border-radius: 40rpx;
      }
      
      .badge-icon {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 28rpx;
        height: 28rpx;
        background: $pitch-pulse-primary;
        border-radius: 14rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 4rpx solid var(--bg-main);
        
        :deep(.u-icon) {
          line-height: 1;
        }
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
        
        .post-title {
          color: $pitch-pulse-primary;
          margin-left: 10rpx;
        }
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

// 适配不同主题的 badge 边框
.theme-light {
  .badge-icon {
    border-color: var(--bg-main) !important;
  }
}
</style>