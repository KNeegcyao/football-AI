<template>
  <view class="page-container">
    <!-- Status Bar -->
    <view class="status-bar"></view>

    <!-- Top Navigation Bar -->
    <view class="nav-header">
      <text class="cancel-btn" @click="goBack">取消</text>
      <text class="page-title">发布帖子</text>
      <view class="publish-btn" :class="{ disabled: !isValid || submitting }" @click="handlePublish">
        <text class="publish-text">{{ submitting ? '发布中...' : '发布' }}</text>
      </view>
    </view>

    <!-- Content Area -->
    <scroll-view scroll-y class="main-content">
      <!-- User Info -->
      <view class="user-info">
        <view class="avatar-container">
          <image class="avatar-img" src="/static/default-team.png" mode="aspectFill"></image>
        </view>
        <view class="user-meta">
          <text class="user-name">足球战术家</text>
          <text class="user-badge">PitchPulse Pro</text>
        </view>
      </view>

      <!-- Editor Section -->
      <view class="editor-section">
        <!-- Title Input -->
        <input 
          class="title-input" 
          placeholder="标题 (可选)" 
          placeholder-class="placeholder-text"
          v-model="form.title"
          maxlength="100"
        />
        
        <textarea 
          class="content-textarea" 
          placeholder="分享你的足球见解..." 
          placeholder-class="placeholder-text"
          v-model="form.content"
          maxlength="5000"
          auto-height
        ></textarea>
      </view>

      <!-- Image Grid -->
      <view class="image-grid">
        <view 
          class="image-item" 
          v-for="(img, index) in images" 
          :key="index"
        >
          <image :src="img" mode="aspectFill" class="grid-img"></image>
          <view class="remove-btn" @click="removeImage(index)">
            <text class="material-icons close-icon">close</text>
          </view>
        </view>
        
        <!-- Upload Placeholder -->
        <view class="upload-btn" @click="chooseImage" v-if="images.length < 9">
          <text class="material-icons add-icon">add_photo_alternate</text>
          <text class="upload-text">添加图片</text>
        </view>
      </view>

      <!-- Circle Selection Preview -->
      <view class="circle-preview" v-if="circleName">
        <text class="section-label">已选圈子</text>
        <view class="circle-badge">
          <view class="circle-icon">
            <image src="/static/soccer-logo.png" mode="aspectFit" class="badge-img"></image>
          </view>
          <text class="circle-name">{{ circleName }}</text>
          <text class="material-icons arrow-right">chevron_right</text>
        </view>
      </view>
    </scroll-view>

    <!-- Functional Bottom Bar -->
    <view class="bottom-bar">
      <view class="toolbar">
        <view class="tool-actions">
          <!-- Mention Button -->
          <view class="tool-btn">
            <view class="icon-circle">
              <text class="material-icons tool-icon">alternate_email</text>
            </view>
            <text class="tool-label">@提到人</text>
          </view>
          <!-- Hashtag Button -->
          <view class="tool-btn">
            <view class="icon-circle">
              <text class="material-icons tool-icon">tag</text>
            </view>
            <text class="tool-label">#话题</text>
          </view>
          <!-- Select Circle Button -->
          <view class="tool-btn">
            <view class="icon-circle">
              <text class="material-icons tool-icon">explore</text>
            </view>
            <text class="tool-label">选择圈子</text>
          </view>
        </view>
        
        <!-- Character Counter -->
        <view class="char-counter">
          <text class="current-count">{{ contentLength }}</text>
          <text class="max-count">/ 800</text>
        </view>
      </view>
      
      <!-- Quick Access Tags -->
      <scroll-view scroll-x class="quick-tags" :show-scrollbar="false">
        <view class="tag-list">
          <text class="tag-item">#双红会</text>
          <text class="tag-item">#滕哈赫</text>
          <text class="tag-item">#战术复盘</text>
          <text class="tag-item">#欧冠决赛</text>
        </view>
      </scroll-view>
      
      <!-- Safe Area -->
      <view class="safe-area"></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { postApi } from '@/api';

