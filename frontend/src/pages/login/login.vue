<template>
  <view class="page-container bg-stadium-gradient min-h-screen flex flex-col font-display relative overflow-hidden">
    <!-- Background Overlay Texture -->
    <image 
      class="fixed inset-0 z-0 opacity-10 w-full h-full object-cover pointer-events-none" 
      src="https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&q=80&w=1000" 
      mode="aspectFill"
    ></image>

    <view class="flex-1 flex flex-col items-center justify-center px-8 py-12 z-10 w-full max-w-md mx-auto">
      <!-- Header Section: Logo & Branding -->
      <view class="w-full text-center mb-12">
        <view class="inline-flex items-center justify-center w-20 h-20 bg-[#f20d33]/10 rounded-xl mb-4 border border-[#f20d33]/20">
          <text class="material-icons text-[#f20d33] text-5xl" style="font-size: 48px;">sports_soccer</text>
        </view>
        <view class="flex flex-col items-center">
          <text class="text-4xl font-extrabold tracking-tighter text-[#d4af37] gold-glow italic mb-2">PITCHPULSE</text>
          <text class="text-xs uppercase tracking-[0.3em] text-[#f20d33]/80 font-semibold">AI Football Community</text>
        </view>
      </view>

      <!-- Login Form Section -->
      <view class="w-full space-y-6">
        <view class="space-y-4">
          <!-- Username/Email Field -->
          <view class="relative group w-full">
            <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
              <text class="material-icons text-slate-500 text-sm" style="font-size: 20px; color: #64748b;">person</text>
            </view>
            <input 
              v-model="form.username"
              class="w-full bg-[#1a0d0f] border border-[#f20d33]/10 rounded-xl py-4 pl-11 pr-4 text-white placeholder-slate-500 outline-none transition-all duration-300 focus:border-[#d4af37] h-12" 
              placeholder="手机号 / 邮箱" 
              placeholder-class="text-slate-500"
              type="text"
            />
          </view>

          <!-- Password Field -->
          <view class="relative group w-full mt-4">
            <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
              <text class="material-icons text-slate-500 text-sm" style="font-size: 20px; color: #64748b;">lock</text>
            </view>
            <input 
              v-model="form.password"
              class="w-full bg-[#1a0d0f] border border-[#f20d33]/10 rounded-xl py-4 pl-11 pr-4 text-white placeholder-slate-500 outline-none transition-all duration-300 focus:border-[#d4af37] h-12" 
              placeholder="密码" 
              placeholder-class="text-slate-500"
              :password="!showPassword"
              type="text"
            />
            <view class="absolute inset-y-0 right-0 pr-4 flex items-center h-full z-10" @click="togglePasswordVisibility">
              <text class="material-icons text-slate-500 text-sm" style="font-size: 20px; color: #64748b;">{{ showPassword ? 'visibility' : 'visibility_off' }}</text>
            </view>
          </view>
        </view>

        <!-- Forgot Password & Register Navigation -->
        <view class="flex flex-row justify-between items-center text-sm px-1 w-full mt-4">
          <text class="text-slate-400 hover:text-[#f20d33] transition-colors">忘记密码?</text>
          <text class="text-[#d4af37] font-medium" @click="goToRegister">立即注册</text>
        </view>

        <!-- Main Login Button -->
        <button 
          class="w-full bg-[#f20d33] hover:bg-[#f20d33]/90 text-white font-bold py-4 rounded-xl shadow-lg shadow-[#f20d33]/20 transition-all flex flex-row items-center justify-center space-x-2 mt-8 border-none"
          @click="handleLogin"
          :loading="loading"
        >
          <text class="mr-2">进入球场</text>
          <text class="material-icons text-lg" style="font-size: 18px;">login</text>
        </button>
      </view>

      <!-- Social Login & Agreement Footer -->
      <view class="w-full mt-16 space-y-8 flex flex-col items-center">
        <view class="relative w-full flex items-center justify-center">
          <view class="absolute inset-0 flex items-center">
            <view class="w-full border-t border-[#f20d33]/10"></view>
          </view>
          <view class="relative bg-[#0a0506] px-4">
            <text class="text-xs uppercase text-slate-500 tracking-widest">其他登录方式</text>
          </view>
        </view>
        
        <view class="flex flex-row justify-center space-x-8 gap-8 mt-6">
          <!-- WeChat -->
          <view class="w-12 h-12 flex items-center justify-center rounded-full bg-[#1a0d0f] border border-[#f20d33]/10 text-slate-300 hover:text-[#d4af37] hover:border-[#d4af37] transition-all">
            <!-- Simple SVG replacement with text or icon for now, as inline SVG is tricky in uniapp without plugin -->
            <text class="material-icons" style="font-size: 24px; color: #cbd5e1;">chat</text>
          </view>
          <!-- Apple -->
          <view class="w-12 h-12 flex items-center justify-center rounded-full bg-[#1a0d0f] border border-[#f20d33]/10 text-slate-300 hover:text-[#d4af37] hover:border-[#d4af37] transition-all">
            <text class="material-icons" style="font-size: 24px; color: #cbd5e1;">apple</text>
          </view>
          <!-- QQ -->
          <view class="w-12 h-12 flex items-center justify-center rounded-full bg-[#1a0d0f] border border-[#f20d33]/10 text-slate-300 hover:text-[#d4af37] hover:border-[#d4af37] transition-all">
             <text class="material-icons" style="font-size: 24px; color: #cbd5e1;">alternate_email</text>
          </view>
        </view>

        <view class="flex flex-row items-start space-x-2 px-2 mt-8">
          <checkbox-group @change="handleAgreementChange">
            <label class="flex items-center">
              <checkbox value="agreed" :checked="isAgreed" color="#f20d33" style="transform:scale(0.7)" />
              <view class="text-xs text-slate-500 leading-tight ml-2">
                已阅读并同意 <text class="text-slate-300">服务协议</text>、<text class="text-slate-300">隐私政策</text> 以及 <text class="text-slate-300">社区守则</text>
              </view>
            </label>
          </checkbox-group>
        </view>
      </view>
    </view>

    <!-- Footer Decoration -->
    <view class="w-full h-1 bg-gradient-to-r from-transparent via-[#f20d33] to-transparent opacity-30 absolute bottom-0"></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api'

