<template>
  <div class="post-management page-enter">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><Document /></el-icon>
        <div>
          <h2>帖子管理</h2>
          <p class="page-subtitle">审核和管理社区帖子内容</p>
        </div>
      </div>
      <div class="page-stats">
        <div class="stat-badge">
          <el-icon><Document /></el-icon>
          <span>总帖子 {{ total }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="glass-card search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="标题/内容"
            clearable
            class="search-input"
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable class="search-select">
            <el-option label="正常" :value="0" />
            <el-option label="已删除" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="btn-primary">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset" class="btn-secondary">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 帖子列表 -->
    <div class="glass-card table-section">
      <div class="table-header">
        <h3>帖子列表</h3>
        <el-button type="primary" class="btn-primary" @click="handleSearch">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      <el-table
        :data="postList"
        v-loading="loading"
        class="custom-table"
        :header-cell-style="{ background: 'transparent', color: '#94a3b8', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="帖子信息" min-width="250">
          <template #default="{ row }">
            <div class="post-info">
              <div class="post-title">{{ row.title }}</div>
              <div class="post-meta">
                <span class="meta-item"><el-icon><User /></el-icon>{{ row.authorName }}</span>
                <span class="meta-item"><el-icon><Collection /></el-icon>{{ row.topicName }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="130">
          <template #default="{ row }">
            <div class="post-stats">
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ row.views || 0 }}</span>
              </div>
              <div class="stat-item">
                <el-icon><Pointer /></el-icon>
                <span>{{ row.likes || 0 }}</span>
              </div>
              <div class="stat-item">
                <el-icon><ChatLineRound /></el-icon>
                <span>{{ row.commentCount || 0 }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status === 0 ? 'status-normal' : 'status-deleted'">
              <el-icon><component :is="row.status === 0 ? 'CircleCheck' : 'CircleClose'" /></el-icon>
              {{ row.status === 0 ? '正常' : '已删除' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link class="action-btn" @click="handleView(row)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-popconfirm title="确定删除该帖子吗？" @confirm="handleDelete(row.id)" confirm-button-type="danger">
              <template #reference>
                <el-button type="danger" link class="action-btn">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          class="custom-pagination"
        />
      </div>
    </div>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="帖子详情" width="650px" class="custom-dialog" destroy-on-close>
      <div v-if="currentPost" class="post-detail">
        <h3 class="detail-title">{{ currentPost.title }}</h3>
        <div class="detail-meta">
          <span class="meta-tag"><el-icon><User /></el-icon>{{ currentPost.authorName }}</span>
          <span class="meta-tag"><el-icon><Collection /></el-icon>{{ currentPost.topicName }}</span>
          <span class="meta-tag"><el-icon><Clock /></el-icon>{{ formatDate(currentPost.createdAt) }}</span>
        </div>
        <div class="detail-content">{{ currentPost.content }}</div>
        <div v-if="currentPost.images && currentPost.images.length" class="detail-images">
          <img v-for="(img, idx) in currentPost.images" :key="idx" :src="img" class="detail-image" />
        </div>
        <div class="detail-stats">
          <div class="detail-stat">
            <el-icon><View /></el-icon>
            <span>{{ currentPost.views || 0 }} 浏览</span>
          </div>
          <div class="detail-stat">
            <el-icon><Pointer /></el-icon>
            <span>{{ currentPost.likes || 0 }} 点赞</span>
          </div>
          <div class="detail-stat">
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ currentPost.commentCount || 0 }} 评论</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document, Search, RefreshRight, Refresh, View, Delete,
  User, Collection, Pointer, ChatLineRound, CircleCheck, CircleClose, Clock
} from '@element-plus/icons-vue'
import { getPostList, deletePost } from '../../api/post'
import { formatDate } from '../../utils/format'

const loading = ref(false)
const postList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  status: null,
  topicId: null
})

const viewDialogVisible = ref(false)
const currentPost = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getPostList({
      page: page.value,
      size: size.value,
      ...searchForm
    })
    postList.value = res.records
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  searchForm.topicId = null
  page.value = 1
  loadData()
}

const handleSizeChange = (val) => {
  size.value = val
  loadData()
}

const handlePageChange = (val) => {
  page.value = val
  loadData()
}

const handleView = (row) => {
  currentPost.value = row
  viewDialogVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await deletePost(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.post-management {
  padding: 24px;
  animation: fadeInUp 0.5s ease-out;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.page-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--text-tertiary);
}

.title-icon {
  font-size: 32px;
  color: var(--primary-500);
  background: linear-gradient(135deg, var(--primary-500), var(--primary-400));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-stats {
  display: flex;
  gap: 12px;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px 24px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.search-input,
.search-select {
  width: 180px;
}

.table-section {
  padding: 20px 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.post-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.post-title {
  font-weight: 700;
  color: #e2e8f0;
  font-size: 15px;
  line-height: 1.4;
  letter-spacing: 0.2px;
}

.post-meta {
  display: flex;
  gap: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.meta-item .el-icon {
  color: var(--primary-400);
  font-size: 14px;
}

.post-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-normal {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.status-deleted {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.time-text {
  color: var(--text-tertiary);
  font-size: 13px;
}

.action-btn {
  font-size: 13px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 详情弹窗 */
.post-detail {
  padding: 8px 0;
}

.detail-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.detail-content {
  margin: 16px 0;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  line-height: 1.8;
  color: var(--text-secondary);
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 16px 0;
}

.detail-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid var(--glass-border);
}

.detail-stats {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--glass-border);
}

.detail-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .post-management {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .search-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .search-select {
    width: 100%;
  }

  .detail-images {
    justify-content: center;
  }
}
</style>
