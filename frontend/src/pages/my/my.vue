<template>
  <view class="page-container bg-[#1A1811] text-white min-h-screen flex flex-col font-display">
    <!-- Header -->
    <view class="sticky top-0 z-50 bg-[#1A1811]/95 ios-blur px-4 pt-12 pb-4 flex flex-row items-center justify-between border-b border-white/5">
      <text class="text-xl font-bold tracking-tight text-white">个人中心</text>
      <view class="w-10 h-10 flex items-center justify-center rounded-full bg-white/5 text-[#f9d406]" @click="goToEdit">
        <text class="material-icons-round" style="font-size: 20px;">edit</text>
      </view>
    </view>

    <scroll-view scroll-y class="flex-1 pb-32">
      <!-- User Info -->
      <view class="px-6 pt-8 pb-6 flex flex-col items-center">
        <view class="relative group">
          <view class="absolute -inset-1 bg-gradient-to-tr from-[#f9d406] to-[#f9d406] rounded-full blur opacity-40 group-hover:opacity-60 transition duration-500"></view>
          <view class="relative w-28 h-28 rounded-full border-2 border-[#f9d406] overflow-hidden bg-[#2C2C2C]" @click="chooseAvatar">
            <image class="w-full h-full" :src="userInfo.avatar" mode="aspectFill"></image>
          </view>
          <view class="absolute bottom-0 right-0 gold-gradient text-[#1A1811] text-[10px] font-bold px-2 py-1 rounded-full border-2 border-[#1A1811] shadow-lg">
            <text>等级 {{ userInfo.level }}</text>
          </view>
        </view>
        <view class="mt-4 text-center flex flex-col items-center">
          <text class="text-2xl font-bold text-white">{{ userInfo.nickname }}</text>
          <text class="text-[#f9d406] font-medium text-sm">{{ userInfo.username }}</text>
        </view>
      </view>

      <!-- Stats -->
      <view class="px-6 mb-8">
        <view class="grid grid-cols-3 gap-4 p-4 rounded-xl bg-white/5 border border-white/5">
          <view class="text-center flex flex-col items-center">
            <text class="text-xl font-bold text-white">{{ userInfo.stats.posts }}</text>
            <text class="text-[12px] tracking-wide text-gray-400 font-medium">动态</text>
          </view>
          <view class="text-center border-x border-white/10 flex flex-col items-center">
            <text class="text-xl font-bold text-white">{{ userInfo.stats.likes }}</text>
            <text class="text-[12px] tracking-wide text-gray-400 font-medium">获赞</text>
          </view>
          <view class="text-center flex flex-col items-center">
            <text class="text-xl font-bold text-white">{{ userInfo.stats.collections }}</text>
            <text class="text-[12px] tracking-wide text-gray-400 font-medium">收藏</text>
          </view>
        </view>
      </view>

      <!-- Menu -->
      <view class="px-4 space-y-2">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-2 block">我的活动</text>
        <view class="space-y-1">
          <view @click="goTo('posts')" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 hover:bg-white/10 rounded-xl border border-white/5 group transition-all">
            <view class="flex flex-row items-center gap-4">
              <view class="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center text-[#f9d406]">
                <text class="material-icons-round" style="font-size: 24px;">dynamic_feed</text>
              </view>
              <text class="font-medium text-white">我的发布</text>
            </view>
            <view class="flex flex-row items-center text-gray-400">
              <text class="text-xs mr-2">共 {{ userInfo.stats.posts }} 篇</text>
              <text class="material-icons-round text-lg" style="font-size: 18px;">chevron_right</text>
            </view>
          </view>
          
          <view @click="goTo('collections')" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 hover:bg-white/10 rounded-xl border border-white/5 group transition-all">
            <view class="flex flex-row items-center gap-4">
              <view class="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center text-[#f9d406]">
                <text class="material-icons-round" style="font-size: 24px;">bookmark</text>
              </view>
              <text class="font-medium text-white">我的收藏</text>
            </view>
            <text class="material-icons-round text-gray-400 text-lg" style="font-size: 18px;">chevron_right</text>
          </view>

          <view @click="goTo('report')" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 hover:bg-white/10 rounded-xl border border-white/5 group transition-all relative overflow-hidden">
            <view class="absolute top-0 right-0">
              <view class="w-16 h-16 bg-[#f9d406]/5 rounded-full -mr-8 -mt-8"></view>
            </view>
            <view class="flex flex-row items-center gap-4">
              <view class="w-10 h-10 rounded-lg bg-gradient-to-br from-[#f9d406] to-[#b89b00] flex items-center justify-center text-[#1A1811] shadow-lg shadow-[#f9d406]/20">
                <text class="material-icons-round" style="font-size: 24px;">psychology</text>
              </view>
              <view class="text-left flex flex-col">
                <text class="font-medium block text-white">AI 比赛报告</text>
                <text class="text-[10px] text-[#f9d406] font-bold">高级深度分析</text>
              </view>
            </view>
            <text class="material-icons-round text-gray-400 text-lg" style="font-size: 18px;">chevron_right</text>
          </view>
        </view>

        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-2 mt-8 block">系统设置</text>
        <view class="space-y-1">
          <view @click="goTo('settings')" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 hover:bg-white/10 rounded-xl border border-white/5 group transition-all">
            <view class="flex flex-row items-center gap-4">
              <view class="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center text-gray-400">
                <text class="material-icons-round" style="font-size: 24px;">settings</text>
              </view>
              <text class="font-medium text-white">设置</text>
            </view>
            <text class="material-icons-round text-gray-400 text-lg" style="font-size: 18px;">chevron_right</text>
          </view>

          <view @click="goTo('help')" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 hover:bg-white/10 rounded-xl border border-white/5 group transition-all">
            <view class="flex flex-row items-center gap-4">
              <view class="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center text-gray-400">
                <text class="material-icons-round" style="font-size: 24px;">help_outline</text>
              </view>
              <text class="font-medium text-white">帮助与支持</text>
            </view>
            <text class="material-icons-round text-gray-400 text-lg" style="font-size: 18px;">chevron_right</text>
          </view>
        </view>
      </view>

      <view class="mt-12 mb-8 px-6 text-center flex flex-col items-center">
        <text class="text-[#f9d406] font-bold mb-4 opacity-80" @click="logout">退出登录</text>
        <text class="text-[10px] text-gray-600 uppercase tracking-[0.2em]">PitchPulse v2.4.0 (Build 503)</text>
      </view>
    </scroll-view>

    <!-- 底部导航占位 -->
    <view class="safe-area-bottom"></view>

    <!-- 底部导航栏 -->
    <view class="tab-bar">
      <view v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: currentTab === index }"
        @tap="handleTabClick(index)">
        <u-icon :name="tab.icon" :color="currentTab === index ? '#f9d406' : '#7A7E83'" size="24"></u-icon>
        <text class="tab-text">{{ tab.text }}</text>
      </view>
    </view>
    <!-- Logout Modal -->
    <view v-if="showLogoutModal" class="fixed inset-0 z-[9999] flex items-center justify-center p-4">
      <view class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="showLogoutModal = false"></view>
      <view class="relative w-full max-w-[280px] bg-[#1A1811] rounded-2xl border border-[#f9d406]/20 p-6 flex flex-col items-center">
        <view class="w-12 h-12 rounded-full bg-[#f9d406]/10 flex items-center justify-center mb-4 text-[#f9d406]">
          <text class="material-icons-round" style="font-size: 24px;">logout</text>
        </view>
        <text class="text-white text-lg font-bold mb-2">退出登录</text>
        <text class="text-gray-400 text-sm text-center mb-6">确定要退出当前账号吗？</text>
        
        <view class="flex flex-row w-full gap-3">
          <view class="flex-1 bg-white/5 active:bg-white/10 text-white text-sm py-2.5 rounded-xl border border-white/5 text-center flex items-center justify-center" @click="showLogoutModal = false">
            取消
          </view>
          <view class="flex-1 bg-[#f9d406] active:bg-[#eac406] text-[#1A1811] text-sm font-bold py-2.5 rounded-xl shadow-lg shadow-[#f9d406]/20 text-center flex items-center justify-center" @click="confirmLogout">
            确认退出
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userApi, fileApi } from '@/api'

