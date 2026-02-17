<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
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

view, text, image, scroll-view, swiper, swiper-item {
  box-sizing: border-box;
}

/* #ifdef H5 */
body {
  background-color: #000;
  margin: 0;
  padding: 0;
}
uni-page-body {
  max-width: 440px !important;
  margin: 0 auto !important;
  min-height: 100vh;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  background-color: #1a1811;
}
/* 强制隐藏原生 TabBar 容器 */
.uni-tabbar {
  display: none !important;
}
/* #endif */
</style>
