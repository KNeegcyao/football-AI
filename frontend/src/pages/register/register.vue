<template>
  <view class="page-container">
    <!-- Background Layer -->
    <view class="background-wrapper">
      <image 
        class="bg-image" 
        src="https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/pexels-florian-thomschke-833330274-19436350.jpg" 
        mode="aspectFill"
      ></image>
      <view class="bg-overlay"></view>
    </view>

    <!-- Content Layer -->
    <view class="content-wrapper">
      <!-- Header Section: Logo & Branding -->
      <view class="header-section">
        <view class="logo-box">
          <image 
            class="logo-image" 
            src="https://ai-football-kneeg.oss-cn-beijing.aliyuncs.com/logo/logo.png" 
            mode="aspectFit"
          ></image>
        </view>
        <view class="branding-box">
          <text class="brand-name italic">Pitch<text class="highlight">Pulse</text></text>
          <text class="brand-tagline">开启你的 AI 足球社区之旅</text>
        </view>
      </view>

      <!-- Registration Form Section -->
      <view class="form-section">
        <view class="input-group">
          <!-- Nickname Input -->
          <view class="input-item">
            <text class="material-icons input-icon">badge</text>
            <input 
              v-model="form.nickname"
              class="input-field"
              placeholder="昵称" 
              placeholder-class="input-placeholder"
              type="text"
            />
          </view>

          <!-- Email Input -->
          <view class="input-item mt-4">
            <text class="material-icons input-icon">email</text>
            <input 
              v-model="form.email"
              class="input-field"
              placeholder="邮箱 (选填)" 
              placeholder-class="input-placeholder"
              type="text"
            />
          </view>

          <!-- Phone Number Input -->
          <view class="input-item mt-4">
            <text class="material-icons input-icon">smartphone</text>
            <input 
              v-model="form.username"
              class="input-field"
              placeholder="手机号" 
              placeholder-class="input-placeholder"
              type="number"
              maxlength="11"
            />
          </view>

          <!-- Verification Code Input -->
          <view class="code-input-group mt-4">
            <view class="input-item flex-1">
              <text class="material-icons input-icon">security</text>
              <input 
                v-model="form.code"
                class="input-field"
                placeholder="验证码" 
                placeholder-class="input-placeholder"
                type="number"
                maxlength="6"
              />
            </view>
            <button 
              class="send-code-btn"
              @click="getVerificationCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </view>

          <!-- Password Input -->
          <view class="input-item mt-4">
            <text class="material-icons input-icon">lock_open</text>
            <input 
              v-model="form.password"
              class="input-field"
              placeholder="设置密码" 
              placeholder-class="input-placeholder"
              :password="!showPassword"
              type="text"
            />
            <view class="password-toggle" @click="togglePasswordVisibility">
              <text class="material-icons toggle-icon">{{ showPassword ? 'visibility' : 'visibility_off' }}</text>
            </view>
          </view>

          <!-- Confirm Password Input -->
          <view class="input-item mt-4">
            <text class="material-icons input-icon">lock</text>
            <input 
              v-model="form.confirmPassword"
              class="input-field"
              placeholder="确认密码" 
              placeholder-class="input-placeholder"
              :password="!showConfirmPassword"
              type="text"
            />
            <view class="password-toggle" @click="toggleConfirmPasswordVisibility">
              <text class="material-icons toggle-icon">{{ showConfirmPassword ? 'visibility' : 'visibility_off' }}</text>
            </view>
          </view>
        </view>

        <!-- Terms & Privacy -->
        <view class="agreement-box">
          <checkbox-group @change="handleAgreementChange">
            <label class="agreement-label">
              <checkbox value="agreed" :checked="isAgreed" color="#f20d33" />
              <view class="agreement-text">
                已阅读并同意 <text class="link-highlight">《用户协议》</text> 与 <text class="link-highlight">《隐私政策》</text>
              </view>
            </label>
          </checkbox-group>
        </view>

        <!-- Register Button -->
        <button 
          class="login-btn mt-6"
          @click="handleRegister"
          :loading="loading"
        >
          <text class="btn-text">立即注册</text>
          <text class="material-icons btn-icon">arrow_forward</text>
        </button>

        <!-- Secondary Actions -->
        <view class="nav-links justify-center mt-6">
          <text class="link-text">已有账号？</text>
          <text class="link-text highlight ml-1" @click="goToLogin">立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '@/api'

const isMobile = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

const isEmail = (email) => {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

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
      phone: form.value.username, // 手机号即用户名
      code: form.value.code // 传递验证码
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
    uni.showToast({
      title: e.message || '注册失败，请稍后重试',
      icon: 'none'
    })
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

<style lang="scss" scoped>
.page-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-color: #000;
}

.background-wrapper {
  position: absolute;
  inset: 0;
  z-index: 0;
  width: 100%;
  height: 100%;

  .bg-image {
    width: 100%;
    height: 100%;
    opacity: 0.7;
  }

  .bg-overlay {
    position: absolute;
    inset: 0;
    background-color: rgba(0, 0, 0, 0.4);
  }
}

