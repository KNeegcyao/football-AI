<template>
  <view class="schedule-container" :class="themeClass" :style="{ '--status-bar-height': statusBarHeight + 'px' }">
    <!-- 固定顶部容器 -->
    <view class="fixed-header">
      <!-- 状态栏占位 -->
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }"></view>

      <!-- 顶部导航栏 -->
      <view class="nav-bar bg-nav-bar border-b border-theme-main">
        <view class="logo-area">
          <view class="logo-icon">
            <image src="/static/soccer-logo.png" mode="aspectFit" class="logo-img"></image>
          </view>
          <view class="logo-text italic">
            PITCH<text class="highlight text-theme-main">PULSE</text>
          </view>
        </view>

        <view class="nav-actions">
          <view class="action-btn" @click="goToSearch">
            <text class="material-icons" style="font-size: 48rpx; color: var(--text-main);">search</text>
          </view>
          <view class="avatar-box" @click="goToProfile">
            <image class="avatar" :src="userAvatar" mode="aspectFill" @error="handleAvatarError"></image>
          </view>
        </view>
      </view>

      <!-- 日期选择器容器 -->
      <view class="calendar-fixed-area bg-theme-main">
        <!-- 日期选择器标题 -->
        <view class="calendar-header">
          <text class="current-month text-theme-main">{{ currentYearMonth }}</text>
          <text class="view-calendar-btn">查看日历</text>
        </view>
        <!-- 日期选择器滚动条 -->
        <scroll-view scroll-x class="date-selector" show-scrollbar="false">
          <view class="date-list">
            <view 
              v-for="(item, index) in dates" 
              :key="index"
              class="date-item bg-card"
              :class="{ active: activeDateIndex === index }"
              @click="selectDate(index)"
            >
              <text class="date-week" :class="activeDateIndex === index ? 'text-white' : 'text-theme-secondary'">{{ item.week }}</text>
              <text class="date-day" :class="activeDateIndex === index ? 'text-white' : 'text-theme-main'">{{ item.day }}日</text>
              <view class="date-dot" v-if="activeDateIndex === index"></view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 赛事列表内容 -->
    <scroll-view scroll-y class="content-scroll">
      <!-- 顶部占位 -->
      <view class="header-placeholder"></view>
      
      <!-- 正在直播 -->
      <view class="section" v-if="liveMatches.length > 0">
        <view class="match-card live-card bg-card border-theme-main" v-for="match in liveMatches" :key="match.id">
          <!-- 顶部装饰栏 -->
          <view class="card-header-line">
            <view class="competition-info">
              <view class="ai-live-badge">
                <text class="material-icons badge-icon">auto_awesome</text>
                <text>AI LIVE</text>
              </view>
              <text class="competition-text text-theme-secondary">{{ match.competitionName }} · {{ match.round }}</text>
            </view>
            <view class="live-badge-gold">
              <view class="live-dot-pulse"></view>
              <text class="live-text">{{ match.liveTime || match.matchMinute || "" }}'</text>
            </view>
          </view>

          <view class="card-body">
            <view class="teams-score">
              <!-- 主队 -->
              <view class="team-side home">
                <view class="logo-wrapper">
                  <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
                </view>
                <text class="team-name text-theme-main">{{ match.homeTeam?.name }}</text>
              </view>

              <!-- 比分 -->
              <view class="score-center">
                <view class="score-display">
                  <text class="score-num home">{{ match.homeScore }}</text>
                  <text class="score-divider">:</text>
                  <text class="score-num away">{{ match.awayScore }}</text>
                </view>
                <view class="status-tag">
                  <text>{{ match.statusName || '进行中' }}</text>
                </view>
              </view>

              <!-- 客队 -->
              <view class="team-side away">
                <view class="logo-wrapper">
                  <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
                </view>
                <text class="team-name text-theme-main">{{ match.awayTeam?.name }}</text>
              </view>
            </view>
          </view>

          <!-- AI 预测区域 -->
          <view class="card-footer-ai-gold" @click.stop="openAiAnalysis(match)">
            <view class="btn-inner">
              <view class="ai-glow"></view>
              <view class="ai-header">
                <view class="ai-title">
                  <text class="material-icons ai-spark">auto_awesome</text>
                  <text class="ai-label">AI 实时胜率预测</text>
                </view>
                <view class="ai-action">
                  <text>深度洞察</text>
                  <text class="material-icons">chevron_right</text>
                </view>
              </view>
              
              <view class="ai-content">
                <view class="ai-progress-bar">
                  <view class="progress-segment home" :style="{ width: Math.round((match.homeWinProb || 0) * 100) + '%' }">
                    <view class="glow-bar"></view>
                  </view>
                  <view class="progress-segment draw" :style="{ width: Math.round((match.drawProb || 0) * 100) + '%' }"></view>
                  <view class="progress-segment away" :style="{ width: Math.round((match.awayWinProb || 0) * 100) + '%' }">
                    <view class="glow-bar"></view>
                  </view>
                </view>
                
                <view class="ai-stats">
                  <view class="stat-item">
                    <text class="stat-label">主胜</text>
                    <text class="stat-value home-text">{{ Math.round((match.homeWinProb || 0) * 100) }}%</text>
                  </view>
                  <view class="stat-item">
                    <text class="stat-label">平局</text>
                    <text class="stat-value draw-text">{{ Math.round((match.drawProb || 0) * 100) }}%</text>
                  </view>
                  <view class="stat-item">
                    <text class="stat-label">客胜</text>
                    <text class="stat-value away-text">{{ Math.round((match.awayWinProb || 0) * 100) }}%</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- AI Analysis Modal -->
      <view v-if="showAiModal" class="ai-modal-overlay">
        <view class="ai-modal-backdrop" @click="showAiModal = false"></view>
        <view class="ai-modal-content bg-card border-theme-main">
          <view class="modal-header">
            <view class="header-title">
              <text class="material-icons ai-icon">{{ aiModalIcon }}</text>
              <text class="title-text text-theme-main">{{ aiModalTitle }}</text>
            </view>
            <view class="close-btn" @click="showAiModal = false">
              <text class="material-icons">close</text>
            </view>
          </view>
          
          <view class="modal-body">
            <view v-if="aiLoading" class="ai-loading">
              <view class="loading-spinner"></view>
              <text class="loading-text text-theme-secondary">AI 正在深度分析对阵数据...</text>
            </view>
            <scroll-view v-else scroll-y class="ai-result-scroll">
              <view class="ai-analysis-text text-theme-main">
                <text>{{ aiAnalysisResult }}</text>
              </view>
            </scroll-view>
          </view>
          
          <view class="modal-footer">
            <button class="modal-close-btn bg-primary" @click="showAiModal = false">完成</button>
          </view>
        </view>
      </view>

      <!-- AI 战报详情弹窗 -->
      <AiReportPopup v-model:visible="showReportPopup" :loading="aiReportLoading" :data="reportData" />

      <!-- 即将开始 -->
      <view class="section" v-if="upcomingMatches.length > 0">
        <view class="section-title text-theme-secondary">即将开始</view>
        <view class="match-card-mini bg-card border-theme-main" v-for="match in upcomingMatches" :key="match.id">
          <view class="mini-header">
            <view class="header-left">
              <view class="indicator"></view>
              <text class="match-meta text-theme-secondary">{{ match.competitionName }} · {{ formatMatchTime(match.matchTime) }}</text>
            </view>
            <text class="material-icons" style="font-size: 44rpx; opacity: 0.2; color: var(--text-main);">notifications</text>
        </view>
        <view class="mini-body">
            <view class="mini-team">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
              </view>
              <text class="mini-name text-theme-main">{{ match.homeTeam?.name }}</text>
            </view>
            <text class="vs-text text-theme-secondary">VS</text>
            <view class="mini-team reverse">
              <view class="mini-logo">
                <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
              </view>
              <text class="mini-name text-theme-main">{{ match.awayTeam?.name }}</text>
            </view>
          </view>
          <!-- AI 预测：点击进入赛前深度预测 -->
          <view class="card-footer-summary-gold" @click.stop="openAiAnalysis(match)">
            <view class="btn-inner">
              <view class="ai-glow"></view>
              <text class="material-icons ai-sparkle">psychology</text>
              <text class="btn-text">查看 AI 赛前深度预测</text>
              <text class="material-icons arrow">chevron_right</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 已完赛 -->
      <view class="section" v-if="finishedMatches.length > 0">
        <view class="section-title text-theme-secondary">已完赛</view>
        <view class="match-card finished-card bg-card border-theme-main" v-for="match in finishedMatches" :key="match.id">
          <!-- 顶部装饰栏 -->
          <view class="card-header-line">
            <view class="competition-info">
              <view class="ai-insight-badge">
                <text class="material-icons badge-icon">auto_awesome</text>
                <text>AI ANALYSIS READY</text>
              </view>
              <text class="competition-text text-theme-secondary">{{ match.competitionName }} · {{ formatMatchDate(match.matchTime) }}</text>
            </view>
            <view class="status-tag finished">已完赛</view>
          </view>

          <view class="card-body">
            <view class="teams-score-finished">
              <view class="team home">
                <view class="logo-box">
                  <image :src="getFullImageUrl(match.homeTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.homeTeam)"></image>
                </view>
                <text class="name">{{ match.homeTeam?.name }}</text>
              </view>

              <view class="score-box">
                <text class="score-val" :class="{ 'home-win': match.homeScore > match.awayScore }">{{ match.homeScore }}</text>
                <text class="score-sep">-</text>
                <text class="score-val" :class="{ 'away-win': match.awayScore > match.homeScore }">{{ match.awayScore }}</text>
              </view>

              <view class="team away">
                <view class="logo-box">
                  <image :src="getFullImageUrl(match.awayTeam?.logoUrl)" mode="aspectFit" @error="handleImageError(match.awayTeam)"></image>
                </view>
                <text class="name">{{ match.awayTeam?.name }}</text>
              </view>
            </view>
          </view>

          <!-- 赛事总结按钮区域 -->
          <view class="card-footer-summary-gold" @click.stop="openMatchSummary(match)">
            <view class="btn-inner">
              <view class="ai-glow"></view>
              <text class="material-icons ai-sparkle">psychology</text>
              <text class="btn-text">查看 AI 深度战报总结</text>
              <text class="material-icons arrow">chevron_right</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 无比赛提示 -->
      <view class="no-match" v-if="liveMatches.length === 0 && upcomingMatches.length === 0 && finishedMatches.length === 0">
        <text class="material-icons" style="font-size: 128rpx; opacity: 0.1; color: var(--text-main);">info</text>
        <text class="no-match-text text-theme-secondary">今日暂无比赛</text>
      </view>
      
      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 底部导航占位 -->
    <view class="safe-area-bottom"></view>

    <!-- 底部导航栏 -->
    <CustomTabBar :currentTab="1" />
  </view>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { matchApi, userApi, aiApi } from '@/api'
