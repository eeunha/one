import {defineStore} from 'pinia';
import {ref} from 'vue';
import {LikeService} from '@/services/likeService.js';

export const useLikeStore = defineStore('like', () => {

    const likeCount = ref(0);
    const isLiked = ref(false);
    const isLoading = ref(false);

    const fetchLikeStatus = async (postId) => {
        if (isLoading.value) return;

        isLoading.value = true;

        try {
            // 1. API 호출 (인증 실패할 가능성이 있음)
            const responseData = await LikeService.fetchLikeStatus(postId);

            // 2. 성공적으로 데이터를 받았을 경우 상태 반영
            likeCount.value = responseData.likeCount;
            isLiked.value = responseData.isLiked; // 로그인된 사용자라면 정확한 isLiked 값을 가짐

            console.log("Like Store: 좋아요 데이터 로드 완료");
            
        } catch (error) {
            console.error("Like Store: 좋아요 데이터 로드 실패. 비로그인 상태이거나 게시글을 찾을 수 없음.", error);

            // 3. 🚨 에러 발생 시 방어 로직 🚨
            // 서버가 401(Unauthorized) 또는 404를 반환했을 경우,
            // 좋아요 기능은 작동 불가이므로 안전하게 초기화합니다.
            likeCount.value = 0; // 카운트는 0 (혹은 기본값)
            isLiked.value = false; // 좋아요 상태는 무조건 false로 표시

            throw error; // 에러는 컴포넌트로 전달하여 토스트 등으로 알릴 수 있음
        } finally {
            isLoading.value = false;
        }
    };

    const likePost = async (postId) => {
        if (isLoading.value) return;

        isLoading.value = true;

        try {
            const responseData = await LikeService.likePost(postId);
            console.log('게시글 좋아요 성공: ', responseData);

            likeCount.value = responseData.likeCount;
            isLiked.value = responseData.isLiked;

            return responseData;

        } catch (error) {
            console.error('게시글 좋아요 실패: ', error.response ? error.response.data : error.message);
            throw error; // View 컴포넌트가 사용자에게 알리도록 에러를 던짐

        } finally {
            isLoading.value = false;
        }
    };

    const unlikePost = async (postId) => {
        if (isLoading.value) return;

        isLoading.value = true;

        try {
            const responseData = await LikeService.unlikePost(postId);
            console.log('게시글 좋아요 취소 성공: ', responseData);

            likeCount.value = responseData.likeCount;
            isLiked.value = responseData.isLiked;

            return responseData;

        } catch (error) {
            console.error('게시글 좋아요 취소 실패: ', error.response ? error.response.data : error.message);
            throw error;

        } finally {
            isLoading.value = false;
        }
    };

    return {
        likeCount,
        isLiked,
        isLoading,
        fetchLikeStatus,
        likePost,
        unlikePost
    }
});