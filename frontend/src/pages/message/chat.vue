<template>
  <view class="container" :class="themeClass">
    <view class="header-wrapper">
      <view class="status-bar bg-nav-bar"></view>
      
      <!-- Header -->
      <view class="header bg-nav-bar border-b border-theme-main">
        <view class="header-left" @click="goBack">
          <text class="material-icons">arrow_back_ios_new</text>
        </view>
        <view class="header-center">
          <text class="username">{{ otherUserNickname || '对话中' }}</text>
        </view>
        <view class="header-right" @click="showActionSheet">
          <text class="material-icons">more_horiz</text>
        </view>
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
        <!-- Time Separator -->
        <view class="time-separator" v-if="shouldShowTime(index)">
          <text class="bg-theme-secondary text-secondary">{{ formatTime(msg.createdAt) }}</text>
        </view>

        <view class="message-item" :class="{ 'message-me': Number(msg.senderId) === Number(currentUserId) }">
          <image 
            class="avatar bg-theme-secondary" 
            :src="Number(msg.senderId) === Number(currentUserId) ? currentUserAvatar : otherUserAvatar" 
            mode="aspectFill"
            @error="onAvatarError(msg.senderId)"
          ></image>
          <view class="message-bubble shadow-sm">
            <text v-if="Number(msg.type) === 0" class="message-text">{{ msg.content }}</text>
            <image 
              v-else-if="Number(msg.type) === 1" 
              class="message-image" 
              :src="getAvatarUrl(msg.content)" 
              mode="widthFix" 
              @click="previewImage(msg.content)"
            ></image>
          </view>
        </view>
      </view>
      <view class="bottom-padding"></view>
    </scroll-view>

    <!-- Input Bar -->
    <view class="input-bar-container bg-nav-bar border-t border-theme-main">
      <view class="tool-bar">
        <view class="tool-item" @click="chooseImage">
          <text class="material-icons text-secondary">image</text>
        </view>
        <view class="tool-item" @click="toggleEmojiPicker">
          <text class="material-icons text-secondary">{{ showEmojiPicker ? 'keyboard' : 'sentiment_satisfied' }}</text>
        </view>
      </view>
      <view class="input-bar">
        <view class="input-wrapper bg-theme-secondary">
          <textarea 
            class="chat-input" 
            v-model="inputText" 
            auto-height 
            :fixed="true"
            :cursor-spacing="20"
            placeholder="发送消息..."
            placeholder-style="color: var(--text-secondary)"
            :focus="inputFocus"
            @focus="onInputFocus"
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
      
      <!-- Emoji Picker -->
      <view class="emoji-picker bg-nav-bar border-t border-theme-main" v-if="showEmojiPicker">
        <scroll-view scroll-y class="emoji-scroll">
          <view class="emoji-list">
            <text 
              v-for="(emoji, index) in emojiList" 
              :key="index" 
              class="emoji-item"
              @click="addEmoji(emoji)"
            >{{ emoji }}</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue';
import { onLoad, onUnload } from '@dcloudio/uni-app';
import { useChatStore } from '@/store/chat';
import { useThemeStore } from '@/store/theme';
import { chatApi, fileApi, userApi } from '@/api';
import { BASE_URL } from '@/utils/request';
import { formatChatMessageTime } from '@/utils/utils';

const chatStore = useChatStore();
const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const sessionId = ref(null);
const otherUserId = ref(null);
const otherUserNickname = ref('');
const otherUserAvatar = ref('/static/soccer-logo.png');
const currentUserId = ref(null);
const currentUserAvatar = ref('/static/soccer-logo.png');

const getAvatarUrl = (avatar) => {
  if (!avatar) return '/static/soccer-logo.png';
  
  if (avatar.includes('/uploads/')) {
    const relativePath = avatar.substring(avatar.indexOf('/uploads/'))
    return fileApi.getFileUrl(relativePath)
  }
  
  if (avatar.startsWith('http')) {
    return avatar
  }
  
  if (avatar.startsWith('/static')) return avatar;
  
  return fileApi.getFileUrl(avatar)
};

