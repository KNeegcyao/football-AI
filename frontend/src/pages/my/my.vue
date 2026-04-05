<template>
  <view class="page-container font-display" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- 顶部导航栏 -->
    <view class="header-container">
      <!-- Status Bar Placeholder -->
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

      <!-- Header (Aligned with index.vue) -->
      <view class="nav-bar bg-nav-bar border-b border-theme-main">
        <view class="header-left" v-if="isOthersProfile" @click="goBack">
          <text class="material-icons" style="font-size: 48rpx; color: var(--text-main);">arrow_back</text>
        </view>
        <view class="header-center" :class="{ 'center-with-back': isOthersProfile }">
          <text class="logo-text text-theme-main">{{ userInfo.nickname }}的战术板</text>
        </view>
        <view class="header-right" v-if="!isOthersProfile">
          <button class="action-btn" @click="goToSettings">
            <text class="material-icons" style="font-size: 48rpx; color: var(--text-main);">settings</text>
          </button>
        </view>
        <view class="header-right" v-else>
          <!-- 占位，保持中间标题居中 -->
          <view style="width: 48rpx;"></view>
        </view>
      </view>
    </view>

    <view class="main-content">
      <!-- 移除 header-placeholder，因为 header-container 已经占位 -->
      
      <!-- Profile Header Section -->
      <view class="profile-header anim-fade-in">
        <view class="cover-image-container" @click="chooseCover">
          <view class="cover-overlay"></view>
          <image v-if="userInfo.cover" class="cover-image" :src="getFullImageUrl(userInfo.cover)" mode="aspectFill"></image>
          <view v-else class="cover-placeholder bg-theme-secondary"></view>
        </view>
        
        <view class="profile-info-card anim-slide-up stagger-1">
          <view class="avatar-section">
            <view class="progress-ring-box">
              <svg class="progress-ring" viewBox="0 0 112 112">
                <circle class="ring-bg" cx="56" cy="56" r="50"></circle>
                <circle class="ring-active" cx="56" cy="56" r="50" 
                  :style="{ strokeDashoffset: 314.159 * (1 - progress), stroke: levelColor }"></circle>
              </svg>
              
              <!-- Lv.5 流光背景层 (必须在最底层) -->
              <view v-if="Number(userInfo.level) === 5" class="frame-glow-lv5-bg"></view>

              <view class="avatar-wrapper border-2 border-white/20">
                <image class="avatar-img" :src="getFullImageUrl(userInfo.avatar)" mode="aspectFill" @click="chooseAvatar"></image>
              </view>

              <!-- 头像框装饰层 -->
              <view class="avatar-frame" :class="'frame-lv' + userInfo.level">
                <!-- Lv.2 足球装饰 (左上+左下) -->
                <template v-if="Number(userInfo.level) === 2">
                  <text class="material-icons frame-deco deco-tl" style="font-size: 44rpx; color: #B45309;">sports_soccer</text>
                  <text class="material-icons frame-deco deco-bl" style="font-size: 44rpx; color: #B45309;">sports_soccer</text>
                </template>
                
                <!-- Lv.4 皇冠装饰 (正上方) -->
                <text v-if="Number(userInfo.level) === 4" class="material-icons top-crown" style="font-size: 48rpx; color: #FACC15;">verified</text>
                
                <!-- Lv.5 皇冠装饰 (正上方) -->
                <text v-if="Number(userInfo.level) === 5" class="material-icons top-crown" style="font-size: 48rpx; color: #f2b90d;">verified</text>
              </view>
              
              <!-- 等级标识 -->
              <view class="level-badge" @click="goToLevelDetail" :class="'lv-badge-' + (userInfo.level || 1)">
                <text class="material-icons badge-icon" style="font-size: 32rpx; margin-right: 4rpx; color: white;">verified</text>
                <text class="badge-text">LV.{{ userInfo.level || 1 }}</text>
              </view>
            </view>
          </view>
          
          <view class="identity-info">
            <view class="name-row">
              <text class="nickname">{{ userInfo.nickname }}</text>
              <text class="material-icons" style="font-size: 44rpx; margin-left: 8rpx; color: #f2b90d;">verified</text>
            </view>
            
            <!-- 层次感称号标签 -->
            <view class="title-container" v-if="userInfo.levelTitle">
              <text class="primary-title">{{ userInfo.levelTitle.split('|')[0] }}</text>
              <text class="divider">|</text>
              <text class="secondary-title">{{ userInfo.levelTitle.split('|')[1] }}</text>
            </view>
            <text v-else class="user-handle">{{ userInfo.nickname }} | 绿茵观察员</text>
            
            <text class="user-bio">{{ userInfo.bio || '此人没有留下任何足迹...' }}</text>
          </view>

        </view>
      </view>

      <view class="action-buttons-row anim-slide-up stagger-2">
        <template v-if="!isOthersProfile">
          <button class="btn-primary" hover-class="btn-active" @click="goToEdit">编辑资料</button>
          <button class="btn-secondary" hover-class="btn-active" @click="logout">退出登录</button>
        </template>
        <template v-else>
          <button class="btn-primary" hover-class="btn-active" @click="handleFollow">关注</button>
          <button class="btn-secondary" hover-class="btn-active" @click="handleMessage">
            <text class="material-icons" style="font-size: 44rpx; margin-right: 8rpx; color: var(--text-main);">chat_bubble</text>
            <text>私信</text>
          </button>
        </template>
      </view>

      <!-- Stats Section -->
      <view class="stats-container anim-slide-up stagger-3">
        <view class="stat-item" hover-class="btn-active" @click="goToFollow('following')">
          <text class="stat-value">{{ formatStats(userInfo.stats.following) }}</text>
          <text class="stat-label">关注</text>
        </view>
        <view class="stat-item" hover-class="btn-active" @click="goToFollow('followers')">
          <text class="stat-value">{{ formatStats(userInfo.stats.followers) }}</text>
          <text class="stat-label">粉丝</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ formatStats(userInfo.stats.likesReceived) }}</text>
          <text class="stat-label">获赞</text>
        </view>
      </view>

      <!-- Tabs Navigation -->
      <view class="tabs-sticky bg-nav-bar border-b border-theme-main anim-slide-up stagger-4">
        <view class="tabs-content">
          <view v-for="(tab, index) in profileTabs" :key="index" 
                class="tab-item" 
                :class="{ active: currentProfileTab === index }"
                @click="currentProfileTab = index">
            <text class="tab-text" :class="currentProfileTab === index ? 'text-theme-main font-bold' : 'text-theme-secondary'">{{ tab.name }} ({{ tab.count }})</text>
            <view class="tab-indicator" v-if="currentProfileTab === index"></view>
          </view>
        </view>
      </view>

      <!-- Content Feed -->
      <view class="content-feed">
        <!-- 动态列表 (Tab 0) -->
        <view v-if="currentProfileTab === 0">
          <view v-for="(post, index) in posts" :key="post.id" class="post-card bg-card border border-theme-main" @click="goToPostDetail(post.id)">
            <view class="post-content-row">
              <view class="post-text-side">
                <text class="post-title text-theme-main">{{ post.title }}</text>
                <text class="post-excerpt text-theme-secondary">{{ post.content }}</text>
      <view class="post-meta">
                  <view class="meta-item text-theme-secondary">
                    <text class="material-icons" style="font-size: 44rpx;">favorite</text>
                    <text>{{ formatStats(post.likes) }}</text>
                  </view>
                  <view class="meta-item text-theme-secondary">
                    <text class="material-icons" style="font-size: 44rpx;">chat_bubble</text>
                    <text>{{ post.commentCount }}</text>
                  </view>
                  <text class="post-time text-theme-secondary">{{ formatTime(post.createTime) }}</text>
                </view>
              </view>
              <image v-if="post.image" class="post-image" :src="post.image" mode="aspectFill"></image>
            </view>
          </view>
          
          <u-loadmore :status="loadStatus" :color="themeStore.theme === 'dark' ? '#64748b' : '#94a3b8'" v-if="posts.length > 0" />
          <view class="empty-state" v-if="posts.length === 0 && !loading">
            <text class="material-icons empty-icon text-theme-secondary" style="font-size: 80rpx;">description</text>
            <text class="text-theme-secondary">暂无动态</text>
          </view>
        </view>
        
        <!-- 收藏列表 (Tab 1) -->
        <view v-else-if="currentProfileTab === 1">
          <!-- 收藏二级分类 -->
          <view class="fav-sub-tabs bg-theme-secondary border border-theme-main">
            <view v-for="(subTab, subIdx) in favSubTabs" :key="subIdx" 
                  class="sub-tab-item" 
                  :class="{ active: currentFavSubTab === subIdx }"
                  @click="changeFavSubTab(subIdx)">
              <text :class="currentFavSubTab === subIdx ? 'text-theme-main font-bold' : 'text-theme-secondary'">{{ subTab }}</text>
            </view>
          </view>

          <!-- 帖子收藏列表 -->
          <view v-if="currentFavSubTab === 0">
            <view v-for="(item, index) in favorites" :key="item.id" class="post-card bg-card border border-theme-main" @click="goToPostDetail(item.id)">
              <view class="post-content-row">
                <view class="post-text-side">
                  <text class="post-title text-theme-main">{{ item.title }}</text>
                  <text class="post-excerpt text-theme-secondary">{{ item.content }}</text>
                  <view class="post-meta">
                    <view class="meta-item text-theme-secondary">
                      <text class="material-icons" style="font-size: 44rpx;">favorite</text>
                      <text>{{ formatStats(item.likes) }}</text>
                    </view>
                    <view class="meta-item text-theme-secondary">
                      <text class="material-icons" style="font-size: 44rpx;">chat_bubble</text>
                      <text>{{ item.commentCount }}</text>
                    </view>
                    <text class="post-time text-theme-secondary">{{ formatTime(item.createTime) }}</text>
                  </view>
                </view>
                <image v-if="item.image" class="post-image" :src="item.image" mode="aspectFill"></image>
              </view>
            </view>
          </view>

          <!-- 新闻收藏列表 -->
          <view v-else-if="currentFavSubTab === 1">
            <view v-for="(item, index) in favoriteNews" :key="item.id" class="post-card bg-card border border-theme-main" @click="goToNewsDetail(item.id)">
              <view class="post-content-row">
                <view class="post-text-side">
                  <text class="post-title text-theme-main">{{ item.title }}</text>
                  <text class="post-excerpt text-theme-secondary">{{ item.content }}</text>
                  <view class="post-meta">
                    <text class="post-time text-theme-secondary">{{ formatTime(item.createTime) }}</text>
                  </view>
                </view>
                <image v-if="item.image" class="post-image" :src="item.image" mode="aspectFill"></image>
              </view>
            </view>
          </view>

          <!-- 球员收藏列表 -->
          <view v-else-if="currentFavSubTab === 2" class="player-grid">
            <view v-for="(player, index) in favoritePlayers" :key="player.id" class="player-card bg-card border border-theme-main" @click="goToPlayerDetail(player.id)">
              <image class="player-avatar" :src="player.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
              <text class="player-name text-theme-main">{{ player.name }}</text>
              <text class="player-position text-theme-secondary">{{ player.position }}</text>
            </view>
          </view>
          
          <u-loadmore :status="favLoadStatus" :color="themeStore.theme === 'dark' ? '#64748b' : '#94a3b8'" v-if="getCurrentFavList().length > 0" />
          <view class="empty-state" v-if="getCurrentFavList().length === 0 && !favLoading">
            <text class="material-icons empty-icon text-theme-secondary" style="font-size: 80rpx;">bookmark</text>
            <text class="text-theme-secondary">暂无{{ favSubTabs[currentFavSubTab] }}收藏</text>
          </view>
        </view>
        
        <!-- 相册 (Tab 2) -->
        <view v-else-if="currentProfileTab === 2" class="empty-state">
          <text class="material-icons empty-icon text-theme-secondary" style="font-size: 80rpx;">photo_library</text>
          <text class="text-theme-secondary">暂无相册</text>
        </view>
      </view>
      
      <view class="footer-info">
        <text class="version-text text-theme-secondary">PitchPulse v2.4.0 (Build 503)</text>
      </view>
      
      <!-- 底部占位：适配底部导航栏，避免内容被遮挡。高度与 CustomTabBar.vue 中的 .tab-bar 保持一致 -->
      <view class="bottom-placeholder" :style="{ height: isOthersProfile ? 'env(safe-area-inset-bottom)' : 'calc(110rpx + env(safe-area-inset-bottom))' }"></view>
    </view>

    <!-- 底部导航栏 -->
    <CustomTabBar v-if="!isOthersProfile" :currentTab="4" />

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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userApi, fileApi, postApi, favoriteApi, playerApi } from '@/api'
import { BASE_URL, getFullImageUrl } from '@/utils/request'
import { useThemeStore } from '@/store/theme'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight


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
  levelTitle: '新秀观察员 | 足球爱好者',
  bio: '',
  description: '',
  stats: {
    following: 0,
    followers: 0,
    likesReceived: 0
  }
})

