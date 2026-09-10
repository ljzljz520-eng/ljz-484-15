<template>
  <div class="container studio-page" v-loading="loading">
    <div class="page-head">
      <div>
        <h1 class="page-title text-gradient">作者工作台</h1>
        <p class="page-sub">统计数据由后端实时计算，点击任意卡片可查看对应列表</p>
      </div>
      <el-tag type="info" effect="plain" round v-if="stats.weekStart">
        本周起：{{ formatDate(stats.weekStart) }}
      </el-tag>
    </div>

    <el-alert
      v-if="loadError"
      class="load-error"
      type="error"
      :closable="false"
      show-icon
      title="统计数据加载失败"
    >
      <template #default>
        <div class="load-error-body">
          <span>无法连接后台统计服务，当前展示的不是真实数据。</span>
          <el-button size="small" type="error" plain round :loading="loading" @click="fetchStats">
            重新加载
          </el-button>
        </div>
      </template>
    </el-alert>

    <div class="stat-grid">
      <div class="stat-card glass-panel hover-lift" @click="go('/author/novels')">
        <div class="stat-icon icon-indigo"><Collection /></div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.novelCount ?? '-' }}</div>
          <div class="stat-label">作品数</div>
        </div>
        <el-icon class="stat-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="stat-card glass-panel hover-lift" @click="go('/author/chapters?status=DRAFT')">
        <div class="stat-icon icon-amber"><EditPen /></div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.draftChapterCount ?? '-' }}</div>
          <div class="stat-label">草稿章节</div>
        </div>
        <el-icon class="stat-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="stat-card glass-panel hover-lift" @click="go('/author/chapters?status=PUBLISHED')">
        <div class="stat-icon icon-green"><Promotion /></div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.publishedChapterCount ?? '-' }}</div>
          <div class="stat-label">已发布章节</div>
        </div>
        <el-icon class="stat-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="stat-card glass-panel hover-lift" @click="go('/author/chapters')">
        <div class="stat-icon icon-slate"><Clock /></div>
        <div class="stat-body">
          <div class="stat-value stat-time">{{ stats.lastUpdatedAt ? formatDate(stats.lastUpdatedAt) : '暂无' }}</div>
          <div class="stat-label">最近更新时间</div>
        </div>
        <el-icon class="stat-arrow"><ArrowRight /></el-icon>
      </div>

      <div class="stat-card stat-card-wide glass-panel hover-lift" @click="go('/author/chapters?weekly=1')">
        <div class="stat-icon icon-purple"><Histogram /></div>
        <div class="stat-body">
          <div class="stat-value">{{ stats.weeklyWordCount ?? '-' }}<span class="unit">字</span></div>
          <div class="stat-label">本周新增字数</div>
        </div>
        <el-icon class="stat-arrow"><ArrowRight /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  Collection, EditPen, Promotion, Clock, Histogram, ArrowRight
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const loadError = ref(false)
const stats = ref({})
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const fetchStats = async () => {
  loading.value = true
  loadError.value = false
  try {
    const res = await axios.get(`${API_URL}/author/stats`)
    stats.value = res.data
  } catch (err) {
    console.error('加载统计信息失败', err)
    stats.value = {}
    loadError.value = true
    ElMessage.error('统计数据加载失败，请检查网络后重试')
  } finally {
    loading.value = false
  }
}

const go = (path) => {
  router.push(path)
}

const formatDate = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(fetchStats)
</script>

<style scoped>
.studio-page {
  padding: 50px 20px 60px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 36px;
}

.page-title {
  font-size: 2.4rem;
  margin-bottom: 8px;
}

.page-sub {
  color: var(--text-sub);
  margin: 0;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.load-error {
  margin-bottom: 24px;
}

.load-error-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  width: 100%;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 28px;
  background: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  position: relative;
}

.stat-card-wide {
  grid-column: span 1;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.icon-indigo { background: rgba(99, 102, 241, 0.12); color: #4f46e5; }
.icon-amber  { background: rgba(245, 158, 11, 0.12); color: #d97706; }
.icon-green  { background: rgba(16, 185, 129, 0.12); color: #059669; }
.icon-slate  { background: rgba(100, 116, 139, 0.12); color: #475569; }
.icon-purple { background: rgba(139, 92, 246, 0.12); color: #7c3aed; }

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: var(--slate-900);
  line-height: 1.2;
}

.stat-time {
  font-size: 1.15rem;
  white-space: nowrap;
}

.unit {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--text-sub);
  margin-left: 6px;
}

.stat-label {
  color: var(--text-sub);
  font-size: 0.92rem;
  margin-top: 4px;
}

.stat-arrow {
  color: var(--slate-300);
  font-size: 1.1rem;
  transition: transform 0.2s, color 0.2s;
}

.stat-card:hover .stat-arrow {
  color: var(--primary-color);
  transform: translateX(4px);
}

@media (max-width: 900px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