const onAvatarError = (senderId) => {
  if (Number(senderId) === Number(currentUserId.value)) {
    currentUserAvatar.value = '/static/soccer-logo.png';
  } else {
    otherUserAvatar.value = '/static/soccer-logo.png';
  }
};

const messages = computed(() => chatStore.messages[sessionId.value] || []);
const inputText = ref('');
const lastMessageId = ref('');
const sending = ref(false);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
const showEmojiPicker = ref(false);
const inputFocus = ref(false);

const emojiList = [
  '😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊', '😋', '😎', '😍', '😘', '🥰', '😗', '😙', '😚', '☺️', '🙂', '🤗', '🤩',
  '🤔', '🤨', '😐', '😑', '😶', '🙄', '😏', '😣', '😥', '😮', '🤐', '😯', '😪', '😫', '😴', '😌', '😛', '😜', '😝', '🤤', '😒', '😓',
  '😔', '😕', '🙃', '🤑', '😲', '☹️', '🙁', '😖', '😞', '😟', '😤', '😢', '😭', '😦', '😧', '😨', '😩', '🤯', '😬', '😰', '😱', '🥵',
  '🥶', '😳', '🤪', '😵', '😡', '😠', '🤬', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '😇', '🤠', '🤡', '🥳', '🥴', '🥺', '🤥', '🤫', '🤭',
  '🧐', '🤓', '😈', '👿', '👹', '👺', '💀', '👻', '👽', '🤖', '💩', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾', '🙌', '👏',
  '👋', '👍', '👎', '👊', '✊', '🤛', '🤜', '🤞', '✌️', '🤟', '🤘', '👌', '👈', '👉', '👆', '👇', '✋', '🤚', '🖐', '🖖', '💪', '🙏',
  '⚽', '🏀', '🏈', '⚾', '🎾', '🏐', '🏉', '🎱', '🏓', '🏸', '🥅', '⛳', '⛸', '🎣', '🛶', '🏄', '🏇', '🏆', '🥇', '🥈', '🥉', '🏅'
];

onLoad(async (options) => {
  sessionId.value = options.sessionId ? Number(options.sessionId) : null;
  otherUserId.value = options.otherUserId;
  otherUserNickname.value = options.otherNickname ? decodeURIComponent(options.otherNickname) : '对话中';
  otherUserAvatar.value = options.otherAvatar ? getAvatarUrl(decodeURIComponent(options.otherAvatar)) : '/static/soccer-logo.png';

  let userInfo = uni.getStorageSync('userInfo');
  if (userInfo && typeof userInfo === 'string') {
    try {
      userInfo = JSON.parse(userInfo);
    } catch (e) {
      console.error('Failed to parse userInfo in chat.vue:', e);
    }
  }

  if (userInfo) {
    currentUserId.value = userInfo.id;
    currentUserAvatar.value = getAvatarUrl(userInfo.avatar);
    
    if (!userInfo.avatar || currentUserAvatar.value === '/static/soccer-logo.png') {
      userApi.getProfile().then(res => {
        if (res && res.avatar) {
          currentUserAvatar.value = getAvatarUrl(res.avatar);
          userInfo.avatar = res.avatar;
          uni.setStorageSync('userInfo', JSON.stringify(userInfo));
        }
      }).catch(err => console.error('Failed to sync profile in chat:', err));
    }
  }

  if (!sessionId.value && otherUserId.value) {
    try {
      const res = await chatApi.getSessionByUserId(otherUserId.value);
      sessionId.value = res;
    } catch (e) {
      console.error('Failed to get session id:', e);
    }
  }

  if (sessionId.value) {
    chatStore.setCurrentSession(sessionId.value);
    await loadMessages();
    scrollToBottom();
  }

  uni.$on('newChatMessage', (msg) => {
    if (msg.sessionId === sessionId.value) {
      scrollToBottom();
    }
  });
});

onUnload(() => {
  chatStore.setCurrentSession(null);
  uni.$off('newChatMessage');
});

