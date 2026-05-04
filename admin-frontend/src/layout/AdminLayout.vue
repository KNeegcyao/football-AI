<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <el-icon size="28" color="#10b981" v-if="isCollapse"><Football /></el-icon>
        <template v-else>
          <el-icon size="24" color="#10b981"><Football /></el-icon>
          <span class="logo-text">绿茵集后台</span>
        </template>
      </div>
      
      <el-menu
        :default-active="$route.path"
        router
        :collapse="isCollapse"
        :collapse-transition="false"
        class="admin-menu"
      >
        <el-menu-item index="/dashboard" class="menu-item">
          <el-icon size="18"><DataAnalysis /></el-icon>
          <template #title>
            <span class="menu-title">数据看板</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/users" class="menu-item">
          <el-icon size="18"><User /></el-icon>
          <template #title>
            <span class="menu-title">用户管理</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/posts" class="menu-item">
          <el-icon size="18"><Document /></el-icon>
          <template #title>
            <span class="menu-title">帖子管理</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/comments" class="menu-item">
          <el-icon size="18"><ChatDotRound /></el-icon>
          <template #title>
            <span class="menu-title">评论管理</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/topics" class="menu-item">
          <el-icon size="18"><PriceTag /></el-icon>
          <template #title>
            <span class="menu-title">话题管理</span>
          </template>
        </el-menu-item>
        
        <el-menu-item index="/news" class="menu-item">
          <el-icon size="18"><Notification /></el-icon>
          <template #title>
            <span class="menu-title">资讯管理</span>
          </template>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <el-button 
          text 
          class="collapse-btn"
          @click="isCollapse = !isCollapse"
        >
          <el-icon size="18">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </el-button>
      </div>
    </el-aside>
    
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">
              <el-icon size="14"><HomeFilled /></el-icon>
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title || '首页' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-tooltip content="全屏" placement="bottom">
            <el-button text circle class="header-btn" @click="toggleFullscreen">
              <el-icon size="18"><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="36" src="/static/default-avatar.png" class="user-avatar" />
              <span class="user-name">管理员</span>
              <el-icon size="14"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-dropdown-menu">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { 
  DataAnalysis, User, Document, ChatDotRound, 
  PriceTag, Notification, Fold, Expand, 
  HomeFilled, FullScreen, ArrowDown, SwitchButton,
  Football
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  background: var(--bg-primary);
}

.sidebar {
  background: linear-gradient(180deg, var(--bg-secondary) 0%, rgba(15, 23, 42, 0.98) 100%);
  border-right: 1px solid var(--border-color);
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  position: relative;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid var(--border-color);
  padding: 0 20px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
  white-space: nowrap;
}

.admin-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 12px 8px;
}

.menu-item {
  border-radius: 10px;
  margin: 4px 0;
  height: 48px;
  color: var(--text-secondary);
  transition: all 0.25s ease;
}

.menu-item:hover {
  background: rgba(16, 185, 129, 0.1);
  color: var(--primary-400);
}

.menu-item.is-active {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(16, 185, 129, 0.1));
  color: var(--primary-400);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.15);
}

.menu-title {
  font-size: 14px;
  margin-left: 4px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
}

.collapse-btn {
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  color: var(--primary-400);
  background: rgba(16, 185, 129, 0.1);
}

.main-container {
  background: var(--bg-primary);
  overflow: hidden;
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  padding: 0 24px;
  backdrop-filter: blur(10px);
}

.header-left :deep(.el-breadcrumb__item) {
  color: var(--text-secondary);
}

.header-left :deep(.el-breadcrumb__inner) {
  color: var(--text-secondary);
  font-weight: 500;
}

.header-left :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-btn {
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.header-btn:hover {
  color: var(--primary-400);
  background: rgba(16, 185, 129, 0.1);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.user-dropdown:hover {
  background: rgba(16, 185, 129, 0.1);
}

.user-avatar {
  border: 2px solid rgba(16, 185, 129, 0.3);
  transition: all 0.3s ease;
}

.user-dropdown:hover .user-avatar {
  border-color: var(--primary-500);
  box-shadow: 0 0 12px rgba(16, 185, 129, 0.3);
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.main-content {
  padding: 24px;
  overflow-y: auto;
  background: var(--bg-primary);
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 下拉菜单样式 */
:deep(.user-dropdown-menu) {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  box-shadow: var(--shadow-lg);
}

:deep(.user-dropdown-menu .el-dropdown-menu__item) {
  color: var(--text-secondary);
  padding: 10px 16px;
}

:deep(.user-dropdown-menu .el-dropdown-menu__item:hover) {
  background: rgba(16, 185, 129, 0.1);
  color: var(--primary-400);
}
</style>
