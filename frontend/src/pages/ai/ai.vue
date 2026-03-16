<template>
  <view class="container" :class="themeClass">
    <!-- 状态栏占位 -->
    <view class="status-bar"></view>

    <header class="sticky-header glass-effect">
      <view class="header-content">
        <view class="logo-area">
          <view class="logo-icon bg-primary">
            <text class="material-symbols-outlined text-accent" style="font-size: 40rpx;">sports_soccer</text>
          </view>
          <text class="logo-text text-slate-100">PITCHPULSE</text>
        </view>
        <button class="search-btn">
          <text class="material-symbols-outlined text-slate-100" style="font-size: 44rpx;">search</text>
        </button>
      </view>
    </header>

    <scroll-view scroll-y class="chat-main" :scroll-into-view="lastMessageId">
      <!-- 快捷功能卡片 -->
      <view class="quick-actions">
        <view class="action-card bg-card-dark border-border-dark" 
              v-for="(action, index) in quickActions" 
              :key="index"
              @click="handleQuickAction(action.title)">
          <text class="material-symbols-outlined text-primary action-icon" style="font-size: 48rpx;">{{ action.icon }}</text>
          <text class="action-text text-slate-300">{{ action.title }}</text>
        </view>
      </view>

      <!-- 聊天内容 -->
      <view class="chat-list">
        <view v-for="(msg, index) in chatMessages" :key="index" 
              :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']"
              :id="'msg-' + index">
          <view class="avatar-box" :class="msg.role === 'user' ? 'user-avatar-bg' : 'ai-avatar-bg'">
            <text class="material-symbols-outlined" 
                  :style="{ color: msg.role === 'user' ? '#FFD700' : '#8B0000', fontSize: '32rpx' }">
              {{ msg.role === 'user' ? 'person' : 'smart_toy' }}
            </text>
          </view>
          <view class="msg-content-wrapper" :class="{ 'items-end': msg.role === 'user' }">
            <text class="role-label" :class="{ 'user-label': msg.role === 'user' }">
              {{ msg.role === 'user' ? '我' : 'AI Pulse Assistant' }}
            </text>
            <view class="msg-bubble shadow-sm" :class="msg.role === 'user' ? 'user-bubble pulse-glow' : 'ai-bubble'">
              <text class="msg-text" :class="msg.role === 'user' ? 'text-white' : 'text-slate-200'">{{ msg.content }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area-fixed">
      <view class="input-wrapper glass-effect pulse-glow">
        <text class="material-symbols-outlined text-slate-400 mic-btn" style="font-size: 40rpx;">mic</text>
        <input class="chat-input text-slate-200" 
               placeholder="咨询 AI 助手..." 
               v-model="userInput" 
               @confirm="sendMessage"
               :disabled="isTyping"
               placeholder-style="color: #64748b" />
        <view class="send-btn bg-primary" :class="{ 'opacity-50': isTyping }" @click="sendMessage">
          <text class="send-btn-text">Pulse AI</text>
          <text class="material-symbols-outlined text-white" style="font-size: 28rpx;">send</text>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <view class="tab-bar bg-tab-bar border-theme-main">
      <view v-for="(tab, index) in tabs" :key="index" 
            :class="['tab-item', tab.isCenter ? 'center-item' : '', currentTab === index ? 'active' : '']"
            @tap="handleTabClick(index)">
        <view v-if="tab.isCenter" class="center-icon bg-primary pulse-glow">
          <text class="material-symbols-outlined text-accent animate-pulse" style="font-size: 56rpx;">{{ tab.icon }}</text>
        </view>
        <template v-else>
          <text class="material-symbols-outlined" :style="{ color: currentTab === index ? '#f9d406' : 'rgba(255, 255, 255, 0.4)', fontSize: '48rpx' }">{{ tab.icon }}</text>
          <text class="tab-text" :class="currentTab === index ? 'text-[#f9d406]' : 'text-theme-secondary'">{{ tab.text }}</text>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useThemeStore } from '@/store/theme'
