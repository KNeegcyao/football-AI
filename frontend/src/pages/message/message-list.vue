<template>
  <view class="container">
    <view class="status-bar"></view>
    
    <!-- Header -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="material-icons">arrow_back_ios_new</text>
      </view>
      <view class="header-center">
        <text class="title">私信</text>
      </view>
      <view class="header-right"></view>
    </view>

    <!-- Conversation List -->
    <scroll-view scroll-y class="conversation-list" @scrolltolower="loadMore">
      <view 
        class="conversation-item" 
        v-for="item in conversations" 
        :key="item.id"
        @click="goToChat(item)"
      >
        <view class="avatar-wrapper">
          <image class="avatar" :src="item.otherAvatar || '/static/avatar/default.png'" mode="aspectFill"></image>
          <view class="unread-badge" v-if="item.unreadCount > 0">
            <text>{{ item.unreadCount > 99 ? '99+' : item.unreadCount }}</text>
          </view>
        </view>
        
        <view class="content-wrapper">
          <view class="top-row">
            <text class="nickname">{{ item.otherNickname || '用户' }}</text>
            <text class="time">{{ formatTime(item.lastMessageTime) }}</text>
          </view>
          <view class="bottom-row">
            <text class="last-msg">{{ item.lastMessage }}</text>
          </view>
        </view>
      </view>

      <view v-if="conversations.length === 0 && !loading" class="empty-state">
        <text class="material-icons empty-icon">mail_outline</text>
        <text>暂无私信对话</text>
      </view>
      
      <view v-if="loading" class="loading-state">
        <text>加载中...</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useChatStore } from '@/store/chat';
import { fileApi } from '@/api';

const chatStore = useChatStore();
const loading = ref(false);

const conversations = computed(() => chatStore.sessions);

onShow(() => {
  loadConversations();
  // 建立连接
  chatStore.connect();
});

const loadConversations = async () => {
  if (loading.value) return;
  loading.value = true;
  await chatStore.fetchSessions();
  loading.value = false;
};

const goToChat = (item) => {
  uni.navigateTo({
    url: `/pages/message/chat?sessionId=${item.id}&otherUserId=${item.otherUserId}&otherNickname=${item.otherNickname}&otherAvatar=${item.otherAvatar}`
  });
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  
  if (date.toDateString() === now.toDateString()) {
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
  }
  
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天';
  }
  
  return (date.getMonth() + 1) + '月' + date.getDate() + '日';
};

const goBack = () => {
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #0f172a;
  color: #fff;
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
  background-color: #1e293b;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  
  .header-left, .header-right {
    width: 40px;
    display: flex;
    align-items: center;
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

.conversation-list {
  flex: 1;
}

.conversation-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background-color 0.2s;
  
  &:active {
    background-color: rgba(255, 255, 255, 0.05);
  }
  
  .avatar-wrapper {
    position: relative;
    margin-right: 12px;
    
    .avatar {
      width: 48px;
      height: 48px;
      border-radius: 24px;
      background-color: #1e293b;
    }
    
    .unread-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background-color: #ef4444;
      color: #fff;
      font-size: 10px;
      min-width: 16px;
      height: 16px;
      line-height: 16px;
      text-align: center;
      border-radius: 8px;
      padding: 0 4px;
      border: 2px solid #0f172a;
    }
  }
  
  .content-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    overflow: hidden;
    
    .top-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
      
      .nickname {
        font-size: 15px;
        font-weight: 600;
        color: #f8fafc;
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .time {
        font-size: 12px;
        color: #64748b;
        margin-left: 8px;
      }
    }
    
    .bottom-row {
      .last-msg {
        font-size: 13px;
        color: #94a3b8;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        display: block;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100px;
  color: #64748b;
  font-size: 14px;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
    opacity: 0.5;
  }
}

.loading-state {
  text-align: center;
  padding: 20px;
  color: #64748b;
  font-size: 14px;
}
</style>
