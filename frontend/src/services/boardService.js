import axios from '@/utils/axios.js';

const BOARD_API_BASE_URL = '/posts'; // 백엔드 경로. board 아님!!!

/**
 * BoardService: 게시판 관련 API 호출을 전담하는 객체
 */
export const BoardService = {

    /**
     * 전체 게시글 목록을 가져옵니다.
     * @returns {Promise<Array>} 게시글 목록 배열
     */
    async fetchBoardList() {
        try {
            // GET 요청: /api/board
            // 이 axios는 JWT 갱신 로직(인터셉터)을 포함하고 있습니다.
            const response = await axios.get(BOARD_API_BASE_URL);

            // 응답 데이터는 response.data에 포함되어 있습니다.
            return response.data;
        } catch (error) {
            console.error('Failed to fetch board list:', error);
            // 호출 실패 시 오류를 던져 View에서 처리하도록 합니다.
            throw error;
        }
    }
    // TODO: 4단계에서 fetchBoardDetail(id) 및 writeBoard(postData) 추가 예정
}