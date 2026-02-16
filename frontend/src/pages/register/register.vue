<template>
  <view class="page-container bg-stadium-gradient min-h-screen flex flex-col font-display relative overflow-hidden">
    <!-- Status Bar Spacer -->
    <view class="h-12 w-full"></view>

    <!-- Background Mock Image -->
    <view class="fixed inset-0 z-0 overflow-hidden pointer-events-none">
      <view class="absolute top-0 right-0 w-[300px] h-[300px] bg-[#f20d33]/5 rounded-full blur-[100px]"></view>
      <view class="absolute bottom-0 left-0 w-[400px] h-[400px] bg-[#d4af37]/5 rounded-full blur-[120px]"></view>
    </view>

    <view class="flex-1 flex flex-col px-8 pb-10 z-10">
      <!-- Header Section -->
      <view class="mt-8 mb-12 text-center">
        <view class="inline-block p-4 rounded-full bg-[#f20d33]/10 mb-6">
          <text class="material-icons text-[#f20d33] text-4xl" style="font-size: 36px;">sports_soccer</text>
        </view>
        <text class="text-3xl font-bold text-[#d4af37] tracking-tight block">加入 PitchPulse</text>
        <text class="mt-2 text-slate-400 text-sm block">开启你的 AI 足球社区之旅</text>
      </view>

      <!-- Registration Form -->
      <view class="space-y-5">
        <!-- Nickname Input (Added to match backend) -->
        <view class="relative">
          <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
            <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">badge</text>
          </view>
          <input 
            v-model="form.nickname"
            class="block w-full pl-12 pr-4 py-4 bg-[#221013] border border-white/10 rounded-xl focus:ring-2 focus:ring-[#f20d33] focus:border-transparent transition-all outline-none placeholder:text-slate-500 text-white h-12" 
            placeholder="昵称" 
            placeholder-class="text-slate-500"
            type="text"
          />
        </view>

        <!-- Email Input -->
        <view class="relative mt-4">
          <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
            <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">email</text>
          </view>
          <input 
            v-model="form.email"
            class="block w-full pl-12 pr-4 py-4 bg-[#221013] border border-white/10 rounded-xl focus:ring-2 focus:ring-[#f20d33] focus:border-transparent transition-all outline-none placeholder:text-slate-500 text-white h-12" 
            placeholder="邮箱 (选填)" 
            placeholder-class="text-slate-500"
            type="text"
          />
        </view>

        <!-- Phone Number Input -->
        <view class="relative mt-4">
          <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
            <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">smartphone</text>
          </view>
          <input 
            v-model="form.username"
            class="block w-full pl-12 pr-4 py-4 bg-[#221013] border border-white/10 rounded-xl focus:ring-2 focus:ring-[#f20d33] focus:border-transparent transition-all outline-none placeholder:text-slate-500 text-white h-12" 
            placeholder="手机号" 
            placeholder-class="text-slate-500"
            type="number"
            maxlength="11"
          />
        </view>

        <!-- Verification Code Input -->
        <view class="flex flex-row gap-3 mt-4">
          <view class="relative flex-1">
            <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
              <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">verified_user</text>
            </view>
            <input 
              v-model="form.code"
              class="block w-full pl-12 pr-4 py-4 bg-[#221013] border border-white/10 rounded-xl focus:ring-2 focus:ring-[#f20d33] focus:border-transparent outline-none placeholder:text-slate-500 text-white h-12" 
              placeholder="验证码" 
              placeholder-class="text-slate-500"
              type="number"
              maxlength="6"
            />
          </view>
          <button 
            class="px-5 py-0 h-12 bg-[#d4af37]/10 border border-[#d4af37]/30 text-[#d4af37] font-semibold rounded-xl active:scale-95 transition-transform text-sm whitespace-nowrap flex items-center justify-center"
            @click="getVerificationCode"
            :disabled="countdown > 0"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>

        <!-- Password Input -->
        <view class="relative mt-4">
          <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
            <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">lock_open</text>
          </view>
          <input 
            v-model="form.password"
            class="block w-full pl-12 pr-12 py-4 bg-[#221013] border border-white/10 rounded-xl focus:ring-2 focus:ring-[#f20d33] focus:border-transparent outline-none placeholder:text-slate-500 text-white h-12" 
            placeholder="设置密码" 
            placeholder-class="text-slate-500"
            :password="!showPassword"
            type="text"
          />
          <view class="absolute inset-y-0 right-0 pr-4 flex items-center h-full z-10" @click="togglePasswordVisibility">
            <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">{{ showPassword ? 'visibility' : 'visibility_off' }}</text>
          </view>
        </view>

        <!-- Confirm Password Input -->
        <view class="relative mt-4">
          <view class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none h-full z-10">
            <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">lock</text>
          </view>
          <input 
            v-model="form.confirmPassword"
            class="block w-full pl-12 pr-12 py-4 bg-[#221013] border border-white/10 rounded-xl focus:ring-2 focus:ring-[#f20d33] focus:border-transparent outline-none placeholder:text-slate-500 text-white h-12" 
            placeholder="确认密码" 
            placeholder-class="text-slate-500"
            :password="!showConfirmPassword"
            type="text"
          />
          <view class="absolute inset-y-0 right-0 pr-4 flex items-center h-full z-10" @click="toggleConfirmPasswordVisibility">
            <text class="material-icons text-slate-500 text-xl" style="font-size: 20px;">{{ showConfirmPassword ? 'visibility' : 'visibility_off' }}</text>
          </view>
        </view>

        <!-- Terms & Privacy -->
        <view class="flex items-start gap-3 pt-2 mt-4">
          <checkbox-group @change="handleAgreementChange">
            <label class="flex items-center">
              <checkbox value="agreed" :checked="isAgreed" color="#f20d33" style="transform:scale(0.7)" />
              <view class="text-xs text-slate-400 leading-relaxed ml-2">
                我已阅读并同意 <text class="text-[#d4af37]">《用户协议》</text> 与 <text class="text-[#d4af37]">《隐私政策》</text>
              </view>
            </label>
          </checkbox-group>
        </view>

        <!-- Register Button -->
        <button 
          class="w-full mt-6 py-4 bg-[#f20d33] text-white font-bold rounded-xl shadow-lg shadow-[#f20d33]/20 active:scale-[0.98] transition-all flex flex-row items-center justify-center gap-2 border-none h-12"
          @click="handleRegister"
          :loading="loading"
        >
          <text>立即注册</text>
          <text class="material-icons text-lg" style="font-size: 18px;">arrow_forward</text>
        </button>
      </view>

      <!-- Secondary Actions -->
      <view class="mt-auto pt-8 text-center flex justify-center">
        <text class="text-slate-400 text-sm">已有账号？ </text>
        <text class="text-[#f20d33] font-semibold ml-1" @click="goToLogin">立即登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api'
