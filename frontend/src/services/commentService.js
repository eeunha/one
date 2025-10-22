import { publicClient, authenticatedClient } from '@/utils/axios.js';

const COMMENT_API_BASE_URL = '/posts';

export const CommentService = {

    /**
     * 1. 게시글별 댓글 목록 조회 (GET /api/posts/{postId}/comments)
     * @param {number} postId
     * @returns {Promise<CommentResponseDTO[]>} 댓글 목록 배열
     */
    async fetchComments(postId) {
        console.log('commentService - fetchCommentList 진입');
        try {
            // GET /posts/{postId}/comments
            const response = await publicClient.get(`${COMMENT_API_BASE_URL}/${postId}/comments`);
            // 컨트롤러가 List<DTO>를 직접 반환하므로, response.data는 배열입니다.
            return response.data;
        } catch (error) {
            console.error('Failed to fetch comment list:', error);
            throw error;
        }
    },

    /**
     * 2. 댓글 생성 (POST /api/posts/{postId}/comments)
     * @param {number} postId
     * @param {object} commentData - { content: string }
     * @returns {Promise<CommentResponseDTO>} 생성된 댓글 DTO
     */
    async createComment(postId, commentData) {
        console.log('commentService - createComment 진입');
        try {
            // POST /posts/{postId}/comments
            const response = await authenticatedClient.post(`${COMMENT_API_BASE_URL}/${postId}/comments`, commentData);
            return response.data;
        } catch (error) {
            console.error('Failed to create comment:', error);
            throw error;
        }
    },

    /**
     * 3. 댓글 수정 (PUT /api/posts/{postId}/comments/{commentId})
     * @param {number} postId
     * @param {number} commentId
     * @param {object} commentData - { content: string }
     * @returns {Promise<CommentResponseDTO>} 수정된 댓글 DTO
     */
    async updateComment(postId, commentId, commentData) {
        console.log('commentService - updateComment 진입');
        try {
            // PUT /posts/{postId}/comments/{commentId}
            const response = await authenticatedClient.put(`${COMMENT_API_BASE_URL}/${postId}/comments/${commentId}`, commentData);
            return response.data;
        } catch (error) {
            console.error('Failed to update comment:', error);
            throw error;
        }
    },

    /**
     * 4. 댓글 소프트 삭제 (DELETE /api/posts/{postId}/comments/{commentId})
     * @param {number} postId
     * @param {number} commentId
     * @returns {Promise<void>} 204 No Content
     */
    async deleteComment(postId, commentId) {
        console.log('commentService - deleteComment 진입');
        try {
            // DELETE /posts/{postId}/comments/{commentId}
            const response = await authenticatedClient.delete(`${COMMENT_API_BASE_URL}/${postId}/comments/${commentId}`);
        } catch (error) {
            console.error('Failed to delete comment:', error);
            throw error;
        }
    },
}