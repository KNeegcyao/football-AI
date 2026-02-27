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
              {{ getActionDesc(item.type) }}
              <text v-if="item.postTitle" class="highlight-text">"{{ item.postTitle }}"</text>
            </p>
            <view class="unread-dot" v-if="!item.isRead"></view>
          </view>

          <!-- 引用内容 (评论/回复内容) -->
          <view class="quote-box" v-if="item.content" :class="getQuoteClass(item.type)">
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
        5: '关注'
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
        5: 'tag-primary'  // 关注
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
        5: 'account-fill'
      };
      return map[type] || 'bell-fill';
    },
    // 获取操作描述
    getActionDesc(type) {
      const map = {
        1: '赞了你的帖子',
        2: '赞了你的评论',
        3: '评论了你的帖子',
        4: '回复了你的评论',
        5: '关注了你'
      };
      return map[type] || '有一条新消息';
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
                size: this.size,
                type: this.currentType // 增加类型过滤
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
      } else if ([1, 2, 3, 4].includes(item.type)) {
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
// 变量定义
$bg-color: #000000;
$card-bg: #000000;
$text-primary: #ffffff;
$text-secondary: #94a3b8;
$text-muted: #64748b;
$accent-color: #991B1B; // Deep Crimson
$gold-color: #D4AF37;

.container {
  min-height: 100vh;
  background-color: $bg-color;
  color: $text-primary;
}

/* 自定义导航栏样式 */
.custom-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);

  .navbar-inner {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32rpx;

    .left, .right {
      display: flex;
      align-items: center;
      gap: 20rpx;
      min-width: 100rpx;
    }

    .center {
      flex: 1;
      display: flex;
      justify-content: center;
      
      .brand-text {
        font-size: 34rpx;
        font-weight: 800;
        color: $accent-color;
        letter-spacing: 2rpx;
      }
    }
    
    .icon-btn {
      padding: 10rpx;
      &:active {
        opacity: 0.7;
      }
    }
  }
}

/* 分类功能区 */
.category-section {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 32rpx;
  background-color: #000;

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;

    .icon-wrapper {
      width: 112rpx;
      height: 112rpx;
      border-radius: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.2s;

      &:active {
        transform: scale(0.9);
      }

      &.bg-emerald { background-color: rgba(16, 185, 129, 0.15); }
      &.bg-rose { background-color: rgba(244, 63, 94, 0.15); }
      &.bg-sky { background-color: rgba(14, 165, 233, 0.15); }
    }

    .label {
      font-size: 24rpx;
      font-weight: 500;
      color: #94a3b8;
    }
  }
}

.notification-list {
  padding: 0 32rpx 40rpx;
  
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
        width: 112rpx;
        height: 112rpx;
        border-radius: 50%;
        border: 2rpx solid rgba($accent-color, 0.2);
        background-color: #1a1a1a;
      }
      
      .badge-icon {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 36rpx;
        height: 36rpx;
        background-color: $gold-color;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 4rpx solid #000;
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
            font-size: 30rpx;
            font-weight: 700;
            color: #f1f5f9;
            
            &.official {
              color: $accent-color;
            }
          }
          
          .official-tag {
            background-color: $gold-color;
            padding: 2rpx 6rpx;
            border-radius: 6rpx;
            display: flex;
            align-items: center;
          }
        }
        
        .time {
          font-size: 22rpx;
          color: #64748b;
        }
      }

      .message-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;

        .desc-text {
          flex: 1;
          font-size: 28rpx;
          color: #94a3b8;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          
          .highlight-text {
            color: $accent-color;
            margin-left: 8rpx;
          }
        }

        .unread-dot {
          width: 12rpx;
          height: 12rpx;
          background-color: #f43f5e;
          border-radius: 50%;
          margin-left: 16rpx;
          box-shadow: 0 0 10rpx rgba(244, 63, 94, 0.5);
        }
      }
      
      .quote-box {
        background-color: #111;
        border-radius: 12rpx;
        padding: 16rpx 20rpx;
        border-left: 4rpx solid $accent-color;
        
        .quote-text {
          font-size: 26rpx;
          color: #64748b;
          line-height: 1.5;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }
        
        &.border-green { border-left-color: #10b981; }
        &.border-blue { border-left-color: #0ea5e9; }
      }
    }
  }
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>