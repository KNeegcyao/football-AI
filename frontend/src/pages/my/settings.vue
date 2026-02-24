<template>
  <view class="page-container bg-[#1A1811] text-white min-h-screen flex flex-col font-display">
    <!-- Header -->
    <view class="sticky top-0 z-50 bg-[#1A1811]/95 backdrop-blur-md px-4 pt-12 pb-4 flex flex-row items-center border-b border-white/5">
      <view @click="goBack" class="w-10 h-10 flex items-center justify-center rounded-full bg-white/5 mr-2">
        <text class="material-icons-round" style="font-size: 24px;">chevron_left</text>
      </view>
      <text class="text-xl font-bold tracking-tight text-white">设置</text>
    </view>

    <scroll-view scroll-y class="flex-1 px-4 py-6">
      <!-- Account Section -->
      <view class="mb-8">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-3 block">账号与安全</text>
        <view class="space-y-1">
          <view @click="showPasswordModal = true" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 active:scale-[0.98] transition-all">
            <text class="font-medium">修改密码</text>
            <text class="material-icons-round text-gray-400" style="font-size: 20px;">chevron_right</text>
          </view>
          <view @click="showDeleteAccountModal = true" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 active:scale-[0.98] transition-all">
            <text class="font-medium">注销账号</text>
            <text class="text-xs text-gray-500 mr-1">永久删除账号数据</text>
          </view>
        </view>
      </view>

      <!-- System Section -->
      <view class="mb-8">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-3 block">系统设置</text>
        <view class="space-y-1">
          <view class="w-full flex flex-row items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
            <text class="font-medium">推送通知</text>
            <switch checked color="#f9d406" @change="onSwitchChange('push')" />
          </view>
          <view @click="clearCache" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
            <text class="font-medium">清理缓存</text>
            <text class="text-xs text-gray-500">24.5 MB</text>
          </view>
        </view>
      </view>

      <!-- About Section -->
      <view class="mb-8">
        <text class="text-xs font-bold text-[#f9d406]/80 uppercase tracking-widest ml-2 mb-3 block">关于</text>
        <view class="space-y-1">
          <view class="w-full flex flex-row items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5">
            <text class="font-medium">当前版本</text>
            <text class="text-xs text-gray-400">v2.4.0 (Build 503)</text>
          </view>
          <view @click="goToProtocol('privacy')" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 active:scale-[0.98] transition-all">
            <text class="font-medium">隐私协议</text>
            <text class="material-icons-round text-gray-400" style="font-size: 20px;">open_in_new</text>
          </view>
          <view @click="goToProtocol('user')" class="w-full flex flex-row items-center justify-between p-4 bg-white/5 rounded-xl border border-white/5 hover:bg-white/10 active:scale-[0.98] transition-all">
            <text class="font-medium">用户协议</text>
            <text class="material-icons-round text-gray-400" style="font-size: 20px;">open_in_new</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Change Password Modal -->
    <view v-if="showPasswordModal" class="fixed inset-0 z-[100] flex items-center justify-center px-6">
      <view class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="showPasswordModal = false"></view>
      <view class="relative w-full bg-[#1A1811] border border-white/10 rounded-2xl overflow-hidden animate-in fade-in zoom-in duration-300">
        <view class="p-6">
          <text class="text-xl font-bold mb-6 block text-white">修改密码</text>
          <view class="space-y-4">
            <view class="space-y-2">
              <text class="text-xs text-gray-400 ml-1">当前密码</text>
              <input v-model="passwordForm.old" password placeholder="请输入当前密码" class="w-full h-12 bg-white/5 border border-white/10 rounded-xl px-4 text-white text-sm" />
            </view>
            <view class="space-y-2">
              <text class="text-xs text-gray-400 ml-1">新密码</text>
              <input v-model="passwordForm.new" password placeholder="请输入新密码" class="w-full h-12 bg-white/5 border border-white/10 rounded-xl px-4 text-white text-sm" />
            </view>
            <view class="space-y-2">
              <text class="text-xs text-gray-400 ml-1">确认新密码</text>
              <input v-model="passwordForm.confirm" password placeholder="请再次输入新密码" class="w-full h-12 bg-white/5 border border-white/10 rounded-xl px-4 text-white text-sm" />
            </view>
          </view>
          <view class="flex flex-row gap-3 mt-8">
            <view @click="showPasswordModal = false" class="flex-1 py-3 bg-white/5 rounded-xl flex items-center justify-center border border-white/10">
              <text class="text-white font-medium">取消</text>
            </view>
            <view @click="handleUpdatePassword" class="flex-1 py-3 gold-gradient rounded-xl flex items-center justify-center">
              <text class="text-[#1A1811] font-bold">确定修改</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Delete Account Modal -->
    <view v-if="showDeleteAccountModal" class="fixed inset-0 z-[100] flex items-center justify-center px-6">
      <view class="absolute inset-0 bg-black/80 backdrop-blur-sm" @click="showDeleteAccountModal = false"></view>
      <view class="relative w-full bg-[#1A1811] border border-white/10 rounded-2xl overflow-hidden animate-in fade-in zoom-in duration-300">
        <view class="p-6">
          <view class="w-16 h-16 bg-red-500/10 rounded-full flex items-center justify-center mx-auto mb-4">
            <text class="material-icons-round text-red-500" style="font-size: 32px;">warning</text>
          </view>
          <text class="text-xl font-bold text-center block text-white mb-2">注销账号</text>
          <text class="text-sm text-gray-400 text-center block mb-8 px-4">账号注销后，所有数据将无法找回。请确认您已备份重要信息。</text>
          <view class="flex flex-row gap-3">
            <view @click="showDeleteAccountModal = false" class="flex-1 py-3 bg-white/5 rounded-xl flex items-center justify-center border border-white/10">
              <text class="text-white font-medium">我再想想</text>
            </view>
            <view @click="handleDeleteAccount" class="flex-1 py-3 bg-red-600 rounded-xl flex items-center justify-center shadow-lg shadow-red-600/20">
              <text class="text-white font-bold">确认注销</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { userApi } from '@/api/index'

