<script setup>
import { ref, onMounted } from 'vue';
import axios from '../utils/axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const name = ref('');
const email = ref('');
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await axios.get('/auth/profile');
    name.value = res.data.name;
    email.value = res.data.email;
  } catch (err) {
    console.error('사용자 정보를 가져오는 데 실패했습니다.', err);
    // 로그인 필요 → alert 후 이동
    const confirmLogin = window.confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?');
    if (confirmLogin) router.push('/login');
    else router.push('/');
  } finally {
    loading.value = false;
  }
});

const logout = async () => {
  try {
    await axios.post('/auth/logout', {}, { withCredentials: true });
    alert('로그아웃 성공');
    router.push('/login');
  } catch (err) {
    console.error('로그아웃 실패', err);
    alert('로그아웃 실패. 다시 시도해주세요.');
  }
};
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-4">
    <div class="bg-white p-8 rounded-xl shadow-lg text-center">
      <h2 class="text-2xl font-bold mb-4 text-gray-800">프로필</h2>
      <div v-if="loading" class="text-gray-600">Loading...</div>
      <div v-else>
        <p class="text-gray-700 text-lg mb-2"><strong>이름:</strong> {{ name }}</p>
        <p class="text-gray-700 text-lg"><strong>이메일:</strong> {{ email }}</p>
        <button
            @click="logout"
            class="mt-6 w-full py-2 px-4 bg-red-500 text-white font-semibold rounded-lg shadow-md hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-400 focus:ring-opacity-75 transition duration-300"
        >
          로그아웃
        </button>
      </div>
    </div>
  </div>
</template>
