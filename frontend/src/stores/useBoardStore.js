import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { BoardService } from '@/services/boardService.js';

/**
 * 게시판 관련 상태 및 비즈니스 로직을 관리하는 Pinia Store
 */
export const useBoardStore = defineStore('post', () => {

    // 상태 (State): ref()를 사용하여 반응성(Reactive) 상태 정의
    const posts = ref([]);           // 게시글 목록 상태
    const isLoading = ref(false);      // API 요청 로딩 상태 플래그

    // Getter (컴퓨팅된 상태)
    const postCount = computed(() => posts.value.length);

    // Actions

    /**
     * BoardService를 통해 서버에서 게시글 목록을 가져옵니다.
     */
    const fetchPosts = async () => {

        if (isLoading.value) return;

        isLoading.value = true;

        try {
            // Service 호출 (API 통신)
            const data = await BoardService.fetchBoardList();

            // 상태 업데이트
            posts.value = data;
            console.log("Board Store: 게시글 목록 로드 완료");

        } catch (error) {
            console.error("Board Store: 게시글 목록 로드 실패", error);
            // View에서 에러 처리를 할 수 있도록 상태를 비웁니다.
            posts.value = [];
            throw error;
        } finally {
            isLoading.value = false;
        }
    };

    return {
        posts,
        isLoading,
        postCount,
        fetchPosts,
    }
});
