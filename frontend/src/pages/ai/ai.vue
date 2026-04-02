<template>
  <view class="container" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- 固定顶部容器 -->
    <view class="fixed-header">
      <!-- Status Bar Placeholder -->
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

      <!-- Header (Aligned with index.vue) -->
      <view class="nav-bar bg-nav-bar border-b border-theme-main">
        <view class="header-content">
          <view class="header-left">
            <view class="ai-status">
              <view class="status-dot animate-pulse"></view>
              <text class="status-text text-theme-secondary">Pulse AI 在线</text>
            </view>
          </view>
          <view class="header-center">
            <text class="title text-theme-main">AI 智能助手</text>
          </view>
          <view class="header-right">
            <view class="clear-btn" @tap="clearChat">
              <text class="material-icons" style="font-size: 48rpx;">delete</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 顶部占位 -->
    <view class="header-placeholder"></view>

    <!-- 聊天记录区域 -->
    <scroll-view 
      class="chat-content" 
      scroll-y 
      :scroll-top="scrollTop" 
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
      @scroll="onScroll"
      @scrolltoupper="onScrollToUpper"
      upper-threshold="50"
    >
      <view class="message-list">
        <!-- 历史加载状态 -->
        <view v-if="isHistoryLoading" class="history-loading">
          <view class="loading-spinner small"></view>
          <text class="loading-text">正在加载历史记录...</text>
        </view>
        <view v-for="(msg, index) in messageList" :key="msg.id || index" :id="'msg-' + index" :class="['message-item', msg.role]">
          <view class="avatar" v-if="msg.role === 'ai'">
            <text class="material-icons" style="font-size: 48rpx; color: #f9d406;">smart_toy</text>
          </view>
          <view class="bubble-container">
            <view class="bubble">
              <!-- 用户上传的图片 -->
              <view v-if="msg.image" class="msg-image-wrapper" @tap="previewMsgImage(msg.image)">
                <image :src="msg.image" mode="widthFix" class="msg-image" style="max-width: 100%; border-radius: 12rpx; margin-bottom: 10rpx; display: block;"></image>
              </view>
              <rich-text v-if="msg.role === 'ai'" :nodes="renderMarkdown(msg.content)" class="markdown-content"></rich-text>
              <text v-else-if="msg.content" class="text">{{ msg.content }}</text>
            </view>
          </view>
          <view class="avatar" v-if="msg.role === 'user'">
            <text class="material-icons" style="font-size: 48rpx; color: var(--text-main); opacity: 0.6;">person</text>
          </view>
        </view>
        
        <!-- Loading 状态 -->
        <view v-if="isLoading" id="loading-msg" class="message-item ai">
          <view class="avatar">
            <text class="material-icons" style="font-size: 48rpx; color: #f9d406;">smart_toy</text>
          </view>
          <view class="bubble-container">
            <view class="bubble loading-bubble">
              <view class="dot-loader">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 占位，确保内容不被输入框遮挡 -->
        <view class="bottom-placeholder"></view>
      </view>
    </scroll-view>

    <!-- 底部输入区域 -->
    <view class="input-area-container glass-effect border-theme-main">
      <!-- 图片预览区域 -->
      <view v-if="selectedImage" class="image-preview-bar">
        <view class="preview-item">
          <image :src="selectedImage" mode="aspectFill" class="preview-img" @tap="previewSelectedImage"></image>
          <view class="remove-btn" @tap="removeSelectedImage">
            <text class="material-icons" style="font-size: 44rpx;">close</text>
          </view>
          <view v-if="isUploading" class="upload-mask">
            <view class="loading-spinner"></view>
          </view>
        </view>
      </view>

      <view class="input-wrapper">
        <!-- 上传图片按钮 -->
        <view class="action-btn" @tap="chooseImage">
          <text class="material-icons" style="font-size: 48rpx; color: var(--text-main); opacity: 0.8;">image</text>
        </view>

        <textarea
          class="chat-input text-theme-main"
          v-model="inputValue"
          placeholder="输入您的问题..."
          auto-height
          :maxlength="500"
          @confirm="sendMessage"
          :adjust-position="true"
        />
        
        <!-- 停止生成按钮 -->
        <view v-if="isLoading" class="stop-btn" @tap="stopAiResponse">
          <text class="material-icons" style="font-size: 56rpx; color: #ff2e63;">close</text>
        </view>
        
        <!-- 发送按钮 -->
        <view v-else :class="['send-btn', !inputValue.trim() && !selectedImage ? 'disabled' : '']" @tap="sendMessage">
          <text class="material-icons" :style="{ fontSize: '48rpx', color: !inputValue.trim() && !selectedImage ? '#ccc' : '#f9d406' }">send</text>
        </view>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <CustomTabBar :currentTab="2" />
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { aiApi } from '@/api'
import { getFullImageUrl } from '@/utils/request'
import { useThemeStore } from '@/store/theme'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { marked } from 'marked'

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true,
  headerIds: false,
  mangle: false
})

