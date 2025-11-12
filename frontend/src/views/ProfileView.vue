<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { authenticatedClient } from '@/utils/axios';
import { useAuthStore } from '@/stores/useAuthStore.js';
import { storeToRefs } from 'pinia'; // storeToRefs import

import ConfirmationModal from '@/components/ConfirmationModal.vue';
import WithdrawButton from '@/components/button/WithdrawButton.vue';

const router = useRouter();
const authStore = useAuthStore();
const { user } = storeToRefs(authStore); // 스토어에서 user 정보 가져오기

// --- 상태 관리 ---
// 1. 회원 탈퇴 모달 상태
const showWithdrawalModal = ref(false);
// 2. 탈퇴 처리 중 로딩 상태 (모달의 isLoading props에 전달)
const isProcessing = ref(false);
// 3. 탈퇴 에러 메시지 (모달의 error props에 전달)
const withdrawalError = ref('');

const logout = async () => {
  try {
    // 1. 백엔드 로그아웃 API 호출
    await authenticatedClient.post('/auth/logout');

    // 2. Pinia 스토어에서 정보 제거
    authStore.clearLoginInfo();

    console.log('로그아웃 완료');

    // 3. 로그인 페이지로 리다이렉트
    router.push('/login');

  } catch (err) {
    console.error(err);
    alert('로그아웃 실패');
  }
};

const handleWithdrawalConfirm = async () => {
  isProcessing.value = true;
  withdrawalError.value = '';

  try {
    await authStore.withdraw();

    authStore.clearLoginInfo();
    alert('회원 탈퇴가 완료되었습니다. 이용해주셔서 감사합니다.');
    router.push({name: 'Login'});
  } catch (err) {
    console.error('회원 탈퇴 실패: ', err.response || err);
    isProcessing.value = false;

    if (err.response && err.response.status === 400) {
      // RT 만료 등으로 백엔드가 400 응답 시
      withdrawalError.value = '세션이 만료되어 탈퇴할 수 없습니다. 다시 로그인해주세요.'
    } else {
      withdrawalError.value = '탈퇴 처리 중 문제가 발생했습니다. 다시 시도해주세요.'
    }
    // 실패했으므로 모달은 닫지 않고 에러 메시지 유지
  }
};

</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen p-4">
    <div class="bg-white p-8 rounded-xl shadow-lg text-center">
      <h2 class="text-2xl font-bold mb-4 text-gray-800">프로필</h2>
      <!-- user 객체가 null이 아닐 때만 내용을 표시 -->
      <div v-if="user">
        <p class="text-gray-700 text-lg mb-2"><strong>이름:</strong> {{ user.name }}</p>
        <p class="text-gray-700 text-lg"><strong>이메일:</strong> {{ user.email }}</p>
      </div>
      <div v-else>
        <!-- 로딩 상태 또는 로그인 정보가 없을 때 메시지를 표시 -->
        <p class="text-gray-600">로딩 중이거나 로그인 정보가 없습니다.</p>
      </div>
      <button
          @click="logout"
          class="mt-6 w-full py-2 px-4 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-lg shadow-md focus:outline-none focus:ring-2 focus:ring-red-400 focus:ring-opacity-75 transition duration-300"
      >
        로그아웃
      </button>

      <WithdrawButton
        @open-withdrawal-modal="showWithdrawalModal = true"
      />
    </div>
  </div>

  <ConfirmationModal
      :show="showWithdrawalModal"
      @update:show="showWithdrawalModal = $event"
      @confirm="handleWithdrawalConfirm"

      title="경고: 회원 탈퇴"
      message="정말로 계정을 탈퇴하시겠습니까? 탈퇴 후에는 계정을 복구할 수 없으며, 작성하신 루틴과 응원/질문은 익명 처리됩니다."
      :is-loading="isProcessing"
      :error="withdrawalError"
      confirm-button-text="탈퇴 확인"
      confirm-button-class="bg-red-600 hover:bg-red-700"
  />
</template>
