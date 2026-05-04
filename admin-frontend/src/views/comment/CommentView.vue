<template>
  <div class="comment-management page-enter">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><ChatLineRound /></el-icon>
        <div>
          <h2>评论管理</h2>
          <p class="page-subtitle">审核和管理用户评论内容</p>
        </div>
      </div>
      <div class="page-stats">
        <div class="stat-badge">
          <el-icon><ChatLineRound /></el-icon>
          <span>总评论 {{ total }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="glass-card search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="评论内容"
            clearable
            class="search-input"
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="帖子ID">
          <el-input
            v-model="searchForm.postId"
            placeholder="帖子ID"
            clearable
            class="search-input"
          />
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

    <!-- 评论列表 -->
    <div class="glass-card table-section">
      <div class="table-header">
        <h3>评论列表</h3>
        <el-button type="primary" class="btn-primary" @click="handleSearch">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      <el-table
        :data="commentList"
        v-loading="loading"
        class="custom-table"
        :header-cell-style="{ background: 'transparent', color: '#94a3b8', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="评论内容" min-width="280">
          <template #default="{ row }">
            <div class="comment-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="所属帖子" min-width="160">
          <template #default="{ row }">
            <div class="post-ref">
              <el-icon><Document /></el-icon>
              <span class="post-title-text">{{ row.postTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评论者" width="130">
          <template #default="{ row }">
            <div class="commenter">
              <div class="commenter-avatar">{{ row.authorName?.charAt(0) || '?' }}</div>
              <span class="commenter-name">{{ row.authorName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="点赞" width="80">
          <template #default="{ row }">
            <span class="like-count">
              <el-icon><Pointer /></el-icon>
              {{ row.likes || 0 }}
            </span>
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
        <el-table-column prop="createdAt" label="时间" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该评论吗？" @confirm="handleDelete(row.id)" confirm-button-type="danger">
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineRound, Search, RefreshRight, Refresh, Delete,
  Document, Pointer, CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import { getCommentList, deleteComment } from '../../api/comment'
import { formatDate } from '../../utils/format'

const loading = ref(false)
const commentList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  postId: null
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      keyword: searchForm.keyword
    }
    if (searchForm.postId) {
      params.postId = parseInt(searchForm.postId)
    }
    const res = await getCommentList(params)
    commentList.value = res.records
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
  searchForm.postId = null
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

const handleDelete = async (id) => {
  try {
    await deleteComment(id)
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
.comment-management {
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

.search-input {
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

.comment-content {
  color: #e2e8f0;
  font-size: 14px;
  line-height: 1.6;
  max-width: 300px;
  font-weight: 500;
}

.post-ref {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
}

.post-ref .el-icon {
  color: var(--primary-400);
}

.post-title-text {
  color: #e2e8f0;
  font-weight: 600;
}

.commenter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.commenter-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-400));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.25);
}

.commenter-name {
  font-size: 14px;
  color: #e2e8f0;
  font-weight: 600;
}

.like-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-tertiary);
  font-size: 13px;
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
  .comment-management {
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

  .search-input {
    width: 100%;
  }
}
</style>
