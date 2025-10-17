<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useBoardStore } from "@/stores/useBoardStore.js";
import { useAuthStore } from "@/stores/useAuthStore.js";

const route = useRoute();
const router = useRouter();
const boardStore = useBoardStore();
const authStore = useAuthStore();
const id = route.params.id; // URL 파라미터로 게시글 ID 획득

// 폼 데이터 상태
const postData = ref({
  id: id,
  title: '',
  content: ''
});

const errorMessage = ref('');

// 폼 유효성 검사
const isFormValid = computed(() => {
  return postData.value.title.trim() !== '' && postData.value.content.trim() !== '';
});

/**
 * 기존 게시글 데이터를 불러와 폼에 채우는 함수
 */
const loadPostData = async () => {
  if (!id) return;

  // 1. 상세 데이터 로드
  await boardStore.fetchPostDetail(id);

  // 2. 권한 확인 (작성자 본인인지 확인)
  const post = boardStore.currentPost;
  const currentUserId = authStore.user?.id;

  // 게시글이 없거나, 작성자가 아니거나, 로그인 상태가 아니면 목록으로 리다이렉션
  if (!post || post.authorId !== currentUserId) {
    router.replace({ name: 'BoardList' }); // 브라우저 히스토리 스택에 현재 항목 덮어쓰기. 뒤로가기 불가
    return;
  }

  // 3. 폼 데이터 초기화
  postData.value = {
    id: post.id,
    title: post.title,
    content: post.content
  };
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  loadPostData();
})

/**
 * 폼 제출 처리 함수 (수정 로직)
 */
const handleSubmit = async () => {
  errorMessage.value = ''; // 에러 초기화

  if (!isFormValid.value) {
    errorMessage.value = '제목과 내용을 모두 입력해주세요.';
    return;
  }

  try {
    // Store의 updatePost 액션 호출
    await boardStore.updatePost(postData.value.id, {
      newTitle: postData.value.title,
      newContent: postData.value.content
    });

    boardStore.setTransientToast('게시글이 성공적으로 수정되었습니다.', 'success');

    // 수정 성공 시 상세 페이지로 이동
    router.push({ name: 'BoardDetail', params: { id: id } });

  } catch (error) {
    // API 호출 실패 시 에러 메시지 표시 (백엔드에서 받은 메시지 우선 사용)
    const message = error.response?.data?.message || '게시글 수정 중 오류가 발생했습니다.';
    errorMessage.value = message;
    console.error('Update Submission Error: ', error);
  }
};
</script>

<template>
  <div class="container mx-auto p-4 md:p-10 max-w-4xl">
    <h1 class="text-3xl font-bold mb-6 text-gray-800">게시글 수정</h1>

    <!-- 로딩/데이터 없음 인디케이터 -->
    <div v-if="boardStore.isLoading && !postData.id" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-4 text-lg text-blue-600">데이터를 불러오는 중입니다...</p>
    </div>
    <div v-else-if="!boardStore.currentPost && !boardStore.isLoading" class="text-center py-20 bg-white rounded-xl shadow-lg">
      <p class="text-2xl text-red-500 font-bold">수정할 게시글을 찾을 수 없거나 권한이 없습니다.</p>
      <button
          @click="router.push({ name: 'BoardList' })"
          class="mt-6 bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-6 rounded-lg transition duration-200 shadow"
      >
        목록으로 돌아가기
      </button>
    </div>

    <!-- 수정 폼 -->
    <form @submit.prevent="handleSubmit" v-else class="bg-white p-8 rounded-xl shadow-2xl space-y-6">
      <!-- 제목 입력 -->
      <div>
        <label for="title" class="block text-lg font-semibold text-gray-700 mb-2">제목</label>
        <input
            id="title"
            v-model="postData.title"
            type="text"
            required
            placeholder="제목을 입력하세요."
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 text-gray-800"
        />
      </div>

      <!-- 내용 입력 -->
      <div>
        <label for="content" class="block text-lg font-semibold text-gray-700 mb-2">내용</label>
        <textarea
            id="content"
            v-model="postData.content"
            required
            rows="10"
            placeholder="내용을 입력하세요."
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 text-gray-800 resize-none"
        ></textarea>
      </div>

      <!-- 에러 메시지 -->
      <p v-if="errorMessage" class="text-red-500 font-medium bg-red-100 p-3 rounded-lg border border-red-300">
        {{ errorMessage }}
      </p>

      <!-- 버튼 영역 -->
      <div class="flex justify-end space-x-4">
        <button
            type="button"
            @click="router.push({ name: 'BoardDetail', params: { id: id } })"
            class="bg-gray-400 hover:bg-gray-500 text-white font-bold py-3 px-6 rounded-lg transition duration-200 shadow-md"
        >
          취소
        </button>
        <button
            type="submit"
            :disabled="!isFormValid || boardStore.isLoading"
            class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg transition duration-200 shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
        >
          수정 완료
        </button>
      </div>
    </form>
  </div>
</template>