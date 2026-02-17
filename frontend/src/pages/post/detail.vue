<template>
  <view class="container">
    <!-- Status Bar -->
    <view class="status-bar"></view>

    <!-- Header -->
    <view class="header">
      <view class="header-left">
        <view class="back-btn" @click="goBack">
          <text class="material-icons" style="font-size: 24px; color: #fff;">arrow_back_ios_new</text>
        </view>
        <view class="author-info">
          <view class="avatar-container">
            <image class="author-avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAWv-26aws5gEp9SoPQuU_Y-mk8QV0gVdlTStCEFcVTwsy5_dpIg9jX8z_6TMMYA6RohT7BZTADrl62UOwDEJ23hX1j3GwDPKg23dgfcLBNFE_Po8QqWSUE29sRIpoMJeKGhNYU_RNibZqTHR7BUXl-t1MjNtQse5Q2mhhOHf1dj9q2-ADEfG-F2HAzvi8eVXOBvvwZvK9Fo3xd0dcamjww90CjWcXPDXEvxZq1tkztf7es8VwQyPk2NfW3m5zvNNHnOMUVhYeocJc1" mode="aspectFill"></image>
            <view class="verified-badge">
              <text class="material-icons" style="font-size: 6px; color: #fff;">bolt</text>
            </view>
          </view>
          <view class="author-details">
            <view class="author-name-row">
              <text class="author-name">TacticalMaster_AI</text>
              <text class="material-icons verified-icon">verified</text>
            </view>
            <text class="author-role">Expert Predictor</text>
          </view>
        </view>
      </view>
      <button class="follow-btn">Follow</button>
    </view>

    <scroll-view scroll-y class="content-scroll">
      <!-- Article Content -->
      <view class="article">
        <view class="article-meta">
          <text class="tag"># UCL_Predictions</text>
          <text class="time">2 hours ago</text>
        </view>
        
        <text class="article-title">AI Analysis: Why Real Madrid's transition game will break City's high press ⚡️</text>
        
        <view class="article-body">
          <text class="body-text">After simulating the match 10,000 times on our PitchPulse AI engine, the data shows a staggering 68% probability of a counter-attacking goal in the first 30 minutes. The hybrid positioning of Bellingham is the key...</text>
          <text class="read-more">Read more</text>
        </view>

        <!-- Image Grid -->
        <view class="image-grid">
          <view class="grid-item">
            <image class="grid-image" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBU26CqlmforlJF_IccguOr0tQXy2qXyVmdIap6Ehv3s6rOzM4dHZSGoLbe0mmGuNNdCRxDVbxlFoUIEN9HlXBhLoc6Mo4bqmJmksZ6OzW6IujCTR1VsSlM0WK6ULUP38Vina2mJ2O9iX8qVmRUS29GBge_eEYo9EkAebRy2OAmkOIjk7sCxuW623dXScmOyl0DAvF-6Zww-6I7NBjM646ekQMDBGI2Ps0u2Ph9ARHXuV9WAxHif7lwGjrZhhOiUJM6PMbWIQiPQVs5" mode="aspectFill"></image>
          </view>
          <view class="grid-item">
            <image class="grid-image" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAfVhxd9UX1WqDsvs1HwgDbCGu5M41hjWe7UHY8sbkULMVCMop-K-w-4Zc2huAXOGR6xX6X_sqpiDFNUtXOeyJV2LouCPdS9yIN0nca_quoKCKb32nx6VC__ZbhOL0qaMhysMl0CcHPUbjDuRUnBDA-ogoiKu_hXdCe5FIHiTTp1vN5DFuNeetIEVSYCNGddknExGB7EesozGS7JZ46Bu9o39GgzKIYOdOZ4LrW7dXwlaYscBWDxCHXR7SczLUt0e9auhGBgKLnHjrW" mode="aspectFill"></image>
          </view>
          <view class="grid-item relative">
            <image class="grid-image" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDIZaff89z6j8zG4Qc3b89ewscYjSTz-bgb7OVT7A9eTCsm5wBACICeoZhqdWc9hwxernbN5mbfEyZYlJiCNXI_iCkaZQJmS-auAR6UZaOiZ-lzVbEXDPNuhtMs_2ddW9yNM8UDi6c-Stc_UBt4ZLwdBWvFdBBM_emFrBB9-d5bPIfJymcOI6wL9M6ug84XFEYm9VGPHupQGY2FE1fxP1mWRjLcv6wlCM8hkvTL1FOlV2iyxE1eegrSfoN6lnKwNESumNGkkaZeSx4g" mode="aspectFill"></image>
            <view class="grid-overlay">
              <text class="overlay-text">+12 Data Charts</text>
            </view>
          </view>
          <view class="grid-item">
            <image class="grid-image" src="https://lh3.googleusercontent.com/aida-public/AB6AXuACPlUE9NbwSJYJRgiwcroN7ZBpCZ9o1WPdynqh9QWOy2V1BHFzAu7xOEIoIsZP34nF24Q6xC3_qg_VkX1Bzj9IMlnnTi9_6DGIvLfe7PH-V2YaBi5yeWDx-SVY8qG4ZUPtx_jLhTqeccvOnMhq4wnenJtlte0ykmMX9VSa7CFcIrQUx76Ei0f55LKOKAxNE5oYnSPHy9Tq3F16bQ_mKflH_GQ8ImTLuV55_ykg3Wv7fPyfTA0RM0uiCh0-_6RDvRcLo5TZqi7sw1AK" mode="aspectFill"></image>
          </view>
        </view>

        <!-- Stats -->
        <view class="interaction-stats">
          <view class="avatar-stack">
            <image class="stack-avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuA3aemkmila5RtDnSEVeYfpggtwtb0tFxiELe0pvXWCqFODNFHUlY80bp2bNUUGiBiHJGk3wzRi9xhd_iY1vl7abR6J7rrhwuhIB-jE6zi5yzI92ORuKwe01HXbiOt0dj8VJ-C3NY9CPwWj25gW6gbEXSMS1DgMlGSnKdZVw5MMBTwj3Um_ioQwn851hkkGIT06PCHicE_oBKKo5E5nRiePNPzmf5FAVyO5Bp7GF_2Uh-t1m-K4SoaJT0VjyTALgIo31hpBsOc3Jxo7"></image>
            <image class="stack-avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCFK69v87a3OMm_841rfP0IJiVxpUezzAYh6oc1zDkECpd4KJo_3lxxa2kV5y5-qoruTXJkLVIBNFf_ra9RMte9SZZMBeScS1PvdYZV8l_86CkrTngnnTELO1Ud7AlEfk2CJrp7V5obxYFwKRuPJvihC3KalhMxjZKCFXHSIRIYvj3QHJdyP0NWJvjDw_7D8caEgqboMsFpqXt8Q2OJFX0j_Wc0NjsuJNNfChJmnrYB7WsAMNTEw-cNX4nzzg1ilPefeDFG8MD95CAQ"></image>
            <image class="stack-avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBs_vDqGdh99yAOMvBR5eHoZ600sGvKOQQRcqEtNaZcyJdl3_W2pqt4rJItb6hQjOBm073onC3e1ihuy-HKb1jBM8TFg-2dF7vcJFToB7kPj3wBOwvWEBv9Mzz4eQASGZEwlQgBE1_v8J9kTY1VdhTFd7Ry8-qdQKcJQoIQ9ROVwGsygatDBtmhfx7uw8FfveMrwC2J6rJCuDiXUaH78iDwXolhcN6WtidRqQB7baK0cE3XTmXkWisiHynww5smrmYUVZO4GEChhXLG"></image>
            <view class="stack-count">+1.2k</view>
          </view>
          <view class="view-stats">
            <text class="stat-highlight">12,482</text> Views • <text class="stat-highlight">482</text> Likes
          </view>
        </view>
      </view>

      <!-- Comments -->
      <view class="comments-section">
        <view class="comments-header">
          <text class="comments-title">Community Discussion (142)</text>
          <view class="sort-btn">
            <text>Newest</text>
            <text class="material-icons" style="font-size: 14px;">expand_more</text>
          </view>
        </view>

        <view class="comments-list">
          <view class="comment-item" v-for="(comment, index) in comments" :key="index">
            <image class="comment-avatar" :src="comment.user.avatar" mode="aspectFill"></image>
            <view class="comment-content-wrapper">
              <view class="comment-bubble">
                <view class="comment-user-row">
                  <text class="comment-username">{{ comment.user.name }}</text>
                  <text class="comment-time">{{ comment.time }}</text>
                </view>
                <text class="comment-text">{{ comment.content }}</text>
              </view>
              <view class="comment-actions">
                <view class="action-item">
                  <text class="material-icons" style="font-size: 14px;">favorite_border</text>
                  <text class="action-text">{{ comment.likes }}</text>
                </view>
                <text class="reply-btn">Reply</text>
              </view>

              <!-- Nested Replies -->
              <view class="nested-replies" v-if="comment.replies && comment.replies.length > 0">
                <view class="comment-item nested" v-for="(reply, rIndex) in comment.replies" :key="rIndex">
                  <image class="comment-avatar small" :src="reply.user.avatar" mode="aspectFill"></image>
                  <view class="comment-content-wrapper">
                    <view class="comment-bubble semi-transparent">
                      <view class="comment-user-row">
                        <text class="comment-username">{{ reply.user.name }}</text>
                        <text class="comment-time">{{ reply.time }}</text>
                      </view>
                      <text class="comment-text">{{ reply.content }}</text>
                    </view>
                    <view class="comment-actions">
                      <view class="action-item">
                        <text class="material-icons" style="font-size: 12px; color: #f2b90d;">favorite</text>
                        <text class="action-text">{{ reply.likes }}</text>
                      </view>
                      <text class="reply-btn">Reply</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { postApi } from '@/api';

