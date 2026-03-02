<template>
  <view class="level-detail-container">
    <!-- 顶部背景 -->
    <view class="header-bg"></view>
    
    <!-- 背景足球场线条 -->
    <view class="pitch-lines">
      <view class="pitch-rect"></view>
      <view class="pitch-center-line"></view>
      <view class="pitch-center-circle"></view>
    </view>

    <!-- 导航栏 -->
    <view class="nav-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <text class="material-symbols-outlined nav-icon">chevron_left</text>
      </view>
      <text class="nav-title">等级详情</text>
      <view class="nav-right" @click="handleShare">
        <text class="material-symbols-outlined nav-icon">share</text>
      </view>
    </view>

    <!-- 分享海报遮罩 -->
    <view v-if="showPoster" class="poster-mask" @click="showPoster = false">
      <view class="poster-content" @click.stop>
        <view class="poster-card" id="poster-card">
          <view class="poster-header">
            <image class="poster-avatar" :src="getAvatarUrl(userInfo.avatar)" mode="aspectFill"></image>
            <view class="poster-user-info">
              <text class="poster-nickname">{{ userInfo.nickname || userInfo.username }}</text>
              <text class="poster-level">LV.{{ userInfo.level }} {{ userLevelTitle.split('|')[0] }}</text>
            </view>
          </view>
          <view class="poster-body">
            <view class="poster-stats">
              <view class="stat-item">
                <text class="stat-value">{{ userInfo.experience || 0 }}</text>
                <text class="stat-label">总经验值</text>
              </view>
              <view class="stat-item">
                <text class="stat-value">{{ userInfo.level }}</text>
                <text class="stat-label">当前等级</text>
              </view>
            </view>
            <view class="poster-quote">
              <text>“专注足球战术复盘，绿茵场上的每一步都有逻辑。”</text>
            </view>
          </view>
          <view class="poster-footer">
            <view class="qr-code">
              <text class="material-symbols-outlined qr-icon">qr_code_2</text>
            </view>
            <view class="app-info">
              <text class="app-name">足球社区</text>
              <text class="app-slogan">扫码查看我的足球成就</text>
            </view>
          </view>
        </view>
        <view class="poster-btns">
          <button class="poster-btn save-btn" @click="savePoster">保存图片</button>
          <button class="poster-btn close-btn" @click="showPoster = false">关闭</button>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main-content" :style="{ height: contentHeight + 'px' }">
      <view class="content-wrapper">
        <!-- 用户简要信息 -->
        <view class="user-summary">
          <view class="avatar-section">
            <view class="progress-ring-box">
              <svg class="progress-ring">
                <circle class="ring-bg" cx="56" cy="56" r="50"></circle>
                <circle class="ring-active" cx="56" cy="56" r="50" 
                  :style="{ strokeDashoffset: progressOffset, stroke: levelColor }"></circle>
              </svg>
              
              <!-- Lv.5 流光背景层 (必须在最底层) -->
              <view v-if="userInfo.level === 5" class="frame-glow-lv5-bg"></view>

              <view class="avatar-wrapper">
                <image class="avatar-img" :src="getAvatarUrl(userInfo.avatar)" mode="aspectFill"></image>
              </view>

              <!-- 头像框装饰层 -->
              <view class="avatar-frame" :class="'frame-lv' + userInfo.level">
                <!-- Lv.2 足球装饰 (左上+左下) -->
                <template v-if="userInfo.level === 2">
                  <text class="material-symbols-outlined frame-deco deco-tl">sports_soccer</text>
                  <text class="material-symbols-outlined frame-deco deco-bl">sports_soccer</text>
                </template>
                
                <!-- Lv.4 皇冠装饰 (正上方) -->
                <text v-if="userInfo.level === 4" class="material-symbols-outlined top-crown">workspace_premium</text>
                
                <!-- Lv.5 皇冠装饰 (正上方) -->
                <text v-if="userInfo.level === 5" class="material-symbols-outlined top-crown">workspace_premium</text>
              </view>
              <view class="level-badge" :class="'lv-badge-' + userInfo.level">
                <text class="material-symbols-outlined badge-icon">workspace_premium</text>
                <text class="badge-text">LV.{{ userInfo.level }}</text>
              </view>
            </view>
          </view>
          
          <view class="user-info-text">
            <view class="name-row">
              <text class="user-name">{{ userInfo.nickname || userInfo.username }}</text>
              <text class="material-symbols-outlined verified-icon">verified</text>
            </view>
            <view class="tag-row">
              <text class="level-tag">{{ userLevelTitle.split('|')[0] }}</text>
              <text class="sub-tag">{{ userLevelTitle.split('|')[1] }}</text>
            </view>
            <text class="user-bio">{{ userInfo.description || userInfo.bio || '专注足球战术复盘，提供专业的比赛分析。绿茵场上的每一步都有逻辑。' }}</text>
          </view>
        </view>

        <!-- 经验进度卡片 -->
        <view class="exp-card">
          <view class="exp-info">
            <view class="exp-left">
              <text class="exp-label">当前等级经验值</text>
              <view class="exp-values">
                <text class="current-exp">{{ userInfo.experience || 0 }}</text>
                <text class="total-exp">/ {{ nextLevelExp }} XP</text>
              </view>
            </view>
            <view class="exp-right">
              <text class="need-exp">还差 {{ Math.max(0, nextLevelExp - (userInfo.experience || 0)) }} XP 升级</text>
            </view>
          </view>
          <view class="progress-bar-bg">
            <view class="progress-bar-fill" :style="{ width: progressPercent + '%' }"></view>
          </view>
        </view>

        <!-- 等级权益 -->
        <view class="section-container">
          <view class="section-header">
            <text class="section-title">等级权益</text>
            <text class="view-all">查看全部</text>
          </view>
          <view class="privilege-grid">
            <view class="privilege-item">
              <view class="icon-box medal">
                <text class="material-symbols-outlined">military_tech</text>
              </view>
              <text class="item-name">专属勋章</text>
            </view>
            <view class="privilege-item">
              <view class="icon-box emoji">
                <text class="material-symbols-outlined">add_reaction</text>
              </view>
              <text class="item-name">评论表情</text>
            </view>
            <view class="privilege-item">
              <view class="icon-box top">
                <text class="material-symbols-outlined">vertical_align_top</text>
              </view>
              <text class="item-name">发帖置顶</text>
            </view>
          </view>
        </view>

        <!-- 经验规则 -->
        <view class="section-container">
          <text class="section-title mb-30">经验获取</text>
          <view class="rules-card">
            <view class="rule-item">
              <view class="rule-left">
                <view class="rule-icon post">
                  <text class="material-symbols-outlined">edit_note</text>
                </view>
                <text class="rule-name">发布动态/文章</text>
              </view>
              <text class="rule-value">+10 EXP</text>
            </view>
            <view class="rule-item">
              <view class="rule-left">
                <view class="rule-icon like">
                  <text class="material-symbols-outlined">favorite</text>
                </view>
                <text class="rule-name">获得点赞</text>
              </view>
              <text class="rule-value">+5 EXP</text>
            </view>
            <view class="rule-item">
              <view class="rule-left">
                <view class="rule-icon comment">
                  <text class="material-symbols-outlined">forum</text>
                </view>
                <text class="rule-name">发表评论</text>
              </view>
              <text class="rule-value">+2 EXP</text>
            </view>
          </view>
        </view>

        <!-- 经验记录 -->
        <view class="section-container">
          <text class="section-title mb-30">经验记录</text>
          <view class="record-list">
            <view v-if="expRecords.length === 0" class="empty-tip">暂无战术数据</view>
            <view class="record-item" v-for="(item, index) in expRecords" :key="index">
              <view class="record-left">
                <view class="record-icon" :class="item.type">
                  <text class="material-symbols-outlined">{{ item.icon }}</text>
                </view>
                <view class="record-text">
                  <text class="record-name">{{ item.name }}</text>
                  <text class="record-time">{{ item.time }}</text>
                </view>
              </view>
              <text class="record-value">+{{ item.value }}</text>
            </view>
          </view>

          <!-- 分页控制器 -->
          <view v-if="totalPages > 1" class="pagination-box">
            <view class="page-btn" :class="{ disabled: currentPage === 1 }" @tap="changePage(currentPage - 1)">
              <text class="material-symbols-outlined">chevron_left</text>
            </view>
            <text class="page-info">{{ currentPage }} / {{ totalPages }}</text>
            <view class="page-btn" :class="{ disabled: currentPage === totalPages }" @tap="changePage(currentPage + 1)">
              <text class="material-symbols-outlined">chevron_right</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { fileApi, userApi } from '@/api'

