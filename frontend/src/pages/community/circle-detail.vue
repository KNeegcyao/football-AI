<template>
  <view class="container">
    <!-- Custom Navbar -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <view class="nav-btn-glass">
          <text class="material-icons back-icon">arrow_back</text>
        </view>
      </view>
      <view class="nav-center">
        <text class="nav-title" :style="{ opacity: scrollTop > 100 ? 1 : 0 }">{{ circleName }}圈</text>
      </view>
      <view class="nav-right">
        <view class="nav-btn-glass">
          <text class="material-icons nav-icon">search</text>
        </view>
        <view class="nav-btn-glass">
          <text class="material-icons nav-icon">more_horiz</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="content-scroll" @scroll="onScroll" @scrolltolower="loadMore">
      <!-- Hero Section -->
      <view class="hero-section">
        <image class="hero-bg" :src="heroImage" mode="aspectFill" @error="onHeroError"></image>
        <view class="hero-gradient"></view>
      </view>

      <!-- Circle Info -->
      <view class="circle-info-section">
        <view class="info-header">
          <view class="logo-container">
            <image 
              class="circle-logo" 
              :src="circleImage || '/static/default-team.png'" 
              mode="aspectFill"
              @error="onImageError"
            ></image>
          </view>
          <button class="join-btn">加入</button>
        </view>
        
        <view class="info-content">
          <text class="circle-name">{{ circleName }}圈</text>
          <view class="circle-stats">
            <text class="stat-highlight">1.2M</text> <text class="stat-label">成员</text>
            <text class="stat-divider">•</text>
            <text class="stat-highlight">5,240</text> <text class="stat-label">在线</text>
          </view>
          <text class="circle-desc">{{ circleDesc }}</text>
        </view>
      </view>

      <!-- Tabs -->
      <view class="tabs-container" :class="{ 'sticky-tabs': isSticky }">
        <view class="tab-item" :class="{ active: currentTab === 0 }" @click="currentTab = 0">
          <text class="tab-text">讨论</text>
          <view class="tab-indicator" v-if="currentTab === 0"></view>
        </view>
        <view class="tab-item" :class="{ active: currentTab === 1 }" @click="currentTab = 1">
          <text class="tab-text">赛程</text>
          <view class="tab-indicator" v-if="currentTab === 1"></view>
        </view>
        <view class="tab-item" :class="{ active: currentTab === 2 }" @click="currentTab = 2">
          <text class="tab-text">球员</text>
          <view class="tab-indicator" v-if="currentTab === 2"></view>
        </view>
        <view class="tab-item" :class="{ active: currentTab === 3 }" @click="currentTab = 3">
          <text class="tab-text">相册</text>
          <view class="tab-indicator" v-if="currentTab === 3"></view>
        </view>
      </view>

      <!-- Content Area -->
      <view class="content-area">
        
        <!-- AI Analysis Card -->
        <view class="ai-card" v-if="currentTab === 0">
          <view class="ai-header">
            <view class="ai-badge">
              <view class="ai-icon-bg">
                <text class="material-icons ai-icon">smart_toy</text>
              </view>
              <view class="ai-title-group">
                <text class="ai-name">PitchPulse AI</text>
                <view class="ai-tag">AI 分析</view>
              </view>
            </view>
            <text class="post-time">刚刚</text>
          </view>
          
          <text class="post-title">【战术分析】欧冠四分之一决赛首回合展望</text>
          <text class="post-content">基于维尼修斯最近的跑位数据，他在左路的突破效率提升了15%。今晚对阵曼城的关键在于能否利用罗德里戈的交叉换位拉开空间...</text>
          
          <view class="ai-images-grid">
            <image class="ai-img-half" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCEO7GwNM9dg0djapjxAuWJ1nFgCkIfgn1ufmunk1_nRl99l7EJRbCDg8VJBLGpfu3ZavpB755dqbeIOIGHL9TtiuioRiy2RoCmkE3ye5vX8wVkD-mAkaGxMdmc9Eqjxxdp_aVcGkwsXdI9EibI2xjUhLZ0TenrzdAa_l8Ho45auGaVTAQ0rYe3QY-Pt1dZb2wzbJd5k9oVtgDVj8NqTgXcvnhHwtdVY1zLIv5hwwvnCMwiyym6KpTVw_65I303HK2El2EXnJBnQTRi" mode="aspectFill"></image>
            <image class="ai-img-half" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCcs8hChqz6NtBT9EYHf8647M6hV4uLY6o4kBnPm96pX40o20WDok-X2gQzILgbL2b6_-wxHTPGlgyDDNiyFR6lacvKhmuxDfmS5S6aAXobnrkCRpfNKhia6-gIvYSgVLk1EeqzHI6DzV2tHCM7NSynNcn20ss2VLEkN7nZ8wP3Qi8UZq9qUEfPCWQbrNlWfkVKhjM4mng3J1zZ8hDvDzHzoYtaHU2dasMsjswycEPgGHwYQPQZTCvNJ8P7XL5jOYaMvKYCvkL7KhW5" mode="aspectFill"></image>
          </view>
          
          <view class="post-footer">
            <view class="interaction-item">
              <text class="material-icons footer-icon">favorite</text>
              <text>1.2k</text>
            </view>
            <view class="interaction-item">
              <text class="material-icons footer-icon">chat_bubble</text>
              <text>248</text>
            </view>
            <view class="interaction-item">
              <text class="material-icons footer-icon">share</text>
              <text>56</text>
            </view>
          </view>
        </view>

        <!-- User Posts -->
        <view class="post-card" v-for="(post, index) in posts" :key="post.id" @click="navigateToPost(post)">
          <view class="post-header">
            <view class="user-info">
              <image class="user-avatar" :src="post.userAvatar || '/static/default-avatar.png'" mode="aspectFill"></image>
              <text class="user-name">{{ post.userName }}</text>
            </view>
            <text class="post-time">{{ post.time }}</text>
          </view>
          
          <text class="post-content-text">{{ post.content }}</text>
          
          <image v-if="post.image" class="post-main-img" :src="post.image" mode="aspectFill"></image>
          
          <view class="post-footer">
            <view class="interaction-item" @click.stop="handleLike(post)">
              <text class="material-icons footer-icon" :style="{ color: post.isLiked ? '#f2b90d' : '#9ca3af' }">
                {{ post.isLiked ? 'favorite' : 'favorite_border' }}
              </text>
              <text :style="{ color: post.isLiked ? '#f2b90d' : '#9ca3af' }">{{ post.likes }}</text>
            </view>
            <view class="interaction-item">
              <text class="material-icons footer-icon">chat_bubble_outline</text>
              <text>{{ post.comments }}</text>
            </view>
            <view class="interaction-item">
              <text class="material-icons footer-icon">share</text>
              <text>{{ post.shares }}</text>
            </view>
          </view>
        </view>

        <!-- Load More / No More -->
        <view class="loading-status">
          <text v-if="loading">加载中...</text>
          <text v-else-if="noMore">没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- FAB -->
    <view class="fab-btn" @click="handlePostClick">
      <text class="material-icons fab-icon">add</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { communityApi, fileApi, postApi } from '@/api/index';

