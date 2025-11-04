<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/useAuthStore.js';
import { publicClient } from '@/utils/axios';
import ConfirmationModal from '@/components/ConfirmationModal.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore(); // 스토어 인스턴스 사용

const isModalOpen = ref(false);
const modalTitle = ref('로그인 실패');
const modalMessage = ref('');
const isErrorModal = ref(false);


onMounted(async () => {

  const code = route.query.code;

  if (code) {
    try {
      // 1. 백엔드 API 호출 (code를 쿼리 파라미터로 전달)
      const response = await publicClient.post(`/auth/google/login`, { code: code });

      // 2. 응답 데이터에서 토큰과 프로필 정보 추출
      const { accessToken, id, name, email } = response.data; // 순서 상관 X

      console.log("로그인 성공! 응답 데이터:", response.data);

      // 3. Pinia 스토어에 로그인 정보 저장
      // setLoginInfo는 액세스 토큰과 사용자 데이터를 받습니다.
      authStore.setLoginInfo(accessToken, { id, name, email });

      // 4. 프로필 페이지로 이동하면서 받은 데이터를 라우터의 state로 전달
      // 이렇게 하면 Profile.vue가 API를 다시 호출할 필요가 없습니다.
      router.push({ name: "Home"});

    } catch (err) {
      console.error("로그인 중 오류가 발생했습니다:", err);

      if (err.response && err.response.status === 403) {
        const errorData = err.response.data;
        if (errorData.error === 'UserWithdrawn') {
          modalTitle.value = '계정 접속 불가';
          modalMessage.value = errorData.message;
          isErrorModal.value = true;
        } else {
          // alert('접근 권한이 없습니다.');
          modalTitle.value = '권한 오류';
          modalMessage.value = '접근 권한이 없습니다.';
          isErrorModal.value = true;
        }
      } else {
        // alert("로그인 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        modalTitle.value = '알 수 없는 오류';
        modalMessage.value = '로그인 처리 중 알 수 없는 오류가 발생했습니다. 다시 시도해주세요.';
        isErrorModal.value = true;
      }

      isModalOpen.value = true;
    }
  } else {
    // code가 없는 경우, 로그인 페이지로 리다이렉트
    console.error("인증 코드가 없습니다.");
    router.push({ name: 'Login' });
  }
});

const handleModalClose = () => {
  isModalOpen.value = false;
  router.push({ name: 'Login' });
}
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen">
    <p>로그인 처리 중입니다...</p>
  </div>
  <ConfirmationModal
      :show="isModalOpen"
      @update:show="handleModalClose"
      @confirm="handleModalClose"
      :title="modalTitle"
      :message="modalMessage"
      :confirm-button-text="isErrorModal ? '확인하고 로그인 화면으로' : '확인'"
      cancel-button-text=""
      :confirm-button-class="isErrorModal ? 'bg-blue-600 hover:bg-blue-700' : 'bg-red-600 hover:bg-red-700'"
  />
</template>