const renderMarkdown = (content) => {
  if (!content) return ''
  return marked.parse(content)
}

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const statusBarHeight = ref(0)
onMounted(() => {
  statusBarHeight.value = uni.getSystemInfoSync().statusBarHeight || 0
})

const inputValue = ref('')
const messageList = ref([
  { role: 'ai', content: '您好！我是足球智能助手，您可以问我关于赛程、球员或者是战术分析的问题。' }
])
const isLoading = ref(false)
const isUploading = ref(false)
const selectedImage = ref('')
const uploadedFileId = ref('')
const currentTaskId = ref('')
const scrollTop = ref(0)
const scrollIntoView = ref('')
const conversationId = ref(uni.getStorageSync('last_conversation_id') || '')
const hasMore = ref(false)
const isHistoryLoading = ref(false)
const firstId = ref('')

const getUserId = () => {
  return uni.getStorageSync('userInfo')?.id || uni.getStorageSync('guest_id') || 'guest-unknown'
}

// 获取历史消息
const loadHistory = async (isLoadMore = false) => {
  if (!conversationId.value || isHistoryLoading.value) return
  
  try {
    const userId = getUserId()
    if (!userId) return

    isHistoryLoading.value = true
    const res = await difyApi.getMessages(conversationId.value, userId, isLoadMore ? firstId.value : null)
    
    if (res && res.data) {
      hasMore.value = res.has_more
      if (res.data.length > 0) {
        firstId.value = res.data[0].id // 记录当前页的第一条 ID，用于下次加载更多
      }

      // Dify 返回的是倒序（最新的在前），我们需要正序显示
      const history = []
      res.data.reverse().forEach(msg => {
        // 添加用户消息
        const userMsg = {
          role: 'user',
          content: msg.query,
          id: msg.id + '_user'
        }
        if (msg.message_files && msg.message_files.length > 0) {
          const userFile = msg.message_files.find(f => f.belongs_to === 'user')
          if (userFile) userMsg.image = userFile.url
        }
        history.push(userMsg)

        // 添加 AI 消息
        const aiMsg = {
          role: 'ai',
          content: msg.answer,
          id: msg.id + '_ai'
        }
        if (msg.message_files && msg.message_files.length > 0) {
          const aiFile = msg.message_files.find(f => f.belongs_to === 'assistant')
          if (aiFile) aiMsg.image = aiFile.url
        }
        history.push(aiMsg)
      })

      if (isLoadMore) {
        // 加载更多：插入到列表前面
        messageList.value = [...history, ...messageList.value]
      } else if (history.length > 0) {
        // 初始加载且有记录：替换列表
        messageList.value = history
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    // 如果是因为会话不存在导致的加载失败，清除本地会话 ID
    if (error.message && (error.message.includes('404') || error.message.includes('Conversation Not Exists'))) {
      console.warn('检测到历史会话 ID 已失效，清除本地缓存')
      conversationId.value = ''
      uni.removeStorageSync('last_conversation_id')
    }
  } finally {
    isHistoryLoading.value = false
  }
}

const onScrollToUpper = () => {
  if (hasMore.value && !isHistoryLoading.value) {
    loadHistory(true)
  }
}



// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      selectedImage.value = res.tempFilePaths[0]
      await uploadImage(selectedImage.value)
    }
  })
}

