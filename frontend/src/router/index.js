import { createRouter, createWebHistory } from 'vue-router';
import axios from '../utils/axios'; // axios 인스턴스 import
import HomeView from '../views/HomeView.vue';
import TestView from '../views/TestView.vue';
import LoginView from "@/views/LoginView.vue";
import ProfileView from "@/views/ProfileView.vue";
import OAuth2RedirectHandler from "@/views/OAuth2RedirectHandler.vue";

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/about', name: 'about', component: () => import('../views/AboutView.vue') },
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
  if (!to.meta.requiresAuth) {
    return next(); // 인증 필요 없는 페이지는 바로 통과
  }

  try {
    // 백엔드 profile API 호출 → 쿠키 기반 JWT 유효성 확인
    await axios.get('/auth/profile', { withCredentials: true });
    next(); // 토큰 유효 → 이동 허용
  } catch (err) {
    // 토큰 없거나 만료 → 로그인 필요
    const confirmLogin = window.confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?');
    if (confirmLogin) next('/login');
    else next('/');
  }
});

export default router;
