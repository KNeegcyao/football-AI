<template>
  <view class="container">
    <view v-if="loading" class="loading-state">
      <text class="loading-text">加载中...</text>
    </view>
    
    <scroll-view v-else scroll-y class="content-scroll">
      <!-- Header Navigation -->
      <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="nav-btn-glass" @click="goBack">
          <text class="material-icons nav-icon">arrow_back_ios_new</text>
        </view>
        <view class="nav-actions">
          <view class="nav-btn-glass">
            <text class="material-icons nav-icon">share</text>
          </view>
          <view class="nav-btn-glass">
            <text class="material-icons nav-icon active">favorite</text>
          </view>
        </view>
      </view>

      <!-- Hero Section -->
      <view class="hero-section">
        <image class="hero-bg" :src="player.photo || '/static/default-player-bg.jpg'" mode="aspectFill"></image>
        <view class="hero-gradient"></view>
        <view class="hero-content">
          <view class="player-number-row">
            <text class="player-number">{{ player.number || '--' }}</text>
            <text class="player-pos">{{ player.position || 'Player' }}</text>
          </view>
          <text class="player-name-cn">{{ player.name || '未知球员' }}</text>
          <text class="player-name-en">{{ player.firstname }} {{ player.lastname }}</text>
        </view>
      </view>

      <!-- Summary Floating Card -->
      <view class="summary-card glass-card">
        <view class="team-info">
          <view class="team-logo-wrapper">
            <image class="team-logo" :src="player.teamLogo || '/static/default-team.png'" mode="aspectFit"></image>
          </view>
          <view class="team-text">
            <text class="label">所属球队</text>
            <text class="value">{{ player.teamName || '未知球队' }}</text>
          </view>
        </view>
        <view class="market-value">
        <text class="value">{{ player.marketValue || '--' }}</text>
        <text class="label">市场身价</text>
      </view>
      </view>

      <!-- Quick Info Strips -->
      <view class="quick-info-grid">
        <view class="info-box">
          <text class="material-icons info-icon">straighten</text>
          <text class="info-value">{{ player.height || '--' }}</text>
          <text class="info-label">身高</text>
        </view>
        <view class="info-box">
          <text class="material-icons info-icon">monitor_weight</text>
          <text class="info-value">{{ player.weight || '--' }}</text>
          <text class="info-label">体重</text>
        </view>
        <view class="info-box">
          <text class="material-icons info-icon">cake</text>
          <text class="info-value">{{ player.age || '--' }}岁</text>
          <text class="info-label">年龄</text>
        </view>
      </view>

      <!-- Personal Info Section -->
      <view class="section">
        <view class="section-header">
          <view class="section-indicator"></view>
          <text class="section-title">个人资料</text>
        </view>
        <view class="info-list glass-card">
          <view class="info-item">
            <text class="item-label">生日</text>
            <text class="item-value">{{ player.birthDate || '--' }}</text>
          </view>
          <view class="info-item">
            <text class="item-label">国籍</text>
            <view class="item-value-row">
              <text class="item-value">{{ player.nationality || '--' }}</text>
            </view>
          </view>
          <view class="info-item">
            <text class="item-label">惯用脚</text>
            <text class="item-value">{{ player.preferredFoot || '--' }}</text>
          </view>
          <view class="info-item">
            <text class="item-label">合同到期</text>
            <text class="item-value">{{ player.contractUntil || '--' }}</text>
          </view>
        </view>
      </view>

      <!-- Season Stats Section -->
      <view class="section">
        <view class="section-header">
          <view class="section-indicator"></view>
          <text class="section-title">本赛季数据</text>
        </view>
        <view class="stats-grid glass-card">
          <view class="stat-item">
            <text class="stat-num">{{ player.appearences || 0 }}</text>
            <text class="stat-label">出场</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ player.goals || 0 }}</text>
            <text class="stat-label">进球</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ player.assists || 0 }}</text>
            <text class="stat-label">助攻</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ player.rating || '--' }}</text>
            <text class="stat-label">评分</text>
          </view>
        </view>
      </view>

    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { playerApi, fileApi } from '@/api/index';

