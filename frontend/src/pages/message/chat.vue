<template>
  <view class="container">
    <view class="status-bar"></view>
    
    <!-- Header -->
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="material-icons">arrow_back_ios_new</text>
      </view>
      <view class="header-center">
        <text class="username">{{ otherUserNickname || '对话中' }}</text>
      </view>
      <view class="header-right">
        <text class="material-icons">more_horiz</text>
      </view>
    </view>

    <!-- Message List -->
    <scroll-view 
      scroll-y 
      class="message-list" 
      :scroll-into-view="lastMessageId" 
      :scroll-with-animation="true"
      @scrolltoupper="loadHistory"
    >
      <view class="message-item-wrapper" v-for="(msg, index) in messages" :key="msg.id" :id="'msg-' + msg.id">
        <!-- Time Separator (Optional: show if time difference > 5 mins) -->
        <view class="time-separator" v-if="shouldShowTime(index)">
          <text>{{ formatTime(msg.createdAt) }}</text>
        </view>

        <view class="message-item" :class="{ 'message-me': msg.senderId === currentUserId }">
          <image 
            class="avatar" 
            :src="msg.senderId === currentUserId ? currentUserAvatar : otherUserAvatar" 
            mode="aspectFill"
          ></image>
          <view class="message-bubble">
            <text class="message-text">{{ msg.content }}</text>
          </view>
        </view>
      </view>
      <view class="bottom-padding"></view>
    </scroll-view>

    <!-- Input Bar -->
    <view class="input-bar">
      <view class="input-wrapper">
        <textarea 
          class="chat-input" 
          v-model="inputText" 
          auto-height 
          :fixed="true"
          :cursor-spacing="20"
          placeholder="发送消息..."
        ></textarea>
      </view>
      <button 
        class="send-btn" 
        :class="{ 'send-btn-active': inputText.trim() }"
        @click="sendMessage"
        :disabled="!inputText.trim() || sending"
      >
        {{ sending ? '...' : '发送' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { onLoad, onUnload } from '@dcloudio/uni-app';
import { messageApi, userApi, fileApi } from '@/api';

const otherUserId = ref(null);
const otherUserNickname = ref('');
const otherUserAvatar = ref('/static/avatar/default.png');
const currentUserId = ref(null);
const currentUserAvatar = ref('/static/avatar/default.png');

const messages = ref([]);
const inputText = ref('');
const lastMessageId = ref('');
const sending = ref(false);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

let refreshTimer = null;

onLoad((options) => {
  otherUserId.value = options.id;
  if (!otherUserId.value) {
    uni.showToast({ title: '用户不存在', icon: 'none' });
    setTimeout(() => uni.navigateBack(), 1500);
    return;
  }
  
  initData();
});

onUnload(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }
});

const initData = async () => {
  // 获取当前用户信息
  try {
    const profile = await userApi.getProfile();
    currentUserId.value = profile.id;
    currentUserAvatar.value = profile.avatar ? fileApi.getFileUrl(profile.avatar) : '/static/avatar/default.png';
  } catch (e) {
    console.error('Failed to get profile:', e);
  }

  // 初次加载消息
  await loadMessages();
  scrollToBottom();

  // 轮询新消息 (实际项目推荐用 WebSocket)
  refreshTimer = setInterval(() => {
    fetchNewMessages();
  }, 5000);
};

const loadMessages = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const res = await messageApi.getMessages(otherUserId.value, { page: page.value, size: 20 });
    if (res && res.records) {
      const newMsgs = res.records.reverse();
      messages.value = [...newMsgs, ...messages.value];
      hasMore.value = res.current < res.pages;
      
      // 如果是第一次加载，设置对方信息（从第一条消息中提取，或者额外请求用户信息）
      if (newMsgs.length > 0) {
        const firstMsg = newMsgs.find(m => m.senderId == otherUserId.value);
        if (firstMsg) {
          // 这里假设后端返回的消息包含基本用户信息，如果没有，需要额外接口
          // 目前先用占位
        }
      }
    }
  } catch (e) {
    console.error('Failed to load messages:', e);
  } finally {
    loading.value = false;
  }
};

