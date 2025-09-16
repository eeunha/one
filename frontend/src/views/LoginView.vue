<script setup>
// 구글 로그인 버튼 클릭 시 백엔드의 OAuth2 인증 시작점으로 이동
const googleLogin = () => {
  const clientId = '879558565108-ld94ep4oh3ei2srs1tnnjnvq90iohv0o.apps.googleusercontent.com'; // 여기에 클라이언트 ID를 넣으세요.
  // Google 로그인 후 돌아올 프론트엔드 URL입니다.
  const redirectUri = 'http://localhost:8086/oauth2/redirect';
  const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=${clientId}&scope=openid%20profile%20email&redirect_uri=${redirectUri}`;

  window.location.href = authUrl;
};
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-100 p-4">
    <div class="bg-white p-8 rounded-xl shadow-lg text-center">
      <h1 class="text-3xl font-bold mb-6 text-gray-800">로그인</h1>
      <button
          @click="googleLogin"
          class="flex items-center justify-center bg-white border border-gray-300 text-gray-700 font-semibold py-3 px-6 rounded-lg shadow-sm hover:bg-gray-50 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 cursor-pointer"
      >
        <img src="/src/assets/images/google.png" alt="Google logo" class="w-6 h-6 mr-3">
        Login with Google
      </button>
    </div>
  </div>
</template>

<style scoped>
</style>