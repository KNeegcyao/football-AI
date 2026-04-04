<template>
  <view class="container" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- 顶部固定区域 -->
    <view class="fixed-header">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="nav-bar">
        <view class="nav-left">
      <view class="action-btn-circle" @click="goBack" v-if="canGoBack">
        <text class="material-icons" style="font-size: 40rpx; color: #fff;">arrow_back</text>
      </view>
      <view class="action-btn-circle" @click="showHistory" v-else>
        <text class="material-icons" style="font-size: 40rpx; color: #fff;">history</text>
      </view>
    </view>
    <view class="nav-center">
      <text class="logo-text italic">
        PITCH<text class="highlight">PULSE</text>
      </text>
      <view class="ai-badge-new">
        <text class="material-icons" style="font-size: 24rpx; color: #d4af37;">auto_awesome</text>
        <text class="ai-text">AI</text>
      </view>
    </view>
    <view class="nav-right" style="gap: 16rpx;">
      <view class="action-btn-circle" @click="newChat">
        <text class="material-icons" style="font-size: 40rpx; color: #fff;">chat_bubble_outline</text>
      </view>
      <view class="action-btn-circle" @click="clearChat">
        <text class="material-icons" style="font-size: 40rpx; color: #fff;">delete_outline</text>
      </view>
    </view>
      </view>
    </view>

    <!-- 聊天内容区域 -->
    <scroll-view 
      scroll-y 
      class="chat-content" 
      :scroll-top="scrollTop"
      :scroll-with-animation="!isTyping"
    >
      <view class="header-placeholder"></view>
      
      <view class="message-list">
        <!-- 欢迎消息与快捷指令 -->
    <view class="message assistant" v-if="chatList.length === 0">
      <view class="avatar-box">
        <image src="/static/soccer-logo.png" mode="aspectFit" class="avatar-img"></image>
      </view>
      <view class="msg-bubble">
        <text class="msg-text">你好！我是 PitchPulse AI 助手。你可以问我关于足球赛事、战术分析、球员数据等任何问题。</text>
        
        <!-- 快捷指令 -->
        <view class="quick-prompts-container">
          <view class="prompt-chip" @click="sendQuickPrompt('分析最新一轮英超焦点战')">
            <view class="chip-content">
              <text class="material-icons chip-icon">analytics</text>
              <text class="chip-text">分析英超焦点战</text>
            </view>
            <text class="material-icons chip-arrow">chevron_right</text>
          </view>
          <view class="prompt-chip" @click="sendQuickPrompt('预测今晚欧冠的胜负走势')">
            <view class="chip-content">
              <text class="material-icons chip-icon">timeline</text>
              <text class="chip-text">预测欧冠走势</text>
            </view>
            <text class="material-icons chip-arrow">chevron_right</text>
          </view>
          <view class="prompt-chip" @click="sendQuickPrompt('帮我对比梅西和C罗的数据')">
            <view class="chip-content">
              <text class="material-icons chip-icon">compare_arrows</text>
              <text class="chip-text">球员数据对比</text>
            </view>
            <text class="material-icons chip-arrow">chevron_right</text>
          </view>
        </view>
      </view>
    </view>

        <!-- 聊天记录 -->
        <view v-for="(msg, index) in chatList" :key="index" :class="['message', msg.role]">
          <view class="avatar-box" v-if="msg.role === 'assistant'">
            <image src="/static/default-avatar.png" mode="aspectFit" class="avatar-img"></image>
          </view>
          <view class="avatar-box" v-else>
            <image :src="userAvatar" mode="aspectFill" class="avatar-img" @error="handleAvatarError"></image>
          </view>
          
      <view class="msg-bubble">
        <!-- 用户消息保持纯文本 -->
        <text class="msg-text" v-if="msg.role === 'user'">{{ msg.content }}</text>
        <!-- AI 消息使用 Markdown 渲染 -->
        <rich-text class="msg-text markdown-body" v-else :nodes="renderMarkdown(msg.content)"></rich-text>
      </view>
        </view>

        <!-- 加载中动画 -->
        <view class="message assistant" v-if="isLoading">
          <view class="avatar-box">
            <image src="/static/default-avatar.png" mode="aspectFit" class="avatar-img"></image>
          </view>
          <view class="msg-bubble loading-bubble">
            <text class="loading-dot">.</text>
            <text class="loading-dot">.</text>
            <text class="loading-dot">.</text>
          </view>
        </view>
      </view>
      
      <!-- 底部留白，防止被输入框和TabBar遮挡 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部输入区域 -->
    <view class="chat-input-bar">
      <view class="input-wrapper">
        <input 
          class="chat-input" 
          v-model="inputText" 
          placeholder="问问 AI..." 
          placeholder-class="input-placeholder"
          @confirm="sendMessage"
          :adjust-position="false"
        />
        <view class="send-btn" :class="{ 'active': inputText.trim() && !isLoading && !isTyping }" @click="sendMessage">
          <text class="material-icons" style="font-size: 40rpx;">send</text>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <CustomTabBar :currentTab="2" />

    <!-- 历史会话侧边栏/抽屉 -->
    <view class="history-overlay" v-if="historyVisible" @click="closeHistory"></view>
    <view class="history-drawer" :class="{ 'show': historyVisible }">
      <view class="drawer-header">
        <text class="drawer-title">历史会话</text>
        <view class="close-btn" @click="closeHistory">
          <text class="material-icons">close</text>
        </view>
      </view>
      <scroll-view 
        class="history-list" 
        scroll-y 
        @scrolltolower="loadMoreHistory"
      >
        <view class="history-empty" v-if="!historyLoading && historyList.length === 0">
          <text class="material-icons" style="font-size: 80rpx; color: rgba(255,255,255,0.2); margin-bottom: 16rpx;">chat_bubble_outline</text>
          <text>暂无历史会话</text>
        </view>
        <view 
          class="history-item" 
          v-for="(item, index) in historyList" 
          :key="item.id"
          :class="{ 'active': item.id === conversationId }"
          @click="selectConversation(item)"
        >
          <view class="history-content">
            <text class="history-name">{{ item.name || '新会话' }}</text>
            <text class="history-time">{{ formatTime(item.created_at) }}</text>
          </view>
          <view class="delete-btn" @click.stop="deleteConversation(item, index)">
            <text class="material-icons">delete_outline</text>
          </view>
        </view>
        <view class="loading-more" v-if="historyLoading">
          <text class="loading-dot">.</text>
          <text class="loading-dot">.</text>
          <text class="loading-dot">.</text>
        </view>
        <view class="no-more" v-if="!historyLoading && !hasMoreHistory && historyList.length > 0">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import { userApi, difyApi } from '@/api'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { marked } from 'marked'

