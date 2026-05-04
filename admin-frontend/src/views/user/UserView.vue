<template>
  <div class="user-management page-enter">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon class="title-icon"><UserFilled /></el-icon>
        <div>
          <h2>用户管理</h2>
          <p class="page-subtitle">管理系统用户，查看用户状态与角色</p>
        </div>
      </div>
      <div class="page-stats">
        <div class="stat-badge">
          <el-icon><User /></el-icon>
          <span>总用户 {{ total }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="glass-card search-section">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/昵称"
            clearable
            class="search-input"
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable class="search-select">
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable class="search-select">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 用户列表 -->
    <div class="glass-card table-section">
      <div class="table-header">
        <h3>用户列表</h3>
        <el-button type="primary" class="btn-primary" @click="handleSearch">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      <el-table
        :data="userList"
        v-loading="loading"
        class="custom-table"
        :header-cell-style="{ background: 'transparent', color: '#94a3b8', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户信息" min-width="180">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-avatar">
                {{ row.nickname?.charAt(0) || row.username?.charAt(0) || '?' }}
              </div>
              <div class="user-details">
                <div class="user-name">{{ row.nickname || row.username }}</div>
                <div class="user-username">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">
            <span class="email-text">{{ row.email || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="110">
          <template #default="{ row }">
            <span class="role-badge" :class="row.role === 'ADMIN' ? 'role-admin' : 'role-user'">
              <el-icon><component :is="row.role === 'ADMIN' ? 'StarFilled' : 'User'" /></el-icon>
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row.id, val)"
              class="status-switch"
            />
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="80">
          <template #default="{ row }">
            <span class="level-badge">Lv.{{ row.level || 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link class="action-btn" @click="handleEditRole(row)">
              <el-icon><Edit /></el-icon>角色
            </el-button>
            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row.id)" confirm-button-type="danger">
              <template #reference>
                <el-button type="danger" link class="action-btn">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 修改角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="修改角色" width="420px" class="custom-dialog" destroy-on-close>
      <div class="dialog-body">
        <el-form :model="roleForm" label-position="top">
          <el-form-item label="选择角色">
            <el-select v-model="roleForm.role" placeholder="请选择角色" class="full-width">
              <el-option label="用户" value="USER">
                <el-icon><User /></el-icon> 普通用户
              </el-option>
              <el-option label="管理员" value="ADMIN">
                <el-icon><StarFilled /></el-icon> 管理员
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialogVisible = false" class="btn-secondary">取消</el-button>
          <el-button type="primary" @click="handleRoleSubmit" class="btn-primary">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, User, Search, RefreshRight, Refresh, Edit, Delete, StarFilled } from '@element-plus/icons-vue'
import { getUserList, updateUserStatus, updateUserRole, deleteUser } from '../../api/user'
import { formatDate } from '../../utils/format'

const loading = ref(false)
const userList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  role: '',
  status: null
})

const roleDialogVisible = ref(false)
const roleForm = reactive({
  id: null,
  role: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size: size.value,
      ...searchForm
    })
    userList.value = res.records
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
  searchForm.role = ''
  searchForm.status = null
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

const handleStatusChange = async (id, status) => {
  try {
    await updateUserStatus(id, status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error(error)
  }
}

const handleEditRole = (row) => {
  roleForm.id = row.id
  roleForm.role = row.role
  roleDialogVisible.value = true
}

const handleRoleSubmit = async () => {
  try {
    await updateUserRole(roleForm.id, roleForm.role)
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id) => {
  try {
    await deleteUser(id)
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
.user-management {
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

.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-400));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 700;
  color: #e2e8f0;
  font-size: 15px;
  letter-spacing: 0.3px;
}

.user-username {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.email-text {
  color: var(--text-secondary);
  font-size: 13px;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.role-admin {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.role-user {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.level-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: linear-gradient(135deg, var(--primary-500), var(--primary-400));
  color: white;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
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

.full-width {
  width: 100%;
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
  .user-management {
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
}
</style>