const statusBarHeight = ref(0)
const contentHeight = ref(0)
const userInfo = ref({})

// 定义一个统一的头像处理函数 (同步自 my.vue)
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

const levelTitles = {
  1: '新秀观察员 | 足球爱好者',
  2: '战术研究员 | 比赛分析者',
  3: '资深分析师 | 绿茵智囊',
  4: '战术大师 | 绿茵战略家',
  5: '战术宗师 | 足球哲学家'
}

const levelExpMap = {
  1: 100,
  2: 200,
  3: 300,
  4: 400,
  5: 99999
}

const expRecords = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 3

const fetchExpRecords = async (page) => {
  try {
    const res = await userApi.getExperienceRecordsPage({ page, size: pageSize })
    if (res && res.records) {
      expRecords.value = res.records.map(formatExpRecord)
      currentPage.value = res.current
      totalPages.value = res.pages
    }
  } catch (e) {
    console.error('Fetch Experience Records Failed:', e)
  }
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  fetchExpRecords(page)
}

const formatExpRecord = (record) => {
  const mapping = {
    'Post': { name: '发布足球动态/文章', icon: 'article', type: 'post' },
    'Like': { name: '内容获得点赞', icon: 'thumb_up', type: 'star' },
    'Comment': { name: '参与战术复盘评论', icon: 'chat_bubble', type: 'chat' }
  }
  
  const info = mapping[record.reason] || { name: record.reason, icon: 'stars', type: 'post' }
  
  // 格式化时间
  const date = new Date(record.createdAt)
  const timeStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  
  return {
    name: info.name,
    time: timeStr,
    value: record.amount,
    icon: info.icon,
    type: info.type
  }
}