const circleId = ref(null);
const circleName = ref('');
const circleImage = ref('');
const circleMembers = ref('1.2M'); // Placeholder
const circleDesc = ref('');
const heroImage = ref('');
const currentTab = ref(0);
const posts = ref([]);
const loading = ref(false);
const noMore = ref(false);
const page = ref(1);
const statusBarHeight = ref(20);
const scrollTop = ref(0);
const isSticky = ref(false);

const teamData = {
    '利物浦': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/anfield/anfield50.jpg',
      desc: '你永远不会独行！安菲尔德的呐喊。YNWA!'
    },
    '阿森纳': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/emirates_stadium/emirates_stadium18.jpg',
      desc: '兵工厂的枪声！COYG!'
    },
    '切尔西': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/stamford_bridge/stamford_bridge23.jpg',
      desc: '蓝军出击！斯坦福桥的荣耀。KTBFFH!'
    },
    '拜仁慕尼黑': {
      hero: 'http://stadiumdb.com/pictures/stadiums/ger/allianz_arena/allianz_arena132.jpg',
      desc: 'Mia San Mia！南部之星的荣耀。分享拜仁最新战况。'
    },
    '尤文图斯': {
      hero: 'https://stadiumdb.com/pictures/stadiums/ita/juventus_stadium/juventus_stadium19.jpg',
      desc: '斑马军团！Fino Alla Fine!'
    },
    'AC米兰': {
      hero: 'https://stadiumdb.com/pictures/stadiums/ita/san_siro/san_siro58.jpg',
      desc: '红黑军团！Forza Milan!'
    },
    '国际米兰': {
      hero: 'https://stadiumdb.com/pictures/stadiums/ita/san_siro/san_siro58.jpg',
      desc: '蓝黑军团！Pazza Inter!'
    },
    '巴黎圣日耳曼': {
      hero: 'http://stadiumdb.com/pictures/stadiums/fra/parc_des_princes/parc_des_princes23.jpg',
      desc: '这里是巴黎！Ici c\'est Paris!'
    },
    '多特蒙德': {
      hero: 'http://stadiumdb.com/pictures/stadiums/ger/westfalenstadion/westfalenstadion61.jpg',
      desc: '黄黑色的海洋！Echte Liebe!'
    },
    '那不勒斯': {
      hero: 'https://sp-static-images.s3.amazonaws.com/venue_images/seriea/stadio-diego-armando-maradona/1200x630_auto/stadio-diego-armando-maradona.jpg',
      desc: '那不勒斯的骄傲！Forza Napoli!'
    },
    '勒沃库森': {
      hero: 'http://stadiumdb.com/pictures/stadiums/ger/bayarena/bayarena01.jpg',
      desc: '药厂！Bayer 04 Leverkusen!'
    },
    '马德里竞技': {
      hero: 'http://stadiumdb.com/pictures/stadiums/esp/wanda_metropolitano/wanda_metropolitano12.jpg',
      desc: '床单军团！Aúpa Atleti!'
    },
    '热刺': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/tottenham_hotspur_stadium/tottenham_hotspur_stadium01.jpg',
      desc: 'To Dare Is To Do! COYS!'
    }
};

