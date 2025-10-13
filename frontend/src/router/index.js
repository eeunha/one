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

  const isLoggedIn = !!authStore.accessToken;
  console.log("beforeEach - isLoggedIn:", isLoggedIn, "accessToken:", authStore.accessToken)

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
