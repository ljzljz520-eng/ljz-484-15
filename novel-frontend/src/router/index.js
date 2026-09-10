import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Detail from '../views/Detail.vue'
import Read from '../views/Read.vue'
import Studio from '../views/author/Studio.vue'
import AuthorNovels from '../views/author/Novels.vue'
import AuthorChapters from '../views/author/Chapters.vue'

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
        path: '/author',
        name: 'AuthorStudio',
        component: Studio
    },
    {
        path: '/author/novels',
        name: 'AuthorNovels',
        component: AuthorNovels
    },
    {
        path: '/author/chapters',
        name: 'AuthorChapters',
        component: AuthorChapters
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
