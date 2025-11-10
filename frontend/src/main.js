import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from "@/stores/useAuthStore.js";

// ⭐️ Font Awesome Import 시작 ⭐️
import { library } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

// 사용할 아이콘들을 여기서 명시적으로 불러옵니다.
import {
    faHeart,   // 좋아요 (이전 fa-thumbs-up 대신 fa-heart 사용)
    faComment, // 댓글
    faEye,   // 조회수
    // 만약 다른 아이콘이 필요하면 여기에 추가합니다.
} from '@fortawesome/free-solid-svg-icons';

// 라이브러리에 아이콘을 추가합니다.
library.add(faHeart, faComment, faEye);
// ⭐️ Font Awesome Import 끝 ⭐️

const pinia = createPinia()
const app = createApp(App)

app.use(pinia) // 1. Pinia 등록

/**
 * 비동기 앱 초기화 함수:
 * 모든 await 작업이 완료된 후 router와 app.mount()를 호출합니다.
 */
async function bootstrapApp() {
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
}

app.component('font-awesome-icon', FontAwesomeIcon);

// 명시적으로 비동기 초기화 함수를 호출합니다.
bootstrapApp();