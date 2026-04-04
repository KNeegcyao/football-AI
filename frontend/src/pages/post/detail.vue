<template>
  <view class="container" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- Status Bar -->
    <view class="status-bar bg-nav-bar"></view>

    <!-- Loading State -->
    <view v-if="loading" class="loading-state">
      <text :style="{ color: 'var(--text-main)' }">Loading...</text>
    </view>

    <template v-else-if="post">
      <!-- Header -->
      <view class="header bg-nav-bar border-b border-theme-main">
        <view class="header-left">
          <view class="back-btn" @click="goBack">
            <image :src="getFullImageUrl('/static/icons/actions/arrow_back.svg')" :style="{ width: '48rpx', height: '48rpx', filter: iconFilter, opacity: 0.8 }"></image>
          </view>
        </view>
        <view class="header-center">
          <view 
            class="author-info" 
            @click.stop="navigateToProfile(post.userId)"
            hover-class="clickable-hover"
            :hover-stay-time="100"
          >
            <view class="avatar-container">
              <image class="author-avatar bg-theme-secondary" :src="post.userAvatar" mode="aspectFill"></image>
            </view>
            <view class="author-details">
              <view class="author-name-row">
                <text class="author-name">{{ post.userName }}</text>
                <image :src="getFullImageUrl('/static/icons/status/verified.svg')" class="verified-icon" :style="{ width: '14px', height: '14px', filter: iconFilter }"></image>
              </view>
              <text class="author-role">社区成员</text>
            </view>
          </view>
        </view>
        <view class="header-right">
          <button 
            v-if="post.userId !== currentUserId"
            class="follow-btn" 
            :class="{ 'following': isFollowing }"
            @click="toggleFollow"
          >
            {{ isFollowing ? '已关注' : '关注' }}
          </button>
        </view>
      </view>

      <scroll-view scroll-y class="content-scroll" :scroll-into-view="scrollTarget" :scroll-with-animation="true">
        <!-- Article Content -->
        <view class="article">
          <view class="article-meta">
            <text class="tag">{{ categoryText }}</text>
            <text class="time">{{ formatTime(post.createdAt) }}</text>
          </view>
          
          <text class="article-title">{{ post.title }}</text>
          
          <view class="article-body">
            <text class="body-text">{{ post.content }}</text>
          </view>

          <!-- Image Grid -->
          <view class="image-grid" v-if="post.images && post.images.length > 0">
            <view class="grid-item bg-theme-secondary" v-for="(img, index) in post.images" :key="index" @click="previewImage(index)">
              <image class="grid-image" :src="img" mode="aspectFill"></image>
            </view>
          </view>

          <!-- Stats -->
          <view class="interaction-stats border-theme-main">
            <view class="avatar-stack" v-if="post.recentLikes && post.recentLikes.length > 0">
              <image 
                v-for="(liker, index) in post.recentLikes" 
                :key="index" 
                class="stack-avatar bg-theme-secondary" 
                :src="liker.avatar" 
                mode="aspectFill"
                @click.stop="navigateToProfile(liker.id)"
                hover-class="clickable-hover"
                :hover-stay-time="100"
              ></image>
              <view class="stack-count" style="background-color: #f2b90d; color: #000; display: flex; align-items: center; justify-content: center;">
                <image :src="getFullImageUrl('/static/icons/status/thumb_up.svg')" style="width: 24rpx; height: 24rpx; filter: none;"></image>
              </view>
            </view>
            <view class="view-stats">
              <text class="stat-highlight">{{ post.views || 0 }}</text> 浏览 • <text class="stat-highlight">{{ post.likes || 0 }}</text> 点赞
            </view>
          </view>
        </view>

        <!-- Comments -->
        <view class="comments-section">
          <view class="comments-header">
            <text class="comments-title">评论 ({{ post.commentCount || comments.length }})</text>
            <view class="header-actions">
              <view class="sort-btn" @click="toggleSort">
                <text>{{ sortType === 'newest' ? '最新 ▼' : '最热 ▼' }}</text>
              </view>
            </view>
          </view>

          <!-- AI Summary Box (Minimal Design) -->
          <view v-if="aiSummaryResult || summarizingAi" class="ai-summary-card">
            <view class="ai-summary-header">
              <view class="ai-avatar">
                <svg class="ai-avatar-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .963 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.581a.5.5 0 0 1 0 .964L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.963 0z" />
                </svg>
              </view>
              <text class="ai-title-text">AI 总结</text>
            </view>

            <view class="ai-summary-body">
              <view v-if="summarizingAi" class="ai-loading-skeleton">
                <view class="skeleton-line" style="width: 100%;"></view>
                <view class="skeleton-line" style="width: 85%;"></view>
                <view class="skeleton-line" style="width: 60%;"></view>
              </view>
              <template v-else>
                <view class="ai-content-wrapper" @click="aiSummaryExpanded = !aiSummaryExpanded">
                  <text class="ai-text" :class="{ 'collapsed': !aiSummaryExpanded }">{{ aiSummaryResult }}</text>
                </view>
              </template>
            </view>
            
            <view class="ai-summary-footer" v-if="!summarizingAi">
              <text class="ai-footer-text">内容由 AI 生成</text>
              <view v-if="aiSummaryResult.length > 60" class="ai-expand-trigger" @click="aiSummaryExpanded = !aiSummaryExpanded">
                <text>{{ aiSummaryExpanded ? '收起' : '展开' }}</text>
                <view class="expand-icon-svg" :class="{ 'rotated': aiSummaryExpanded }">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
                </view>
              </view>
            </view>
          </view>

          <view class="comments-list">
            <view class="comment-item" v-for="(comment, index) in comments" :key="index" :id="'comment-' + comment.id" :class="{ 'highlight': comment.id == targetId }">
              <image 
                class="comment-avatar bg-theme-secondary" 
                :src="comment.userAvatar" 
                mode="aspectFill" 
                @click.stop="navigateToProfile(comment.userId)"
                hover-class="clickable-hover"
                :hover-stay-time="100"
              ></image>
              <view class="comment-content-wrapper">
                <view class="comment-bubble bg-theme-secondary">
                  <view class="comment-user-row">
                    <text 
                      class="comment-username" 
                      @click.stop="navigateToProfile(comment.userId)"
                      hover-class="text-hover"
                      :hover-stay-time="100"
                    >{{ comment.userName }}</text>
                    <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
                  </view>
                  <text class="comment-text">{{ comment.content }}</text>
                </view>
                <view class="comment-actions">
                  <view class="action-item" @click="likeComment(comment)">
                    <image :src="getFullImageUrl('/static/icons/status/favorite.svg')" :style="{ width: '28rpx', height: '28rpx', filter: iconFilter, opacity: 0.6 }"></image>
                    <text class="action-text">{{ comment.likes || 0 }}</text>
                  </view>
                  <text class="reply-btn" @click="handleReply(comment)">回复</text>
                </view>

                <!-- Nested Replies -->
                <view class="nested-replies" v-if="comment.replies && comment.replies.length > 0">
                  <view class="comment-item nested" v-for="(reply, rIndex) in comment.replies" :key="rIndex" :id="'comment-' + reply.id" :class="{ 'highlight': reply.id == targetId }">
                    <image 
                      class="comment-avatar small bg-theme-secondary" 
                      :src="reply.userAvatar" 
                      mode="aspectFill" 
                      @click.stop="navigateToProfile(reply.userId)"
                      hover-class="clickable-hover"
                      :hover-stay-time="100"
                    ></image>
                    <view class="comment-content-wrapper">
                      <view class="comment-bubble semi-transparent bg-theme-secondary">
                        <view class="comment-user-row">
                          <text 
                            class="comment-username" 
                            @click.stop="navigateToProfile(reply.userId)"
                            hover-class="text-hover"
                            :hover-stay-time="100"
                          >{{ reply.userName }}</text>
                          <text class="comment-time">{{ formatTime(reply.createdAt) }}</text>
                        </view>
                        <text class="comment-text">
                          <text class="reply-label" v-if="reply.replyToUserName">回复 @{{ reply.replyToUserName }}: </text>
                          {{ reply.content }}
                        </text>
                      </view>
                      <view class="comment-actions">
                          <view class="action-item" @click="likeComment(reply)">
                            <image :src="getFullImageUrl('/static/icons/status/favorite.svg')" :style="{ width: '24rpx', height: '24rpx', filter: iconFilter, opacity: 0.6 }"></image>
                            <text class="action-text">{{ reply.likes || 0 }}</text>
                          </view>
                        <text class="reply-btn" @click="handleReply(reply)">回复</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
            
            <view v-if="comments.length === 0" style="text-align: center; padding: 20px; color: var(--text-secondary);">
              <text>暂无评论。</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </template>
    
    <view v-else class="error-state">
      <text :style="{ color: 'var(--text-main)' }">加载帖子失败。</text>
    </view>

    <!-- Unified Bottom Bar -->
    <view class="bottom-bar-wrapper" v-if="post">
      <view class="reply-info bg-theme-secondary">
        <text class="reply-to">回复 @{{ replyTarget.userName }}</text>
        <image :src="getFullImageUrl('/static/icons/actions/close.svg')" class="cancel-reply" @click="cancelReply" :style="{ width: '32rpx', height: '32rpx', filter: iconFilter, opacity: 0.6 }"></image>
      </view>
      <view class="bottom-bar bg-nav-bar border-t border-theme-main">
        <input 
          class="comment-input bg-theme-secondary" 
          :placeholder="replyTarget ? '回复评论...' : '说点什么...'" 
          v-model="commentText"
          :disabled="submitting"
          cursor-spacing="20"
          :focus="!!replyTarget"
          placeholder-style="color: var(--text-secondary)"
        />
        
        <button 
          v-if="commentText.trim()"
          class="send-btn" 
          :disabled="submitting"
          @click="submitComment"
        >
          发送
        </button>
        
        <template v-else>
          <view class="action-btn ai-btn" @click="generateAiComment" :class="{ 'disabled': generatingAi }">
            <text style="font-size: 16px; margin-right: 2px;">🤖</text>
            <text class="action-text" style="color: #f2b90d; font-size: 13px; font-weight: 600;">AI评论</text>
          </view>
          <view class="action-icons-group">
            <view class="action-btn" @click="handleLike">
              <image :src="post.isLiked ? getFullImageUrl('/static/icons/status/thumb_up.svg') : getFullImageUrl('/static/icons/status/thumb_up.svg')" :style="{ width: '22px', height: '22px', filter: post.isLiked ? 'none' : iconFilter, opacity: post.isLiked ? 1 : 0.8 }"></image>
              <text class="action-text" :style="{ color: post.isLiked ? '#f2b90d' : 'var(--text-main)' }">
                {{ post.likes || 0 }}
              </text>
            </view>
            <view class="action-btn" @click="handleFavorite">
              <image :src="post.isFavorited ? getFullImageUrl('/static/icons/actions/star_fill.svg') : getFullImageUrl('/static/icons/actions/star.svg')" :style="{ width: '24px', height: '24px', filter: post.isFavorited ? 'none' : iconFilter, opacity: post.isFavorited ? 1 : 0.8 }"></image>
            </view>
            <view class="action-btn">
              <image :src="getFullImageUrl('/static/icons/actions/chat_bubble.svg')" :style="{ width: '22px', height: '22px', filter: iconFilter, opacity: 0.8 }"></image>
              <text class="action-text" style="color: var(--text-main)">{{ post.commentCount || comments.length }}</text>
            </view>
          </view>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { postApi, fileApi, favoriteApi, aiApi } from '@/api';
