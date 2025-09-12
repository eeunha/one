<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import axios from '@/utils/axios';

const router = useRouter();
const userStore = useUserStore();

onMounted(async () => {
  if (!userStore.isAuthenticated) {
    try {
      await userStore.fetchProfile();
    } catch (err) {
      // 로그인 필요 alert + confirm 처리
      const confirmLogin = window.confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?');
      if (confirmLogin) router.push('/login');
      else router.push('/');
      return;
    }
  }
});

const logout = async () => {
  try {
    await axios.post('/auth/logout', {}, {withCredentials: true});
    userStore.logout();
    alert('로그아웃 완료');
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
      <p class="text-gray-700 text-lg mb-2"><strong>이름:</strong> {{ userStore.name }}</p>
      <p class="text-gray-700 text-lg"><strong>이메일:</strong> {{ userStore.email }}</p>
      <button
          @click="logout"
          class="mt-6 w-full py-2 px-4 bg-red-500 text-white font-semibold rounded-lg shadow-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-400 focus:ring-opacity-75 transition duration-300"
      >
        로그아웃
      </button>
    </div>
  </div>
</template>
