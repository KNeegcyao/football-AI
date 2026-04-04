<template>
  <view class="container" :class="themeClass">
    <!-- 自定义导航栏 -->
    <view class="navbar bg-nav-bar border-b border-theme-main" :style="{ paddingTop: statusBarHeight + 'px', paddingRight: navbarPaddingRight + 'px' }">
      <view class="nav-left" @click="goBack">
        <text class="material-icons" :style="{ color: themeStore.theme === 'dark' ? '#fff' : '#000', fontSize: '48rpx' }">arrow_back</text>
      </view>
      <view class="nav-center">
        <text class="page-title text-theme-main">消息通知</text>
      </view>
      <view class="nav-right">
        <view class="action-btn bg-theme-secondary" @click="markAllRead" title="一键清理">
          <text class="material-icons" :style="{ color: themeStore.theme === 'dark' ? '#fff' : '#000', fontSize: '44rpx' }">cleaning_services</text>
        </view>
        <view class="action-btn bg-theme-secondary" @click="showSettings = true">
          <text class="material-icons" :style="{ color: themeStore.theme === 'dark' ? '#fff' : '#000', fontSize: '44rpx' }">settings</text>
        </view>
      </view>
    </view>

    <!-- 设置弹窗 -->
    <view class="settings-modal" v-if="showSettings" @click="showSettings = false">
      <view class="modal-content bg-theme-main" @click.stop>
        <view class="modal-header">
          <text class="modal-title text-theme-main">通知设置</text>
          <text class="material-icons close-btn text-theme-secondary" @click="showSettings = false">close</text>
        </view>
        <view class="settings-list">
          <view class="settings-item border-theme-main">
            <view class="item-left">
              <text class="item-title text-theme-main">消息提醒</text>
              <text class="item-desc text-theme-secondary">关闭后APP首页将不再进行数字提醒</text>
            </view>
            <switch :checked="enableNotification" @change="toggleNotification" color="#f2b90d" />
          </view>
        </view>
      </view>
    </view>

    <!-- 分类功能区 -->
    <section class="category-section">
      <view class="category-item" @click="filterByType('reply')">
        <view class="icon-wrapper bg-theme-secondary border-theme-main bg-emerald">
          <text class="material-icons" style="color: #10b981; font-size: 56rpx;">chat</text>
          <view class="category-badge" v-if="unreadCounts.reply > 0">{{ unreadCounts.reply > 99 ? '99+' : unreadCounts.reply }}</view>
        </view>
        <text class="label text-theme-secondary">回复与@</text>
      </view>
      <view class="category-item" @click="filterByType('like')">
        <view class="icon-wrapper bg-theme-secondary border-theme-main bg-rose">
          <text class="material-icons" style="color: #f43f5e; font-size: 56rpx;">thumb_up</text>
          <view class="category-badge" v-if="unreadCounts.like > 0">{{ unreadCounts.like > 99 ? '99+' : unreadCounts.like }}</view>
        </view>
        <text class="label text-theme-secondary">收到喜欢</text>
      </view>
      <view class="category-item" @click="filterByType('follow')">
        <view class="icon-wrapper bg-theme-secondary border-theme-main bg-sky">
          <text class="material-icons" style="color: #0ea5e9; font-size: 56rpx;">person_add</text>
          <view class="category-badge" v-if="unreadCounts.follow > 0">{{ unreadCounts.follow > 99 ? '99+' : unreadCounts.follow }}</view>
        </view>
        <text class="label text-theme-secondary">新增粉丝</text>
      </view>
    </section>

    <view class="notification-list">
      <view class="notification-item border-theme-main" v-for="(item, index) in sessions" :key="index" @click="handleSessionClick(item)">
        <!-- 头像区域 -->
        <view class="avatar-box">
          <image :src="getAvatarUrl(item.otherAvatar)" class="avatar-img border-theme-main bg-theme-secondary" mode="aspectFill"></image>
        </view>

        <!-- 内容区域 -->
        <view class="content-box">
          <view class="header-row">
            <view class="user-info">
              <text class="nickname text-theme-main">{{ item.otherNickname || '用户' }}</text>
            </view>
            <text class="time text-theme-secondary">{{ formatTime(item.lastMessageTime) }}</text>
          </view>

          <!-- 消息正文 -->
          <view class="message-row">
            <text class="desc-text text-theme-secondary">{{ formatMessage(item.lastMessage) }}</text>
            <view class="unread-badge" v-if="item.unreadCount > 0">{{ item.unreadCount > 99 ? '99+' : item.unreadCount }}</view>
          </view>
        </view>
      </view>

      <view class="empty-tip text-theme-secondary" v-if="sessions.length === 0">
        <text>暂无私聊消息</text>
      </view>
      <view class="safe-area-bottom"></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useChatStore } from '@/store/chat';
