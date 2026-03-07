import { defineStore } from 'pinia';

export const useThemeStore = defineStore('theme', {
  state: () => ({
    theme: uni.getStorageSync('app_theme') || 'dark'
  }),
  actions: {
    setTheme(theme) {
      this.theme = theme;
      uni.setStorageSync('app_theme', theme);
    }
  }
});
