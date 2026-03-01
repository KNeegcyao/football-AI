<template>
  <view class="page-container bg-background-dark text-slate-100 min-h-screen font-display">
    <!-- Top Navigation Header -->
    <view class="header-sticky" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="header-left" v-if="isOthersProfile" @click="goBack">
          <text class="material-symbols-outlined text-2xl">arrow_back</text>
        </view>
        <view class="header-center">
          <text class="text-lg font-bold">{{ userInfo.nickname }}的战术板</text>
        </view>
        <view class="header-right" v-if="!isOthersProfile">
          <button class="action-icon-btn" @click="goToSettings">
            <text class="material-symbols-outlined text-2xl">settings</text>
          </button>
        </view>
        <view class="header-right" v-else>
          <!-- 占位，保持中间标题居中 -->
          <view style="width: 48rpx;"></view>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main-content" @scrolltolower="loadMore">
      <!-- Profile Header Section -->
      <view class="profile-header">
        <view class="cover-image-container" @click="chooseCover">
          <view class="cover-overlay"></view>
          <image v-if="userInfo.cover" class="cover-image" :src="userInfo.cover" mode="aspectFill"></image>
          <view v-else class="cover-placeholder"></view>
        </view>
        
        <view class="profile-info-card">
          <view class="avatar-section">
            <view class="avatar-wrapper">
              <view class="avatar-border">
                <image class="avatar-image" :src="userInfo.avatar" mode="aspectFill" @click="chooseAvatar"></image>
              </view>
              <view class="level-badge">
                <text class="material-symbols-outlined text-xs">workspace_premium</text>
                <text class="level-text">LV.{{ userInfo.level }}</text>
              </view>
            </view>
            
            <view class="identity-info">
              <view class="name-row">
                <text class="nickname">{{ userInfo.nickname }}</text>
                <text class="material-symbols-outlined text-primary text-xl">verified</text>
              </view>
              <text class="user-handle">{{ userInfo.bio || '资深战术分析师 | 足球死忠' }}</text>
              <text class="user-bio">{{ userInfo.description || '专注足球战术复盘，提供专业的比赛分析。绿茵场上的每一步都有逻辑。' }}</text>
            </view>

            <view class="action-buttons">
              <template v-if="!isOthersProfile">
                <button class="btn-primary" @click="goToEdit">编辑资料</button>
                <button class="btn-secondary" @click="logout">退出登录</button>
              </template>
              <template v-else>
                <button class="btn-primary" @click="handleFollow">关注</button>
                <button class="btn-secondary" @click="handleMessage">私信</button>
              </template>
            </view>
          </view>
        </view>
      </view>

      <!-- Stats Section -->
      <view class="stats-container">
        <view class="stat-item" @click="goToFollow('following')">
          <text class="stat-value">{{ formatStats(userInfo.stats.following) }}</text>
          <text class="stat-label">关注</text>
        </view>
        <view class="stat-item" @click="goToFollow('followers')">
          <text class="stat-value">{{ formatStats(userInfo.stats.followers) }}</text>
          <text class="stat-label">粉丝</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ formatStats(userInfo.stats.likesReceived) }}</text>
          <text class="stat-label">获赞</text>
        </view>
      </view>

      <!-- Tabs Navigation -->
      <view class="tabs-sticky">
        <view class="tabs-content">
          <view v-for="(tab, index) in profileTabs" :key="index" 
                class="tab-item" 
                :class="{ active: currentProfileTab === index }"
                @click="currentProfileTab = index">
            <text class="tab-text">{{ tab.name }} ({{ tab.count }})</text>
            <view class="tab-indicator" v-if="currentProfileTab === index"></view>
          </view>
        </view>
      </view>

      <!-- Content Feed -->
      <view class="content-feed">
        <!-- 动态列表 (Tab 0) -->
        <view v-if="currentProfileTab === 0">
          <view v-for="(post, index) in posts" :key="post.id" class="post-card" @click="goToPostDetail(post.id)">
            <view class="post-content-row">
              <view class="post-text-side">
                <text class="post-title">{{ post.title }}</text>
                <text class="post-excerpt">{{ post.content }}</text>
                <view class="post-meta">
                  <view class="meta-item">
                    <text class="material-symbols-outlined text-sm">favorite</text>
                    <text>{{ formatStats(post.likes) }}</text>
                  </view>
                  <view class="meta-item">
                    <text class="material-symbols-outlined text-sm">chat_bubble</text>
                    <text>{{ post.commentCount }}</text>
                  </view>
                  <text class="post-time">{{ formatTime(post.createTime) }}</text>
                </view>
              </view>
              <image v-if="post.image" class="post-image" :src="post.image" mode="aspectFill"></image>
            </view>
          </view>
          
          <u-loadmore :status="loadStatus" color="#64748b" v-if="posts.length > 0" />
          <view class="empty-state" v-if="posts.length === 0 && !loading">
            <text class="material-symbols-outlined empty-icon">description</text>
            <text>暂无动态</text>
          </view>
        </view>
        
        <!-- 收藏列表 (Tab 1) -->
        <view v-else-if="currentProfileTab === 1">
          <!-- 收藏二级分类 -->
          <view class="fav-sub-tabs">
            <view v-for="(subTab, subIdx) in favSubTabs" :key="subIdx" 
                  class="sub-tab-item" 
                  :class="{ active: currentFavSubTab === subIdx }"
                  @click="changeFavSubTab(subIdx)">
              <text>{{ subTab }}</text>
            </view>
          </view>

          <!-- 帖子收藏列表 -->
          <view v-if="currentFavSubTab === 0">
            <view v-for="(item, index) in favorites" :key="item.id" class="post-card" @click="goToPostDetail(item.id)">
              <view class="post-content-row">
                <view class="post-text-side">
                  <text class="post-title">{{ item.title }}</text>
                  <text class="post-excerpt">{{ item.content }}</text>
                  <view class="post-meta">
                    <view class="meta-item">
                      <text class="material-symbols-outlined text-sm">favorite</text>
                      <text>{{ formatStats(item.likes) }}</text>
                    </view>
                    <view class="meta-item">
                      <text class="material-symbols-outlined text-sm">chat_bubble</text>
                      <text>{{ item.commentCount }}</text>
                    </view>
                    <text class="post-time">{{ formatTime(item.createTime) }}</text>
                  </view>
                </view>
                <image v-if="item.image" class="post-image" :src="item.image" mode="aspectFill"></image>
              </view>
            </view>
          </view>

          <!-- 新闻收藏列表 -->
          <view v-else-if="currentFavSubTab === 1">
            <view v-for="(item, index) in favoriteNews" :key="item.id" class="post-card" @click="goToNewsDetail(item.id)">
              <view class="post-content-row">
                <view class="post-text-side">
                  <text class="post-title">{{ item.title }}</text>
                  <text class="post-excerpt">{{ item.content }}</text>
                  <view class="post-meta">
                    <text class="post-time">{{ formatTime(item.createTime) }}</text>
                  </view>
                </view>
                <image v-if="item.image" class="post-image" :src="item.image" mode="aspectFill"></image>
              </view>
            </view>
          </view>

          <!-- 球员收藏列表 -->
          <view v-else-if="currentFavSubTab === 2" class="player-grid">
            <view v-for="(player, index) in favoritePlayers" :key="player.id" class="player-card" @click="goToPlayerDetail(player.id)">
              <image class="player-avatar" :src="player.avatar || '/static/soccer-logo.png'" mode="aspectFill"></image>
              <text class="player-name">{{ player.name }}</text>
              <text class="player-position">{{ player.position }}</text>
            </view>
          </view>
          
          <u-loadmore :status="favLoadStatus" color="#64748b" v-if="getCurrentFavList().length > 0" />
          <view class="empty-state" v-if="getCurrentFavList().length === 0 && !favLoading">
            <text class="material-symbols-outlined empty-icon">bookmark</text>
            <text>暂无{{ favSubTabs[currentFavSubTab] }}收藏</text>
          </view>
        </view>
        
        <!-- 相册 (Tab 2) -->
        <view v-else-if="currentProfileTab === 2" class="empty-state">
          <text class="material-symbols-outlined empty-icon">photo_library</text>
          <text>暂无相册</text>
        </view>
      </view>
      
      <view class="footer-info">
        <text class="version-text">PitchPulse v2.4.0 (Build 503)</text>
      </view>
    </scroll-view>

    <!-- Bottom Navigation Bar -->
    <view class="tab-bar">
      <view v-for="(tab, index) in mainTabs" :key="index" class="tab-item" :class="{ active: currentMainTab === index }"
        @tap="handleMainTabClick(index)">
        <u-icon :name="tab.icon" :color="currentMainTab === index ? '#f9d406' : 'rgba(255, 255, 255, 0.4)'" size="24"></u-icon>
        <text class="tab-text">{{ tab.text }}</text>
      </view>
    </view>
    
    <!-- Logout Modal -->
    <u-modal
      :show="showLogoutModal"
      title="退出登录"
      content="确定要退出当前账号吗？"
      showCancelButton
      confirmColor="#f2b90d"
      @confirm="confirmLogout"
      @cancel="showLogoutModal = false"
    ></u-modal>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userApi, fileApi, postApi, favoriteApi, playerApi } from '@/api'
