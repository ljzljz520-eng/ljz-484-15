import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Detail from '../views/Detail.vue'
import Read from '../views/Read.vue'
import Dashboard from '../views/author/Dashboard.vue'
import NovelList from '../views/author/NovelList.vue'
import ChapterList from '../views/author/ChapterList.vue'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home
    },
    {
        path: '/novel/:id',
        name: 'Detail',
        component: Detail
    },
    {
        path: '/chapter/:id',
        name: 'Read',
        component: Read
    },
    {
        path: '/dashboard',
        name: 'AuthorDashboard',
        component: Dashboard
    },
    {
        path: '/author/novels',
        name: 'AuthorNovels',
        component: NovelList
    },
    {
        path: '/author/chapters',
        name: 'AuthorChapters',
        component: ChapterList
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
