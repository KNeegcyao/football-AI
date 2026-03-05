<template>
  <view class="container">
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-inner">
        <view class="left" @click="goBack">
          <u-icon name="arrow-left" color="#fff" size="22"></u-icon>
        </view>
        <view class="center">
          <text class="title">收到喜欢</text>
        </view>
        <view class="right"></view>
      </view>
    </view>

    <view class="list-content">
      <view class="item" v-for="(item, index) in list" :key="index" @click="handleClick(item)">
        <view class="avatar-box">
          <image :src="item.fromUser.avatar || '/static/default-avatar.png'" class="avatar-img" mode="aspectFill"></image>
          <view class="badge-icon">
            <u-icon name="thumb-up-fill" size="10" color="#000"></u-icon>
          </view>
        </view>
        <view class="main-box">
          <view class="header">
            <text class="nickname">{{ item.fromUser.nickname || '用户' }}</text>
            <text class="time">{{ formatTime(item.createdAt) }}</text>
          </view>
          <view class="action">
            <text class="action-text">{{ getActionDesc(item) }}</text>
            <text v-if="item.postTitle" class="post-title">"{{ item.postTitle }}"</text>
          </view>
        </view>
        <view class="unread-dot" v-if="!item.isRead"></view>
      </view>
      <u-loadmore :status="loadStatus" @loadmore="loadData(false)" marginTop="30" color="#64748b" />
    </view>
  </view>
</template>

<script>
import { notificationApi } from '@/api';

export default {
  data() {
    return {
      list: [],
      page: 1,
      size: 10,
      loadStatus: 'loadmore',
      isLoading: false,
      statusBarHeight: 0
    };
  },
  onLoad() {
    this.statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 0;
    this.loadData(true);
  },
  methods: {
    async loadData(refresh = false) {
      if (this.isLoading) return;
      if (refresh) {
        this.page = 1;
        this.list = [];
        this.loadStatus = 'loading';
      }
      this.isLoading = true;
      try {
        const res = await notificationApi.getList({
          page: this.page,
          size: this.size,
          types: '1,2' // 帖子点赞、评论点赞
        });
        const records = (res.records || []).map(item => {
          if (item.fromUser && item.fromUser.avatar) {
            item.fromUser.avatar = this.$utils.getFullImageUrl(item.fromUser.avatar);
          }
          return item;
        });
        if (refresh) {
          this.list = records;
        } else {
          this.list = [...this.list, ...records];
        }
        if (records.length < this.size) {
          this.loadStatus = 'nomore';
        } else {
          this.loadStatus = 'loadmore';
          this.page++;
        }
      } catch (e) {
        console.error(e);
      } finally {
        this.isLoading = false;
        if (refresh) uni.stopPullDownRefresh();
      }
    },
    getActionDesc(item) {
      return item.type === 1 ? '赞了你的帖子' : '赞了你的评论';
    },
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time.replace('T', ' '));
      const now = new Date();
      const diff = now - date;
      const minute = 60 * 1000, hour = 60 * minute, day = 24 * hour;
      if (diff < minute) return '刚刚';
      if (diff < hour) return Math.floor(diff / minute) + '分钟前';
      if (diff < day) return Math.floor(diff / hour) + '小时前';
      return time.substring(0, 10);
    },
    handleClick(item) {
      if (!item.isRead) {
        notificationApi.read(item.id).then(() => { item.isRead = 1; });
      }
      if (item.postId) {
        uni.navigateTo({ url: `/pages/post/detail?id=${item.postId}` });
      }
    },
    goBack() { uni.navigateBack(); }
  }
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #000;
  color: #fff;
}
.custom-navbar {
  position: sticky; top: 0; z-index: 100;
  background-color: #000;
  .navbar-inner {
    height: 44px; display: flex; align-items: center; padding: 0 30rpx;
    .left, .right { width: 100rpx; }
    .center { flex: 1; text-align: center; .title { font-size: 32rpx; font-weight: bold; } }
  }
}
.list-content {
  padding: 20rpx 30rpx;
  .item {
    display: flex; align-items: center; padding: 30rpx 0; border-bottom: 1rpx solid rgba(255,255,255,0.05); position: relative;
    .avatar-box {
      margin-right: 20rpx; position: relative;
      .avatar-img { width: 80rpx; height: 80rpx; border-radius: 40rpx; }
      .badge-icon {
        position: absolute; bottom: 0; right: 0; width: 28rpx; height: 28rpx; 
        background: $pitch-pulse-primary; border-radius: 14rpx; display: flex; align-items: center; justify-content: center;
        border: 4rpx solid #000;
      }
    }
    .main-box {
      flex: 1;
      .header {
        display: flex; justify-content: space-between; margin-bottom: 6rpx;
        .nickname { font-size: 28rpx; font-weight: bold; }
        .time { font-size: 24rpx; color: rgba(255,255,255,0.4); }
      }
      .action {
        font-size: 26rpx; color: rgba(255,255,255,0.7);
        .post-title { color: $pitch-pulse-primary; margin-left: 10rpx; }
      }
    }
    .unread-dot {
      width: 12rpx; height: 12rpx; background: $pitch-pulse-primary; border-radius: 6rpx; margin-left: 20rpx;
    }
  }
}
</style>
