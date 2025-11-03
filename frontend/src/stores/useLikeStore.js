import {defineStore} from 'pinia';
import {computed, ref} from 'vue';
import {LikeService} from '@/services/likeService.js';
import {useBoardStore} from '@/stores/useBoardStore.js';

export const useLikeStore = defineStore('like', () => {

    const isLikedByUser = ref(false);
    const isLoading = ref(false);

    // isLikedByUser를 위한 Getter (optional, ref를 직접 써도 무방)
    const isLiked = computed(() => isLikedByUser.value);

    /**
     * 좋아요 상태를 초기 로드하고 Store를 업데이트합니다.
     * @param {number} postId - 현재 게시글 ID
     * @param {boolean} isAuthenticated - 현재 로그인 상태 여부 (LikeButton.vue에서 전달) ⭐️
     */
    const fetchLikeStatus = async (postId, isAuthenticated) => {
        if (isLoading.value) return;

        // ⭐️ 핵심 수정: 비로그인 상태이면 API 호출을 건너뜁니다. ⭐️
        if (!isAuthenticated) {
            isLikedByUser.value = false;
            console.log("Like Store: 비로그인 상태이므로 사용자 좋아요 상태를 확인하지 않습니다.");
            return;
        }

        isLoading.value = true;

        try {
            // 1. API 호출 (인증 실패할 가능성이 있음)
            const responseData = await LikeService.fetchLikeStatus(postId);

            // 2. 성공적으로 데이터를 받았을 경우 상태 반영
            isLikedByUser.value = responseData.isLiked; // 로그인된 사용자라면 정확한 isLiked 값을 가짐

            console.log("Like Store: 좋아요 데이터 로드 완료");
            
        } catch (error) {
            console.error("Like Store: 좋아요 상태 로드 실패. 401/403 등 인증 문제일 수 있음.", error);
            isLikedByUser.value = false; // 좋아요 상태는 무조건 false로 표시

            // throw error; // 에러는 컴포넌트로 전달하여 토스트 등으로 알릴 수 있음
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 좋아요/취소 액션의 공통 로직을 처리하고 Board Store의 likeCount를 업데이트합니다.
     * @param {number} postId
     * @param {Promise<Object>} apiCallPromise - LikeService.likePost 또는 LikeService.unlikePost의 Promise
     */
    const handleLikeAction = async (postId, apiCallPromise) => {
        if (isLoading.value) return;

        isLoading.value = true;

        const boardStore = useBoardStore();

        try {
            const responseData = await apiCallPromise;

            // 1. [Like Store] 개인 좋아요 상태 업데이트
            isLikedByUser.value = responseData.isLiked;

            // 2. [Board Store] 좋아요 수 업데이트 (실시간 반영) ⭐️ 핵심 동기화 ⭐️
            boardStore.updateLikeCount(postId, responseData.likeCount);

            console.log('게시글 좋아요/취소 성공 및 상태 동기화 완료');
            return responseData;

        } catch (error) {
            console.error('게시글 좋아요/취소 실패: ', error.response ? error.response.data : error.message);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    const likePost = (postId) => handleLikeAction(postId, LikeService.likePost(postId));

    const unlikePost = (postId) => handleLikeAction(postId, LikeService.unlikePost(postId));

    return {
        isLiked,
        isLikedByUser,
        isLoading,
        fetchLikeStatus,
        likePost,
        unlikePost
    }
});