import { BASE_URL } from '@/utils/request'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight
const currentMainTab = ref(3)
const mainTabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar', path: 'pages/schedule/schedule' },
  { text: '社区', icon: 'chat', path: 'pages/community/community' },
  { text: '我的', icon: 'account', path: 'pages/my/my' }
]

const currentProfileTab = ref(0)
const profileTabs = ref([
  { name: '动态', count: 0 },
  { name: '收藏', count: 0 },
  { name: '相册', count: 0 }
])

const userInfo = ref({
  id: '',
  nickname: '加载中...',
  username: '',
  avatar: '/static/soccer-logo.png',
  cover: '',
  level: 1,
  bio: '',
  description: '',
  stats: {
    following: 0,
    followers: 0,
    likesReceived: 0
  }
})

// 添加格式化时间函数
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = (now.getTime() - date.getTime()) / 1000
  
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 2592000) return Math.floor(diff / 86400) + '天前'
  
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const isOthersProfile = ref(false)
const posts = ref([])
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const loadStatus = ref('loadmore')

// 收藏相关状态
const favorites = ref([])
const favoriteNews = ref([])
const favoritePlayers = ref([])
const favPage = ref(1)
const favLoading = ref(false)
const favLoadStatus = ref('loadmore')

// 收藏二级分类
const favSubTabs = ['帖子', '新闻', '球员']
const currentFavSubTab = ref(0)