import { useThemeStore } from '@/store/theme';
import { getFullImageUrl } from '@/utils/request';

const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const iconFilter = computed(() => {
  return themeStore.theme === 'dark' ? 'invert(1) brightness(2)' : 'none';
});

const post = ref(null);
const comments = ref([]);
const postId = ref(null);
const loading = ref(true);
const sortType = ref('newest'); // newest, hottest
const isFollowing = ref(false);
const commentText = ref('');
const submitting = ref(false);
const generatingAi = ref(false);
const summarizingAi = ref(false);
const aiSummaryResult = ref('');
const aiSummaryExpanded = ref(false);
const scrollTarget = ref('');
const targetId = ref(null);
const replyTarget = ref(null); // { id, userName }
const currentUserId = ref(null);
const statusBarHeight = ref(20);

onMounted(() => {
  const userInfoStr = uni.getStorageSync('userInfo');
  if (userInfoStr) {
    try {
      const userInfo = typeof userInfoStr === 'string' ? JSON.parse(userInfoStr) : userInfoStr;
      if (userInfo && userInfo.id) {
        currentUserId.value = userInfo.id;
      }
    } catch (e) {
      console.error('Failed to parse userInfo', e);
    }
  }
  const sysInfo = uni.getSystemInfoSync();
  if (sysInfo.statusBarHeight) {
    statusBarHeight.value = sysInfo.statusBarHeight;
  }
});