onLoad((options) => {
  if (options.id) {
    circleId.value = options.id;
  }
  if (options.name) {
    circleName.value = decodeURIComponent(options.name);
  }
  if (options.image) {
    circleImage.value = decodeURIComponent(options.image);
  }
  
  // Get system info for status bar
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;

  fetchCircleDetail();
  loadPosts();
});

const fetchCircleDetail = async () => {
  if (!circleId.value) return;
  
  try {
    const team = await communityApi.getCircleDetail(circleId.value);
    if (team) {
      circleName.value = team.name;
      circleImage.value = team.logoUrl;
      // Use description or fallback
      circleDesc.value = team.description || team.englishName || '暂无简介'; 
      // Use stadiumBgUrl or fallback
      heroImage.value = team.stadiumBgUrl || team.logoUrl;
      
      // If no description, maybe use teamData fallback if name matches
      if (!team.description && teamData[team.name]) {
        circleDesc.value = teamData[team.name].desc;
      }
      if (!team.stadiumBgUrl && teamData[team.name]) {
        heroImage.value = teamData[team.name].hero;
      }
    }
  } catch (error) {
    console.error('Failed to fetch circle detail:', error);
    // Fallback to teamData if API fails or if we just want to ensure we have data
    if (circleName.value && teamData[circleName.value]) {
        const team = teamData[circleName.value];
        heroImage.value = team.hero;
        circleDesc.value = team.desc;
    } else {
        // Ultimate fallback
        heroImage.value = '/static/teams/real_madrid.jpg';
        if (!circleDesc.value) {
            circleDesc.value = `这里是${circleName.value}的集结地！分享${circleName.value}最新动态、转会传闻及比赛讨论。`;
        }
    }
  }
};

