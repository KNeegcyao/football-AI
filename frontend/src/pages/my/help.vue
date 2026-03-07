<template>
  <view class="page-container min-h-screen flex flex-col font-display" :class="themeClass">
    <!-- Header -->
    <view class="sticky top-0 z-50 px-4 pt-12 pb-4 flex flex-row items-center border-b border-white/5 header-bg">
      <view @click="goBack" class="w-10 h-10 flex items-center justify-center rounded-full bg-white/5 mr-2">
        <text class="material-icons-round" style="font-size: 24px;">chevron_left</text>
      </view>
      <text class="text-xl font-bold tracking-tight text-white">帮助与支持</text>
    </view>

    <scroll-view scroll-y class="flex-1 px-4 py-6">
      <!-- FAQ Section -->
      <view class="mb-8">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-3 block">常见问题</text>
        <view class="space-y-1">
          <view v-for="(item, index) in faqs" :key="index" @click="toggleFaq(index)" 
                class="w-full p-4 bg-white/5 rounded-xl border border-white/5 transition-all">
            <view class="flex flex-row items-center justify-between">
              <text class="font-medium pr-4">{{ item.question }}</text>
              <text class="material-icons-round text-gray-400 transition-transform duration-300" 
                    :style="{ transform: item.active ? 'rotate(180deg)' : 'rotate(0deg)' }">expand_more</text>
            </view>
            <view v-if="item.active" class="mt-3 pt-3 border-t border-white/5 text-sm text-gray-400 leading-relaxed">
              {{ item.answer }}
            </view>
          </view>
        </view>
      </view>

      <!-- Feedback Section -->
      <view class="mb-8">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-3 block">意见反馈</text>
        <view class="w-full p-4 bg-white/5 rounded-xl border border-white/5">
          <textarea v-model="feedback" placeholder="请描述您遇到的问题或建议..." 
                    class="w-full h-32 text-sm text-white placeholder-gray-500 bg-transparent" />
          <view @click="submitFeedback" class="mt-4 w-full gold-gradient py-3 rounded-xl flex items-center justify-center">
            <text class="text-[#1A1811] font-bold">提交反馈</text>
          </view>
        </view>
      </view>

      <!-- Contact Section -->
      <view class="mb-8">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-3 block">联系我们</text>
        <view class="grid grid-cols-2 gap-3">
          <view class="p-4 bg-white/5 rounded-xl border border-white/5 flex flex-col items-center gap-2">
            <view class="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-[#f9d406]">
              <text class="material-icons-round" style="font-size: 20px;">mail</text>
            </view>
            <text class="text-xs font-medium">官方邮箱</text>
            <text class="text-[10px] text-gray-500">support@pitchpulse.com</text>
          </view>
          <view class="p-4 bg-white/5 rounded-xl border border-white/5 flex flex-col items-center gap-2">
            <view class="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-[#f9d406]">
              <text class="material-icons-round" style="font-size: 20px;">qr_code_2</text>
            </view>
            <text class="text-xs font-medium">公众号</text>
            <text class="text-[10px] text-gray-500">搜索 PitchPulse</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useThemeStore } from '@/store/theme'

const themeStore = useThemeStore()
const themeClass = computed(() => `theme-${themeStore.theme}`)

const feedback = ref('')
const faqs = ref([
  { 
    question: '如何发布新帖子？', 
    answer: '在“社区”页面的右下角，点击金色的“+”按钮，即可进入发布页面。您可以选择图片或文字，并关联感兴趣的圈子。',
    active: false
  },
  { 
    question: '如何加入球队圈子？', 
    answer: '在“社区”页面的“热门圈子”栏目点击“查看全部”，找到您支持的球队并点击，进入圈子详情页后点击右上角的“加入”即可。',
    active: false
  },
  { 
    question: 'AI 比赛报告是如何生成的？', 
    answer: '我们基于实时比赛数据，结合先进的足球技战术模型，通过 AI 引擎深度分析后自动生成，通常在赛后 30 分钟内产出。',
    active: false
  }
])

const goBack = () => {
  uni.navigateBack()
}

const toggleFaq = (index) => {
  faqs.value[index].active = !faqs.value[index].active
}

const submitFeedback = () => {
  if (!feedback.value.trim()) {
    uni.showToast({ title: '请输入反馈内容', icon: 'none' })
    return
  }
  uni.showLoading({ title: '提交中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '提交成功，感谢反馈', icon: 'success' })
    feedback.value = ''
  }, 1000)
}
</script>

<style lang="scss" scoped>
.backdrop-blur-md {
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
.gold-gradient {
  background: linear-gradient(135deg, #D4AF37 0%, #FFD700 50%, #B8860B 100%);
}

/* 浅色模式适配 */
.theme-light {
  background-color: var(--bg-main);
  
  .header-bg {
    background-color: var(--bg-main);
    border-bottom: 1px solid var(--border-main);
    .text-white {
      color: var(--text-main) !important;
    }
  }
  
  .bg-white\/5 {
    background-color: #F3F4F6 !important;
    border: 1px solid #E5E7EB !important;
  }
  
  .text-white {
    color: var(--text-main) !important;
  }
  
  .text-gray-400 {
    color: var(--text-secondary) !important;
  }
  
  .border-white/5 {
    border-color: #E5E7EB !important;
  }
}
</style>