const currentTab = ref(3)
const tabs = [
  { text: '首页', icon: 'home', path: 'pages/index/index' },
  { text: '赛程', icon: 'calendar', path: 'pages/schedule/schedule' },
  { text: '社区', icon: 'chat', path: 'pages/community/community' },
  { text: '我的', icon: 'account', path: 'pages/my/my' }
]

const userInfo = ref({
  nickname: '加载中...',
  username: '',
  avatar: '',
  level: 1,
  stats: {
    posts: 0,
    likes: 0,
    collections: 0
  },
  newPosts: 0
})

const showLogoutModal = ref(false)



onShow(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/login/login'
      })
    }, 1000)
    return
  }
  loadUserProfile()
})

const handleTabClick = (index) => {
  const tab = tabs[index]
  if (!tab || !tab.path) return
  
  if (currentTab.value === index) return

  const url = tab.path.startsWith('/') ? tab.path : '/' + tab.path
  
  uni.switchTab({
    url: url,
    fail: () => {
      uni.reLaunch({ url })
    }
  })
}

// 页面挂载时修正当前 Tab 索引
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  if (currentPage) {
    const route = '/' + currentPage.route
    // 兼容路径前缀比较
    const index = tabs.findIndex(tab => ('/' + tab.path) === route)
    if (index !== -1) {
      currentTab.value = index
    }
  }
})

