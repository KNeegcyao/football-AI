<template>
  <view class="container">
    <view class="status-bar"></view>
    <view class="header">
      <view class="back-btn" @click="goBack">
        <text class="material-icons" style="color: #f2b90d; font-size: 24px;">arrow_back</text>
      </view>
      <text class="title">热门圈子</text>
      <view class="placeholder"></view>
    </view>
    
    <scroll-view scroll-y class="list-scroll" @scrolltolower="loadMore">
      <view class="circle-list">
        <view class="circle-item" v-for="(circle, index) in circles" :key="index" @click="navigateToCircle(circle)">
          <image class="circle-avatar" :src="circle.image" mode="aspectFill"></image>
          <view class="circle-info">
            <text class="circle-name">{{ circle.name }}</text>
            <text class="circle-members">{{ circle.members }} 成员</text>
          </view>
          <view class="action-area">
            <text class="material-icons" style="color: rgba(242, 185, 13, 0.5);">chevron_right</text>
          </view>
        </view>
      </view>
      <view class="loading-status">
        <text v-if="loading" class="status-text">加载中...</text>
        <text v-else-if="noMore" class="status-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import request from '@/utils/request';

const circles = ref([]);
const page = ref(1);
const size = ref(20);
const loading = ref(false);
const noMore = ref(false);
const mode = ref('normal'); // normal | select

const loadCircles = async () => {
  if (loading.value || noMore.value) return;
  loading.value = true;
  
  try {
    const res = await request.get('/api/community/circles', { 
      page: page.value, 
      size: size.value,
      isHot: true 
    });
    if (res && res.records) {
      if (page.value === 1) {
        circles.value = res.records;
      } else {
        circles.value = [...circles.value, ...res.records];
      }
      
      if (res.current >= res.pages) {
        noMore.value = true;
      } else {
        page.value++;
      }
    }
  } catch (e) {
    console.error(e);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  uni.navigateBack();
};

const navigateToCircle = (circle) => {
  if (mode.value === 'select') {
    uni.$emit('selectCircle', circle);
    uni.navigateBack();
    return;
  }
  uni.navigateTo({
    url: `/pages/community/circle-detail?id=${circle.id}&name=${encodeURIComponent(circle.name)}&members=${encodeURIComponent(circle.members)}&image=${encodeURIComponent(circle.image)}`
  });
};

const loadMore = () => {
  loadCircles();
};

onLoad((options) => {
  if (options.mode) {
    mode.value = options.mode;
  }
  loadCircles();
});
</script>

<style>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #1A1811;
  color: #fff;
  /* #ifdef H5 */
  max-width: 500px;
  margin: 0 auto;
  /* #endif */
}

.status-bar {
  height: var(--status-bar-height);
  background-color: #1A1811;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background-color: #1A1811;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #f2b90d;
}

.back-btn, .placeholder {
  width: 40px;
  display: flex;
  align-items: center;
}

.list-scroll {
  flex: 1;
  height: 0;
}

.circle-list {
  padding: 16px;
}

.circle-item {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

.circle-avatar {
  width: 50px;
  height: 50px;
  border-radius: 25px;
  margin-right: 12px;
  border: 1px solid rgba(242, 185, 13, 0.3);
}

.circle-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.circle-name {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 4px;
}

.circle-members {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.loading-status {
  padding: 20px;
  text-align: center;
}

.status-text {
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
}
</style>
