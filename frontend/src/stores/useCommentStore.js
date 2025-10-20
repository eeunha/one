import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { CommentService } from '@/services/commentService.js';

/**
 * 특정 게시글의 댓글 관련 상태 및 비즈니스 로직을 관리하는 Pinia Store
 */
export const useCommentStore = defineStore('comment', () => {

    // 상태 (State)
    const comments = ref([]);          // 댓글 목록 상태
    const isLoading = ref(false);     // API 요청 로딩 상태 플래그
    const error = ref(null);          // 에러 상태

    // 현재 댓글이 속한 게시글 ID. fetchComments가 호출될 때 설정됩니다.
    const currentPostId = ref(null);

    // Getter (컴퓨팅된 상태)
    const commentCount = computed(() => comments.value.length);

    // Actions

    /** * ⭐️ 추가: currentPostId 상태를 안전하게 설정하는 액션 ⭐️
     * 직접적인 상태 할당으로 인한 'readonly' 오류를 방지합니다.
     * @param {number} postId - 현재 게시글 ID
     */
    const setCurrentPostId = (postId) => {
        currentPostId.value = postId;
    }

    /**
     * 특정 게시글의 댓글 목록을 가져옵니다.
     * @param {number} postId - 댓글을 가져올 게시글 ID
     * @param {boolean} force - 로딩 중이더라도 강제로 실행할지 여부 (createComment에서 사용)
     */
    const fetchComments = async (postId, force = false) => {
        if (!postId) {
            console.error('Comment Store: postId가 필요합니다.');
            return;
        }

        // // ⭐️ 주의: fetchComments의 isLoading 체크는 다른 fetch 작업 중일 때만 유효해야 합니다.
        // // 현재는 createComment가 자신만의 isLoading을 사용하므로, 이 체크를 제거하거나 수정합니다.
        // // 만약 동시에 여러 fetchComments가 발생하는 것을 막고 싶다면 이 줄을 남깁니다.
        // // if (isLoading.value) return;
        //
        // // 이 로직은 주석 처리하여, createComment가 완료된 후 fetchComments가 강제로 실행되도록 허용합니다.
        // // isLoading.value = true;
        // error.value = null;
        //
        // // fetchComments는 목록을 로드하는 작업만 담당하므로, currentPostId 설정은 호출하는 쪽에서 맡깁니다.
        // // currentPostId.value = postId; // 현재 작업중인 게시물 id

        // ⭐️ 수정: force 플래그가 true가 아니면 중복 호출 방지 (초기 로딩 시에는 force=false)
        if (!force && isLoading.value) return;

        isLoading.value = true;
        error.value = null;

        // currentPostId.value = postId; // fetchComments는 목록 로드만 담당하도록 유지

        try {
            // CommentService 호출 (GET /posts/{postId}/comments)
            const responseData = await CommentService.fetchComments(postId);
            comments.value = responseData;
        } catch (err) {
            console.error('Comment Store: 댓글 목록 로드 실패', err);
            error.value = '댓글 목록을 불러오는 데 실패했습니다.';
            comments.value = [];
            throw err;
        } finally {
            // fetchComments는 로딩 상태를 스스로 관리하지 않거나,
            // 호출하는 측(createComment)에서 최종적으로 해제하도록 위임합니다.
            // isLoading.value = false;

            // fetchComments 단독 호출 시 로딩 해제
            if (!force) {
                isLoading.value = false;
            }
        }
    };

    /**
     * 새 댓글을 생성합니다. (postId는 currentPostId를 사용)
     * @param {object} commentData - { content }
     */
    const createComment = async (commentData) => {
        if (!currentPostId.value) {
            error.value = '댓글을 작성할 게시글 ID가 설정되지 않았습니다.';
            console.error(error.value);
            return;
        }

        if (isLoading.value) return;

        isLoading.value = true;
        error.value = null;

        try {
            // CommentService 호출 (POST /posts/{postId}/comments)
            const responseData = await CommentService.createComment(currentPostId.value, commentData);

            // ⭐️ 변경: 댓글 생성 후 목록 갱신 ⭐️
            // fetchComments 내에서 로딩 상태 충돌이 발생하지 않도록,
            // 별도의 로컬 로딩 상태를 사용하거나, fetchComments 로직을 수정해야 합니다.

            // 현재 구조에서는 fetchComments의 isLoading.value 체크를 제거하는 것이 가장 빠릅니다.
            // 일단 createComment의 로딩 상태를 먼저 해제하고 목록을 불러오는 방식도 고려할 수 있습니다.
            // 하지만 Pinia Store에서 이렇게 두 번의 네트워크 요청을 묶어서 실행하는 것이 일반적입니다.

            // fetchComments가 로딩 상태를 스스로 관리하지 않거나,
            // createComment 내에서 로딩 플래그를 관리하는 방식으로 변경하겠습니다.
            // await fetchComments(currentPostId.value);

            // ⭐️ 변경: 댓글 생성 후 목록 갱신을 강제로 실행 ⭐️
            // createComment의 로딩 상태를 유지한 채 fetchComments를 호출합니다.
            // fetchComments에 force: true를 전달하여 중복 체크를 무시하고 실행하도록 합니다.
            await fetchComments(currentPostId.value, true);

            return responseData;
        } catch (err) {
            console.error("Comment Store: 댓글 작성 실패", err);
            error.value = '댓글 작성에 실패했습니다.';
            throw err;
        } finally {
            isLoading.value = false; // ⭐️ 최종 로딩 해제는 여기서 한 번만 ⭐️
        }
    };

    /**
     * 기존 댓글을 수정합니다.
     * @param {number} commentId - 수정할 댓글 ID
     * @param {object} commentData - { content }
     */
    const updateComment = async (commentId, commentData) => {
        if (!currentPostId.value) {
            error.value = '게시글 ID가 설정되지 않아 댓글을 수정할 수 없습니다.';
            console.error(error.value);
            return;
        }

        if (isLoading.value) return;

        isLoading.value = true;
        error.value = null;

        try {
            // CommentService 호출 (PUT /posts/{postId}/comments/{commentId})
            const updatedComment = await CommentService.updateComment(
                currentPostId.value,
                commentId,
                commentData
            );

            // // 로컬 상태를 낙관적으로 업데이트 (Optimistic Update)
            // const index = comments.value.findIndex(c => c.id === commentId);
            // if (index !== -1) {
            //     // 백엔드에서 반환된 최신 데이터로 교체
            //     comments.value[index] = updatedComment;
            // }

            // ⭐️ 수정: 배열 불변성을 이용한 낙관적 업데이트 ⭐️
            // Vue의 반응성 갱신을 확실히 하기 위해 배열 자체를 교체합니다.
            comments.value = comments.value.map(c =>
                c.commentId === commentId ? updatedComment : c
            );

            return updatedComment;

        } catch (err) {
            console.error("Comment Store: 댓글 수정 실패", err);
            error.value = '댓글 수정에 실패했습니다.';
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    /**
     * 특정 댓글을 삭제합니다.
     * @param {number} commentId - 삭제할 댓글 ID
     */
    const deleteComment = async (commentId) => {
        if (!currentPostId.value) {
            error.value = '게시글 ID가 설정되지 않아 댓글을 삭제할 수 없습니다.';
            console.error(error.value);
            return;
        }

        if (isLoading.value) return;

        isLoading.value = true;
        error.value = null;

        try {
            // CommentService 호출 (DELETE /posts/{postId}/comments/{commentId})
            await CommentService.deleteComment(currentPostId.value, commentId);

            // 로컬 상태를 낙관적으로 업데이트 (Optimistic Update)
            comments.value = comments.value.filter(c => c.commentId !== commentId);

        } catch (err) {
            console.error("Comment Store: 댓글 삭제 실패", err);
            error.value = '댓글 삭제에 실패했습니다.';
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    return {
        comments,
        isLoading,
        error,
        currentPostId,
        commentCount,
        setCurrentPostId,
        fetchComments,
        createComment,
        updateComment,
        deleteComment,
    }
});