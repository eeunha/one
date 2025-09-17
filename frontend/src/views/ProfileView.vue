<script setup>
import { useRouter } from 'vue-router';
import axios from '@/utils/axios';
import { useAuthStore } from '@/stores/auth.js';
import { storeToRefs } from 'pinia'; // storeToRefs import

const router = useRouter();
const authStore = useAuthStore();
const { user } = storeToRefs(authStore); // 스토어에서 user 정보 가져오기

const logout = async () => {
  try {
    // 1. 백엔드 로그아웃 API 호출
    await axios.post('/auth/logout');

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
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-4">
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
          class="mt-6 w-full py-2 px-4 bg-red-500 text-white font-semibold rounded-lg shadow-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-400 focus:ring-opacity-75 transition duration-300"
      >
        로그아웃
      </button>
    </div>
  </div>
</template>
