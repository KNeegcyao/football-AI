<template>
  <view class="container" :class="themeClass">
    <view class="navbar bg-nav-bar border-b border-theme-main" :style="{ paddingTop: statusBarHeight + 'px', paddingRight: navbarPaddingRight + 'px' }">
      <view class="nav-left" @click="goBack">
        <text class="material-icons" :style="{ color: themeStore.theme === 'dark' ? '#fff' : '#000', fontSize: '48rpx' }">arrow_back</text>
      </view>
      <view class="nav-center">
        <text class="nav-title text-theme-main">回复与@</text>
      </view>
      <view class="nav-right">
        <view class="action-btn bg-theme-secondary" @click="showSettings = true">
          <text class="material-icons" :style="{ color: themeStore.theme === 'dark' ? '#fff' : '#000', fontSize: '44rpx' }">settings</text>
        </view>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <u-popup :show="showSettings" mode="bottom" round="24" @close="showSettings = false">
      <view class="settings-panel bg-theme-main">
        <view class="settings-header border-b border-theme-main">
          <text class="settings-title text-theme-main">回复与@消息提醒</text>
          <view class="close-btn" @click="showSettings = false">
            <text class="material-icons" :style="{ color: 'var(--text-secondary)', fontSize: '40rpx' }">close</text>
          </view>
        </view>
        <view class="settings-list">
          <view class="settings-item border-b border-theme-main" @click="handleSettingSelect('all')">
            <text class="item-label text-theme-main">所有人</text>
            <text v-if="notificationSetting === 'all'" class="material-icons" style="color: #f43f5e; font-size: 40rpx;">check_box</text>
          </view>
          <view class="settings-item border-b border-theme-main" @click="handleSettingSelect('follow')">
            <text class="item-label text-theme-main">关注的人</text>
            <text v-if="notificationSetting === 'follow'" class="material-icons" style="color: #f43f5e; font-size: 40rpx;">check_box</text>
          </view>
          <view class="settings-item" @click="handleSettingSelect('none')">
            <text class="item-label text-theme-main">不接收任何消息提醒</text>
            <text v-if="notificationSetting === 'none'" class="material-icons" style="color: #f43f5e; font-size: 40rpx;">check_box</text>
          </view>
        </view>
      </view>
    </u-popup>

    <view class="list-content">
      <view class="item border-theme-main" v-for="(item, index) in list" :key="index" @click="handleClick(item)">
        <view class="avatar-box">
          <image :src="item.fromUser.avatar || '/static/default-avatar.png'" class="avatar-img bg-theme-secondary border border-theme-main" mode="aspectFill"></image>
        </view>
        <view class="main-box">
          <view class="header">
            <text class="nickname text-theme-main">{{ item.fromUser.nickname || '用户' }}</text>
            <text class="time text-theme-secondary">{{ formatTime(item.createdAt) }}</text>
          </view>
          <view class="action">
            <text class="action-text text-theme-secondary">{{ getActionDesc(item) }}</text>
            <text v-if="item.postTitle" class="post-title">"{{ item.postTitle }}"</text>
          </view>
          <view class="content-quote bg-theme-secondary" v-if="item.content">
            <text class="quote-text text-theme-secondary">{{ item.content }}</text>
          </view>
        </view>
        <view class="unread-dot" v-if="!item.isRead"></view>
      </view>
      <u-loadmore :status="loadStatus" @loadmore="loadData(false)" marginTop="30" color="var(--text-secondary)" />
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { notificationApi, userApi } from '@/api';
import { useThemeStore } from '@/store/theme';
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
import { getFullImageUrl } from '@/utils/request';

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

onLoad((options) => {
  console.log('Reply Detail Page Load, options:', options);
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
  console.log('Reply Detail Pull Down Refresh');
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
  console.log(`Loading data, refresh: ${refresh}, isLoading: ${isLoading.value}`);
  if (isLoading.value) return;
  if (refresh) {
    page.value = 1;
    list.value = [];
    loadStatus.value = 'loading';
  }
  isLoading.value = true;
  try {
    console.log('Calling notificationApi.getList...');
    const res = await notificationApi.getList({
      page: page.value,
      size: size.value,
      types: '3,4,6' // 评论、回复、@
    });
    console.log('notificationApi.getList success, res:', res);
    
    const records = (res.records || []).map(item => {
      if (item.fromUser && item.fromUser.avatar) {
        item.fromUser.avatar = getFullImageUrl(item.fromUser.avatar);
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
  const map = { 3: '评论了你的帖子', 4: '回复了你的评论', 6: '提到了你' };
  return map[item.type] || '发来消息';
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
}
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 100rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx;
  backdrop-filter: blur(10px);

  .nav-left {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    flex: 1;
  }

  .nav-center {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 2;
    
    .nav-title {
      font-size: 34rpx;
      font-weight: 800;
      letter-spacing: 2rpx;
    }
  }

  .nav-right {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex: 1;
    gap: 24rpx;
  }
  
  .action-btn {
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
  padding: 20rpx 30rpx;
  .item {
    display: flex; padding: 30rpx 0; position: relative;
    .avatar-box {
      margin-right: 20rpx;
      .avatar-img { width: 80rpx; height: 80rpx; border-radius: 40rpx; }
    }
    .main-box {
      flex: 1;
      .header {
        display: flex; justify-content: space-between; margin-bottom: 10rpx;
        .nickname { font-size: 28rpx; font-weight: bold; }
        .time { font-size: 24rpx; }
      }
      .action {
        font-size: 26rpx; margin-bottom: 15rpx;
        .post-title { color: $pitch-pulse-primary; margin-left: 10rpx; }
      }
      .content-quote {
        padding: 20rpx; border-radius: 8rpx; border-left: 4rpx solid $pitch-pulse-primary;
        .quote-text { font-size: 26rpx; }
      }
    }
    .unread-dot {
      position: absolute; right: 0; top: 50%; width: 12rpx; height: 12rpx; background: $pitch-pulse-primary; border-radius: 6rpx;
    }
  }
}
</style>