// 状态定义
const statusBarHeight = ref(20)
const chatList = ref([])
const inputText = ref('')
const isLoading = ref(false)
const isTyping = ref(false) // 是否正在打字
let typingIntervalId = null // 用于保存打字机定时器
const scrollTop = ref(0)
const userAvatar = ref('/static/default-avatar.png')
const conversationId = ref('') // 用于保存 Dify 会话 ID
const canGoBack = ref(false) // 是否可以返回
const userId = ref('guest-unknown') // 用于标识当前用户，默认 guest-unknown
const historyVisible = ref(false) // 是否显示历史会话抽屉
const historyList = ref([]) // 历史会话列表
const historyLoading = ref(false) // 历史会话加载状态
const hasMoreHistory = ref(false) // 是否有更多历史会话
const lastHistoryId = ref(null) // 分页加载下一页的凭证

// 获取状态栏高度生命周期
onMounted(() => {
  // 获取状态栏高度以适配不同机型
  const sysInfo = uni.getSystemInfoSync()
  if (sysInfo.statusBarHeight) {
    statusBarHeight.value = sysInfo.statusBarHeight
  }
  loadUserProfile()
  
  // 检查是否有上一页，决定是否显示返回按钮
  const pages = getCurrentPages()
  canGoBack.value = pages.length > 1
})