// 上传图片到 Dify
const uploadImage = async (filePath) => {
  if (isUploading.value) return
  isUploading.value = true
  
  try {
    const userId = getUserId()
    console.log('开始上传图片, filePath:', filePath, 'userId:', userId)
    const res = await difyApi.uploadFile(filePath, userId)
    console.log('图片上传结果:', res)
    if (res && res.id) {
      uploadedFileId.value = res.id
      uni.showToast({ title: '图片上传成功', icon: 'success' })
    } else {
      throw new Error(res?.msg || '上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    uni.showToast({ title: error.message || '图片上传失败', icon: 'none' })
    removeSelectedImage()
  } finally {
    isUploading.value = false
  }
}

// 预览已选图片
const previewSelectedImage = () => {
  if (!selectedImage.value) return
  uni.previewImage({
    urls: [selectedImage.value]
  })
}

// 预览消息中的图片
const previewMsgImage = (url) => {
  uni.previewImage({
    urls: [url]
  })
}

// 移除已选图片
const removeSelectedImage = () => {
  selectedImage.value = ''
  uploadedFileId.value = ''
}

// 停止 AI 响应
const stopAiResponse = async () => {
  if (!currentTaskId.value) return
  try {
    const userId = uni.getStorageSync('userInfo')?.id || uni.getStorageSync('guest_id')
    await difyApi.stopResponse(currentTaskId.value, userId)
    isLoading.value = false
    uni.showToast({ title: '已停止响应', icon: 'none' })
  } catch (error) {
    console.error('停止响应失败:', error)
  }
}

const clearChat = () => {
  uni.showModal({
    title: '提示',
    content: '确定清空所有聊天记录吗？',
    success: (res) => {
      if (res.confirm) {
        messageList.value = [{ role: 'ai', content: '对话已清空。您可以开始新的咨询了。' }]
        conversationId.value = ''
        uni.removeStorageSync('last_conversation_id')
      }
    }
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (isLoading.value) {
      scrollIntoView.value = 'loading-msg'
    } else if (messageList.value.length > 0) {
      scrollIntoView.value = 'msg-' + (messageList.value.length - 1)
    }
    // 同时保留 scrollTop 的更新作为兼容性
    scrollTop.value = scrollTop.value + 1000 
  })
}

const sendMessage = async () => {
  if ((!inputValue.value.trim() && !uploadedFileId.value) || isLoading.value || isUploading.value) return

  const userMsg = inputValue.value.trim()
  const fileId = uploadedFileId.value
  const imageUrl = selectedImage.value
  
  inputValue.value = ''
  removeSelectedImage()
  
  // 1. 添加用户消息
  messageList.value.push({
    role: 'user',
    content: userMsg || (fileId ? '[图片]' : ''),
    image: imageUrl
  })
  
  scrollToBottom()
  
  // 2. 设置加载状态
  isLoading.value = true
  
  try {
    const userId = getUserId()

    // 3. 准备发送数据，包含文件信息
    const sendData = {
      message: userMsg,
      conversationId: conversationId.value,
      userId: userId
    }

    if (fileId) {
      sendData.files = [
        {
          type: 'image',
          transfer_method: 'local_file',
          upload_file_id: fileId
        }
      ]
    }

    // 4. 调用后端接口
    // AI 分析图片可能需要较长时间，设置超时时间为 2 分钟 (120000ms)
    const res = await request.post('/api/ai/chat', sendData, {}, { timeout: 120000 })

    if (res && (res.answer || res.data)) {
      const difyRes = res.answer ? res : { answer: res.data, ...res }
      console.log('AI 对话响应:', difyRes)
      
      // 处理会话不存在的特殊错误
      if (difyRes.answer && difyRes.answer.includes('Conversation Not Exists')) {
        console.warn('检测到会话 ID 已失效，清除本地缓存')
        conversationId.value = ''
        uni.removeStorageSync('last_conversation_id')
        throw new Error('会话已失效，请重试')
      }

      if (difyRes && difyRes.answer) {
        // 保存任务 ID 以便停止响应
        currentTaskId.value = difyRes.task_id || ''
        
        // 模拟打字机效果
        const fullAnswer = difyRes.answer
        const aiMsg = {
          role: 'ai',
          content: '',
          id: Date.now() + '_ai'
        }
        messageList.value.push(aiMsg)
        
        const aiMsgIndex = messageList.value.length - 1
        
        let currentText = ''
        const typingSpeed = 20 // 稍微加快一点速度，更流畅
        
        for (let i = 0; i < fullAnswer.length; i++) {
          if (!isLoading.value) break // 如果用户停止了响应，则终止打字
          currentText += fullAnswer[i]
          messageList.value[aiMsgIndex].content = currentText
          scrollToBottom()
          await new Promise(resolve => setTimeout(resolve, typingSpeed))
        }
        
        // 记录会话ID，用于下次对话维持上下文
        conversationId.value = difyRes.conversation_id || conversationId.value
        if (difyRes.conversation_id) {
          uni.setStorageSync('last_conversation_id', difyRes.conversation_id)
        }
      } else {
        throw new Error('AI 返回内容为空')
      }
    } else {
      throw new Error('服务响应异常')
    }
  } catch (error) {
    console.error('AI 对话失败:', error)
    messageList.value.push({
      role: 'ai',
      content: error.message || '抱歉，系统繁忙，请稍后再试。'
    })
  } finally {
    isLoading.value = false
    currentTaskId.value = ''
    scrollToBottom()
  }
}

const onScroll = (e) => {
  scrollTop.value = e.detail.scrollTop
}

onShow(() => {
  uni.hideTabBar()
})

onMounted(() => {
  // 确保有 userId，用于 Dify API
  if (!uni.getStorageSync('userInfo')?.id && !uni.getStorageSync('guest_id')) {
    uni.setStorageSync('guest_id', 'guest-' + Math.random().toString(36).substring(2, 15))
  }
  
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  if (currentPage) {
    const route = '/' + currentPage.route
    // 兼容路径前缀比较
    const index = tabs.value.findIndex(tab => ('/' + tab.path) === route)
    if (index !== -1) {
      currentTab.value = index
    }
  }

  if (conversationId.value) {
    loadHistory()
  } else {
    scrollToBottom()
  }
})
</script>

<style lang="scss" scoped>
/* 固定顶部容器 */
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: var(--nav-bar-bg);
  
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
}

.header-placeholder {
  height: calc(var(--status-bar-height) + 100rpx);
  width: 100%;
}

.nav-bar {
  height: 100rpx;
  width: 100%;
  background-color: var(--nav-bar-bg);
  display: flex;
  align-items: center;
}

/* 历史加载状态 */
.history-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 30rpx 0;
  
  .loading-text {
    font-size: 24rpx;
    color: var(--text-secondary);
    opacity: 0.6;
  }
}

