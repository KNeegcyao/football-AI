<template>
  <view class="page-container bg-[#1A1811] text-white min-h-screen font-display pb-12 max-w-[500px] mx-auto shadow-2xl relative">
    <!-- Header -->
    <view class="fixed top-0 left-0 right-0 z-[999] bg-[#1A1811] px-4 pt-12 pb-4 flex flex-row items-center justify-between border-b border-white/5 shadow-md h5-header-fix">
      <view class="w-10 h-10 flex items-center justify-center rounded-full bg-white/5 text-white active:bg-white/10" @click="goBack">
        <text class="material-icons-round" style="font-size: 24px;">arrow_back</text>
      </view>
      <text class="text-xl font-bold tracking-tight text-white">编辑资料</text>
      <view class="w-10 h-10 flex items-center justify-center rounded-full bg-white/5 text-white active:bg-white/10" @click="loadProfile">
        <text class="material-icons-round" style="font-size: 20px;">refresh</text>
      </view>
    </view>

    <!-- Content -->
    <view class="px-6 pt-[120px]">
      <!-- Avatar Section -->
      <view class="flex flex-col items-center mb-8">
        <view class="w-24 h-24 rounded-full border-2 border-[#f9d406] overflow-hidden bg-[#2C2C2C] mb-4 relative active:opacity-80" @click="chooseAvatar">
          <image class="w-full h-full" :src="form.avatar || '/static/soccer-logo.png'" mode="aspectFill"></image>
        </view>
        <text class="text-sm text-gray-400">点击更换头像</text>
      </view>

      <!-- Form Fields -->
      <view>
        <!-- Nickname Field -->
        <view class="mb-6">
          <text class="text-sm font-bold text-[#f9d406] uppercase tracking-wider block mb-2">昵称</text>
          <!-- Wrap input in a styled container -->
          <view class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus-within:border-[#f9d406] transition-colors flex items-center">
            <input 
              type="text" 
              v-model="form.nickname" 
              placeholder="请输入昵称" 
              placeholder-class="text-gray-600"
              class="flex-1 h-full bg-transparent text-white text-base"
              @click="console.log('Nickname Input Clicked!')"
            />
          </view>
        </view>

        <!-- Phone Field (Read-only) -->
        <view class="mb-6">
          <text class="text-sm font-bold text-[#f9d406] uppercase tracking-wider block mb-2">手机号 (用户名)</text>
          <view class="w-full bg-white/2 border border-white/5 rounded-xl px-4 py-3 flex items-center">
            <input 
              type="text" 
              v-model="form.phone" 
              disabled
              class="flex-1 h-full bg-transparent text-gray-500 text-base cursor-not-allowed"
              @click="console.log('Phone Input Clicked!')"
            />
          </view>
          <text class="text-xs text-gray-500 ml-1 mt-1 block">手机号作为登录账号不可修改</text>
        </view>

        <!-- Email Field -->
        <view class="mb-6">
          <text class="text-sm font-bold text-[#f9d406] uppercase tracking-wider block mb-2">邮箱</text>
          <view class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus-within:border-[#f9d406] transition-colors flex items-center">
            <input 
              type="text" 
              v-model="form.email" 
              placeholder="请输入邮箱" 
              placeholder-class="text-gray-600"
              class="flex-1 h-full bg-transparent text-white text-base"
              @click="console.log('Email Input Clicked!')"
            />
          </view>
        </view>
      </view>

      <!-- Save Button -->
      <view class="mt-12">
        <button 
          @click="handleSave" 
          class="w-full bg-gradient-to-r from-[#f9d406] to-[#b89b00] text-[#1A1811] font-bold py-3 rounded-xl shadow-lg shadow-[#f9d406]/20 active:scale-95 transition-transform flex items-center justify-center"
          :disabled="loading"
        >
          {{ loading ? '保存中...' : '保存修改' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userApi, fileApi } from '@/api'

const isEmail = (email) => {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

const form = ref({
  nickname: '',
  phone: '',
  email: '',
  avatar: ''
})

const loading = ref(false)
const currentAvatarPath = ref('')
const hasToken = ref(false)

onShow(async () => {
  console.log('Edit Page onShow triggered')
  const token = uni.getStorageSync('token')
  hasToken.value = !!token
  
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login/login' })
    }, 1500)
    return
  }
  
  // Always reload profile on show to ensure fresh data
  await loadProfile()
})

const loadProfile = async () => {
  loading.value = true
  console.log('开始加载用户资料...')
  try {
    const res = await userApi.getProfile()
    console.log('用户资料加载成功:', res)
    if (res) {
      // Create a new object to ensure reactivity triggers
      const newData = {
        nickname: res.nickname || '',
        phone: res.phone || res.username || '',
        email: res.email || '',
        avatar: res.avatar ? fileApi.getFileUrl(res.avatar) : ''
      }
      console.log('设置表单数据:', newData)
      form.value = newData
      currentAvatarPath.value = res.avatar || ''
    } else {
      console.error('用户资料为空')
      uni.showToast({ title: '未获取到用户资料', icon: 'none' })
    }
  } catch (e) {
    console.error('加载资料失败:', e)
    uni.showToast({ title: '加载资料失败: ' + (e.message || '未知错误'), icon: 'none' })
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}

const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      uploadAvatar(res.tempFilePaths[0])
    }
  })
}

const uploadAvatar = (filePath) => {
  uni.showLoading({ title: '上传头像...' })
  const token = uni.getStorageSync('token')
  
  uni.uploadFile({
    url: 'http://localhost:8080/api/files/upload',
    filePath: filePath,
    name: 'file',
    header: {
      'Authorization': token ? `Bearer ${token}` : ''
    },
    success: (res) => {
      if (res.statusCode === 200) {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            const newAvatarUrl = data.data
            // Update form immediately for preview
            const fullUrl = fileApi.getFileUrl(newAvatarUrl)
            form.value.avatar = fullUrl
            currentAvatarPath.value = newAvatarUrl 
            uni.showToast({ title: '头像上传成功', icon: 'success' })
          } else {
            uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
          }
        } catch (e) {
          uni.showToast({ title: '解析失败', icon: 'none' })
        }
      } else {
        uni.showToast({ title: '上传失败', icon: 'none' })
      }
    },
    fail: () => {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
    complete: () => {
      uni.hideLoading()
    }
  })
}

const handleSave = async () => {
  if (!form.value.nickname) {
    uni.showToast({ title: '昵称不能为空', icon: 'none' })
    return
  }
  
  if (form.value.email) {
    console.log('Validating email:', form.value.email)
    if (!isEmail(form.value.email)) {
      console.log('Email validation failed')
      uni.showToast({ title: '请输入正确的邮箱格式', icon: 'none' })
      return
    }
    console.log('Email validation passed')
  }
  
  loading.value = true
  try {
    const data = {
      nickname: form.value.nickname,
      email: form.value.email
    }
    
    if (currentAvatarPath.value) {
      data.avatar = currentAvatarPath.value
    }
    
    await userApi.updateProfile(data)
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    console.error('保存失败:', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
  /* No special styles needed, using Tailwind */
</style>