<template>
  <view class="container" :class="themeClass">
    <view class="status-bar bg-nav-bar"></view>
    
    <!-- Header -->
    <view class="header bg-nav-bar border-b border-theme-main">
      <view class="header-left" @click="goBack">
        <text class="material-icons">arrow_back_ios_new</text>
      </view>
      <view class="header-center">
        <text class="title">聊天详情</text>
      </view>
      <view class="header-right"></view>
    </view>

    <!-- Settings List -->
    <view class="settings-list">
      <view class="setting-item bg-nav-bar border-b border-theme-main">
        <view class="setting-info">
          <text class="setting-label">置顶聊天</text>
        </view>
        <switch :checked="isTop" @change="onTopChange" color="#4cd964" />
      </view>

      <view class="setting-item bg-nav-bar border-b border-theme-main">
        <view class="setting-info">
          <text class="setting-label">消息免打扰</text>
          <text class="setting-desc">默认数字提醒，开启免打扰后将不再提醒</text>
        </view>
        <switch :checked="isMute" @change="onMuteChange" color="#4cd964" />
      </view>

      <view class="setting-group-gap"></view>

      <view class="setting-item bg-nav-bar border-b border-theme-main">
        <view class="setting-info">
          <text class="setting-label">加入黑名单</text>
        </view>
        <switch :checked="isBlacklist" @change="onBlacklistChange" color="#4cd964" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { useThemeStore } from '@/store/theme';
import { chatApi } from '@/api';

const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const isTop = ref(false);
const isMute = ref(false);
const isBlacklist = ref(false);
const otherUserId = ref(null);
const sessionId = ref(null);

onLoad(async (options) => {
  if (options.userId) {
    otherUserId.value = options.userId;
    await fetchSettings();
  }
});

const fetchSettings = async () => {
  try {
    const res = await chatApi.getSessionSettings(otherUserId.value);
    if (res) {
      sessionId.value = res.id;
      isTop.value = res.isTop;
      isMute.value = res.isMute;
      isBlacklist.value = res.isBlacklisted;
    }
  } catch (e) {
    console.error('获取设置失败:', e);
  }
};

const goBack = () => {
  uni.navigateBack();
};

const onTopChange = async (e) => {
  const newValue = e.detail.value;
  try {
    await chatApi.setTop(sessionId.value, newValue);
    isTop.value = newValue;
    uni.showToast({
      title: isTop.value ? '已置顶' : '已取消置顶',
      icon: 'none'
    });
    // 通知列表刷新
    uni.$emit('refreshChatSessions');
  } catch (e) {
    console.error('设置置顶失败:', e);
    // 恢复状态
    isTop.value = !newValue;
  }
};

const onMuteChange = async (e) => {
  const newValue = e.detail.value;
  try {
    await chatApi.setMute(sessionId.value, newValue);
    isMute.value = newValue;
    uni.showToast({
      title: isMute.value ? '已开启免打扰' : '已关闭免打扰',
      icon: 'none'
    });
  } catch (e) {
    console.error('设置免打扰失败:', e);
    isMute.value = !newValue;
  }
};

const onBlacklistChange = (e) => {
  const newValue = e.detail.value;
  if (newValue) {
    uni.showModal({
      title: '加入黑名单',
      content: '确定要将该用户加入黑名单吗？加入后将不再收到对方消息。',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            await chatApi.setBlacklist(otherUserId.value, true);
            isBlacklist.value = true;
            uni.showToast({ title: '已加入黑名单', icon: 'none' });
          } catch (e) {
            console.error('加入黑名单失败:', e);
            isBlacklist.value = false;
          }
        } else {
          isBlacklist.value = false;
        }
      }
    });
  } else {
    uni.showModal({
      title: '移出黑名单',
      content: '确定要将该用户从黑名单移出吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await chatApi.setBlacklist(otherUserId.value, false);
            isBlacklist.value = false;
            uni.showToast({ title: '已从黑名单移除', icon: 'none' });
          } catch (e) {
            console.error('移出黑名单失败:', e);
            isBlacklist.value = true;
          }
        } else {
          isBlacklist.value = true;
        }
      }
    });
  }
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
  transition: all 0.3s;
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
  }
  
  .header-center {
    flex: 1;
    text-align: center;
    .title {
      font-size: 17px;
      font-weight: 500;
    }
  }
}

.settings-list {
  margin-top: 12px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  min-height: 56px;
  box-sizing: border-box;

  .setting-info {
    display: flex;
    flex-direction: column;
    flex: 1;
    margin-right: 16px;

    .setting-label {
      font-size: 16px;
      color: var(--text-main);
    }

    .setting-desc {
      font-size: 13px;
      color: var(--text-secondary);
      margin-top: 4px;
      line-height: 1.4;
    }
  }
}

.setting-group-gap {
  height: 12px;
}

/* 适配不同主题的 Switch 样式微调 */
:deep(.uni-switch-input) {
  &.uni-switch-input-checked {
    background-color: #4cd964 !important;
    border-color: #4cd964 !important;
  }
}

.theme-light {
  --bg-main: #f8f9fa;
  --bg-nav-bar: #ffffff;
  --text-main: #1a1a1a;
  --text-secondary: #999999;
  --border-main: rgba(0, 0, 0, 0.05);

  .setting-item {
    background-color: var(--bg-nav-bar) !important;
  }
}

.theme-dark {
  --bg-main: #000000;
  --bg-nav-bar: #1a1a1a;
  --text-main: #ffffff;
  --text-secondary: #888888;
  --border-main: rgba(255, 255, 255, 0.05);
}
</style>
