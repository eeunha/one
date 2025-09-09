<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const loginWithGoogle = async () => {
  const clientId = "879558565108-ld94ep4oh3ei2srs1tnnjnvq90iohv0o.apps.googleusercontent.com";
  const redirectUri = "http://localhost:8085/oauth/callback/google";
  const scope = "openid profile email";
  const url = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}`;
  window.location.href = url;
}
</script>

<template>
  <div>
    <button @click="loginWithGoogle">Login with Google</button>
  </div>
</template>

<style scoped>

</style>