.loading-spinner {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid rgba(249, 212, 6, 0.2);
  border-top: 3rpx solid #f9d406;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  
  &.small {
    width: 30rpx;
    height: 30rpx;
    border-width: 2rpx;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-main);
  margin: 0 auto;
  width: 100%;
  overflow: hidden;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
}

.header-content {
  width: 100%;
  height: 100rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ai-status {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: rgba(249, 212, 6, 0.1);
  padding: 8rpx 20rpx;
  border-radius: 100rpx;
  border: 1px solid rgba(249, 212, 6, 0.2);
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  background-color: #f9d406;
  border-radius: 50%;
  box-shadow: 0 0 10rpx #f9d406;
}

.status-text {
  font-size: 20rpx;
  font-weight: 600;
  color: #f9d406;
}

.title {
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
  color: var(--text-main);
}

.clear-btn {
  color: var(--text-secondary);
  opacity: 0.6;
  .material-icons {
    font-size: 44rpx;
  }
}

/* 聊天内容区域 */
.chat-content {
  flex: 1;
  width: 100%;
  background-color: var(--bg-main);
  overflow: hidden;
}

.message-list {
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.message-item {
  display: flex;
  width: 100%;
  
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-secondary);
    border: 1px solid var(--border-color);
    
    image {
      width: 48rpx;
      height: 48rpx;
    }
  }

  .bubble-container {
    max-width: 75%;
    display: flex;
    flex-direction: column;
  }

  .bubble {
    padding: 24rpx 28rpx;
    border-radius: 28rpx;
    font-size: 28rpx;
    line-height: 1.6;
    word-break: break-all;

    .markdown-content {
      color: inherit;
      font-size: 28rpx;
      line-height: 1.6;
      
      :deep(p) {
        margin-bottom: 12rpx;
        &:last-child { margin-bottom: 0; }
      }
      
      :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
        margin: 16rpx 0 8rpx;
        font-weight: bold;
        color: #f9d406;
      }
      
      :deep(h1) { font-size: 34rpx; }
      :deep(h2) { font-size: 32rpx; }
      :deep(h3) { font-size: 30rpx; }
      
      :deep(ul), :deep(ol) {
        padding-left: 32rpx;
        margin-bottom: 12rpx;
      }
      
      :deep(li) {
        margin-bottom: 4rpx;
      }
      
      :deep(code) {
        background: rgba(255, 255, 255, 0.1);
        padding: 4rpx 8rpx;
        border-radius: 4rpx;
        font-family: monospace;
        margin: 0 4rpx;
      }
      
      :deep(pre) {
        background: rgba(0, 0, 0, 0.3);
        padding: 20rpx;
        border-radius: 12rpx;
        margin: 16rpx 0;
        overflow-x: auto;
        
        code {
          background: transparent;
          padding: 0;
          margin: 0;
          display: block;
        }
      }

      :deep(strong) {
        color: #f9d406;
        font-weight: bold;
      }

      :deep(blockquote) {
        border-left: 4px solid #f9d406;
        padding-left: 20rpx;
        margin: 12rpx 0;
        color: var(--text-secondary);
        opacity: 0.7;
      }
    }
  }

  &.ai {
    justify-content: flex-start;
    gap: 20rpx;
    
    .bubble {
      background: var(--bg-secondary);
      color: var(--text-main);
      border: 1px solid var(--border-color);
      border-top-left-radius: 4rpx;
    }
    
    .avatar {
      background: rgba(249, 212, 6, 0.1);
      border: 1px solid rgba(249, 212, 6, 0.2);
    }
  }

  &.user {
    justify-content: flex-end;
    gap: 20rpx;
    
    .bubble {
      background: #f9d406;
      color: #000000;
      font-weight: 600;
      border-top-right-radius: 4rpx;
      box-shadow: 0 4rpx 20rpx rgba(249, 212, 6, 0.3);
    }
    
    .avatar {
      background: var(--bg-secondary);
    }
  }
}