const comments = ref([]);
const postId = ref(null);

const goBack = () => {
  uni.navigateBack();
};

// Default Mock Data
const defaultComments = [
  {
    user: {
      name: 'Kylian_Stan7',
      avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAUXudDBE16DhOai6np2yUPZLffzpmi2V7ZJdl5lTIhsGlQCUCOyu5n95GdKQryAjFB_eqiVsbIZP7wnv-QiDI00TGNBQ-TsxtVaum5gaGXiT9JqnpdahlV24jtv45QhoVkAiJgwIBeb_IaZQC52v6bDbe1gnbPfbdQcP6jCytd2FeulAEd_vwptCcpDz2kLqLL7c4Qpn784dQmIR_bR0fXrkfbBEZ36Fek9Ac8TzTNoGsFFwFioL44eirMqWoIG10jxI02pu5nTSKU'
    },
    time: '1h ago',
    content: "The AI missed the Rodri factor. If he plays deep, Madrid's transition will be suffocated. Look at the stats from the last 5 games!",
    likes: 24,
    replies: [
      {
        user: {
          name: 'SarahStats',
          avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCorKt5rGdLNiUO420dckysCDHIMz84gOyqb6R2B1M-GqPihcabXVPHJco5fmqyb5BeEgsyNNNM4KfaodSyTYwTSqm-4GE8C8yE5flsAUSG2v6I7NhMFaU9Rt8o2-vwHXq55daSZPP0S80AgNVMxqmpFBQ9flHryatFRT_BMxBNwJFphBAbCjkPTfU9KlX_Zw-FC0vELpC6kNyZSrV4F6jAvNiq8nN11-4bk-ASgKnecdW-ypHtQ6vYrCfVSU__qhXOToKT2zWqxfmp'
        },
        time: '45m ago',
        content: "Actually, @Kylian_Stan7 the heatmaps show Rodri is pushing higher this season. Leaving the gap for Vini Jr.",
        likes: 8
      }
    ]
  }
];

