<script setup>
import { computed, ref } from 'vue';
import { useAuthStore } from '@/stores/useAuthStore.js';
import { useCommentStore} from '@/stores/useCommentStore.js';

const props = defineProps({
  comment: {
    type: Object,
    required: true,
    validator: (comment) => ['commentId', 'content', 'authorId', 'authorName', 'createdAt'].every(key => key in comment)
  }
});

// 부모 컴포넌트(CommentList)에 삭제 모달 요청 이벤트를 보냅니다.
// 또한, 수정 성공 시 토스트 메시지를 상위 컴포넌트로 전달합니다.
const emit = defineEmits(['open-delete-modal', 'comment-submitted']);

const authStore = useAuthStore();
const commentStore = useCommentStore();

// --- 상태 관리 ---
const isEditing = ref(false); // 수정 모드 상태
const editContent = ref(props.comment.content); // 수정 중인 댓글 내용
const editError = ref(null); // 수정 폼 내 에러 메시지

// Computed: 현재 로그인 사용자가 댓글 작성자인지 확인
const isCommentAuthor = computed(() => {
  const currentUserId = authStore.user?.id;
  return currentUserId && currentUserId === props.comment.authorId;
});

// 날짜 포맷팅 함수
const formatDate = (dateString) => {
  if (!dateString) return'';
  const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
  return new Date(dateString).toLocaleDateString('ko-KR', options);
};

// 수정 로직
const startEdit = () => {
  isEditing.value = true;
  editContent.value = props.comment.content;
  editError.value = null;
}

const cancelEdit = () => {
  isEditing.value = false;
  editError.value = null;
}

const saveEdit = async () => {
  // 1. 내용 변경이 없는 경우
  if (editContent.value.trim() === props.comment.content.trim()) {
    isEditing.value = false;
    return;
  }

  // 2. 유효성 검사 (빈 내용 방지)
  if (editContent.value.trim().length === 0) {
    editError.value = '댓글 내용을 입력해주세요.';
    return;
  }

  try {
    await commentStore.updateComment(props.comment.commentId, { content: editContent.value.trim() });
    isEditing.value = false;
    editError.value = null;

    // 부모에게 성공 알림을 보냅니다. (BoardDetail의 Toast를 사용하기 위함)
    emit('comment-submitted', '댓글이 성공적으로 수정되었습니다.', 'success');
  } catch (err) {
    // Store에서 발생한 오류 메시지 사용
    editError.value = commentStore.error || '댓글 수정에 실패했습니다.';
    console.error('댓글 수정 오류:', err);

    // 실패 토스트 메시지를 부모에게 전달
    emit('comment-submitted', editError.value, 'error');
  }
};

// --- 삭제 요청 로직 ---
const openDeleteModal = () => {
  // 부모 컴포넌트에게 삭제할 commentId를 전달하여 모달을 띄우도록 요청
  emit('open-delete-modal', props.comment.commentId);
};
</script>

<template>
  <!-- 각 댓글 항목 -->
  <div class="border-b border-gray-200 pb-4 last:border-b-0">
    <div class="flex justify-between items-start mb-2">
      <div class="text-sm flex flex-col md:flex-row md:items-center">
        <!-- 작성자명과 날짜 -->
        <span class="font-bold text-gray-800 md:mr-3">{{ props.comment.authorName || '익명' }}</span>
        <span class="text-gray-500 text-xs mt-0.5 md:mt-0">{{ formatDate(props.comment.createdAt) }}</span>
      </div>

      <!-- 수정/삭제 버튼 -->
      <div v-if="isCommentAuthor && !isEditing" class="flex space-x-1">
        <button
            @click="startEdit"
            class="text-blue-500 hover:text-blue-700 text-xs font-medium transition duration-150 px-2 py-1 rounded-full"
            title="댓글 수정"
        >
          수정
        </button>
        <button
            @click="openDeleteModal"
            class="text-red-500 hover:text-red-700 text-xs font-medium transition duration-150 px-2 py-1 rounded-full"
            title="댓글 삭제"
        >
          삭제
        </button>
      </div>
    </div>

    <!-- 댓글 내용 표시 영역 (수정 모드가 아닐 때) -->
    <div v-if="!isEditing">
      <p class="text-gray-700 whitespace-pre-wrap pl-1">{{ props.comment.content }}</p>
    </div>

    <!-- 댓글 수정 폼 (수정 모드일 때) -->
    <div v-else class="space-y-2 mt-2">
        <textarea
            v-model="editContent"
            rows="3"
            class="w-full px-3 py-2 border border-blue-400 rounded-lg focus:ring-2 focus:ring-blue-500 transition duration-150 resize-none text-gray-800"
            :disabled="commentStore.isLoading"
        ></textarea>

      <p v-if="editError" class="text-red-500 text-sm font-medium">{{ editError }}</p>

      <div class="flex justify-end space-x-2">
        <button
            @click="cancelEdit"
            class="bg-gray-400 hover:bg-gray-500 text-white text-sm py-1 px-3 rounded-lg transition duration-200"
            :disabled="commentStore.isLoading"
        >
          취소
        </button>
        <button
            @click="saveEdit"
            :disabled="commentStore.isLoading || editContent.trim().length === 0"
            class="bg-blue-600 hover:bg-blue-700 text-white text-sm py-1 px-3 rounded-lg transition duration-200 disabled:opacity-50"
        >
          저장
        </button>
      </div>
    </div>
  </div>
</template>