const API_BASE_URL = 'http://localhost:8080';

const form = ref({
  title: '',
  content: ''
});

const images = ref([]);
const submitting = ref(false);
const circleId = ref(null);
const circleName = ref('');

onLoad((options) => {
  if (options.circleId) {
    circleId.value = options.circleId;
  }
  if (options.circleName) {
    circleName.value = decodeURIComponent(options.circleName);
  }
});

const contentLength = computed(() => form.value.content.length);

const isValid = computed(() => {
  return form.value.content.trim().length > 0;
});

const goBack = () => {
  uni.navigateBack();
};

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - images.value.length,
    success: (res) => {
      images.value = [...images.value, ...res.tempFilePaths];
    }
  });
};

const removeImage = (index) => {
  images.value.splice(index, 1);
};

const handlePublish = async () => {
  if (!isValid.value || submitting.value) return;

  try {
    submitting.value = true;
    
    let uploadedImages = [];
    
    // 如果有图片，先上传图片
    if (images.value.length > 0) {
      uni.showLoading({ title: '上传图片中...' });
      
      const uploadPromises = images.value.map(filePath => {
        return new Promise((resolve, reject) => {
          // H5环境下可以使用相对路径，App/小程序需要完整路径
          // 这里假设是H5或已配置好代理
          uni.uploadFile({
            url: `${API_BASE_URL}/api/files/upload`,
            filePath: filePath,
            name: 'file',
            header: {
              'Authorization': uni.getStorageSync('token') // 假设Token存在本地
            },
            success: (uploadRes) => {
              if (uploadRes.statusCode === 200) {
                // 假设后端返回格式为 { code: 200, data: "url", msg: "success" }
                try {
                  const data = JSON.parse(uploadRes.data);
                  if (data.code === 200) {
                    resolve(data.data);
                  } else {
                    reject(new Error(data.msg || '上传失败'));
                  }
                } catch (e) {
                  reject(new Error('解析响应失败'));
                }
              } else {
                reject(new Error('上传HTTP错误'));
              }
            },
            fail: (err) => {
              reject(err);
            }
          });
        });
      });
      
      try {
        uploadedImages = await Promise.all(uploadPromises);
      } catch (uploadError) {
        uni.hideLoading();
        console.error('Image upload failed:', uploadError);
        uni.showToast({
          title: '图片上传失败',
          icon: 'none'
        });
        submitting.value = false;
        return;
      }
      
      uni.hideLoading();
    }
    
    uni.showLoading({ title: '发布中...' });

    // 如果没有输入标题，自动截取内容前20字作为标题
    const postData = {
      title: form.value.title || form.value.content.slice(0, 20),
      content: form.value.content,
      images: JSON.stringify(uploadedImages),
      circleId: circleId.value
    };

    const res = await postApi.create(postData);

    if (res && res.id) {
      uni.showToast({
        title: '发布成功',
        icon: 'success'
      });
      
      setTimeout(() => {
        uni.redirectTo({
          url: `/pages/post/detail?id=${res.id}`
        });
      }, 1500);
    } else {
      uni.showToast({
        title: res.msg || '发布失败',
        icon: 'none'
      });
    }
  } catch (error) {
    console.error('Publish failed:', error);
    uni.showToast({
      title: '网络错误',
      icon: 'none'
    });
  } finally {
    uni.hideLoading();
    submitting.value = false;
  }
};
</script>

<style scoped lang="scss">
/* 
  Colors based on provided design:
  Background Dark: #121212
  Surface Dark: #1e1e1e
  Primary: #db143c
  Border Dark: #2a2a2a
  Text White: #ffffff
  Text Slate 500: #64748b
*/

.page-container {
  background-color: #121212;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  color: #ffffff;
  font-family: 'Lexend', sans-serif;
}

