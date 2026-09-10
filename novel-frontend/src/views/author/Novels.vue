<template>
  <div class="container author-page" v-loading="loading">
    <div class="page-head">
      <el-button plain round icon="ArrowLeft" @click="router.push('/author')">返回工作台</el-button>
      <h1 class="page-title">作品管理</h1>
      <p class="page-sub">共 {{ total }} 部作品（数据来自 /api/novels）</p>
    </div>

    <div class="novel-grid">
      <router-link
        v-for="novel in novels"
        :key="novel.id"
        :to="'/novel/' + novel.id"
        class="novel-card glass-panel hover-lift"
      >
        <div class="cover" :style="{ backgroundImage: 'url(' + novel.coverUrl + ')' }"></div>
        <div class="info">
          <h3 class="title">{{ novel.title }}</h3>
          <p class="desc">{{ novel.description }}</p>
          <div class="meta">创建于 {{ formatDate(novel.createdAt) }}</div>
        </div>
      </router-link>
    </div>

    <el-empty v-if="!loading && novels.length === 0" description="暂无作品" />

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const novels = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const fetchNovels = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_URL}/novels`, {
      params: { page: page.value, size: size.value }
    })
    novels.value = res.data.data
    total.value = res.data.total
  } catch (err) {
    console.error('加载作品列表失败', err)
  } finally {
    loading.value = false
  }
}

const onPageChange = (val) => {
  page.value = val
  fetchNovels()
}

const formatDate = (val) => {
  if (!val) return ''
  return new Date(val).toLocaleDateString('zh-CN')
}

onMounted(fetchNovels)
</script>

<style scoped>
.author-page {
  padding: 40px 20px 60px;
}

.page-head {
  margin-bottom: 30px;
}

.page-title {
  font-size: 2rem;
  margin: 16px 0 6px;
}

.page-sub {
  color: var(--text-sub);
  margin: 0;
}

.novel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.novel-card {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.85);
}

.cover {
  height: 160px;
  background-size: cover;
  background-position: center;
}

.info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 1.15rem;
  margin-bottom: 8px;
}

.desc {
  color: var(--text-sub);
  font-size: 0.88rem;
  line-height: 1.6;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  margin-top: auto;
  font-size: 0.8rem;
  color: var(--slate-400);
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
