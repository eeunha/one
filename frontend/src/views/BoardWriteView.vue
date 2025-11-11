<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useBoardStore } from "@/stores/useBoardStore.js";

const router = useRouter();
const boardStore = useBoardStore();

// 폼 데이터 상태
const postData = ref({
  title: '',
  content: '',
});

// 에러 메시지 상태
const errorMessage = ref('');

// 폼 유효성 검사 (제목, 내용이 비어있지 않아야 함)
const isFormValid = computed(() => {
  return postData.value.title.trim() !== '' && postData.value.content.trim() !== '';
});

/**
 * 폼 제출 처리 함수
 */
const handleSubmit = async () => {
  errorMessage.value = ''; // 에러 초기화

  if (!isFormValid.value) {
    errorMessage.value = '제목과 내용을 모두 입력해주세요.';
    return;
  }

  try {
    // Store 액션 호출 및 생성된 게시글 ID 받기
    const postId = await boardStore.createPost(postData.value);

    if (postId) {
      // 작성 성공 시 상세 페이지로 이동
      router.push({ name: 'BoardDetail', params: { id: postId } });
    }
  } catch (error) {
    // API 호출 실패 시 에러 메시지 표시 (백엔드에서 받은 메시지 우선 사용)
    const message = error.response?.data?.message || '게시글 작성 중 오류가 발생했습니다.';
    errorMessage.value = message;
    console.error('Submission Error: ', error);
  }
};
</script>

<template>
  <div class="container mx-auto p-4 md:p-10 max-w-4xl">
    <h1 class="text-3xl font-bold mb-6 text-gray-800">오늘의 루틴 기록</h1>

    <!-- 로딩 인디케이터 -->
    <div v-if="boardStore.isLoading" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-4 text-lg text-blue-600">루틴을 등록하는 중입니다...</p>
    </div>

    <!-- 작성 폼 -->
    <form @submit.prevent="handleSubmit" v-else class="bg-white p-8 rounded-xl shadow-2xl space-y-6">
      <!-- 제목 입력 -->
      <div>
        <label for="title" class="block text-lg font-semibold text-gray-700 mb-2">제목</label>
        <input
            id="title"
            v-model="postData.title"
            type="text"
            required
            placeholder="[카테고리] 루틴을 한 줄로 요약해주세요. (예: [운동] 아침 푸시업 20개 성공)"
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
            placeholder="루틴을 실천한 후 느낀 점, 혹은 꾸준히 할 수 있었던 나만의 노하우를 기록해보세요."
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
            @click="router.push({ name: 'BoardList' })"
            class="bg-gray-400 hover:bg-gray-500 text-white font-bold py-3 px-6 rounded-lg transition duration-200 shadow-md"
        >
          돌아가기
        </button>
        <button
            type="submit"
            :disabled="!isFormValid || boardStore.isLoading"
            class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg transition duration-200 shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
        >
          기록하기
        </button>
      </div>
    </form>

<!--    <div class="mt-8 p-4 border rounded-lg bg-green-50 text-green-700">-->
<!--      <h2 class="font-bold text-xl">안내</h2>-->
<!--      <p class="mt-2">이 페이지는 <span class="font-mono bg-green-200 px-1 rounded">requiresAuth: true</span> 메타 태그로 보호되어 있습니다. (src/router/index.js)</p>-->
<!--    </div>-->
  </div>
</template>