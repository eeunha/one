import {authenticatedClient} from '@/utils/axios.js';

const LIKE_API_BASE_URL = '/posts';

export const LikeService = {

    /**
     * 특정 게시글에 대한 '현재 사용자'의 좋아요 상태를 가져옵니다.
     * 이 API는 인증된 사용자만 호출해야 합니다.
     * @param {number} postId - 게시글 번호
     * @returns {Promise<Object>} LikeResponseDTO (업데이트된 좋아요 수, 좋아요 상태)
     */
    async fetchLikeStatus(postId) {
        console.log('LikeService - fetchLikeStatus 진입')

        try {
            const response = await authenticatedClient.get(`${LIKE_API_BASE_URL}/${postId}/likes/status`);
            return response.data;
        } catch (error) {
            console.error('Failed to fetch like status: ', error)
            throw error;
        }
    },

    /**
     * 특정 게시글에 좋아요를 누릅니다. (POST /api/posts/{postId}/likes)
     * @param {number} postId - 좋아요를 누를 게시글 ID
     * @returns {Promise<Object>} LikeResponseDTO (업데이트된 좋아요 수, 좋아요 상태)
     */
    async likePost(postId) {
        console.log('LikeService - likePost 진입')

        try {
            // URL: /posts/{postId}/likes
            const response = await authenticatedClient.post(`${LIKE_API_BASE_URL}/${postId}/likes`)
            return response.data;
        } catch (error) {
            console.error('Failed to like post: ', error)
            throw error
        }
    },

    /**
     * 특정 게시글의 좋아요를 취소합니다. (DELETE /api/posts/{postId}/likes)
     * @param {number} postId - 좋아요를 취소할 게시글 ID
     * @returns {Promise<Object>} LikeResponseDTO (업데이트된 좋아요 수, 좋아요 상태)
     */
    async unlikePost(postId) {
        console.log('LikeService - unlikePost 진입')

        try {
            // URL: /posts/{postId}/likes
            const response = await authenticatedClient.delete(`${LIKE_API_BASE_URL}/${postId}/likes`)
            return response.data;
        } catch (error) {
            console.error('Failed to unlike post: ', error)
            throw error
        }
    },

}