.content-wrapper {
  position: relative;
  z-index: 10;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 48rpx;
  width: 100%;
  box-sizing: border-box;
}

.header-section {
  text-align: center;
  margin-bottom: 60rpx;
  width: 100%;

  .logo-box {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 20rpx;
    margin-left: auto;
    margin-right: auto;
    position: relative;

    .logo-image {
      display: block;
      width: 100%;
      height: 100%;
      padding: 0;
      filter: drop-shadow(0 15rpx 30rpx rgba(0,0,0,0.8));
    }
  }

  .branding-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    margin-top: -10rpx;

    .brand-name {
      text-align: center;
      font-size: 72rpx;
      font-weight: 900;
      color: #d4af37;
      letter-spacing: -2rpx;
      margin-bottom: 12rpx;
      text-shadow: 0 4rpx 15rpx rgba(0,0,0,0.8);

      .highlight {
        color: #fff;
        margin-left: 12rpx;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          bottom: -4rpx;
          left: 0;
          width: 100%;
          height: 4rpx;
          background: linear-gradient(90deg, transparent, #d4af37, transparent);
        }
      }
    }

    .brand-tagline {
      text-align: center;
      font-size: 24rpx;
      text-transform: uppercase;
      letter-spacing: 0.2em;
      color: rgba(255, 255, 255, 0.8);
      font-weight: 600;
      opacity: 0.9;
    }
  }
}

.form-section {
  width: 100%;

  .input-group {
    margin-bottom: 40rpx;
  }

  .input-item {
    width: 100%;
    background-color: rgba(26, 13, 15, 0.6);
    backdrop-filter: blur(20rpx);
    border: 2rpx solid rgba(242, 13, 51, 0.2);
    border-radius: 24rpx;
    padding: 0 32rpx;
    display: flex;
    align-items: center;
    height: 96rpx;
    box-sizing: border-box;

    .input-icon {
      color: #94a3b8;
      margin-right: 24rpx;
      font-size: 44rpx;
    }

    .input-field {
      flex: 1;
      height: 100%;
      color: #fff;
      font-size: 28rpx;
    }

    .input-placeholder {
      color: rgba(148, 163, 184, 0.5);
    }
  }

  .code-input-group {
    display: flex;
    gap: 16rpx;
    
    .send-code-btn {
      background-color: rgba(242, 13, 51, 0.1);
      color: #f20d33;
      font-size: 24rpx;
      padding: 0 32rpx;
      border-radius: 24rpx;
      border: 2rpx solid rgba(242, 13, 51, 0.2);
      height: 96rpx;
      line-height: 96rpx;
      min-width: 200rpx;
      margin: 0;

      &::after {
        border: none;
      }

      &:active {
        opacity: 0.7;
      }
    }
  }

  .password-toggle {
    padding-left: 16rpx;
    height: 100%;
    display: flex;
    align-items: center;

    .toggle-icon {
      color: #94a3b8;
      font-size: 44rpx;
    }
  }

  .agreement-box {
    margin-top: 32rpx;
    padding: 0 16rpx;

    .agreement-label {
      display: flex;
      align-items: center;
    }

    .agreement-text {
      font-size: 24rpx;
      color: #64748b;
      margin-left: 16rpx;
      line-height: 1.4;

      .link-highlight {
        color: #cbd5e1;
      }
    }
  }

  .nav-links {
    display: flex;
    padding: 0 8rpx;
    margin-top: 24rpx;

    &.justify-center {
      justify-content: center;
    }

    .link-text {
      font-size: 24rpx;
      color: #94a3b8;

      &.highlight {
        color: #d4af37;
        font-weight: 500;
      }
    }
  }

  .login-btn {
    width: 100%;
    background-color: #f20d33;
    color: #fff;
    font-weight: bold;
    height: 96rpx;
    border-radius: 24rpx;
    margin-top: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10rpx 30rpx rgba(242, 13, 51, 0.2);
    border: none;

    &::after {
      border: none;
    }

    .btn-text {
      font-size: 28rpx;
      margin-right: 16rpx;
    }

    .btn-icon {
      font-size: 44rpx;
    }
  }
}

/* Utils */
.flex-1 { flex: 1; }
.mt-4 { margin-top: 32rpx; }
.mt-6 { margin-top: 48rpx; }
.ml-1 { margin-left: 8rpx; }

/* 小程序特定的 checkbox 样式覆盖 */
/* #ifdef MP-WEIXIN */
checkbox .wx-checkbox-input {
  width: 32rpx;
  height: 32rpx;
  border-radius: 8rpx;
  background-color: transparent;
  border: 2rpx solid #64748b;
}
checkbox .wx-checkbox-input.wx-checkbox-input-checked {
  background-color: #f20d33;
  border-color: #f20d33;
}
checkbox .wx-checkbox-input.wx-checkbox-input-checked::before {
  color: #fff;
  font-size: 24rpx;
}
/* #endif */
</style>