import { publicClient, authenticatedClient } from '@/utils/axios.js';

const BOARD_API_BASE_URL = '/posts'; // 백엔드 경로. board 아님!!!

/**
 * BoardService: 게시판 관련 API 호출을 전담하는 객체
 */
export const BoardService = {

    async fetchTop4Posts() {
        try {
            const response = await publicClient.get(`${BOARD_API_BASE_URL}/popular`);
            return response.data;
        } catch (error) {
            console.error('Failed to fetch top8 posts: ', error);
            // 호출 실패 시 오류를 던져 View에서 처리하도록 합니다.
            throw error;
        }
    },

    /**
     * 전체 게시글 목록을 가져옵니다.
     * @returns {Promise<Array>} 게시글 목록 배열
     */
    async fetchBoardList(page, size) {
        try {
            // GET 요청: /api/posts?page={page}&size={size} 형태로 전달
            const response = await publicClient.get(BOARD_API_BASE_URL, {
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
    },

    /**
     * 특정 ID의 게시글 상세 정보를 가져옵니다.
     * @param {number} id - 게시글 ID
     * @returns {Promise<Object>} PostResponseDTO 객체
     */
    async fetchPostDetail(id) {
        try {
            const response = await publicClient.get(`${BOARD_API_BASE_URL}/${id}`);
            return response.data;
        } catch (error) {
            console.error(`Failed to fetch post ${id} detail: `, error);
        }
    },

    /**
     * ⭐ 새로운 게시글을 작성합니다. (POST /api/posts) ⭐
     * @param {object} postData - { title, content }
     * @returns {Promise<Object>} 작성된 PostResponseDTO (ID 포함)
     */
    async createPost(postData) {
        try {
            const response = await authenticatedClient.post(BOARD_API_BASE_URL, postData);
            return response.data;
        } catch (error) {
            console.error('Failed to create post: ', error);
            throw error;
        }
    },

    /**
     * 기존 게시글을 수정합니다. (PUT /api/posts/{postId})
     * @param {number} postId - 게시글 ID (URL 경로에 사용)
     * @param {object} postData - { title, content } (Request Body에 사용)
     * @returns {Promise<Object>} 수정된 PostResponseDTO
     */
    async updatePost(postId, postData) {
        console.log('boardService - updatePost 진입')
        try {
            const response = await authenticatedClient.put(`${BOARD_API_BASE_URL}/${postId}`, postData);
            return response.data;
        } catch (error) {
            console.error('Failed to update post: ', error);
            throw error;
        }
    },

    async deletePost(postId) {
        console.log('boardService - deletePost 진입')
        try {
            const response = await authenticatedClient.delete(`${BOARD_API_BASE_URL}/${postId}`);
            return response.data;
        } catch (error) {
            console.error('Failed to delete post: ', error);
            throw error;
        }
    },
}