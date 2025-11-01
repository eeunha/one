import {defineStore} from 'pinia';
import {ref} from 'vue';
import {LikeService} from '@/services/likeService.js';

export const useLikeStore = defineStore('like', () => {

    const isLiking = ref(false);

    const likePost = async (postId) => {
        if (isLiking.value) return;

        isLiking.value = true;

        try {
            const response = await LikeService.likePost(postId);
            console.log('게시글 좋아요 성공: ', response);
            return response.data;

        } catch (error) {
            console.error('게시글 좋아요 실패: ', error.response ? error.response.data : error.message);
            throw error; // View 컴포넌트가 사용자에게 알리도록 에러를 던짐
        } finally {
            isLiking.value = false;
        }
    };

    const unlikePost = async (postId) => {

        if (isLiking.value) return;

        isLiking.value = true;

        try {
            const response = await LikeService.unlikePost(postId);
            console.log('게시글 좋아요 취소 성공: ', response);

            return response.data;
        } catch (error) {
            console.error('게시글 좋아요 취소 실패: ', error.response ? error.response.data : error.message);
            throw error;
        } finally {
            isLiking.value = false;
        }
    };

    return {
        isLiking,
        likePost,
        unlikePost
    }
});