const userLevelTitle = computed(() => {
  if (userInfo.value.levelTitle) return userInfo.value.levelTitle
  return levelTitles[userInfo.value.level] || levelTitles[1]
})

const nextLevelExp = computed(() => {
  return levelExpMap[userInfo.value.level] || 100
})

const progressPercent = computed(() => {
  const exp = userInfo.value.experience || 0
  const next = nextLevelExp.value
  return Math.min(100, (exp / next) * 100)
})

const progressOffset = computed(() => {
  const circumference = 2 * Math.PI * 50
  return circumference * (1 - progressPercent.value / 100)
})

const levelColor = computed(() => {
  const level = userInfo.value.level
  if (level === 1) return '#4B5563' // 哑光石墨
  if (level === 2) return '#B45309' // 古铜
  if (level === 3) return '#CBD5E1' // 银白
  if (level === 4) return '#FACC15' // 亮金
  if (level === 5) return '#f2b90d' // 黑金
  return '#4B5563'
})

const showPoster = ref(false)

const handleShare = () => {
  showPoster.value = true
}

const savePoster = () => {
  // 在 H5 环境下，我们无法直接保存到相册，通常是提示长按保存
  // 这里可以进一步使用 html2canvas 等库将 DOM 转为图片
  uni.showModal({
    title: '保存提示',
    content: '请长按分享图或截图进行保存分享',
    showCancel: false
  })
}

onMounted(async () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight
  contentHeight.value = systemInfo.windowHeight - statusBarHeight.value - 44 // 移除底部按钮高度占用
  
  // 先获取本地存储的用户信息作为兜底
  const storedUserInfo = uni.getStorageSync('userInfo')
  if (storedUserInfo) {
    userInfo.value = storedUserInfo
  }

  // 尝试从后端获取最新数据，确保经验值和头像实时同步
  try {
    const profileRes = await userApi.getProfile()
    if (profileRes) {
      userInfo.value = profileRes
      // 同步更新本地存储
      uni.setStorageSync('userInfo', profileRes)
      console.log('Level Detail Latest UserInfo:', userInfo.value)
    }

    // 获取经验流水
    await fetchExpRecords(1)
  } catch (e) {
    console.error('Level Detail Load Data Failed:', e)
  }
})

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.level-detail-container {
  min-height: 100vh;
  background-color: #0A0A0A;
  color: #FFFFFF;
  position: relative;
  overflow: hidden;
}

/* 海报遮罩 */
.poster-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  backdrop-filter: blur(5px);
}

.poster-content {
  width: 85%;
  max-width: 600rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.poster-card {
  width: 100%;
  background: #1A1A1A;
  border-radius: 32rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20rpx 50rpx rgba(0, 0, 0, 0.5);
}

.poster-header {
  padding: 40rpx;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #3B0764, #1A1A1A);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

  .poster-avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    border: 4rpx solid #FACC15;
    margin-right: 20rpx;
  }

  .poster-user-info {
    display: flex;
    flex-direction: column;

    .poster-nickname {
      font-size: 32rpx;
      font-weight: 600;
      color: #FFFFFF;
      margin-bottom: 4rpx;
    }

    .poster-level {
      font-size: 24rpx;
      color: #FACC15;
    }
  }
}

