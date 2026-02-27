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
          <text class="material-icons logo-icon">sports_soccer</text>
        </view>
        <view class="branding-box">
          <text class="brand-name italic">PITCHPULSE</text>
          <text class="brand-tagline">AI Football Community</text>
        </view>
      </view>

      <!-- Login Form Section -->
      <view class="form-section">
        <view class="input-group">
          <!-- Username/Email Field -->
          <view class="input-item">
            <text class="material-icons input-icon">person</text>
            <input 
              v-model="form.username"
              class="input-field"
              placeholder="手机号 / 邮箱" 
              placeholder-class="input-placeholder"
              type="text"
            />
          </view>

          <!-- Password Field -->
          <view v-if="!isCodeLogin" class="input-item mt-4">
            <text class="material-icons input-icon">lock</text>
            <input 
              v-model="form.password"
              class="input-field"
              placeholder="密码" 
              placeholder-class="input-placeholder"
              :password="!showPassword"
              type="text"
            />
            <view class="password-toggle" @click="togglePasswordVisibility">
              <text class="material-icons toggle-icon">{{ showPassword ? 'visibility' : 'visibility_off' }}</text>
            </view>
          </view>

          <!-- Code Field (Visible when in Code Login mode) -->
          <view v-else class="code-input-group mt-4">
            <view class="input-item flex-1">
              <text class="material-icons input-icon">security</text>
              <input 
                v-model="codeLoginForm.code"
                class="input-field"
                placeholder="验证码" 
                placeholder-class="input-placeholder"
                type="number"
                maxlength="6"
              />
            </view>
            <button 
              class="send-code-btn"
              @click="handleSendLoginCode"
              :disabled="loginCountdown > 0"
            >
              {{ loginCountdown > 0 ? `${loginCountdown}s` : '获取验证码' }}
            </button>
          </view>
        </view>

        <!-- Forgot Password & Register Navigation -->
        <view class="nav-links">
          <text v-if="!isCodeLogin" class="link-text" @click="showForgotModal = true">忘记密码?</text>
          <text v-else class="link-text" @click="isCodeLogin = false">使用密码登录</text>
          <text class="link-text highlight" @click="goToRegister">立即注册</text>
        </view>

        <!-- Main Login Button -->
        <button 
          class="login-btn"
          @click="handleLogin"
          :loading="loading"
        >
          <text class="btn-text">{{ isCodeLogin ? '登录 / 注册' : '进入球场' }}</text>
          <text class="material-icons btn-icon">login</text>
        </button>
      </view>

      <!-- Social Login & Agreement Footer -->
      <view class="footer-section">
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">其他登录方式</text>
          <view class="divider-line"></view>
        </view>
        
        <view class="social-icons">
          <!-- Code/Password Toggle -->
          <view class="social-btn" @click="isCodeLogin = !isCodeLogin">
             <text class="material-icons">{{ isCodeLogin ? 'password' : 'sms' }}</text>
          </view>
          <!-- WeChat -->
          <view class="social-btn">
            <text class="material-icons">chat</text>
          </view>
          <!-- Apple -->
          <view class="social-btn">
            <text class="material-icons">apple</text>
          </view>
        </view>

        <view class="agreement-box">
          <checkbox-group @change="handleAgreementChange">
            <label class="agreement-label">
              <checkbox value="agreed" :checked="isAgreed" color="#f20d33" />
              <view class="agreement-text">
                已阅读并同意 <text class="link-highlight">服务协议</text>、<text class="link-highlight">隐私政策</text> 以及 <text class="link-highlight">社区守则</text>
              </view>
            </label>
          </checkbox-group>
        </view>
      </view>
    </view>
    
    <!-- Footer Decoration -->
    <view class="footer-decoration"></view>

    <!-- Forgot Password Modal -->
    <view v-if="showForgotModal" class="modal-overlay">
      <view class="modal-backdrop" @click="showForgotModal = false"></view>
      <view class="modal-content">
        <text class="modal-title">重置密码</text>
        
        <view class="modal-form">
          <view class="modal-input-item">
            <input v-model="forgotForm.phone" class="modal-input" placeholder="请输入手机号" placeholder-class="input-placeholder" type="number" maxlength="11" />
          </view>
          
          <view class="modal-code-group">
            <view class="modal-input-item flex-1">
              <input v-model="forgotForm.code" class="modal-input" placeholder="验证码" placeholder-class="input-placeholder" type="number" maxlength="6" />
            </view>
            <button class="modal-send-btn" @click="handleSendCode" :disabled="countdown > 0">
              {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
            </button>
          </view>
          
          <view class="modal-input-item">
            <input v-model="forgotForm.password" class="modal-input" placeholder="新密码" placeholder-class="input-placeholder" password />
          </view>
          
          <view class="modal-input-item">
            <input v-model="forgotForm.confirmPassword" class="modal-input" placeholder="确认新密码" placeholder-class="input-placeholder" password />
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="modal-cancel-btn" @click="showForgotModal = false">取消</button>
          <button class="modal-confirm-btn" @click="handleResetPassword">确认重置</button>
        </view>
      </view>
    </view>

    <!-- Verification Code Popup -->
    <view v-if="showCodePopup" class="code-popup">
      <text class="popup-label">您的验证码是</text>
      <text class="popup-code">{{ currentCode }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { authApi, userApi } from '@/api'

const isMobile = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

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
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 128rpx;
    height: 128rpx;
    background-color: rgba(242, 13, 51, 0.1);
    border-radius: 24rpx;
    border: 2rpx solid rgba(242, 13, 51, 0.2);
    margin-bottom: 32rpx;

    .logo-icon {
      color: #f20d33;
      font-size: 72rpx;
    }
  }

  .branding-box {
    display: flex;
    flex-direction: column;
    align-items: center;

    .brand-name {
      font-size: 60rpx;
      font-weight: 800;
      color: #d4af37;
      letter-spacing: -2rpx;
      margin-bottom: 8rpx;
      text-shadow: 0 0 20rpx rgba(212, 175, 55, 0.3);
    }

    .brand-tagline {
      font-size: 20rpx;
      text-transform: uppercase;
      letter-spacing: 0.3em;
      color: rgba(255, 255, 255, 0.9);
      font-weight: 600;
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
      font-size: 40rpx;
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
      font-size: 40rpx;
    }
  }

  .nav-links {
    display: flex;
    justify-content: space-between;
    padding: 0 8rpx;
    margin-top: 24rpx;

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
      font-size: 32rpx;
    }
  }
}