const statusBarHeight = ref(20);
const loading = ref(true);
const player = ref({});

onLoad(async (options) => {
  const sysInfo = uni.getSystemInfoSync();
  statusBarHeight.value = sysInfo.statusBarHeight || 20;

  if (options.id) {
    await loadPlayerDetail(options.id);
  }
});

const goBack = () => {
  uni.navigateBack();
};

const loadPlayerDetail = async (id) => {
  try {
    loading.value = true;
    const res = await playerApi.getSportApiDetail(id);
    
    // 解析 SportAPI 数据
    // 假设 res 是 JsonNode，结构如 { response: [ { player: {}, statistics: [] } ] }
    // 或者 mock 数据直接是 { player: {} }
    
    let p = null;
    let stats = {};
    
    if (res.response && res.response.length > 0) {
      const data = res.response[0];
      p = data.player;
      stats = data.statistics && data.statistics.length > 0 ? data.statistics[0] : {};
    } else if (res.data && res.data.player) {
      p = res.data.player;
      // mock stats
      stats = { 
        team: p.team,
        games: {
            position: p.position,
            number: p.jerseyNumber,
            appearences: 0,
            rating: 0
        },
        goals: { total: 0, assists: 0 }
      };
    } else if (res.player) {
       // Root level player (another mock format)
       p = res.player;
       
       if (res.statistics && res.statistics.length > 0) {
          stats = res.statistics[0];
       } else {
          stats = {
            team: p.team,
            games: {
                position: p.position,
                number: p.jerseyNumber,
                appearences: 0,
                rating: 0
            },
            goals: { total: 0, assists: 0 }
          };
       }
    }
    
    if (p) {
      player.value = {
        id: p.id,
        name: p.name,
        firstname: p.firstname || p.name.split(' ')[0],
        lastname: p.lastname || p.name.split(' ').slice(1).join(' '),
        age: p.age || (p.dateOfBirthTimestamp ? new Date().getFullYear() - new Date(p.dateOfBirthTimestamp * 1000).getFullYear() : '--'),
        birthDate: p.birthDate || p.birth?.date || (p.dateOfBirthTimestamp ? new Date(p.dateOfBirthTimestamp * 1000).toLocaleDateString() : '--'),
        birthPlace: p.birthPlace || p.birth?.place || '--',
        nationality: p.nationality || p.country?.name || '--',
        height: p.height || '--',
        weight: p.weight || '--',
        preferredFoot: p.preferredFoot || '--',
        contractUntil: formatDate(p.contractUntil || p.contractUntilTimestamp),
        status: p.status || 'Active',
        marketValue: p.proposedMarketValue ? ('€' + (p.proposedMarketValue / 1000000).toFixed(1) + 'M') : '--',
        photo: p.photo || `https://images.fotmob.com/image_resources/playerimages/${p.id}.png`,
        teamName: formatTeamName(stats.team?.name || p.teamName || p.team?.name),
        teamLogo: fileApi.getFileUrl(stats.team?.logo || p.teamLogo) || '/static/default-team.png',
        position: stats.games?.position || p.position || '未知',
        number: stats.games?.number || p.jerseyNumber || '--',
        appearences: stats.games?.appearences || 0,
        goals: stats.goals?.total || 0,
        assists: stats.goals?.assists || 0,
        rating: stats.games?.rating || '0.0'
      };
    }
  } catch (e) {
    console.error('Load player detail failed:', e);
    uni.showToast({ title: '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const formatDate = (val) => {
  if (!val) return '--';
  // If it's a timestamp (number)
  if (typeof val === 'number') {
      const date = new Date(val * 1000);
      return formatDateObj(date);
  }
  // If it's a string
  const date = new Date(val);
  if (isNaN(date.getTime())) return val;
  return formatDateObj(date);
};

const formatDateObj = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

const formatTeamName = (name) => {
    if (!name || name === 'null' || name === 'undefined') return '未知球队';
    return name;
};
</script>

<style lang="scss">
/* 颜色变量 */
$primary: #f2b90d;
$bg-dark: #121212;
$surface-dark: #1e1e1e;
$text-white: #ffffff;
$text-gray: #9ca3af;

.container {
  min-height: 100vh;
  background-color: $bg-dark;
  color: $text-white;
  display: flex;
  flex-direction: column;
}

.loading-state {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  
  .loading-text {
    color: $text-gray;
  }
}

.content-scroll {
  flex: 1;
  height: 100vh;
}

/* Navbar */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-btn-glass {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(18, 18, 18, 0.4);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
}

.nav-actions {
  display: flex;
  gap: 8px;
}

.nav-icon {
  color: #fff;
  font-size: 20px;
  
  &.active {
    color: $primary;
  }
}

/* Hero Section */
.hero-section {
  position: relative;
  height: 450px;
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
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(18, 18, 18, 0) 0%, rgba(18, 18, 18, 0.8) 70%, rgba(18, 18, 18, 1) 100%);
}

.hero-content {
  position: absolute;
  bottom: 64px;
  left: 0;
  right: 0;
  padding: 0 24px;
}

.player-number-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.player-number {
  color: $primary;
  font-size: 48px;
  font-weight: bold;
  line-height: 1;
}

.player-pos {
  color: rgba(255, 255, 255, 0.6);
  font-size: 20px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.player-name-cn {
  display: block;
  color: #fff;
  font-size: 48px;
  font-weight: bold;
  line-height: 1.1;
  letter-spacing: -1px;
}

.player-name-en {
  display: block;
  color: $primary;
  font-size: 16px;
  font-weight: 500;
  margin-top: 4px;
}

/* Glass Card Common */
.glass-card {
  background: rgba(30, 30, 30, 0.8);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(242, 185, 13, 0.1);
  border-radius: 16px;
}

/* Summary Card */
.summary-card {
  margin: -48px 16px 0;
  padding: 20px;
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.team-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
  padding-right: 10px;
}

.team-logo-wrapper {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

.team-logo {
  width: 100%;
  height: 100%;
}

.team-text {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.team-text .value {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.label {
  color: $text-gray;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  margin-bottom: 2px;
}

.value {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

.market-value {
  text-align: right;
  flex-shrink: 0;
  
  .value {
    color: $primary;
    font-size: 20px;
    display: block;
  }
}

/* Quick Info Grid */
.quick-info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 24px 16px 0;
}

.info-box {
  background: $surface-dark;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.info-icon {
  color: $primary;
  font-size: 24px;
  margin-bottom: 4px;
}

.info-value {
  color: #fff;
  font-weight: bold;
  font-size: 16px;
}

.info-label {
  color: #64748b;
  font-size: 10px;
  text-transform: uppercase;
  font-weight: bold;
  margin-top: 2px;
}

/* Section Common */
.section {
  padding: 32px 16px 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-indicator {
  width: 4px;
  height: 24px;
  background-color: $primary;
  border-radius: 999px;
}

.section-title {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}

/* Info List */
.info-list {
  border-radius: 16px;
  overflow: hidden;
  
  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .item-label {
    color: $text-gray;
  }
  
  .item-value {
    color: #fff;
    font-weight: 500;
  }
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 20px 0;
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    border-right: 1px solid rgba(255, 255, 255, 0.05);
    
    &:last-child {
      border-right: none;
    }
  }
  
  .stat-num {
    color: $primary;
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 4px;
  }
  
  .stat-label {
    color: $text-gray;
    font-size: 12px;
  }
}
</style>