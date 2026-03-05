<script>
import { useChatStore } from '@/store/chat';

export default {
  onLaunch: function () {
    console.log('App Launch')

    const chatStore = useChatStore();
    // 如果已登录，建立 WebSocket 连接
    const token = uni.getStorageSync('token');
    if (token) {
      chatStore.connect();
    }
    
    // #ifdef MP-WEIXIN
    // 仅在微信小程序中使用 uni.loadFontFace 加载远程字体
    // 注意：微信小程序必须使用 HTTPS 链接，且域名需在小程序后台配置
    uni.loadFontFace({
      family: 'MaterialIcons',
      global: true,
      source: 'url("https://fonts.gstatic.com/s/materialicons/v140/flUhRq6tzZclQEJ-Vdg-IuiaDsNcIhQ8tQ.woff2")',
      success: () => console.log('Material Icons font loaded successfully'),
      fail: (err) => {
        console.warn('Failed to load remote Material Icons font, fallback to local', err)
      }
    })
    
    uni.loadFontFace({
      family: 'MaterialIconsRound',
      global: true,
      source: 'url("https://fonts.gstatic.com/s/materialiconsround/v108/L0kndfmuz1N97JlQD1E1n5uyyjWCcQebAnpG9663.woff2")',
      success: () => console.log('Material Icons Round loaded successfully'),
      fail: (err) => {
        console.warn('Failed to load remote Material Icons Round font, fallback to local', err)
      }
    })
    // #endif

    // #ifdef H5
    // 仅在启动时尝试隐藏原生 TabBar
    uni.hideTabBar({
      animation: false,
      fail: () => {
        // 如果当前不是 Tab 页面，延迟重试一次
        setTimeout(() => {
          uni.hideTabBar({ animation: false, fail: () => {} })
        }, 100)
      }
    })
    // #endif
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import "uview-plus/index.scss";

/* #ifdef H5 || APP-PLUS */
@font-face {
  font-family: 'MaterialIcons';
  src: url('~@/static/MaterialIcons-Regular.ttf') format('truetype');
}
@font-face {
  font-family: 'MaterialIconsRound';
  src: url('~@/static/MaterialIconsRound-Regular.otf') format('opentype');
}
/* #endif */

.material-icons {
  font-family: 'MaterialIcons' !important;
  font-weight: normal;
  font-style: normal;
  display: inline-block;
  line-height: 1;
  text-transform: none;
  letter-spacing: normal;
  word-wrap: normal;
  white-space: nowrap;
  direction: ltr;
}

.material-icons-round {
  font-family: 'MaterialIconsRound' !important;
  font-weight: normal;
  font-style: normal;
  display: inline-block;
  line-height: 1;
  text-transform: none;
  letter-spacing: normal;
  word-wrap: normal;
  white-space: nowrap;
  direction: ltr;
}

view, text, image, scroll-view, swiper, swiper-item {
  box-sizing: border-box;
}

/* #ifdef H5 */
body {
  background-color: #000;
  margin: 0;
  padding: 0;
}

/* 修复 H5 下弹窗全屏问题，使其与 500px 主内容区域一致 */
.u-popup {
  .u-popup__content {
    max-width: 500px !important;
    left: 50% !important;
    transform: translateX(-50%) !important;
    width: 100% !important;
    margin: 0 auto;
    background-color: transparent !important; /* 让内部 view 控制背景 */
  }
  .u-transition {
    /* 遮罩层不应该被限制宽度和偏移 */
    &.u-overlay {
      max-width: none !important;
      left: 0 !important;
      transform: none !important;
      width: 100% !important;
    }
    /* 内容区域的 transition 容器 */
    &:not(.u-overlay) {
      max-width: 500px !important;
      left: 50% !important;
      transform: translateX(-50%) !important;
      width: 100% !important;
    }
  }
}

.u-action-sheet {
  .u-popup__content {
    max-width: 500px !important;
    left: 50% !important;
    transform: translateX(-50%) !important;
    width: 100% !important;
    margin: 0 auto;
  }
  /* 确保选项内容也居中 */
  .u-action-sheet__item-wrap {
    width: 100% !important;
  }
}

uni-page-body {
  max-width: 500px !important;
  margin: 0 auto !important;
  min-height: 100vh;
  height: 100vh;
  display: block;
  position: relative;
  overflow-y: auto;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  background-color: #1a1811;
}
/* #endif */
.uni-tabbar {
  display: none !important;
}

/* 全局去除所有输入框的蓝色聚焦边框 */
input, textarea, select, uni-input, uni-textarea {
  outline: none !important;
  -webkit-tap-highlight-color: transparent;
}

.uni-input-input, .uni-textarea-textarea {
  outline: none !important;
  border: none !important;
  box-shadow: none !important;
}

.h5-header-fix {
  max-width: 500px !important;
  left: 50% !important;
  transform: translateX(-50%) !important;
  width: 100% !important;
}

/* 全局优化 uni.showToast 样式 */
.uni-sample-toast, .uni-toast {
  z-index: 9999 !important;
  background-color: rgba(0, 0, 0, 0.85) !important;
  backdrop-filter: blur(8px) !important;
  border: 1px solid rgba(212, 175, 55, 0.2) !important;
  border-radius: 16px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5) !important;
  padding: 24px !important;
  width: auto !important;
  min-width: 120px !important;
  max-width: 80% !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
}

.uni-toast__icon {
  margin: 0 0 12px 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 48px !important;
  height: 48px !important;
  line-height: 1 !important;
}

.uni-icon-success-no-circle {
  font-size: 40px !important;
  color: #d4af37 !important; /* 金色对号更符合 PitchPulse 主题 */
  display: block !important;
  margin: 0 !important;
}

.uni-icon-error, .uni-icon-warn {
  font-size: 40px !important;
  color: #f20d33 !important;
  display: block !important;
  margin: 0 !important;
}

.uni-toast__content {
  font-size: 15px !important;
  font-weight: 500 !important;
  color: #ffffff !important;
  margin: 0 !important;
  line-height: 1.4 !important;
  text-align: center !important;
  letter-spacing: 0.5px !important;
}

/* 优化 uni.showLoading 样式 */
.uni-loading {
  width: 32px !important;
  height: 32px !important;
  margin-bottom: 12px !important;
  border-color: #f20d33 !important;
  border-top-color: transparent !important;
}

/* 优化 uni.showModal 样式 (H5) */
.uni-modal {
  background-color: #1a1811 !important;
  border: 1px solid rgba(212, 175, 55, 0.2) !important;
  border-radius: 20px !important;
  overflow: hidden !important;
}

.uni-modal__title {
  color: #d4af37 !important;
  font-weight: bold !important;
  padding: 20px 20px 10px !important;
}

.uni-modal__bd {
  color: rgba(255, 255, 255, 0.8) !important;
  padding: 10px 20px 25px !important;
}

.uni-modal__footer {
  border-top: 1px solid rgba(255, 255, 255, 0.05) !important;
}

.uni-modal__btn {
  font-weight: 600 !important;
}

.uni-modal__btn_default {
  color: rgba(255, 255, 255, 0.5) !important;
}

.uni-modal__btn_primary {
  color: #f20d33 !important;
}
/* #endif */
</style>
