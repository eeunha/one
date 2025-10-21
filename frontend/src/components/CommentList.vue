<script setup>
import { watch } from 'vue';
import { useCommentStore } from '@/stores/useCommentStore.js';
import CommentItem from './CommentItem.vue';
import CommentForm from './CommentForm.vue';

const props = defineProps({
  postId: {
    type: Number,
    required: true,
  },
});

// 자식 컴포넌트(CommentItem, CommentForm)에서 발생한 이벤트를 부모(BoardDetail)에게 전달하기 위해 정의합니다.
const emit = defineEmits(['open-delete-modal', 'comment-submitted']);

const commentStore = useCommentStore();

// postId가 변경될 때마다 댓글을 다시 불러옵니다. (BoardDetail이 리로드되거나 라우터 파라미터가 변경될 때)
watch(() => props.postId, (newPostId) => {
  if (newPostId && !isNaN(newPostId)) {
    commentStore.fetchComments(newPostId);
  }
}, { immediate: true });

// ⭐️ 중계 로직: CommentItem에서 삭제 버튼 클릭 시, BoardDetail에 모달을 열도록 요청합니다. ⭐️
const handleOpenDeleteModal = (commentId) => {
  emit('open-delete-modal', commentId);
};

// ⭐️ 중계 로직: CommentItem이나 CommentForm에서 댓글 작성/수정 성공 시, BoardDetail에 토스트를 띄우도록 요청합니다. ⭐️
const handleCommentSubmitted = (message, type) => {
  emit('comment-submitted', message, type);
};
</script>

<template>
  <div>
    <!-- 댓글 개수를 표시하는 제목 -->
    <h2 class="text-2xl font-bold mb-6 mt-8 text-gray-800 border-b pb-3">
      댓글 ({{ commentStore.commentCount }})
    </h2>

    <!-- 로딩 상태 -->
    <div v-if="commentStore.isLoading && commentStore.commentCount === 0" class="text-center py-5">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-2 text-gray-600">댓글을 불러오는 중...</p>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="commentStore.error && commentStore.commentCount === 0" class="text-center py-5 text-red-500 border border-red-300 bg-red-50 rounded-lg">
      {{ commentStore.error }}
    </div>

    <!-- 빈 목록 상태 -->
    <div v-else-if="commentStore.commentCount === 0" class="text-center py-5 text-gray-500">
      등록된 댓글이 없습니다. 첫 댓글을 작성해보세요!
    </div>

    <!-- 댓글 목록 -->
    <div v-else class="space-y-6">
      <CommentItem
          v-for="comment in commentStore.comments"
          :key="comment.commentId"
          :comment="comment"
          @open-delete-modal="handleOpenDeleteModal"
          @comment-submitted="handleCommentSubmitted"
      />
    </div>

    <!-- ⭐️ 댓글 작성 폼 배치 (목록 위에 위치) ⭐️ -->
    <CommentForm @comment-submitted="handleCommentSubmitted" />
  </div>
</template>