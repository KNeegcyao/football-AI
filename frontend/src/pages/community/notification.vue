<template>
  <view class="container">
    <!-- 自定义导航栏 -->
    <u-navbar :safeAreaInsetTop="true" :placeholder="true" bgColor="rgba(10, 10, 10, 0.85)" leftIconColor="#fff">
      <view class="navbar-content" slot="center">
        <text class="brand-text">PITCHPULSE</text>
        <text class="title-text">消息通知</text>
      </view>
      <view class="navbar-right" slot="right">
        <view class="read-all-btn" @click="markAllRead">
          <text>全部已读</text>
        </view>
      </view>
    </u-navbar>

    <view class="notification-list">
      <view class="notification-item" v-for="(item, index) in list" :key="index" @click="handleNotificationClick(item)">
        <!-- 头像区域 -->
        <view class="avatar-box">
          <u-avatar :src="item.fromUser.avatar || '/static/default-avatar.png'" size="48"></u-avatar>
          <view class="badge-icon" v-if="getTypeIcon(item.type)">
            <u-icon :name="getTypeIcon(item.type)" size="12" color="#fff"></u-icon>
          </view>
        </view>

        <!-- 内容区域 -->
        <view class="content-box">
          <view class="header-row">
            <view class="user-info">
              <text class="nickname">{{ item.fromUser.nickname }}</text>
              <view class="type-tag" :class="getTypeClass(item.type)">
                <text>{{ getTypeLabel(item.type) }}</text>
              </view>
            </view>
            <text class="time">{{ formatTime(item.createdAt) }}</text>
          </view>

          <!-- 描述文本 -->
          <text class="desc-text">
            {{ getActionDesc(item.type) }}
            <text v-if="item.postTitle" class="highlight-text">"{{ item.postTitle }}"</text>
          </text>

          <!-- 引用内容 (评论/回复内容) -->
          <view class="quote-box" v-if="item.content" :class="getQuoteClass(item.type)">
            <text class="quote-text">"{{ item.content }}"</text>
          </view>
        </view>

        <!-- 未读红点 -->
        <view class="status-box" v-if="!item.isRead">
          <view class="red-dot"></view>
        </view>
      </view>

      <u-loadmore :status="loadStatus" @loadmore="loadMore" marginTop="30" />
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
      isLoading: false
    };
  },
  onLoad() {
    this.loadData(true);
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
                size: this.size
            }
        });
        
        // request.js 已解包 data，直接使用
        const records = res.records || [];
        if (refresh) {
            this.list = records;
            uni.stopPullDownRefresh();
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
        this.loadStatus = 'loadmore';
        if (refresh) uni.stopPullDownRefresh();
      } finally {
        this.isLoading = false;
      }
    },
    
    async markAllRead() {
        // TODO: 调用后端全部已读接口
        uni.showToast({ title: '全部已读', icon: 'success' });
        this.list.forEach(item => item.isRead = 1);
    },

    async handleNotificationClick(item) {
      // 标记已读
      if (!item.isRead) {
        try {
            await this.$request({
                url: `/api/notifications/${item.id}/read`,
                method: 'PUT'
            });
            item.isRead = 1;
        } catch (e) {
            console.error(e);
        }
      }

      // 跳转逻辑
      if (item.type === 5) {
        // 关注通知 -> 跳转到用户主页
        uni.navigateTo({ url: `/pages/my/profile?id=${item.fromUser.id}` });
      } else if ([1, 2, 3, 4].includes(item.type)) {
        // 评论/点赞 -> 跳转到帖子详情
        if (item.postId) {
            // 传递 targetId (评论ID/回复ID) 以便定位
            // 注意：对于点赞帖子(type=1)，targetId可能是点赞记录ID，详情页无法定位，但也没关系
            // 对于评论/回复(type=3,4)，targetId是评论ID，可以定位
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
$bg-color: #0A0A0A;
$card-bg: #161616;
$text-primary: #f1f5f9;
$text-secondary: #cbd5e1;
$text-muted: #64748b;
$accent-color: #f2b90d;
$border-color: rgba(255, 255, 255, 0.05);

.container {
  min-height: 100vh;
  background-color: $bg-color;
  padding-bottom: env(safe-area-inset-bottom);
}

.navbar-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .brand-text {
    font-size: 20rpx;
    color: $accent-color;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 4rpx;
    margin-bottom: 4rpx;
  }
  
  .title-text {
    font-size: 32rpx;
    font-weight: bold;
    color: #fff;
  }
}

.navbar-right {
  margin-right: 20rpx;
  
  .read-all-btn {
    padding: 8rpx 20rpx;
    background-color: rgba($accent-color, 0.1);
    border-radius: 999px;
    
    text {
      font-size: 24rpx;
      color: $accent-color;
      font-weight: 500;
    }
    
    &:active {
      background-color: rgba($accent-color, 0.2);
    }
  }
}

.notification-list {
  padding: 30rpx;
  
  .notification-item {
    background-color: $card-bg;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 24rpx;
    display: flex;
    align-items: flex-start;
    border: 1px solid $border-color;
    transition: background-color 0.2s;
    
    &:active {
      background-color: rgba(255, 255, 255, 0.05);
    }
    
    .avatar-box {
      position: relative;
      margin-right: 24rpx;
      flex-shrink: 0;
      
      .badge-icon {
        position: absolute;
        bottom: -4rpx;
        right: -4rpx;
        background-color: $accent-color;
        border-radius: 50%;
        padding: 4rpx;
        border: 2rpx solid $card-bg;
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
          gap: 16rpx;
          
          .nickname {
            font-size: 28rpx;
            font-weight: bold;
            color: #fff;
          }
          
          .type-tag {
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
            
            text {
              font-size: 20rpx;
              font-weight: bold;
              text-transform: uppercase;
            }
            
            &.tag-primary { background-color: rgba($accent-color, 0.1); text { color: $accent-color; } }
            &.tag-blue { background-color: rgba(59, 130, 246, 0.1); text { color: #60a5fa; } }
            &.tag-pink { background-color: rgba(236, 72, 153, 0.1); text { color: #f472b6; } }
            &.tag-green { background-color: rgba(34, 197, 94, 0.1); text { color: #4ade80; } }
          }
        }
        
        .time {
          font-size: 22rpx;
          color: $text-muted;
        }
      }
      
      .desc-text {
        font-size: 26rpx;
        color: $text-secondary;
        margin-bottom: 16rpx;
        display: block;
        
        .highlight-text {
          color: rgba($accent-color, 0.9);
          font-weight: 500;
          margin-left: 8rpx;
        }
      }
      
      .quote-box {
        background-color: rgba(255, 255, 255, 0.05);
        border-radius: 16rpx;
        padding: 20rpx;
        border-left: 4rpx solid $accent-color;
        
        .quote-text {
          font-size: 24rpx;
          color: $text-muted;
          line-height: 1.5;
          // 多行省略
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 2;
          overflow: hidden;
        }
        
        &.border-green { border-left-color: #4ade80; }
        &.border-blue { border-left-color: #60a5fa; }
      }
    }
    
    .status-box {
      margin-left: 20rpx;
      margin-top: 20rpx;
      
      .red-dot {
        width: 16rpx;
        height: 16rpx;
        background-color: #ef4444;
        border-radius: 50%;
      }
    }
  }
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
}
</style>