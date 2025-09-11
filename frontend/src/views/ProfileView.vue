<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const name = ref('');
const email = ref('');
const loading = ref(true);

onMounted(async () => {
  try {
    // 쿠키는 자동 포함되므로 Authorization 헤더 없이 요청 가능
    const res = await axios.get('http://localhost:8085/auth/profile', {
      withCredentials: true
    });
    name.value = res.data.name;
    email.value = res.data.email;
  } catch (err) {
    console.error('사용자 정보를 가져오는 데 실패했습니다.', err);

    // 로그인 페이지로 리디렉션
    window.location.href = 'http://localhost:8086/';
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-4">
    <div class="bg-white p-8 rounded-xl shadow-lg text-center">
      <h2 class="text-2xl font-bold mb-4 text-gray-800">프로필</h2>
      <div v-if="loading" class="text-gray-600">Loading...</div>
      <div v-else>
        <p class="text-gray-700 text-lg mb-2"><strong>이름:</strong> {{ name }}</p>
        <p class="text-gray-700 text-lg"><strong>이메일:</strong> {{ email }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>