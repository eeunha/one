import axios from 'axios';
import { useAuthStore } from '@/stores/useAuthStore.js';
import router from '@/router'; // 라우터 import

// -------------------------------------------------------------
// 기본 설정
// -------------------------------------------------------------
const BASE_URL = 'http://localhost:8085/api';

// =============================================================
// 1. PUBLIC CLIENT: 토큰을 첨부하지 않는 공개 API용 (permitAll 경로)
// =============================================================
// 이 클라이언트는 어떠한 인터셉터도 설정하지 않습니다.
export const publicClient = axios.create({
    baseURL: BASE_URL,
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
});

// =============================================================
// 2. AUTHENTICATED CLIENT: 인증이 필수인 API용 (토큰 첨부 및 갱신 로직 포함)
// =============================================================
export const authenticatedClient = axios.create({
    baseURL: BASE_URL,
    withCredentials: true,
    headers: { 'Content-Type': 'application/json' },
});

// -------------------------------------------------------------
// 요청 인터셉터 (Access Token 자동 첨부 및 사전 갱신)
// -------------------------------------------------------------
authenticatedClient.interceptors.request.use(
    async (config) => {
        const authStore = useAuthStore();
        const accessToken = authStore.accessToken;

        console.log('authenticatedClient - 요청 인터셉터 진입');

        // ⭐ 1. 갱신 중인 경우: 갱신이 완료될 때까지 대기 ⭐
        if (authStore.isRefreshing) {
            // Promise를 사용하여 갱신이 완료될 때까지 요청을 대기시킵니다.
            // 여기서는 요청 인터셉터가 토큰 갱신을 주도하므로, 간단히 현재 토큰으로 요청을 리턴합니다.
            // (응답 인터셉터가 401을 받아 재시도해 줄 것이므로 안전합니다.)
            if (accessToken) {
                config.headers.Authorization = `Bearer ${accessToken}`;
            }
            return config;
        }

        // 2. AT가 있고, 만료 임박했다면 사전 갱신 시도
        // (가정: isTokenExpiredOrNear는 authStore의 getter나 함수입니다.)
        if (accessToken && authStore.isTokenExpiredOrNear(accessToken, 5)) {
            console.log('요청 인터셉터: AT 만료 임박 감지, 사전 갱신 시도');

            try {
                // refreshTokensWithServer는 새로운 AT를 받아 setAccessToken까지 처리한다고 가정
                const newAccessToken = await authStore.refreshTokensWithServer();
                config.headers.Authorization = `Bearer ${newAccessToken}`;
                return config; // 새 토큰으로 요청 실행
            } catch (error) {
                // RT 만료 등으로 갱신 실패 시
                console.error("요청 인터셉터: 사전 갱신 실패, 로그아웃");
                authStore.clearLoginInfo();
                router.push('/login');
                return Promise.reject(error); // 요청 거부
            }
        }

        // 3. AT가 유효하면 기존 로직 수행
        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// -------------------------------------------------------------
// 응답 인터셉터 (401 발생 시 토큰 갱신 및 재시도)
// -------------------------------------------------------------
authenticatedClient.interceptors.response.use(
    // 1. 정상 응답이면 그대로 반환
    (response) => response,

    // 2. 에러 응답 처리
    async (error) => {
        console.log('axios.js - 에러 응답 처리 진입');

        // 3. 원래 요청 객체 가져오기 (재시도할 때 필요)
        const originalRequest = error.config;
        const authStore = useAuthStore(); // Store 인스턴스를 미리 가져옵니다.

        // ⭐⭐ FIX: 로그아웃 요청 자체는 갱신 로직을 타지 않도록 즉시 종료 ⭐⭐
        // 로그아웃 요청(예: /auth/logout)이 401을 받더라도 갱신하지 않고 즉시 종료합니다.
        if (originalRequest.url.includes('/auth/logout')) {
            console.log('axios.js - 로그아웃 요청 중 에러 발생: 갱신 무시');
            return Promise.reject(error);
        }

        // ⭐⭐ 무한 루프 방지 로직 (가장 중요) ⭐⭐
        // 갱신 요청(URL이 '/auth/refresh'인 요청) 자체가 에러를 반환하면,
        // 이는 Refresh Token마저 만료되었다는 뜻이므로 즉시 로그아웃 처리하고 종료합니다.
        if (originalRequest.url.includes('/auth/refresh') && error.response.status === 401) {
            console.log('axios.js - Refresh 요청 실패: 최종 로그아웃 처리');
            authStore.clearLoginInfo();
            router.push('/login');
            return Promise.reject(error); // 에러를 최종적으로 반환하여 체인 종료
        }

        // 4. 에러 응답이 존재하고, 상태 코드가 401이며 아직 재시도하지 않은 요청인지 확인
        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            // 5. 재시도 플래그 설정 (무한 루프 방지)
            originalRequest._retry = true;

            try {
                // 6. Refresh Token으로 Access Token 재발급 요청
                //    쿠키(HttpOnly)에 저장된 Refresh Token이 자동 전송됨
                const refreshResponse = await authenticatedClient.post('/auth/refresh', {});

                // 7. 새로 발급받은 Access Token 추출
                const newAccessToken = refreshResponse.data.accessToken;
                console.log('axios - newAccessToken: ', newAccessToken);

                // Store에 새 Access Token을 저장
                authStore.setAccessToken(newAccessToken);

                // 8. 원래 요청 헤더에 새로운 Access Token 설정
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;

                // 9. 원래 요청 재시도
                return authenticatedClient(originalRequest);
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
