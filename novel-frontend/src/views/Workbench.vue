<template>
  <div class="container workbench-page">
    <!-- 页头 -->
    <div class="page-header">
      <div>
        <h1 class="page-title text-gradient">作者工作台</h1>
        <p class="page-subtitle">数据实时统计自服务端</p>
      </div>
      <el-button round plain :icon="Refresh" :loading="statsLoading || listLoading" @click="refresh">刷新数据</el-button>
    </div>

    <!-- 统计卡片：点击进入对应列表 -->
    <div class="stat-grid" v-loading="statsLoading">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="stat-card glass-panel hover-lift"
        :class="{ active: activeTab === card.key }"
        @click="switchTab(card.key)"
      >
        <div class="stat-icon" :style="{ background: card.bg }">{{ card.icon }}</div>
        <div class="stat-body">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">
            <template v-if="stats">
              <span v-if="card.type === 'time'" class="stat-time">{{ card.value ? formatDateTime(card.value) : '暂无更新' }}</span>
              <template v-else>
                <span class="stat-number">{{ formatNumber(card.value) }}</span>
                <span v-if="card.unit" class="stat-unit">{{ card.unit }}</span>
              </template>
            </template>
            <template v-else><el-skeleton-item variant="text" class="stat-skeleton" /></template>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表区域 -->
    <div class="list-panel glass-panel" v-loading="listLoading">
      <el-tabs :model-value="activeTab" class="list-tabs" @tab-change="switchTab">
        <el-tab-pane label="作品列表" name="novels">
          <div v-if="activeTab === 'novels'" class="tab-body">
            <div v-if="novels.length" class="novel-grid">
              <div v-for="novel in novels" :key="novel.id" class="novel-row" @click="openNovel(novel.id)">
                <img class="novel-cover" :src="novel.coverUrl" :alt="novel.title" />
                <div class="novel-info">
                  <h3 class="novel-name">{{ novel.title }}</h3>
                  <p class="novel-desc">{{ novel.description }}</p>
                  <div class="novel-meta">
                    <el-tag size="small" type="success">已发布 {{ novel.publishedChapters }}</el-tag>
                    <el-tag size="small" type="info">草稿 {{ novel.draftChapters }}</el-tag>
                    <span class="meta-text">{{ formatNumber(novel.wordCount) }} 字</span>
                    <span class="meta-text">更新于 {{ formatDateTime(novel.lastUpdatedAt) }}</span>
                  </div>
                </div>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
            <el-empty v-else-if="!listLoading" description="还没有作品" />
          </div>
        </el-tab-pane>

        <el-tab-pane name="drafts">
          <template #label>
            草稿章节<span v-if="stats" class="tab-count">{{ stats.draftChapterCount }}</span>
          </template>
          <div v-if="activeTab === 'drafts'" class="tab-body">
            <el-table v-if="chapters.length" :data="chapters" stripe @row-click="openChapter" class="chapter-table">
              <el-table-column label="章节" width="80">
                <template #default="{ row }">{{ padOrder(row.orderNo) }}</template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="220" />
              <el-table-column prop="novelTitle" label="所属作品" min-width="160" />
              <el-table-column label="字数" width="110">
                <template #default="{ row }">{{ formatNumber(row.wordCount) }}</template>
              </el-table-column>
              <el-table-column label="更新时间" width="180">
                <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }"><el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag></template>
              </el-table-column>
            </el-table>
            <el-empty v-else-if="!listLoading" description="暂无草稿章节" />
          </div>
        </el-tab-pane>

        <el-tab-pane name="published">
          <template #label>
            已发布章节<span v-if="stats" class="tab-count">{{ stats.publishedChapterCount }}</span>
          </template>
          <div v-if="activeTab === 'published'" class="tab-body">
            <el-table v-if="chapters.length" :data="chapters" stripe @row-click="openChapter" class="chapter-table">
              <el-table-column label="章节" width="80">
                <template #default="{ row }">{{ padOrder(row.orderNo) }}</template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="220" />
              <el-table-column prop="novelTitle" label="所属作品" min-width="160" />
              <el-table-column label="字数" width="110">
                <template #default="{ row }">{{ formatNumber(row.wordCount) }}</template>
              </el-table-column>
              <el-table-column label="更新时间" width="180">
                <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }"><el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag></template>
              </el-table-column>
            </el-table>
            <el-empty v-else-if="!listLoading" description="暂无已发布章节" />
          </div>
        </el-tab-pane>

        <el-tab-pane name="recent">
          <template #label>最近更新</template>
          <div v-if="activeTab === 'recent'" class="tab-body">
            <el-table v-if="chapters.length" :data="chapters" stripe @row-click="openChapter" class="chapter-table">
              <el-table-column label="章节" width="80">
                <template #default="{ row }">{{ padOrder(row.orderNo) }}</template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="220" />
              <el-table-column prop="novelTitle" label="所属作品" min-width="160" />
              <el-table-column label="字数" width="110">
                <template #default="{ row }">{{ formatNumber(row.wordCount) }}</template>
              </el-table-column>
              <el-table-column label="更新时间" width="180">
                <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }"><el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag></template>
              </el-table-column>
            </el-table>
            <el-empty v-else-if="!listLoading" description="暂无章节" />
          </div>
        </el-tab-pane>

        <el-tab-pane name="weekly">
          <template #label>本周新增</template>
          <div v-if="activeTab === 'weekly'" class="tab-body">
            <el-alert v-if="stats" type="success" :closable="false" class="weekly-alert">
              <template #title>
                本周（自 {{ formatDate(weekStart) }} 起）新增
                <b>{{ formatNumber(stats.weeklyWordCount) }}</b> 字，共 {{ chapters.length }} 个章节（含草稿）
              </template>
            </el-alert>
            <el-table v-if="chapters.length" :data="chapters" stripe @row-click="openChapter" class="chapter-table">
              <el-table-column label="章节" width="80">
                <template #default="{ row }">{{ padOrder(row.orderNo) }}</template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="220" />
              <el-table-column prop="novelTitle" label="所属作品" min-width="160" />
              <el-table-column label="字数" width="110">
                <template #default="{ row }">{{ formatNumber(row.wordCount) }}</template>
              </el-table-column>
              <el-table-column label="创建时间" width="180">
                <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }"><el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag></template>
              </el-table-column>
            </el-table>
            <el-empty v-else-if="!listLoading" description="本周还没有新增章节" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { Refresh, ArrowRight } from '@element-plus/icons-vue'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const route = useRoute()