import { aiApi } from '@/api'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const currentTab = ref(2) // AI 助手在索引 2
const lastMessageId = ref('')
const userInput = ref('')
const isTyping = ref(false)

const tabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar_month', path: 'pages/schedule/schedule' },
  { text: 'AI助手', icon: 'psychology', path: 'pages/ai/ai', isCenter: true },
  { text: '社区', icon: 'forum', path: 'pages/community/community' },
  { text: '我的', icon: 'person', path: 'pages/my/my' }
]

const quickActions = [
  { title: '生成机智回复', icon: 'auto_awesome' },
  { title: '战术分析', icon: 'strategy' },
  { title: '球员数据对比', icon: 'leaderboard' },
  { title: '比赛预测', icon: 'query_stats' }
]

const chatMessages = ref([
  { role: 'ai', content: '你好！我是你的 PitchPulse 助手。今晚的欧冠比赛你怎么看？我可以为你提供详细的战术分析或球员对比数据。' },
  { role: 'user', content: '帮我对比一下哈兰德和姆巴佩本赛季在欧冠的进球效率。' },
  { role: 'ai', content: '这是一个极佳的切入点！以下是截至目前的对比：\n\n• 哈兰德: 场均进球 1.25，每 68 分钟一次得分。\n• 姆巴佩: 场均进球 0.95，每 84 分钟一次得分。\n\n哈兰德在禁区内的终结能力略胜一筹，而姆巴佩的突破成功率更高。需要我生成图表对比吗？' }
])

const handleTabClick = (index) => {
  if (index === currentTab.value) return
  uni.switchTab({
    url: '/' + tabs[index].path
  })
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isTyping.value) return
  const content = userInput.value
  chatMessages.value.push({ role: 'user', content })
  userInput.value = ''
  
  // 滚动到底部
  scrollToBottom()
  
  isTyping.value = true
  chatMessages.value.push({ role: 'ai', content: '正在为您分析数据...' })
  scrollToBottom()
  
  try {
    const res = await aiApi.askRule({ question: content })
    // 替换最后一条消息（“正在分析数据...”）
    chatMessages.value[chatMessages.value.length - 1].content = res.data
    scrollToBottom()
  } catch (e) {
    console.error('AI回复失败:', e)
    chatMessages.value[chatMessages.value.length - 1].content = '抱歉，我现在遇到了一点问题，请稍后再试。'
    scrollToBottom()
  } finally {
    isTyping.value = false
  }
}

const handleQuickAction = (title) => {
  userInput.value = title
  sendMessage()
}

const scrollToBottom = () => {
  setTimeout(() => {
    lastMessageId.value = 'msg-' + (chatMessages.value.length - 1)
  }, 100)
}

onShow(() => {
  uni.hideTabBar()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #0a0a0a; // 强制使用深色背景
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
}

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #333;
}

.glass-effect {
  background: rgba(26, 26, 26, 0.8);
  backdrop-filter: blur(12px);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 32rpx;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.logo-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(139, 0, 0, 0.3);
}

.logo-text {
  font-size: 40rpx;
  font-weight: 800;
  letter-spacing: -2rpx;
  font-family: 'Public Sans', sans-serif;
}

.chat-main {
  flex: 1;
  padding: 32rpx;
  box-sizing: border-box;
  background: radial-gradient(circle at top right, rgba(139, 0, 0, 0.1) 0%, transparent 60%);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  margin-bottom: 48rpx;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 40rpx 32rpx;
  border-radius: 24rpx;
  border: 1px solid #333;
  background-color: #1a1a1a;
  transition: all 0.3s;
  
  &:active {
    border-color: #8B0000;
    transform: scale(0.98);
  }

  .action-icon {
    transition: transform 0.3s;
  }

  &:hover .action-icon {
    transform: scale(1.1);
  }
}