// 返回上一页
const goBack = () => {
  if (canGoBack.value) {
    uni.navigateBack()
  }
}

// 查看历史会话
const showHistory = async () => {
  historyVisible.value = true
  lastHistoryId.value = null
  historyList.value = []
  hasMoreHistory.value = true
  await fetchHistory()
}

// 获取历史会话列表
const fetchHistory = async () => {
  if (historyLoading.value || !hasMoreHistory.value) return
  historyLoading.value = true
  try {
    const res = await difyApi.getConversations(userId.value, lastHistoryId.value, 20)
    if (res && res.data) {
      const list = res.data
      if (list.length > 0) {
        historyList.value.push(...list)
        lastHistoryId.value = list[list.length - 1].id
        hasMoreHistory.value = res.has_more
      } else {
        hasMoreHistory.value = false
      }
    }
  } catch (error) {
    console.error('获取历史会话失败:', error)
    uni.showToast({ title: '获取历史会话失败', icon: 'none' })
  } finally {
    historyLoading.value = false
  }
}

// 加载更多历史
const loadMoreHistory = () => {
  if (hasMoreHistory.value) {
    fetchHistory()
  }
}

// 关闭历史抽屉
const closeHistory = () => {
  historyVisible.value = false
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  // 假设 timestamp 是秒级，需转换为毫秒
  const date = new Date(timestamp * 1000)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
}