// 等级体系称号映射
const levelTitles = {
  1: '新秀观察员 | 足球爱好者',
  2: '战术研究员 | 比赛分析者',
  3: '资深分析师 | 绿茵智囊',
  4: '战术大师 | 绿茵战略家',
  5: '战术宗师 | 足球哲学家'
}

const levelColor = computed(() => {
  const level = userInfo.value.level
  if (level === 1) return '#4B5563' // 哑光石墨
  if (level === 2) return '#B45309' // 古铜
  if (level === 3) return '#CBD5E1' // 银白
  if (level === 4) return '#FACC15' // 亮金
  if (level === 5) return '#f2b90d' // 黑金
  return '#4B5563'
})

const progress = computed(() => {
  if (!userInfo.value.experience) return 0
  const level = userInfo.value.level || 1
  const levelExpMap = {
    1: 100,
    2: 200,
    3: 300,
    4: 400,
    5: 99999
  }
  const nextExp = levelExpMap[level] || 100
  return Math.min(1, userInfo.value.experience / nextExp)
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

const goToLevelDetail = () => {
  uni.navigateTo({
    url: '/pages/my/level-detail'
  })
}

const getCurrentFavList = () => {
  if (currentFavSubTab.value === 0) return favorites.value
  if (currentFavSubTab.value === 1) return favoriteNews.value
  return favoritePlayers.value
}

// 监听 tab 切换，切换到收藏时加载数据
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
  const userInfoStr = uni.getStorageSync('userInfo');
  let currentUserId = null;
  if (userInfoStr) {
    try {
      const userInfo = typeof userInfoStr === 'string' ? JSON.parse(userInfoStr) : userInfoStr;
      currentUserId = userInfo?.id;
    } catch (e) {
      console.error('Failed to parse userInfo', e);
    }
  }
  isOthersProfile.value = !!options.userId && options.userId !== currentUserId;

  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  

  
  loadUserProfile(options.userId)
})

