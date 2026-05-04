<template>
  <div class="news-management page-enter">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><Reading /></el-icon>
        <div>
          <h2>资讯管理</h2>
          <p class="page-subtitle">管理足球资讯内容</p>
        </div>
      </div>
      <div class="page-stats">
        <div class="stat-badge">
          <el-icon><Reading /></el-icon>
          <span>总资讯 {{ total }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="glass-card search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="标题"
            clearable
            class="search-input"
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-input
            v-model="searchForm.category"
            placeholder="分类"
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
          <el-button type="success" @click="handleAdd" class="btn-success">
            <el-icon><Plus /></el-icon>新增资讯
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 资讯列表 -->
    <div class="glass-card table-section">
      <div class="table-header">
        <h3>资讯列表</h3>
        <el-button type="primary" class="btn-primary" @click="handleSearch">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      <el-table
        :data="newsList"
        v-loading="loading"
        class="custom-table"
        :header-cell-style="{ background: 'transparent', color: '#94a3b8', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="资讯信息" min-width="300">
          <template #default="{ row }">
            <div class="news-info">
              <div class="news-title">{{ row.title }}</div>
              <div class="news-meta">
                <span class="meta-tag category-tag">{{ row.category || '未分类' }}</span>
                <span class="meta-tag author-tag">
                  <el-icon><User /></el-icon>{{ row.author }}
                </span>
                <span v-if="row.tags" class="meta-tag tags-tag">{{ row.tags }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="互动数据" width="130">
          <template #default="{ row }">
            <div class="news-stats">
              <div class="stat-item">
                <el-icon><Pointer /></el-icon>
                <span>{{ row.likeCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <el-icon><ChatLineRound /></el-icon>
                <span>{{ row.commentCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <el-icon><Star /></el-icon>
                <span>{{ row.collectCount || 0 }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link class="action-btn" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-popconfirm title="确定删除该资讯吗？" @confirm="handleDelete(row.id)" confirm-button-type="danger">
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑资讯' : '新增资讯'"
      width="720px"
      class="custom-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入标题"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="摘要" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="2"
              placeholder="请输入摘要"
            />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="作者" prop="author" class="form-col">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
            <el-form-item label="分类" prop="category" class="form-col">
              <el-input v-model="form.category" placeholder="请输入分类" />
            </el-form-item>
          </div>
          <el-form-item label="封面URL" prop="coverUrl">
            <el-input v-model="form.coverUrl" placeholder="请输入封面图片URL">
              <template #prefix>
                <el-icon><Picture /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="标签" prop="tags">
            <el-input v-model="form.tags" placeholder="请输入标签，用逗号分隔">
              <template #prefix>
                <el-icon><PriceTag /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="10"
              placeholder="请输入内容"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-secondary">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="btn-primary">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Reading, Search, RefreshRight, Refresh, Plus, Edit, Delete,
  User, Pointer, ChatLineRound, Star, Picture, PriceTag
} from '@element-plus/icons-vue'
import { getNewsList, createNews, updateNews, deleteNews } from '../../api/news'
import { formatDate } from '../../utils/format'

const loading = ref(false)
const newsList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  category: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  title: '',
  summary: '',
  content: '',
  coverUrl: '',
  author: '',
  tags: '',
  category: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getNewsList({
      page: page.value,
      size: size.value,
      ...searchForm
    })
    newsList.value = res.records
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
  searchForm.category = ''
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

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.title = ''
  form.summary = ''
  form.content = ''
  form.coverUrl = ''
  form.author = ''
  form.tags = ''
  form.category = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.title = row.title
  form.summary = row.summary
  form.content = row.content
  form.coverUrl = row.coverUrl
  form.author = row.author
  form.tags = row.tags
  form.category = row.category
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const data = { ...form }
    delete data.id
    if (isEdit.value) {
      await updateNews(form.id, data)
      ElMessage.success('修改成功')
    } else {
      await createNews(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await deleteNews(id)
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
.news-management {
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
  width: 200px;
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

.news-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.news-title {
  font-weight: 700;
  color: #e2e8f0;
  font-size: 15px;
  line-height: 1.4;
  letter-spacing: 0.2px;
}

.news-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.meta-tag .el-icon {
  font-size: 13px;
}

.category-tag {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.35);
}

.author-tag {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.35);
}

.tags-tag {
  background: rgba(139, 92, 246, 0.2);
  color: #a78bfa;
  border: 1px solid rgba(139, 92, 246, 0.35);
}

.news-stats {
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

.dialog-body {
  padding: 8px 0;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-col {
  flex: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
  .news-management {
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

  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
