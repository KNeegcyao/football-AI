<template>
  <view class="container">
    <!-- Header Navigation -->
    <view class="nav-bar glass-effect">
      <view class="nav-left" @click="goBack">
        <view class="icon-btn">
          <u-icon name="arrow-left" color="#fff" size="40rpx"></u-icon>
        </view>
      </view>
      <text class="nav-title" :class="{ 'show': showTitle }">{{ circleName }}</text>
      <view class="nav-right">
        <view class="icon-btn">
          <u-icon name="search" color="#fff" size="40rpx"></u-icon>
        </view>
        <view class="icon-btn">
          <u-icon name="more-dot-fill" color="#fff" size="40rpx"></u-icon>
        </view>
      </view>
    </view>

    <!-- Hero Section -->
    <view class="hero-section">
      <image class="hero-bg" :src="heroImage" mode="aspectFill"></image>
      <view class="hero-gradient"></view>
    </view>

    <!-- Circle Info -->
    <view class="circle-info">
      <view class="info-header">
        <view class="circle-logo-wrapper">
          <image class="circle-logo" :src="circleLogo" mode="aspectFit"></image>
        </view>
        <button class="join-btn" hover-class="btn-hover">
          <text>加入</text>
        </button>
      </view>
      <view class="info-content">
        <text class="circle-name">{{ circleName }}</text>
        <view class="circle-stats">
          <text class="stat-highlight">{{ memberCount }}</text> 成员 • 
          <text class="stat-highlight">{{ onlineCount }}</text> 在线
        </view>
        <text class="circle-desc">
          这里是美凌格的集结地！分享皇马最新动态、转会传闻及比赛讨论。Hala Madrid!
        </text>
      </view>
    </view>

    <!-- Tabs -->
    <view class="tabs-container glass-effect">
      <view class="tab-item active">
        <text class="tab-text">讨论</text>
        <view class="tab-indicator"></view>
      </view>
      <view class="tab-item">
        <text class="tab-text">赛程</text>
      </view>
      <view class="tab-item">
        <text class="tab-text">球员</text>
      </view>
      <view class="tab-item">
        <text class="tab-text">相册</text>
      </view>
    </view>

    <!-- Feed Section -->
    <view class="feed-section">
      <!-- AI Post Card -->
      <view class="post-card ai-card">
        <view class="card-header">
          <view class="user-info">
            <view class="avatar-box ai-avatar">
              <u-icon name="android-fill" color="#f2b90d" size="32rpx"></u-icon>
            </view>
            <view class="user-meta">
              <view class="name-row">
                <text class="username">PitchPulse AI</text>
                <view class="ai-badge">AI 分析</view>
              </view>
            </view>
          </view>
          <text class="time-ago">刚刚</text>
        </view>
        <text class="post-title">【战术分析】欧冠四分之一决赛首回合展望</text>
        <text class="post-content">
          基于维尼修斯最近的跑位数据，他在左路的突破效率提升了15%。今晚对阵曼城的关键在于能否利用罗德里戈的交叉换位拉开空间...
        </text>
        <view class="media-grid">
          <image class="media-item" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCEO7GwNM9dg0djapjxAuWJ1nFgCkIfgn1ufmunk1_nRl99l7EJRbCDg8VJBLGpfu3ZavpB755dqbeIOIGHL9TtiuioRiy2RoCmkE3ye5vX8wVkD-mAkaGxMdmc9Eqjxxdp_aVcGkwsXdI9EibI2xjUhLZ0TenrzdAa_l8Ho45auGaVTAQ0rYe3QY-Pt1dZb2wzbJd5k9oVtgDVj8NqTgXcvnhHwtdVY1zLIv5hwwvnCMwiyym6KpTVw_65I303HK2El2EXnJBnQTRi" mode="aspectFill"></image>
          <image class="media-item" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCcs8hChqz6NtBT9EYHf8647M6hV4uLY6o4kBnPm96pX40o20WDok-X2gQzILgbL2b6_-wxHTPGlgyDDNiyFR6lacvKhmuxDfmS5S6aAXobnrkCRpfNKhia6-gIvYSgVLk1EeqzHI6DzV2tHCM7NSynNcn20ss2VLEkN7nZ8wP3Qi8UZq9qUEfPCWQbrNlWfkVKhjM4mng3J1zZ8hDvDzHzoYtaHU2dasMsjswycEPgGHwYQPQZTCvNJ8P7XL5jOYaMvKYCvkL7KhW5" mode="aspectFill"></image>
        </view>
        <view class="card-footer">
          <view class="action-item">
            <u-icon name="heart" size="32rpx" color="#9CA3AF"></u-icon>
            <text>1.2k</text>
          </view>
          <view class="action-item">
            <u-icon name="chat" size="32rpx" color="#9CA3AF"></u-icon>
            <text>248</text>
          </view>
          <view class="action-item">
            <u-icon name="share" size="32rpx" color="#9CA3AF"></u-icon>
            <text>56</text>
          </view>
        </view>
      </view>

      <!-- Regular User Post Card -->
      <view class="post-card">
        <view class="card-header">
          <view class="user-info">
            <image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBrRbCMQ9jttw9cIS5KvBEH7v21rrGegUNbE_x3qMLLvCDd206ZrgoV8t35q1ze3OwpjV-cPZXyCIm1TqRCFeUOqs4mCXUC8F-oTUefRjWEwQdBJh86UE363VP2GDt0LgnG9bc3NJ1lyO-GADQ-onflDElLt02WLLahKrxTJxmrFSuuOf6LG7R6XHaLY60uxTjZFYPkVB-E_FheufCuakN_sq3efmkaIRAfs5J0NBc-sUlCQbnc9UeZwKWUiPMKa0rhKyOrgOk6HnXc" mode="aspectFill"></image>
            <text class="username">伯纳乌之王7</text>
          </view>
          <text class="time-ago">15分钟前</text>
        </view>
        <text class="post-content">
          今天在商店买到了本赛季的第三套球衣，实物比照片好看多了！大家觉得呢？黑色金边真的太高级了。
        </text>
        <image class="post-image" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDjDt9a7gdu8exaUUYLxHjBEO2BmO8SiYwMqMmpzFOw5WR7FmspEOb1PY7gOp73aj8HK3bePbFIfiZY7qNAhQZLrYGVOTnt_DN3EcskzbHMr8qT8QZuLFiTf1PQe86u6WOLnC7vdLvrKF4xQ1vz9l0rdPA_vRuGDlOo4GaHHV9074qLZeCpYWkDz-k0vWNDzi84bAnbHiKLrVDVTBM9hJi71NaAtRaeS6p6hDZZ5k6ViRzmNO6Gjotd_f1oj6wFhNUtArHUO94rfBMt" mode="aspectFill"></image>
        <view class="card-footer">
          <view class="action-item">
            <u-icon name="heart" size="32rpx" color="#9CA3AF"></u-icon>
            <text>856</text>
          </view>
          <view class="action-item">
            <u-icon name="chat" size="32rpx" color="#9CA3AF"></u-icon>
            <text>142</text>
          </view>
          <view class="action-item">
            <u-icon name="share" size="32rpx" color="#9CA3AF"></u-icon>
            <text>12</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Floating Action Button -->
    <view class="fab-btn">
      <u-icon name="plus" color="#12110a" size="48rpx" bold></u-icon>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const circleName = ref('皇家马德里圈')
