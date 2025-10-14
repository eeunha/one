<script setup>
import { onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useBoardStore } from "@/stores/useBoardStore.js";
import { useAuthStore } from "@/stores/useAuthStore.js";

const route = useRoute();
const router = useRouter();
const boardStore = useBoardStore();
const authStore = useAuthStore();
const id = route.params.id;

// 컴포넌트 마운트 시 상세 정보 로드
onMounted(() => {
  if (id) {
    boardStore.fetchPostDetail(id);
  }
})

// ⭐ [추가] 현재 로그인된 사용자가 게시글 작성자인지 확인하는 Computed 속성 ⭐
const isAuthor = computed(() => {
  const post = boardStore.currentPost;

  // user?.id를 사용하여 user 객체가 null이 아닐 때만 id 속성에 접근합니다.
  const currentUserId = authStore.user?.id;

  // 게시글이 로드되었고, 사용자가 로그인했고, 게시글 작성자 ID와 사용자 ID가 일치할 때
  return (
      post &&
      post.authorId &&
      currentUserId &&
      currentUserId === post.authorId
  );
});

// 수정 버튼 클릭 핸들러 (다음 단계에서 구현)
const handleEdit = () => {
  console.log('수정 버튼 클릭: ', id);
  router.push({ name: 'BoardUpdate', params: { id: id } });
}

// 삭제 버튼 클릭 핸들러 (다음 단계에서 구현)
const handleDelete = () => {
  console.log('삭제 버튼 클릭: ', id);
}

const formatDate = (dateString) => {
  if (!dateString) return '날짜 없음';
  const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
  return new Date(dateString).toLocaleDateString('ko-KR', options);
}
</script>

<template>
  <div class="container mx-auto p-4 md:p-10 max-w-4xl">
    <div v-if="boardStore.isLoading" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-4 text-lg text-gray-600">게시글을 불러오는 중...</p>
    </div>

    <div v-else-if="boardStore.currentPost" class="bg-white rounded-xl shadow-2xl p-6 md:p-10">
      <!-- 헤더: 제목 및 정보 -->
      <header class="border-b pb-4 mb-6">
        <h1 class="text-4xl font-extrabold text-gray-900 mb-3 break-words">
          {{ boardStore.currentPost.title }}
        </h1>
        <div class="flex items-center text-sm text-gray-500">
          <span class="mr-4">
            작성자: <strong class="text-gray-700">{{ boardStore.currentPost.authorName || '익명' }}</strong>
          </span>
          <span class="mr-4">
            작성일: {{ formatDate(boardStore.currentPost.createdAt) }}
          </span>
          <span>
            조회수: {{ boardStore.currentPost.viewCount || 0 }}
          </span>
        </div>
      </header>

      <!-- 본문 내용 -->
      <section class="min-h-[200px] text-lg text-gray-700 leading-relaxed whitespace-pre-wrap mb-10">
        {{ boardStore.currentPost.content }}
      </section>

      <!-- 액션 버튼 영역 -->
      <footer class="flex justify-between border-t pt-4">
        <button
            @click="router.push({ name: 'BoardList' })"
            class="bg-gray-200 hover:bg-gray-300 text-gray-700 font-semibold py-2 px-4 rounded-lg transition duration-200 shadow"
        >
          목록으로
        </button>

        <!-- ⭐ [수정] 수정/삭제 버튼: isAuthor가 true일 때만 표시 ⭐ -->
        <div v-if="isAuthor" class="space-x-2">
          <button
              @click="handleEdit"
              class="bg-yellow-500 hover:bg-yellow-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 shadow"
          >
            수정
          </button>
          <button
              @click="handleDelete"
              class="bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 shadow"
          >
            삭제
          </button>
        </div>
      </footer>
    </div>

    <!-- 게시글이 없을 경우 -->
    <div v-else class="text-center py-20 bg-white rounded-xl shadow-lg">
      <p class="text-2xl text-red-500 font-bold">게시글을 찾을 수 없거나 삭제되었습니다.</p>
      <button
          @click="router.push({ name: 'BoardList' })"
          class="mt-6 bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-6 rounded-lg transition duration-200 shadow"
      >
        목록으로 돌아가기
      </button>
    </div>
  </div>
</template>