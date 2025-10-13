import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/useAuthStore.js';

import HomeView from '../views/HomeView.vue';
import TestView from '../views/TestView.vue';
import LoginView from "@/views/LoginView.vue";
import ProfileView from "@/views/ProfileView.vue";
import OAuth2RedirectHandler from "@/views/OAuth2RedirectHandler.vue";

import BoardListView from "@/views/BoardListView.vue";
import BoardDetailView from "@/views/BoardDetailView.vue";
import BoardWriteView from "@/views/BoardWriteView.vue";

const routes = [
  { path: '/', name: 'Home', component: HomeView },
  { path: '/test', name: 'Test', component: TestView },
  { path: '/login', name: 'Login', component: LoginView },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfileView,
    meta: { requiresAuth: true } // 인증 필수
  },
  { path: '/oauth2/redirect', name: 'Oauth2Redirect', component: OAuth2RedirectHandler },

    // 게시판
  { path: '/board', name: 'BoardList', component: BoardListView },
  {
    path: '/board/:id',
    name: 'BoardDetail',
    component: BoardDetailView,
    props: true
  }, // URL 파라미터를 props로 전달
  {
    path: '/board/write',
    name: 'BoardWrite',
    component: BoardWriteView,
    meta: {requiresAuth: true} // 인증 필수
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

/**
 * 전역 네비게이션 가드:
 * 1. 인증이 필요한 페이지 접근 시 JWT 상태 복원 및 로그인 체크를 수행합니다.
 * 2. 로그인 상태에서 로그인 페이지 접근을 차단합니다.
 */
router.beforeEach(async (to, from, next) => {

  const authStore = useAuthStore();

  console.log("beforeEach");

  // 인증이 필요한 페이지에 접근 시
  if (to.meta.requiresAuth) {

    // Pinia 상태에 인증 정보가 없다면 복원 시도
    if (!authStore.accessToken) {
      await authStore.restoreAuth();
    }

    // 복원 후에도 인증되지 않았다면 로그인 페이지로 리다이렉션
    if (!authStore.isAuthenticated) {
      console.log(`[Auth Guard] ${to.name} 접근 실패: 로그인이 필요합니다.`);
      next({name: 'Login'});
      return;
    }
  }

  // 인증 불필요한 페이지 접근 시
  // 로그인 했는데 로그인 페이지 접근 시
  if (to.name === 'Login' && authStore.isAuthenticated) {
    console.log(`[Auth Guard] 이미 로그인됨. ${to.name} 대신 Profile로 이동`);
    next({ name: 'Profile' });
    return;
  }

  // 그 외의 경우
  next();
});

export default router;
