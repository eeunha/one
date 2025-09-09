<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';

const name = ref('')
const email = ref('')

onMounted(async () => {
  try {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      alert('No access token, please login')
      return
    }

    const res = await axios.get('http://localhost:8085/auth/profile', {
      headers: { Authorization: `Bearer ${token}` }
    })

    name.value = res.data.name
    email.value = res.data.email
  } catch (err) {
    console.error(err)
    alert('Failed to fetch profile')
  }
})
</script>

<template>
  <div>
    <h2>Profile</h2>
    <p>Name: {{ name }}</p>
    <p>Email: {{ email }}</p>
  </div>
</template>

<style scoped>

</style>