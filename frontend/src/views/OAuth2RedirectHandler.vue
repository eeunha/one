<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from '../utils/axios';

const router = useRouter();

onMounted(async () => {
  try {
    // 쿠키 기반으로 profile API 호출 → 로그인 여부 확인
    await axios.get('/auth/profile');
    // 로그인 성공 → profile 페이지로 이동
    router.replace('/profile');
  } catch (err) {
    // 로그인 실패 → login 페이지로 이동
    router.replace('/login');
  }
});
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen">
    <p>로그인 처리 중...</p>
  </div>
</template>