const form = ref({
  username: '',
  password: ''
})

const showPassword = ref(false)
const loading = ref(false)
const isAgreed = ref(false)

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const handleAgreementChange = (e) => {
  isAgreed.value = e.detail.value.includes('agreed')
}

const goToRegister = () => {
  uni.navigateTo({
    url: '/pages/register/register'
  })
}

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  
  if (!isAgreed.value) {
    uni.showToast({ title: '请先同意服务协议', icon: 'none' })
    return
  }

  loading.value = true
  try {
    const res = await authApi.login(form.value)
    // 假设后端返回 { token: 'xxx' }
    const token = res.token
    if (token) {
      uni.setStorageSync('token', token)
      // 可选：获取用户信息并存储
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({
          url: '/pages/index/index'
        })
      }, 1500)
    } else {
       uni.showToast({ title: '登录失败: 无Token', icon: 'none' })
    }
  } catch (e) {
    console.error('登录失败:', e)
    // request.js 已经处理了错误提示
  } finally {
    loading.value = false
  }
}
</script>

<style>
/* 引入字体图标，如果项目中已有全局引入可省略 */
@import url("https://fonts.googleapis.com/icon?family=Material+Icons");

.bg-stadium-gradient {
  background: radial-gradient(circle at top, #221013 0%, #0a0506 100%);
}
.gold-glow {
  text-shadow: 0 0 15px rgba(212, 175, 55, 0.4);
}
</style>