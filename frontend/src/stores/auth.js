import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from "@/utils/axios.js";

export const useAuthStore = defineStore('auth', () => {
    // 상태 (State)
    const accessToken = ref(null);
    const user = ref(null);

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

    const restoreAuth = async () => {
        console.log('auth.js');
        console.log('localStorage: ', localStorage);
        const storedToken = localStorage.getItem('accessToken');
        console.log('storedToken: ', storedToken);

        if (storedToken) {
            try {
                console.log("Restoring auth state...");
                // 토큰을 스토어에 임시로 설정하여 요청 인터셉터가 헤더에 담을 수 있도록 합니다.
                accessToken.value = storedToken;

                // 백엔드에 토큰 유효성 검사 및 프로필 정보 요청
                // axios.js의 인터셉터가 이 요청의 Authorization 헤더에 storedToken을 자동으로 추가합니다.
                const response = await axios.get('/auth/profile');

                // ★ 응답으로 받은 새로운 accessToken과 사용자 정보를 추출
                const { accessToken: newAccessToken, ...userData } = response.data;

                // ★ 새로운 accessToken을 스토어와 localStorage에 저장하여 세션을 갱신합니다.
                accessToken.value = newAccessToken;
                localStorage.setItem('accessToken', newAccessToken);

                // 프로필 정보만 스토어에 저장
                user.value = userData;
                console.log("Auth state restored.");
            } catch (error) {
                console.error("Failed to restore auth state:", error);
                // 토큰이 유효하지 않으면 로그인 정보 초기화
                clearLoginInfo();
            }
        }
    };

    // 상태와 액션을 반환
    return { accessToken, user, setLoginInfo, setAccessToken, clearLoginInfo, restoreAuth };
});