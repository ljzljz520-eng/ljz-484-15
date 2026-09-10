<template>
  <div class="container author-page" v-loading="loading">
    <div class="page-head">
      <el-button plain round icon="ArrowLeft" @click="router.push('/author')">返回工作台</el-button>
      <h1 class="page-title">{{ pageTitle }}</h1>
      <p class="page-sub">{{ pageDescription }}（共 {{ total }} 章）</p>
    </div>

    <div class="filter-bar glass-panel">
      <el-radio-group v-model="statusFilter" @change="onFilterChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="DRAFT">草稿</el-radio-button>
        <el-radio-button label="PUBLISHED">已发布</el-radio-button>
      </el-radio-group>
      <el-switch
        v-model="weeklyOnly"
        active-text="仅看本周新增"
        inline-prompt
        @change="onFilterChange"
      />
    </div>

    <div class="table-panel glass-panel">
      <el-table :data="chapters" style="width: 100%" :header-cell-style="{ background: 'transparent' }">
        <el-table-column label="所属作品" min-width="200">
          <template #default="{ row }">
            <router-link :to="'/novel/' + row.novelId" class="novel-link">{{ row.novelTitle }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="章节标题" min-width="220" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'warning'" round size="small">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="字数" width="100">
          <template #default="{ row }">{{ row.wordCount }} 字</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push('/chapter/' + row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && chapters.length === 0" description="暂无章节" />
    </div>

    <div class="pagination-wrapper" v-if="total > size">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const chapters = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 筛选条件由统计卡片跳转的 query 初始化
const statusFilter = ref(route.query.status === 'DRAFT' || route.query.status === 'PUBLISHED'
  ? route.query.status : '')
const weeklyOnly = ref(route.query.weekly === '1')

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const pageTitle = computed(() => {
  if (weeklyOnly.value) return '本周新增章节'
  if (statusFilter.value === 'DRAFT') return '草稿章节'
  if (statusFilter.value === 'PUBLISHED') return '已发布章节'
  return '全部章节'
})

const pageDescription = computed(() => {
  if (weeklyOnly.value) return '本周一 00:00 起新创建的章节'
  if (statusFilter.value === 'DRAFT') return '状态为草稿的章节'
  if (statusFilter.value === 'PUBLISHED') return '状态为已发布的章节'
  return '按最近更新时间排序'
})

const fetchChapters = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value) params.status = statusFilter.value
    if (weeklyOnly.value) params.weekly = true
    const res = await axios.get(`${API_URL}/author/chapters`, { params })
    chapters.value = res.data.data
    total.value = res.data.total
  } catch (err) {
    console.error('加载章节列表失败', err)
    ElMessage.error('加载章节列表失败')
  } finally {
    loading.value = false
  }
}

const onFilterChange = () => {
  page.value = 1
  router.replace({
    query: {
      ...(statusFilter.value ? { status: statusFilter.value } : {}),
      ...(weeklyOnly.value ? { weekly: '1' } : {})
    }
  })
  fetchChapters()
}

const onPageChange = (val) => {
  page.value = val
  fetchChapters()
}

const formatDateTime = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(fetchChapters)
</script>

<style scoped>
.author-page {
  padding: 40px 20px 60px;
}

.page-head {
  margin-bottom: 26px;
}

.page-title {
  font-size: 2rem;
  margin: 16px 0 6px;
}

.page-sub {
  color: var(--text-sub);
  margin: 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.85);
}

.table-panel {
  padding: 10px 20px 20px;
  background: rgba(255, 255, 255, 0.9);
}

.novel-link {
  color: var(--primary-color);
  font-weight: 500;
}

.novel-link:hover {
  text-decoration: underline;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
