<script setup>
import { onMounted } from "vue";
import { useBoardStore } from "@/stores/useBoardStore.js";
import { useRouter } from "vue-router";

// Pinia Store 및 Router 사용
const boardStore = useBoardStore();
const router = useRouter();

// 로드 시점: 컴포넌트 마운트 후 Store의 액션을 호출
onMounted(() => {
  // Store에 데이터가 없다면 로드 시작
  // 데이터가 이미 캐싱되어 있다면 다시 호출하지 않아 불필요한 API 요청을 줄입니다.
  if (boardStore.posts.length === 0) {
    boardStore.fetchPosts();
  }
});

/**
 * 글 작성 페이지로 이동
 */
const goToWrite = () => {
  router.push({ name: 'BoardWrite' });
};

/**
 * 상세 페이지로 이동
 * @param {number} id - 게시글 ID
 */
const goToDetail = (id) => {
  // ⭐ FIX: ID가 유효한지 먼저 확인합니다. ⭐
  if (!id) {
    console.error("게시글 ID가 유효하지 않아 상세 페이지로 이동할 수 없습니다. Post ID:", id);
    return;
  }
  // 라우터의 params를 사용하여 게시글 ID를 동적 라우트로 전달합니다.
  router.push({ name:'BoardDetail', params: { id: id } });
};

// 날짜 포맷팅 함수 (예시)
const formatDate = (dateString) => {
  if (!dateString) return '날짜 없음';
  // 서버에서 받은 ISO 문자열을 보기 좋게 포맷합니다.
  const options = { year: 'numeric', month: '2-digit', day: '2-digit' };
  return new Date(dateString).toLocaleDateString('ko-KR', options);
}
</script>

<template>
  <div class="container mx-auto p-4 md:p-8 max-w-4xl">
    <!-- 헤더 및 버튼 -->
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-extrabold text-gray-800">
        게시판 목록 (총 {{ boardStore.postCount }}개)
      </h1>
      <button
          @click="goToWrite"
          class="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 shadow-md"
      >
        글 작성
      </button>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="boardStore.isLoading" class="flex justify-center items-center h-48">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      <p class="ml-4 text-lg text-gray-600">게시글을 불러오는 중...</p>
    </div>

    <!-- 게시글 목록 -->
    <div v-else class="space-y-4">
      <template v-if="boardStore.posts.length > 0">
        <div
            v-for="post in boardStore.posts"
            :key="post.id"
            @click="goToDetail(post.id)"
            class="bg-white p-5 rounded-xl shadow-lg hover:shadow-xl transition duration-300 cursor-pointer border-l-4 border-blue-500 hover:border-blue-700"
        >
          <div class="flex justify-between items-center mb-1">
            <!-- 제목 (글이 길면 ...으로 처리) -->
            <h2 class="text-xl font-bold text-gray-800 truncate pr-4">
              {{ post.title }}
            </h2>
            <!-- 작성일 -->
            <span class="text-sm text-gray-500 font-medium flex-shrink-0">
                            {{ formatDate(post.createdAt) }}
                        </span>
          </div>

          <!-- 내용 미리보기 (두 줄까지만 표시) -->
          <p class="text-gray-600 text-sm mb-2 line-clamp-2">
            {{ post.content }}
          </p>

          <!-- 작성자 및 조회수 -->
          <div class="flex justify-between text-xs text-gray-500 mt-2">
            <span>게시글번호: {{ post.id || '게시글번호' }}</span>
            <span>작성자: {{ post.authorName || '익명' }}</span>
            <span>조회수: {{ post.viewCount || 0 }}</span>
          </div>
        </div>
      </template>
      <!-- 게시글이 없을 경우 -->
      <div v-else class="text-center py-10 bg-white rounded-xl shadow-lg">
        <p class="text-xl text-gray-500">아직 작성된 게시글이 없습니다.</p>
        <p class="text-md text-gray-400 mt-2">새 글을 작성해 보세요!</p>
      </div>
    </div>
  </div>
</template>