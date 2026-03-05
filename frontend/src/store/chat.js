import { defineStore } from 'pinia'
import { chatApi } from '@/api'

export const useChatStore = defineStore('chat', {
  state: () => ({
    socket: null,
    connected: false,
    sessions: [],
    messages: {}, // { sessionId: [messages] }
    currentSessionId: null
  }),

  actions: {
    connect() {
      if (this.socket || this.connected) {
        console.log('WebSocket is already connected or connecting')
        return
      }

      const url = chatApi.getWsUrl()
      console.log('Connecting to WebSocket:', url)
      this.socket = uni.connectSocket({
        url,
        complete: (res) => {
          console.log('uni.connectSocket complete:', res)
        }
      })

      this.socket.onOpen(() => {
        this.connected = true
        console.log('WebSocket Connected')
        // 连接成功后可以尝试刷新会话列表
        this.fetchSessions()
      })

      this.socket.onMessage((res) => {
        try {
          const message = JSON.parse(res.data)
          this.handleNewMessage(message)
        } catch (e) {
          console.error('WebSocket message parse error:', e, res.data)
        }
      })

      this.socket.onClose(() => {
        this.connected = false
        this.socket = null
        console.log('WebSocket Closed')
        // 自动重连
        setTimeout(() => this.connect(), 5000)
      })

      this.socket.onError((err) => {
        console.error('WebSocket Error:', err)
        this.connected = false
        this.socket = null
      })
    },

    disconnect() {
      if (this.socket) {
        this.socket.close()
        this.socket = null
        this.connected = false
      }
    },

    handleNewMessage(message) {
      const { sessionId, senderId } = message
      
      let userInfo = uni.getStorageSync('userInfo')
      if (userInfo && typeof userInfo === 'string') {
        try {
          userInfo = JSON.parse(userInfo)
        } catch (e) {
          console.error('Failed to parse userInfo in chat store:', e)
        }
      }
      
      const currentUserId = userInfo ? userInfo.id : null
      
      // 更新消息列表
      if (!this.messages[sessionId]) {
        this.messages[sessionId] = []
      }
      
      // 避免重复添加 (特别是发送者的回执消息)
      const isDuplicate = this.messages[sessionId].some(m => m.id === message.id)
      if (!isDuplicate) {
        this.messages[sessionId].push(message)
      }

      // 如果不是当前正在聊天的会话，且消息是发给我的，增加未读数
      if (this.currentSessionId !== sessionId && Number(message.receiverId) === Number(currentUserId)) {
        const session = this.sessions.find(s => s.id === sessionId)
        if (session) {
          session.unreadCount++
          session.lastMessage = message.content
          session.lastMessageTime = message.createdAt
        } else {
          // 如果会话不在列表中，刷新列表
          this.fetchSessions()
        }
      } else if (this.currentSessionId === sessionId) {
          // 如果是当前会话，更新最后一条消息
          const session = this.sessions.find(s => s.id === sessionId)
          if (session) {
            session.lastMessage = message.content
            session.lastMessageTime = message.createdAt
          }
      }

      // 触发全局事件，供页面局部更新
      uni.$emit('newChatMessage', message)
    },

    async fetchSessions() {
      try {
        const res = await chatApi.getSessions()
        this.sessions = res || []
      } catch (e) {
        console.error('Fetch sessions failed:', e)
      }
    },

    async fetchMessages(sessionId, page = 1) {
      try {
        const res = await chatApi.getMessages(sessionId, { page, size: 20 })
        if (res && res.records) {
          // 后端返回的是按时间倒序，前端显示需要正序
          const newMessages = res.records.reverse()
          if (page === 1) {
            this.messages[sessionId] = newMessages
          } else {
            this.messages[sessionId] = [...newMessages, ...this.messages[sessionId]]
          }
          return res
        }
      } catch (e) {
        console.error('Fetch messages failed:', e)
      }
      return null
    },

    sendMessage(receiverId, content, type = 0) {
      if (!this.connected || !this.socket) {
        uni.showToast({ title: '网络未连接', icon: 'none' })
        return
      }

      const payload = {
        receiverId,
        content,
        type
      }

      this.socket.send({
        data: JSON.stringify(payload)
      })
    },

    setCurrentSession(sessionId) {
      this.currentSessionId = sessionId
      if (sessionId) {
        const session = this.sessions.find(s => s.id === sessionId)
        if (session) {
          session.unreadCount = 0
        }
      }
    }
  }
})
