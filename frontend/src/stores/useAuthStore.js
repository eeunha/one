import {defineStore} from 'pinia';
import {ref} from 'vue';
import {jwtDecode} from "jwt-decode";
import {AuthService} from "@/services/authService.js"

export const useAuthStore = defineStore('auth', () => {
    // 상태 (State)
    const accessToken = ref(null);
    const user = ref(null);

    // 토큰 갱신 중 상태 플래그
    const isRefreshing = ref(false);

    // 액션 (Actions)
    const setLoginInfo = (token, userData) => {
        accessToken.value = token;
        user.value = userData;
        localStorage.setItem('accessToken', token); // localStorage에도 저장
    };

    const setAccessToken = (token) => {
        accessToken.value = token;
        localStorage.setItem('accessToken', token);
    };

    const clearLoginInfo = () => {
        accessToken.value = null;
        user.value = null;
        localStorage.removeItem('accessToken'); // localStorage에서도 삭제
    };

    /**
     * AT의 만료 여부 및 만료 임박 여부를 확인합니다.
     * @param {string} token - 액세스 토큰
     * @param {number} bufferSeconds - 만료 시간 전에 갱신을 시도할 여유 시간 (초 단위)
     * @returns {boolean} 만료되었거나 만료가 임박하면 true
     */
    const isTokenExpiredOrNear = (token, bufferSeconds = 60) => {
        if (!token) return true;

        try {
            const decoded = jwtDecode(token); // jwtDecode 함수 사용
            const expirationTime = decoded.exp; // 초 단위
            const currentTime = Date.now() / 1000; // 초 단위

            // AT는 만료되었거나 만료가 임박(60초 전)했으면 갱신 필요
            return expirationTime < currentTime || (expirationTime - currentTime < bufferSeconds);

        } catch (error) {
            console.error("JWT decoding failed:", error);
            return true;
        }
    };

    /**
     * 서버에 RT 갱신을 요청하고 새 AT를 받아와 저장합니다.
     * @returns {string} 새로 발급된 액세스 토큰 (newAccessToken)
     */
    const refreshTokensWithServer = async () => {
        // 🚨 RT는 HTTP-only 쿠키에 담겨있어 요청 시 자동으로 전송됩니다.

        // 1. 갱신 시작 시 락 설정
        isRefreshing.value = true;

        try {
            // ⭐️ Service 호출 ⭐️
            const newAT = await AuthService.refreshTokens();

            // 갱신 성공 시, 새로운 AT를 localStorage에 저장
            localStorage.setItem('accessToken', newAT);

            return newAT;
        } catch (error) {
            // RT 만료 등으로 갱신 실패 시
            console.error("Refresh token validation failed:", error);
            throw error;
        } finally {
            // 2. 갱신 완료(성공/실패) 시 락 해제
            isRefreshing.value = false;
        }
    };

    /**
     * 유효한 AT를 사용하여 사용자 프로필 정보를 서버에서 가져옵니다.
     */
    const fetchUserProfile = async () => {
        // ⭐️ Service 호출 ⭐️
        // Store는 받은 데이터를 상태에 저장하는 역할만 수행합니다. (Store의 책임)
        user.value = await AuthService.fetchUserProfile();
    };

    const restoreAuth = async () => {
        console.log('useAuthStore.js');
        console.log('localStorage: ', localStorage);
        const storedAT = localStorage.getItem('accessToken');
        console.log('storedAT: ', storedAT);

        if (storedAT) {
            try {
                console.log("Restoring auth state...");

                // 1. AT의 만료일을 클라이언트에서 직접 확인 (10초 테스트 환경에 맞게 5초 버퍼 설정)
                if (isTokenExpiredOrNear(storedAT, 5)) {
                    console.log("AT expired/near. Attempting RT refresh via server...");

                    // 2. AT 만료 시, RT 갱신 요청을 시도합니다. (RT 유효성은 서버가 판단)
                    const newAT = await refreshTokensWithServer();
                    setAccessToken(newAT);

                } else {
                    console.log('AT 유효');

                    // 3. AT가 아직 유효한 경우, AT만 Pinia store에 설정합니다.
                    setAccessToken(storedAT);
                }

                // ★★★ FIX: 토큰이 유효함을 확인했으므로, 프로필 정보를 가져옵니다.
                await fetchUserProfile();

                console.log("Auth state restored.");
            } catch (error) {
                console.error("Failed to restore auth state:", error);
                // 갱신 실패 (RT 만료, 401/403 응답 등) 시 로그인 정보 초기화
                // 토큰이 유효하지 않으면 로그인 정보 초기화
                clearLoginInfo();
            }
        }
        // storedAT가 없으면 아무 작업도 하지 않고 로그아웃 상태를 유지합니다.
    };

    // 상태와 액션을 반환
    return {
        accessToken,
        user,
        isRefreshing,
        setLoginInfo,
        setAccessToken,
        clearLoginInfo,
        restoreAuth,
        isTokenExpiredOrNear,
        refreshTokensWithServer,
        fetchUserProfile,
    };
});