const memberCount = ref('1.2M')
const onlineCount = ref('5,240')
const circleLogo = ref('https://lh3.googleusercontent.com/aida-public/AB6AXuCaoNlkv2lbC_VubJnaBL9_IipqSj4VkXuFBISB3-gEaDczfN_8LogYz8DjHAZy3bzsQeoONCiVfvdB3SZwQfLVwOWHrfuMeJXrw02gpPivr-MnUCmaVCtxcLR-jSVfw_UgKHAWgwM4J_XsgtEcb3hZIjoST-NAUvLf74uEaHSVW5aGW6FoLppd4PpU0CWEmP6d9cwrjmqhI1Fktcrp8o-XHQNvtFWLTV5EXW_qBUQOpEqTizbxiVzWRTGMvu-ONfzF91Suxg9Ouur-')
const heroImage = ref('https://lh3.googleusercontent.com/aida-public/AB6AXuA8zpB1RsXacFHlnYl0ZHCNNrtr-7jQEcqiIxTf8sy9UGQq683lkXLnLA8tzeDNaXMTiTQRpUxzrOG_kLzBH55SuVH2El0CzU-ZWfIr8TwzC7uOd5Phd2zDX0v2_n-Tw2m0uCXPBxn83P_tfVqZuYrJ7jYAPEJ5rXP3cnwuo75sgeOk6BKrCbjdYKSdE_OC27eT5vx8_hNb8OqyZO7P8JlRgoGIr-9YVRifNfUMtHJID7b5o824ClfOK99y6TGEY6_AGcV0eVsErgXm')
const showTitle = ref(false)

onLoad((options) => {
  if (options.name) {
    circleName.value = decodeURIComponent(options.name)
  }
  if (options.image) {
    circleLogo.value = decodeURIComponent(options.image)
  }
  if (options.members) {
    memberCount.value = decodeURIComponent(options.members)
  }
})

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
$primary: #f2b90d;
$bg-dark: #12110a;
$surface-dark: #1c1a11;
$border-dark: #2d2a1d;
$text-gray: #9CA3AF;

