<template>
  <view class="container">
    <!-- Custom Navbar -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px', paddingRight: navbarPaddingRight + 'px' }">
      <view class="nav-left" @click="goBack">
        <view class="nav-btn-glass">
          <text class="material-icons back-icon">arrow_back</text>
        </view>
      </view>
      <view class="nav-center">
        <text class="nav-title" :style="{ opacity: scrollTop > 100 ? 1 : 0 }">{{ circleName }}圈</text>
      </view>
      <view class="nav-right">
        <view class="nav-btn-glass" @click="navigateToSearch">
          <text class="material-icons nav-icon">search</text>
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
          <button class="join-btn" :class="{ 'joined': isJoined }" @click="handleJoin">
            {{ isJoined ? '已加入' : '加入' }}
          </button>
        </view>
        
        <view class="info-content">
          <text class="circle-name">{{ circleName }}圈</text>
          <view class="circle-stats">
            <text class="stat-highlight">{{ circleMembers }}</text> <text class="stat-label">成员</text>
            <text class="stat-divider">•</text>
            <text class="stat-highlight">{{ circleOnline }}</text> <text class="stat-label">在线</text>
          </view>
          <text class="circle-desc">{{ circleDesc }}</text>
        </view>
      </view>

      <!-- Tabs -->
      <view class="tabs-container" :class="{ 'sticky-tabs': isSticky }" :style="{ top: (statusBarHeight + 60) + 'px' }">
        <view class="tab-item" :class="{ active: currentTab === 0 }" @click="handleTabChange(0)">
          <text class="tab-text">讨论</text>
          <view class="tab-indicator" v-if="currentTab === 0"></view>
        </view>
        <view class="tab-item" :class="{ active: currentTab === 1 }" @click="handleTabChange(1)">
          <text class="tab-text">赛程</text>
          <view class="tab-indicator" v-if="currentTab === 1"></view>
        </view>
        <view class="tab-item" :class="{ active: currentTab === 2 }" @click="handleTabChange(2)">
          <text class="tab-text">球员</text>
          <view class="tab-indicator" v-if="currentTab === 2"></view>
        </view>
      </view>

      <!-- Content Area -->
      <view class="content-area">
        
        <!-- Tab 0: Posts -->
        <view v-if="currentTab === 0">
          <!-- AI Analysis Card -->
          <view class="ai-card">
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
              <image class="ai-img-half" src="https://images.fotmob.com/image_resources/news/574629810360-7efbbe195018.jpg" mode="aspectFill" @error="(e) => onImgError(e, 'ai-1')"></image>
              <image class="ai-img-half" src="https://images.fotmob.com/image_resources/news/508098682722-e99c43a406b2.jpg" mode="aspectFill" @error="(e) => onImgError(e, 'ai-2')"></image>
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
              <view class="user-info-rich">
                <view class="avatar-container">
                  <image class="user-avatar-rich" :src="post.userAvatar || '/static/default-avatar.png'" mode="aspectFill"></image>
                  <view class="verified-badge">
                    <text class="material-icons" style="font-size: 6px; color: #fff;">bolt</text>
                  </view>
                </view>
                <view class="author-details">
                  <view class="author-name-row">
                    <text class="author-name">{{ post.userName }}</text>
                    <text class="material-icons verified-icon">verified</text>
                  </view>
                  <view class="author-meta-row">
                    <text class="author-role">社区成员</text>
                    <text class="meta-divider">·</text>
                    <text class="post-time-rich">{{ post.time }}</text>
                  </view>
                </view>
              </view>
              
              <button 
                v-if="post.userId !== currentUserId"
                class="follow-btn" 
                :class="{ 'following': post.isFollowing }"
                @click.stop="handleFollow(post)"
              >
                {{ post.isFollowing ? '已关注' : '关注' }}
              </button>
            </view>
            
            <text class="post-title" v-if="post.title && post.title !== post.content">{{ post.title }}</text>
            <text class="post-content-text">{{ post.content }}</text>
            
            <image 
              v-if="post.images && post.images.length === 1" 
              class="post-main-img" 
              :src="post.images[0]" 
              mode="aspectFill"
              @click.stop="previewImage(post.images, 0)"
            ></image>
            
            <view 
              v-else-if="post.images && post.images.length > 1" 
              class="post-images-grid"
              :class="{ 'grid-2': post.images.length === 2 || post.images.length === 4 }"
            >
              <image 
                v-for="(img, idx) in post.images" 
                :key="idx" 
                class="post-grid-img" 
                :src="img" 
                mode="aspectFill"
                @click.stop="previewImage(post.images, idx)"
              ></image>
            </view>
            
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

          <!-- Pagination Buttons -->
          <view class="pagination-box" v-if="!loading && posts.length > 0">
            <button 
              class="page-btn" 
              :disabled="page === 1" 
              @click="handlePrevPage"
            >上一页</text>
            <text class="page-num">第 {{ page }} 页</text>
            <button 
              class="page-btn" 
              :disabled="noMore" 
              @click="handleNextPage"
            >下一页</button>
          </view>
        </view>

        <!-- Tab 1: Schedule -->
        <view v-if="currentTab === 1" class="schedule-list">
          <view v-if="matches.length === 0" class="empty-state">
            <text>暂无赛程信息</text>
          </view>
          <view class="match-card" v-for="match in matches" :key="match.id">
            <view class="match-date" v-if="match.matchTime">
              <text class="material-icons date-icon">event</text>
              {{ formatMatchDate(match.matchTime) }}
            </view>
            <view class="match-teams">
              <view class="team-info home">
                <image class="team-logo-sm" :src="match.homeTeam?.logoUrl || '/static/default-team.png'" mode="aspectFit"></image>
                <text class="team-name">{{ match.homeTeam?.name || '主队' }}</text>
              </view>
              <view class="match-score">
                <text class="score-text" v-if="match.status > 0">{{ match.homeScore }} - {{ match.awayScore }}</text>
                <text class="vs-text" v-else>VS</text>
                <view class="match-status-container">
                  <view class="match-status">{{ match.status === 2 ? '已结束' : (match.status === 1 ? '进行中' : '未开始') }}</view>
                </view>
              </view>
              <view class="team-info away">
                <image class="team-logo-sm" :src="match.awayTeam?.logoUrl || '/static/default-team.png'" mode="aspectFit"></image>
                <text class="team-name">{{ match.awayTeam?.name || '客队' }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Tab 2: Players -->
        <view v-if="currentTab === 2" class="player-list">
          <view v-if="players.length === 0" class="empty-state">
            <text>暂无球员信息</text>
          </view>
          <view class="player-card" v-for="player in players" :key="player.id" @click="navigateToPlayer(player)">
            <image class="player-avatar" :src="player.photoUrl || '/static/default-avatar.png'" mode="aspectFill"></image>
            <view class="player-info">
              <view class="player-name-row">
                <text class="player-number">{{ player.number || player.jerseyNumber }}</text>
                <text class="player-name">{{ player.displayName || player.name }}</text>
                <view v-if="formatStatus(player.status)" :class="['status-badge', getStatusClass(player.status)]">
                  {{ formatStatus(player.status) }}
                </view>
              </view>
              <text class="player-position">{{ player.detailedPos || player.position }}</text>
            </view>
          </view>
        </view>

      </view>
    </scroll-view>

    <!-- FAB -->
    <view class="fab-btn" v-if="currentTab === 0" @click="handlePostClick">
      <text class="material-icons fab-icon">add</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { communityApi, fileApi, postApi, matchApi, playerApi } from '@/api/index';

const circleId = ref(null);
const circleName = ref('');
const circleImage = ref('');
const circleMembers = ref('0');
const memberCount = ref(0);
const circleOnline = ref('0');
const circleDesc = ref('');
const heroImage = ref('');
const currentTab = ref(0);
const posts = ref([]);
const matches = ref([]);
const players = ref([]);
const matchesLoaded = ref(false);
const playersLoaded = ref(false);
const loading = ref(false);
const noMore = ref(false);
const page = ref(1);
const statusBarHeight = ref(20);
const navbarPaddingRight = ref(16); // 默认 16px
const scrollTop = ref(0);
const isSticky = ref(false);
const isJoined = ref(false);
const currentUserId = ref(null);

onMounted(() => {
  const userInfo = uni.getStorageSync('userInfo');
  if (userInfo && userInfo.id) {
    currentUserId.value = userInfo.id;
  }
});

// 格式化数字工具函数
const formatCount = (count) => {
  if (!count) return '0';
  const num = parseInt(count);
  if (isNaN(num)) return count;
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  return num.toString();
};

const loadMatches = async () => {
  if (matchesLoaded.value || !circleId.value) return;
  try {
    const res = await matchApi.getByTeam(circleId.value);
    matches.value = res.map(match => {
      if (match.homeTeam && match.homeTeam.logoUrl) {
        match.homeTeam.logoUrl = fileApi.getFileUrl(match.homeTeam.logoUrl);
      }
      if (match.awayTeam && match.awayTeam.logoUrl) {
        match.awayTeam.logoUrl = fileApi.getFileUrl(match.awayTeam.logoUrl);
      }
      return match;
    });
    matchesLoaded.value = true;
  } catch (e) {
    console.error('Load matches failed:', e);
  }
};

const loadPlayers = async () => {
  if (playersLoaded.value || !circleId.value) return;
  try {
    const res = await playerApi.listByTeamSportApi(circleId.value);
    if (res && res.response) {
      players.value = res.response.map(item => ({
        id: item.player.id,
        name: item.player.name, // Short name
        displayName: item.player.shortName || item.player.name,
        position: item.statistics?.[0]?.games?.position || '未知',
        detailedPos: item.statistics?.[0]?.games?.position || '未知',
        photoUrl: `https://images.fotmob.com/image_resources/playerimages/${item.player.id}.png`,
        jerseyNumber: item.player.jerseyNumber || item.statistics?.[0]?.games?.number || '-',
        status: 'active' // Default status
      }));
    }
    playersLoaded.value = true;
  } catch (e) {
    console.error('Load players failed:', e);
  }
};

const navigateToPlayer = (player) => {
  uni.navigateTo({
    url: `/pages/player-detail/player-detail?id=${player.id}`
  });
};

const navigateToSearch = () => {
  uni.navigateTo({
    url: `/pages/community/circle-search?circleId=${circleId.value}&circleName=${encodeURIComponent(circleName.value)}`
  });
};

const handleTabChange = (index) => {
    currentTab.value = index;
    if (index === 1) {
        loadMatches();
    } else if (index === 2) {
        loadPlayers();
    }
}

const loadMore = () => {
    if (currentTab.value === 0) {
        loadPosts();
    }
}

const checkJoinStatus = async () => {
  if (!circleId.value) return;
  try {
    const res = await communityApi.checkJoinStatus(circleId.value);
    isJoined.value = res;
  } catch (e) {
    console.error('Check join status failed:', e);
  }
};

const handleJoin = async () => {
  if (!circleId.value) return;
  try {
    if (isJoined.value) {
      await communityApi.leaveCircle(circleId.value);
      uni.showToast({ title: '已退出圈子', icon: 'none' });
      isJoined.value = false;
      // 实时更新成员数
      memberCount.value = Math.max(0, memberCount.value - 1);
      circleMembers.value = formatCount(memberCount.value);
    } else {
      await communityApi.joinCircle(circleId.value);
      uni.showToast({ title: '加入成功', icon: 'success' });
      isJoined.value = true;
      // 实时更新成员数
      memberCount.value++;
      circleMembers.value = formatCount(memberCount.value);
    }
  } catch (e) {
    console.error('Toggle join failed:', e);
    uni.showToast({ title: '操作失败', icon: 'none' });
  }
};

const circleConfigs = {
    '利物浦': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/anfield/anfield50.jpg',
      desc: '“YNWA，安菲尔德永远不离不弃。”\n红军魂，逆转命。在这里，没有一个灵魂会感到孤独。'
    },
    '阿森纳': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/emirates_stadium/emirates_stadium18.jpg',
      desc: '“枪手出击，美丽足球的终极朝盛。”\n走过兵工厂的辉煌，历经漫长的蛰伏。北伦敦的红，是青春最热血的底色。'
    },
    '切尔西': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/stamford_bridge/stamford_bridge23.jpg',
      desc: '“Keep the Blue Flag Flying High! 铁血蓝军，永不言败。”\n无论是老男孩的奇迹，还是新时代的重组，切尔西的字典里永远没有退缩。'
    },
    '拜仁慕尼黑': {
      hero: 'http://stadiumdb.com/pictures/stadiums/ger/allianz_arena/allianz_arena132.jpg',
      desc: '“Mia San Mia，德甲巨无霸的绝对统治。”\n稳定、严谨、强大。作为南大王，我们的目标只有一个：冠军。'
    },
    '尤文图斯': {
      hero: 'https://stadiumdb.com/pictures/stadiums/ita/juventus_stadium/juventus_stadium19.jpg',
      desc: '“胜利不是重要，而是唯一的选择。”\n斑马军团，意甲永恒的统治力。不管风雨如何，黑白色的信念永远在巅峰等你。'
    },
    'AC米兰': {
      hero: 'https://stadiumdb.com/pictures/stadiums/ita/san_siro/san_siro58.jpg',
      desc: '“红黑之心，罗森内里的圣西罗梦。”\n拥有欧冠基因的豪门，优雅与铁血并存。只要红黑旗帜飘扬，我们就永远相信复兴。'
    },
    '国际米兰': {
      hero: 'https://stadiumdb.com/pictures/stadiums/ita/san_siro/san_siro58.jpg',
      desc: '“疯狂的蓝黑，一生只爱这一个灵魂。”\n既有“小将”萨内蒂的忠诚，也有三冠王的荣光。这里是内拉组里（Nerazzurri）的精神主场，我们从不缺乏血性。'
    },
    '巴黎圣日耳曼': {
      hero: 'http://stadiumdb.com/pictures/stadiums/fra/parc_des_princes/parc_des_princes23.jpg',
      desc: '“巴黎之光，星光璀璨的法兰西霸主。”\n王子公园球场的喧嚣，全球注视的豪门梦想。'
    },
    '多特蒙德': {
      hero: 'http://stadiumdb.com/pictures/stadiums/ger/westfalenstadion/westfalenstadion61.jpg',
      desc: '“黄黑之墙，世界最狂热的青春风暴。”\n威斯特法伦的咆哮，永远跳动的心脏。这里是足球最纯粹的样子。'
    },
    '那不勒斯': {
      hero: 'https://sp-static-images.s3.amazonaws.com/venue_images/seriea/stadio-diego-armando-maradona/1200x630_auto/stadio-diego-armando-maradona.jpg',
      desc: '“马拉多纳的孩子们，南义大利的狂热信仰。”\n这里不只是足球，是那不勒斯人的生命。马拉多纳球场，永远的热泪盈眶。'
    },
    '勒沃库森': {
      hero: 'http://stadiumdb.com/pictures/stadiums/ger/bayarena/bayarena01.jpg',
      desc: '“不败之师，‘药厂’神话的亲历者。”\n撕掉旧标签，书写新篇章。龙哥带队的奇迹，我们一起见证。'
    },
    '巴塞罗那': {
      hero: 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&q=80&w=1000',
      desc: '“不仅仅是一家俱乐部。萨迷集合，守护拉玛西亚的荣光。”\n我们深爱这里的传控哲学，也深爱诺坎普的每一寸草坪。红蓝魂，永流传。'
    },
    '皇家马德里': {
      hero: 'https://images.unsplash.com/photo-1522778119026-d647f0596c20?q=80&w=1200&auto=format&fit=crop',
      desc: '“Hala Madrid! 银河战舰，欧冠之巅的绝对王者。”\n纯白色的高贵，永恒的争胜欲望。在伯纳乌，我们只谈论传奇。'
    },
    '曼联': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/old_trafford/old_trafford01.jpg',
      desc: '“梦剧场的孩子，红魔精神永不熄灭。”\n弗爵爷的遗产、7号的传承。在这里，我们只聊最纯粹的绝杀与逆转。'
    },
    '曼城': {
      hero: 'http://stadiumdb.com/pictures/stadiums/eng/city_of_manchester_stadium/city_of_manchester_stadium01.jpg',
      desc: '“蓝月当空，定义现代足球的艺术巅峰。”\n战术的极致，传控的盛宴。曼城球迷见证的，是属于这个时代的蓝色王朝。'
    }
};