import { getFullImageUrl } from '@/utils/request'
import { useThemeStore } from '@/store/theme'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import AiReportPopup from '@/components/AiReportPopup/AiReportPopup.vue'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight)
const liveMatches = ref([])
const upcomingMatches = ref([])
const finishedMatches = ref([])
const userAvatar = ref('/static/default-avatar.png')
const navbarPaddingRight = ref(16) // 默认 16px

const showAiModal = ref(false)
const aiLoading = ref(false)
const aiAnalysisResult = ref('')
const aiModalTitle = ref('AI 深度预测分析')
const aiModalIcon = ref('psychology')

// 新增 AI 战报弹窗状态
const showReportPopup = ref(false)
const aiReportLoading = ref(false)
const reportData = ref({})

const openAiAnalysis = async (match) => {
  const isUpcoming = match.status === 0

  // 提前准备好头部数据并打开弹窗显示 Loading 状态
  reportData.value = {
    homeName: match.homeTeam?.name,
    homeLogo: match.homeTeam?.logoUrl,
    homeScore: match.homeScore,
    awayName: match.awayTeam?.name,
    awayLogo: match.awayTeam?.logoUrl,
    awayScore: match.awayScore,
    matchTime: match.matchTime,
    competition: match.competitionName,
    round: match.round,
    location: match.venueName || '未知场地',
    date: formatMatchDate(match.matchTime),
    statusName: isUpcoming ? '未开赛' : `LIVE ${match.liveTime || match.matchMinute || ""}'`,
    isPrediction: isUpcoming,
    isLive: !isUpcoming
  }
  showReportPopup.value = true
  aiReportLoading.value = true

  try {
    // 准备比赛数据，供 AI 分析
    const matchData = `
      赛事：${match.competitionName}
      轮次：${match.round}
      时间：${isUpcoming ? formatMatchDate(match.matchTime) : (match.liveTime || match.matchMinute || "") + "' (LIVE)"}
      当前比分：${match.homeTeam?.name} ${match.homeScore} - ${match.awayScore} ${match.awayTeam?.name}
      状态：${isUpcoming ? '未开赛 (请进行赛前胜负和战术预测分析)' : '正在进行中 (请进行实时战况深度分析)'}
      主队：${match.homeTeam?.name}
      客队：${match.awayTeam?.name}
      主队实时胜率：${Math.round((match.homeWinProb || 0) * 100)}%
      平局实时胜率：${Math.round((match.drawProb || 0) * 100)}%
      客队实时胜率：${Math.round((match.awayWinProb || 0) * 100)}%
      注意：${isUpcoming ? '这是一场未开赛的比赛，请以“赛前预测”为基调。预测两队可能采取的战术，预测关键球员，并且 events 数组必须返回空。所有文案应为前瞻性质。非常重要提示：为了避免球员转会带来的信息滞后，在分析战术和预测关键球员时，绝对不要写出任何具体的球员名字（例如绝对不要出现姆巴佩、本泽马、梅西等真实姓名）！请一律使用“主队核心前锋”、“客队中场大师”、“主队主力门将”等位置代词来指代球员！' : '这是一场正在进行的比赛，请基于当前比分和实时胜率，输出实时战况分析。'}
    `.trim()

    // 调用 AI 接口生成实时深度洞察
    const res = await aiApi.generateMatchReport({ matchData })
    
    // 解析 AI 返回的内容
    let aiReport = {}
    try {
      let jsonStr = typeof res === 'string' ? res : JSON.stringify(res)
      const jsonMatch = jsonStr.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        aiReport = JSON.parse(jsonMatch[0])
      } else {
        throw new Error('AI 实时分析解析失败，请稍后重试')
      }
    } catch (parseError) {
      console.error('JSON Parse Error:', parseError)
      throw new Error('AI 实时分析解析失败，请稍后重试')
    }

    // 解析 events 数据
    let parsedEvents = [];
    if (aiReport.events && Array.isArray(aiReport.events)) {
      parsedEvents = aiReport.events;
    } else if (match.events) {
      try {
        parsedEvents = typeof match.events === 'string' ? JSON.parse(match.events) : match.events;
      } catch (e) {
        console.error('Failed to parse match.events:', e);
      }
    }

    // 填充完整数据
    reportData.value = {
      ...reportData.value,
      ...aiReport,
      events: parsedEvents
    }

  } catch (e) {
    console.error('AI Live Analysis failed:', e)
    showReportPopup.value = false // 失败时关闭弹窗
    uni.showToast({
      title: e.message || 'AI 实时分析不可用，请稍后再试',
      icon: 'none'
    })
  } finally {
    aiReportLoading.value = false
  }
}