.container {
  min-height: 100vh;
  background-color: #f8f8f5;
  /* #ifdef H5 */
  background-color: $bg-dark;
  /* #endif */
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .container {
    background-color: $bg-dark;
  }
}
/* Force dark mode style for this specific design */
.container {
  background-color: $bg-dark;
  color: #fff;
}

.glass-effect {
  background: rgba(28, 26, 17, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1rpx solid rgba($border-dark, 0.5);
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  /* Adapt to safe area */
  padding-top: calc(20rpx + env(safe-area-inset-top));
  height: calc(88rpx + env(safe-area-inset-top));
  box-sizing: border-box;
  
  /* #ifdef H5 */
  max-width: 500px;
  margin: 0 auto;
  /* #endif */
}

.nav-left, .nav-right {
  display: flex;
  gap: 16rpx;
}

.icon-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  opacity: 0;
  transition: opacity 0.3s;
  
  &.show {
    opacity: 1;
  }
}

.hero-section {
  position: relative;
  width: 100%;
  height: 500rpx;
  overflow: hidden;
}

.hero-bg {
  width: 100%;
  height: 100%;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, $bg-dark, rgba($bg-dark, 0.2), transparent);
}

.circle-info {
  position: relative;
  margin-top: -96rpx;
  padding: 0 32rpx 32rpx;
}

.info-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32rpx;
}

.circle-logo-wrapper {
  width: 192rpx;
  height: 192rpx;
  border-radius: 24rpx;
  background-color: $surface-dark;
  border: 8rpx solid $bg-dark;
  padding: 16rpx;
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.3);
}

.circle-logo {
  width: 100%;
  height: 100%;
}

.join-btn {
  margin: 0 0 8rpx 0;
  height: 88rpx;
  padding: 0 64rpx;
  background-color: $primary;
  color: $bg-dark;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba($primary, 0.2);
  
  &.btn-hover {
    transform: scale(0.98);
  }
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.circle-name {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}

.circle-stats {
  font-size: 28rpx;
  color: $text-gray;
}

.stat-highlight {
  color: #fff;
  font-weight: 500;
}

.circle-desc {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #d1d5db;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.tabs-container {
  position: sticky;
  top: calc(88rpx + env(safe-area-inset-top));
  z-index: 40;
  display: flex;
  padding: 0 32rpx;
  margin-bottom: 16rpx;
}

.tab-item {
  flex: 1;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  
  &.active {
    .tab-text {
      color: $primary;
      font-weight: bold;
    }
    
    .tab-indicator {
      position: absolute;
      bottom: 0;
      width: 64rpx;
      height: 4rpx;
      background-color: $primary;
      border-radius: 4rpx;
    }
  }
}

.tab-text {
  font-size: 28rpx;
  color: $text-gray;
}

.feed-section {
  padding: 32rpx;
  padding-bottom: 200rpx; /* Space for FAB and bottom safe area */
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.post-card {
  background-color: $surface-dark;
  border-radius: 24rpx;
  padding: 32rpx;
  border: 1rpx solid $border-dark;
  
  &.ai-card {
    border-color: rgba($primary, 0.2);
    box-shadow: 0 0 16rpx rgba($primary, 0.1);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.avatar-box, .avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
}

.ai-avatar {
  background-color: rgba($primary, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.username {
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
}

.ai-badge {
  font-size: 20rpx;
  background-color: rgba($primary, 0.2);
  color: $primary;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  border: 1rpx solid rgba($primary, 0.3);
}

.time-ago {
  font-size: 24rpx;
  color: $text-gray;
}

.post-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 16rpx;
  display: block;
}

.post-content {
  font-size: 28rpx;
  color: #d1d5db;
  line-height: 1.6;
  margin-bottom: 24rpx;
  display: block;
}

.media-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.media-item {
  width: 100%;
  height: 200rpx;
  border-radius: 16rpx;
  background-color: #333;
}

.post-image {
  width: 100%;
  height: 384rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
}

.card-footer {
  display: flex;
  gap: 48rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  text {
    font-size: 24rpx;
    color: $text-gray;
  }
}

.fab-btn {
  position: fixed;
  bottom: 96rpx;
  right: 32rpx;
  width: 112rpx;
  height: 112rpx;
  background-color: $primary;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba($primary, 0.3);
  z-index: 50;
  
  /* #ifdef H5 */
  /* Ensure it stays within max-width container visually if needed, but fixed usually relates to viewport */
  /* For H5 demo, we might want to constrain it, but fixed right: 32rpx works fine usually */
  /* #endif */
}
</style>