import { useThemeStore } from '@/store/theme';
import { getFullImageUrl } from '@/utils/request.js';
import request from '@/utils/request';
import { onPullDownRefresh, onLoad, onShow } from '@dcloudio/uni-app';

const chatStore = useChatStore();
const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const navbarPaddingRight = ref(16);
const statusBarHeight = ref(0);
const sessions = computed(() => chatStore.sessions);
const showSettings = ref(false);
const enableNotification = computed(() => chatStore.enableNotification);

const unreadCounts = ref({
  reply: 0,
  like: 0,
  follow: 0
});

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  
  fetchSessions();

  // #ifdef MP-WEIXIN
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect();
    navbarPaddingRight.value = (systemInfo.screenWidth - menuButton.left) + 8;
  } catch (e) {
    navbarPaddingRight.value = 94;
  }
  // #endif
});

onShow(() => {
  fetchUnreadCounts();
});

const fetchUnreadCounts = async () => {
  try {
    const res = await request.get('/api/notifications/unread-count-by-type');
    if (res) {
      // 后端类型: 1=点赞帖子, 2=点赞评论, 3=评论帖子, 4=回复评论, 5=关注, 6=@提及
      const likeCount = (res[1] || 0) + (res[2] || 0);
      const replyCount = (res[3] || 0) + (res[4] || 0) + (res[6] || 0);
      const followCount = (res[5] || 0);
      
      unreadCounts.value = {
        like: likeCount,
        reply: replyCount,
        follow: followCount
      };
    }
  } catch (e) {
    console.error('获取分类未读消息数失败:', e);
  }
};

onPullDownRefresh(() => {
  Promise.all([fetchSessions(), fetchUnreadCounts()]).finally(() => {
    uni.stopPullDownRefresh();
  });
});

const fetchSessions = () => {
  return chatStore.fetchSessions();
};

const filterByType = async (type) => {
  console.log('filterByType:', type);

  // 点击时标记该分类的消息为已读
  const typeMap = {
    reply: '3,4,6', // 评论帖子, 回复评论, @提及
    like: '1,2',    // 点赞帖子, 点赞评论
    follow: '5'     // 关注
  };
  if (typeMap[type]) {
    try {
      await request.put('/api/notifications/read-by-type', { types: typeMap[type] }, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      });
      // 乐观更新本地未读数量
      unreadCounts.value[type] = 0;
    } catch (e) {
      console.error('标记已读失败:', e);
    }
  }

  const routes = {
    reply: '/pages/community/reply-detail',
    like: '/pages/community/like-detail',
    follow: '/pages/community/fan-detail'
  };
  const url = routes[type];
  if (url) {
    console.log('Navigating to:', url);
    uni.navigateTo({ 
      url,
      success: () => {
        console.log('Navigation success');
      },
      fail: (err) => {
        console.error('Navigation fail:', err);
        uni.showToast({
          title: '页面跳转失败: ' + (err.errMsg || ''),
          icon: 'none'
        });
      }
    });
  }
};

const getAvatarUrl = (url) => {
  return getFullImageUrl(url) || '/static/default-avatar.png';
};

const formatMessage = (content) => {
  if (!content) return '暂无新消息';
  if (content.match(/\.(jpg|jpeg|png|gif|webp)(\?.*)?$/i) || content.startsWith('/uploads/')) {
    return '[图片]';
  }
  return content;
};

