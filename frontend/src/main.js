import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import axios from 'axios'
import { useAuthStore } from "@/stores/auth.js";

axios.defaults.withCredentials = true; // 모든 요청에서 쿠키 자동 포함

const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(router)

// 애플리케이션 마운트 후, router.isReady를 기다린 다음 restoreAuth 호출
router.isReady().then(async () => {
    console.log("main.js - restoreAuth 호출 직전");
    console.log('main.js')
    // const authStore = useAuthStore();
    // authStore.restoreAuth();

    const authStore = useAuthStore()
    if (!authStore.accessToken && localStorage.getItem("accessToken")) {
        await authStore.restoreAuth();
    }
});

app.mount('#app')
