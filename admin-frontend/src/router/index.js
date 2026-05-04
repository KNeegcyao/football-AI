import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/UserView.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'posts',
        name: 'Posts',
        component: () => import('../views/post/PostView.vue'),
        meta: { title: '帖子管理', icon: 'Document' }
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('../views/comment/CommentView.vue'),
        meta: { title: '评论管理', icon: 'ChatDotRound' }
      },
      {
        path: 'topics',
        name: 'Topics',
        component: () => import('../views/topic/TopicView.vue'),
        meta: { title: '话题管理', icon: 'PriceTag' }
      },
      {
        path: 'news',
        name: 'News',
        component: () => import('../views/news/NewsView.vue'),
        meta: { title: '资讯管理', icon: 'Notification' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