// 使用统一的 getFullImageUrl 处理图片 URL
const getAvatarUrl = (path, type = 'avatar') => {
  if (!path) {
    return getFullImageUrl(type === 'avatar' ? '/static/default-avatar.png' : '')
  }
  return getFullImageUrl(path)
}

const loadUserProfile = async (userId) => {
  try {
    const profileRes = userId ? await userApi.getProfile(userId) : await userApi.getProfile()
    if (profileRes) {
      // 使用统一函数处理头像
      const avatarUrl = getAvatarUrl(profileRes.avatar, 'avatar')
      const coverUrlFromRes = getAvatarUrl(profileRes.cover, 'cover')
      
      console.log('加载用户信息成功:', profileRes)
      
      const level = profileRes.level || 1
      userInfo.value = {
        ...userInfo.value,
        id: profileRes.id,
        nickname: profileRes.nickname || profileRes.username,
        username: '@' + profileRes.username,
        avatar: avatarUrl,
        // 如果后端返回了有效的封面图，则更新；否则保留当前（可能是刚刚上传预览的）
        cover: coverUrlFromRes || userInfo.value.cover,
        level: level,
        levelTitle: levelTitles[level] || '新秀观察员 | 足球爱好者',
        bio: profileRes.bio || '此人没有留下任何足迹...',
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
        image: item.images && item.images.length > 0 ? getFullImageUrl(item.images[0]) : '',
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
          image: item.images && item.images.length > 0 ? getFullImageUrl(item.images[0]) : '',
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
          image: item.image ? getFullImageUrl(item.image) : '',
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
  
  // 如果是头像上传，使用专门的用户头像上传接口，该接口会自动更新数据库
  const uploadUrl = type === 'avatar' 
    ? (userApi.uploadAvatarUrl.startsWith('/') ? userApi.uploadAvatarUrl : '/' + userApi.uploadAvatarUrl)
    : (fileApi.uploadUrl.startsWith('/') ? fileApi.uploadUrl : '/' + fileApi.uploadUrl)

  uni.uploadFile({
    url: BASE_URL + uploadUrl,
    filePath: path,
    name: 'file',
    formData: { type }, // 保持原有 formData 兼容性
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
        if (data.code === 200) {
          const url = data.data
          if (type === 'avatar') {
            userInfo.value.avatar = getFullImageUrl(url)
            uni.showToast({ title: '头像更新成功', icon: 'success' })
          } else if (type === 'cover') {
            userInfo.value.cover = getFullImageUrl(url)
            // 如果封面也需要同步更新到数据库，后续可以添加专门接口
            // 目前假设 cover 只是上传并返回 URL，由保存按钮统一提交
            uni.showToast({ title: '封面上传成功', icon: 'success' })
          }
        } else {
          uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
        }
      } catch (e) {
        console.error('解析上传响应失败:', e)
        uni.showToast({ title: '解析响应失败', icon: 'none' })
      }
    },
    fail: (err) => {
      console.error('上传文件失败:', err)
      uni.showToast({ title: '网络请求失败', icon: 'none' })
    },
    complete: () => {
      uni.hideLoading()
    }
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
  background-color: var(--bg-main);
  margin: 0 auto;
  width: 100%;
  min-height: 100vh;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
}

.header-container {
  background-color: var(--nav-bar-bg);
  backdrop-filter: blur(20px);
  z-index: 1000;
  width: 100%;
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
}

.nav-bar {
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  width: 100%;
}

.header-left, .header-right {
  width: 100rpx;
  display: flex;
  align-items: center;
}

.header-right {
  justify-content: flex-end;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  
  &.center-with-back {
    margin-right: 0;
  }
}

.logo-text {
  font-size: 34rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
}

.action-btn {
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60rpx;
  height: 60rpx;
  &::after { border: none; }
}

.main-content {
  width: 100%;
}

.bottom-placeholder {
  width: 100%;
  pointer-events: none;
}

.profile-header {
    position: relative;
    overflow: hidden;

    .cover-image-container {
      height: 520rpx;
      position: relative;
      background-color: var(--bg-main);
      
      &::after {
        content: '';
        position: absolute;
        left: 0;
        right: 0;
        bottom: 0;
        height: 60%;
        background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, transparent 100%);
        z-index: 2;
      }

      .cover-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .cover-placeholder {
        width: 100%;
        height: 100%;
        background: linear-gradient(45deg, var(--bg-main), var(--bg-secondary));
      }
    }

    .profile-info-card {
      position: absolute;
      bottom: 40rpx;
      left: 0;
      right: 0;
      z-index: 10;
      background: transparent !important;
      box-shadow: none !important;
      padding: 0 40rpx;
      display: flex;
      align-items: flex-end;
      gap: 30rpx;

      .avatar-section {
        flex-shrink: 0;
        position: relative;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: visible;
        z-index: 50;
        width: 224rpx;
        height: 224rpx;
        border-radius: 50%;
      }

      .identity-info {
        flex: 1;
        margin-top: 0;
        padding-bottom: 10rpx;

        .name-row {
          display: flex;
          align-items: center;
          gap: 16rpx;
          margin-bottom: 12rpx;

          .nickname {
            font-size: 44rpx;
            font-weight: 800;
            color: #ffffff !important;
            text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.3);
          }
        }

        .title-container {
          display: inline-flex;
          align-items: center;
          background: rgba(255, 255, 255, 0.15);
          backdrop-filter: blur(4rpx);
          padding: 6rpx 16rpx;
          border-radius: 8rpx;
          margin-top: 12rpx;
          border: 1rpx solid rgba(255, 255, 255, 0.2);
          gap: 12rpx;

          .primary-title {
            font-size: 24rpx;
            font-weight: 600;
            color: #f2b90d;
          }

          .divider {
            color: rgba(255, 255, 255, 0.5);
            margin: 0 10rpx;
            font-size: 20rpx;
          }

          .secondary-title {
            font-size: 22rpx;
            color: rgba(255, 255, 255, 0.9);
          }
        }

        .user-bio {
          font-size: 26rpx;
          color: rgba(255, 255, 255, 0.8) !important;
          line-height: 1.4;
          margin-top: 10rpx;
          width: 100%;
          display: block;
          word-break: break-all;
          text-shadow: 0 1rpx 4rpx rgba(0,0,0,0.3);
        }
      }
    }
  }

  .action-buttons-row {
    display: flex;
    gap: 20rpx;
    margin: 30rpx 40rpx 0;
    
    button {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      font-size: 30rpx;
      font-weight: 700;
      border-radius: 44rpx;
      transition: all 0.3s;
      
      &:active { transform: scale(0.97); }
      &::after { border: none; }

      &.btn-primary {
        background: #f2b90d;
        color: #000000;
        box-shadow: 0 8rpx 20rpx rgba(242, 185, 13, 0.3);
      }

      &.btn-secondary {
        background: rgba(255, 255, 255, 0.15);
        backdrop-filter: blur(4rpx);
        color: #ffffff;
        border: 1rpx solid rgba(255, 255, 255, 0.2);
      }
    }
  }

.progress-ring-box {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
  z-index: 50;
}

.level-badge {
  position: absolute;
  bottom: 0;
  right: -10rpx;
  z-index: 100;

  /* 1. 放弃 min-width，直接给固定宽度 */
  width: 100rpx; /* 稍微增加宽度，确保内容不溢出 */
  height: 36rpx; /* 稍微增加高度，视觉比例更好 */

  /* 2. 确保背景和边框正确 */
  background-color: #f2b90d; /* 兜底背景色 */
  background: linear-gradient(135deg, #f2b90d, #f9d406);
  border: 4rpx solid #1a1a1a;
  border-radius: 100rpx;

  /* 3. 强制内部内容水平垂直居中 */
  display: flex !important;
  flex-direction: row !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 0;
  white-space: nowrap;
  pointer-events: auto;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.4);
  overflow: visible !important;

  .badge-icon {
    font-size: 24rpx !important;
    font-weight: normal !important;
    color: #000000 !important;
    margin: 0 2rpx 0 0 !important;
    line-height: 1;
    position: relative; /* 启用 z-index */
    transform: translateY(1rpx);
    z-index: 110;
  }

  .badge-text {
    font-size: 18rpx !important;
    font-weight: 900 !important;
    color: #000000 !important;
    line-height: 1;
    margin: 0 !important;
    position: relative; /* 启用 z-index */
    transform: translateY(2rpx);
    z-index: 110;
  }

  &.lv-badge-5 {
    background: linear-gradient(90deg, #f2b90d, #fff) !important;
    box-shadow: 0 0 15rpx rgba(242, 185, 13, 0.6) !important;
  }
}

.progress-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  z-index: 10;
  
  circle {
    fill: transparent;
    stroke-width: 2;
    stroke-linecap: round;
  }
  
  .ring-bg {
    stroke: rgba(255, 255, 255, 0.1);
  }
  
  .ring-active {
    transition: stroke-dashoffset 0.35s;
    stroke-dasharray: 314.159;
  }
}

.avatar-wrapper {
  position: absolute;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  overflow: hidden;
  z-index: 20;
  background: #1e293b;
  border: 4rpx solid #1e293b;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;

  .avatar-img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    display: block;
    background-color: #2d3748;
  }
}