const openMatchSummary = async (match) => {
  // 提前准备好头部数据并打开弹窗显示 Loading 状态
  reportData.value = {
    homeName: match.homeTeam?.name,
    homeLogo: match.homeTeam?.logoUrl,
    homeScore: match.homeScore,
    awayName: match.awayTeam?.name,
    awayLogo: match.awayTeam?.logoUrl,
    awayScore: match.awayScore,
    matchTime: match.matchTime,
    competition: match.competitionName,
    round: match.round,
    location: match.venueName || '未知场地',
    date: formatMatchDate(match.matchTime),
    statusName: '已完赛',
    isPrediction: false,
    isLive: false
  }
  showReportPopup.value = true
  aiReportLoading.value = true

  try {
    // 准备比赛数据，供 AI 分析 (增加更详细的上下文)
    const matchData = `
      赛事：${match.competitionName}
      轮次：${match.round}
      时间：${formatMatchDate(match.matchTime)}
      地点：${match.venueName || '未知'}
      比分：${match.homeTeam?.name} ${match.homeScore} - ${match.awayScore} ${match.awayTeam?.name}
      状态：已完赛
      主队：${match.homeTeam?.name}
      客队：${match.awayTeam?.name}
      真实技术统计(stats): ${match.stats ? match.stats : '暂无'}
      真实关键事件(events): ${match.events ? match.events : '暂无'}
    `.trim()

    // 调用 AI 接口生成战报
    const res = await aiApi.generateMatchReport({ matchData })
    
    // 解析 AI 返回的内容 (request.js 已经处理了 code === 200，res 现在就是 data 字段，即 AI 返回的字符串)
    let aiReport = {}
    try {
      // 增强型 JSON 解析逻辑：处理 AI 偶尔返回 Markdown 代码块的情况
      let jsonStr = typeof res === 'string' ? res : JSON.stringify(res)
      
      // 尝试匹配 JSON 对象部分 (防止 AI 返回多余的文字或 Markdown 标签)
      const jsonMatch = jsonStr.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        jsonStr = jsonMatch[0]
      }
      
      aiReport = JSON.parse(jsonStr)
    } catch (e) {
      console.error('AI Report parse failed:', e, res)
      throw new Error('AI 战报解析失败，请稍后重试')
    }

    // 解析 events 数据
    let parsedEvents = [];
    if (aiReport.events && Array.isArray(aiReport.events)) {
      parsedEvents = aiReport.events;
    } else if (match.events) {
      try {
        parsedEvents = typeof match.events === 'string' ? JSON.parse(match.events) : match.events;
      } catch (e) {
        console.error('Failed to parse match.events:', e);
      }
    }

    // 填充基础信息并合并 AI 生成的内容
    reportData.value = {
      homeName: match.homeTeam?.name || '主队',
      homeLogo: getFullImageUrl(match.homeTeam?.logoUrl),
      homeScore: match.homeScore,
      awayName: match.awayTeam?.name || '客队',
      awayLogo: getFullImageUrl(match.awayTeam?.logoUrl),
      awayScore: match.awayScore,
      competition: match.competitionName || '英超',
      round: match.round || '常规赛',
      date: formatMatchDate(match.matchTime),
      location: match.venueName || '体育场',
      // AI 生成的内容，增加空值容错
      summary: {
        text: aiReport.summary?.text || `${match.homeTeam?.name} 与 ${match.awayTeam?.name} 联手奉献了一场精彩对决。`,
        tone: aiReport.summary?.tone || '激烈对抗',
        emoji: aiReport.summary?.emoji || '🔥'
      },
      insights: aiReport.insights || [
        { title: '全场综述', description: '双方打出了极高的战术水准，场面非常胶着。' }
      ],
      attackArea: aiReport.attackArea || { home: 33, middle: 34, away: 33 },
      mvp: {
        name: aiReport.mvp?.name || '待定',
        reason: aiReport.mvp?.reason || '全场表现最稳定的球员。'
      },
      darkHorse: {
        name: aiReport.darkHorse?.name || '无',
        reason: aiReport.darkHorse?.reason || '本场比赛暂无显著黑马。'
      },
      stats: {
        homePossession: aiReport.stats?.homePossession || 50,
        awayPossession: aiReport.stats?.awayPossession || 50,
        homeShots: aiReport.stats?.homeShots || 0,
        awayShots: aiReport.stats?.awayShots || 0,
        homeXG: aiReport.stats?.homeXG || 0,
        awayXG: aiReport.stats?.awayXG || 0,
        homeTackles: aiReport.stats?.homeTackles || 0,
        awayTackles: aiReport.stats?.awayTackles || 0,
        homePassSuccess: aiReport.stats?.homePassSuccess || 0,
        awayPassSuccess: aiReport.stats?.awayPassSuccess || 0
      },
      events: parsedEvents,
      engagement: {
        voteTopic: aiReport.engagement?.voteTopic || '你对本场比赛的判罚有何看法？'
      }
    }
  } catch (e) {
    console.error('获取 AI 战报失败:', e)
    showReportPopup.value = false // 失败时关闭弹窗
    uni.showToast({
      title: '生成战报失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    aiReportLoading.value = false
  }
}

const dates = ref([])
  const activeDateIndex = ref(0)
  const currentYearMonth = ref('')
  let refreshTimer = null

const handleAvatarError = () => {
  userAvatar.value = '/static/default-avatar.png'
}

const getUserProfile = async () => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) {
      userAvatar.value = '/static/default-avatar.png'
      return
    }
    const res = await userApi.getProfile()
    // 兼容：res 直接是数据，或者是包含 data 属性的对象
    const userData = res?.data || res
    if (userData && userData.avatar) {
      userAvatar.value = getFullImageUrl(userData.avatar)
    } else {
      userAvatar.value = '/static/default-avatar.png'
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
    userAvatar.value = '/static/default-avatar.png'
  }
}