.status-bar {
  height: var(--status-bar-height);
  background-color: rgba(18, 18, 18, 0.8);
}

/* Header */
.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: rgba(18, 18, 18, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid #2a2a2a;
  position: sticky;
  top: 0;
  z-index: 50;
}

.cancel-btn {
  color: #94a3b8;
  font-size: 16px;
  font-weight: 500;
}

.page-title {
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
}

.publish-btn {
  background-color: #db143c;
  padding: 6px 20px;
  border-radius: 9999px;
  box-shadow: 0 10px 15px -3px rgba(219, 20, 60, 0.2);
  transition: all 0.3s;
  
  &.disabled {
    opacity: 0.5;
    background-color: #2a2a2a;
    box-shadow: none;
  }
}

.publish-text {
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
}

/* Content */
.main-content {
  flex: 1;
  padding: 16px;
  box-sizing: border-box;
}

/* User Info */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.avatar-container {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(219, 20, 60, 0.2);
  border: 1px solid rgba(219, 20, 60, 0.3);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
}

.user-badge {
  font-size: 10px;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Editor */
.editor-section {
  min-height: 160px;
  margin-bottom: 20px;
}

.title-input {
  font-size: 18px;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 12px;
  border-bottom: 1px solid #2a2a2a;
  padding-bottom: 8px;
}

.content-textarea {
  width: 100%;
  min-height: 120px;
  background-color: transparent;
  color: #ffffff;
  font-size: 18px;
  line-height: 1.6;
}

.placeholder-text {
  color: #475569;
}

/* Image Grid */
.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 24px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
}

.grid-img {
  width: 100%;
  height: 100%;
}

.remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  font-size: 16px;
  color: #ffffff;
}

.upload-btn {
  aspect-ratio: 1;
  border-radius: 12px;
  border: 2px dashed #2a2a2a;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: background-color 0.3s;

  &:active {
    background-color: #1e1e1e;
  }
}

.add-icon {
  color: #64748b;
  font-size: 24px;
}

.upload-text {
  font-size: 10px;
  color: #64748b;
  font-weight: 500;
}

/* Circle Preview */
.circle-preview {
  padding-top: 16px;
  border-top: 1px solid #2a2a2a;
}

.section-label {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
  display: block;
}

.circle-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background-color: rgba(219, 20, 60, 0.1);
  border: 1px solid rgba(219, 20, 60, 0.2);
  padding: 8px 12px;
  border-radius: 12px;
}

.circle-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.badge-img {
  width: 20px;
  height: 20px;
}

.circle-name {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
}

.arrow-right {
  font-size: 14px;
  color: #94a3b8;
}

/* Bottom Bar */
.bottom-bar {
  background-color: #1e1e1e;
  border-top: 1px solid #2a2a2a;
  padding-top: 12px;
  padding-bottom: 32px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  margin-bottom: 16px;
}

.tool-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.tool-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #1e293b;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s;
}

.tool-btn:active .icon-circle {
  background-color: rgba(219, 20, 60, 0.2);
}

.tool-icon {
  color: #94a3b8;
  font-size: 20px;
}

.tool-btn:active .tool-icon {
  color: #db143c;
}

.tool-label {
  font-size: 10px;
  color: #64748b;
  font-weight: 500;
}

.char-counter {
  text-align: right;
}

.current-count {
  font-size: 12px;
  font-weight: 700;
  color: #db143c;
}

.max-count {
  font-size: 12px;
  color: #475569;
}

.quick-tags {
  white-space: nowrap;
  width: 100%;
}

.tag-list {
  display: flex;
  padding: 0 16px;
  gap: 8px;
}

.tag-item {
  display: inline-block;
  background-color: #1e293b;
  color: #94a3b8;
  font-size: 11px;
  padding: 4px 12px;
  border-radius: 9999px;
  border: 1px solid #2a2a2a;
}

.safe-area {
  height: 20px;
  background-color: #1e1e1e;
}
</style>