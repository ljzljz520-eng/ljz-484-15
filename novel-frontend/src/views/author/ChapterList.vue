<template>
  <div class="container list-page">
    <div class="page-head">
      <el-button circle plain icon="ArrowLeft" @click="router.push('/dashboard')"></el-button>
      <div>
        <h1 class="page-title text-gradient">{{ pageHeading }}</h1>
        <p class="page-subtitle">共 {{ total }} 章 · 合计 {{ formatNumber(totalWords) }} 字（数据来自后端）</p>
      </div>
    </div>

    <div class="toolbar glass-panel">
      <el-select v-model="statusFilter" clearable placeholder="全部状态" class="filter-item" @change="reload(1)">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已发布" value="PUBLISHED" />
      </el-select>
      <el-select v-model="novelFilter" clearable placeholder="全部作品" class="filter-novel" @change="reload(1)">
        <el-option v-for="n in novelOptions" :key="n.id" :label="n.title" :value="n.id" />
      </el-select>
      <el-checkbox v-model="thisWeekFilter" @change="reload(1)">仅看本周新增</el-checkbox>
      <el-input
        v-model="keyword"
        placeholder="搜索章节标题"
        clearable
        class="search-input"
        @keyup.enter="reload(1)"
        @clear="reload(1)"
      />
      <el-button type="primary" @click="reload(1)">筛选</el-button>
    </div>

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      :closable="false"
      class="error-alert"
    />

    <div class="table-wrap glass-panel" v-loading="loading">
      <el-table :data="chapters" style="width: 100%">
        <el-table-column prop="orderNo" label="序号" width="80" align="center">
          <template #default="{ row }">{{ String(row.orderNo).padStart(2, '0') }}</template>
        </el-table-column>
        <el-table-column label="章节标题" min-width="240">
          <template #default="{ row }">
            <span class="chapter-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属作品" min-width="180">
          <template #default="{ row }">
            <router-link :to="'/novel/' + row.novelId" class="novel-link">{{ row.novelTitle }}</router-link>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="字数" width="100" align="center">
          <template #default="{ row }">{{ formatNumber(row.wordCount) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PUBLISHED'"
              link
              type="primary"
              @click="router.push('/chapter/' + row.id)"
            >
              阅读
            </el-button>
            <el-button
              v-else
              link
              type="info"
              disabled
              title="草稿章节对读者不可见"
            >
              未发布
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="emptyText" />
        </template>
      </el-table>

      <div class="pagination-wrapper" v-if="total > size">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="reload"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { fetchAuthorChapters, fetchAuthorNovels } from '../../api/author'

const route = useRoute()
const router = useRouter()

const chapters = ref([])
const novelOptions = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const errorMsg = ref('')

// 筛选条件：支持从工作台统计卡点击带参进入
const statusFilter = ref(route.query.status || '')
const novelFilter = ref(route.query.novelId ? Number(route.query.novelId) : null)
const thisWeekFilter = ref(route.query.thisWeek === 'true')
const keyword = ref('')

const pageHeading = computed(() => {
  if (thisWeekFilter.value) return '本周新增章节'
  if (statusFilter.value === 'DRAFT') return '草稿箱'
  if (statusFilter.value === 'PUBLISHED') return '已发布章节'
  return '章节管理'
})

const emptyText = computed(() => (loading.value ? '加载中…' : '没有符合条件的章节'))

const totalWords = computed(() =>
  chapters.value.reduce((sum, c) => sum + (c.wordCount || 0), 0)
)

const buildParams = () => ({
  page: page.value,
  size: size.value,
  status: statusFilter.value || undefined,
  novelId: novelFilter.value || undefined,
  thisWeek: thisWeekFilter.value || undefined,
  keyword: keyword.value || undefined
})

const load = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await fetchAuthorChapters(buildParams())
    chapters.value = res.data
    total.value = res.total
  } catch (err) {
    console.error(err)
    errorMsg.value = '章节列表加载失败。'
  } finally {
    loading.value = false
  }
}

const reload = (p = page.value) => {
  page.value = p
  load()
}

const loadNovelOptions = async () => {
  try {
    const res = await fetchAuthorNovels({ page: 1, size: 100 })
    novelOptions.value = res.data
  } catch (err) {
    console.error(err)
  }
}

// 浏览器前进/后退时同步筛选条件
watch(
  () => route.query,
  (q) => {
    statusFilter.value = q.status || ''
    novelFilter.value = q.novelId ? Number(q.novelId) : null
    thisWeekFilter.value = q.thisWeek === 'true'
    reload(1)
  }
)

const formatNumber = (val) => Number(val || 0).toLocaleString('zh-CN')
const formatDateTime = (val) =>
  val
    ? new Date(val).toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      })
    : '—'

onMounted(() => {
  loadNovelOptions()
  load()
})
</script>

<style scoped>
.list-page {
  padding-top: 32px;
}

.page-head {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 2rem;
  margin: 0;
}

.page-subtitle {
  color: var(--text-sub);
  margin: 4px 0 0;
  font-size: 0.9rem;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.85);
}

.filter-item {
  width: 130px;
}

.filter-novel {
  width: 220px;
}

.search-input {
  max-width: 240px;
}

.error-alert {
  margin-bottom: 16px;
}

.table-wrap {
  padding: 8px 16px 16px;
  background: rgba(255, 255, 255, 0.85);
}

.chapter-title {
  font-weight: 600;
  color: var(--slate-800);
}

.novel-link {
  color: var(--primary-color);
}

.novel-link:hover {
  text-decoration: underline;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