// 选择会话加载聊天记录
const selectConversation = async (item) => {
  if (item.id === conversationId.value) {
    closeHistory()
    return
  }
  if (typingIntervalId) {
    clearInterval(typingIntervalId)
    typingIntervalId = null
    isTyping.value = false
  }
  conversationId.value = item.id
  chatList.value = []
  isLoading.value = true
  closeHistory()
  
  try {
    const res = await difyApi.getMessages(item.id, userId.value, null, 100)
    if (res && res.data) {
      // Dify 的 messages 是倒序返回的（最新的在前面），所以我们需要 reverse 一下
      const messages = res.data.reverse()
      const formattedList = []
      for (const msg of messages) {
        formattedList.push({ role: 'user', content: msg.query })
        formattedList.push({ role: 'assistant', content: msg.answer })
      }
      chatList.value = formattedList
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取会话详情失败:', error)
    uni.showToast({ title: '加载聊天记录失败', icon: 'none' })
  } finally {
    isLoading.value = false
  }
}

// 删除会话
const deleteConversation = (item, index) => {
  uni.showModal({
    title: '删除会话',
    content: '确定要删除该会话吗？',
    confirmColor: '#d4af37',
    success: async (res) => {
      if (res.confirm) {
        try {
          await difyApi.deleteConversation(item.id, userId.value)
          historyList.value.splice(index, 1)
          if (conversationId.value === item.id) {
            newChat()
          }
          uni.showToast({ title: '删除成功', icon: 'success' })
        } catch (error) {
          console.error('删除会话失败:', error)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 新建会话
const newChat = () => {
  if (typingIntervalId) {
    clearInterval(typingIntervalId)
    typingIntervalId = null
    isTyping.value = false
  }
  chatList.value = []
  conversationId.value = ''
  uni.showToast({ title: '已开启新会话', icon: 'none' })
}

// 发送快捷指令
const sendQuickPrompt = (text) => {
  inputText.value = text
  sendMessage()
}

// 加载用户信息获取头像
const loadUserProfile = async () => {
  try {
    const res = await userApi.getProfile()
    if (res && res.avatar) {
      userAvatar.value = res.avatar
    }
    if (res && res.id) {
      userId.value = String(res.id)
    }
  } catch (e) {
    console.error('Failed to load profile', e)
  }
}

// 头像加载失败处理
const handleAvatarError = () => {
  userAvatar.value = '/static/default-avatar.png'
}

// 清空聊天记录
const clearChat = () => {
  if (chatList.value.length === 0) return
  uni.showModal({
    title: '清空对话',
    content: '确定要清空所有对话记录吗？',
    confirmColor: '#d4af37',
    success: (res) => {
      if (res.confirm) {
        if (typingIntervalId) {
          clearInterval(typingIntervalId)
          typingIntervalId = null
          isTyping.value = false
        }
        chatList.value = []
        conversationId.value = '' // 清空 Dify 会话 ID
      }
    }
  })
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    // 赋予极大值，使用微小差异触发视图更新，避免 Math.random() 导致的潜在渲染抖动
    scrollTop.value = scrollTop.value === 999999 ? 999998 : 999999
  })
}

// 渲染 Markdown
const renderMarkdown = (text) => {
  if (!text) return ''
  let html = marked(text)
  // 优化排版：调整段落和列表间距，减少空白
  html = html.replace(/<p>/g, '<p style="margin: 0 0 16rpx 0; padding: 0; line-height: 1.6; letter-spacing: 0.5px;">')
             .replace(/<ul>/g, '<ul style="margin: 0 0 16rpx 0; padding-left: 32rpx;">')
             .replace(/<ol>/g, '<ol style="margin: 0 0 16rpx 0; padding-left: 32rpx;">')
             .replace(/<li>/g, '<li style="margin-bottom: 8rpx;">')
             .replace(/<strong>/g, '<strong style="color: #d4af37; font-weight: bold;">') // 加粗文字使用主题色高亮
             .replace(/<pre>/g, '<pre style="background: rgba(0,0,0,0.3); padding: 12px; border-radius: 8px; overflow-x: auto; margin: 12px 0; border: 1px solid rgba(255,255,255,0.05);">')
             .replace(/<code>/g, '<code style="background: rgba(255,255,255,0.1); padding: 2px 6px; border-radius: 4px; font-family: monospace; color: #d4af37; font-size: 0.9em;">')
  return html
}

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isLoading.value || isTyping.value) return

  // 1. 本地展示用户消息
  chatList.value.push({ role: 'user', content: text })
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  // 2. 发起后端请求 (对接 Dify)
  try {
    const payload = { 
      message: text,
      conversationId: conversationId.value || null,
      userId: userId.value
    }
    const res = await request.post('/api/ai/chat', payload)
    
    isLoading.value = false // 隐藏 loading 动画

    if (res && res.answer) {
      // 3. 实现打字机效果
      isTyping.value = true
      const assistantMsg = { role: 'assistant', content: '' }
      chatList.value.push(assistantMsg)
      
      const fullAnswer = res.answer
      let currentIndex = 0
      
      // 动态计算每次追加的字符数，保证长回复能在合理时间内（约1.5秒）打完，减少持续重绘带来的闪烁
      const charsPerFrame = Math.max(2, Math.ceil(fullAnswer.length / 50))
      
      typingIntervalId = setInterval(() => {
        if (currentIndex < fullAnswer.length) {
          const charsToAdd = fullAnswer.substring(currentIndex, currentIndex + charsPerFrame)
          assistantMsg.content += charsToAdd
          currentIndex += charsPerFrame
          
          // 关闭了打字期间的动画后，直接调用触底不会引起抖动，同时能保证最新内容可见
          scrollToBottom()
        } else {
          clearInterval(typingIntervalId)
          typingIntervalId = null
          isTyping.value = false
          scrollToBottom()
        }
      }, 30) // 30ms 间隔

      // 保存会话 ID，以便 Dify 保持上下文
      if (res.conversation_id) {
        conversationId.value = res.conversation_id
      }
    } else {
      chatList.value.push({ role: 'assistant', content: '抱歉，服务未返回有效内容。' })
      scrollToBottom()
    }
  } catch (error) {
    console.error('AI Chat Error:', error)
    isLoading.value = false
    chatList.value.push({ role: 'assistant', content: error.message || '网络错误，请稍后再试。' })
    scrollToBottom()
  }
}
</script>

<style lang="scss" scoped>
/* Skill 要求的主题色变量 */
$pitch-pulse-bg-dark: #121212;
$pitch-pulse-primary: #d4af37;

.container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $pitch-pulse-bg-dark;
  overflow: hidden;
  position: relative;
}

/* 顶部固定区域 */
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 999;
  background-color: #1a1a1a;
  
  /* H5 居中适配 */
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
}

.status-bar {
  width: 100%;
}

.nav-bar {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  box-sizing: border-box;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.nav-left, .nav-right {
  flex: 1;
  display: flex;
  align-items: center;
}
.nav-left {
  justify-content: flex-start;
}
.nav-right {
  justify-content: flex-end;
}

.nav-center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.logo-icon {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
}
.logo-img {
  width: 100%;
  height: 100%;
}

.logo-text {
  font-size: 36rpx;
  font-weight: 800;
  color: $pitch-pulse-primary;
  &.italic {
    font-style: italic;
  }
  .highlight {
    color: #fff;
    margin-left: 6rpx;
  }
}

.ai-badge-new {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3) 0%, rgba(212, 175, 55, 0.1) 100%);
  padding: 4rpx 16rpx;
  border-radius: 24rpx;
  border: 1px solid rgba(212, 175, 55, 0.8); /* 强化边框使其更明显 */
  gap: 8rpx;
  box-shadow: 0 0 12rpx rgba(212, 175, 55, 0.4);
}