onLoad((options) => {
  if (options.id) {
    circleId.value = options.id;
    checkJoinStatus();
  }
  if (options.name) {
    circleName.value = decodeURIComponent(options.name);
  }
  if (options.memberCount) {
    memberCount.value = parseInt(options.memberCount);
    circleMembers.value = formatCount(memberCount.value);
  }
  if (options.image) {
    circleImage.value = fileApi.getFileUrl(decodeURIComponent(options.image));
  }
  
  // Get system info for status bar
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;

  // #ifdef MP-WEIXIN
  // 适配小程序胶囊按钮，防止遮挡右上角功能键
  try {
    const menuButton = uni.getMenuButtonBoundingClientRect();
    // 胶囊到右边的距离 + 胶囊宽度 + 额外间距 (8px)
    navbarPaddingRight.value = (systemInfo.screenWidth - menuButton.right) + menuButton.width + 8;
    // 同时也微调导航栏高度，使其与胶囊按钮垂直居中
    const navBarHeight = (menuButton.top - systemInfo.statusBarHeight) * 2 + menuButton.height;
    // 如果需要更精确的控制，可以在这里设置整体高度
  } catch (e) {
    console.error('获取胶囊按钮信息失败:', e);
    navbarPaddingRight.value = 94; // 微信小程序默认胶囊区域宽度约为 94px
  }
  // #endif

  fetchCircleDetail();
  loadPosts();
});