.action-text {
  font-size: 24rpx;
  font-weight: 500;
  color: #cbd5e1;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.message-item {
  display: flex;
  gap: 24rpx;
  max-width: 90%;
  
  &.user-message {
    align-self: flex-end;
    flex-direction: row-reverse;
  }
}

.avatar-box {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-avatar-bg {
  background: rgba(139, 0, 0, 0.2);
  border: 1px solid rgba(139, 0, 0, 0.4);
}

.user-avatar-bg {
  background: rgba(255, 215, 0, 0.2);
  border: 1px solid rgba(255, 215, 0, 0.4);
}

.msg-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8rpx;

  &.items-end {
    align-items: flex-end;
  }
}

.role-label {
  font-size: 18rpx;
  font-weight: 700;
  color: #8B0000;
  text-transform: uppercase;
  letter-spacing: 2rpx;
  
  &.user-label {
    color: #64748b;
  }
}

.msg-bubble {
  padding: 24rpx 32rpx;
  border-radius: 32rpx;
  
  &.ai-bubble {
    border-top-left-radius: 4rpx;
    background-color: #1a1a1a;
    border: 1px solid #333;
    color: #e2e8f0;
  }
  
  &.user-bubble {
    border-top-right-radius: 4rpx;
    background-color: #8B0000;
    color: #ffffff;
  }
}

.msg-text {
  font-size: 28rpx;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.input-area-fixed {
  position: fixed;
  bottom: 160rpx;
  left: 32rpx;
  right: 32rpx;
  z-index: 99;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 12rpx 12rpx 12rpx 24rpx;
  border-radius: 32rpx;
  border: 1px solid rgba(139, 0, 0, 0.3);
  background: rgba(26, 26, 26, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 0 25rpx rgba(139, 0, 0, 0.3);
}

.chat-input {
  flex: 1;
  font-size: 28rpx;
  background: transparent;
  border: none;
  height: 80rpx;
}

.mic-btn {
  padding: 10rpx;
}

.send-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 32rpx;
  border-radius: 20rpx;
  background-color: #8B0000;
  transition: transform 0.2s;
  
  &:active {
    transform: scale(0.95);
  }
  
  .send-btn-text {
    color: white;
    font-size: 22rpx;
    font-weight: 700;
  }
}

.bg-primary { background-color: #8B0000; }
.bg-card-dark { background-color: #1a1a1a; }
.border-border-dark { border-color: #333333; }
.pulse-glow { box-shadow: 0 0 20rpx rgba(139, 0, 0, 0.5); }

/* 底部导航栏样式同步自 index.vue */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  margin: 0 auto;
  width: 100%;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
  
  height: 120rpx; 
  background-color: rgba(26, 24, 17, 0.98); 
  backdrop-filter: blur(20px); 
  border-top: 1rpx solid rgba(255, 255, 255, 0.1); 
  display: flex; 
  justify-content: space-around; 
  align-items: center; 
  padding-bottom: env(safe-area-inset-bottom); 
  z-index: 9999; 
  box-sizing: border-box; 
  pointer-events: auto;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 100%;
  transition: all 0.3s ease;
  
  &.center-item {
    position: relative;
    overflow: visible;
  }
  
  .center-icon {
    position: absolute;
    top: -40rpx;
    width: 110rpx;
    height: 110rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 8rpx solid #0a0a0a;
    z-index: 10001;
    
    &.bg-primary {
      background-color: #8B0000;
    }
    
    &.pulse-glow {
      box-shadow: 0 0 20rpx rgba(139, 0, 0, 0.6);
    }
  }
  
  .tab-text {
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.4);
    font-weight: 500;
  }
  
  &.active {
    .tab-text {
      color: #f9d406;
      font-weight: 700;
    }
  }
}

.bottom-space {
  height: 300rpx;
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .5; }
}
</style>