.ai-text {
  font-size: 20rpx;
  font-weight: 800;
  color: $pitch-pulse-primary;
  letter-spacing: 1rpx;
}

.action-btn-circle {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.05);
  transition: all 0.2s;
  position: relative;
  
  /* 增加 hitSlop 扩展点击区域至少 44pt (88rpx) */
  &::after {
    content: '';
    position: absolute;
    top: -8rpx;
    bottom: -8rpx;
    left: -8rpx;
    right: -8rpx;
  }
  
  &:active {
    background-color: rgba(255, 255, 255, 0.1);
    transform: scale(0.95);
  }
}

/* 聊天内容区域 */
.chat-content {
  flex: 1;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  
  /* H5 居中适配 */
  /* #ifdef H5 */
  margin: 0 auto;
  max-width: 500px;
  /* #endif */
}

.header-placeholder {
  /* nav-bar 高度 100rpx + 状态栏高度 */
  height: calc(100rpx + var(--status-bar-height, 20px));
}

.bottom-placeholder {
  /* 110rpx 是 tabbar 高度，140rpx 是输入框高度，并加上安全区 */
  height: calc(140rpx + 110rpx + env(safe-area-inset-bottom));
}

.message-list {
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 16rpx; /* 缩小头像和气泡之间的间距，节省空间 */
  width: 100%;
  animation: messageFadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes messageFadeIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  flex-direction: row-reverse;
}

.avatar-box {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  overflow: hidden;
  background-color: #2a2a2a;
  flex-shrink: 0;
  border: 2rpx solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
}
.avatar-img {
  width: 100%;
  height: 100%;
}

.msg-bubble {
  max-width: 85%; /* 扩大消息气泡的最大宽度，解决太窄的问题 */
  padding: 24rpx 30rpx;
  border-radius: 32rpx;
  word-break: break-all;
  transition: all 0.3s ease;
}

.message.assistant .msg-bubble {
  background: linear-gradient(145deg, #2a2a2a, #1f1f1f); /* 使用明显的渐变代替可能失效的透明度和高斯模糊 */
  border-top-left-radius: 8rpx;
  border: 1px solid rgba(212, 175, 55, 0.3); /* 加入项目主色的边框 */
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.4);
}

.message.user .msg-bubble {
  background-color: $pitch-pulse-primary;
  border-top-right-radius: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.2);
}

.msg-text {
  font-size: 30rpx;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.95);
  white-space: pre-wrap; /* 支持文本内换行 */
  letter-spacing: 0.5px;
  text-align: justify; /* 文本两端对齐，减少右侧参差不齐的空白 */
}
.message.user .msg-text {
  color: rgba(0, 0, 0, 0.9);
  font-weight: 500;
}

/* 底部输入框 */
.chat-input-bar {
  position: fixed;
  left: 0;
  width: 100%;
  z-index: 100;
  /* 位于 tabBar(高度110rpx) 的正上方，并叠加安全区 */
  bottom: calc(110rpx + env(safe-area-inset-bottom)); 
  padding: 20rpx 30rpx;
  box-sizing: border-box;
  background-color: #1a1a1a;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  
  /* H5 居中适配 */
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
}

