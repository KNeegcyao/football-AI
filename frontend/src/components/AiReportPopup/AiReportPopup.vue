<template>
  <view v-if="visible" class="report-popup-overlay" @click.stop="handleClose">
    <view 
      class="report-popup-content bg-card" 
      :style="{ transform: `translateY(${translateY}px)`, transition: isDragging ? 'none' : 'transform 0.3s ease-out' }"
      @click.stop
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
    >
      <!-- 下滑关闭指示器 -->
      <view class="close-indicator" @click="handleClose"></view>

      <scroll-view scroll-y class="report-scroll-view">
        <!-- 1. Header: 身份识别 -->
        <view class="report-header">
          <view class="ai-badge-wrapper">
            <view class="ai-badge-glow"></view>
            <text class="ai-badge">SMART REPORT</text>
          </view>
          
          <view class="match-main-info">
            <view class="team home">
              <image :src="data.homeLogo" mode="aspectFit" class="team-logo"></image>
              <text class="team-name">{{ data.homeName }}</text>
            </view>
            <view class="score-box">
              <text class="score">{{ data.homeScore }} : {{ data.awayScore }}</text>
              <view class="match-meta">
                <text>{{ data.competition }} · {{ data.round }}</text>
                <text>{{ data.date }} · {{ data.location }}</text>
              </view>
            </view>
            <view class="team away">
              <image :src="data.awayLogo" mode="aspectFit" class="team-logo"></image>
              <text class="team-name">{{ data.awayName }}</text>
            </view>
          </view>
        </view>

        <!-- Loading State: 炫酷骨架屏 -->
        <view v-if="loading" class="ai-loading-container">
          <view class="ai-scanner">
            <view class="scan-line"></view>
          </view>
          <text class="loading-text ai-blink">AI 正在深度解析局势，请稍候...</text>
          
          <!-- 骨架占位 -->
          <view class="skeleton-section">
            <view class="skeleton-title"></view>
            <view class="skeleton-card"></view>
          </view>
          <view class="skeleton-section mt-40">
            <view class="skeleton-title"></view>
            <view class="skeleton-card"></view>
            <view class="skeleton-card mt-20"></view>
          </view>
        </view>

        <!-- 内容区域 (加载完成显示) -->
        <block v-else>
          <!-- 2. AI Core Summary: 灵魂内容 -->
        <view class="section summary-section">
          <view class="section-title">
            <text class="material-icons title-icon">psychology</text>
            <text>AI 核心简评</text>
            <view class="tone-tag" v-if="data.summary?.tone">
              <text class="tone-emoji">{{ data.summary?.emoji || '⚽' }}</text>
              <text>{{ data.summary?.tone }}</text>
            </view>
          </view>
          <view class="summary-card gold-border-breathe" v-if="data.summary?.text">
            <view class="quote-icon left">“</view>
            <text class="summary-text">{{ data.summary.text }}</text>
            <view class="quote-icon right">”</view>
          </view>
          <view class="summary-card empty-state" v-else>
            <text class="summary-text opacity-50">AI 正在深度复盘本场对决的每一个瞬间...</text>
          </view>
        </view>

        <!-- 3. Tactical Insights: 战术洞察 -->
        <view class="section tactical-section" v-if="data.insights && data.insights.length > 0">
          <view class="section-title">
            <text class="material-icons title-icon">insights</text>
            <text>战术洞察</text>
          </view>
          <view class="insight-list">
            <view v-for="(insight, index) in data.insights" :key="index" class="insight-item card-glass">
              <view class="insight-header">
                <view class="insight-number">0{{ index + 1 }}</view>
                <text class="insight-title">{{ insight.title }}</text>
              </view>
              <text class="insight-desc">{{ insight.description }}</text>
            </view>
          </view>
          <!-- 核心区域表现 -->
          <view class="attack-area-box card-glass" v-if="data.attackArea">
            <view class="area-header">
              <text class="area-title">主队进攻区域分布</text>
              <text class="area-subtitle">DANGER ZONES</text>
            </view>
            <view class="area-bar-container">
              <view class="area-bar home" :style="{ width: (data.attackArea?.home || 33) + '%' }">
                <text class="area-val" v-if="data.attackArea?.home > 15">左路 {{ data.attackArea?.home }}%</text>
              </view>
              <view class="area-bar middle" :style="{ width: (data.attackArea?.middle || 34) + '%' }">
                <text class="area-val" v-if="data.attackArea?.middle > 15">中路 {{ data.attackArea?.middle }}%</text>
              </view>
              <view class="area-bar away" :style="{ width: (data.attackArea?.away || 33) + '%' }">
                <text class="area-val" v-if="data.attackArea?.away > 15">右路 {{ data.attackArea?.away }}%</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 4. Key Players: 关键球员 -->
        <view class="section player-section" v-if="data.mvp?.name || data.darkHorse?.name">
          <view class="section-title">
            <text class="material-icons title-icon">stars</text>
            <text>关键球员</text>
          </view>
          <view class="player-cards">
            <view class="player-card mvp gold-border-breathe card-glass" v-if="data.mvp?.name">
              <view class="player-header">
                <view class="badge mvp">MVP</view>
                <text class="player-name">{{ data.mvp.name }}</text>
              </view>
              <view class="player-reason-box">
                <text class="player-reason">{{ data.mvp.reason }}</text>
              </view>
            </view>
            <view class="player-card sleeper card-glass" v-if="data.darkHorse?.name">
              <view class="player-header">
                <view class="badge sleeper">黑马</view>
                <text class="player-name">{{ data.darkHorse.name }}</text>
              </view>
              <view class="player-reason-box">
                <text class="player-reason">{{ data.darkHorse.reason }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 5. Visual Stats: 数据对比 -->
        <view class="section stats-section">
          <view class="section-title">
            <text class="material-icons title-icon">analytics</text>
            <text>核心统计</text>
            <view class="xg-badge" v-if="data.stats?.homeXG">
              <text>xG {{ data.stats.homeXG }} : {{ data.stats.awayXG }}</text>
            </view>
          </view>
          
          <!-- 进球时间轴 -->
          <view class="goal-timeline card-glass" v-if="data.events && data.events.length > 0">
            <view class="timeline-line"></view>
            <view 
              v-for="(event, index) in data.events" 
              :key="index" 
              class="event-node"
              :style="{ left: (Math.min(event.minute, 90) / 90 * 100) + '%' }"
              :class="event.teamType"
            >
              <view class="event-icon-box">
                <text class="material-icons event-icon">sports_soccer</text>
              </view>
              <view class="event-popover gold-border-breathe">
                <text class="time">{{ event.minute }}'</text>
                <text class="player">{{ event.playerName }}</text>
              </view>
            </view>
            <view class="timeline-labels">
              <text>START</text>
              <text>HALF</text>
              <text>FULL TIME</text>
            </view>
          </view>

          <view class="stats-container card-glass">
            <view v-for="(stat, key) in statsMap" :key="key" class="stat-row">
              <view class="stat-info">
                <text class="val left">{{ data.stats?.[stat.homeKey] ?? 0 }}{{ stat.unit }}</text>
                <text class="stat-label">{{ stat.label }}</text>
                <text class="val right">{{ data.stats?.[stat.awayKey] ?? 0 }}{{ stat.unit }}</text>
              </view>
              <view class="stat-bars">
                <view class="bar-fill left" :style="{ width: calcPercent(data.stats?.[stat.homeKey] ?? 0, data.stats?.[stat.awayKey] ?? 0) + '%' }">
                  <view class="glow"></view>
                </view>
                <view class="bar-fill right" :style="{ width: (100 - calcPercent(data.stats?.[stat.homeKey] ?? 0, data.stats?.[stat.awayKey] ?? 0)) + '%' }">
                  <view class="glow"></view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 6. Engagement: 互动引导 -->
        <view class="section engagement-section">
          <view class="vote-card gold-border-breathe card-glass">
            <view class="vote-header">
              <text class="material-icons vote-icon">poll</text>
              <text class="vote-question">{{ data.engagement?.voteTopic || '本场比赛 AI 战报是否客观？' }}</text>
            </view>
            <view class="vote-btns">
              <view class="vote-btn-gold" @click="handleVote('support')">
                <text class="material-icons">thumb_up</text>
                <text>非常犀利</text>
              </view>
              <view class="vote-btn-outline" @click="handleVote('oppose')">
                <text class="material-icons">feedback</text>
                <text>我有话说</text>
              </view>
            </view>
          </view>
          
          <view class="action-footer">
            <view class="share-btn-premium" @click="handleShare">
              <view class="ai-glow"></view>
              <text class="material-icons">auto_awesome</text>
              <text>生成 AI 深度战报海报</text>
              <text class="material-icons">chevron_right</text>
            </view>
          </view>
        </view>
        </block>

        <view class="safe-area-bottom"></view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue';
// 尝试引入 html2canvas (仅在 H5 环境有效)
let html2canvas = null;
// #ifdef H5
import('html2canvas').then(module => {
  html2canvas = module.default;
});
// #endif

const props = defineProps({
  visible: Boolean,
  loading: {
    type: Boolean,
    default: false
  },
  data: {
    type: Object,
    default: () => ({
      homeName: '',
      homeLogo: '',
      homeScore: 0,
      awayName: '',
      awayLogo: '',
      awayScore: 0,
      competition: '',
      round: '',
      date: '',
      location: '',
      summary: {
        text: '',
        tone: '',
        emoji: ''
      },
      insights: [],
      attackArea: { home: 33, middle: 34, away: 33 },
      mvp: { name: '', reason: '' },
      darkHorse: { name: '', reason: '' },
      stats: {
        homePossession: 50, awayPossession: 50,
        homeShots: 0, awayShots: 0,
        homeXG: 0, awayXG: 0,
        homeTackles: 0, awayTackles: 0,
        homePassSuccess: 0, awayPassSuccess: 0
      },
      events: [],
      engagement: {
        voteTopic: ''
      }
    })
  }
});

const emit = defineEmits(['update:visible']);

// 下滑关闭逻辑
const translateY = ref(0);
const startY = ref(0);
const isDragging = ref(false);

const handleTouchStart = (e) => {
  startY.value = e.touches[0].clientY;
  isDragging.value = true;
};

const handleTouchMove = (e) => {
  const currentY = e.touches[0].clientY;
  const deltaY = currentY - startY.value;
  if (deltaY > 0) {
    translateY.value = deltaY;
  }
};

const handleTouchEnd = () => {
  isDragging.value = false;
  if (translateY.value > 150) {
    handleClose();
  } else {
    translateY.value = 0;
  }
};

const statsMap = [
  { label: '控球率', homeKey: 'homePossession', awayKey: 'awayPossession', unit: '%' },
  { label: '射门数', homeKey: 'homeShots', awayKey: 'awayShots', unit: '' },
  { label: '预期进球', homeKey: 'homeXG', awayKey: 'awayXG', unit: '' },
  { label: '抢断数', homeKey: 'homeTackles', awayKey: 'awayTackles', unit: '' },
  { label: '传球成功率', homeKey: 'homePassSuccess', awayKey: 'awayPassSuccess', unit: '%' }
];

const calcPercent = (val1, val2) => {
  const total = parseFloat(val1) + parseFloat(val2);
  if (total === 0) return 50;
  return Math.round((parseFloat(val1) / total) * 100);
};

const handleClose = () => {
  translateY.value = 0;
  emit('update:visible', false);
};

const handleVote = (type) => {
  uni.showToast({
    title: type === 'support' ? '感谢支持！' : '感谢反馈！',
    icon: 'success'
  });
};

const handleShare = async () => {
  uni.showLoading({ title: '正在生成海报...' });

  try {
    // #ifdef H5
    if (!html2canvas) {
      throw new Error('海报生成组件未加载完成，请稍后再试');
    }
    
    // 获取需要截图的 DOM 节点
    const targetNode = document.querySelector('.report-popup-content');
    if (!targetNode) {
      throw new Error('未找到需要生成海报的内容');
    }

    // 调用 html2canvas 生成 canvas
    const canvas = await html2canvas(targetNode, {
      backgroundColor: '#141414', // 设置背景色以防透明背景发黑
      useCORS: true, // 允许跨域图片
      scale: window.devicePixelRatio > 1 ? window.devicePixelRatio : 2, // 提高清晰度
      logging: false,
      allowTaint: true,
      // 忽略不需要截图的元素（比如底部的关闭按钮和分享按钮本身）
      ignoreElements: (element) => {
        if (element.classList.contains('action-footer') || element.classList.contains('drag-handle')) {
          return true;
        }
        return false;
      }
    });

    // 将 canvas 转换为 base64 图片 URL
    const imgDataUrl = canvas.toDataURL('image/png');

    // 触发下载或保存操作
    const link = document.createElement('a');
    link.href = imgDataUrl;
    link.download = `AI深度战报-${props.data.homeName}-vs-${props.data.awayName}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    uni.hideLoading();
    uni.showToast({
      title: '海报已开始下载',
      icon: 'success'
    });
    // #endif

    // #ifndef H5
    // 非 H5 端的处理 (App / 小程序)
    // 注意：App 和小程序端无法使用 DOM 方案，通常需要使用 Canvas 绘制方案 (如 uni.createCanvasContext)
    // 这里暂时给出提示，因为后续如果上 App 需要接入特定的 Canvas 海报生成组件 (如 Painter)
    uni.hideLoading();
    uni.showToast({
      title: '当前环境暂不支持直接保存，敬请期待 App 端专属海报功能',
      icon: 'none',
      duration: 3000
    });
    // #endif
  } catch (error) {
    console.error('生成海报失败:', error);
    uni.hideLoading();
    uni.showToast({
      title: '海报生成失败，请稍后重试',
      icon: 'none'
    });
  }
};
</script>

<style lang="scss" scoped>
.report-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  z-index: 999;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.report-popup-content {
  width: 100%;
  height: 85vh;
  border-radius: 48rpx 48rpx 0 0;
  background: rgba(20, 20, 20, 0.85);
  backdrop-filter: blur(15px);
  position: relative;
  border: 1rpx solid rgba(212, 175, 55, 0.2);
  box-shadow: 0 -10rpx 40rpx rgba(0, 0, 0, 0.6);
  overflow: hidden;
  animation: slideUp 0.4s cubic-bezier(0.25, 1, 0.5, 1);
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.close-indicator {
  width: 80rpx;
  height: 8rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4rpx;
  margin: 24rpx auto;
}

.report-scroll-view {
  height: calc(85vh - 56rpx);
  padding: 0 40rpx;
  box-sizing: border-box;
}

/* Header Styles */
.report-header {
  padding: 20rpx 0 40rpx;
  text-align: center;

  .ai-badge-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 30rpx;

    .ai-badge {
      background: linear-gradient(90deg, #d4af37, #f9e29c);
      -webkit-background-clip: text;
      color: transparent;
      font-size: 22rpx;
      font-weight: 800;
      letter-spacing: 4rpx;
      position: relative;
      z-index: 2;
    }

    .ai-badge-glow {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 120%;
      height: 120%;
      background: rgba(212, 175, 55, 0.2);
      filter: blur(15rpx);
      animation: badgePulse 2s infinite;
    }
  }

  @keyframes badgePulse {
    0% { opacity: 0.3; transform: translate(-50%, -50%) scale(0.8); }
    50% { opacity: 0.8; transform: translate(-50%, -50%) scale(1.1); }
    100% { opacity: 0.3; transform: translate(-50%, -50%) scale(0.8); }
  }

  .match-main-info {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .team {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16rpx;

      .team-logo {
        width: 100rpx;
        height: 100rpx;
      }

      .team-name {
        font-size: 28rpx;
        font-weight: 700;
        color: #fff;
      }
    }

    .score-box {
      flex: 1.5;
      display: flex;
      flex-direction: column;
      align-items: center;

      .score {
        font-size: 64rpx;
        font-weight: 800;
        font-family: 'DIN Alternate', sans-serif;
        color: #fff;
        margin-bottom: 12rpx;
        text-shadow: 0 0 20rpx rgba(255, 255, 255, 0.2);
      }

      .match-meta {
        display: flex;
        flex-direction: column;
        gap: 4rpx;
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.5);
      }
    }
  }
}

/* Common Section Styles */
.section {
  margin-bottom: 48rpx;

  .section-title {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 24rpx;
    font-size: 30rpx;
    font-weight: 700;
    color: #fff;

    .title-icon {
      font-size: 36rpx;
      color: #d4af37;
    }

    .tone-tag {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 6rpx 16rpx;
      background: rgba(212, 175, 55, 0.1);
      border: 1rpx solid rgba(212, 175, 55, 0.2);
      border-radius: 100rpx;
      font-size: 20rpx;
      color: #d4af37;
      
      .tone-emoji {
        font-size: 24rpx;
      }
    }

    .xg-badge {
      margin-left: auto;
      padding: 4rpx 16rpx;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 8rpx;
      font-size: 20rpx;
      font-weight: 800;
      font-family: 'DIN Alternate', sans-serif;
      color: rgba(255, 255, 255, 0.6);
      letter-spacing: 1rpx;
    }
  }
}

.card-glass {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1rpx solid rgba(255, 255, 255, 0.05);
  border-radius: 24rpx;
  padding: 30rpx;
}

.gold-border-breathe {
  border: 1rpx solid rgba(212, 175, 55, 0.3);
  box-shadow: 0 0 15rpx rgba(212, 175, 55, 0.1);
  animation: goldBreathe 3s infinite ease-in-out;
}

@keyframes goldBreathe {
  0% { border-color: rgba(212, 175, 55, 0.2); box-shadow: 0 0 10rpx rgba(212, 175, 55, 0.05); }
  50% { border-color: rgba(212, 175, 55, 0.5); box-shadow: 0 0 25rpx rgba(212, 175, 55, 0.2); }
  100% { border-color: rgba(212, 175, 55, 0.2); box-shadow: 0 0 10rpx rgba(212, 175, 55, 0.05); }
}

/* 4. Player Cards */
.player-cards {
  display: flex;
  gap: 24rpx;

  .player-card {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 16rpx;

    .player-header {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .badge {
        padding: 4rpx 12rpx;
        border-radius: 6rpx;
        font-size: 18rpx;
        font-weight: 900;
        letter-spacing: 1rpx;

        &.mvp {
          background: linear-gradient(135deg, #d4af37, #f9e29c);
          color: #000;
        }

        &.sleeper {
          background: rgba(255, 255, 255, 0.1);
          color: #fff;
          border: 1rpx solid rgba(255, 255, 255, 0.2);
        }
      }

      .player-name {
        font-size: 26rpx;
        font-weight: 700;
        color: #fff;
      }
    }

    .player-reason-box {
      .player-reason {
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.6);
        line-height: 1.5;
      }
    }
  }
}

/* 5. Stats Section */
.goal-timeline {
  height: 120rpx;
  position: relative;
  margin-bottom: 40rpx;
  display: flex;
  align-items: center;
  padding: 0 40rpx;

  .timeline-line {
    position: absolute;
    left: 40rpx;
    right: 40rpx;
    height: 4rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 2rpx;
  }

  .event-node {
    position: absolute;
    transform: translateX(-50%);
    z-index: 2;

    .event-icon-box {
      width: 40rpx;
      height: 40rpx;
      background: #1a1a1a;
      border: 2rpx solid #fff;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;

      .event-icon {
        font-size: 24rpx;
        color: #fff;
      }
    }

    &.home .event-icon-box { border-color: #ff4d4f; .event-icon { color: #ff4d4f; } }
    &.away .event-icon-box { border-color: #40a9ff; .event-icon { color: #40a9ff; } }

    .event-popover {
      position: absolute;
      bottom: 50rpx;
      left: 50%;
      transform: translateX(-50%);
      padding: 8rpx 16rpx;
      background: rgba(30, 30, 30, 0.9);
      border-radius: 12rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      white-space: nowrap;
      opacity: 0;
      transition: all 0.3s;

      .time { font-size: 18rpx; font-weight: 800; color: #d4af37; }
      .player { font-size: 20rpx; color: #fff; }
    }

    &:hover .event-popover {
      opacity: 1;
      bottom: 60rpx;
    }
  }

  .timeline-labels {
    position: absolute;
    bottom: 10rpx;
    left: 40rpx;
    right: 40rpx;
    display: flex;
    justify-content: space-between;
    font-size: 16rpx;
    color: rgba(255, 255, 255, 0.3);
    font-weight: 700;
  }
}

.stats-container {
  display: flex;
  flex-direction: column;
  gap: 30rpx;

  .stat-row {
    .stat-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;

      .stat-label {
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.5);
        font-weight: 600;
      }

      .val {
        font-size: 24rpx;
        font-weight: 800;
        font-family: 'DIN Alternate', sans-serif;
        
        &.left { color: #ff7875; }
        &.right { color: #69c0ff; }
      }
    }

    .stat-bars {
      height: 8rpx;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 4rpx;
      display: flex;
      overflow: hidden;

      .bar-fill {
        height: 100%;
        position: relative;
        transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);

        &.left { 
          background: linear-gradient(90deg, #ff4d4f, #ff7875);
          border-radius: 0 4rpx 4rpx 0;
        }
        &.right { 
          background: linear-gradient(90deg, #69c0ff, #40a9ff);
          border-radius: 4rpx 0 0 4rpx;
          margin-left: auto;
        }

        .glow {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
          animation: barGlow 2s infinite;
        }
      }
    }
  }
}

@keyframes barGlow {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 6. Engagement Section */
.vote-card {
  margin-bottom: 40rpx;

  .vote-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 30rpx;

    .vote-icon {
      font-size: 40rpx;
      color: #d4af37;
    }

    .vote-question {
      font-size: 26rpx;
      font-weight: 700;
      color: #fff;
    }
  }

  .vote-btns {
    display: flex;
    gap: 20rpx;

    .vote-btn-gold, .vote-btn-outline {
      flex: 1;
      height: 80rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      font-size: 24rpx;
      font-weight: 700;
      transition: all 0.3s;

      &:active { transform: scale(0.95); }

      .material-icons { font-size: 32rpx; }
    }

    .vote-btn-gold {
      background: linear-gradient(135deg, #d4af37, #b8860b);
      color: #000;
    }

    .vote-btn-outline {
      background: rgba(255, 255, 255, 0.05);
      border: 1rpx solid rgba(255, 255, 255, 0.1);
      color: #fff;
    }
  }
}

.action-footer {
  padding-bottom: 40rpx;

  .share-btn-premium {
    width: 100%;
    height: 100rpx;
    background: linear-gradient(90deg, #1a1a1a, #2a2a2a);
    border: 1rpx solid rgba(212, 175, 55, 0.4);
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    position: relative;
    overflow: hidden;
    
    text {
      font-size: 28rpx;
      font-weight: 800;
      color: #d4af37;
      letter-spacing: 2rpx;
      z-index: 2;
    }

    .material-icons {
      font-size: 36rpx;
      color: #d4af37;
      z-index: 2;
    }

    .ai-glow {
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.1), transparent);
      animation: aiSweep 3s infinite;
    }
  }
}

@keyframes aiSweep {
  0% { left: -100%; }
  50% { left: 100%; }
  100% { left: 100%; }
}

/* Summary Card */
.summary-card {
  padding: 32rpx;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 24rpx;
  
  .summary-text {
    font-size: 28rpx;
    line-height: 1.6;
    color: rgba(255, 255, 255, 0.9);
    font-style: italic;
  }

  &.gold-border-breathe {
    border: 1rpx solid rgba(212, 175, 55, 0.4);
    box-shadow: 0 0 10rpx rgba(212, 175, 55, 0.3);
    animation: goldBreathe 3s infinite;
  }
}

@keyframes goldBreathe {
  0% { border-color: rgba(212, 175, 55, 0.3); box-shadow: 0 0 5rpx rgba(212, 175, 55, 0.1); }
  50% { border-color: rgba(212, 175, 55, 0.6); box-shadow: 0 0 15rpx rgba(212, 175, 55, 0.4); }
  100% { border-color: rgba(212, 175, 55, 0.3); box-shadow: 0 0 5rpx rgba(212, 175, 55, 0.1); }
}

/* Insight Styles */
.insight-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  .insight-item {
    .insight-header {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 8rpx;

      .dot {
        width: 8rpx;
        height: 8rpx;
        background: #d4af37;
        border-radius: 50%;
      }

      .insight-title {
        font-size: 26rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .insight-desc {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.6);
      padding-left: 20rpx;
      line-height: 1.5;
    }
  }
}

.attack-area-box {
  margin-top: 32rpx;
  background: rgba(0, 0, 0, 0.2);
  padding: 24rpx;
  border-radius: 20rpx;

  .area-title {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.5);
    margin-bottom: 16rpx;
    display: block;
  }

  .area-bar-container {
    height: 40rpx;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8rpx;
    display: flex;
    overflow: hidden;

    .area-bar {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18rpx;
      color: #fff;
      font-weight: 700;
      transition: width 0.5s ease;

      &.home { background: #ff4d4f; }
      &.middle { background: #434343; }
      &.away { background: #40a9ff; }
    }
  }
}

/* Player Cards */
.player-cards {
  display: flex;
  gap: 24rpx;

  .player-card {
    flex: 1;
    background: rgba(255, 255, 255, 0.03);
    padding: 24rpx;
    border-radius: 24rpx;
    position: relative;
    border: 1rpx solid rgba(255, 255, 255, 0.05);

    .player-badge {
      position: absolute;
      top: -12rpx;
      right: 12rpx;
      padding: 4rpx 16rpx;
      border-radius: 8rpx;
      font-size: 18rpx;
      font-weight: 800;
    }

    &.mvp .player-badge { background: #d4af37; color: #000; }
    &.sleeper .player-badge { background: #40a9ff; color: #fff; }

    .player-name {
      display: block;
      font-size: 28rpx;
      font-weight: 700;
      color: #fff;
      margin-bottom: 12rpx;
    }

    .player-reason {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.5);
      line-height: 1.4;
    }
  }
}

/* Stats Styles */
.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 32rpx;

  .stat-row {
    .stat-label {
      display: block;
      text-align: center;
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.4);
      margin-bottom: 12rpx;
    }

    .stat-bars {
      display: flex;
      align-items: center;
      gap: 24rpx;

      .val {
        font-size: 24rpx;
        font-weight: 700;
        color: #fff;
        width: 80rpx;
        &.left { text-align: right; }
        &.right { text-align: left; }
      }

      .bar-bg {
        flex: 1;
        height: 12rpx;
        background: rgba(255, 255, 255, 0.05);
        border-radius: 6rpx;
        display: flex;
        overflow: hidden;

        .bar-fill {
          height: 100%;
          transition: width 1s ease-out;
          &.left { background: linear-gradient(90deg, #ff4d4f, #ff7875); border-radius: 6rpx 0 0 6rpx; }
          &.right { background: linear-gradient(90deg, #69c0ff, #40a9ff); border-radius: 0 6rpx 6rpx 0; }
        }
      }
    }
  }
}

/* Goal Timeline */
.goal-timeline {
  position: relative;
  height: 120rpx;
  margin: 40rpx 20rpx 60rpx;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12rpx;
  padding: 0 20rpx;

  .timeline-line {
    position: absolute;
    top: 50%;
    left: 20rpx;
    right: 20rpx;
    height: 2rpx;
    background: rgba(255, 255, 255, 0.1);
    transform: translateY(-50%);
  }

  .event-node {
    position: absolute;
    top: 50%;
    transform: translate(-50%, -50%);
    z-index: 2;

    .event-icon-box {
      width: 40rpx;
      height: 40rpx;
      background: rgba(0, 0, 0, 0.6);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1rpx solid rgba(255, 255, 255, 0.2);
    }

    .event-icon {
      font-size: 24rpx;
      color: #fff;
    }

    &.home .event-icon { color: #ff4d4f; }
    &.away .event-icon { color: #40a9ff; }

    .event-popover {
      position: absolute;
      bottom: 40rpx;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(0, 0, 0, 0.8);
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      white-space: nowrap;
      display: flex;
      flex-direction: column;
      align-items: center;
      opacity: 0.8;

      .time { font-size: 18rpx; color: #d4af37; font-weight: 700; }
      .player { font-size: 18rpx; color: #fff; }
    }
  }

  .timeline-labels {
    position: absolute;
    bottom: -40rpx;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    font-size: 18rpx;
    color: rgba(255, 255, 255, 0.3);
  }
}

/* Engagement Styles */
.vote-card {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), transparent);
  padding: 32rpx;
  border-radius: 32rpx;
  border: 1rpx solid rgba(212, 175, 55, 0.1);
  margin-bottom: 40rpx;

  .vote-question {
    display: block;
    font-size: 28rpx;
    font-weight: 700;
    color: #fff;
    margin-bottom: 24rpx;
    text-align: center;
  }

  .vote-btns {
    display: flex;
    gap: 20rpx;

    .vote-btn {
      flex: 1;
      height: 80rpx;
      line-height: 80rpx;
      font-size: 26rpx;
      border-radius: 40rpx;
      background: #d4af37;
      color: #000;
      font-weight: 700;
      border: none;

      &.outline {
        background: transparent;
        border: 1rpx solid rgba(255, 255, 255, 0.2);
        color: #fff;
      }
    }
  }
}

.action-footer {
  .share-btn {
    width: 100%;
    height: 100rpx;
    line-height: 100rpx;
    border-radius: 50rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    font-size: 30rpx;
    font-weight: 700;
    background: linear-gradient(90deg, #d4af37, #f9e29c);
    color: #000;
    box-shadow: 0 10rpx 30rpx rgba(212, 175, 55, 0.3);
  }
}

.safe-area-bottom {
  height: 80rpx;
}

@keyframes scanSweep {
  0% { top: -20%; opacity: 0; }
  20% { opacity: 1; }
  80% { opacity: 1; }
  100% { top: 120%; opacity: 0; }
}

@keyframes blinkText {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

@keyframes skeletonPulse {
  0% { background-color: rgba(255, 255, 255, 0.03); }
  50% { background-color: rgba(255, 255, 255, 0.08); }
  100% { background-color: rgba(255, 255, 255, 0.03); }
}

/* ====== Loading State ====== */
.ai-loading-container {
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;

  .ai-scanner {
    width: 200rpx;
    height: 200rpx;
    border: 2rpx solid rgba(212, 175, 55, 0.3);
    border-radius: 50%;
    position: relative;
    overflow: hidden;
    margin-bottom: 32rpx;
    box-shadow: 0 0 40rpx rgba(212, 175, 55, 0.1) inset;

    .scan-line {
      position: absolute;
      left: 0;
      width: 100%;
      height: 4rpx;
      background: linear-gradient(90deg, transparent, #d4af37, transparent);
      box-shadow: 0 0 20rpx #d4af37;
      animation: scanSweep 2s infinite cubic-bezier(0.4, 0, 0.2, 1);
    }
  }

  .loading-text {
    color: #d4af37;
    font-size: 28rpx;
    letter-spacing: 2rpx;
    margin-bottom: 60rpx;
  }
  
  .ai-blink {
    animation: blinkText 1.5s infinite;
  }

  .skeleton-section {
    width: 100%;
    
    &.mt-40 { margin-top: 40rpx; }

    .skeleton-title {
      height: 32rpx;
      width: 200rpx;
      border-radius: 8rpx;
      margin-bottom: 24rpx;
      animation: skeletonPulse 1.5s infinite;
    }

    .skeleton-card {
      height: 160rpx;
      width: 100%;
      border-radius: 24rpx;
      animation: skeletonPulse 1.5s infinite;
      
      &.mt-20 { margin-top: 20rpx; }
    }
  }
}
</style>