const loadMessages = async () => {
  if (loading.value || !sessionId.value) return;
  loading.value = true;
  const res = await chatStore.fetchMessages(sessionId.value, page.value);
  if (res) {
    hasMore.value = res.current < res.pages;
  }
  loading.value = false;
};

const loadHistory = async () => {
  if (hasMore.value && !loading.value) {
    page.value++;
    await loadMessages();
  }
};

const sendMessage = async () => {
  if (!inputText.value.trim() || !otherUserId.value) return;
  
  sending.value = true;
  chatStore.sendMessage(otherUserId.value, inputText.value.trim(), 0);
  inputText.value = '';
  sending.value = false;
  
  nextTick(() => {
    scrollToBottom();
  });
};

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value;
  if (showEmojiPicker.value) {
    inputFocus.value = false;
  }
};

const onInputFocus = () => {
  showEmojiPicker.value = false;
};

const addEmoji = (emoji) => {
  inputText.value += emoji;
};

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePaths = res.tempFilePaths;
      uploadImage(tempFilePaths[0]);
    }
  });
};

const uploadImage = (filePath) => {
  uni.showLoading({ title: '发送中...' });
  
  const uploadUrl = fileApi.uploadUrl.startsWith('http') 
    ? fileApi.uploadUrl 
    : (BASE_URL + (fileApi.uploadUrl.startsWith('/') ? fileApi.uploadUrl : '/' + fileApi.uploadUrl));
    
  const token = uni.getStorageSync('token');

  uni.uploadFile({
    url: uploadUrl,
    filePath: filePath,
    name: 'file',
    header: {
      'Authorization': 'Bearer ' + token
    },
    success: (uploadFileRes) => {
      let resData;
      try {
        resData = typeof uploadFileRes.data === 'string' 
          ? JSON.parse(uploadFileRes.data) 
          : uploadFileRes.data;
      } catch (e) {
        console.error('Failed to parse upload response:', e);
        uni.showToast({ title: '服务器响应格式错误', icon: 'none' });
        return;
      }

      const imageUrl = resData.code === 200 ? resData.data : (resData.data || resData);
      
      if (imageUrl && typeof imageUrl === 'string') {
        chatStore.sendMessage(otherUserId.value, imageUrl, 1);
        nextTick(() => {
          scrollToBottom();
        });
      } else {
        uni.showToast({ title: '获取图片地址失败', icon: 'none' });
      }
    },
    fail: (err) => {
      uni.showToast({ title: '发送图片失败: ' + (err.errMsg || '网络错误'), icon: 'none' });
    },
    complete: () => {
      uni.hideLoading();
    }
  });
};

const previewImage = (url) => {
  const fullUrl = getAvatarUrl(url);
  uni.previewImage({
    urls: [fullUrl],
    current: fullUrl
  });
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messages.value.length > 0) {
      lastMessageId.value = 'msg-' + messages.value[messages.value.length - 1].id;
    }
  });
};

const shouldShowTime = (index) => {
  if (index === 0) return true;
  const curr = new Date(messages.value[index].createdAt);
  const prev = new Date(messages.value[index - 1].createdAt);
  return (curr - prev) > 5 * 60 * 1000;
};

const formatTime = (time) => {
  return formatChatMessageTime(time);
};

const goBack = () => {
  uni.navigateBack();
};

const showActionSheet = () => {
  uni.navigateTo({
    url: `/pages/message/chat-settings?userId=${otherUserId.value}`
  });
};
</script>

<style lang="scss" scoped>
/* 1. 根页面背景适配 */
:deep(page) {
  background-color: var(--bg-main);
  transition: background-color 0.3s;
}

.container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-main);
  color: var(--text-main);
  transition: all 0.3s;
}

/* 2. Header 宽度限制与背景修复 */
.header-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  max-width: 500px;
  margin: 0 auto;
}

.status-bar {
  height: var(--status-bar-height);
  background-color: var(--bg-nav-bar) !important;
}

.header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: var(--bg-nav-bar) !important;
  border-bottom: 1px solid var(--border-main);
  
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
    .username {
      font-size: 16px;
      font-weight: bold;
      color: var(--text-main);
    }
  }
}