const goToProfile = () => {
  uni.switchTab({
    url: '/pages/my/my'
  })
}

onShow(() => {
  uni.hideTabBar()
  getUserProfile()
})

// 初始化日期列表（前后3天）
const initDates = () => {
  const list = []
  const today = new Date()
  
  // 前3天
  for (let i = 3; i >= 1; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    list.push({
      week: getWeekName(d),
      day: d.getDate(),
      fullDate: formatFullDate(d),
      isToday: false
    })
  }
  
  // 今天
  list.push({
    week: '今天',
    day: today.getDate(),
    fullDate: formatFullDate(today),
    isToday: true
  })
  
  // 后3天
  for (let i = 1; i <= 3; i++) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    list.push({
      week: getWeekName(d),
      day: d.getDate(),
      fullDate: formatFullDate(d),
      isToday: false
    })
  }
  
  dates.value = list
  // 默认选中今天 (索引为3)
  activeDateIndex.value = 3
  updateCurrentYearMonth(today)
}

const updateCurrentYearMonth = (date) => {
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  currentYearMonth.value = `${y}年${m}月`
}

const getWeekName = (date) => {
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weeks[date.getDay()]
}

const formatDate = (date) => {
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${m < 10 ? '0' + m : m}-${d < 10 ? '0' + d : d}`
}

const formatFullDate = (date) => {
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${y}-${m < 10 ? '0' + m : m}-${d < 10 ? '0' + d : d}`
}

const formatMatchTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const h = date.getHours()
  const m = date.getMinutes()
  return `${h < 10 ? '0' + h : h}:${m < 10 ? '0' + m : m}`
}

const formatMatchDate = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const m = date.getMonth() + 1
  const d = date.getDate()
  return `${m < 10 ? '0' + m : m}-${d < 10 ? '0' + d : d}`
}

const handleImageError = (team) => {
  if (team) {
    team.logoUrl = '/static/teams/generic_logo.png'
  }
}

const goToSearch = () => {
  uni.navigateTo({
    url: '/pages/search/search'
  })
}

// 获取比赛数据
const fetchMatches = async () => {
  try {
    const dateItem = dates.value[activeDateIndex.value]
    if (!dateItem) return
    
    console.log('Fetching matches for date:', dateItem.fullDate)
    const res = await matchApi.getByDate(dateItem.fullDate)
    
    // 兼容：res 直接是数组，或者是包含 records/data 的对象
    const actualMatches = Array.isArray(res) 
      ? res 
      : (res?.records || res?.data || [])
    
    console.log('Fetched matches:', actualMatches.length)
    
    if (Array.isArray(actualMatches) && actualMatches.length > 0) {
      // 状态码兼容：0:未开始, 1:进行中, 2:已结束 (请根据后端实际情况调整)
      liveMatches.value = actualMatches.filter(m => m.status === 1 || m.status === '1')
      upcomingMatches.value = actualMatches.filter(m => m.status === 0 || m.status === '0')
      finishedMatches.value = actualMatches.filter(m => m.status === 2 || m.status === '2')
    } else {
      liveMatches.value = []
      upcomingMatches.value = []
      finishedMatches.value = []
    }
  } catch (e) {
    console.error('获取赛程失败:', e)
    // 只有在非自动刷新时才弹出 Toast，避免干扰
    if (!refreshTimer) {
      uni.showToast({ title: '赛程加载失败', icon: 'none' })
    }
  }
}

const selectDate = (index) => {
  activeDateIndex.value = index
  const selectedDate = new Date(dates.value[index].fullDate)
  updateCurrentYearMonth(selectedDate)
  fetchMatches()
  
  // 切换日期后也开启自动刷新，以便在非“今天”页面停留时也能及时看到数据同步
  startAutoRefresh()
}

