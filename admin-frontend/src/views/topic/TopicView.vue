<template>
  <div class="topic-management page-enter">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><Collection /></el-icon>
        <div>
          <h2>话题管理</h2>
          <p class="page-subtitle">管理社区话题分类</p>
        </div>
      </div>
      <div class="page-stats">
        <div class="stat-badge">
          <el-icon><Collection /></el-icon>
          <span>总话题 {{ total }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="glass-card search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="话题标题"
            clearable
            class="search-input"
            :prefix-icon="Search"
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
            <el-icon><Plus /></el-icon>新增话题
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 话题列表 -->
    <div class="glass-card table-section">
      <div class="table-header">
        <h3>话题列表</h3>
        <el-button type="primary" class="btn-primary" @click="handleSearch">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      <el-table
        :data="topicList"
        v-loading="loading"
        class="custom-table"
        :header-cell-style="{ background: 'transparent', color: '#94a3b8', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="话题信息" min-width="280">
          <template #default="{ row }">
            <div class="topic-info">
              <div class="topic-title">{{ row.title }}</div>
              <div class="topic-desc">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="120">
          <template #default="{ row }">
            <div class="topic-stats">
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ row.viewCount || 0 }} 浏览</span>
              </div>
              <div class="stat-item">
                <el-icon><Document /></el-icon>
                <span>{{ row.postCount || 0 }} 帖子</span>
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
            <el-popconfirm title="确定删除该话题吗？" @confirm="handleDelete(row.id)" confirm-button-type="danger">
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
      :title="isEdit ? '编辑话题' : '新增话题'"
      width="520px"
      class="custom-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item label="话题标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入话题标题"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="话题描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入话题描述"
              maxlength="200"
              show-word-limit
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
  Collection, Search, RefreshRight, Refresh, Plus, Edit, Delete, View, Document
} from '@element-plus/icons-vue'
import { getTopicList, createTopic, updateTopic, deleteTopic } from '../../api/topic'
import { formatDate } from '../../utils/format'

const loading = ref(false)
const topicList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  title: '',
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入话题标题', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTopicList({
      page: page.value,
      size: size.value,
      ...searchForm
    })
    topicList.value = res.records
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
  form.description = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.title = row.title
  form.description = row.description
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateTopic(form.id, { title: form.title, description: form.description })
      ElMessage.success('修改成功')
    } else {
      await createTopic({ title: form.title, description: form.description })
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
    await deleteTopic(id)
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
.topic-management {
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
  width: 240px;
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

.topic-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.topic-title {
  font-weight: 700;
  color: #e2e8f0;
  font-size: 15px;
  letter-spacing: 0.2px;
}

.topic-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  font-weight: 500;
}

.topic-stats {
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
  .topic-management {
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
