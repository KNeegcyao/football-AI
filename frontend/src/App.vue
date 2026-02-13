<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
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
/* 注意要写在第一行，同时给style标签加入lang="scss"属性 */
@import "uview-plus/index.scss";

view, text, image, scroll-view, swiper, swiper-item {
  box-sizing: border-box;
}

/* #ifdef H5 */
body {
  background-color: #000;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}
uni-page-body {
  max-width: 500px !important;
  margin: 0 auto !important;
  min-height: 100vh;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
  background-color: #1a1811;
  overflow-x: hidden;
}
uni-app {
  display: block;
}
/* #endif */
</style>