.input-wrapper {
  display: flex;
  align-items: center;
  background-color: #222;
  border: 1px solid rgba(212, 175, 55, 0.5); /* 加入项目主色的边框 */
  border-radius: 40rpx;
  padding: 10rpx 10rpx 10rpx 16rpx;
  height: 80rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.4);
}

/* 新增功能样式 */
.quick-prompts-container {
  margin-top: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  width: 100%;
}

.prompt-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  transition: all 0.2s;
  width: 100%;
  box-sizing: border-box;
}

.prompt-chip:active {
  background: rgba(212, 175, 55, 0.15);
  border-color: rgba(212, 175, 55, 0.4);
  transform: scale(0.98);
}

.chip-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0; /* 解决 flex 子项溢出截断问题 */
}

.chip-icon {
  font-size: 36rpx;
  color: #D4AF37;
  flex-shrink: 0;
}

.chip-text {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  flex: 1;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chip-arrow {
  font-size: 40rpx;
  color: rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.chat-input {
  flex: 1;
  height: 100%;
  color: #fff;
  font-size: 28rpx;
  background: transparent;
  padding-left: 10rpx;
}

.input-placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.send-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: transparent;
  color: rgba(255, 255, 255, 0.3);
  transition: all 0.3s;
  position: relative;
  
  /* 增加 hitSlop 扩展点击区域至少 44pt (88rpx) */
  &::after {
    content: '';
    position: absolute;
    top: -12rpx;
    bottom: -12rpx;
    left: -12rpx;
    right: -12rpx;
  }
}

.send-btn.active {
  background-color: $pitch-pulse-primary;
  color: #000;
  box-shadow: 0 4rpx 12rpx rgba(212, 175, 55, 0.3);
  transform: scale(1.05);
}

/* Loading 动画 */
.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 24rpx 30rpx !important;
}

.loading-dot {
  font-size: 40rpx;
  line-height: 1;
  color: $pitch-pulse-primary;
  animation: loading 1.4s infinite both;
}

.loading-dot:nth-child(1) { animation-delay: -0.32s; }
.loading-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes loading {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

/* 历史记录抽屉 */
.history-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.history-drawer {
  position: fixed;
  top: 0;
  left: -80%; /* 默认隐藏在左侧 */
  width: 80%;
  max-width: 600rpx;
  height: 100%;
  background: #1a1a1a;
  z-index: 1001;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 10rpx 0 30rpx rgba(0, 0, 0, 0.5);
  border-right: 1px solid rgba(212, 175, 55, 0.2);
}

.history-drawer.show {
  transform: translateX(100%);
}

.drawer-header {
  height: calc(100rpx + var(--status-bar-height, 20px));
  padding-top: var(--status-bar-height, 20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 30rpx;
  padding-right: 30rpx;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.drawer-title {
  font-size: 34rpx;
  font-weight: bold;
  color: $pitch-pulse-primary;
}

.close-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.2s;
  &:active {
    background: rgba(255, 255, 255, 0.1);
  }
}

.history-list {
  flex: 1;
  overflow: hidden;
  padding: 20rpx;
  box-sizing: border-box;
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 100rpx;
  color: rgba(255, 255, 255, 0.4);
  font-size: 28rpx;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid transparent;
  transition: all 0.2s;
  
  &.active {
    background: rgba(212, 175, 55, 0.1);
    border-color: rgba(212, 175, 55, 0.3);
  }
  
  &:active {
    transform: scale(0.98);
    background: rgba(255, 255, 255, 0.05);
  }
}

.history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.history-name {
  font-size: 28rpx;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-time {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.4);
}

.delete-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.3);
  margin-left: 16rpx;
  border-radius: 50%;
  
  &:active {
    color: #ff4d4f;
    background: rgba(255, 77, 79, 0.1);
  }
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: 20rpx 0;
}

.no-more {
  text-align: center;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.3);
  padding: 20rpx 0;
}
</style>
