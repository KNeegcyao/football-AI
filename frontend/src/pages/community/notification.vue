<template>
  <view class="container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-inner">
        <view class="left" @click="goBack">
          <u-icon name="arrow-left" color="#fff" size="22"></u-icon>
        </view>
        <view class="center">
          <text class="brand-text">PITCHPULSE</text>
        </view>
        <view class="right" :style="{ paddingRight: navbarPaddingRight + 'px' }">
          <view class="icon-btn" @click="markAllRead" title="全部已读">
            <u-icon name="order" color="#fff" size="20"></u-icon>
          </view>
          <view class="icon-btn" title="设置">
            <u-icon name="setting" color="#fff" size="20"></u-icon>
          </view>
        </view>
      </view>
    </view>

    <!-- 分类功能区 -->
    <section class="category-section">
      <view class="category-item" @click="filterByType('reply')">
        <view class="icon-wrapper bg-emerald">
          <u-icon name="chat" color="#10b981" size="28"></u-icon>
        </view>
        <text class="label">回复与@</text>
      </view>
      <view class="category-item" @click="filterByType('like')">
        <view class="icon-wrapper bg-rose">
          <u-icon name="thumb-up" color="#f43f5e" size="28"></u-icon>
        </view>
        <text class="label">收到喜欢</text>
      </view>
      <view class="category-item" @click="filterByType('follow')">
        <view class="icon-wrapper bg-sky">
          <u-icon name="account" color="#0ea5e9" size="28"></u-icon>
        </view>
        <text class="label">新增粉丝</text>
      </view>
      <view class="category-item" @click="goToMessageList">
        <view class="icon-wrapper bg-orange">
          <u-icon name="email-fill" color="#f97316" size="28"></u-icon>
        </view>
        <text class="label">私信消息</text>
      </view>
    </section>

    <view class="notification-list">
      <view class="notification-item" v-for="(item, index) in list" :key="index" @click="handleNotificationClick(item)">
        <!-- 头像区域 -->
        <view class="avatar-box">
          <image :src="item.fromUser.avatar || '/static/default-avatar.png'" class="avatar-img" mode="aspectFill"></image>
          <view class="badge-icon" v-if="getTypeIcon(item.type)">
            <u-icon :name="getTypeIcon(item.type)" size="10" color="#fff"></u-icon>
          </view>
        </view>

        <!-- 内容区域 -->
        <view class="content-box">
          <view class="header-row">
            <view class="user-info">
              <text class="nickname" :class="{ 'official': item.fromUser.id === 1 }">{{ item.fromUser.nickname }}</text>
              <view v-if="item.fromUser.id === 1" class="official-tag">
                <u-icon name="flash-fill" color="#fff" size="10"></u-icon>
              </view>
            </view>
            <text class="time">{{ formatTime(item.createdAt) }}</text>
          </view>

          <!-- 消息正文 -->
          <view class="message-row">
            <p class="desc-text">
              {{ getActionDesc(item) }}
              <text v-if="item.postTitle && item.type !== 6" class="highlight-text">"{{ item.postTitle }}"</text>
            </p>
            <view class="unread-dot" v-if="!item.isRead"></view>
          </view>

          <!-- 引用内容 (评论/回复内容，私信不显示) -->
          <view class="quote-box" v-if="item.content && item.type !== 6" :class="getQuoteClass(item.type)">
            <text class="quote-text">"{{ item.content }}"</text>
          </view>
        </view>
      </view>

      <u-loadmore :status="loadStatus" @loadmore="loadMore" marginTop="30" color="#64748b" />
      <view class="safe-area-bottom"></view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      list: [],
      page: 1,
      size: 10,
      loadStatus: 'loadmore',
      isLoading: false,
      navbarPaddingRight: 16,
      statusBarHeight: 0,
      currentType: null // 当前筛选的类型
    };
  },
  onLoad() {
    const systemInfo = uni.getSystemInfoSync();
    this.statusBarHeight = systemInfo.statusBarHeight || 0;
    
    this.loadData(true);

    // #ifdef MP-WEIXIN
    try {
      const menuButton = uni.getMenuButtonBoundingClientRect();
      this.navbarPaddingRight = (systemInfo.screenWidth - menuButton.left) + 8;
    } catch (e) {
      this.navbarPaddingRight = 94;
    }
    // #endif
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  onReachBottom() {
    this.loadData();
  },
  methods: {
    // 获取类型标签文本
    getTypeLabel(type) {
      const map = {
        1: '点赞',
        2: '点赞',
        3: '评论',
        4: '回复',
        5: '关注',
        6: '@我',
        7: '私信'
      };
      return map[type] || '通知';
    },
    // 获取类型样式类
    getTypeClass(type) {
      const map = {
        1: 'tag-pink',    // 点赞
        2: 'tag-pink',    // 点赞
        3: 'tag-green',   // 评论
        4: 'tag-blue',    // 回复
        5: 'tag-primary', // 关注
        6: 'tag-purple',  // @我
        7: 'tag-orange'   // 私信
      };
      return map[type] || 'tag-primary';
    },
    // 获取类型图标
    getTypeIcon(type) {
      const map = {
        1: 'thumb-up-fill',
        2: 'thumb-up-fill',
        3: 'chat-fill',
        4: 'chat-fill',
        5: 'account-fill',
        6: 'at',
        7: 'email-fill'
      };
      return map[type] || 'bell-fill';
    },
    // 获取操作描述
    getActionDesc(item) {
      const map = {
        1: '赞了你的帖子',
        2: '赞了你的评论',
        3: '评论了你的帖子',
        4: '回复了你的评论',
        5: '关注了你',
        6: '在内容中提到了你',
        7: '给你发了一条私信'
      };
      return map[item.type] || '有一条新消息';
    },
    // 引用框样式
    getQuoteClass(type) {
      const map = {
        3: 'border-green',
        4: 'border-blue'
      };
      return map[type] || 'border-primary';
    },
    
    formatTime(time) {
      if (!time) return '';
      // 简单处理：将 T 替换为空格，去掉秒
      // 示例: 2023-10-01T12:00:00 -> 2023-10-01 12:00
      const dateStr = time.replace('T', ' ');
      
      // 计算时间差，显示人性化时间
      const date = new Date(dateStr);
      const now = new Date();
      const diff = now - date;
      
      const minute = 60 * 1000;
      const hour = 60 * minute;
      const day = 24 * hour;
      
      if (diff < minute) return '刚刚';
      if (diff < hour) return Math.floor(diff / minute) + '分钟前';
      if (diff < day) return Math.floor(diff / hour) + '小时前';
      if (diff < 7 * day) return Math.floor(diff / day) + '天前';
      
      return dateStr.substring(0, 10);
    },
    
    async loadData(refresh = false) {
      if (this.isLoading) return;
      if (refresh) {
        this.page = 1;
        this.list = [];
        this.loadStatus = 'loading';
      } else if (this.loadStatus === 'nomore') {
        return;
      }
      
      this.isLoading = true;
      try {
        const res = await this.$request({
            url: '/api/notifications',
            method: 'GET',
            data: {
                page: this.page,
                size: this.size
            }
        });
        
        const records = res.records || [];
        
        // 格式化数据，确保 fromUser 对象存在，并处理头像
        const formattedRecords = records.map(item => {
          if (item.fromUser && item.fromUser.avatar) {
            item.fromUser.avatar = this.$utils.getFullImageUrl(item.fromUser.avatar);
          }
          return item;
        });

        if (refresh) {
            this.list = formattedRecords;
            uni.stopPullDownRefresh();
        } else {
            this.list = [...this.list, ...formattedRecords];
        }
        
        if (formattedRecords.length < this.size) {
            this.loadStatus = 'nomore';
        } else {
            this.loadStatus = 'loadmore';
            this.page++;
        }
      } catch (e) {
        console.error(e);
        this.loadStatus = 'loadmore';
        if (refresh) uni.stopPullDownRefresh();
      } finally {
        this.isLoading = false;
      }
    },
    
    async markAllRead() {
        try {
            await this.$request({
                url: '/api/notifications/read-all',
                method: 'PUT'
            });
            this.list.forEach(item => item.isRead = 1);
            uni.showToast({ title: '全部已读', icon: 'success' });
        } catch (e) {
            console.error(e);
            uni.showToast({ title: '操作失败', icon: 'none' });
        }
    },

    // 过滤类型
    filterByType(type) {
      const pageMap = {
        'reply': '/pages/community/reply-detail',
        'like': '/pages/community/like-detail',
        'follow': '/pages/community/fan-detail'
      };
      if (pageMap[type]) {
        uni.navigateTo({
          url: pageMap[type]
        });
      }
    },

    goToMessageList() {
      uni.navigateTo({
        url: '/pages/message/message-list'
      });
    },
    
    goBack() {
      // 如果当前有筛选，点击返回则先清除筛选，否则返回上一页
      if (this.currentType) {
        this.currentType = null;
        this.loadData(true);
      } else {
        uni.navigateBack();
      }
    },
    
    handleNotificationClick(item) {
      // 标记已读
      if (!item.isRead) {
        this.$request({
            url: `/api/notifications/${item.id}/read`,
            method: 'PUT'
        }).then(() => {
            item.isRead = 1;
        });
      }

      // 跳转逻辑
      if (item.type === 5) {
        uni.navigateTo({ url: `/pages/my/profile?id=${item.fromUser.id}` });
      } else if (item.type === 7) {
        // 跳转到私信详情 (假设有此页面)
        uni.navigateTo({ url: `/pages/message/chat?id=${item.fromUser.id}` });
      } else if ([1, 2, 3, 4, 6].includes(item.type)) {
        if (item.postId) {
            const targetParam = item.targetId ? `&targetId=${item.targetId}` : '';
            uni.navigateTo({
                url: `/pages/post/detail?id=${item.postId}${targetParam}`
            });
        }
      }
    },
    loadMore() {
        this.loadData();
    }
  }
};
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: $pitch-pulse-bg-dark;
  color: #fff;
}