.poster-body {
  padding: 60rpx 40rpx;
  text-align: center;

  .poster-stats {
    display: flex;
    justify-content: space-around;
    margin-bottom: 60rpx;

    .stat-item {
      display: flex;
      flex-direction: column;

      .stat-value {
        font-size: 48rpx;
        font-weight: 700;
        color: #FACC15;
        margin-bottom: 8rpx;
      }

      .stat-label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.5);
      }
    }
  }

  .poster-quote {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
    font-style: italic;
    line-height: 1.6;
    padding: 0 20rpx;
    position: relative;

    &::before, &::after {
      content: '"';
      font-size: 40rpx;
      color: #FACC15;
      opacity: 0.5;
    }
  }
}

.poster-footer {
  padding: 40rpx;
  background: rgba(255, 255, 255, 0.03);
  display: flex;
  align-items: center;
  border-top: 1rpx solid rgba(255, 255, 255, 0.05);

  .qr-code {
    width: 100rpx;
    height: 100rpx;
    background: #FFFFFF;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;

    .qr-icon {
      font-size: 80rpx;
      color: #1A1A1A;
    }
  }

  .app-info {
    display: flex;
    flex-direction: column;

    .app-name {
      font-size: 28rpx;
      font-weight: 600;
      color: #FFFFFF;
      margin-bottom: 4rpx;
    }

    .app-slogan {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.4);
    }
  }
}

.poster-btns {
  width: 100%;
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;

  .poster-btn {
    flex: 1;
    height: 90rpx;
    border-radius: 45rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    font-weight: 600;
    border: none;

    &.save-btn {
      background: #FACC15;
      color: #000000;
    }

    &.close-btn {
      background: rgba(255, 255, 255, 0.1);
      color: #FFFFFF;
      border: 1rpx solid rgba(255, 255, 255, 0.2);
    }
  }
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 500rpx;
  background: linear-gradient(to bottom, rgba(88, 28, 135, 0.3), transparent);
  pointer-events: none;
}

.pitch-lines {
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 500rpx;
  opacity: 0.1;
  pointer-events: none;
  
  .pitch-rect {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: 1rpx solid #FFFFFF;
  }
  
  .pitch-center-line {
    position: absolute;
    top: 0;
    left: 50%;
    width: 1rpx;
    height: 100%;
    background-color: #FFFFFF;
  }
  
  .pitch-center-circle {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 150rpx;
    height: 150rpx;
    border: 1rpx solid #FFFFFF;
    border-radius: 50%;
    transform: translate(-50%, -50%);
  }
}

.nav-header {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  
  .nav-left, .nav-right {
    width: 80rpx;
    height: 80rpx;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .nav-icon {
    color: #FFFFFF;
    font-size: 40rpx;
  }
  
  .nav-title {
    font-size: 32rpx;
    font-weight: 600;
  }
}

.main-content {
  position: relative;
  z-index: 10;
}

.content-wrapper {
  padding: 40rpx 30rpx 100rpx;
}

.user-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-top: 20rpx;
}

.avatar-section {
  position: relative;
  margin-bottom: 40rpx;
  overflow: visible; /* 确保皇冠不被截断 */
}

.progress-ring-box {
  position: relative;
  width: 224rpx;
  height: 224rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible; /* 确保皇冠不被截断 */
}