const router = useRouter()

const stats = ref(null)
const statsLoading = ref(false)
const novels = ref([])
const chapters = ref([])
const listLoading = ref(false)

const VALID_TABS = ['novels', 'drafts', 'published', 'recent', 'weekly']
const activeTab = ref(VALID_TABS.includes(route.query.tab) ? route.query.tab : 'novels')

const weekStart = computed(() => {
  const now = new Date()
  const day = (now.getDay() + 6) % 7 // 周一为 0
  const d = new Date(now)
  d.setHours(0, 0, 0, 0)
  d.setDate(d.getDate() - day)
  return d
})

const statCards = computed(() => [
  { key: 'novels', label: '作品数', value: stats.value?.novelCount ?? 0, unit: '部', icon: '📚', bg: 'linear-gradient(135deg,#6366f1,#818cf8)' },
  { key: 'drafts', label: '草稿章节', value: stats.value?.draftChapterCount ?? 0, unit: '章', icon: '✏️', bg: 'linear-gradient(135deg,#f59e0b,#fbbf24)' },
  { key: 'published', label: '已发布章节', value: stats.value?.publishedChapterCount ?? 0, unit: '章', icon: '✅', bg: 'linear-gradient(135deg,#10b981,#34d399)' },
  { key: 'recent', label: '最近更新', value: stats.value?.lastUpdatedAt ?? null, icon: '🕐', bg: 'linear-gradient(135deg,#0ea5e9,#38bdf8)', type: 'time' },
  { key: 'weekly', label: '本周新增字数', value: stats.value?.weeklyWordCount ?? 0, unit: '字', icon: '🔥', bg: 'linear-gradient(135deg,#ef4444,#f97316)' }
])

const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await axios.get(`${API_URL}/author/stats`)
    stats.value = res.data
  } catch (err) {
    console.error('加载统计信息失败', err)
  } finally {
    statsLoading.value = false
  }
}

