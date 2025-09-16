import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import TestView from '../views/TestView.vue';
import LoginView from "@/views/LoginView.vue";
import ProfileView from "@/views/ProfileView.vue";
import OAuth2RedirectHandler from "@/views/OAuth2RedirectHandler.vue";
import { useAuthStore } from '@/stores/auth.js'; // ★ Pinia 스토어 import

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/test', name: 'test', component: TestView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
  { path: '/oauth2/redirect', name: 'oauth2-redirect', component: OAuth2RedirectHandler },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 전역 네비게이션 가드
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const isLoggedIn = !!authStore.accessToken;

  // 인증이 필요한 페이지에 접근했고, 로그인 상태가 아닌 경우
  if (to.meta.requiresAuth && !isLoggedIn) {
    const confirmLogin = window.confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?');
    if (confirmLogin) {
      next('/login');
    } else {
      next('/');
    }
  } else if (to.name === 'login' && isLoggedIn) {
      // 이미 로그인한 상태에서 로그인 페이지에 접근하는 경우
      next('/profile');
  } else {
      // 그 외의 경우 (인증이 필요 없거나, 이미 로그인한 상태에서 다른 페이지로 이동)
      next();
  }
});

export default router;
