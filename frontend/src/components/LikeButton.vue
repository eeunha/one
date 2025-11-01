<script setup>
import {onMounted} from 'vue';
import {useLikeStore} from '@/stores/useLikeStore.js';

// Pinia Store 인스턴스
const likeStore = useLikeStore();

// 부모 컴포넌트로부터 postId를 props로 수신
const props = defineProps({
  postId: {
    type: Number,
    required: true,
  },
});

// 컴포넌트 마운트 시, 해당 게시글의 좋아요 상태와 카운트를 로드하여 Store에 설정
// 이 정보는 로그인된 사용자 기준의 '좋아요 여부'와 '총 좋아요 수'입니다.
onMounted(async () => {
  if (props.postId) {
    // Store 액션을 호출하여 초기 데이터 로드
    await likeStore.fetchLikeStatus(props.postId);
  }
});

// 좋아요 상태 토글 함수
const toggleLike = async () => {
  // 유효하지 않은 ID거나 로딩 중이면 실행하지 않음
  if (!props.postId || likeStore.isLoading) return;

  if (likeStore.isLiked) {
    // 현재 좋아요 상태: 취소 요청 (DELETE)
    await likeStore.unlikePost(props.postId);
  } else {
    // 현재 좋아요를 누르지 않은 상태: 좋아요 요청 (POST)
    await likeStore.likePost(props.postId);
  }
};
</script>

<template>
  <div class="flex items-center space-x-2">
    <!-- 좋아요 버튼 -->
    <button
      @click="toggleLike"
      :disabled="likeStore.isLoading"
      :class="[
          'flex items-center space-x-1 p-2 rounded-full transition duration-150 ease-in-out focus:outline-none focus:ring-2 focus:ring-opacity-50 disabled:opacity-50',
          likeStore.isLiked
            ? 'text-red-500 hover:bg-red-50 focus:ring-red-500' // 좋아요 상태 (빨간색)
            : 'text-gray-400 hover:text-red-500 hover:bg-red-50 focus:ring-gray-300' // 취소 상태(회색)
      ]"
    >
      <!-- 로딩 스피너 -->
      <svg v-if="likeStore.isLoading" class="animate-spin h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>

      <!-- 하트 아이콘 -->
      <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-heart"
           :fill="likeStore.isLiked ? 'currentColor' : 'none'"
           :class="{'scale-110': likeStore.isLiked, 'transition-transform duration-200': true}"
      >
        <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
      </svg>
    </button>

    <!-- 좋아요 수 -->
    <span class="text-xl font-bold text-gray-800 tabular-nums">
      {{ likeStore.likeCount }}
    </span>
  </div>
</template>

<style scoped>
/* 로딩 스피너를 위한 기본 애니메이션 스타일 (Tailwind CSS가 로드되지 않을 경우 대비) */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-spin { animation: spin 1s linear infinite; }
</style>