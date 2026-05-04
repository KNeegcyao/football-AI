<template>
  <view class="container" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <view class="status-bar bg-nav-bar"></view>
    
    <!-- Header -->
    <view class="header bg-nav-bar border-b border-theme-main">
      <view class="header-left" @click="goBack">
        <text class="material-icons">arrow_back</text>
      </view>
      <view class="header-center">
        <text class="title">黑名单</text>
      </view>
      <view class="header-right"></view>
    </view>

    <!-- Blacklist Content -->
    <scroll-view scroll-y class="list-container" @scrolltolower="loadMore">
      <view class="empty-state" v-if="!loading && blacklist.length === 0">
        <text class="material-icons empty-icon text-secondary">block</text>
        <text class="empty-text text-secondary">暂无黑名单用户</text>
      </view>

      <view class="user-item bg-nav-bar border-b border-theme-main" v-for="user in blacklist" :key="user.id">
        <image class="avatar bg-theme-secondary" :src="getAvatarUrl(user.avatar)" mode="aspectFill"></image>
        <view class="user-info">
          <text class="nickname">{{ user.nickname || user.username }}</text>
        </view>
        <button class="unblock-btn" @click="unblock(user.id)">移除</button>
      </view>
      
      <view class="loading-more" v-if="loading">
        <text class="text-secondary">加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useThemeStore } from '@/store/theme';
import { relationshipApi, chatApi } from '@/api';
import { getFullImageUrl } from '@/utils/request';

const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const statusBarHeight = ref(20);
const blacklist = ref([]);
const page = ref(1);
const loading = ref(false);
const hasMore = ref(true);

onMounted(() => {
  const sysInfo = uni.getSystemInfoSync();
  if (sysInfo.statusBarHeight) {
    statusBarHeight.value = sysInfo.statusBarHeight;
  }
});

onLoad(() => {
  loadBlacklist(true);
});

const loadBlacklist = async (reset = false) => {
  if (loading.value || (!hasMore.value && !reset)) return;
  
  if (reset) {
    page.value = 1;
    blacklist.value = [];
    hasMore.value = true;
  }
  
  loading.value = true;
  try {
    const res = await relationshipApi.getBlacklist({
      page: page.value,
      size: 20
    });
    
    if (res.code === 200 || res.code === 0) {
      const records = res.data.records || [];
      if (reset) {
        blacklist.value = records;
      } else {
        blacklist.value = [...blacklist.value, ...records];
      }
      
      hasMore.value = records.length >= 20;
      page.value++;
    }
  } catch (error) {
    console.error('获取黑名单失败:', error);
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    });
  } finally {
    loading.value = false;
  }
};

const loadMore = () => {
  loadBlacklist();
};

const unblock = async (userId) => {
  try {
    uni.showLoading({ title: '处理中' });
    // 调用现有的设置黑名单接口，isBlacklist 传 false 表示解除
    const res = await chatApi.setBlacklist(userId, false);
    if (res.code === 200 || res.code === 0) {
      uni.showToast({
        title: '已移出黑名单',
        icon: 'success'
      });
      // 从列表中移除
      blacklist.value = blacklist.value.filter(u => u.id !== userId);
    } else {
      throw new Error(res.msg || '操作失败');
    }
  } catch (error) {
    console.error('移除黑名单失败:', error);
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
  }
};

const getAvatarUrl = (avatar) => {
  return getFullImageUrl(avatar) || '/static/default-avatar.png';
};

const goBack = () => {
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
:deep(page) {
  background-color: var(--bg-main);
}

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
}

.status-bar {
  height: var(--status-bar-height);
}

.header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  
  .header-left, .header-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .material-icons {
      font-size: 24px;
      color: var(--text-main);
    }
  }
  
  .header-center {
    flex: 1;
    text-align: center;
    .title {
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.list-container {
  flex: 1;
  background-color: var(--bg-main);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.5;
  }
  
  .empty-text {
    font-size: 14px;
  }
}

.user-item {
  display: flex;
  align-items: center;
  padding: 16px;
  
  .avatar {
    width: 48px;
    height: 48px;
    border-radius: 24px;
    margin-right: 12px;
  }
  
  .user-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .nickname {
      font-size: 16px;
      font-weight: 500;
    }
  }
  
  .unblock-btn {
    margin: 0;
    padding: 0 16px;
    height: 32px;
    line-height: 32px;
    font-size: 14px;
    border-radius: 16px;
    background-color: var(--bg-secondary);
    color: var(--text-main);
    border: 1px solid var(--border-main);
    
    &::after {
      border: none;
    }
    
    &:active {
      opacity: 0.7;
    }
  }
}

.loading-more {
  text-align: center;
  padding: 16px;
  font-size: 12px;
}

.theme-light {
  --bg-main: #f8f9fa;
  --bg-nav-bar: #ffffff;
  --bg-secondary: #f1f5f9;
  --text-main: #1a1a1a;
  --text-secondary: #999999;
  --border-main: rgba(0, 0, 0, 0.05);
}

.theme-dark {
  --bg-main: #000000;
  --bg-nav-bar: #1a1a1a;
  --bg-secondary: #333333;
  --text-main: #ffffff;
  --text-secondary: #888888;
  --border-main: rgba(255, 255, 255, 0.05);
}
</style>