// 自动刷新逻辑
const startAutoRefresh = () => {
  stopAutoRefresh()
  // 保持后台定时刷新，获取最新数据（无论是否在“今天”标签）
  refreshTimer = setInterval(fetchMatches, 30000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 页面加载
onMounted(() => {
  initDates()
  fetchMatches()
})
</script>

<style lang="scss" scoped>
.schedule-container {
  height: 100vh; /* 固定高度为视口高度 */
  background-color: var(--bg-main);
  color: var(--text-main);
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  margin: 0 auto;
  overflow: hidden; /* 防止容器本身滚动 */
  box-sizing: border-box;
  transition: background-color 0.3s, color 0.3s;
  
  /* #ifdef H5 */
  max-width: 500px;
  /* #endif */
}

.content-scroll {
  flex: 1;
  height: 0; /* 必须：强制 flex 计算生效 */
  width: 100%;
  box-sizing: border-box;
}

.bottom-placeholder {
  height: calc(120rpx + env(safe-area-inset-bottom));
  width: 100%;
}

.status-bar {
  width: 100%;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 999;
  background-color: #1a1811;
  
  /* #ifdef H5 */
  left: 50%;
  transform: translateX(-50%);
  max-width: 500px;
  /* #endif */
}

/* 赛程卡片基础样式重构 */
.card-footer-summary-gold {
  margin: 0 24rpx 24rpx;
  position: relative;
  overflow: hidden;
  border-radius: 20rpx;

  .btn-inner {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24rpx;
    background: linear-gradient(90deg, rgba(212, 175, 55, 0.1), rgba(212, 175, 55, 0.05));
    border: 1rpx solid rgba(212, 175, 55, 0.2);
    border-radius: 20rpx;
    position: relative;
    z-index: 2;

    .ai-sparkle {
      font-size: 32rpx;
      color: #d4af37;
      margin-right: 16rpx;
      animation: aiSparkle 2s infinite;
    }

    .btn-text {
      font-size: 24rpx;
      font-weight: 700;
      color: #d4af37;
      letter-spacing: 1rpx;
    }

    .arrow {
      font-size: 32rpx;
      color: #d4af37;
      opacity: 0.5;
      margin-left: 8rpx;
    }
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

.match-card {
  margin: 24rpx;
  border-radius: 32rpx;
  overflow: hidden;
  position: relative;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.6);
  border: 1rpx solid rgba(212, 175, 55, 0.1);
  background: linear-gradient(180deg, rgba(30, 28, 20, 0.95), rgba(15, 15, 15, 0.98));

  &.live-card {
    border: 1rpx solid rgba(212, 175, 55, 0.4);
    box-shadow: 0 12rpx 60rpx rgba(212, 175, 55, 0.15);
    background: linear-gradient(180deg, rgba(30, 28, 20, 0.95), rgba(15, 15, 15, 1));
    animation: goldBreathe 4s infinite ease-in-out;
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: radial-gradient(circle at 50% 0%, rgba(212, 175, 55, 0.1), transparent 70%);
      pointer-events: none;
    }
    
    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 2rpx;
      background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.5), transparent);
    }
  }
}

.card-footer-summary {
  margin: 0 24rpx 24rpx;
  padding: 24rpx;
  background: linear-gradient(90deg, rgba(212, 175, 55, 0.08), transparent);
  border-radius: 20rpx;
  border: 1rpx solid rgba(212, 175, 55, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.98);
    background: rgba(212, 175, 55, 0.15);
  }

  .summary-btn-content {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .summary-icon {
      font-size: 32rpx;
      color: #d4af37;
      animation: iconPulse 2s infinite;
    }

    .summary-label {
      flex: 1;
      font-size: 24rpx;
      font-weight: 600;
      color: #d4af37;
      letter-spacing: 2rpx;
    }

    .chevron {
      font-size: 28rpx;
      color: #d4af37;
      opacity: 0.5;
    }
  }
}

@keyframes iconPulse {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; }
  100% { transform: scale(1); opacity: 0.8; }
}

.card-header-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx 16rpx;

  .live-card & {
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
    padding: 24rpx 32rpx;
  }
  
  .competition-info {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .dot {
      width: 6rpx;
      height: 6rpx;
      background: var(--text-theme-main);
      border-radius: 50%;
      opacity: 0.5;
    }

    .competition-text {
      font-size: 22rpx;
      letter-spacing: 1rpx;
      text-transform: uppercase;
    }
  }

  .live-badge {
    display: flex;
    align-items: center;
    background: rgba(255, 46, 99, 0.12);
    padding: 8rpx 20rpx;
    border-radius: 12rpx;
    border: 1rpx solid rgba(255, 46, 99, 0.2);
    box-shadow: 0 4rpx 12rpx rgba(255, 46, 99, 0.1);

    .live-dot {
      width: 12rpx;
      height: 12rpx;
      background: #ff2e63;
      border-radius: 50%;
      margin-right: 12rpx;
      box-shadow: 0 0 12rpx #ff2e63;
      animation: liveBreathe 1.5s infinite;
    }

    .live-text {
      color: #ff2e63;
      font-size: 22rpx;
      font-weight: 900;
      font-family: 'DIN Alternate', sans-serif;
      letter-spacing: 1rpx;
    }
  }
}

@keyframes liveBreathe {
  0% { transform: scale(1); opacity: 0.8; box-shadow: 0 0 8rpx #ff2e63; }
  50% { transform: scale(1.2); opacity: 1; box-shadow: 0 0 20rpx #ff2e63; }
  100% { transform: scale(1); opacity: 0.8; box-shadow: 0 0 8rpx #ff2e63; }
}

.card-footer-ai-gold {
  margin: 0 24rpx 24rpx;
  position: relative;
  overflow: hidden;
  border-radius: 24rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: scale(0.98);
  }

  .btn-inner {
    padding: 24rpx;
    background: linear-gradient(135deg, rgba(212, 175, 55, 0.12), rgba(212, 175, 55, 0.04));
    border: 1rpx solid rgba(212, 175, 55, 0.2);
    border-radius: 24rpx;
    position: relative;
    z-index: 2;
    backdrop-filter: blur(4px);
  }

  .ai-glow {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.15), transparent);
    animation: aiSweep 3s infinite;
    z-index: 1;
  }

  .ai-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .ai-title {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .ai-spark {
        font-size: 32rpx;
        color: #d4af37;
        animation: aiSparkle 2s infinite;
      }

      .ai-label {
        font-size: 24rpx;
        font-weight: 800;
        color: #d4af37;
        letter-spacing: 1rpx;
        text-transform: uppercase;
      }
    }

    .ai-action {
      display: flex;
      align-items: center;
      gap: 4rpx;
      font-size: 20rpx;
      color: #d4af37;
      opacity: 0.7;
      font-weight: 600;

      .material-icons {
        font-size: 24rpx;
      }
    }
  }

  .ai-content {
    .ai-progress-bar {
      height: 12rpx;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 100rpx;
      display: flex;
      overflow: hidden;
      margin-bottom: 20rpx;
      border: 1rpx solid rgba(255, 255, 255, 0.05);

      .progress-segment {
        height: 100%;
        position: relative;
        transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);

        &.home {
          background: linear-gradient(90deg, #ff4d4f, #ff7875);
          .glow-bar {
            position: absolute;
            top: 0;
            right: 0;
            width: 20rpx;
            height: 100%;
            background: rgba(255, 77, 79, 0.5);
            filter: blur(4rpx);
          }
        }
        &.draw { background: rgba(255, 255, 255, 0.15); }
        &.away {
          background: linear-gradient(90deg, #40a9ff, #69c0ff);
          .glow-bar {
            position: absolute;
            top: 0;
            left: 0;
            width: 20rpx;
            height: 100%;
            background: rgba(64, 169, 255, 0.5);
            filter: blur(4rpx);
          }
        }
      }
    }

    .ai-stats {
      display: flex;
      justify-content: space-between;

      .stat-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4rpx;

        .stat-label {
          font-size: 18rpx;
          color: rgba(212, 175, 55, 0.5);
          font-weight: 600;
        }

        .stat-value {
          font-size: 24rpx;
          font-weight: 800;
          font-family: 'DIN Alternate', sans-serif;
          
          &.home-text { color: #ff4d4f; }
          &.draw-text { color: rgba(255, 255, 255, 0.6); }
          &.away-text { color: #40a9ff; }
        }
      }
    }
  }
}

.card-body {
  padding: 20rpx 40rpx 40rpx;

  .teams-score {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 0;

    .live-card & {
      padding: 32rpx 0;
    }
    
    &.mini {
      padding: 10rpx 0;
      
      .team-name {
        font-size: 28rpx;
      }
      
      .score-num {
        font-size: 48rpx;
      }
    }

    .team-side {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16rpx;
      max-width: 220rpx;

      .logo-wrapper {
        width: 110rpx;
        height: 110rpx;
        background: rgba(255, 255, 255, 0.03);
        border-radius: 50%;
        padding: 16rpx;
        box-shadow: inset 0 0 20rpx rgba(0, 0, 0, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;

        &.small {
          width: 80rpx;
          height: 80rpx;
          padding: 12rpx;
        }

        image {
          width: 100%;
          height: 100%;
          filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.3));
        }
      }

      .team-name {
        font-size: 28rpx;
        font-weight: 700;
        text-align: center;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 100%;
        letter-spacing: 0.5rpx;

        .live-card & {
          font-size: 26rpx;
          color: #fff;
        }
      }
    }

    .score-center {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;

    .score-display {
      display: flex;
      align-items: center;
      gap: 20rpx;
      font-family: 'DIN Alternate', sans-serif;

      .score-num {
        font-size: 64rpx;
        font-weight: 800;
        font-family: 'DIN Alternate', 'DINCondensed-Bold', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
        color: #fff;
        text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.5);
        transition: all 0.3s ease;

        .live-card & {
          color: #fff;
          text-shadow: 0 0 20rpx rgba(255, 255, 255, 0.4);
          animation: liveScoreBreathe 2s infinite ease-in-out;
        }

        &.winner {
          color: #ff4d4f;
          text-shadow: 0 0 15rpx rgba(255, 77, 79, 0.4);
        }
      }

        .score-divider {
          font-size: 40rpx;
          opacity: 0.3;
          font-weight: 400;
          margin-top: -8rpx;
        }
      }

      .status-tag {
        background: rgba(255, 255, 255, 0.08);
        padding: 4rpx 20rpx;
        border-radius: 100rpx;
        font-size: 20rpx;
        font-weight: 500;
        color: rgba(255, 255, 255, 0.6);
      }
    }
  }
}