/* 自定义导航栏样式 */
.custom-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba($pitch-pulse-bg-dark, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

  .navbar-inner {
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 40rpx;

    .left {
      display: flex;
      align-items: center;
      min-width: 80rpx;
    }

    .center {
      flex: 1;
      display: flex;
      justify-content: center;
      
      .brand-text {
        font-size: 34rpx;
        font-weight: 800;
        color: #fff;
        letter-spacing: 2rpx;
        
        &::after {
          content: 'PULSE';
          color: $pitch-pulse-primary;
        }
      }
    }

    .right {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 20rpx;
      min-width: 80rpx;
    }
    
    .icon-btn {
      width: 64rpx;
      height: 64rpx;
      background-color: rgba(255, 255, 255, 0.05);
      border-radius: 16rpx;
      display: flex;
      justify-content: center;
      align-items: center;
      &:active {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

/* 分类功能区 */
.category-section {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 32rpx;
  background-color: transparent;

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;

    .icon-wrapper {
      width: 100rpx;
      height: 100rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: rgba(255, 255, 255, 0.05);
      border: 1rpx solid rgba(255, 255, 255, 0.1);
      transition: all 0.2s;

      &:active {
        transform: scale(0.95);
        background-color: rgba(255, 255, 255, 0.1);
      }

      &.bg-emerald { 
        border-color: rgba(16, 185, 129, 0.3);
      }
      &.bg-rose { 
        border-color: rgba(244, 63, 94, 0.3);
      }
      &.bg-sky { 
        border-color: rgba(14, 165, 233, 0.3);
      }
      &.bg-orange {
        border-color: rgba(249, 115, 22, 0.3);
      }
    }

    .label {
      font-size: 24rpx;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.6);
    }
  }
}

.notification-list {
  padding: 0 40rpx 40rpx;
  
  .notification-item {
    display: flex;
    padding: 32rpx 0;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
    
    &:active {
      background-color: rgba(255, 255, 255, 0.02);
    }
    
    .avatar-box {
      position: relative;
      margin-right: 24rpx;
      flex-shrink: 0;

      .avatar-img {
        width: 96rpx;
        height: 96rpx;
        border-radius: 16rpx;
        border: 1rpx solid rgba(255, 255, 255, 0.1);
        background-color: rgba(255, 255, 255, 0.05);
      }
      
      .badge-icon {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 36rpx;
        height: 36rpx;
        background-color: $pitch-pulse-primary;
        border-radius: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 4rpx solid $pitch-pulse-bg-dark;
        
        :deep(.u-icon__icon) {
          color: #000 !important;
        }
      }
    }
    
    .content-box {
      flex: 1;
      min-width: 0;
      
      .header-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8rpx;
        
        .user-info {
          display: flex;
          align-items: center;
          gap: 12rpx;
          
          .nickname {
            font-size: 28rpx;
            font-weight: 700;
            color: #fff;
            
            &.official {
              color: $pitch-pulse-primary;
            }
          }
          
          .official-tag {
            background-color: $pitch-pulse-primary;
            padding: 2rpx 8rpx;
            border-radius: 4rpx;
            display: flex;
            align-items: center;
            
            :deep(.u-icon__icon) {
              color: #000 !important;
            }
          }
        }
        
        .time {
          font-size: 22rpx;
          color: rgba(255, 255, 255, 0.4);
        }
      }
      
      .message-row {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 20rpx;
        
        .desc-text {
          font-size: 26rpx;
          color: rgba(255, 255, 255, 0.7);
          line-height: 1.5;
          
          .highlight-text {
            color: $pitch-pulse-primary;
            margin-left: 8rpx;
            font-weight: 500;
          }
        }
        
        .unread-dot {
          width: 12rpx;
          height: 12rpx;
          background-color: $pitch-pulse-primary;
          border-radius: 4rpx;
          margin-top: 12rpx;
          flex-shrink: 0;
          box-shadow: 0 0 10rpx rgba($pitch-pulse-primary, 0.5);
        }
      }
      
      .quote-box {
        margin-top: 16rpx;
        padding: 16rpx 20rpx;
        background-color: rgba(255, 255, 255, 0.03);
        border-radius: 12rpx;
        border-left: 4rpx solid rgba(255, 255, 255, 0.1);
        
        &.border-green { border-left-color: #10b981; }
        &.border-blue { border-left-color: #0ea5e9; }
        &.border-primary { border-left-color: $pitch-pulse-primary; }
        
        .quote-text {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.5);
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }
      }
    }
  }
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>