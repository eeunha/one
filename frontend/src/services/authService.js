import { publicClient, authenticatedClient } from "@/utils/axios.js";

const USER_API_BASE_URL = '/users'; // 사용자 기본 경로

/**
 * 순수한 API 통신(Service) 로직을 캡슐화한 객체
 */
export const AuthService = {

    /**
     * RT 갱신 요청을 서버에 보냅니다.
     * @returns {Promise<string>} 새로 발급된 액세스 토큰
     */
    async refreshTokens() {
        // RT는 HTTP-only 쿠키에 담겨 요청 시 자동으로 전송됨
        const response = await authenticatedClient.post('/auth/refresh');

        // 새로운 AT만 반환하여 Store가 상태 관리에 집중하도록 합니다.
        return response.data.accessToken;
    },

    /**
     * 유효한 AT를 사용하여 사용자 프로필 정보를 서버에서 가져옵니다.
     * @returns {Promise<Object>} 사용자 프로필 데이터
     */
    async fetchUserProfile() {
        const response = await authenticatedClient.get('/auth/profile');

        // 프로필 데이터 객체만 반환합니다.
        return response.data;
    },

    /**
     * 회원 탈퇴를 요청합니다. (DELETE /api/users/me)
     * @returns {Promise<void>}
     */
    async withdraw() {
        try {
            await authenticatedClient.delete(`${USER_API_BASE_URL}/me`);
            console.log('AuthService: 회원 탈퇴 요청 성공');
        } catch (error) {
            console.log('AuthService: 회원 탈퇴 요청 실패: ', error);
            throw error;
        }
    }
};