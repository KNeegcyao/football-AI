<template>
  <view class="page-container bg-stadium-gradient min-h-screen flex flex-col font-display relative overflow-hidden max-w-[500px] mx-auto shadow-2xl">
    <!-- Background Overlay Texture -->
    <image 
      class="absolute inset-0 z-0 opacity-40 w-full h-full object-cover pointer-events-none" 
      style="object-position: 85% bottom;"
      src="https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&q=80&w=1000" 
      mode="aspectFill"
    ></image>

    <view class="flex-1 flex flex-col items-center justify-center px-6 py-8 z-10 w-full mx-auto">
      <!-- Header Section: Logo & Branding -->
      <view class="w-full text-center mb-8">
        <view class="inline-flex items-center justify-center w-16 h-16 bg-[#f20d33]/10 rounded-xl mb-4 border border-[#f20d33]/20">
          <text class="material-icons text-[#f20d33] text-4xl" style="font-size: 36px;">sports_soccer</text>
        </view>
        <view class="flex flex-col items-center">
          <text class="text-3xl font-extrabold tracking-tighter text-[#d4af37] gold-glow italic mb-2">PITCHPULSE</text>
          <text class="text-[10px] uppercase tracking-[0.3em] text-[#f20d33]/80 font-semibold">AI Football Community</text>
        </view>
      </view>

      <!-- Login Form Section -->
      <view class="w-full space-y-5">
        <view class="space-y-4">
          <!-- Username/Email Field -->
          <view class="w-full bg-[#1a0d0f] border border-[#f20d33]/10 rounded-xl px-4 flex items-center h-11 transition-all duration-300 focus-within:border-[#d4af37]">
            <text class="material-icons text-slate-500 mr-3" style="font-size: 18px; color: #64748b;">person</text>
            <input 
              v-model="form.username"
              class="flex-1 bg-transparent text-white text-sm h-full placeholder-slate-500"
              placeholder="手机号 / 邮箱" 
              placeholder-class="text-slate-500"
              type="text"
            />
          </view>

          <!-- Password Field (Visible when not in Code Login mode) -->
          <view v-if="!isCodeLogin" class="w-full bg-[#1a0d0f] border border-[#f20d33]/10 rounded-xl px-4 flex items-center h-11 mt-4 transition-all duration-300 focus-within:border-[#d4af37]">
            <text class="material-icons text-slate-500 mr-3" style="font-size: 18px; color: #64748b;">lock</text>
            <input 
              v-model="form.password"
              class="flex-1 bg-transparent text-white text-sm h-full placeholder-slate-500"
              placeholder="密码" 
              placeholder-class="text-slate-500"
              :password="!showPassword"
              type="text"
            />
            <view class="ml-2 flex items-center h-full" @click="togglePasswordVisibility">
              <text class="material-icons text-slate-500" style="font-size: 18px; color: #64748b;">{{ showPassword ? 'visibility' : 'visibility_off' }}</text>
            </view>
          </view>

          <!-- Code Field (Visible when in Code Login mode) -->
          <view v-else class="flex gap-2 mt-4">
            <view class="flex-1 bg-[#1a0d0f] border border-[#f20d33]/10 rounded-xl px-4 flex items-center h-11 transition-all duration-300 focus-within:border-[#d4af37]">
              <text class="material-icons text-slate-500 mr-3" style="font-size: 18px; color: #64748b;">security</text>
              <input 
                v-model="codeLoginForm.code"
                class="flex-1 bg-transparent text-white text-sm h-full placeholder-slate-500"
                placeholder="验证码" 
                placeholder-class="text-slate-500"
                type="number"
                maxlength="6"
              />
            </view>
            <button 
              class="bg-[#f20d33]/10 text-[#f20d33] text-xs px-3 rounded-xl border border-[#f20d33]/20 flex items-center justify-center whitespace-nowrap min-w-[90px] h-11"
              @click="handleSendLoginCode"
              :disabled="loginCountdown > 0"
            >
              {{ loginCountdown > 0 ? `${loginCountdown}s` : '获取验证码' }}
            </button>
          </view>
        </view>

        <!-- Forgot Password & Register Navigation -->
        <view class="flex flex-row justify-between items-center text-xs px-1 w-full mt-3">
          <text v-if="!isCodeLogin" class="text-slate-400 hover:text-[#f20d33] transition-colors cursor-pointer" @click="showForgotModal = true">忘记密码?</text>
          <text v-else class="text-slate-400 hover:text-[#f20d33] transition-colors cursor-pointer" @click="isCodeLogin = false">使用密码登录</text>
          <text class="text-[#d4af37] font-medium cursor-pointer" @click="goToRegister">立即注册</text>
        </view>

        <!-- Main Login Button -->
        <button 
          class="w-full bg-[#f20d33] hover:bg-[#f20d33]/90 text-white font-bold py-3 rounded-xl shadow-lg shadow-[#f20d33]/20 transition-all flex flex-row items-center justify-center space-x-2 mt-6 border-none"
          @click="handleLogin"
          :loading="loading"
        >
          <text class="mr-2 text-sm">{{ isCodeLogin ? '登录 / 注册' : '进入球场' }}</text>
          <text class="material-icons text-base" style="font-size: 16px;">login</text>
        </button>
      </view>

      <!-- Social Login & Agreement Footer -->
      <view class="w-full mt-12 space-y-6 flex flex-col items-center">
        <view class="relative w-full flex items-center justify-center">
          <view class="absolute inset-0 flex items-center">
            <view class="w-full border-t border-[#f20d33]/10"></view>
          </view>
          <view class="relative bg-[#0a0506] px-4">
            <text class="text-xs uppercase text-slate-500 tracking-widest">其他登录方式</text>
          </view>
        </view>
        
        <view class="flex flex-row justify-center space-x-8 gap-8 mt-6">
          <!-- Code Login Toggle -->
          <view 
            v-if="!isCodeLogin"
            class="w-12 h-12 flex items-center justify-center rounded-full bg-[#1a0d0f] border border-[#f20d33]/10 text-slate-300 hover:text-[#d4af37] hover:border-[#d4af37] transition-all cursor-pointer" 
            @click="isCodeLogin = true"
          >
             <text class="material-icons" style="font-size: 24px; color: #cbd5e1;">sms</text>
          </view>
          <!-- Password Login Toggle -->
          <view 
            v-else
            class="w-12 h-12 flex items-center justify-center rounded-full bg-[#1a0d0f] border border-[#f20d33]/10 text-slate-300 hover:text-[#d4af37] hover:border-[#d4af37] transition-all cursor-pointer" 
            @click="isCodeLogin = false"
          >
             <text class="material-icons" style="font-size: 24px; color: #cbd5e1;">password</text>
          </view>
          
          <!-- WeChat -->
          <view class="w-12 h-12 flex items-center justify-center rounded-full bg-[#1a0d0f] border border-[#f20d33]/10 text-slate-300 hover:text-[#d4af37] hover:border-[#d4af37] transition-all">
            <!-- Simple SVG replacement with text or icon for now, as inline SVG is tricky in uniapp without plugin -->
            <text class="material-icons" style="font-size: 24px; color: #cbd5e1;">chat</text>
          </view>
          <!-- Apple -->
          <view class="w-12 h-12 flex items-center justify-center rounded-full bg-[#1a0d0f] border border-[#f20d33]/10 text-slate-300 hover:text-[#d4af37] hover:border-[#d4af37] transition-all">
            <text class="material-icons" style="font-size: 24px; color: #cbd5e1;">apple</text>
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

    <!-- Forgot Password Modal -->
    <view v-if="showForgotModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <view class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="showForgotModal = false"></view>
      <view class="relative w-full max-w-sm bg-[#1A1811] rounded-2xl border border-[#d4af37]/20 p-6 shadow-2xl shadow-black/50">
        <text class="text-xl font-bold text-white mb-6 block text-center">重置密码</text>
        
        <view class="space-y-4">
          <!-- Phone -->
          <view class="w-full bg-[#0a0506] border border-[#f20d33]/10 rounded-xl px-4 py-3 flex items-center">
            <input 
              v-model="forgotForm.phone"
              class="flex-1 bg-transparent text-white text-sm h-full"
              placeholder="请输入手机号" 
              placeholder-class="text-slate-500"
              type="number"
              maxlength="11"
            />
          </view>
          
          <!-- Code -->
          <view class="flex gap-2">
            <view class="flex-1 bg-[#0a0506] border border-[#f20d33]/10 rounded-xl px-4 py-3 flex items-center">
              <input 
                v-model="forgotForm.code"
                class="flex-1 bg-transparent text-white text-sm h-full"
                placeholder="验证码" 
                placeholder-class="text-slate-500"
                type="number"
                maxlength="6"
              />
            </view>
            <button 
              class="bg-[#f20d33]/10 text-[#f20d33] text-xs px-3 rounded-xl border border-[#f20d33]/20 flex items-center justify-center whitespace-nowrap min-w-[100px]"
              @click="handleSendCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
            </button>
          </view>
          
          <!-- New Password -->
          <view class="w-full bg-[#0a0506] border border-[#f20d33]/10 rounded-xl px-4 py-3 flex items-center">
            <input 
              v-model="forgotForm.password"
              class="flex-1 bg-transparent text-white text-sm h-full"
              placeholder="新密码" 
              placeholder-class="text-slate-500"
              password
            />
          </view>
          
          <!-- Confirm Password -->
          <view class="w-full bg-[#0a0506] border border-[#f20d33]/10 rounded-xl px-4 py-3 flex items-center">
            <input 
              v-model="forgotForm.confirmPassword"
              class="flex-1 bg-transparent text-white text-sm h-full"
              placeholder="确认新密码" 
              placeholder-class="text-slate-500"
              password
            />
          </view>
        </view>
        
        <view class="flex gap-3 mt-8">
          <button class="flex-1 bg-white/5 text-slate-300 text-sm py-3 rounded-xl" @click="showForgotModal = false">
            取消
          </button>
          <button class="flex-1 bg-[#d4af37] text-[#1A1811] text-sm font-bold py-3 rounded-xl shadow-lg shadow-[#d4af37]/20" @click="handleResetPassword">
            确认重置
          </button>
        </view>
      </view>
    </view>

      <!-- Verification Code Popup -->
    <view 
      v-if="showCodePopup" 
      class="fixed top-1/2 left-1/2 z-[60] bg-white/95 text-black px-8 py-6 rounded-2xl shadow-2xl flex flex-col items-center justify-center fade-out-anim"
    >
      <text class="text-sm text-gray-500 mb-2">您的验证码是</text>
      <text class="text-3xl font-bold tracking-widest text-[#f20d33]">{{ currentCode }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi, userApi } from '@/api'
