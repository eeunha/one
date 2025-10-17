<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useBoardStore } from "@/stores/useBoardStore.js";
import { useAuthStore } from "@/stores/useAuthStore.js";
import DeleteConfirmationModal from '@/components/DeleteConfirmationModal.vue';

const route = useRoute();
const router = useRouter();
const boardStore = useBoardStore();
const authStore = useAuthStore();

// props 정의를 제거하고, route.params에서 ID를 직접 가져와 Number로 변환합니다.
// 이 방식이 router 설정 유무에 관계없이 가장 확실하게 ID를 가져옵니다.
const postId = computed(() => Number(route.params.id));

// 모달 상태 및 에러 메시지 관리
const isDeleteModalOpen = ref(false);
const deleteError = ref ('');

// 컴포넌트 마운트 시 상세 정보 로드
onMounted(async () => { // onMounted 훅을 async로 선언합니다.

  if (postId.value && !isNaN(postId.value)) {
    try {
      // await을 사용하여 fetchPostDetail 액션이 완료될 때까지 기다립니다.
      // 이 시점에 currentPost.value에는 조회수가 1 증가된 최신 데이터가 들어옵니다.
      await boardStore.fetchPostDetail(postId.value);
    } catch (error) {
      // API 호출 실패 시 (e.g., 404), postNotFound 상태가 활성화됩니다.
      console.error("게시글 상세 정보 로드 중 오류 발생: ", error);
    }
  } else {
    // ID가 유효하지 않은 경우, 로딩 상태를 false로 설정하여 '찾을 수 없음' 표시
    boardStore.isLoading = false;
    console.error("게시글 ID가 유효하지 않습니다:", route.params.id);
  }

})

// 현재 로그인된 사용자가 게시글 작성자인지 확인하는 Computed 속성
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

// 수정 버튼 클릭 핸들러
const handleEdit = () => {
  console.log('수정 버튼 클릭: ', postId.value);
  router.push({ name: 'BoardUpdate', params: { id: postId.value } });
}

// 삭제 버튼 클릭 핸들러
const handleDelete = () => {
  console.log('삭제 버튼 클릭: ', postId.value);

  isDeleteModalOpen.value = true;
  deleteError.value = ''; // 모달을 열 때 이전 에러 초기화
}

// ⭐️ [수정] 모달에서 '삭제 확인' 버튼 클릭 시 최종 로직 실행 (토스트 활성화) ⭐️
const confirmDelete = async () => {
  if (!postId.value) return;

  // Pinia Store의 isLoading 상태가 true가 되면, 모달의 확인 버튼이 자동으로 비활성화됩니다.
  try {
    await boardStore.deletePost(postId.value);

    // 1. 삭제 성공: 모달 닫기
    isDeleteModalOpen.value = false;

    // 2. ⭐️ [핵심] 토스트 메시지 상태를 Pinia Store에 저장하고 이동합니다. ⭐️
    // BoardList.vue에서 이 상태를 확인하고 토스트를 띄웁니다.
    boardStore.setTransientToast('게시글이 성공적으로 삭제되었습니다.', 'success');

    // 3. 목록으로 이동
    router.push({ name: 'BoardList' });

    console.log('게시글 삭제 성공 및 목록 이동');

  } catch (error) {
    // 3. 삭제 실패: 에러 메시지를 모달에 표시
    const errorMessage = error.response?.data?.message || "게시글 삭제에 실패했습니다. 권한을 확인해 주세요.";
    deleteError.value = errorMessage;

    console.error("삭제 실패:", error);
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '날짜 없음';
  const options = { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
  return new Date(dateString).toLocaleDateString('ko-KR', options);
}

// 게시글 로드 완료 후 데이터가 없는지 확인하는 Computed 속성
const postNotFound = computed(() => !boardStore.isLoading && !boardStore.currentPost);

</script>

<template>
  <div class="container mx-auto p-4 md:p-10 max-w-4xl">
    <div v-if="boardStore.isLoading" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
      <p class="mt-4 text-lg text-gray-600">게시글을 불러오는 중...</p>
    </div>

    <!-- 게시글을 찾을 수 없는 경우를 postNotFound로 처리 -->
    <div v-else-if="postNotFound" class="text-center py-20 bg-white rounded-xl shadow-lg">
      <p class="text-2xl text-red-500 font-bold">게시글을 찾을 수 없거나 삭제되었습니다.</p>
      <button
          @click="router.push({ name: 'BoardList' })"
          class="mt-6 bg-blue-500 hover:bg-blue-600 text-white font-semibold py-2 px-6 rounded-lg transition duration-200 shadow"
      >
        목록으로 돌아가기
      </button>
    </div>

    <div v-else class="bg-white rounded-xl shadow-2xl p-6 md:p-10">
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

        <!-- 수정/삭제 버튼: isAuthor가 true일 때만 표시 -->
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
  </div>

  <!-- 커스텀 모달 컴포넌트 연결 -->
  <DeleteConfirmationModal
    :show="isDeleteModalOpen"
    :title="'게시글 삭제 확인'"
    :message="'정말로 이 게시글을 삭제하시겠습니까? 삭제된 게시글은 복구할 수 없습니다.'"
    :is-loading="boardStore.isLoading"
    :error="deleteError"
    @update:show="isDeleteModalOpen = $event"
    @confirm="confirmDelete"
  />
</template>