const changeFavSubTab = (index) => {
  if (currentFavSubTab.value === index) return
  currentFavSubTab.value = index
  favPage.value = 1
  favorites.value = []
  favoriteNews.value = []
  favoritePlayers.value = []
  favLoadStatus.value = 'loadmore'
  loadFavorites()
}

const getCurrentFavList = () => {
  if (currentFavSubTab.value === 0) return favorites.value
  if (currentFavSubTab.value === 1) return favoriteNews.value
  return favoritePlayers.value
}

// 监听 tab 切换，切换到收藏时加载数据
import { watch } from 'vue'
watch(currentProfileTab, (newTab) => {
  if (newTab === 1 && getCurrentFavList().length === 0) {
    loadFavorites()
  }
})

const showLogoutModal = ref(false)

onShow(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  // 检查是否有 userId 参数，如果有则是查看别人主页
  const options = currentPage.options || {}
  isOthersProfile.value = !!options.userId && options.userId !== uni.getStorageSync('userInfo')?.id

  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  
  // 强制刷新主页标识
  currentMainTab.value = 3
  
  loadUserProfile(options.userId)
})

// 定义一个统一的头像处理函数
const getAvatarUrl = (avatar, type = 'avatar') => {
  if (!avatar) {
    return type === 'avatar' ? '/static/soccer-logo.png' : ''
  }
  
  // 处理后端返回的带 /uploads/ 的各种路径 (包括 localhost, 127.0.0.1 或旧 IP)
  if (avatar.includes('/uploads/')) {
    const relativePath = avatar.substring(avatar.indexOf('/uploads/'))
    return fileApi.getFileUrl(relativePath)
  }
  
  // 如果已经是完整 URL 且不含 /uploads/，则直接返回
  if (avatar.startsWith('http')) {
    return avatar
  }
  
  // 如果是相对路径
  return fileApi.getFileUrl(avatar)
}

