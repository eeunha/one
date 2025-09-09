<script setup>
import { useRouter } from 'vue-router';
import { ref } from 'vue';

const router = useRouter();
const loading = ref(false);

// 구글 로그인 버튼 클릭 시 OAuth 페이지로 이동
const googleLogin = () => {
  const clientId = "879558565108-ld94ep4oh3ei2srs1tnnjnvq90iohv0o.apps.googleusercontent.com"; // 실제 구글 클라이언트 ID로 교체
  const redirectUri = "http://localhost:8085/oauth/callback/google";
  const url = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=openid profile email&prompt=select_account`;

  window.location.href = url;
};
</script>

<template>
  <div>
    <h1>로그인 페이지</h1>
    <button @click="googleLogin">Login with Google</button>
  </div>
</template>

<style scoped>
button {
  margin: 10px;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
  background-color: lightgray;
  border-radius: 5px;
}
</style>