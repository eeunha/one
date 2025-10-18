<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
</script>

<template>
  <!--
    ROOT CONTAINER FIX:
    - min-h-screen: 최소한 화면 전체 높이를 채웁니다. (세로 문제 해결)
    - w-full: 화면 전체 너비를 채웁니다. (가로 문제 해결)
    - bg-gray-50: 이 전체 영역에 회색 배경을 적용합니다.
    - 이전의 max-w-7xl, mx-auto, p-8 클래스는 제거되었습니다.
  -->
  <div id="app" class="min-h-screen w-full bg-gray-50">

    <!--
      HEADER:
      - sticky: 상단 고정
      - w-full: 배경색/그림자를 화면 전체에 늘립니다.
      - border-b: 깔끔함을 위해 아래쪽에 얇은 경계선을 추가합니다.
    -->
    <header class="bg-white shadow-lg sticky top-0 z-20 w-full border-b border-gray-200">

      <!--
        HEADER CONTENT WRAPPER:
        - max-w-6xl: 콘텐츠의 최대 너비를 설정합니다 (976px보다 넓은 1152px).
        - mx-auto: 콘텐츠를 항상 중앙에 정렬합니다.
        - px-4 py-3: 좌우/상하 패딩을 추가하여 모바일에서도 보기 좋게 합니다.
        - flex justify-between items-center: 로고와 내비게이션을 좌우로 분리하고 수직 중앙 정렬합니다.
      -->
      <div class="max-w-6xl mx-auto px-4 py-3 flex justify-between items-center">

        <!-- 로고/제목 디자인 변경 -->
        <RouterLink to="/" class="flex items-center space-x-2 text-3xl font-extrabold text-indigo-700 hover:text-indigo-900 transition duration-300">
          <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <span class="hidden sm:inline">JWT Board</span>
        </RouterLink>

        <!-- Navbar 컴포넌트는 여전히 여기 있습니다. -->
        <Navbar />
      </div>
    </header>

    <!--
     MAIN CONTENT:
     - max-w-6xl: 헤더 콘텐츠와 동일한 최대 너비를 사용합니다.
     - mx-auto: 항상 중앙 정렬됩니다.
     - mt-8: 헤더와의 간격을 확보했습니다.
     - p-4 sm:p-8: 전체적으로 충분한 패딩을 제공합니다.
     - 이 <main> 태그가 회색 배경 영역(app div)의 중앙에 배치됩니다.
   -->
    <main class="max-w-6xl mx-auto mt-8 p-4 sm:p-8">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>

/*
  [최종 정리]:
  - nav와 nav a 관련 CSS는 모두 Navbar.vue의 Tailwind 클래스로 대체되어 제거되었습니다.
  - 이제 App.vue의 scoped CSS는 line-height와 같이 전역적으로 제어하기 어려운 최소한의 값만 남았습니다.
*/
header {
  line-height: 1.5;
}
</style>