const loadUserProfile = async (userId) => {
  try {
    const profileRes = userId ? await userApi.getProfile(userId) : await userApi.getProfile()
    if (profileRes) {
      // 使用统一函数处理头像
      const avatarUrl = getAvatarUrl(profileRes.avatar, 'avatar')
      const coverUrlFromRes = getAvatarUrl(profileRes.cover, 'cover')
      
      console.log('加载用户信息成功:', profileRes)
      console.log('处理后的头像 URL:', avatarUrl)
      console.log('处理后的封面 URL:', coverUrlFromRes)
      
      userInfo.value = {
        ...userInfo.value,
        id: profileRes.id,
        nickname: profileRes.nickname || profileRes.username,
        username: '@' + profileRes.username,
        avatar: avatarUrl,
        // 如果后端返回了有效的封面图，则更新；否则保留当前（可能是刚刚上传预览的）
        cover: coverUrlFromRes || userInfo.value.cover,
        level: profileRes.level || 1,
        bio: profileRes.bio || '资深战术分析师 | 足球死忠',
        description: profileRes.description || '专注足球战术复盘，提供专业的比赛分析。绿茵场上的每一步都有逻辑。'
      }
      if (!userId) {
        uni.setStorageSync('userInfo', profileRes)
      }
      
      // 在用户信息加载完成后加载动态
      page.value = 1
      posts.value = []
      loadStatus.value = 'loadmore'
      loadPosts()
    }

    const statsRes = userId ? await userApi.getStats(userId) : await userApi.getStats()
    if (statsRes) {
      userInfo.value.stats = {
        following: statsRes.followingCount || 0,
        followers: statsRes.followerCount || 0,
        likesReceived: statsRes.likeReceivedCount || 0
      }
      
      // Update profile tabs counts if needed
      profileTabs.value[0].count = statsRes.postCount || 0
      profileTabs.value[1].count = statsRes.favoriteCount || 0
      
      // 如果当前正在收藏页，刷新收藏数据
      if (currentProfileTab.value === 1) {
        favPage.value = 1
        favorites.value = []
        favLoadStatus.value = 'loadmore'
        loadFavorites()
      }
    }
  } catch (e) {
    console.error('加载用户信息失败:', e)
  }
}

