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
    async fetchBoardList(page, size) {
        try {
            // GET 요청: /api/posts?page={page}&size={size} 형태로 전달
            const response = await axios.get(BOARD_API_BASE_URL, {
                // axios의 'params' 옵션을 사용하여 쿼리 파라미터를 깔끔하게 전달합니다.
                params: {
                    page: page, // 0-based 페이지 번호
                    size: size  // 페이지 당 항목 수
                }
            });

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