/* 已完赛卡片重构 */
.finished-card {
  border: 1rpx solid rgba(212, 175, 55, 0.15);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.4);
  background: linear-gradient(180deg, rgba(30, 28, 20, 0.8), rgba(20, 20, 20, 0.95));

  .card-header-line {
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
    padding: 24rpx 32rpx;
    
    .ai-insight-badge {
      display: flex;
      align-items: center;
      gap: 8rpx;
      background: rgba(212, 175, 55, 0.1);
      padding: 4rpx 16rpx;
      border-radius: 8rpx;
      margin-right: 16rpx;
      border: 1rpx solid rgba(212, 175, 55, 0.2);

      .badge-icon {
        font-size: 20rpx;
        color: #d4af37;
      }

      text {
        font-size: 18rpx;
        color: #d4af37;
        font-weight: 800;
        letter-spacing: 1rpx;
      }
    }
  }

  .teams-score-finished {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10rpx 0;

    .team {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16rpx;

      .logo-box {
        width: 80rpx;
        height: 80rpx;
        padding: 12rpx;
        background: rgba(255, 255, 255, 0.03);
        border-radius: 50%;
        border: 1rpx solid rgba(255, 255, 255, 0.05);
        
        image {
          width: 100%;
          height: 100%;
        }
      }

      .name {
        font-size: 26rpx;
        font-weight: 700;
        color: #fff;
      }
    }

    .score-box {
      display: flex;
      align-items: center;
      gap: 24rpx;

      .score-val {
        font-size: 60rpx;
        font-weight: 800;
        font-family: 'DIN Alternate', sans-serif;
        color: rgba(255, 255, 255, 0.3);

        &.home-win, &.away-win {
          color: #fff;
          text-shadow: 0 0 20rpx rgba(255, 255, 255, 0.2);
        }
      }

      .score-sep {
        font-size: 32rpx;
        color: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

@keyframes aiSparkle {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 0.8; }
}

.live-badge-gold {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 16rpx;
  background: rgba(212, 175, 55, 0.1);
  border: 1rpx solid rgba(212, 175, 55, 0.3);
  border-radius: 100rpx;
  backdrop-filter: blur(4px);

  .live-dot-pulse {
    width: 10rpx;
    height: 10rpx;
    background: #ff4d4f;
    border-radius: 50%;
    position: relative;
    box-shadow: 0 0 12rpx #ff4d4f;
    animation: livePulse 1.5s infinite;

    &::after {
      content: '';
      position: absolute;
      top: -4rpx;
      left: -4rpx;
      right: -4rpx;
      bottom: -4rpx;
      border: 2rpx solid #ff4d4f;
      border-radius: 50%;
      animation: liveRipple 1.5s infinite;
    }
  }

  .live-text {
    font-size: 20rpx;
    font-weight: 900;
    color: #d4af37;
    font-family: 'DIN Alternate', sans-serif;
    letter-spacing: 0.5rpx;
  }
}

.ai-live-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 12rpx;
  background: linear-gradient(90deg, rgba(212, 175, 55, 0.2), rgba(212, 175, 55, 0.05));
  border-radius: 8rpx;
  margin-right: 12rpx;
  border-left: 4rpx solid #d4af37;

  .badge-icon {
    font-size: 20rpx;
    color: #d4af37;
    animation: aiSparkle 2s infinite;
  }

  text {
    font-size: 16rpx;
    font-weight: 900;
    color: #d4af37;
    letter-spacing: 1rpx;
  }
}

@keyframes livePulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes liveRipple {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2.5); opacity: 0; }
}

@keyframes aiSparkle {
  0% { transform: scale(1) rotate(0); opacity: 0.8; }
  50% { transform: scale(1.2) rotate(180deg); opacity: 1; }
  100% { transform: scale(1) rotate(360deg); opacity: 0.8; }
}

@keyframes aiSweep {
  0% { left: -100%; }
  50% { left: 100%; }
  100% { left: 100%; }
}

