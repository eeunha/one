<script setup>
import { ref, onMounted } from 'vue';
import { getUsers, createUser } from '../services/UserService';

// 상태 변수
const users = ref([]);
const username = ref('');
const email = ref('');

// GET 요청
const fetchUsers = async () => {
  users.value = await getUsers();
};

// POST 요청
const addUser = async () => {
  if (!username.value || !email.value) return alert('모두 입력해주세요');
  const newUser = { username: username.value, email: email.value };
  const savedUser = await createUser(newUser);
  if (savedUser) {
    alert('User created!');
    username.value = '';
    email.value = '';
    fetchUsers(); // 생성 후 리스트 갱신
  }
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchUsers();
});
</script>

<template>
  <div>
    <h2>Users</h2>
    <ul>
      <li v-for="user in users" :key="user.id">
        {{ user.id }} - {{ user.username }} - {{ user.email }}
      </li>
    </ul>

    <h2>Create User</h2>
    <input v-model="username" placeholder="Username" />
    <input v-model="email" placeholder="Email" />
    <button @click="addUser">Create</button>
  </div>
</template>

<style scoped>
input { margin: 5px; }
button { margin: 5px; }
</style>