/* 头像框通用样式 */
.avatar-frame {
  position: absolute;
  inset: -10rpx;
  border-radius: 50%;
  z-index: 30;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;

  /* Lv.1: 哑光石墨 */
  &.frame-lv1 {
    border: 2rpx solid rgba(156, 163, 175, 0.4) !important;
  }

  /* Lv.2: 青铜足球 */
  &.frame-lv2 {
    border: 4rpx dashed #B45309 !important;
    
    .frame-deco {
      position: absolute;
      font-size: 32rpx;
      color: #B45309;
      text-shadow: 0 0 6rpx rgba(0, 0, 0, 0.5);
      z-index: 40;
      
      &.deco-tl { top: 0; left: 0; transform: translate(-20%, -20%); }
      &.deco-bl { bottom: 40rpx; left: -10rpx; }
    }
  }

  /* Lv.3: 白金银色 */
  &.frame-lv3 {
    border: 5rpx double #CBD5E1 !important;
    background: none;
    box-shadow: 0 0 15rpx rgba(203, 213, 225, 0.5);
    
    &::after {
      content: "";
      position: absolute;
      inset: 4rpx;
      border-radius: 50%;
      border: 1rpx dashed rgba(255, 255, 255, 0.3);
    }
  }

  /* Lv.4: 黄金至尊 (呼吸灯) */
  &.frame-lv4 {
    border: 6rpx solid #FACC15 !important;
    animation: gold-pulse 2s infinite ease-in-out;
    
    .top-crown {
      position: absolute;
      top: -30rpx;
      left: 50%;
      transform: translateX(-50%);
      font-size: 44rpx;
      color: #FACC15;
      text-shadow: 0 0 10rpx rgba(250, 204, 21, 0.5);
      z-index: 40;
    }
  }

  /* Lv.5: 黑金至尊 (旋转流光) */
  &.frame-lv5 {
    background: none !important;
    border: none !important;
    
    .top-crown {
      position: absolute;
      top: -34rpx;
      left: 50%;
      transform: translateX(-50%);
      font-size: 56rpx;
      color: #f2b90d;
      text-shadow: 0 0 20rpx rgba(242, 185, 13, 0.8), 0 0 40rpx rgba(0, 0, 0, 0.5);
      z-index: 40;
    }
  }
}