import { isMobile, isEmail } from '@/utils/validate'

const form = ref({
  nickname: '',
  email: '',
  username: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)
const isAgreed = ref(false)
const countdown = ref(0)
let timer = null

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const toggleConfirmPasswordVisibility = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

const handleAgreementChange = (e) => {
  isAgreed.value = e.detail.value.includes('agreed')
}

const getVerificationCode = async () => {
  if (!form.value.username) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!isMobile(form.value.username)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (countdown.value > 0) return
  
  uni.showLoading({ title: '发送中...' })
  try {
    const code = await authApi.sendCode({ phone: form.value.username })
    uni.hideLoading()
    
    // For development/demo purposes, we can show the code in a toast or console
    // In production, the user receives SMS
    console.log('Verification code:', code)
    uni.showToast({ title: '验证码已发送: ' + code, icon: 'none', duration: 3000 })

    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    uni.hideLoading()
    console.error('发送验证码失败:', e)
  }
}

const handleRegister = async () => {
  if (!form.value.nickname) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  if (!form.value.username) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!isMobile(form.value.username)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (form.value.email && !isEmail(form.value.email)) {
    uni.showToast({ title: '请输入正确的邮箱格式', icon: 'none' })
    return
  }
  if (!form.value.code) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (!form.value.password) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  if (!isAgreed.value) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
    return
  }

  loading.value = true
  try {
    // 构造后端需要的参数
    const data = {
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname,
      email: form.value.email,
      phone: form.value.username // 手机号即用户名
    }
    
    await authApi.register(data)
    
    uni.showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => {
      // 注册成功后跳转到登录页，使用 redirectTo 避免返回堆栈冗余
      uni.redirectTo({
        url: '/pages/login/login'
      })
    }, 1500)
  } catch (e) {
    console.error('注册失败:', e)
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  uni.navigateTo({
    url: '/pages/login/login'
  })
}
</script>

<style>

.bg-stadium-gradient {
  background: radial-gradient(circle at top right, #2d1418 0%, #12080a 70%);
}
</style>