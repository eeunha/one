<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

onMounted(async () => {
  try {
    // 백엔드에서 JWT 쿠키 확인 + 프로필 가져오기
    await userStore.fetchProfile();

    // 로그인 완료 후 profile 페이지로 이동
    router.push('/profile');
  } catch (err) {
    // 실패하면 로그인 페이지로
    router.push('/login');
  }
});
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen">
    <p>로그인 처리 중입니다...</p>
  </div>
</template>