.frame-glow-lv5-bg {
  position: absolute;
  inset: -8rpx;
  border-radius: 50%;
  /* 黑金交替渐变 */
  background: conic-gradient(
    from 0deg, 
    #000000 0%, 
    #f2b90d 25%, 
    #000000 50%, 
    #f2b90d 75%, 
    #000000 100%
  );
  animation: rotate 1.5s linear infinite;
  z-index: 4;
  box-shadow: 0 0 25rpx rgba(242, 185, 13, 0.4);
}


.stats-container {
  display: flex;
  justify-content: space-around;
  padding: 30rpx;
  margin: 30rpx 40rpx 0;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  border-radius: 24rpx;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: all 0.3s;
    
    .stat-value {
      font-size: 36rpx;
      font-weight: 800;
      color: #ffffff;
    }
    
    .stat-label {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.7);
      margin-top: 4rpx;
    }

    &:active {
      transform: scale(0.92);
    }
  }
}

/* 针对浅色模式的覆盖样式 */
.theme-light {
  .stats-container {
    background: rgba(0, 0, 0, 0.04);
    border-color: rgba(0, 0, 0, 0.08);
    
    .stat-item {
      .stat-value {
        color: #1a1a1a;
      }
      .stat-label {
        color: #666666;
      }
    }
  }

  .action-buttons-row {
    .btn-secondary {
      background: rgba(0, 0, 0, 0.05);
      color: #333333;
      border: 1rpx solid rgba(0, 0, 0, 0.1);
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes gold-pulse {
  0% { transform: scale(1); border-color: #FACC15; }
  50% { transform: scale(1.02); border-color: #FFF176; box-shadow: 0 0 20rpx #FACC15; }
  100% { transform: scale(1); border-color: #FACC15; }
}

@keyframes frame-breath {
  0% { box-shadow: 0 0 10rpx rgba(242, 185, 13, 0.2); }
  50% { box-shadow: 0 0 25rpx rgba(242, 185, 13, 0.5); }
  100% { box-shadow: 0 0 10rpx rgba(242, 185, 13, 0.2); }
}

.tabs-sticky {
  position: sticky;
  top: 0;
  z-index: 90;
  background-color: var(--nav-bar-bg);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid var(--border-main);
}

.tabs-content {
  display: flex;
  padding: 0 32rpx;
  border-bottom: 1rpx solid var(--border-main);
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
  background-color: var(--card-bg);
  border-radius: 32rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  border: 1rpx solid var(--border-main);
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
  padding: 40rpx 0;
  color: #64748b;
  gap: 16rpx;
}

.empty-icon {
  font-size: 96rpx;
  opacity: 0.2;
}

.footer-info {
  padding: 20rpx 0 0;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

  /* 收藏二级分类样式 */
  .fav-sub-tabs {
    display: flex;
    padding: 20rpx 32rpx;
    gap: 20rpx;
    background-color: var(--bg-main);
    
    .sub-tab-item {
      padding: 12rpx 32rpx;
      border-radius: 30rpx;
      background-color: var(--bg-secondary);
      color: var(--text-secondary);
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
      background-color: var(--card-bg);
      border-radius: 16rpx;
      padding: 24rpx 16rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      transition: all 0.3s;
      border: 1rpx solid var(--border-main);
      
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
  color: #64748b;
  letter-spacing: 2rpx;
  opacity: 0.6;
}
</style>
