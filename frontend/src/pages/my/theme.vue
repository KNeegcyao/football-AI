<template>
  <view class="page-container min-h-screen flex flex-col font-display" :class="themeClass">
    <!-- Header -->
    <view class="sticky top-0 z-50 backdrop-blur-md px-4 pt-12 pb-4 flex flex-row items-center border-b border-theme-main bg-theme-main/95">
      <view @click="goBack" class="w-10 h-10 flex items-center justify-center rounded-full bg-theme-secondary mr-2">
        <text class="material-icons-round text-theme-main" style="font-size: 24px;">chevron_left</text>
      </view>
      <text class="text-xl font-bold tracking-tight text-theme-main">外观主题</text>
    </view>

    <scroll-view scroll-y class="flex-1 px-4 py-6">
      <!-- Theme Selection -->
      <view class="mb-8">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-3 block">选择主题</text>
        <view class="space-y-3">
          <view 
            v-for="theme in themes" 
            :key="theme.id"
            @click="selectTheme(theme.id)"
            class="w-full flex flex-row items-center justify-between p-4 rounded-xl border transition-all"
            :class="[
              themeStore.theme === theme.id ? 'border-[#f9d406] bg-[#f9d406]/5' : 'border-theme-main bg-theme-secondary',
              themeClass
            ]"
          >
            <view class="flex flex-row items-center">
              <view 
                class="w-10 h-10 rounded-lg flex items-center justify-center mr-3"
                :class="theme.bgColor"
              >
                <text class="material-icons-round" :class="theme.iconColor" style="font-size: 20px;">{{ theme.icon }}</text>
              </view>
              <view>
                <text class="font-medium block text-theme-main">{{ theme.name }}</text>
                <text class="text-xs text-theme-secondary">{{ theme.desc }}</text>
              </view>
            </view>
            <view 
              class="w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all"
              :class="themeStore.theme === theme.id ? 'border-[#f9d406] bg-[#f9d406]' : 'border-white/20'"
            >
              <text v-if="themeStore.theme === theme.id" class="material-icons-round text-[#1A1811]" style="font-size: 16px; font-weight: bold;">done</text>
            </view>
          </view>
        </view>
      </view>

      <view class="px-2">
        <text class="text-xs text-theme-secondary leading-relaxed italic">
          注：主题切换将实时应用到所有支持主题变量的页面。
        </text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { computed } from 'vue';
import { useThemeStore } from '@/store/theme';

const themeStore = useThemeStore();
const themeClass = computed(() => `theme-${themeStore.theme}`);

const themes = [
  {
    id: 'light',
    name: '浅色模式',
    desc: '明亮清爽，适合白天使用',
    icon: 'light_mode',
    bgColor: 'bg-gray-200',
    iconColor: 'text-gray-600'
  },
  {
    id: 'dark',
    name: '深色模式',
    desc: '极简深邃，保护视力',
    icon: 'dark_mode',
    bgColor: 'bg-zinc-800',
    iconColor: 'text-[#f9d406]'
  }
];

const goBack = () => {
  uni.navigateBack();
};

const selectTheme = (themeId) => {
  themeStore.setTheme(themeId);
  
  uni.showToast({
    title: '主题已更新',
    icon: 'success',
    duration: 1000
  });
};
</script>

<style scoped>
.font-display {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 局部主题类覆盖已不再需要，由全局 App.vue 管理 */

.page-container {
  background-color: var(--bg-main);
  color: var(--text-main);
}

.bg-theme-main { background-color: var(--bg-main); }
.bg-theme-secondary { background-color: var(--bg-secondary); }
.text-theme-main { color: var(--text-main); }
.text-theme-secondary { color: var(--text-secondary); }
.border-theme-main { border-color: var(--border-main); }
</style>
