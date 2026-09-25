<template>
  <div class="container list-page">
    <div class="page-head">
      <el-button circle plain icon="ArrowLeft" @click="router.push('/dashboard')"></el-button>
      <div>
        <h1 class="page-title text-gradient">作品管理</h1>
        <p class="page-subtitle">共 {{ total }} 部作品（数据来自后端）</p>
      </div>
    </div>

    <div class="toolbar glass-panel">
      <el-input
        v-model="keyword"
        placeholder="搜索作品标题或简介"
        clearable
        class="search-input"
        @keyup.enter="reload(1)"
        @clear="reload(1)"
      />
      <el-button type="primary" @click="reload(1)">搜索</el-button>
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
      <el-table :data="novels" style="width: 100%" :show-header="true">
        <el-table-column label="作品" min-width="260">
          <template #default="{ row }">
            <router-link :to="'/novel/' + row.id" class="novel-cell">
              <img :src="row.coverUrl" class="mini-cover" alt="" />
              <span class="novel-title">{{ row.title }}</span>
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="totalChapterCount" label="章节总数" width="100" align="center" />
        <el-table-column label="草稿" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.draftChapterCount > 0" type="warning" size="small">
              {{ row.draftChapterCount }}
            </el-tag>
            <span v-else class="muted">0</span>
          </template>
        </el-table-column>
        <el-table-column label="已发布" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.publishedChapterCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总字数" width="110" align="center">
          <template #default="{ row }">{{ formatNumber(row.wordCount) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="最近更新" width="170">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewChapters(row.id)">章节管理</el-button>
            <el-button link type="primary" @click="router.push('/novel/' + row.id)">读者页</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无作品" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { fetchAuthorNovels } from '../../api/author'

const router = useRouter()
const novels = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)
const errorMsg = ref('')

const load = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await fetchAuthorNovels({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined
    })
    novels.value = res.data
    total.value = res.total
  } catch (err) {
    console.error(err)
    errorMsg.value = '作品列表加载失败。'
  } finally {
    loading.value = false
  }
}

const reload = (p = page.value) => {
  page.value = p
  load()
}

const viewChapters = (novelId) => {
  router.push({ path: '/author/chapters', query: { novelId: String(novelId) } })
}

const formatNumber = (val) => Number(val || 0).toLocaleString('zh-CN')
const formatDate = (val) =>
  val ? new Date(val).toLocaleDateString('zh-CN') : '—'
const formatDateTime = (val) =>
  val
    ? new Date(val).toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      })
    : '—'

onMounted(load)
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
  gap: 12px;
  padding: 16px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.85);
}

.search-input {
  max-width: 360px;
}

.error-alert {
  margin-bottom: 16px;
}

.table-wrap {
  padding: 8px 16px 16px;
  background: rgba(255, 255, 255, 0.85);
}

.novel-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mini-cover {
  width: 48px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.novel-title {
  font-weight: 600;
  color: var(--slate-800);
}

.novel-title:hover {
  color: var(--primary-color);
}

.muted {
  color: var(--slate-400);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
