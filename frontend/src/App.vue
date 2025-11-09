<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
</script>

<template>
  <!--
    ROOT CONTAINER: min-h-screen, w-full, overflow-x-hidden 유지.
    tailwind의 기본 스타일 리셋 덕분에 이제 깔끔하게 작동합니다.
  -->
  <div id="app" class="min-h-screen w-full bg-gray-50 overflow-x-hidden">

    <!-- HEADER: 배경 확장을 위해 w-full 사용 -->
    <header class="bg-white shadow-lg sticky top-0 z-20 w-full border-b border-gray-200">

      <!-- [CONTENT WRAPPER]: 중앙 정렬 담당 (max-w-6xl, mx-auto, w-full) -->
      <div class="max-w-6xl mx-auto px-4 py-3 flex justify-between items-center w-full">

        <!-- 로고/제목 -->
        <RouterLink to="/" class="flex items-center space-x-2 text-3xl font-extrabold text-emerald-600 hover:text-emerald-800 transition duration-300">
          <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <span class="hidden sm:inline">Routinote</span>
        </RouterLink>

        <!-- Navbar 컴포넌트 -->
        <Navbar />
      </div>
    </header>

    <!-- MAIN CONTENT: 중앙 정렬 담당 -->
    <main class="max-w-6xl mx-auto w-full mt-8 mb-8 p-4 sm:p-8 bg-white rounded-xl shadow-2xl">
      <RouterView />
    </main>
  </div>
</template>

<!--
  Global Style Block (base.css 역할을 대체):
  1. 폰트 설정 (가장 중요)
  2. 최소한의 html/body 여백 정리 (혹시 모를 브라우저 간섭 방지)
-->
<style>
/* 1. 폰트 설정: 기본 시스템 폰트(Inter)와 함께 한글 폰트를 지정하여 가독성 확보 */
html {
  /* Inter, system-ui, Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol, Noto Color Emoji */
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
}

/* 2. html/body 여백 정리: !important를 제거하고, Tailwind가 기본적으로 제공하는 reset을 신뢰합니다. */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  /* 스크롤바 튀어나옴 방지를 위해 body 레벨에서 overflow-x 숨김 */
  overflow-x: hidden;
}

/* 3. 기타 전역 스타일 */
header, #app {
  line-height: 1.5;
}

/* router-link의 white-space: nowrap 스타일은 유지합니다. */
.router-link-active {
  white-space: nowrap;
}
</style>