.progress-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  z-index: 10; /* SVG 进度条 z-index: 10 */
  
  circle {
    fill: transparent;
    stroke-width: 2;
    stroke-linecap: round;
  }
  
  .ring-bg {
    stroke: rgba(255, 255, 255, 0.1);
  }
  
  .ring-active {
    stroke: #FACC15;
    transition: stroke-dashoffset 0.35s;
    stroke-dasharray: 314.159; /* 2 * PI * 50 */
  }
}

  .avatar-wrapper {
    position: absolute;
    width: 200rpx;
    height: 200rpx;
    border-radius: 50%;
    overflow: hidden;
    z-index: 20; /* 头像 z-index: 20 */
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
      background-color: #2d3748; /* 添加占位背景色 */
    }
  }

  /* 头像框通用样式 */
  .avatar-frame {
    position: absolute;
    inset: -10rpx; /* 略大于头像 */
    border-radius: 50%;
    z-index: 30; /* 头像框 z-index: 30 */
    pointer-events: none;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none; /* 清除默认 */

    /* Lv.1: 哑光石墨 */
    &.frame-lv1 {
      border: 2rpx solid rgba(156, 163, 175, 0.4) !important;
    }

    /* Lv.2: 青铜足球 (缝线感) */
    &.frame-lv2 {
      border: 4rpx dashed #B45309 !important;
      
      .frame-deco {
        position: absolute;
        font-size: 32rpx;
        color: #B45309;
        text-shadow: 0 0 6rpx rgba(0, 0, 0, 0.5);
        z-index: 40;
        
        &.deco-tl { top: 0; left: 0; transform: translate(-20%, -20%); }
        &.deco-bl { bottom: 40rpx; left: -10rpx; } /* 移高，避开 badge */
      }
    }

    /* Lv.3: 白金银色 (双层实线) */
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

  .level-badge {
    position: absolute;
    bottom: -4rpx;
    left: 50%;
    transform: translateX(-50%);
    background: linear-gradient(135deg, #FDE047 0%, #EAB308 100%);
    color: #000000;
    padding: 4rpx 16rpx;
    border-radius: 999rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.4);
    z-index: 40; /* 等级勋章 z-index: 40 */
    white-space: nowrap;
    border: 2rpx solid #1e293b;
  
  .badge-icon {
    font-size: 22rpx !important; /* 稍微缩小图标 */
    font-weight: bold;
  }
  
  .badge-text {
    font-size: 20rpx; /* 稍微缩小文字 */
    font-weight: 800;
    line-height: 1;
  }
  
  &.lv-badge-5 {
    background: linear-gradient(90deg, #f2b90d, #fff);
    box-shadow: 0 0 15rpx rgba(242, 185, 13, 0.6);
  }
}

.user-info-text {
  .name-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 20rpx;
    margin-top: 20rpx; /* 增加顶部间距，避免与头像太近 */
    
    .user-name {
      font-size: 56rpx;
      font-weight: 700;
    }
    
    .verified-icon {
      color: #EF4444;
      font-size: 48rpx;
    }
  }
  
  .tag-row {
    display: flex;
    gap: 16rpx;
    margin-bottom: 24rpx;
    
    .level-tag {
      background: rgba(250, 204, 21, 0.1);
      border: 1rpx solid rgba(250, 204, 21, 0.3);
      color: #FACC15;
      padding: 4rpx 20rpx;
      border-radius: 999rpx;
      font-size: 24rpx;
      font-weight: 500;
    }
    
    .sub-tag {
      background: rgba(255, 255, 255, 0.05);
      border: 1rpx solid rgba(255, 255, 255, 0.1);
      color: #9CA3AF;
      padding: 4rpx 20rpx;
      border-radius: 999rpx;
      font-size: 24rpx;
      font-weight: 500;
    }
  }
  
  .user-bio {
    color: #9CA3AF;
    font-size: 28rpx;
    line-height: 1.6;
    max-width: 600rpx;
  }
}

.exp-card {
  margin-top: 60rpx;
  background: #161616;
  border-radius: 32rpx;
  padding: 40rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  
  .exp-info {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 30rpx;
    
    .exp-label {
      font-size: 20rpx;
      color: #6B7280;
      text-transform: uppercase;
      letter-spacing: 2rpx;
      margin-bottom: 8rpx;
      display: block;
    }
    
    .exp-values {
      display: flex;
      align-items: baseline;
      gap: 8rpx;
      
      .current-exp {
        font-size: 48rpx;
        font-weight: 700;
      }
      
      .total-exp {
        font-size: 28rpx;
        color: #6B7280;
      }
    }
    
    .need-exp {
      font-size: 24rpx;
      color: #FACC15;
      font-weight: 500;
    }
  }
  
  .progress-bar-bg {
    height: 16rpx;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 999rpx;
    overflow: hidden;
    
    .progress-bar-fill {
      height: 100%;
      background: #FACC15;
      border-radius: 999rpx;
      transition: width 0.3s ease;
    }
  }
}

.section-container {
  margin-top: 80rpx;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
    
    .view-all {
      color: #FACC15;
      font-size: 28rpx;
      font-weight: 500;
    }
  }
  
  .section-title {
    font-size: 36rpx;
    font-weight: 700;
    
    &.mb-30 {
      margin-bottom: 40rpx;
      display: block;
    }
  }

  .privilege-grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    gap: 20rpx;
  }
  
  .privilege-item {
    width: 30%; /* 确保横向排列 */
    background: #161616;
    border-radius: 24rpx; /* 统一圆角 */
    padding: 30rpx 10rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.05);
    display: flex;
    flex-direction: column;
    align-items: center;
    box-sizing: border-box;
    min-height: 220rpx; /* 设置统一高度 */
    justify-content: center; /* 居中内容 */

    .icon-box {
      width: 96rpx;
      height: 96rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 24rpx;
      
      text {
        font-size: 48rpx;
      }
      
      &.medal {
        background: rgba(250, 204, 21, 0.1);
        text { color: #FACC15; }
      }
      
      &.emoji {
        background: rgba(59, 130, 246, 0.1);
        text { color: #3B82F6; }
      }
      
      &.top {
        background: rgba(249, 115, 22, 0.1);
        text { color: #F97316; }
      }
    }
    
    .item-name {
      font-size: 24rpx;
      color: #D1D5DB;
    }
  }
}

.record-list {
  display: flex;
  flex-direction: column;
}

.record-item {
  background: #161616;
  border-radius: 24rpx; /* 统一圆角 */
  padding: 30rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx; /* 添加间距 */
  box-sizing: border-box;

  .record-left {
    display: flex;
    align-items: center;
    gap: 24rpx;

    .record-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      text {
        font-size: 40rpx;
      }
      
      &.login {
        background: rgba(34, 197, 94, 0.1);
        text { color: #22C55E; }
      }
      
      &.star {
        background: rgba(168, 85, 247, 0.1);
        text { color: #A855F7; }
      }
      
      &.chat {
        background: rgba(59, 130, 246, 0.1);
        text { color: #3B82F6; }
      }
      
      &.post {
        background: rgba(250, 204, 21, 0.1);
        text { color: #FACC15; }
      }
    }
    
    .record-text {
      .record-name {
        font-size: 28rpx;
        font-weight: 500;
        color: #FFFFFF;
        display: block;
        margin-bottom: 4rpx;
      }
      
      .record-time {
        font-size: 24rpx;
        color: #6B7280;
      }
    }
  }
  
  .record-value {
    color: #FACC15;
    font-weight: 700;
    font-size: 32rpx;
  }
}

.pagination-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 30rpx;
  margin-top: 20rpx;
  padding: 20rpx 0;

  .page-btn {
    width: 64rpx;
    height: 64rpx;
    background: #161616;
    border: 1rpx solid rgba(255, 255, 255, 0.1);
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;

    &:active {
      opacity: 0.7;
      transform: scale(0.95);
    }

    &.disabled {
      opacity: 0.3;
      pointer-events: none;
    }

    text {
      font-size: 36rpx;
      color: #FACC15;
    }
  }

  .page-info {
    font-size: 26rpx;
    color: #9CA3AF;
    font-weight: 500;
    min-width: 80rpx;
    text-align: center;
  }
}

.empty-tip {
  padding: 60rpx 0;
  text-align: center;
  color: #6B7280;
  font-size: 28rpx;
}

.rules-card {
  background: #161616;
  border-radius: 24rpx;
  padding: 10rpx 30rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.05);

  .rule-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

    &:last-child {
      border-bottom: none;
    }

    .rule-left {
      display: flex;
      align-items: center;
      gap: 20rpx;

      .rule-icon {
        width: 64rpx;
        height: 64rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        text {
          font-size: 36rpx;
        }

        &.post {
          background: rgba(250, 204, 21, 0.1);
          text { color: #FACC15; }
        }

        &.like {
          background: rgba(239, 68, 68, 0.1);
          text { color: #EF4444; }
        }

        &.comment {
          background: rgba(59, 130, 246, 0.1);
          text { color: #3B82F6; }
        }
      }

      .rule-name {
        font-size: 28rpx;
        color: #FFFFFF;
      }
    }

    .rule-value {
      font-size: 26rpx;
      color: #9CA3AF;
      font-weight: 500;
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
    0%, 100% { 
      border-color: rgba(250, 204, 21, 0.8);
      box-shadow: 0 0 10rpx rgba(250, 204, 21, 0.4);
    }
    50% { 
      border-color: rgba(250, 204, 21, 1);
      box-shadow: 0 0 25rpx rgba(250, 204, 21, 0.8);
    }
  }
</style>