@keyframes goldBreathe {
  0% { border-color: rgba(212, 175, 55, 0.2); box-shadow: 0 0 10rpx rgba(212, 175, 55, 0.05); }
  50% { border-color: rgba(212, 175, 55, 0.5); box-shadow: 0 0 25rpx rgba(212, 175, 55, 0.2); }
  100% { border-color: rgba(212, 175, 55, 0.2); box-shadow: 0 0 10rpx rgba(212, 175, 55, 0.05); }
}

@keyframes liveScoreBreathe {
  0% { opacity: 0.8; text-shadow: 0 0 10rpx rgba(255, 255, 255, 0.2); }
  50% { opacity: 1; text-shadow: 0 0 25rpx rgba(255, 255, 255, 0.5); }
  100% { opacity: 0.8; text-shadow: 0 0 10rpx rgba(255, 255, 255, 0.2); }
}

.header-placeholder {
  height: calc(var(--status-bar-height) + 100rpx + 220rpx); /* statusBarHeight + nav-bar(100rpx) + calendar(220rpx) */
  width: 100%;
}

/* AI Modal Styles */
.ai-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
}

.ai-modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(10px);
}

.ai-modal-content {
  position: relative;
  width: 100%;
  max-width: 600rpx;
  background-color: var(--card-bg);
  border-radius: 40rpx;
  border: 1px solid var(--border-main);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalFadeIn 0.3s ease-out;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.modal-header {
  padding: 30rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-main);

  .header-title {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .ai-icon {
      font-size: 48rpx;
      color: #f9d406;
    }

    .title-text {
      font-size: 32rpx;
      font-weight: 700;
      color: var(--text-main);
    }
  }

  .close-btn {
    color: var(--text-secondary);
    padding: 10rpx;
  }
}

.modal-body {
  padding: 40rpx;
  min-height: 300rpx;
  max-height: 60vh;

  .ai-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60rpx 0;

    .loading-spinner {
      width: 64rpx;
      height: 64rpx;
      border: 6rpx solid rgba(249, 212, 6, 0.1);
      border-top-color: #f9d406;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 24rpx;
    }

    .loading-text {
      font-size: 26rpx;
      color: var(--text-secondary);
      text-align: center;
    }
  }

  .ai-result-scroll {
    height: 100%;
  }

  .ai-analysis-text {
    font-size: 28rpx;
    line-height: 1.6;
    color: var(--text-main);
    white-space: pre-wrap;
  }
}

.modal-footer {
  padding: 30rpx 40rpx;
  border-top: 1px solid var(--border-main);

  .modal-close-btn {
    width: 100%;
    height: 88rpx;
    background-color: #f9d406;
    color: #000;
    border-radius: 20rpx;
    font-size: 30rpx;
    font-weight: 700;
    display: flex;
    justify-content: center;
    align-items: center;
    border: none;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.nav-bar {
  width: 100%;
  height: 100rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  box-sizing: border-box;
  background-color: #1a1811;
  border-bottom: 1rpx solid var(--border-main);
}

.calendar-fixed-area {
  width: 100%;
  border-bottom: 1rpx solid var(--border-main);
  background-color: #1a1811;
  padding-bottom: 16rpx;
}

.league-selector {
  width: 100%;
  white-space: nowrap;
  padding: 16rpx 24rpx;
  box-sizing: border-box;

  .league-list {
    display: inline-flex;
    padding-right: 48rpx; /* 留出右侧空间 */
  }

  .league-item {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12rpx 32rpx;
    margin-right: 20rpx;
    border-radius: 32rpx;
    background: rgba(255, 255, 255, 0.05);
    border: 1rpx solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #d4af37 0%, #aa8c2c 100%);
      border-color: #d4af37;
      box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.3);
    }

    .league-icon {
      font-size: 28rpx;
      margin-right: 8rpx;
    }

    .league-name {
      font-size: 26rpx;
      font-weight: 500;
    }
  }
}

.logo-area {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-shrink: 0;

  .logo-icon {
    width: 56rpx;
    height: 56rpx;
    margin-right: 12rpx;
    display: flex;
    align-items: center;

    .logo-img {
      width: 100%;
      height: 100%;
    }
  }

  .logo-text {
    font-size: 32rpx;
    font-weight: 800;
    letter-spacing: 2rpx;
    line-height: 1;
    color: #d4af37;
    
    &.italic {
      font-style: italic;
    }
    
    .highlight {
      color: #fff;
      margin-left: 6rpx;
    }
  }
}

.nav-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
  flex-shrink: 0;

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60rpx;
    height: 60rpx;
  }

  .avatar-box {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    border: 2rpx solid rgba(249, 212, 6, 0.3);
    overflow: hidden;

    .avatar {
      width: 100%;
      height: 100%;
    }
  }
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx 20rpx;
  background-color: var(--bg-main);

  .current-month {
    font-size: 36rpx;
    font-weight: 800;
    color: var(--text-main);
    letter-spacing: 1rpx;
  }

  .view-calendar-btn {
    font-size: 26rpx;
    font-weight: 600;
    color: #ff2e63;
  }
}