const showPasswordModal = ref(false)
const showDeleteAccountModal = ref(false)
const passwordForm = reactive({
  old: '',
  new: '',
  confirm: ''
})

const goBack = () => {
  uni.navigateBack()
}

const goToProtocol = (type) => {
  const titles = {
    privacy: '隐私协议',
    user: '用户协议'
  }
  uni.showModal({
    title: titles[type],
    content: `这是${titles[type]}的内容。在实际开发中，这里通常会跳转到一个专门的协议展示页面或加载富文本内容。`,
    showCancel: false,
    confirmText: '我知道了'
  })
}

const handleUpdatePassword = async () => {
  if (!passwordForm.old || !passwordForm.new || !passwordForm.confirm) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (passwordForm.new !== passwordForm.confirm) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
    return
  }
  
  uni.showLoading({ title: '正在提交...' })
  try {
    const res = await userApi.updatePassword({
      oldPassword: passwordForm.old,
      newPassword: passwordForm.new
    })
    
    uni.hideLoading()
    if (res.code === 200) {
      uni.showToast({ title: '修改成功', icon: 'success' })
      showPasswordModal.value = false
      passwordForm.old = ''
      passwordForm.new = ''
      passwordForm.confirm = ''
    } else {
      uni.showToast({ title: res.message || '修改失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '网络错误，请稍后再试', icon: 'none' })
    console.error('Update password error:', error)
  }
}

const handleDeleteAccount = async () => {
  uni.showLoading({ title: '正在处理...' })
  try {
    const res = await userApi.deleteAccount()
    uni.hideLoading()
    
    if (res.code === 200) {
      uni.showToast({ title: '账号已注销', icon: 'success' })
      showDeleteAccountModal.value = false
      
      // 清除本地缓存
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      
      // 延迟跳转到登录页
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/login/login' })
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '注销失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '操作失败，请重试', icon: 'none' })
    console.error('Delete account error:', error)
  }
}

const onSwitchChange = (type) => {
  uni.showToast({
    title: '设置已更新',
    icon: 'none'
  })
}

const clearCache = () => {
  uni.showLoading({ title: '清理中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '缓存已清理',
      icon: 'success'
    })
  }, 1000)
}
</script>

<style scoped>
.backdrop-blur-md {
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}
</style>
