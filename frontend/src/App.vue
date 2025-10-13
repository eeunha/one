<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
</script>

<template>
  <div>
    <header class="bg-white shadow-md p-4 sticky top-0 z-10">
      <!-- flex items-center space-x-8: 가로로 나열하고 8단위의 간격을 부여합니다. -->
      <!-- ⭐️ 유지: wrapper에 max-w-4xl을 적용하여 내부 컨텐츠만 중앙 정렬 및 너비 제한 -->
      <div class="wrapper max-w-4xl mx-auto flex items-center space-x-8">
        <h1 class="text-2xl font-extrabold text-indigo-700 whitespace-nowrap">JWT Board</h1>
        <Navbar /> <!-- Navbar 컴포넌트 사용 -->
      </div>
    </header>

    <main class="flex-grow max-w-4xl mx-auto w-full p-4 sm:p-6"> <!-- ⭐️ 유지: main 컨텐츠는 너비 제한 -->
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
header {
  line-height: 1.5;
  max-height: 100vh;
}

nav {
  width: 100%;
  font-size: 12px;
  text-align: center;
  margin-top: 2rem;
}

nav a.router-link-exact-active {
  color: var(--color-text);
}

nav a.router-link-exact-active:hover {
  background-color: transparent;
}

nav a {
  display: inline-block;
  padding: 0 1rem;
  border-left: 1px solid var(--color-border);
}

nav a:first-of-type {
  border: 0;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }

  nav {
    text-align: left;
    margin-left: -1rem;
    font-size: 1rem;

    padding: 1rem 0;
    margin-top: 1rem;
  }
}
</style>
