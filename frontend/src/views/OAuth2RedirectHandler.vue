<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
import axios from '@/utils/axios';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore(); // 스토어 인스턴스 사용

onMounted(async () => {
  console.log('OAuth2RedirectHandler onMounted');

  const code = route.query.code;

  if (code) {
    console.log('if code');
    try {
      console.log('try');
      // 1. 백엔드 API 호출 (code를 쿼리 파라미터로 전달)
      const response = await axios.post(`/auth/google/login`, { code: code });

      console.log('response: ', response);

      // 2. 응답 데이터에서 토큰과 프로필 정보 추출
      const { accessToken, id, name, email } = response.data; // 순서 상관 X
      console.log("로그인 성공! 응답 데이터:", response.data);

      // 3. Pinia 스토어에 로그인 정보 저장
      // setLoginInfo는 액세스 토큰과 사용자 데이터를 받습니다.
      authStore.setLoginInfo(accessToken, { id, name, email });

      // 4. 프로필 페이지로 이동하면서 받은 데이터를 라우터의 state로 전달
      // 이렇게 하면 Profile.vue가 API를 다시 호출할 필요가 없습니다.
      router.push('/profile');

    } catch (err) {
      console.error("로그인 중 오류가 발생했습니다:", err);
      // 실패하면 로그인 페이지로
      router.push('/login');
    }
  } else {
    // code가 없는 경우, 로그인 페이지로 리다이렉트
    console.error("인증 코드가 없습니다.");
    router.push('/login');
  }
});
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen">
    <p>로그인 처리 중입니다...</p>
  </div>
</template>