const fetchNewMessages = async () => {
  try {
    const res = await messageApi.getMessages(otherUserId.value, { page: 1, size: 10 });
    if (res && res.records) {
      const latestMsgs = res.records;
      const newOnes = latestMsgs.filter(m => !messages.value.some(exist => exist.id === m.id)).reverse();
      if (newOnes.length > 0) {
        messages.value = [...messages.value, ...newOnes];
        scrollToBottom();
      }
    }
  } catch (e) {
    console.error('Failed to fetch new messages:', e);
  }
};

const loadHistory = async () => {
  if (!hasMore.value || loading.value) return;
  page.value++;
  const oldFirstId = messages.value[0]?.id;
  await loadMessages();
  // 加载完历史记录后，保持滚动位置
  if (oldFirstId) {
    nextTick(() => {
      lastMessageId.value = 'msg-' + oldFirstId;
    });
  }
};

const sendMessage = async () => {
  if (!inputText.value.trim() || sending.value) return;
  
  const content = inputText.value.trim();
  sending.value = true;
  
  try {
    await messageApi.send({
      receiverId: otherUserId.value,
      content: content
    });
    
    inputText.value = '';
    // 立即刷新
    await fetchNewMessages();
  } catch (e) {
    // 这里的错误可能是“非互关只能发一条”
    uni.showToast({ 
      title: e.message || '发送失败', 
      icon: 'none',
      duration: 2000
    });
  } finally {
    sending.value = false;
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messages.value.length > 0) {
      lastMessageId.value = 'msg-' + messages.value[messages.value.length - 1].id;
    }
  });
};

const goBack = () => {
  uni.navigateBack();
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  
  if (date.toDateString() === now.toDateString()) {
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
  }
  return (date.getMonth() + 1) + '/' + date.getDate() + ' ' + date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
};

const shouldShowTime = (index) => {
  if (index === 0) return true;
  const curr = new Date(messages.value[index].createdAt);
  const prev = new Date(messages.value[index - 1].createdAt);
  return (curr - prev) > 5 * 60 * 1000; // 5分钟间隔
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
    .username {
      font-size: 16px;
      font-weight: bold;
    }
  }
}

.message-list {
  flex: 1;
  padding: 16px;
  box-sizing: border-box;
}

.time-separator {
  text-align: center;
  margin: 16px 0;
  text {
    font-size: 12px;
    color: #64748b;
    background-color: rgba(255, 255, 255, 0.05);
    padding: 2px 8px;
    border-radius: 4px;
  }
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  
  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 20px;
    margin-right: 12px;
    background-color: #1e293b;
  }
  
  .message-bubble {
    max-width: 70%;
    background-color: #1e293b;
    padding: 10px 14px;
    border-radius: 4px 16px 16px 16px;
    
    .message-text {
      font-size: 14px;
      line-height: 1.5;
      color: #cbd5e1;
      word-break: break-all;
    }
  }
  
  &.message-me {
    flex-direction: row-reverse;
    
    .avatar {
      margin-right: 0;
      margin-left: 12px;
    }
    
    .message-bubble {
      background-color: #f2b90d;
      border-radius: 16px 4px 16px 16px;
      
      .message-text {
        color: #000;
      }
    }
  }
}

.bottom-padding {
  height: 20px;
}

.input-bar {
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background-color: #1e293b;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  
  .input-wrapper {
    flex: 1;
    background-color: #0f172a;
    border-radius: 20px;
    padding: 8px 16px;
    
    .chat-input {
      width: 100%;
      min-height: 20px;
      max-height: 100px;
      font-size: 14px;
      color: #fff;
    }
  }
  
  .send-btn {
    width: 60px;
    height: 36px;
    line-height: 36px;
    background-color: rgba(255, 255, 255, 0.1);
    color: #64748b;
    font-size: 14px;
    border-radius: 18px;
    padding: 0;
    margin: 0;
    
    &::after {
      border: none;
    }
    
    &.send-btn-active {
      background-color: #f2b90d;
      color: #000;
    }
  }
}
</style>