const loadPosts = async () => {
  if (loading.value || noMore.value) return;
  
  loading.value = true;
  try {
    const pageData = await communityApi.getCirclePosts(circleName.value, {
      page: page.value,
      size: 10
    });
    
    if (pageData && pageData.records) {
      const newPosts = pageData.records.map(post => {
        // Mock user info since backend doesn't provide it yet
        // Update: Now backend provides it
        let postImages = [];
        try {
          if (post.images) {
            // Check if it's already an array (sometimes API returns object/array directly)
            if (Array.isArray(post.images)) {
                postImages = post.images;
            } else if (typeof post.images === 'string') {
                if (post.images.startsWith('[')) {
                    postImages = JSON.parse(post.images);
                } else {
                    postImages = [post.images];
                }
            }
          }
        } catch (e) {
          console.error('Error parsing post images:', e);
        }

        return {
          id: post.id,
          content: post.content,
          image: postImages.length > 0 ? fileApi.getFileUrl(postImages[0]) : '', 
          likes: post.likes || 0,
          isLiked: post.isLiked || false,
          comments: post.commentCount || 0,
          shares: 0, 
          userName: post.userName || '未知用户', 
          userAvatar: post.userAvatar ? fileApi.getFileUrl(post.userAvatar) : '/static/default-avatar.png', 
          time: new Date(post.createdAt).toLocaleDateString()
        };
      });
      
      if (pageData.records.length < 10) {
        noMore.value = true;
      }
      
      if (page.value === 1) {
        posts.value = newPosts;
      } else {
        posts.value = [...posts.value, ...newPosts];
      }
      page.value++;
    }
  } catch (error) {
    console.error('Failed to load posts:', error);
    uni.showToast({ title: '加载帖子失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const handleLike = async (post) => {
  try {
    const res = await postApi.like({
      targetId: post.id,
      targetType: 1
    });
    
    if (res && res.liked !== undefined) {
      post.isLiked = res.liked;
      if (res.liked) {
        post.likes++;
        uni.showToast({ title: '已点赞', icon: 'none' });
      } else {
        post.likes--;
        uni.showToast({ title: '已取消', icon: 'none' });
      }
    }
  } catch (error) {
    console.error('Like failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const onScroll = (e) => {
  scrollTop.value = e.detail.scrollTop;
  isSticky.value = e.detail.scrollTop > 200;
};

const onHeroError = (e) => {
  console.error('Hero Image Load Failed:', e);
  // Fallback to local image
  heroImage.value = '/static/teams/real_madrid.jpg';
};

const onImageError = (e) => {
  console.error('Logo Image Load Failed:', e);
  // Fallback to local default logo
  circleImage.value = '/static/default-team.png';
};

const goBack = () => uni.navigateBack();

const handlePostClick = () => {
  uni.navigateTo({
    url: `/pages/post/publish?circleId=${circleId.value}&circleName=${encodeURIComponent(circleName.value)}`
  });
};

const navigateToPost = (post) => {
  uni.navigateTo({
    url: `/pages/post/detail?id=${post.id}`
  });
};

const loadMore = () => {
  loadPosts();
};
</script>

<style>
/* Color Palette */
:root {
  --primary: #f2b90d;
  --background-light: #f8f8f5;
  --background-dark: #12110a;
  --surface-dark: #1c1a11;
  --border-dark: #2d2a1d;
  --text-gray: #9ca3af;
}

.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #12110a; /* background-dark */
  color: #fff;
  position: relative;
}

/* Navbar */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  /* #ifdef H5 */
  max-width: 500px;
  margin: 0 auto;
  /* #endif */
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  box-sizing: border-box;
  z-index: 1000;
  background: rgba(28, 26, 17, 0.8); /* glass-effect */
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(45, 42, 29, 0.5); /* border-dark/50 */
}

.nav-left, .nav-right {
  display: flex;
  align-items: center;
}

.nav-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  transition: opacity 0.3s;
  white-space: nowrap;
}

.nav-btn-glass {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 8px;
}

.nav-left .nav-btn-glass {
  margin-left: 0;
}

.back-icon, .nav-icon {
  color: #fff;
  font-size: 20px;
}

.content-scroll {
  flex: 1;
  height: 100%;
}

/* Hero Section */
.hero-section {
  position: relative;
  height: 240px; /* Reduced height slightly */
  width: 100%;
  overflow: hidden;
}

.hero-bg {
  width: 100%;
  height: 100%;
}

.hero-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0) 0%, rgba(18,17,10,0.6) 60%, #12110a 100%);
}

/* Circle Info Section */
.circle-info-section {
  position: relative;
  margin-top: -32px; /* Adjusted overlap */
  padding: 0 20px 20px 20px;
  z-index: 10;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;
}

