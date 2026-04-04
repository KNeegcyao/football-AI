<template>
  <view class="tab-bar-container">
    <view class="tab-bar bg-tab-bar border-theme-main">
      <view v-for="(tab, index) in tabs" :key="index" 
            :class="['tab-item', tab.isCenter ? 'center-item' : '', currentTab === index ? 'active' : '']"
            @tap="handleTabClick(index)">
        <view v-if="tab.isCenter" class="center-icon-container">
          <view class="center-icon pulse-glow">
            <text class="material-icons animate-pulse">{{ tab.icon }}</text>
          </view>
          <text class="tab-text center-text">{{ tab.text }}</text>
        </view>
        <template v-else>
          <view class="icon-wrapper" style="position: relative; display: flex; align-items: center; justify-content: center;">
            <text class="material-icons" :style="{ fontSize: '52rpx' }">{{ tab.icon }}</text>
            <view class="badge" v-if="tab.path === 'pages/community/community' && totalUnread > 0">{{ totalUnread > 99 ? '99+' : totalUnread }}</view>
          </view>
          <text class="tab-text">{{ tab.text }}</text>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getFullImageUrl } from '@/utils/request'
import { useThemeStore } from '@/store/theme'
import { useChatStore } from '@/store/chat'
import request from '@/utils/request'

const themeStore = useThemeStore()
const chatStore = useChatStore()

const systemUnreadCount = ref(0)
const totalUnread = computed(() => systemUnreadCount.value + chatStore.totalUnreadCount)

const fetchSystemUnreadCount = async () => {
  try {
    const res = await request.get('/api/notifications/unread-count')
    systemUnreadCount.value = res || 0
  } catch (e) {
    systemUnreadCount.value = 0
  }
}

onMounted(() => {
  fetchSystemUnreadCount()
  uni.$on('refreshSystemUnread', fetchSystemUnreadCount)
})

onUnmounted(() => {
  uni.$off('refreshSystemUnread', fetchSystemUnreadCount)
})

const props = defineProps({
  currentTab: {
    type: Number,
    default: 0
  }
})

const tabs = ref([
  { text: '首页', icon: 'home', path: 'pages/index/index' },         // stadium 改回 home
  { text: '赛程', icon: 'event', path: 'pages/schedule/schedule' }, // calendar_month 改回 event
  { text: 'AI助手', icon: 'psychology', path: 'pages/ai/ai', isCenter: true },
  { text: '社区', icon: 'forum', path: 'pages/community/community' },
  { text: '我的', icon: 'person', path: 'pages/my/my' }
])

const handleTabClick = (index) => {
  if (index === props.currentTab) return
  uni.switchTab({
    url: '/' + tabs.value[index].path
  })
}
</script>

<style lang="scss" scoped>
/* 底部导航栏 */
.tab-bar-container {
  position: fixed;
  bottom: 0;
  width: 100%;
  z-index: 9999;
  pointer-events: none; /* 让点击穿透到内容区，但其子元素通过 auto 重新获得点击 */
  background: transparent;
  
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
  /* #ifndef H5 */
  left: 0;
  right: 0;
  /* #endif */
}

.material-icons {
  font-family: 'MaterialIcons' !important; /* 确保这里跟你引入的字体名一致 */
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  display: inline-block;
  line-height: 1;
  text-transform: none;
  letter-spacing: normal;
  word-wrap: normal;
  white-space: nowrap;
  direction: ltr;
}

.tab-bar {
  pointer-events: auto; /* 重新获得点击 */
  width: 100%;
  height: 110rpx; /* 稍微减小高度，增加精致感 */
  background-color: var(--tab-bar-bg); /* 使用主题背景色 */
  border-top: 1rpx solid var(--border-main); /* 使用主题边框色 */
  display: flex; 
  flex-direction: row;
  align-items: center; 
  padding-bottom: env(safe-area-inset-bottom); 
  box-sizing: content-box; 
  transition: all 0.3s ease;
  box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.15); /* 添加顶部阴影增加立体感 */
}

.tab-item {
  flex: 1;
  min-width: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  
  &.center-item {
    overflow: visible;
  }
  
  .center-icon-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  
  .center-icon {
    position: absolute;
    top: -50rpx; /* 稍微再往上提一点，增加悬浮感 */
    left: 50%;
    transform: translateX(-50%);
    width: 106rpx;
    height: 106rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    /* 金色渐变：从深金到亮金 */
    background: linear-gradient(135deg, #f2b90d 0%, #f9d406 100%);
    border: 8rpx solid var(--tab-bar-bg); /* 使用主题背景色作为切口边框 */
    z-index: 10001;
    box-shadow: 0 12rpx 24rpx rgba(242, 185, 13, 0.3); /* 金色发光阴影 */
    box-sizing: border-box;
    
    &.pulse-glow {
      animation: pulse-gold 2s infinite; /* 换成金色的呼吸灯 */
    }

    .material-icons {
      color: #000 !important; /* 金色背景配黑色图标，对比度最高 */
      font-size: 60rpx !important;
      font-weight: bold;
    }
  }
  
  .tab-text {
    font-size: 20rpx;
    color: var(--text-secondary); /* 使用主题次要文字颜色 */
    opacity: 0.6; /* 未激活时稍微透明一点 */
    font-weight: 500;
    margin-top: 6rpx;
    
    &.center-text {
      margin-top: 55rpx; /* 适配新的 top 值 */
    }
  }
  
  &.active {
    &:not(.center-item) .material-icons {
      color: var(--accent-color) !important; /* 使用主题强调色 */
      text-shadow: 0 0 10rpx rgba(249, 212, 6, 0.3); /* 选中的图标微光 */
    }
    .tab-text {
      color: var(--accent-color);
      opacity: 1;
      font-weight: bold;
    }
  }
}

/* 4. 新的金色呼吸动画 */
@keyframes pulse-gold {
  0% {
    box-shadow: 0 0 0 0 rgba(242, 185, 13, 0.5);
    transform: translateX(-50%) scale(1);
  }
  70% {
    box-shadow: 0 0 0 15rpx rgba(242, 185, 13, 0);
    transform: translateX(-50%) scale(1.05);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(242, 185, 13, 0);
    transform: translateX(-50%) scale(1);
  }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.badge {
  position: absolute;
  top: -6rpx;
  right: -16rpx;
  background-color: #ff4d4f;
  color: white;
  font-size: 20rpx;
  padding: 0 8rpx;
  height: 30rpx;
  line-height: 30rpx;
  border-radius: 15rpx;
  min-width: 30rpx;
  text-align: center;
  box-sizing: border-box;
  font-weight: bold;
  border: 2rpx solid var(--tab-bar-bg);
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: .7; transform: scale(0.95); }
}
</style>