// Initialize with default data
comments.value = defaultComments;

const loadComments = async (id) => {
  try {
    const res = await postApi.getComments(id);
    if (res) {
      comments.value = res;
    }
  } catch (e) {
    console.error('Failed to load comments:', e);
    // Keep default data
  }
};

onLoad((options) => {
  if (options.id) {
    postId.value = options.id;
    loadComments(options.id);
  }
  if (options.title) {
    console.log('Post loaded for topic:', decodeURIComponent(options.title));
  }
});
</script>

<style scoped>
/* Colors */
.container {
  background-color: #0A0A0A;
  min-height: 100vh;
  color: #fff;
}

.status-bar {
  height: var(--status-bar-height);
  width: 100%;
  background-color: rgba(248, 245, 246, 0.8);
}

.header {
  position: sticky;
  top: 0;
  z-index: 50;
  background-color: rgba(10, 10, 10, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(242, 185, 13, 0.1);
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  padding: 4px;
  margin-left: -4px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-container {
  position: relative;
}

.author-avatar {
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
  border: 2px solid #0A0A0A;
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

.author-role {
  font-size: 10px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.follow-btn {
  background-color: #f2b90d;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  padding: 6px 16px;
  border-radius: 9999px;
  margin: 0;
  line-height: 1.5;
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
  background-color: rgba(242, 185, 13, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.time {
  font-size: 12px;
  color: #64748b;
}

.article-title {
  font-size: 20px;
  font-weight: bold;
  line-height: 1.25;
  margin-bottom: 12px;
  color: #fff;
  display: block;
}

.article-body {
  margin-bottom: 16px;
}

.body-text {
  font-size: 14px;
  color: #cbd5e1;
  line-height: 1.625;
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
  background-color: #1A1A1A;
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
  padding: 12px 0;
  border-top: 1px solid rgba(30, 41, 59, 0.5);
  border-bottom: 1px solid rgba(30, 41, 59, 0.5);
}

.avatar-stack {
  display: flex;
  margin-left: 8px;
}

.stack-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid #0A0A0A;
  margin-left: -8px;
}

.stack-count {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #1e293b;
  border: 2px solid #0A0A0A;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8px;
  font-weight: bold;
  color: #94a3b8;
  margin-left: -8px;
}

.view-stats {
  font-size: 11px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: -0.05em;
}

.stat-highlight {
  color: #cbd5e1;
  font-weight: bold;
}

/* Comments */
.comments-section {
  background-color: #0A0A0A;
  margin-top: 8px;
}

.comments-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comments-title {
  font-weight: bold;
  font-size: 14px;
  color: #fff;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #94a3b8;
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
}

.comment-item.nested {
  margin-top: 16px;
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
  background-color: #1A1A1A;
  padding: 12px;
  border-radius: 12px;
  border-top-left-radius: 0;
}

.comment-bubble.semi-transparent {
  background-color: rgba(26, 26, 26, 0.5);
  padding: 10px;
}

.comment-user-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.comment-username {
  font-size: 12px;
  font-weight: bold;
  color: #e2e8f0;
}

.comment-time {
  font-size: 10px;
  color: #64748b;
}

.comment-text {
  font-size: 14px;
  color: #cbd5e1;
}

.highlight-text {
  color: #f2b90d;
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
  color: #64748b;
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
  color: #64748b;
}

.nested-replies {
  margin-top: 16px;
  border-left: 2px solid #1e293b;
  padding-left: 16px;
}
</style>