import { isMobile } from '@/utils/validate'

const form = ref({
  username: '',
  password: ''
})

const showPassword = ref(false)
const loading = ref(false)
const isAgreed = ref(false)
const isCodeLogin = ref(false)

// Forgot Password Logic
const showForgotModal = ref(false)
const forgotForm = ref({
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})
const countdown = ref(0)
const generatedCode = ref('')
const showCodePopup = ref(false)
const currentCode = ref('')

// Code Login Logic
// showCodeLoginModal removed in favor of isCodeLogin toggle
const codeLoginForm = ref({
  phone: '',
  code: ''
})
const loginCountdown = ref(0)
const generatedLoginCode = ref('')

const handleSendLoginCode = async () => {
  const phone = form.value.username
  
  if (!phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!isMobile(phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (loginCountdown.value > 0) return

  uni.showLoading({ title: '发送中...' })
  try {
    const code = await authApi.sendCode({ phone })
    uni.hideLoading()
    
    generatedLoginCode.value = code
    currentCode.value = code
    
    showCodePopup.value = true
    setTimeout(() => {
      showCodePopup.value = false
    }, 3000)

    loginCountdown.value = 60
    const timer = setInterval(() => {
      loginCountdown.value--
      if (loginCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
    uni.showToast({ title: '验证码已发送', icon: 'success' })
  } catch (e) {
    uni.hideLoading()
    console.error('发送验证码失败:', e)
  }
}

const handleSendCode = async () => {
  if (!forgotForm.value.phone) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  if (!isMobile(forgotForm.value.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (countdown.value > 0) return

  uni.showLoading({ title: '发送中...' })
  try {
    const code = await authApi.sendCode({ phone: forgotForm.value.phone })
    uni.hideLoading()
    
    // Generate 6-digit code
    generatedCode.value = code
    currentCode.value = code
    
    // Show popup with fading effect
    showCodePopup.value = true
    setTimeout(() => {
      showCodePopup.value = false
    }, 3000) // Fade out after 3 seconds

    // Start countdown
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
    uni.showToast({ title: '验证码已发送', icon: 'success' })
  } catch (e) {
    uni.hideLoading()
    console.error('发送验证码失败:', e)
  }
}

const handleResetPassword = async () => {
  if (!forgotForm.value.phone || !forgotForm.value.code || !forgotForm.value.password || !forgotForm.value.confirmPassword) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  // 前端简单校验，最终以服务端为准
  if (generatedCode.value && forgotForm.value.code !== generatedCode.value) {
    uni.showToast({ title: '验证码错误', icon: 'none' })
    return
  }
  if (forgotForm.value.password !== forgotForm.value.confirmPassword) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
    return
  }

  // API call
  uni.showLoading({ title: '重置中...' })
  try {
    await authApi.resetPassword({
      username: forgotForm.value.phone,
      password: forgotForm.value.password,
      code: forgotForm.value.code
    })
    
    uni.hideLoading()
    uni.showToast({ title: '密码重置成功', icon: 'success' })
    showForgotModal.value = false
    // Reset form
    forgotForm.value = { phone: '', code: '', password: '', confirmPassword: '' }
    generatedCode.value = ''
    countdown.value = 0
  } catch (e) {
    console.error('重置密码失败:', e)
    uni.hideLoading()
    // 错误信息由 request.js 统一处理
  }
}

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
  if (!isAgreed.value) {
    uni.showToast({ title: '请先阅读并同意服务协议', icon: 'none' })
    return
  }

  if (isCodeLogin.value) {
    // 验证码登录逻辑
    const phone = form.value.username
    const code = codeLoginForm.value.code
    
    if (!phone || !code) {
      uni.showToast({ title: '请输入手机号和验证码', icon: 'none' })
      return
    }
    
    if (generatedLoginCode.value && code !== generatedLoginCode.value) {
      uni.showToast({ title: '验证码错误', icon: 'none' })
      return
    }
    
    loading.value = true
    try {
      const data = await authApi.loginByCode({
        phone: phone,
        code: code
      })
      
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('userInfo', JSON.stringify(data.userInfo))
      
      uni.showToast({ title: '登录成功', icon: 'success' })
      
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index/index' })
      }, 1500)
    } catch (e) {
      console.error('登录失败:', e)
    } finally {
      loading.value = false
    }
  } else {
    // 密码登录逻辑 (原有逻辑)
    if (!form.value.username || !form.value.password) {
      uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
      return
    }

    loading.value = true
    try {
      const data = await authApi.login(form.value)
      
      uni.setStorageSync('token', data.token)
      
      // 获取用户信息
      try {
        const userRes = await userApi.getProfile()
        uni.setStorageSync('userInfo', JSON.stringify(userRes))
      } catch (err) {
        console.error('获取用户信息失败:', err)
      }
      
      uni.showToast({ title: '登录成功', icon: 'success' })
      
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index/index' })
      }, 1500)
    } catch (e) {
      console.error('登录失败:', e)
    } finally {
      loading.value = false
    }
  }
}
</script>

<style>
/* 引入字体图标，如果项目中已有全局引入可省略 */

.bg-stadium-gradient {
  background: radial-gradient(circle at top, #221013 0%, #0a0506 100%);
}
.gold-glow {
  text-shadow: 0 0 15px rgba(212, 175, 55, 0.4);
}

.fade-out-anim {
  transform: translate(-50%, -50%);
  animation: fadeOut 3s forwards;
}

@keyframes fadeOut {
  0% { opacity: 0; transform: translate(-50%, -50%) scale(0.9); }
  10% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
  70% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
  100% { opacity: 0; transform: translate(-50%, -50%) scale(0.95); }
}
</style>