const fetchCircleDetail = async () => {
  if (!circleId.value) return;
  
  try {
    const team = await communityApi.getCircleDetail(circleId.value);
    if (team) {
      circleName.value = team.name;
      circleImage.value = fileApi.getFileUrl(team.logoUrl);
      
      // 更新成员数与在线人数（确保使用详情接口返回的真实实时数据）
      memberCount.value = team.followerCount || 0;
      circleMembers.value = formatCount(memberCount.value);
      circleOnline.value = formatCount(team.onlineCount || 0);
      
      // 使用后端简介或回退到本地配置
      const config = circleConfigs[team.name] || circleConfigs['皇家马德里'];
      circleDesc.value = team.description || team.englishName || config.desc || '暂无简介'; 
      heroImage.value = team.stadiumBgUrl ? fileApi.getFileUrl(team.stadiumBgUrl) : config.hero;
      
      if (!team.description && circleConfigs[team.name]) {
        circleDesc.value = circleConfigs[team.name].desc;
      }
      if (!team.stadiumBgUrl && circleConfigs[team.name]) {
        heroImage.value = circleConfigs[team.name].hero;
      }
    }
  } catch (error) {
    console.error('Failed to fetch circle detail:', error);
    // 接口失败时的回退逻辑
    if (circleName.value && circleConfigs[circleName.value]) {
        const config = circleConfigs[circleName.value];
        heroImage.value = config.hero;
        circleDesc.value = config.desc;
    } else {
        heroImage.value = 'https://images.unsplash.com/photo-1522778119026-d647f0596c20?q=80&w=1200&auto=format&fit=crop';
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
      size: 5 // 一次最多显示 5 个
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
          userId: post.userId,
          title: post.title,
          content: post.content,
          image: postImages.length > 0 ? fileApi.getFileUrl(postImages[0]) : '', 
          images: postImages.length > 0 ? postImages.map(img => fileApi.getFileUrl(img)) : [],
          likes: post.likes || 0,
          isLiked: post.isLiked || false,
          isFollowing: false, // Default to false as list API might not return it
          comments: post.commentCount || 0,
          shares: 0, 
          userName: post.userName || '未知用户', 
          userAvatar: post.userAvatar ? fileApi.getFileUrl(post.userAvatar) : '/static/default-avatar.png', 
          time: new Date(post.createdAt).toLocaleDateString()
        };
      });
      
      if (pageData.records.length < 5) { // 一次最多显示 5 个
        noMore.value = true;
      }
      
      if (page.value === 1) {
        posts.value = newPosts;
      } else {
        // 如果是点击下一页，我们直接替换列表内容而不是追加，这样才符合换页按钮的逻辑
        posts.value = newPosts;
      }
    }
  } catch (error) {
    console.error('Failed to load posts:', error);
    uni.showToast({ title: '加载帖子失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const handleFollow = async (post) => {
  if (!post.userId) return;
  
  try {
    if (post.isFollowing) {
      await postApi.unfollowUser(post.userId);
      post.isFollowing = false;
      uni.showToast({ title: '已取消关注', icon: 'none' });
    } else {
      await postApi.followUser(post.userId);
      post.isFollowing = true;
      uni.showToast({ title: '已关注', icon: 'success' });
    }
  } catch (error) {
    console.error('Follow failed:', error);
    uni.showToast({ title: '操作失败', icon: 'none' });
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

const goBack = () => {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    // 兜底逻辑：如果没有上一页，跳转到首页
    uni.switchTab({
      url: '/pages/index/index'
    });
  }
};

const handlePostClick = () => {
  uni.navigateTo({
    url: `/pages/post/publish?circleId=${circleId.value}&circleName=${encodeURIComponent(circleName.value)}`
  });
};

const formatStatus = (status) => {
  if (!status || status === 'active') return '';
  const map = {
    'injured': '伤缺',
    'suspended': '停赛',
    'loan': '租借',
    'doubtful': '存疑'
  };
  return map[status.toLowerCase()] || status;
};

const getStatusClass = (status) => {
    if (!status) return '';
    return `status-${status.toLowerCase()}`;
}

const formatMatchDate = (timeStr) => {
  if (!timeStr) return '';
  try {
    // Handle iOS date parsing
    let safeTimeStr = timeStr;
    if (typeof timeStr === 'string' && timeStr.includes('-') && !timeStr.includes('T')) {
      safeTimeStr = timeStr.replace(/-/g, '/');
    }
    
    const date = new Date(safeTimeStr);
    if (isNaN(date.getTime())) return timeStr;
    
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hour = date.getHours().toString().padStart(2, '0');
    const minute = date.getMinutes().toString().padStart(2, '0');
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const weekDay = weekDays[date.getDay()];
    return `${month}月${day}日 ${hour}:${minute} ${weekDay}`;
  } catch (e) {
    return timeStr;
  }
};

const isNavigating = ref(false);

const onImageError = (e) => {
  console.error('Logo Image Load Failed:', e);
  // Fallback to local default logo
  circleImage.value = '/static/default-team.png';
};

const onHeroError = (e) => {
  console.error('Hero Image Load Failed:', e);
  // Fallback to local image
  heroImage.value = '/static/teams/real_madrid.jpg';
};

const onImgError = (e, type) => {
  console.error(`Image Load Failed (${type}):`, e);
  if (type.startsWith('ai-')) {
    // 替换为更可靠的占位图
    const target = e.target || e.detail;
    if (type === 'ai-1') {
      e.target.src = 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=20&w=200';
    } else {
      e.target.src = 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=20&w=200';
    }
  }
};

const handlePrevPage = () => {
  if (page.value > 1) {
    page.value--;
    noMore.value = false;
    loadPosts();
    // 滚动到顶部方便阅读
    uni.pageScrollTo({
      scrollTop: 0,
      duration: 300
    });
  }
};

const handleNextPage = () => {
  if (!noMore.value) {
    page.value++;
    loadPosts();
    uni.pageScrollTo({
      scrollTop: 0,
      duration: 300
    });
  }
};

const navigateToPost = (post) => {
  if (isNavigating.value) return;
  isNavigating.value = true;
  
  uni.navigateTo({
    url: `/pages/post/detail?id=${post.id}`,
    complete: () => {
      setTimeout(() => {
        isNavigating.value = false;
      }, 500);
    },
    fail: () => {
      isNavigating.value = false;
    }
  });
};

const previewImage = (urls, current) => {
  if (!urls || urls.length === 0) return;
  uni.previewImage({
    urls: urls,
    current: current || 0,
    fail: (err) => {
        console.error('Preview image failed', err);
    }
  });
};


</script>

<style scoped>
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
  z-index: 9999 !important; /* Force top layer */
  pointer-events: auto;
  background: rgba(28, 26, 17, 0.8); /* glass-effect */
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(45, 42, 29, 0.5); /* border-dark/50 */
}

.nav-left {
  position: relative;
  z-index: 10000;
  cursor: pointer;
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

.join-btn.joined {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
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
  white-space: pre-wrap;
  margin-top: 12px;
}

/* Tabs */
.tabs-container {
  display: flex;
  border-bottom: 1px solid #2d2a1d; /* border-dark */
  background: rgba(28, 26, 17, 0.8);
  backdrop-filter: blur(12px);
  padding: 0 16px;
  position: sticky;
  /* top: 0; handled by inline style */
  z-index: 90;
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
  position: fixed;
  bottom: calc(40px + env(safe-area-inset-bottom)); /* Adjusted position with safe area */
  right: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: #f2b90d;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 10px rgba(242, 185, 13, 0.4);
  z-index: 999;
  transition: transform 0.2s cubic-bezier(0.25, 0.8, 0.5, 1);
}

.fab-btn:active {
  transform: scale(0.95);
}
.fab-icon {
  font-size: 24px;
  color: #12110a;
  font-weight: bold;
}

/* Post Images Grid */
.post-images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 12px;
}

.post-grid-img {
  width: calc(33.33% - 3px);
  height: 220rpx;
  border-radius: 4px;
  background-color: #2d2a1d;
}

.post-images-grid.grid-2 .post-grid-img {
  width: calc(50% - 2px);
  height: 330rpx;
}

.loading-status {
  text-align: center;
  color: #666;
  padding: 20px;
  font-size: 12px;
}

/* Pagination */
.pagination-box {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0 40px;
  gap: 20px;
}

.page-btn {
  background-color: #1c1a11;
  color: #f2b90d;
  border: 1px solid #2d2a1d;
  font-size: 14px;
  padding: 0 20px;
  height: 36px;
  line-height: 34px;
  border-radius: 18px;
  margin: 0;
}

.page-btn[disabled] {
  background-color: #12110a;
  color: #4b5563;
  border-color: #1c1a11;
}

.page-btn:active:not([disabled]) {
  background-color: #2d2a1d;
  transform: scale(0.95);
}

.page-num {
  font-size: 14px;
  color: #9ca3af;
  font-weight: 500;
}

/* Match Card */
.match-card {
  background-color: #1c1a11;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #2d2a1d;
}

.match-date {
  font-size: 13px;
  color: #f2b90d;
  margin-bottom: 12px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  background-color: rgba(242, 185, 13, 0.1);
  padding: 4px 12px;
  border-radius: 100px;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

.date-icon {
  font-size: 14px;
  margin-right: 4px;
  color: #f2b90d;
}

.match-teams {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.team-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 30%;
}

.team-logo-sm {
  width: 48px;
  height: 48px;
  margin-bottom: 8px;
}

.team-name {
  font-size: 14px;
  color: #fff;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.match-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 40%;
}

.score-text {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  line-height: 1;
  margin-bottom: 8px;
}

.vs-text {
  font-size: 20px;
  font-weight: bold;
  color: #6b7280;
  line-height: 1;
  margin-bottom: 8px;
}

.match-status-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

.match-status {
  text-align: center;
  font-size: 11px;
  color: #f2b90d;
  background-color: rgba(242, 185, 13, 0.1);
  padding: 2px 10px;
  border-radius: 100px;
  font-weight: 500;
}

/* Player Card */
.player-list, .schedule-list {
    padding-bottom: 20px;
}

.player-card {
  display: flex;
  align-items: center;
  background-color: #1c1a11;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid #2d2a1d;
}

.player-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
  background-color: #2d2a1d;
}

.player-info {
  flex: 1;
}

.player-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.player-number {
  font-size: 16px;
  font-weight: bold;
  color: #f2b90d;
  margin-right: 8px;
  font-family: 'DIN Alternate', sans-serif;
}

.player-name {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
}

.status-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 6px;
  color: #fff;
  background-color: #999;
}

.status-injured {
  background-color: #ff4d4f;
}

.status-suspended {
  background-color: #faad14;
}

.status-doubtful {
  background-color: #faad14;
}

.status-loan {
  background-color: #1890ff;
}

.player-position {
  font-size: 12px;
  color: #9ca3af;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

/* Rich User Header */
.user-info-rich {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.avatar-container {
  position: relative;
}

.user-avatar-rich {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(242, 185, 13, 0.2);
}

.verified-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  background-color: #f2b90d;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #1c1a11; /* Match card background */
  display: flex;
  align-items: center;
  justify-content: center;
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
  color: #fff;
}

.verified-icon {
  color: #f2b90d;
  font-size: 14px;
}

.author-meta-row {
  display: flex;
  align-items: center;
}

.author-role {
  font-size: 10px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.meta-divider {
  margin: 0 4px;
  color: #64748b;
  font-size: 10px;
}

.post-time-rich {
  font-size: 10px;
  color: #64748b;
}

.follow-btn {
  background-color: #f2b90d;
  color: #000;
  font-size: 12px;
  font-weight: bold;
  padding: 0 12px;
  height: 24px;
  line-height: 24px;
  border-radius: 9999px;
  margin: 0;
}

.follow-btn.following {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}
</style>