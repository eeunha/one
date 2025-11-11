<script setup>
import {ref, computed} from 'vue';
import { useCommentStore } from '@/stores/useCommentStore.js';
import { useAuthStore } from '@/stores/useAuthStore.js';

const commentStore = useCommentStore();
const authStore = useAuthStore();

const newCommentContent = ref('');
const commentError = ref(null);

const isAuthenticated = computed(() => authStore.isAuthenticated);

const authorName = computed(() => authStore.user?.name || '익명');

// 댓글 내용 유효성 검사
const isFormValid = computed(() => newCommentContent.value.trim().length > 0);

const handleSubmitComment = async () => {
  if (!isAuthenticated.value) {
    commentError.value = '로그인 후 댓글을 작성할 수 있습니다.';
    return;
  }
  if (!isFormValid.value) {
    commentError.value = '댓글 내용을 입력해주세요.';
    return;
  }

  const commentData = {
    content: newCommentContent.value.trim(),
  };

  commentError.value = null;

  try {
    await commentStore.createComment(commentData);
    newCommentContent.value = '';

    // 댓글 작성 성공 토스트 메시지를 부모에게 전달합니다. (BoardDetail.vue에서 수신)
    emit('comment-submitted', '댓글이 성공적으로 작성되었습니다.', 'success');
  } catch (err) {
    // Store에서 발생한 오류 메시지 사용
    commentError.value = commentStore.error || '댓글 작성 중 알 수 없는 오류가 발생했습니다.';
    console.error('댓글 작성 오류: ', err);

    // 실패 토스트 메시지를 부모에게 전달합니다.
    emit('comment-submmited', commentError.value, 'error');
  }
};

// 부모 컴포넌트(BoardDetail)에 토스트 메시지를 전달하기 위한 이벤트 정의
const emit = defineEmits('comment-submitted');

</script>

<template>
  <div class="mb-6 mt-6">

    <!-- 비로그인 상태 안내 -->
    <div v-if="!isAuthenticated" class="p-4 mt-6 bg-gray-100 border border-gray-300 rounded-lg text-center text-gray-600">
      응원/질문을 작성하려면 로그인해주세요.
    </div>

    <!-- 로그인 상태 - 댓글 폼 -->
    <div v-else class="space-y-3">
        <textarea
            v-model="newCommentContent"
            rows="4"
            :placeholder="`궁금한 점을 질문하거나, 응원의 메시지를 남겨보세요!`"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 transition duration-150 resize-none text-gray-800"
            :disabled="commentStore.isLoading"
        ></textarea>

      <p v-if="commentError" class="text-red-500 text-sm font-medium">{{ commentError }}</p>

      <div class="flex justify-end">
        <button
            @click="handleSubmitComment"
            :disabled="!isFormValid || commentStore.isLoading"
            class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-5 rounded-lg transition duration-200 shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ commentStore.isLoading ? '등록 중...' : '등록하기' }}
        </button>
      </div>
    </div>
  </div>
</template>