.bg-nav-bar {
  background-color: var(--bg-nav-bar) !important;
}

.message-list {
  flex: 1;
  padding: 16px;
  padding-top: calc(var(--status-bar-height) + 44px + 16px);
  box-sizing: border-box;
  background-color: transparent;
}

.time-separator {
  text-align: center;
  margin: 16px 0;
  text {
    font-size: 12px;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
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
  }
  
  .message-bubble {
    max-width: 70%;
    padding: 10px 14px;
    border-radius: 18px;
    font-size: 15px;
    line-height: 1.4;
    position: relative;
    background-color: var(--bg-secondary);
    color: var(--text-main);
    border-top-left-radius: 4px;
    word-break: break-all;

    .message-text {
      font-size: 14px;
      line-height: 1.5;
      color: var(--text-main);
      word-break: break-all;
    }

    .message-image {
      max-width: 100%;
      border-radius: 8px;
      display: block;
    }
  }

  &.message-me {
    flex-direction: row-reverse;

    .avatar {
      margin-right: 0;
      margin-left: 12px;
    }

    .message-bubble {
      background-color: $pitch-pulse-primary;
      color: #000;
      border-top-left-radius: 18px;
      border-top-right-radius: 4px;

      .message-text {
        color: #000;
      }
    }
  }
}

.bottom-padding {
  height: 20px;
}

.input-bar-container {
  padding-bottom: env(safe-area-inset-bottom);
  background-color: var(--bg-nav-bar);

  .tool-bar {
    display: flex;
    padding: 8px 12px 0;
    gap: 16px;

    .tool-item {
      .material-icons {
        font-size: 24px;
      }

      &:active {
        opacity: 0.7;
      }
    }
  }

  .input-bar {
    display: flex;
    align-items: flex-end;
    padding: 8px 12px;
    gap: 10px;

    .input-wrapper {
      flex: 1;
      border-radius: 20px;
      padding: 8px 16px;

      .chat-input {
        width: 100%;
        font-size: 15px;
        color: var(--text-main);
        line-height: 20px;
        max-height: 100px;
      }
    }

    .send-btn {
      width: 64px;
      height: 36px;
      line-height: 36px;
      background-color: var(--bg-secondary);
      color: var(--text-secondary);
      font-size: 14px;
      border-radius: 18px;
      padding: 0;
      margin: 0;
      border: none;

      &::after {
        border: none;
      }

      &.send-btn-active {
        background-color: $pitch-pulse-primary;
        color: #000;
      }
    }
  }
}

.emoji-picker {
  height: 250px;

  .emoji-scroll {
    height: 100%;
  }

  .emoji-list {
    display: flex;
    flex-wrap: wrap;
    padding: 10px;

    .emoji-item {
      width: 12.5%;
      height: 45px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;

      &:active {
        background-color: var(--bg-secondary);
        border-radius: 8px;
      }
    }
  }
}

// 浅色模式全局强制匹配
.theme-light {
  --bg-main: #f8f9fa;
  --bg-nav-bar: #ffffff;
  --text-main: #1a1a1a;
  --border-main: rgba(0, 0, 0, 0.05);
  
  background-color: var(--bg-main) !important;
  
  .container {
    background-color: var(--bg-main) !important;
  }
  
  .status-bar, .header {
    background-color: var(--bg-nav-bar) !important;
    border-bottom: 1px solid var(--border-main);
  }

  .message-list {
    background-color: var(--bg-main) !important;
  }

  .message-item:not(.message-me) {
    .message-bubble {
      background-color: #f0f0f0;
      color: #333;
    }
  }
  .time-separator text {
    background-color: #f0f0f0;
    color: #666;
  }
  .input-wrapper {
    background-color: #f5f5f5 !important;
    .chat-input {
      color: #333;
    }
  }
  .send-btn:not(.send-btn-active) {
    background-color: #f5f5f5;
    color: #999;
  }
  .tool-item .material-icons {
    color: #666;
  }
  .header .username {
    color: #1a1a1a;
  }
  .header .material-icons {
    color: #333 !important;
  }
}
</style>