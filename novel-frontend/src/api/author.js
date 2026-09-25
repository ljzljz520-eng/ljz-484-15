import axios from 'axios'

// 与现有页面保持一致：本地开发直连后端，亦可通过 VITE_API_URL 覆盖
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

/**
 * 作者工作台统计信息（全部由 Java 后端基于本地数据计算）
 */
export function fetchAuthorStats() {
  return axios.get(`${API_URL}/author/stats`).then((res) => res.data)
}

/**
 * 作者视角作品列表
 */
export function fetchAuthorNovels(params = {}) {
  return axios.get(`${API_URL}/author/novels`, { params }).then((res) => res.data)
}

/**
 * 作者视角章节列表（草稿/已发布均可见，支持过滤与本周筛选）
 */
export function fetchAuthorChapters(params = {}) {
  return axios.get(`${API_URL}/author/chapters`, { params }).then((res) => res.data)
}