const categoryText = computed(() => {
  if (!post.value) return '# 足球';
  let text = post.value.topicName || post.value.circleName || post.value.category || '足球';
  // 确保只有一个 #
  text = text.replace(/^#\s*/, '');
  return `# ${text}`;
});

const goBack = () => {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    // 如果没有上一页（如分享卡片进入或发布跳转导致栈替换），回到社区首页
    uni.switchTab({
      url: '/pages/community/community'
    });
  }
};

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + 'm ago';
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + 'h ago';
  } else {
    return `${date.getMonth() + 1}/${date.getDate()}`;
  }
};

const loadPostDetail = async (id) => {
  try {
    loading.value = true;
    const data = await postApi.getDetail(id);
    if (data) {
      // Parse images if needed
      let images = [];
      if (data.images) {
        try {
            images = typeof data.images === 'string' ? JSON.parse(data.images) : data.images;
        } catch (e) {
            console.error('Failed to parse images JSON:', e);
            images = [];
        }
      }
      
      // Process image URLs
      const processedImages = images.map(img => getFullImageUrl(img));
      
      // Process avatar URL
      const avatarUrl = data.userAvatar ? getFullImageUrl(data.userAvatar) : '/static/default-avatar.png';

      // Process recent likes avatars
      const processedRecentLikes = (data.recentLikes || []).map(liker => ({
        ...liker,
        avatar: liker.avatar ? getFullImageUrl(liker.avatar) : '/static/default-avatar.png'
      }));

      post.value = {
        ...data,
        userName: data.userName || 'Unknown User',
        userAvatar: avatarUrl,
        images: processedImages,
        recentLikes: processedRecentLikes,
        views: data.views || 0,
        likes: data.likes || 0,
        isLiked: data.isLiked || false,
        isFavorited: data.isFavorited || false
      };
      
      // Check follow status
      if (post.value.userId) {
        try {
          const res = await postApi.checkFollowStatus(post.value.userId);
          isFollowing.value = res;
        } catch (e) {
          console.error('Failed to check follow status:', e);
        }
      }
    }
  } catch (error) {
    console.error('Failed to load post detail:', error);
  } finally {
    loading.value = false;
  }
};

