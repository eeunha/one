import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { BoardService } from '@/services/boardService.js';

/**
 * 게시판 관련 상태 및 비즈니스 로직을 관리하는 Pinia Store
 */
export const useBoardStore = defineStore('post', () => {

    // 상태 (State): ref()를 사용하여 반응성(Reactive) 상태 정의
    const posts = ref([]);           // 게시글 목록 상태 (현재 페이지의 데이터)
    const isLoading = ref(false);      // API 요청 로딩 상태 플래그

    // 현재 게시글 (상세 페이지에서 사용)
    const currentPost = ref(null);

    // ⭐ FIX 1: pagination 객체 선언 방식을 ref({})로 수정 ⭐
    const pagination = ref({
        currentPage: 1,      // 현재 페이지 번호 (1부터 시작)
        size: 10,            // 페이지 당 항목 수
        totalElements: 0,    // 전체 게시글 수
        totalPages: 0,       // 전체 페이지 수
    });

    // Getter (컴퓨팅된 상태)
    const postCount = computed(() => pagination.value.totalElements);

    // Actions

    /**
     * BoardService를 통해 서버에서 게시글 목록을 가져옵니다.
     * @param {number} page - 요청할 페이지 번호 (기본 0)
     * @param {number} size - 페이지당 항목 수 (기본 10)
     */
    const fetchPosts = async (page = 1, size = 10) => { // ⭐ FIX 2: 페이지네이션 파라미터 추가 ⭐

        if (isLoading.value) return;

        isLoading.value = true;

        try {
            // ⭐ FIX 2: 1-based 페이지 번호를 0-based로 변환하여 API에 전달 ⭐
            const apiPage = page - 1;

            // Service 호출 시 페이지네이션 파라미터 전달
            const responseData = await BoardService.fetchBoardList(apiPage, size);

            // ⭐ FIX 3: API 응답이 전체 Page 객체라고 가정하고 상태 업데이트 ⭐
            posts.value = responseData.content;

            // 페이지네이션 정보 업데이트
            pagination.value = {
                currentPage: responseData.pageable.pageNumber + 1,
                size: responseData.pageable.pageSize,
                totalElements: responseData.totalElements,
                totalPages: responseData.totalPages,
            };

            console.log("Board Store: 게시글 목록 로드 완료");

        } catch (error) {
            console.error("Board Store: 게시글 목록 로드 실패", error);
            posts.value = [];
            // 오류 발생 시 pagination 정보도 초기화하는 것이 좋습니다.
            pagination.value = { currentPage: 0, size: 10, totalElements: 0, totalPages: 0 };
            throw error;
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * ⭐ FIX 4: 특정 ID의 게시글 상세 정보를 불러오는 액션 추가 ⭐
     * @param {number} id - 게시글 ID
     */
    const fetchPostDetail = async (id) => {
        if (!id || isLoading.value) return;

        isLoading.value = true;
        currentPost.value = null; // 이전 게시글 정보 초기화

        try {
            // Service 호출 (API 통신)
            const data = await BoardService.fetchPostDetail(id); // BoardService에 해당 메서드가 있다고 가정
            currentPost.value = data;
        } catch (error) {
            console.error(`게시글 ${id} 상세 정보 로드 실패:`, error);
        } finally {
            isLoading.value = false;
        }
    };

    const createPost = async (postData) => {
        isLoading.value = true;

        try {
            // BoardService의 createPost 메서드를 호출하여 POST 요청을 보냄
            const responseData = await BoardService.createPost(postData);
            console.log('게시글 작성 성공: ', responseData);

            // 백엔드가 반환한 DTO에서 ID를 추출하여 반환
            return responseData.id;

        } catch (error) {
            console.error('게시글 작성 실패: ', error.response ? error.response.data : error.message);
            throw error; // View 컴포넌트가 사용자에게 알리도록 에러를 던짐
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 기존 게시글을 수정합니다. (PUT /api/posts/{id})
     * @param {number} id - 게시글 ID (URL 경로에 사용)
     * @param {object} postData - { title, content } (Request Body에 사용)
     */
    const updatePost = async (id, postData) => {
        isLoading.value = true;

        try {
            // BoardService의 createPost 메서드를 호출하여 POST 요청을 보냄
            const responseData = await BoardService.updatePost(id, postData);
            console.log('게시글 수정 성공: ', responseData);

            // 백엔드가 반환한 DTO에서 ID를 추출하여 반환
            return responseData.id;

        } catch (error) {
            console.error('게시글 수정 실패: ', error.response ? error.response.data : error.message);
            throw error; // View 컴포넌트가 사용자에게 알리도록 에러를 던짐
        } finally {
            isLoading.value = false;
        }
    }

    return {
        posts,
        isLoading,
        currentPost,
        pagination,
        postCount,
        fetchPosts,
        fetchPostDetail,
        createPost,
        updatePost,
    }
});