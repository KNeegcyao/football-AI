<template>
  <view class="container" :class="themeClass">
    <!-- 顶部状态栏占位 -->
    <view class="status-bar"></view>

    <!-- 顶部导航 -->
    <view class="header sticky-header glass-effect">
      <view class="header-content">
        <view class="header-left">
          <view class="ai-status">
            <view class="status-dot animate-pulse"></view>
            <text class="status-text text-theme-secondary">Pulse AI 在线</text>
          </view>
        </view>
        <view class="header-center">
          <text class="title text-theme-main">Pulse AI 足球助手</text>
        </view>
        <view class="header-right">
          <text class="material-symbols-outlined text-theme-secondary" style="font-size: 40rpx;" @click="clearHistory">delete_sweep</text>
        </view>
      </view>
    </view>

    <!-- 聊天内容区域 -->
    <scroll-view class="chat-main" 
                 scroll-y 
                 :scroll-into-view="lastMessageId" 
                 scroll-with-animation
                 @scrolltoupper="loadMoreHistory">
      <view class="chat-list">
        <view v-for="(msg, index) in chatMessages" :key="index" 
              :id="'msg-' + index"
              :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']">
          <view class="avatar-box" :class="msg.role === 'user' ? 'user-avatar-bg' : 'ai-avatar-bg'">
            <image v-if="msg.role === 'user' && userAvatar" :src="userAvatar" class="avatar-img" mode="aspectFill"></image>
            <text v-else class="material-symbols-outlined" 
                  :style="{ color: msg.role === 'user' ? '#f9d406' : '#8B0000', fontSize: '32rpx' }">
              {{ msg.role === 'user' ? 'person' : 'smart_toy' }}
            </text>
          </view>
          <view class="msg-content-wrapper" :class="{ 'items-end': msg.role === 'user' }">
            <text class="role-label" :class="{ 'user-label': msg.role === 'user' }">
              {{ msg.role === 'user' ? 'YOU' : 'AI PULSE ASSISTANT' }}
              <text v-if="msg.isRag" class="rag-badge">RAG 增强</text>
            </text>
            <view class="msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
              <rich-text v-if="msg.role === 'ai'" :nodes="renderMarkdown(msg.content)" class="markdown-body"></rich-text>
              <text v-else class="message-text">{{ msg.content }}</text>
            </view>
          </view>
        </view>
        <!-- 正在输入状态 (仅在没有占位消息时显示，避免出现两个 AI 对话框) -->
        <view v-if="isTyping && chatMessages[chatMessages.length - 1]?.role === 'user'" class="message-item ai-message" id="msg-typing">
          <view class="avatar-box ai-avatar-bg">
            <text class="material-symbols-outlined" style="color: #8B0000; fontSize: 32rpx;">smart_toy</text>
          </view>
          <view class="msg-content-wrapper">
            <text class="role-label">AI PULSE ASSISTANT</text>
            <view class="msg-bubble ai-bubble">
              <view class="typing-indicator-dots">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area-fixed">
      <view class="quick-actions" v-if="chatMessages.length < 5">
        <scroll-view scroll-x class="quick-scroll" :show-scrollbar="false">
          <view class="quick-list">
            <view v-for="(action, index) in quickActions" :key="index" 
                  class="quick-item border-border-dark bg-card-dark" @click="handleQuickAction(action.title)">
              <text class="material-symbols-outlined text-primary" style="font-size: 28rpx;">{{ action.icon }}</text>
              <text class="quick-text text-slate-300">{{ action.title }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="input-wrapper glass-effect pulse-glow">
        <view class="mic-btn-wrapper" 
              @touchstart="startVoice" 
              @touchend="endVoice" 
              @touchcancel="endVoice">
          <text class="material-symbols-outlined mic-btn" 
                :class="{ 'recording': isRecording }"
                style="font-size: 40rpx;">{{ isRecording ? 'mic_active' : 'mic' }}</text>
        </view>
        <input class="chat-input text-slate-200" 
               placeholder="咨询 AI 助手..." 
               v-model="userInput" 
               @confirm="sendMessage"
               :disabled="isTyping || isRecording"
               placeholder-style="color: #64748b" />
        <view class="send-btn bg-primary" :class="{ 'opacity-50': isTyping || !userInput.trim() }" @click="sendMessage">
          <text class="send-btn-text">发送</text>
          <text class="material-symbols-outlined text-white" style="font-size: 28rpx;">send</text>
        </view>
      </view>
    </view>

    <!-- 录音状态浮层 -->
    <view v-if="isRecording" class="recording-overlay">
      <view class="recording-card">
        <view class="voice-waves">
          <view v-for="i in 5" :key="i" class="wave-bar" :style="{ animationDelay: (i * 0.1) + 's' }"></view>
        </view>
        <text class="recording-tip">正在录音...</text>
        <text class="recording-sub-tip">松开 发送</text>
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
import { ref, computed, nextTick } from 'vue'
import { onShow, onLoad } from '@dcloudio/uni-app'
import { useThemeStore } from '@/store/theme'
import { aiApi, userApi } from '@/api'
import { BASE_URL } from '@/utils/request'
import { marked } from 'marked'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const currentTab = ref(2) // AI 助手在索引 2
const lastMessageId = ref('')
const userInput = ref('')
const isTyping = ref(false)
const isRecording = ref(false)
const userProfile = ref(null)

// 录音管理器
let recorderManager = null

const initRecorder = () => {
  // #ifndef H5
  recorderManager = uni.getRecorderManager()
  recorderManager.onStart(() => {
    console.log('录音开始')
    isRecording.value = true
  })
  recorderManager.onStop(async (res) => {
    console.log('录音结束, 路径:', res.tempFilePath)
    isRecording.value = false
    await uploadVoice(res.tempFilePath)
  })
  recorderManager.onError((err) => {
    console.error('录音错误:', err)
    isRecording.value = false
    uni.showToast({ title: '录音失败', icon: 'none' })
  })
  // #endif
}

const startVoice = () => {
  if (isTyping.value) return
  // #ifdef H5
  uni.showModal({
    title: '提示',
    content: '语音录入功能需要原生的麦克风权限，建议在 App 或小程序环境中体验完整功能。',
    showCancel: false,
    confirmText: '我知道了'
  })
  return
  // #endif
  
  // 震动反馈
  uni.vibrateShort()
  
  recorderManager.start({
    duration: 60000,
    sampleRate: 16000,
    numberOfChannels: 1,
    encodeBitRate: 48000,
    format: 'mp3'
  })
}

const endVoice = () => {
  if (!isRecording.value) return
  recorderManager.stop()
}

const uploadVoice = async (tempFilePath) => {
  uni.showLoading({ title: '正在识别语音...' })
  try {
    // 调用后端 STT 接口
    const res = await uni.uploadFile({
      url: `${BASE_URL}/api/ai/stt`,
      filePath: tempFilePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${uni.getStorageSync('token')}`
      }
    })
    
    uni.hideLoading()
    
    if (res.statusCode === 200) {
      const data = JSON.parse(res.data)
      if (data.code === 200 && data.data) {
        userInput.value = data.data
        // 自动发送
        sendMessage()
      } else {
        uni.showToast({ title: data.msg || '语音识别失败', icon: 'none' })
      }
    } else {
      throw new Error('网络请求失败')
    }
  } catch (e) {
    uni.hideLoading()
    console.error('语音识别失败:', e)
    uni.showToast({ title: '语音识别失败', icon: 'none' })
  }
}

const fetchUserProfile = async () => {
  try {
    const res = await userApi.getProfile()
    if (res) {
      userProfile.value = res
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
}

const userAvatar = computed(() => {
  if (userProfile.value && userProfile.value.avatar) {
    if (userProfile.value.avatar.startsWith('http')) {
      return userProfile.value.avatar
    }
    // 使用 request.js 中定义的 BASE_URL，确保 IP 一致
    return `${BASE_URL}${userProfile.value.avatar.startsWith('/') ? '' : '/'}${userProfile.value.avatar}`
  }
  return ''
})

const renderMarkdown = (content) => {
  if (!content) return ''
  // 确保 marked 返回的 HTML 字符串可以被 rich-text 正确解析
  return marked.parse(content)
}

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
  { role: 'ai', content: '你好！我是你的 懂球帝AI助手。今晚的欧冠比赛你怎么看？我可以为你提供详细的战术分析或球员对比数据。' }
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
  const isRagSearch = !['生成机智回复', '战术分析'].includes(content) && 
                      !content.toLowerCase().includes('数据') && 
                      !content.toLowerCase().includes('对比')
  
  chatMessages.value.push({ 
    role: 'ai', 
    content: isRagSearch ? '正在实时检索足球知识库与最新资讯...' : '正在为您多维分析数据...',
    isRag: isRagSearch
  })
  scrollToBottom()
  
  try {
    let res
    const lowerContent = content.toLowerCase()
    
    // 智能分流与通用对话逻辑
    if (content === '生成机智回复') {
      // 这里的 content 只是触发词，实际生成需要上下文，暂取上一条用户消息或默认
      const lastUserMsg = chatMessages.value.filter(m => m.role === 'user').slice(-2, -1)[0]?.content || '这场比赛很精彩'
      res = await aiApi.generateComment({ content: lastUserMsg })
    } else if (content === '战术分析' || lowerContent.includes('战术')) {
      res = await aiApi.analyzeTactics({ info: content })
    } else if (lowerContent.includes('数据') || lowerContent.includes('对比') || lowerContent.includes('进球') || lowerContent.includes('助攻')) {
      res = await aiApi.queryData({ question: content })
    } else if (lowerContent.includes('战报') || lowerContent.includes('比分') || lowerContent.includes('比赛结果')) {
      res = await aiApi.generateMatchReport({ matchData: content })
    } else if (lowerContent.includes('预测') || lowerContent.includes('谁能赢') || lowerContent.includes('胜率')) {
      res = await aiApi.predictMatch({ teamA: '主队', teamB: '客队', recentForm: content })
    } else {
      // 默认使用集成 RAG 的通用对话
      res = await aiApi.chat({ question: content })
    }
    
    // 替换最后一条消息，res 直接就是返回的字符串内容
    const lastMsg = chatMessages.value[chatMessages.value.length - 1]
    lastMsg.content = res || '抱歉，我没有获取到有效回复。'
    lastMsg.isRag = isRagSearch
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

const clearHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空聊天记录吗？',
    success: (res) => {
      if (res.confirm) {
        chatMessages.value = [
          { role: 'ai', content: '聊天记录已清空。你好！我是你的 懂球帝AI助手。有什么我可以帮你的吗？' }
        ]
        scrollToBottom()
      }
    }
  })
}

const loadMoreHistory = () => {
  // 模拟加载历史记录
  console.log('加载更多历史记录...')
}

const scrollToBottom = () => {
  nextTick(() => {
    lastMessageId.value = ''
    nextTick(() => {
      if (isTyping.value) {
        lastMessageId.value = 'msg-typing'
      } else {
        const index = chatMessages.value.length - 1
        if (index >= 0) {
          lastMessageId.value = 'msg-' + index
        }
      }
    })
  })
}

onShow(() => {
  uni.hideTabBar()
})

onLoad(() => {
  initRecorder()
  fetchUserProfile()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #0a0a0a; // 强制使用深色背景
  margin: 0 auto;
  width: 100%;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
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
  height: 100rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

.header-left, .header-right {
  flex: 1;
  display: flex;
  align-items: center;
}

.header-right {
  justify-content: flex-end;
}

.header-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.ai-status {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: rgba(0, 255, 0, 0.05);
  padding: 8rpx 16rpx;
  border-radius: 100rpx;
  border: 1px solid rgba(0, 255, 0, 0.1);
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  background-color: #00ff00;
  border-radius: 50%;
  box-shadow: 0 0 10rpx #00ff00;
}

.status-text {
  font-size: 20rpx;
  font-weight: 600;
}

.title {
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.chat-main {
  flex: 1;
  padding: 32rpx;
  box-sizing: border-box;
  background-color: #f5f5f5;
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
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.ai-avatar-bg {
  background: #ffffff;
  border: 1px solid #eee;
}

.user-avatar-bg {
  background: #f9d406;
  border: 1px solid #e5c105;
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
  font-size: 20rpx;
  font-weight: 800;
  color: #8B0000;
  text-transform: uppercase;
  letter-spacing: 1rpx;
  margin-bottom: 4rpx;
  
  &.user-label {
    color: #333;
  }
}

.rag-badge {
  font-size: 16rpx;
  background: rgba(0, 255, 0, 0.1);
  color: #00ff00;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
  margin-left: 12rpx;
  border: 1px solid rgba(0, 255, 255, 0.2);
}

.msg-bubble {
  padding: 24rpx 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  
  &.ai-bubble {
    border-top-left-radius: 4rpx;
    background-color: #ffffff;
    color: #333;
    border: 1px solid #eee;
  }
  
  &.user-bubble {
    border-top-right-radius: 4rpx;
    background-color: #f9d406;
    color: #000000;
  }
}

.message-text {
  font-size: 28rpx;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

/* Markdown 样式 */
.markdown-body {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
}

.markdown-body :deep(h1), 
.markdown-body :deep(h2), 
.markdown-body :deep(h3) {
  font-weight: bold;
  margin-top: 24rpx;
  margin-bottom: 12rpx;
  color: #8B0000;
  display: block;
}

.markdown-body :deep(h1) { font-size: 36rpx; }
.markdown-body :deep(h2) { font-size: 32rpx; }
.markdown-body :deep(h3) { font-size: 30rpx; }

.markdown-body :deep(p) {
  margin-bottom: 16rpx;
  display: block;
}

.markdown-body :deep(ul), 
.markdown-body :deep(ol) {
  padding-left: 40rpx;
  margin-bottom: 16rpx;
  display: block;
}

.markdown-body :deep(li) {
  margin-bottom: 8rpx;
  display: list-item;
}

.markdown-body :deep(code) {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 4rpx 8rpx;
  border-radius: 4rpx;
  font-family: monospace;
}

.markdown-body :deep(strong) {
  font-weight: bold;
  color: #000;
}

/* 正在输入动画 */
.typing-indicator-dots {
  display: flex;
  gap: 8rpx;
  padding: 8rpx 4rpx;
  
  .dot {
    width: 12rpx;
    height: 12rpx;
    background-color: #8B0000;
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.input-area-fixed {
  position: fixed;
  bottom: 160rpx;
  left: 0;
  right: 0;
  margin: 0 auto;
  width: 100%;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
  
  padding: 0 32rpx;
  box-sizing: border-box;
  z-index: 99;
}

.quick-actions {
  margin-bottom: 24rpx;
  
  .quick-scroll {
    width: 100%;
    white-space: nowrap;
  }
  
  .quick-list {
    display: flex;
    gap: 16rpx;
    padding: 4rpx;
  }
  
  .quick-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    padding: 16rpx 28rpx;
    border-radius: 100rpx;
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(26, 26, 26, 0.8);
    backdrop-filter: blur(10px);
    flex-shrink: 0;
    
    &:active {
      background: rgba(139, 0, 0, 0.2);
      border-color: #8B0000;
    }
    
    .quick-text {
      font-size: 24rpx;
      font-weight: 500;
    }
  }
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background-color: #ffffff;
  border: 1px solid #ddd;
  padding: 12rpx 24rpx;
  border-radius: 100rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.mic-btn-wrapper {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  margin-right: 16rpx;
}

.mic-btn {
  color: #64748b;
  transition: all 0.2s;
}

.mic-btn.recording {
  color: #8B0000;
  transform: scale(1.2);
  animation: pulse-red 1s infinite;
}

@keyframes pulse-red {
  0% { transform: scale(1.2); opacity: 1; }
  50% { transform: scale(1.4); opacity: 0.7; }
  100% { transform: scale(1.2); opacity: 1; }
}

/* 录音浮层样式 */
.recording-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recording-card {
  background: rgba(255, 255, 255, 0.9);
  width: 300rpx;
  height: 300rpx;
  border-radius: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.2);
}

.voice-waves {
  display: flex;
  align-items: center;
  height: 80rpx;
  gap: 8rpx;
  margin-bottom: 30rpx;
}

.wave-bar {
  width: 8rpx;
  height: 20rpx;
  background: #8B0000;
  border-radius: 4rpx;
  animation: wave-grow 0.8s ease-in-out infinite;
}

@keyframes wave-grow {
  0%, 100% { height: 20rpx; }
  50% { height: 60rpx; }
}

.recording-tip {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.recording-sub-tip {
  font-size: 24rpx;
  color: #666;
}

.chat-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
  color: #333;
}

.send-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 32rpx;
  border-radius: 100rpx;
  background-color: #8B0000;
  transition: all 0.2s ease;
  
  &:active {
    transform: scale(0.95);
  }
}

.send-btn-text {
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 600;
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