const loadUserProfile = async () => {
  try {
    // 获取用户基本信息
    const profileRes = await userApi.getProfile()
    if (profileRes) {
      userInfo.value.nickname = profileRes.nickname || profileRes.username
      userInfo.value.username = '@' + profileRes.username
      userInfo.value.avatar = profileRes.avatar ? fileApi.getFileUrl(profileRes.avatar) : '/static/soccer-logo.png'
      userInfo.value.level = profileRes.level || 1
    }

    // 获取用户统计数据
    const statsRes = await userApi.getStats()
    if (statsRes) {
      userInfo.value.stats = {
        posts: statsRes.postCount || 0,
        likes: statsRes.likeCount || 0,
        collections: statsRes.favoriteCount || 0
      }
    }
  } catch (e) {
    console.error('加载用户信息失败:', e)
    // 如果是未登录导致的错误，request.js 已经处理了提示和跳转，这里不再重复提示
    if (e.message !== '未登录或登录已过期' && e.message !== '未登录') {
      uni.showToast({
        title: '加载用户信息失败',
        icon: 'none'
      })
    }
  }
}

const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePaths = res.tempFilePaths
      uploadAvatar(tempFilePaths[0])
    }
  })
}

const uploadAvatar = (filePath) => {
  uni.showLoading({ title: '上传中...' })
  
  const token = uni.getStorageSync('token')
  
  uni.uploadFile({
    url: 'http://localhost:8080/api/files/upload',
    filePath: filePath,
    name: 'file',
    header: {
      'Authorization': token ? `Bearer ${token}` : ''
    },
    success: (uploadFileRes) => {
      console.log('上传结果:', uploadFileRes)
      if (uploadFileRes.statusCode === 200) {
        try {
          const data = JSON.parse(uploadFileRes.data)
          if (data.code === 200) {
            const newAvatarUrl = data.data
            // 更新本地显示
            userInfo.value.avatar = fileApi.getFileUrl(newAvatarUrl)
            
            // 更新后端用户资料
            updateUserProfile({ avatar: newAvatarUrl })
            
            uni.showToast({ title: '头像更新成功', icon: 'success' })
          } else {
            uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
          }
        } catch (e) {
          console.error('解析响应失败:', e)
          uni.showToast({ title: '上传失败', icon: 'none' })
        }
      } else {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    },
    fail: (err) => {
      console.error('上传请求失败:', err)
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
    complete: () => {
      uni.hideLoading()
    }
  })
}

const updateUserProfile = async (data) => {
  try {
    await userApi.updateProfile(data)
  } catch (e) {
    console.error('更新用户资料失败:', e)
    uni.showToast({ title: '资料同步失败', icon: 'none' })
  }
}

const goToEdit = () => {
  uni.navigateTo({
    url: '/pages/my/edit'
  })
}

const goTo = (page) => {
  console.log('Navigate to:', page)
  
  if (page === 'collections') {
    uni.navigateTo({
      url: '/pages/my/favorites'
    });
    return;
  }
  
  if (page === 'posts') {
    uni.navigateTo({
      url: '/pages/my/posts'
    });
    return;
  }
  
  uni.showToast({
    title: '即将跳转: ' + page,
    icon: 'none'
  })
}

const logout = () => {
  showLogoutModal.value = true
}

const confirmLogout = () => {
  showLogoutModal.value = false
  console.log('User confirmed logout')
  // 清除 Token
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
  
  uni.showToast({
    title: '已退出',
    icon: 'success'
  })
  setTimeout(() => {
    handleTabClick(0) // Go to home
  }, 1500)
}
</script>

<style lang="scss" scoped>
  .ios-blur {
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
  }
  .gold-gradient {
    background: linear-gradient(135deg, #D4AF37 0%, #FFD700 50%, #B8860B 100%);
  }
  .glass-card {
    background: rgba(236, 19, 55, 0.05);
    border: 1px solid rgba(236, 19, 55, 0.1);
  }
</style>

<style lang="scss">
/* #ifdef H5 */
.h5-header-fix {
  max-width: 500px;
  margin: 0 auto;
  left: 0;
  right: 0;
}
/* #endif */

/* 底部导航占位 */
.safe-area-bottom {
  height: 160rpx;
}

/* 底部导航栏 */
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
  padding-bottom: constant(safe-area-inset-bottom);
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
</style>