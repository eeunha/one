import axios from 'axios';
import { useAuthStore } from '@/stores/auth.js';
import router from '@/router'; // 라우터 import

// 기본 axios 인스턴스 설정
const instance = axios.create({
    baseURL: 'http://localhost:8085/api',
    withCredentials: true, // 쿠키 전송 허용
    headers: {
        'Content-Type': 'application/json',
    },
});

// 요청 인터셉터
// 모든 API 요청을 보내기 전에 Pinia 스토어에서 액세스 토큰을 가져와 Authorization 헤더에 자동으로 추가
instance.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();
        const accessToken = authStore.accessToken;

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터
// 백엔드로부터 401 (Unauthorized) 오류를 받으면, Pinia 스토어의 로그인 정보를 초기화하고 자동으로 로그인 페이지로 이동
instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            console.log('Authentication failed! Redirecting to login page.');
            const authStore = useAuthStore();
            authStore.clearLoginInfo();
            // Redirect to login page
            router.push('/login');
        }
        return Promise.reject(error);
    }
);

export default instance;
