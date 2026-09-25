<template>
  <div class="container dashboard-page">
    <section class="dashboard-header">
      <h1 class="page-title text-gradient">作者工作台</h1>
      <p class="page-subtitle">
        数据由后端实时统计
        <span v-if="stats" class="calc-time">（计算于 {{ formatDateTime(stats.calculatedAt) }}）</span>
      </p>
    </section>

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      :closable="false"
      class="error-alert"
    >
      <el-button type="primary" size="small" @click="loadStats">重试</el-button>
    </el-alert>

    <section v-loading="loading" class="stat-grid">
      <article
        v-for="item in statCards"
        :key="item.key"
        class="stat-card glass-panel hover-lift"
        role="button"
        tabindex="0"
        @click="goList(item.to)"
        @keyup.enter="goList(item.to)"
      >
        <div class="stat-icon" :style="{ background: item.bg }">{{ item.icon }}</div>
        <div class="stat-body">
          <span class="stat-label">{{ item.label }}</span>
          <span class="stat-value" v-if="item.key !== 'lastUpdatedAt'">
            {{ formatNumber(item.value) }}<em class="stat-unit">{{ item.unit }}</em>
          </span>
          <span class="stat-value stat-value--time" v-else>{{ formatDateTime(item.value) || '—' }}</span>
          <span class="stat-hint">{{ item.hint }} ›</span>
        </div>
      </article>
    </section>

    <section class="quick-entry glass-panel">
      <h2 class="entry-title">快捷入口</h2>
      <div class="entry-actions">
        <el-button type="primary" round @click="goList('/author/novels')">管理作品</el-button>
        <el-button round @click="goList('/author/chapters')">全部章节</el-button>
        <el-button round @click="goList('/author/chapters?status=DRAFT')">草稿箱</el-button>
        <el-button round @click="goList('/author/chapters?thisWeek=true')">本周更新</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAuthorStats } from '../../api/author'

const router = useRouter()
const stats = ref(null)
const loading = ref(true)
const errorMsg = ref('')

const statCards = computed(() => {
  const s = stats.value
  return [
    {
      key: 'novelCount',
      label: '作品数',
      value: s?.novelCount ?? 0,
      unit: '部',
      hint: '查看作品列表',
      to: '/author/novels',
      icon: '📚',
      bg: 'linear-gradient(135deg,#6366f1,#8b5cf6)'
    },
    {
      key: 'draftChapterCount',
      label: '草稿章节',
      value: s?.draftChapterCount ?? 0,
      unit: '章',
      hint: '进入草稿箱',
      to: '/author/chapters?status=DRAFT',
      icon: '📝',
      bg: 'linear-gradient(135deg,#f59e0b,#f97316)'
    },
    {
      key: 'publishedChapterCount',
      label: '已发布章节',
      value: s?.publishedChapterCount ?? 0,
      unit: '章',
      hint: '查看已发布章节',
      to: '/author/chapters?status=PUBLISHED',
      icon: '🚀',
      bg: 'linear-gradient(135deg,#10b981,#059669)'
    },
    {
      key: 'lastUpdatedAt',
      label: '最近更新时间',
      value: s?.lastUpdatedAt || '',
      unit: '',
      hint: '查看最近章节',
      to: '/author/chapters',
      icon: '🕒',
      bg: 'linear-gradient(135deg,#0ea5e9,#3b82f6)'
    },
    {
      key: 'weeklyNewWords',
      label: '本周新增字数',
      value: s?.weeklyNewWords ?? 0,
      unit: '字',
      hint: `本周新增 ${s?.weeklyNewChapterCount ?? 0} 章，点击查看`,
      to: '/author/chapters?thisWeek=true',
      icon: '✍️',
      bg: 'linear-gradient(135deg,#ec4899,#f43f5e)'
    }
  ]
})

const loadStats = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    stats.value = await fetchAuthorStats()
  } catch (err) {
    console.error(err)
    errorMsg.value = '统计信息加载失败，请确认后端服务已启动。'
  } finally {
    loading.value = false
  }
}

const goList = (path) => {
  router.push(path)
}

const formatNumber = (val) => Number(val || 0).toLocaleString('zh-CN')

const formatDateTime = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(loadStats)
</script>

<style scoped>
.dashboard-page {
  padding-top: 40px;
}

.page-title {
  font-size: 2.4rem;
  margin-bottom: 8px;
}

.page-subtitle {
  color: var(--text-sub);
  margin-bottom: 8px;
}

.calc-time {
  font-size: 0.85rem;
}

.error-alert {
  margin-bottom: 24px;
  align-items: center;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
  margin: 28px 0 36px;
  min-height: 140px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.85);
  outline: none;
}

.stat-card:focus-visible {
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.35);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.6rem;
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.12);
}

.stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.stat-label {
  font-size: 0.9rem;
  color: var(--text-sub);
}

.stat-value {
  font-size: 1.9rem;
  font-weight: 800;
  line-height: 1.15;
  color: var(--slate-800);
}

.stat-value--time {
  font-size: 1.15rem;
  font-weight: 700;
}

.stat-unit {
  font-style: normal;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-sub);
  margin-left: 4px;
}

.stat-hint {
  font-size: 0.8rem;
  color: var(--primary-color);
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.2s;
}

.stat-card:hover .stat-hint {
  opacity: 1;
  transform: translateX(0);
}

.quick-entry {
  padding: 28px 32px;
  background: rgba(255, 255, 255, 0.85);
}

.entry-title {
  font-size: 1.15rem;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 4px solid var(--primary-color);
}

.entry-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