/* Loading 动画 */
.loading-bubble {
  padding: 24rpx 32rpx !important;
}

.dot-loader {
  display: flex;
  gap: 8rpx;
  
  .dot {
    width: 12rpx;
    height: 12rpx;
    background: #f9d406;
    border-radius: 50%;
    animation: dot-pulse 1.4s infinite ease-in-out;
    
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes dot-pulse {
  0%, 80%, 100% { transform: scale(0); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* 底部输入框 */
.input-area-container {
  padding: 20rpx 30rpx calc(20rpx + 140rpx); // 留出底部导航栏位置
  background-color: var(--nav-bar-bg);
  border-top: 1px solid var(--border-color);
  
  .image-preview-bar {
    display: flex;
    padding-bottom: 20rpx;
    
    .preview-item {
      position: relative;
      width: 120rpx;
      height: 120rpx;
      border-radius: 12rpx;
      overflow: hidden;
      border: 1px solid var(--border-color);
      
      .preview-img {
        width: 100%;
        height: 100%;
      }
      
      .remove-btn {
        position: absolute;
        top: 4rpx;
        right: 4rpx;
        width: 36rpx;
        height: 36rpx;
        background: rgba(0, 0, 0, 0.6);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .material-icons {
          font-size: 44rpx;
          color: #fff;
        }
      }
      
      .upload-mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.4);
        display: flex;
        align-items: center;
        justify-content: center;
        
        .loading-spinner {
          width: 32rpx;
          height: 32rpx;
          border: 2rpx solid #f9d406;
          border-top-color: transparent;
          border-radius: 50%;
          animation: spin 0.8s linear infinite;
        }
      }
    }
  }

  .input-wrapper {
    background: var(--bg-secondary);
    border: 1px solid var(--border-color);
    border-radius: 40rpx;
    padding: 12rpx 24rpx;
    display: flex;
    align-items: flex-end;
    gap: 16rpx;

    .action-btn {
      padding: 12rpx 0;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--text-main);
      opacity: 0.6;
      
      &:active {
        color: #f9d406;
        opacity: 1;
      }
    }

    .chat-input {
      flex: 1;
      min-height: 40rpx;
      max-height: 200rpx;
      background-color: var(--bg-secondary);
      border-radius: 12rpx;
      padding: 18rpx 24rpx;
      font-size: 28rpx;
      line-height: 1.4;
      border: 1px solid var(--border-color);
    }

    .stop-btn {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16rpx;
      background: rgba(255, 46, 99, 0.1);
      transition: all 0.2s;
      
      &:active {
        transform: scale(0.95);
        background: rgba(255, 46, 99, 0.2);
      }
    }

    .send-btn {
      width: 80rpx;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16rpx;
      background: rgba(249, 212, 6, 0.1);
      transition: all 0.2s;
      
      &:active:not(.disabled) {
        transform: scale(0.95);
        background: rgba(249, 212, 6, 0.2);
      }
      
      &.disabled {
        opacity: 0.5;
      }
      
      .material-icons {
        font-size: 44rpx;
      }
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 底部导航占位 */
.bottom-placeholder {
  height: calc(120rpx + env(safe-area-inset-bottom));
}
</style>