.footer-section {
  width: 100%;
  margin-top: 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;

  .divider {
    position: relative;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 32rpx;

    .divider-line {
      flex: 1;
      height: 2rpx;
      background-color: rgba(242, 13, 51, 0.1);
    }

    .divider-text {
      font-size: 24rpx;
      color: #94a3b8;
      text-transform: uppercase;
      letter-spacing: 4rpx;
    }
  }

  .social-icons {
    display: flex;
    justify-content: center;
    gap: 64rpx;
    margin-top: 48rpx;

    .social-btn {
      width: 96rpx;
      height: 96rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background-color: #1a0d0f;
      border: 2rpx solid rgba(242, 13, 51, 0.1);
      color: #cbd5e1;

      .material-icons {
        font-size: 48rpx;
      }
    }
  }

  .agreement-box {
    margin-top: 64rpx;
    padding: 0 16rpx;

    .agreement-label {
      display: flex;
      align-items: flex-start;
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
}

.footer-decoration {
  position: absolute;
  bottom: 0;
  width: 100%;
  height: 2rpx;
  background: linear-gradient(to right, transparent, #f20d33, transparent);
  opacity: 0.3;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
}

.modal-backdrop {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(8rpx);
}

.modal-content {
  position: relative;
  width: 100%;
  max-width: 600rpx;
  background-color: #1A1811;
  border-radius: 32rpx;
  border: 2rpx solid rgba(212, 175, 55, 0.2);
  padding: 48rpx;
  box-shadow: 0 40rpx 80rpx rgba(0, 0, 0, 0.5);

  .modal-title {
    display: block;
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
    text-align: center;
    margin-bottom: 48rpx;
  }
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.modal-input-item {
  background-color: #0a0506;
  border: 2rpx solid rgba(242, 13, 51, 0.1);
  border-radius: 24rpx;
  padding: 0 32rpx;
  height: 88rpx;
  display: flex;
  align-items: center;

  .modal-input {
    flex: 1;
    height: 100%;
    color: #fff;
    font-size: 28rpx;
  }
}

.modal-code-group {
  display: flex;
  gap: 16rpx;

  .modal-send-btn {
    background-color: rgba(242, 13, 51, 0.1);
    color: #f20d33;
    font-size: 24rpx;
    border-radius: 24rpx;
    border: 2rpx solid rgba(242, 13, 51, 0.2);
    height: 88rpx;
    line-height: 88rpx;
    min-width: 200rpx;
    margin: 0;

    &::after {
      border: none;
    }
  }
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  margin-top: 64rpx;

  button {
    flex: 1;
    height: 88rpx;
    border-radius: 24rpx;
    font-size: 28rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;

    &::after {
      border: none;
    }
  }

  .modal-cancel-btn {
    background-color: rgba(255, 255, 255, 0.05);
    color: #94a3b8;
  }

  .modal-confirm-btn {
    background-color: #d4af37;
    color: #1A1811;
    font-weight: bold;
    box-shadow: 0 10rpx 30rpx rgba(212, 175, 55, 0.2);
  }
}

/* Popup Styles */
.code-popup {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 150;
  background-color: rgba(26, 13, 15, 0.95);
  backdrop-filter: blur(40rpx);
  border: 2rpx solid rgba(212, 175, 55, 0.3);
  padding: 48rpx 64rpx;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 40rpx 100rpx rgba(0, 0, 0, 0.6);

  .popup-label {
    font-size: 28rpx;
    color: #94a3b8;
    margin-bottom: 16rpx;
  }

  .popup-code {
    font-size: 60rpx;
    font-weight: bold;
    color: #d4af37;
    letter-spacing: 8rpx;
    text-shadow: 0 0 20rpx rgba(212, 175, 55, 0.3);
  }
}

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

/* Utils */
.flex-1 { flex: 1; }
.mt-4 { margin-top: 32rpx; }

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

<script>
export default {
  methods: {
    // 如果有非 setup 的逻辑可以在这里定义
  }
}
</script>