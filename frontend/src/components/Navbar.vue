<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
<!--  <nav class="flex space-x-4 border-b border-gray-200 mb-6 pb-2">-->
  <nav class="flex space-x-4 border-gray-200">
    <!-- Tailwind CSS를 사용하여 스타일을 인라인으로 변경했습니다. -->
    <RouterLink
        to="/"
        class="text-gray-600 hover:text-indigo-600 transition duration-150 p-2 rounded-md"
        active-class="font-bold text-indigo-600 bg-indigo-50"
    >
      Home
    </RouterLink>
    <RouterLink
        to="/board"
        class="text-gray-600 hover:text-indigo-600 transition duration-150 p-2 rounded-md"
        active-class="font-bold text-indigo-600 bg-indigo-50"
    >
      게시판
    </RouterLink>
    <RouterLink
        to="/login"
        class="text-gray-600 hover:text-indigo-600 transition duration-150 p-2 rounded-md"
        active-class="font-bold text-indigo-600 bg-indigo-50"
    >
      Login
    </RouterLink>
    <RouterLink
        to="/profile"
        class="text-gray-600 hover:text-indigo-600 transition duration-150 p-2 rounded-md"
        active-class="font-bold text-indigo-600 bg-indigo-50"
    >
      Profile
    </RouterLink>
    <!-- Test 경로는 사용하지 않을 것 같아 임시 제거합니다. 필요하면 다시 추가하세요. -->
  </nav>
</template>

<style scoped>

</style>