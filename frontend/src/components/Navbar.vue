<script setup>
import { RouterLink, useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/useAuthStore.js';
import { storeToRefs } from 'pinia';
import axios from '@/utils/axios';
import { computed } from 'vue';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const { user } = storeToRefs(authStore);

// 사용자 로그인 상태를 계산합니다.
const isLoggedIn = computed(() => !!user.value);

// 3. Tailwind CSS 클래스 정의 (반복되는 스타일을 상수로 관리)
const baseClasses = 'transition duration-150 p-2 rounded-md flex items-center';
const inactiveClasses = 'text-gray-600 hover:text-indigo-600';
const activeClasses = 'font-bold text-indigo-600 bg-indigo-50';

/**
 * 게시판 메뉴의 활성 상태를 수동으로 계산하는 함수를 정의합니다.
 * 현재 경로가 '/board'로 시작하는지 (목록, 상세, 작성, 수정 모두 포함) 확인합니다.
 */
const isBoardActive = () => {
  return route.path.startsWith('/board');
};

/**
 * 주어진 경로가 현재 경로와 정확히 일치하는지 확인하여 활성 상태를 결정합니다.
 */
const isActivePath = (path) => {
  return route.path === path;
};

/**
 * 로그아웃 처리 함수
 */
const logout = async () => {
  // [중요] confirm() 대신 커스텀 모달 UI가 필요하지만, 여기서는 console.log로 대체합니다.
  console.log('로그아웃 요청됨. (실제 앱에서는 모달을 띄워 사용자 확인을 받아야 합니다.)');

  try {
    // 1. 백엔드 로그아웃 API 호출
    await axios.post('/auth/logout');

    // 2. Pinia 스토어에서 정보 제거
    authStore.clearLoginInfo();

    console.log('로그아웃 완료');

    // 3. 로그인 페이지로 리다이렉트
    router.push('/login');

  } catch (err) {
    console.error('로그아웃 실패:', err);
    // [중요] alert() 대신 커스텀 모달 UI가 필요합니다.
    console.error('로그아웃 실패 처리: 서버 오류가 발생했으나 로컬 정보는 삭제됩니다.');
    // API 호출 실패 시에도 UX를 위해 로컬 정보는 지워주는 것이 일반적입니다.
    authStore.clearLoginInfo();
    router.push('/login');
  }
};
</script>

<template>
  <nav class="flex space-x-4 border-gray-200 items-center">

    <RouterLink
        to="/"
        :class="[
            baseClasses,
            isActivePath('/') ? activeClasses : inactiveClasses
        ]"
    >
      Home
    </RouterLink>
    <RouterLink
        to="/board"
        :class="[
            baseClasses,
            isBoardActive() ? activeClasses : inactiveClasses
        ]"
    >
      게시판
    </RouterLink>

    <!-- 로그인 시에만 Profile 링크 표시 -->
    <RouterLink
        v-if="isLoggedIn"
        to="/profile"
        :class="[
            baseClasses,
            isActivePath('/profile') ? activeClasses : inactiveClasses
        ]"
    >
      Profile
    </RouterLink>

    <!-- 로그인 상태에 따른 조건부 렌더링: Login 링크 -->
    <RouterLink
        v-if="!isLoggedIn"
        to="/login"
        :class="[
            baseClasses,
            isActivePath('/login') ? activeClasses : inactiveClasses
        ]"
    >
      Login
    </RouterLink>

    <!-- 로그인 시에만 사용자명, 로그아웃 버튼 표시 -->
    <div
        v-if="isLoggedIn"
        class="ml-6 text-gray-600 font-medium text-sm flex items-center space-x-3">
      <p>
        {{user.name}} 님
      </p>

      <button
          @click="logout"
          class="border border-gray-300 leading-tight transition duration-150 p-2 rounded-md hover:bg-red-50 hover:text-red-600 hover:border-red-300"
      >
        Logout
      </button>
    </div>
  </nav>
</template>