.date-selector {
  background-color: var(--bg-main);
  padding-bottom: 30rpx;
  margin-bottom: 20rpx;

  .date-list {
    display: flex;
    padding: 0 32rpx;
    gap: 24rpx;
  }

  .date-item {
    flex-shrink: 0;
    width: 100rpx;
    height: 140rpx;
    border-radius: 28rpx;
    background-color: var(--card-bg);
    border: 1px solid var(--border-main);
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    position: relative;

    .date-week {
      font-size: 22rpx;
      color: var(--text-secondary);
      margin-bottom: 12rpx;
      font-weight: 500;
    }

    .date-day {
      font-size: 32rpx;
      font-weight: 800;
      color: var(--text-main);
      margin-bottom: 4rpx;
    }

    .date-dot {
      width: 8rpx;
      height: 8rpx;
      background-color: #fff;
      border-radius: 50%;
      position: absolute;
      bottom: 20rpx;
    }

    &.active {
      background: linear-gradient(180deg, #ff4b5c 0%, #ff2e63 100%);
      border-color: transparent;
      box-shadow: 0 8rpx 20rpx rgba(255, 46, 99, 0.3);
      transform: translateY(-4rpx);

      .date-week {
        color: rgba(255, 255, 255, 0.9);
      }
      
      .date-day {
        font-size: 36rpx;
        color: #fff;
      }
    }
  }
}

.content-scroll {
  padding: 40rpx 32rpx 0;
}

.section {
  margin-bottom: 48rpx;

  .section-title {
    font-size: 28rpx;
    font-weight: 700;
    color: var(--text-secondary);
    margin-bottom: 24rpx;
    text-transform: uppercase;
    letter-spacing: 2rpx;
  }
}

.live-badge {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #4a0404;
  padding: 8rpx 24rpx;
  border-bottom-left-radius: 24rpx;

  .live-text {
    font-size: 20rpx;
    font-weight: 700;
    color: #fff;
    text-transform: uppercase;
    animation: pulse 2s infinite;
  }
}

.card-body {
  padding: 40rpx;

  .match-info {
      margin-bottom: 32rpx;
      .competition-text {
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }

    .teams-score {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 40rpx 0;

      .team-side {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        overflow: hidden;

        .logo-container {
          width: 90rpx;
          height: 90rpx;
          background-color: #fff;
          border-radius: 50%;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-bottom: 16rpx;
          box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
          flex-shrink: 0;

          image {
            width: 64rpx;
            height: 64rpx;
          }
        }

        .team-name {
          font-size: 28rpx;
          font-weight: 800;
          color: var(--text-main);
          text-align: center;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          width: 100%;
          padding: 0 10rpx;
          box-sizing: border-box;
        }
      }

      .score-center {
        flex: 0 0 160rpx; /* 固定宽度，不参与 flex 缩放 */
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .score-text {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 12rpx; /* 缩小间距 */
          margin-bottom: 12rpx;

          .num {
            font-size: 64rpx; /* 稍微缩小字号 */
            font-weight: 900;
            color: var(--text-main);
            width: 60rpx; /* 缩小容器宽度 */
            text-align: center;
            font-family: 'DIN Alternate', 'PingFang SC', sans-serif;
          }

          .divider {
            font-size: 32rpx; /* 缩小分隔符 */
            color: var(--text-secondary);
            font-weight: 300;
          }
        }

        .status-badge {
          background: linear-gradient(90deg, #ff2e63 0%, #ff4b5c 100%);
          color: #fff;
          font-size: 20rpx; /* 缩小字号 */
          padding: 4rpx 16rpx; /* 缩小内边距 */
          border-radius: 24rpx;
          font-weight: 700;
          box-shadow: 0 4rpx 12rpx rgba(255, 46, 99, 0.4);
          white-space: nowrap;
        }
      }
    }
  }

  .footer-right {
    display: flex;
    align-items: center;
    gap: 12rpx;

    &.mini {
      justify-content: flex-end;
    }

    .ai-label {
      font-size: 22rpx;
      font-weight: 500;
      color: var(--text-secondary);
      opacity: 0.8;
    }

    .ai-probs {
      display: flex;
      align-items: center;
      gap: 16rpx;
      background: rgba(255, 255, 255, 0.05);
      padding: 4rpx 16rpx;
      border-radius: 20rpx;
      border: 1rpx solid rgba(255, 255, 255, 0.1);
    }

    .prob-item {
      display: flex;
      align-items: center;
      gap: 4rpx;

      .prob-label {
        font-size: 20rpx;
        color: var(--text-secondary);
        font-weight: 400;
      }

      .prob-value {
        font-size: 24rpx;
        color: #f2b90d;
        font-weight: 700;
        min-width: 50rpx;
        text-align: right;
      }
    }
  }

  .card-footer {
    background-color: rgba(0, 0, 0, 0.05);
    padding: 24rpx 40rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 24rpx;
    border-top: 1rpx solid var(--border-main);

    .footer-left {
      display: flex;
      align-items: center;
      gap: 16rpx;
      .footer-text {
        color: var(--text-secondary);
      }
    }
  }

  /* 迷你卡片 AI 样式 */
  .match-card-mini {
  margin: 20rpx 24rpx;
  background: linear-gradient(180deg, rgba(30, 28, 20, 0.9), rgba(15, 15, 15, 0.98));
  border-radius: 24rpx;
  padding: 24rpx;
  border: 1rpx solid rgba(212, 175, 55, 0.1);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;

  .mini-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .indicator {
        width: 4rpx;
        height: 20rpx;
        background: #d4af37;
        border-radius: 4rpx;
        opacity: 0.5;
      }

      .match-meta {
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.5);
      }
    }
  }

  .mini-body {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10rpx 0;

    .mini-team {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 16rpx;
      max-width: 40%;

      &.reverse {
        flex-direction: row-reverse;
      }

      .mini-logo {
        width: 50rpx;
        height: 50rpx;
        filter: drop-shadow(0 4rpx 8rpx rgba(0, 0, 0, 0.3));
        image {
          width: 100%;
          height: 100%;
        }
      }

      .mini-name {
        font-size: 26rpx;
        font-weight: 700;
        color: #fff;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .vs-text {
      font-size: 20rpx;
      font-weight: 900;
      color: rgba(212, 175, 55, 0.3);
      font-style: italic;
    }
  }
}

.mini-footer-ai-gold {
  margin-top: 24rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.08), rgba(212, 175, 55, 0.03));
  border: 1rpx solid rgba(212, 175, 55, 0.15);
  border-radius: 16rpx;
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(4px);

  .ai-glow {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.08), transparent);
    animation: aiSweep 3s infinite;
  }

  .ai-progress-container {
    position: relative;
    z-index: 2;
  }

  .ai-progress-bar {
    height: 10rpx;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 100rpx;
    display: flex;
    overflow: hidden;
    margin-bottom: 16rpx;
    border: 1rpx solid rgba(255, 255, 255, 0.05);

    .progress-segment {
      height: 100%;
      transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;

      &.home { 
        background: linear-gradient(90deg, #ff4d4f, #ff7875);
      }
      &.draw { background: rgba(255, 255, 255, 0.1); }
      &.away { 
        background: linear-gradient(90deg, #40a9ff, #69c0ff);
      }
    }
  }

  .ai-labels-mini {
    display: flex;
    justify-content: space-between;
    
    .label-text {
      font-size: 18rpx;
      font-weight: 800;
      font-family: 'DIN Alternate', sans-serif;
      letter-spacing: 0.5rpx;
      
      &.home { color: #ff7875; }
      &.draw { color: rgba(255, 255, 255, 0.5); }
      &.away { color: #69c0ff; }
    }
  }
}

.no-match {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  opacity: 0.5;
  
  .no-match-text {
    margin-top: 24rpx;
    font-size: 26rpx;
    letter-spacing: 2rpx;
  }
}

@keyframes breathe {
  0% { opacity: 0.4; transform: scale(0.9); }
  50% { opacity: 1; transform: scale(1.1); box-shadow: 0 0 15rpx rgba(255, 77, 79, 0.6); }
  100% { opacity: 0.4; transform: scale(0.9); }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; text-shadow: 0 0 15rpx rgba(255, 215, 0, 0.8); }
  100% { transform: scale(1); opacity: 0.8; }
}

/* 底部导航占位 */
.safe-area-bottom {
  height: 160rpx;
}
</style>