.logo-container {
  width: 88px; /* Slightly smaller */
  height: 88px;
  border-radius: 16px;
  background-color: #1c1a11;
  border: 4px solid #12110a;
  padding: 4px; /* Less padding */
  box-sizing: border-box;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.2);
}

.circle-logo {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.join-btn {
  background-color: #f2b90d; /* primary */
  color: #12110a; /* background-dark */
  font-size: 14px;
  font-weight: bold;
  padding: 0 32px;
  height: 44px;
  line-height: 44px;
  border-radius: 12px;
  margin-bottom: 4px;
  box-shadow: 0 4px 6px -1px rgba(242, 185, 13, 0.2);
  transition: transform 0.1s;
  margin-left: auto;
  margin-right: 0;
}

        .join-btn:active {
          transform: scale(0.95);
        }

        .circle-name {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 4px;
}

.circle-stats {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.stat-highlight {
  color: #fff;
  font-weight: 500;
  margin-right: 4px;
}

.stat-label {
  color: #9ca3af; /* text-gray-400 */
}

.stat-divider {
  margin: 0 8px;
  color: #9ca3af;
}

.circle-desc {
  font-size: 14px;
  color: #d1d5db; /* text-gray-300 */
  line-height: 1.625;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* Tabs */
.tabs-container {
  display: flex;
  border-bottom: 1px solid #2d2a1d; /* border-dark */
  background: rgba(28, 26, 17, 0.8);
  backdrop-filter: blur(12px);
  padding: 0 16px;
  position: sticky;
  top: 0; /* sticky top-[64px] equivalent */
  z-index: 40;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 0;
  position: relative;
}

.tab-text {
  font-size: 14px;
  font-weight: bold;
  color: #9ca3af;
}

.tab-item.active .tab-text {
  color: #f2b90d; /* primary */
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  width: 32px;
  height: 2px;
  background-color: #f2b90d;
}

/* Content Area */
.content-area {
  padding: 16px;
  padding-bottom: 96px; /* space for bottom nav/fab */
  background-color: #12110a;
}

/* Cards Common */
.ai-card, .post-card {
  background-color: #1c1a11; /* surface-dark */
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

/* AI Card */
.ai-card {
  border: 1px solid rgba(242, 185, 13, 0.2);
  box-shadow: 0 0 8px rgba(242, 185, 13, 0.3); /* ai-glow */
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.ai-badge {
  display: flex;
  align-items: center;
}

.ai-icon-bg {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(242, 185, 13, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 8px;
}

.ai-icon {
  font-size: 14px;
  color: #f2b90d;
}

.ai-title-group {
  display: flex;
  align-items: center;
}

.ai-name {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
  margin-right: 8px;
}

.ai-tag {
  font-size: 10px;
  color: #f2b90d;
  background-color: rgba(242, 185, 13, 0.2);
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid rgba(242, 185, 13, 0.3);
}

.post-time {
  font-size: 12px;
  color: #6b7280; /* text-gray-500 */
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 8px;
}

.post-content, .post-content-text {
  font-size: 14px;
  color: #d1d5db;
  line-height: 1.625;
  display: block;
  margin-bottom: 16px;
}

.ai-images-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 16px;
}

.ai-img-half {
  width: 100%;
  height: 128px;
  border-radius: 8px;
  background-color: #2d2a1d;
}

/* User Post Card */
.post-card {
  border: 1px solid #2d2a1d;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.user-name {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
}

.post-main-img {
  width: 100%;
  height: 192px; /* h-48 */
  border-radius: 8px;
  margin-bottom: 16px;
  background-color: #2d2a1d;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 24px;
}

.interaction-item {
  display: flex;
  align-items: center;
  color: #9ca3af;
  font-size: 12px;
}

.footer-icon {
  font-size: 14px;
  margin-right: 6px;
}



/* Floating Action Button */
.fab-btn {
  position: absolute;
  bottom: 40px; /* Adjusted position */
  right: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #f2b90d;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 10px rgba(242, 185, 13, 0.4);
  z-index: 50;
}

.fab-icon {
  font-size: 24px;
  color: #12110a;
  font-weight: bold;
}

.loading-status {
  text-align: center;
  color: #666;
  padding: 20px;
  font-size: 12px;
}
</style>