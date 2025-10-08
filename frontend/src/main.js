import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from "@/stores/useAuthStore.js";

import axios from 'axios'

axios.defaults.withCredentials = true; // 모든 요청에서 쿠키 자동 포함

const pinia = createPinia()
const app = createApp(App)

app.use(pinia) // 1. Pinia 등록

// 2. ⭐ 비동기 인증 복원 로직을 await로 먼저 실행합니다. ⭐
const authStore = useAuthStore()
if (localStorage.getItem("accessToken")) {
    try {
        await authStore.restoreAuth();
        console.log("main.js: 인증 상태 복원 완료.");
    } catch (error) {
        console.error("main.js: 인증 상태 복원 실패 (토큰 만료 가능성).", error);
        authStore.clearLoginInfo(); // 실패 시 로그인 정보 정리
    }
}

// 3. 라우터 등록 (이제 Store에는 올바른 인증 상태가 있습니다.)
app.use(router)

app.mount('#app')