const loadComments = async (id) => {
  try {
    const data = await postApi.getComments(id, { 
      page: 1, 
      size: 20,
      sort: sortType.value 
    });
    if (data) {
      const records = data.records || [];
      comments.value = records.map(c => ({
        ...c,
        userName: c.nickname || 'Unknown User',
        userAvatar: c.avatar ? getFullImageUrl(c.avatar) : '/static/default-avatar.png',
        likes: c.likes || 0,
        // Map replies recursively if needed, but for now flat list or simple structure
        replies: (c.replies || []).map(r => ({
            ...r,
            userName: r.nickname || 'Unknown User',
            userAvatar: r.avatar ? getFullImageUrl(r.avatar) : '/static/default-avatar.png',
            likes: r.likes || 0,
            replyToUserName: r.replyToNickname // 映射后端字段
        }))
      }));

      // 如果有目标评论ID，尝试滚动到该位置
      if (targetId.value) {
        nextTick(() => {
          setTimeout(() => {
            scrollTarget.value = 'comment-' + targetId.value;
            console.log('Scrolling to target:', scrollTarget.value);
            
            // 3秒后移除高亮
            setTimeout(() => {
              targetId.value = null;
            }, 3000);
          }, 500);
        });
      }
      
      // Auto trigger AI summary if there are comments and not already generated
      if (comments.value.length > 0 && !aiSummaryResult.value && !summarizingAi.value) {
        summarizeComments();
      }
    }
  } catch (error) {
    console.error('Failed to load comments:', error);
  }
};

