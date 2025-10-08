import axios from 'axios';
import { useAuthStore } from '@/stores/useAuthStore.js';
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

        console.log('axios.js - 요청 인터셉터 진입');

        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터: 401 발생 시 refresh token으로 재발급 시도
instance.interceptors.response.use(
    // 1. 정상 응답이면 그대로 반환
    (response) => response,

    // 2. 에러 응답 처리
    async (error) => {
        console.log('axios.js - 에러 응답 처리 진입');

        // 3. 원래 요청 객체 가져오기 (재시도할 때 필요)
        const originalRequest = error.config;
        const authStore = useAuthStore(); // Store 인스턴스를 미리 가져옵니다.

        // ⭐⭐ 무한 루프 방지 로직 (가장 중요) ⭐⭐
        // 갱신 요청(URL이 '/auth/refresh'인 요청) 자체가 에러를 반환하면,
        // 이는 Refresh Token마저 만료되었다는 뜻이므로 즉시 로그아웃 처리하고 종료합니다.
        if (originalRequest.url === '/auth/refresh') {
            console.log('axios.js - Refresh 요청 실패: 최종 로그아웃 처리');
            authStore.clearLoginInfo();
            router.push('/login');
            return Promise.reject(error); // 에러를 최종적으로 반환하여 체인 종료
        }
        // ⭐⭐ 무한 루프 방지 로직 종료 ⭐⭐

        // 4. 에러 응답이 존재하고, 상태 코드가 401이며 아직 재시도하지 않은 요청인지 확인
        if (error.response && (error.response.status === 401 || error.response.status === 403) && !originalRequest._retry) {
            // 5. 재시도 플래그 설정 (무한 루프 방지)
            originalRequest._retry = true;

            try {
                // 6. Refresh Token으로 Access Token 재발급 요청
                //    쿠키(HttpOnly)에 저장된 Refresh Token이 자동 전송됨
                const refreshResponse = await instance.post('/auth/refresh', {});

                // 7. 새로 발급받은 Access Token 추출
                const newAccessToken = refreshResponse.data.accessToken;
                console.log('axios - newAccessToken: ', newAccessToken);

                // ⭐ Store에 새 Access Token을 저장하는 로직이 필요합니다! (현재 코드에는 없음)
                authStore.setAccessToken(newAccessToken);

                // 8. 원래 요청 헤더에 새로운 Access Token 설정
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;

                // 9. 원래 요청 재시도
                return instance(originalRequest); // 여기도 instance 사용
            } catch (refreshError) {
                console.log('axios.js - refreshError');

                // 10. Refresh Token도 만료되었거나 재발급 실패 → 로그아웃 처리
                // const authStore = useAuthStore();
                authStore.clearLoginInfo();

                // 11. 로그인 페이지로 리다이렉트
                router.push('/login');

                // 12. 재발급 실패 에러를 reject
                return Promise.reject(refreshError);
            }
        }

        // 13. 401 외 기타 에러 또는 재시도 이미 수행한 요청은 그대로 reject
        return Promise.reject(error);
    }
);

export default instance;
