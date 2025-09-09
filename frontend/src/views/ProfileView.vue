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
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div>
    <h2>Profile</h2>
    <div v-if="loading">Loading...</div>
    <div v-else>
      <p>Name: {{ name }}</p>
      <p>Email: {{ email }}</p>
    </div>
  </div>
</template>

<style scoped>
p {
  margin: 8px 0;
  font-size: 16px;
}
</style>