const toggleFollow = async () => {
  if (!post.value || !post.value.userId) return;
  
  try {
    if (isFollowing.value) {
      await postApi.unfollowUser(post.value.userId);
      isFollowing.value = false;
      uni.showToast({ title: '已取消关注', icon: 'none' });
    } else {
      await postApi.followUser(post.value.userId);
      isFollowing.value = true;
      uni.showToast({ title: '已关注', icon: 'success' });
    }
  } catch (error) {
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const toggleSort = () => {
  sortType.value = sortType.value === 'newest' ? 'hottest' : 'newest';
  loadComments(postId.value);
};

const handleReply = (comment) => {
  replyTarget.value = {
    id: comment.id,
    userName: comment.userName
  };
  // 滚动到底部或聚焦输入框
  nextTick(() => {
    // 聚焦逻辑通常在 uniapp 中通过 focus 属性实现
  });
};

const cancelReply = () => {
  replyTarget.value = null;
};

const submitComment = async () => {
  if (!commentText.value.trim()) {
    return;
  }
  
  submitting.value = true;
  try {
    const params = {
      postId: postId.value,
      content: commentText.value
    };
    
    // 如果是回复某条评论
    if (replyTarget.value) {
      params.parentId = replyTarget.value.id;
    }
    
    await postApi.createComment(params);
    
    const isReply = !!replyTarget.value;
    commentText.value = '';
    replyTarget.value = null; // 清除回复目标
    uni.showToast({ title: isReply ? '回复成功' : '评论成功', icon: 'success' });
    
    // Reload comments and update count
    await loadComments(postId.value);
    if (post.value) {
      post.value.commentCount = (post.value.commentCount || 0) + 1;
    }
  } catch (error) {
    uni.showToast({ title: '评论失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

const generateAiComment = async () => {
  if (generatingAi.value) return;
  
  if (!post.value || !post.value.content) {
    uni.showToast({ title: '帖子内容为空，无法生成', icon: 'none' });
    return;
  }

  generatingAi.value = true;
  uni.showLoading({ title: 'AI思考中...' });
  try {
    let targetContent = post.value.content;
    
    // 如果是回复某人，可以将回复对象的评论内容加上，让AI更有针对性
    // 这里简单起见，目前直接根据帖子内容生成
    const res = await aiApi.generateComment({ content: targetContent });
    
    if (res) {
      // 成功生成后，将内容填入输入框，让用户确认后再发送
      commentText.value = res;
      uni.showToast({ title: '已生成', icon: 'success' });
    } else {
      uni.showToast({ title: '生成失败，请重试', icon: 'none' });
    }
  } catch (error) {
    console.error('AI comment generation failed:', error);
    uni.showToast({ title: '生成失败，请重试', icon: 'none' });
  } finally {
    generatingAi.value = false;
    uni.hideLoading();
  }
};

const summarizeComments = async () => {
  if (summarizingAi.value) return;
  if (!comments.value || comments.value.length === 0) {
    return;
  }

  summarizingAi.value = true;
  aiSummaryResult.value = '';
  
  try {
    // 提取所有评论内容，限制数量避免超出token限制
    const commentTexts = comments.value.slice(0, 30).map(c => c.content);
    
    const res = await aiApi.analyzeComments(commentTexts);
    if (res) {
      aiSummaryResult.value = res;
    }
  } catch (error) {
    console.error('AI comment summarization failed:', error);
  } finally {
    summarizingAi.value = false;
  }
};

const likeComment = async (comment) => {
  try {
    const res = await postApi.like({
      targetId: comment.id,
      targetType: 2 // 2 for comment
    });
    
    if (res && res.liked !== undefined) {
      if (res.liked) {
        comment.likes = (comment.likes || 0) + 1;
        uni.showToast({ title: '已点赞', icon: 'none' });
      } else {
        comment.likes = Math.max(0, (comment.likes || 1) - 1);
        uni.showToast({ title: '已取消', icon: 'none' });
      }
    }
  } catch (error) {
    console.error('Comment like failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const handleLike = async () => {
  if (!post.value) return;
  try {
    const res = await postApi.like({
      targetId: post.value.id,
      targetType: 1 // 1 for post
    });
    
    if (res && res.liked !== undefined) {
      post.value.isLiked = res.liked;
      if (res.liked) {
        post.value.likes++;
        uni.showToast({ title: '已点赞', icon: 'none' });
      } else {
        post.value.likes--;
        uni.showToast({ title: '已取消', icon: 'none' });
      }
    }
  } catch (error) {
    console.error('Like failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const handleFavorite = async () => {
  if (!post.value) return;
  try {
    const res = await favoriteApi.toggle({
      postId: post.value.id
    });
    
    if (res && res.favorited !== undefined) {
      post.value.isFavorited = res.favorited;
      if (res.favorited) {
        uni.showToast({ title: '已收藏', icon: 'success' });
      } else {
        uni.showToast({ title: '已取消收藏', icon: 'none' });
      }
    }
  } catch (error) {
    console.error('Favorite failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const previewImage = (index) => {
  if (post.value && post.value.images) {
    uni.previewImage({
      urls: post.value.images,
      current: index
    });
  }
};

const navigateToProfile = (userId) => {
  console.log('Attempting to navigate to profile for userId:', userId);
  if (!userId) {
    console.warn('Cannot navigate: userId is missing');
    return;
  }
  
  const userInfoStr = uni.getStorageSync('userInfo');
  let currentUserId = null;
  if (userInfoStr) {
    try {
      const userInfoObj = typeof userInfoStr === 'string' ? JSON.parse(userInfoStr) : userInfoStr;
      currentUserId = userInfoObj?.id;
    } catch (e) {
      console.error('Failed to parse userInfo', e);
    }
  }
  console.log('Current logged-in userId:', currentUserId);
  
  // 如果是当前用户，使用 switchTab 跳转到 TabBar 的“我的”页面
  if (userId == currentUserId) {
    console.log('Navigating to own profile (TabBar)');
    uni.switchTab({
      url: '/pages/my/my',
      success: () => console.log('Successfully switched to own profile tab'),
      fail: (err) => console.error('Failed to switch to own profile tab:', err)
    });
  } else {
    // 如果是他人，使用 navigateTo 跳转到专门的他人主页
    const url = `/pages/my/user-profile?userId=${userId}`;
    console.log('Navigating to others profile:', url);
    uni.navigateTo({
      url: url,
      success: () => console.log('Successfully navigated to user-profile'),
      fail: (err) => {
        console.error('Failed to navigate to user-profile:', err);
        uni.showToast({ title: '跳转失败', icon: 'none' });
      }
    });
  }
};

onLoad((options) => {
  if (options.id) {
    postId.value = options.id;
    if (options.targetId) {
        targetId.value = options.targetId;
    }
    loadPostDetail(options.id);
    loadComments(options.id);
  }
});
</script>

<style scoped>
/* Colors */
.container {
  background-color: var(--bg-main);
  min-height: 100vh;
  color: var(--text-main);
  transition: all 0.3s;
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
}

.header {
  position: sticky;
  top: 0;
  z-index: 50;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  width: 40px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  overflow: hidden;
  padding: 4px 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.back-btn {
  padding: 4px;
  margin-left: -4px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
}

.avatar-container {
  position: relative;
}

.author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid var(--border-main);
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.author-name {
  font-size: 14px;
  font-weight: bold;
  color: var(--text-main);
}

.verified-icon {
  color: #f2b90d;
  font-size: 14px;
}

.author-role {
  font-size: 10px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.follow-btn {
  background-color: #f2b90d;
  color: #12110a;
  font-size: 12px;
  font-weight: bold;
  padding: 6px 16px;
  border-radius: 9999px;
  margin: 0;
  line-height: 1.5;
}

.follow-btn.following {
  background-color: var(--bg-secondary);
  color: var(--text-main);
  border: 1px solid var(--border-main);
}

/* Article */
.article {
  padding: 16px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  color: #f2b90d;
  font-size: 12px;
  font-weight: 600;
  background-color: rgba(242, 185, 13, 0.15);
  padding: 4px 10px;
  border-radius: 12px;
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
}

.article-title {
  font-size: 20px;
  font-weight: bold;
  line-height: 1.4;
  margin-bottom: 12px;
  color: var(--text-main);
  display: block;
}

.article-body {
  margin-bottom: 16px;
}

.body-text {
  font-size: 15px;
  color: var(--text-main);
  line-height: 1.6;
}

.read-more {
  color: #f2b90d;
  font-weight: 500;
  font-size: 14px;
  margin-left: 4px;
}

/* Image Grid */
.image-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
}

.grid-item {
  aspect-ratio: 1;
  overflow: hidden;
  position: relative;
}

.grid-image {
  width: 100%;
  height: 100%;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.overlay-text {
  color: #fff;
  font-weight: bold;
  font-size: 18px;
}

/* Stats */
.interaction-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  margin-top: 8px;
  border-top: 1px solid rgba(150, 150, 150, 0.1);
}

.avatar-stack {
  display: flex;
  margin-left: 8px;
}

.stack-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid var(--bg-main);
  margin-left: -8px;
}

.stack-count {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid var(--bg-main);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  font-weight: bold;
  margin-left: -8px;
}

.view-stats {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: -0.05em;
}

.stat-highlight {
  color: var(--text-main);
  font-weight: bold;
}

/* Comments */
.comments-section {
  background-color: var(--bg-main);
  margin-top: 8px;
}

.comments-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.comments-title {
  font-weight: bold;
  font-size: 16px;
  color: var(--text-main);
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.jd-ai-summary-container {
  margin: 0 16px 24px;
  padding: 16px;
  background-color: #fcfaff;
  border-radius: 12px;
  border: 1px solid rgba(138, 43, 226, 0.1);
}

.theme-dark .jd-ai-summary-container {
  background-color: rgba(40, 35, 55, 0.5);
  border-color: rgba(138, 43, 226, 0.2);
}

/* AI Summary Card (Minimal Design) */
.ai-summary-card {
  margin: 12px 16px 24px;
  border-radius: 12px;
  background-color: #f8fafc;
  padding: 16px;
  border: 1px solid #e2e8f0;
}

.theme-dark .ai-summary-card {
  background-color: #1e293b;
  border: 1px solid #334155;
}

.ai-summary-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.ai-avatar {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background-color: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  animation: pulseAvatar 2s infinite alternate;
}

.theme-dark .ai-avatar {
  background-color: #f8fafc;
}

.ai-avatar-icon {
  color: #ffffff;
  width: 14px;
  height: 14px;
  animation: floatIcon 3s ease-in-out infinite;
}

.theme-dark .ai-avatar-icon {
  color: #0f172a;
}

.ai-title-text {
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.theme-dark .ai-title-text {
  background: linear-gradient(90deg, #60a5fa, #c084fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.ai-summary-body {
  margin-bottom: 12px;
}

.ai-content-wrapper {
  position: relative;
}

.ai-text {
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap;
  transition: all 0.3s ease;
  
  /* Cool animated gradient text */
  background: linear-gradient(120deg, #1e293b, #3b82f6, #8b5cf6, #1e293b);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  animation: gradientShine 6s ease infinite, fadeInUp 0.6s ease-out forwards;
}

.theme-dark .ai-text {
  background: linear-gradient(120deg, #f8fafc, #60a5fa, #c084fc, #f8fafc);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.ai-text.collapsed {
  -webkit-line-clamp: 4;
}

@keyframes pulseAvatar {
  0% { transform: scale(1); box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.4); }
  100% { transform: scale(1.05); box-shadow: 0 0 8px 2px rgba(59, 130, 246, 0.1); }
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-1.5px); }
}

@keyframes gradientShine {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.ai-loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}

.skeleton-line {
  height: 14px;
  border-radius: 4px;
  background-color: #e2e8f0;
  position: relative;
  overflow: hidden;
}

.theme-dark .skeleton-line {
  background-color: #334155;
}

.skeleton-line::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  animation: shimmer 1.5s infinite;
}

.theme-dark .skeleton-line::after {
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.05), transparent);
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.ai-summary-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-footer-text {
  font-size: 12px;
  color: #94a3b8;
}

.theme-dark .ai-footer-text {
  color: #64748b;
}

.ai-expand-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
}

.theme-dark .ai-expand-trigger {
  color: #94a3b8;
}

.expand-icon-svg {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.expand-icon-svg.rotated {
  transform: rotate(180deg);
}

.comments-list {
  padding: 0 16px 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 8px;
  border-radius: 12px;
  transition: all 0.5s ease;
}

.comment-item.highlight {
  background-color: rgba(242, 185, 13, 0.15);
  transform: scale(1.02);
}

.comment-item.nested {
  margin-top: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.comment-avatar.small {
  width: 28px;
  height: 28px;
}

.comment-content-wrapper {
  flex: 1;
}

.comment-bubble {
  padding: 12px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  border: 1px solid rgba(150, 150, 150, 0.1);
}

.comment-bubble.semi-transparent {
  padding: 10px 12px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  border: 1px solid rgba(150, 150, 150, 0.05);
}

.comment-user-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.comment-username {
  font-size: 12px;
  font-weight: bold;
  color: var(--text-main);
}

.comment-time {
  font-size: 10px;
  color: var(--text-secondary);
}

.comment-text {
  font-size: 14px;
  color: var(--text-main);
  line-height: 1.5;
}

.highlight-text {
  color: #f2b90d;
}

.reply-label {
  color: #f2b90d;
  font-size: 13px;
  font-weight: 500;
  margin-right: 4px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 8px;
  margin-left: 4px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
  color: var(--text-secondary);
}

.action-item.active {
  color: #f2b90d;
}

.action-text {
  font-size: 10px;
  font-weight: bold;
}

.reply-btn {
  font-size: 10px;
  font-weight: bold;
  color: var(--text-secondary);
}

.nested-replies {
  margin-top: 12px;
  border-left: 2px solid rgba(150, 150, 150, 0.15);
  padding-left: 12px;
}

.bottom-bar-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  /* #ifdef H5 */
  max-width: 500px;
  left: 50% !important;
  transform: translateX(-50%);
  width: 100%;
  /* #endif */
}

.reply-info {
  padding: 8px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-main);
  border-bottom: 1px solid var(--border-main);
}

.reply-to {
  font-size: 12px;
  color: #f2b90d;
  font-weight: 500;
}

.cancel-reply {
  font-size: 16px;
  color: var(--text-secondary);
  padding: 4px;
}

.bottom-bar {
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  padding-bottom: env(safe-area-inset-bottom);
  gap: 12px;
  border-top: 1px solid rgba(150, 150, 150, 0.1);
  background: rgba(var(--bg-nav-bar-rgb), 0.95);
  backdrop-filter: blur(10px);
}

.action-icons-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.ai-btn {
  background: linear-gradient(135deg, rgba(242, 185, 13, 0.2), rgba(242, 185, 13, 0.05));
  border: 1px solid rgba(242, 185, 13, 0.3);
  border-radius: 16px;
  padding: 4px 10px;
}

.action-btn.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.action-text {
  font-size: 13px;
  font-weight: 500;
}

.comment-input {
  flex: 1;
  background-color: rgba(150, 150, 150, 0.1) !important;
  border-radius: 20px;
  padding: 8px 16px;
  color: var(--text-main);
  font-size: 14px;
  height: 36px;
}

.send-btn {
  background-color: #f2b90d;
  color: #12110a;
  font-size: 14px;
  font-weight: bold;
  padding: 0 16px;
  height: 36px;
  line-height: 36px;
  border-radius: 18px;
  margin: 0;
  min-width: 60px;
  text-align: center;
}

.send-btn[disabled] {
  opacity: 0.5;
  background-color: #666;
}

/* Add padding to scroll-view to avoid overlap with bottom bar */
.content-scroll {
  height: calc(100vh - 80px - 60px); /* Adjust based on header and bottom bar */
}

.highlight {
  background-color: rgba(242, 185, 13, 0.15);
  border: 1px solid rgba(242, 185, 13, 0.3);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.clickable-hover {
  opacity: 0.7;
  transform: scale(0.98);
  transition: all 0.1s;
}

.text-hover {
  opacity: 0.6;
  text-decoration: underline;
}
</style>