const fetchList = async (tab) => {
  listLoading.value = true
  try {
    if (tab === 'novels') {
      const res = await axios.get(`${API_URL}/author/novels`)
      novels.value = res.data
    } else {
      const params = {}
      if (tab === 'drafts') params.status = 'DRAFT'
      if (tab === 'published') params.status = 'PUBLISHED'
      if (tab === 'weekly') params.thisWeek = true
      // tab === 'recent' 时不带过滤参数：全部章节按更新时间倒序
      const res = await axios.get(`${API_URL}/author/chapters`, { params })
      chapters.value = res.data
    }
  } catch (err) {
    console.error('加载列表失败', err)
  } finally {
    listLoading.value = false
  }
}

const switchTab = (tab) => {
  if (!VALID_TABS.includes(tab) || tab === activeTab.value) return
  activeTab.value = tab
  router.replace({ query: { ...route.query, tab } })
  fetchList(tab)
}

const refresh = () => {
  fetchStats()
  fetchList(activeTab.value)
}

watch(() => route.query.tab, (tab) => {
  if (VALID_TABS.includes(tab) && tab !== activeTab.value) {
    activeTab.value = tab
    fetchList(tab)
  }
})

const openNovel = (id) => router.push(`/novel/${id}`)
const openChapter = (row) => router.push(`/chapter/${row.id}`)

const statusText = (status) => (status === 'PUBLISHED' ? '已发布' : status === 'DRAFT' ? '草稿' : status)
const statusTagType = (status) => (status === 'PUBLISHED' ? 'success' : 'info')
const padOrder = (n) => (n == null ? '' : String(n).padStart(2, '0'))
const formatNumber = (n) => Number(n ?? 0).toLocaleString('zh-CN')

const formatDateTime = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}
const formatDate = (val) => {
  if (!val) return ''
  return val.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(() => {
  fetchStats()
  fetchList(activeTab.value)
})
</script>

<style scoped>
.workbench-page {
  padding-top: 40px;
  padding-bottom: 60px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 30px;
  gap: 20px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 2.2rem;
  margin-bottom: 8px;
}

.page-subtitle {
  color: var(--text-sub);
  margin: 0;
  font-size: 0.95rem;
}

.server-time {
  white-space: nowrap;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 20px;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.stat-card.active {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2), var(--shadow-lg);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  flex-shrink: 0;
}

.stat-body {
  min-width: 0;
}

.stat-label {
  color: var(--text-sub);
  font-size: 0.9rem;
  margin-bottom: 6px;
}

.stat-value {
  line-height: 1.1;
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: var(--slate-900);
}

.stat-number {
  font-size: 1.8rem;
  font-weight: 700;
}

.stat-time {
  font-size: 0.95rem;
  font-weight: 600;
  white-space: nowrap;
}

.stat-unit {
  font-size: 0.9rem;
  font-weight: 400;
  color: var(--text-sub);
}

.stat-skeleton {
  width: 70px;
}

.list-panel {
  padding: 10px 28px 28px;
  background: rgba(255, 255, 255, 0.8);
}

.tab-body {
  padding-top: 10px;
  min-height: 200px;
}

.tab-count {
  display: inline-block;
  margin-left: 6px;
  padding: 0 8px;
  font-size: 0.75rem;
  line-height: 18px;
  border-radius: 9px;
  background: var(--slate-200);
  color: var(--slate-600);
}

.novel-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.novel-row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px;
  background: var(--slate-50);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.novel-row:hover {
  background: white;
  border-color: var(--primary-color);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.novel-cover {
  width: 80px;
  height: 106px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.novel-info {
  flex: 1;
  min-width: 0;
}

.novel-name {
  font-size: 1.1rem;
  margin-bottom: 6px;
}

.novel-desc {
  color: var(--text-sub);
  font-size: 0.88rem;
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.novel-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-text {
  font-size: 0.82rem;
  color: var(--slate-500);
}

.arrow-icon {
  color: var(--slate-300);
  font-size: 1.2rem;
  flex-shrink: 0;
}

:deep(.chapter-table .el-table__row) {
  cursor: pointer;
}

.weekly-alert {
  margin-bottom: 16px;
}

@media (max-width: 640px) {
  .stat-grid {
    grid-template-columns: 1fr 1fr;
  }
  .novel-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