const formatTime = (time) => {
  if (!time) return '';
  const dateStr = time.replace('T', ' ');
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  
  if (diff < minute) return '刚刚';
  if (diff < hour) return Math.floor(diff / minute) + '分钟前';
  if (diff < day) return Math.floor(diff / hour) + '小时前';
  if (diff < 7 * day) return Math.floor(diff / day) + '天前';
  
  return dateStr.substring(0, 10);
};

const markAllRead = () => {
  uni.showToast({ title: '私聊消息暂不支持一键已读', icon: 'none' });
};

const goBack = () => {
  uni.navigateBack();
};

const toggleNotification = (e) => {
  chatStore.setNotification(e.detail.value);
};

const handleSessionClick = (session) => {
  const otherNickname = encodeURIComponent(session.otherNickname || '');
  const otherAvatar = encodeURIComponent(session.otherAvatar || '');
  uni.navigateTo({
    url: `/pages/message/chat?sessionId=${session.id}&otherUserId=${session.otherUserId}&otherNickname=${otherNickname}&otherAvatar=${otherAvatar}`
  });
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
}

/* 自定义导航栏样式 */
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
    
    .page-title {
      font-size: 34rpx;
      font-weight: 600;
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

/* 设置弹窗样式 */
.settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
  
  .modal-content {
    width: 100%;
    border-radius: 40rpx 40rpx 0 0;
    padding: 40rpx;
    padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40rpx;
      
      .modal-title {
        font-size: 34rpx;
        font-weight: 700;
      }
      
      .close-btn {
        font-size: 48rpx;
        padding: 10rpx;
      }
    }
    
    .settings-list {
      .settings-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx 0;
        
        .item-left {
          display: flex;
          flex-direction: column;
          gap: 8rpx;
          
          .item-title {
            font-size: 30rpx;
            font-weight: 500;
          }
          
          .item-desc {
            font-size: 24rpx;
            opacity: 0.8;
          }
        }
      }
    }
  }
}

/* 分类功能区 */
.category-section {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 32rpx;
  background-color: transparent;

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;

    .icon-wrapper {
      position: relative;
      width: 100rpx;
      height: 100rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;

      .category-badge {
        position: absolute;
        top: -10rpx;
        right: -10rpx;
        background-color: #ef4444;
        color: #ffffff;
        font-size: 20rpx;
        font-weight: bold;
        padding: 4rpx 10rpx;
        border-radius: 20rpx;
        min-width: 28rpx;
        text-align: center;
        border: 4rpx solid var(--bg-main);
      }

      &:active {
        transform: scale(0.95);
        opacity: 0.8;
      }

      &.bg-emerald { 
        border-color: rgba(16, 185, 129, 0.3);
      }
      &.bg-rose { 
        border-color: rgba(244, 63, 94, 0.3);
      }
      &.bg-sky { 
        border-color: rgba(14, 165, 233, 0.3);
      }
    }

    .label {
      font-size: 24rpx;
      font-weight: 500;
    }
  }
}

.notification-list {
  padding: 0 40rpx 40rpx;
  
  .notification-item {
    display: flex;
    padding: 32rpx 0;
    
    &:active {
      opacity: 0.7;
    }
    
    .avatar-box {
      position: relative;
      margin-right: 24rpx;
      flex-shrink: 0;

      .avatar-img {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50rpx;
      }
    }
    
    .content-box {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .header-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8rpx;
        
        .user-info {
          display: flex;
          align-items: center;
          gap: 12rpx;
          
          .nickname {
            font-size: 30rpx;
            font-weight: 700;
          }
        }
        
        .time {
          font-size: 22rpx;
        }
      }
      
      .message-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 20rpx;
        
        .desc-text {
          font-size: 26rpx;
          line-height: 1.4;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .unread-badge {
          min-width: 32rpx;
          height: 32rpx;
          padding: 0 8rpx;
          background-color: $pitch-pulse-primary;
          border-radius: 16rpx;
          color: #000;
          font-size: 20rpx;
          font-weight: bold;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 0 10rpx rgba($pitch-pulse-primary, 0.5);
        }
      }
    }
  }

  .empty-tip {
    padding: 100rpx 0;
    text-align: center;
    font-size: 28rpx;
  }
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>