const loadPosts = async () => {
  if (loading.value || loadStatus.value === 'nomore') return
  
  loading.value = true
  loadStatus.value = 'loading'
  
  try {
    const res = await postApi.getList({
      page: page.value,
      size: pageSize.value,
      userId: userInfo.value.id
    })
    
    if (res && res.records) {
      const newPosts = res.records.map(item => ({
        id: item.id,
        title: item.title,
        content: item.content,
        image: item.images && item.images.length > 0 ? fileApi.getFileUrl(item.images[0]) : '',
        createTime: item.createTime,
        likes: item.likes || 0,
        commentCount: item.commentCount || 0
      }))
      
      if (page.value === 1) {
        posts.value = newPosts
      } else {
        posts.value = [...posts.value, ...newPosts]
      }
      
      if (newPosts.length < pageSize.value) {
        loadStatus.value = 'nomore'
      } else {
        loadStatus.value = 'loadmore'
        page.value++
      }
    }
  } catch (e) {
    console.error('加载动态失败:', e)
    loadStatus.value = 'loadmore'
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (currentProfileTab.value === 0) {
    loadPosts()
  } else if (currentProfileTab.value === 1) {
    loadFavorites()
  }
}

const loadFavorites = async () => {
  if (favLoading.value || favLoadStatus.value === 'nomore') return
  
  favLoading.value = true
  favLoadStatus.value = 'loading'
  
  // 移除 HTML 标签的辅助函数
  const stripHtml = (html) => {
    if (!html) return ''
    return html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').trim()
  }
  
  try {
    let res
    if (currentFavSubTab.value === 0) {
      // 帖子收藏
      res = await favoriteApi.list({
        page: favPage.value,
        size: pageSize.value
      })
      if (res && res.records) {
        const newData = res.records.map(item => ({
          id: item.id,
          title: item.title,
          content: stripHtml(item.content),
          image: item.images && item.images.length > 0 ? fileApi.getFileUrl(item.images[0]) : '',
          createTime: item.createTime,
          likes: item.likes || 0,
          commentCount: item.commentCount || 0
        }))
        favorites.value = favPage.value === 1 ? newData : [...favorites.value, ...newData]
        updateFavLoadStatus(newData.length)
      }
    } else if (currentFavSubTab.value === 1) {
      // 新闻收藏
      res = await favoriteApi.listNews({
        page: favPage.value,
        size: pageSize.value
      })
      if (res && res.records) {
        const newData = res.records.map(item => ({
          id: item.id,
          title: item.title,
          content: stripHtml(item.summary || item.content).substring(0, 100),
          image: item.image ? fileApi.getFileUrl(item.image) : '',
          createTime: item.createTime
        }))
        favoriteNews.value = favPage.value === 1 ? newData : [...favoriteNews.value, ...newData]
        updateFavLoadStatus(newData.length)
      }
    } else {
        // 球员收藏
        res = await favoriteApi.listPlayers({
          page: favPage.value,
          size: pageSize.value
        })
        if (res && res.records) {
          // 由于后端返回的是 Map 结构且可能只包含 ID，我们需要补充获取球员详情
          const newData = await Promise.all(res.records.map(async (item) => {
            try {
              // 尝试从 SportAPI 获取详情 (如果 playerId 是 7M/雷速的 ID)
              const playerDetailRes = await playerApi.getSportApiDetail(item.playerId)
              if (playerDetailRes) {
                // 处理两种可能的后端返回结构 (local DB vs API fallback)
                let p = null
                if (playerDetailRes.player) {
                  p = playerDetailRes.player
                } else if (playerDetailRes.response && playerDetailRes.response.length > 0) {
                  p = playerDetailRes.response[0].player
                }
                
                if (p) {
                  const playerAvatar = p.photo || p.photoUrl || p.avatar
                  return {
                    id: item.playerId,
                    name: p.name || p.displayName || p.nickname || '未知球员',
                    position: p.position || '球员',
                    avatar: playerAvatar ? getAvatarUrl(playerAvatar) : `https://images.fotmob.com/image_resources/playerimages/${item.playerId}.png`
                  }
                }
              }
            } catch (err) {
              console.warn(`获取球员详情失败 [ID: ${item.playerId}]:`, err)
            }
            
            // 如果详情接口报错或无数据，返回基础信息
            return {
              id: item.playerId,
              name: item.name || '球员',
              position: item.position || '未知位置',
              avatar: item.avatar ? getAvatarUrl(item.avatar) : `https://images.fotmob.com/image_resources/playerimages/${item.playerId}.png`
            }
          }))
          
          favoritePlayers.value = favPage.value === 1 ? newData : [...favoritePlayers.value, ...newData]
          updateFavLoadStatus(newData.length)
        }
      }
  } catch (e) {
    console.error('加载收藏失败:', e)
    favLoadStatus.value = 'loadmore'
  } finally {
    favLoading.value = false
  }
}

const updateFavLoadStatus = (count) => {
  if (count < pageSize.value) {
    favLoadStatus.value = 'nomore'
  } else {
    favLoadStatus.value = 'loadmore'
    favPage.value++
  }
}

const goToNewsDetail = (id) => {
  uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
}

const goToPlayerDetail = (id) => {
  uni.navigateTo({ url: `/pages/player-detail/player-detail?id=${id}` })
}

const handleMainTabClick = (index) => {
  const tab = mainTabs[index]
  if (currentMainTab.value === index) return
  uni.switchTab({ url: '/' + tab.path })
}

const handleFollow = () => {
  uni.showToast({ title: '关注成功', icon: 'success' })
}

const handleMessage = () => {
  uni.showToast({ title: '私信功能开发中', icon: 'none' })
}

const goToSettings = () => {
  uni.navigateTo({ url: '/pages/my/settings' })
}

const goToFollow = (type) => {
  const tab = type === 'following' ? 0 : 1
  uni.navigateTo({ 
    url: `/pages/my/follow?tab=${tab}&userId=${userInfo.value.id}` 
  })
}

const goToEdit = () => {
  uni.navigateTo({ url: '/pages/my/edit' })
}

const goToPostDetail = (id) => {
  uni.navigateTo({ url: `/pages/post/detail?id=${id}` })
}

const goBack = () => {
  uni.navigateBack()
}

const chooseAvatar = () => {
  if (isOthersProfile.value) return
  uni.chooseImage({
    count: 1,
    success: (res) => {
      uploadFile(res.tempFilePaths[0], 'avatar')
    }
  })
}

const chooseCover = () => {
  if (isOthersProfile.value) return
  uni.chooseImage({
    count: 1,
    success: (res) => {
      uploadFile(res.tempFilePaths[0], 'cover')
    }
  })
}

const uploadFile = (path, type) => {
  uni.showLoading({ title: '上传中...' })
  console.log('开始上传文件:', path, '类型:', type)
  uni.uploadFile({
    url: BASE_URL + (fileApi.uploadUrl.startsWith('/') ? fileApi.uploadUrl : '/' + fileApi.uploadUrl),
    filePath: path,
    name: 'file',
    header: { 
      'Authorization': 'Bearer ' + uni.getStorageSync('token'),
      'Accept': 'application/json'
    },
    success: (res) => {
      console.log('上传原始响应:', res.data, '状态码:', res.statusCode)
      if (res.statusCode !== 200) {
        uni.showToast({ title: '上传失败: ' + res.statusCode, icon: 'none' })
        return
      }
      if (!res.data) {
        uni.showToast({ title: '服务器返回空响应', icon: 'none' })
        return
      }
      try {
        const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
        if (data.code === 200 || data.code === 0) {
          const fileUrl = data.data?.url || data.data || data.url
          if (fileUrl) {
            updateProfile({ [type]: fileUrl })
          } else {
            uni.showToast({ title: '返回数据中未找到文件路径', icon: 'none' })
          }
        } else {
          uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
        }
      } catch (e) {
        console.error('解析上传响应失败:', e, '原始数据:', res.data)
        uni.showToast({ title: '服务器响应格式错误', icon: 'none' })
      }
    },
    fail: (err) => {
      console.error('上传文件失败:', err)
      uni.showToast({ title: '上传失败: ' + (err.errMsg || '网络错误'), icon: 'none' })
    },
    complete: () => uni.hideLoading()
  })
}

const updateProfile = async (data) => {
  try {
    await userApi.updateProfile(data)
    loadUserProfile()
  } catch (e) {
    uni.showToast({ title: '更新失败', icon: 'none' })
  }
}

const logout = () => {
  showLogoutModal.value = true
}

const confirmLogout = () => {
  uni.clearStorageSync()
  uni.reLaunch({ url: '/pages/login/login' })
}

const formatStats = (num) => {
  if (num >= 10000) return (num / 1000).toFixed(1) + 'K'
  return num
}
</script>

<style lang="scss" scoped>
.page-container {
  display: flex;
  flex-direction: column;
  background-color: #12110a;
}

.header-sticky {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(18, 17, 10, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(242, 185, 13, 0.1);
}

.header-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.action-icon-btn {
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  color: #f2b90d;
  display: flex;
  align-items: center;
  justify-content: center;
  &::after { border: none; }
}

.main-content {
  flex: 1;
  height: 0;
}

.profile-header {
  position: relative;
  margin-bottom: 40rpx;
}

.cover-image-container {
  height: 384rpx;
  position: relative;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(18, 17, 10, 0.4) 0%, #12110a 100%),
              url('https://images.unsplash.com/photo-1574629810360-7efbbe195018?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80');
  background-size: cover;
  background-position: center;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, #12110a, transparent);
  z-index: 1;
}

.profile-info-card {
  padding: 0 32rpx;
  margin-top: -128rpx;
  position: relative;
  z-index: 2;
}

.avatar-wrapper {
  position: relative;
  width: 192rpx;
  height: 192rpx;
  margin-bottom: 32rpx;
}

.avatar-border {
  padding: 8rpx;
  background-color: #f2b90d;
  border-radius: 50%;
  width: 100%;
  height: 100%;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 8rpx solid #12110a;
}

.level-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  background-color: #f2b90d;
  color: #12110a;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 20rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
}

.identity-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.nickname {
  font-size: 48rpx;
  font-weight: bold;
}

.user-handle {
  color: rgba(242, 185, 13, 0.7);
  font-size: 28rpx;
  font-weight: 500;
}

.user-bio {
  color: #94a3b8;
  font-size: 28rpx;
  margin-top: 16rpx;
  max-width: 600rpx;
}

.action-buttons {
  display: flex;
  gap: 24rpx;
  margin-top: 48rpx;
}

.btn-primary {
  flex: 1;
  height: 88rpx;
  background-color: #f2b90d;
  color: #12110a;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(242, 185, 13, 0.2);
}

.btn-secondary {
  flex: 1;
  height: 88rpx;
  background-color: #1e1b10;
  color: #f1f5f9;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: 1px solid rgba(242, 185, 13, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stats-container {
  display: flex;
  gap: 24rpx;
  padding: 48rpx 32rpx;
}

.stat-item {
  flex: 1;
  background-color: #1e1b10;
  padding: 32rpx;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid rgba(242, 185, 13, 0.05);
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
}

.stat-label {
  font-size: 24rpx;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 2rpx;
  margin-top: 8rpx;
}

.tabs-sticky {
  position: sticky;
  top: 88rpx;
  z-index: 90;
  background-color: #12110a;
}

.tabs-content {
  display: flex;
  padding: 0 32rpx;
  border-bottom: 1px solid rgba(242, 185, 13, 0.1);
}

.tab-item {
  flex: 1;
  padding: 32rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  opacity: 0.5;
  &.active { opacity: 1; }
}

.tab-text {
  font-size: 28rpx;
  font-weight: bold;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 80rpx;
  height: 4rpx;
  background-color: #f2b90d;
}

.content-feed {
  padding: 32rpx;
}

.post-card {
  background-color: #1e1b10;
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  border: 1px solid rgba(242, 185, 13, 0.05);
}

.post-content-row {
  display: flex;
  gap: 24rpx;
}

.post-text-side {
  flex: 1;
}

.post-title {
  font-size: 32rpx;
  font-weight: bold;
  line-height: 1.4;
  margin-bottom: 16rpx;
}

.post-excerpt {
  font-size: 28rpx;
  color: #94a3b8;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 32rpx;
}

.post-image {
  width: 192rpx;
  height: 192rpx;
  border-radius: 24rpx;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 32rpx;
  font-size: 24rpx;
  color: #64748b;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.post-time {
  margin-left: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 128rpx 0;
  color: #64748b;
  gap: 16rpx;
}

.empty-icon {
  font-size: 96rpx;
  opacity: 0.2;
}.footer-info {
    padding: 64rpx 0 160rpx; /* 确保内容不被 120rpx 的导航栏遮挡 */
    text-align: center;
  }

  /* 收藏二级分类样式 */
  .fav-sub-tabs {
    display: flex;
    padding: 20rpx 32rpx;
    gap: 20rpx;
    background-color: #12110a;
    
    .sub-tab-item {
      padding: 12rpx 32rpx;
      border-radius: 30rpx;
      background-color: rgba(255, 255, 255, 0.05);
      color: rgba(255, 255, 255, 0.6);
      font-size: 24rpx;
      transition: all 0.3s;
      
      &.active {
        background-color: #f9d406;
        color: #000;
        font-weight: bold;
      }
    }
  }

  /* 球员卡片网格样式 */
  .player-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    padding: 20rpx 32rpx;
    
    .player-card {
      background-color: rgba(255, 255, 255, 0.05);
      border-radius: 16rpx;
      padding: 24rpx 16rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      transition: all 0.3s;
      
      &:active {
        background-color: rgba(255, 255, 255, 0.1);
      }
      
      .player-avatar {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50%;
        margin-bottom: 16rpx;
        border: 2rpx solid rgba(249, 212, 6, 0.3);
      }
      
      .player-name {
        font-size: 26rpx;
        color: #fff;
        font-weight: bold;
        text-align: center;
        margin-bottom: 4rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        width: 100%;
      }
      
      .player-position {
        font-size: 20rpx;
        color: rgba(255, 255, 255, 0.4);
      }
    }
  }

  .version-text {
  font-size: 20rpx;
  color: #334155;
  letter-spacing: 4rpx;
  text-transform: uppercase;
}

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
  
  height: 120rpx; /* 统一高度为 120rpx */
  background-color: rgba(26, 24, 17, 0.98); /* 统一深色背景 */
  backdrop-filter: blur(20px); /* 开启毛玻璃效果 */
  border-top: 1rpx solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-around;
  align-items: center;
  
  /* 适配安全区域 */
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  
  z-index: 9999;
  box-sizing: border-box;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  
  .tab-text {
    font-size: 20rpx;
    color: rgba(255, 255, 255, 0.4);
    font-weight: 500;
  }
  
  &.active {
    .tab-text {
      color: #f9d406; /* 选中颜色统一为金黄色 */
      font-weight: 700;
    }
  }
}

/* Material Icons Support */
.material-symbols-outlined {
  font-family: 'MaterialIcons' !important;
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}
</style>
