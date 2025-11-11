<script setup>
import { ref, onMounted, watch } from "vue";
import { useBoardStore } from "@/stores/useBoardStore.js";
import { useRouter, useRoute } from "vue-router";
import Pagination from "@/components/Pagination.vue";
import Toast from '@/components/Toast.vue';

// Pinia Store 및 Router 사용
const router = useRouter();
const route = useRoute();
const boardStore = useBoardStore();

// 로컬 토스트 상태
// ⭐️ [추가] 목록 페이지에서 토스트를 띄우기 위한 상태 변수 ⭐️
const isToastVisible = ref(false);
const toastMessage = ref('');
const toastType = ref('success');

// URL에서 현재 페이지를 가져오거나 기본값 1을 사용합니다.
const getCurrentPageFromRoute = () => {
  // URL 쿼리 파라미터는 문자열이므로 숫자로 파싱 (기본값은 1)
  const page = route.query.page ? parseInt(route.query.page) : 1;
  // 페이지 번호는 최소 1이 되도록 보장
  return page > 0 ? page : 1;
};

// 게시글을 로드하는 핵심 함수
const loadPosts = (page) => {
  // Store 액션에 1부터 시작하는 페이지 번호를 바로 전달합니다. (Store가 page-1 변환 담당)
  boardStore.fetchPosts(page);
};

// ⭐️ [핵심] Pinia Store에 임시 토스트가 있는지 확인하고 처리하는 함수 ⭐️
const handleTransientToast = () => {
  // Pinia Store에 메시지가 남아있다면
  if (boardStore.transientToast) {
    const { message, type } = boardStore.transientToast;

    // 1. 토스트 띄우기
    showToast(message, type);

    // 2. 메시지를 즉시 지우기 (매우 중요! 다음에 페이지 로드해도 다시 뜨지 않도록)
    boardStore.clearTransientToast();
  }
};

// 로드 시점: 컴포넌트 마운트 후 Store의 액션을 호출
onMounted(async () => {
  // ⭐️ 마운트 시 URL 쿼리를 기준으로 데이터 로드 시작 ⭐️
  // fetchPosts가 비동기 함수이므로 await을 추가하여 데이터 로드를 기다립니다.
  await loadPosts(getCurrentPageFromRoute());

  // ⭐️ [핵심] 마운트 후 혹시 도착한 토스트 메시지가 있는지 확인합니다. ⭐️
  handleTransientToast();
});

// ⭐ FIX: 로드 시점 2: URL 쿼리 파라미터 'page'의 변경을 감지하고 데이터 재로드 ⭐
watch(
    () => route.query.page, // 감시 대상: URL 쿼리 파라미터의 'page' 값
    (newPage, oldPage) => {
      // page 쿼리가 변경되었을 때만 데이터 로드 (페이지를 떠날 때는 실행하지 않음)
      if (newPage !== oldPage) {
        loadPosts(getCurrentPageFromRoute());
        window.scrollTo(0, 0);
      }
    }
);

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

// 날짜 포맷팅 함수
const formatDate = (dateString) => {
  if (!dateString) return '날짜 없음';
  // 서버에서 받은 ISO 문자열을 보기 좋게 포맷합니다.
  const options = { year: 'numeric', month: '2-digit', day: '2-digit' };
  return new Date(dateString).toLocaleDateString('ko-KR', options);
};

const changePage = (page) => {
  router.push({ name: 'BoardList', query: { page } })
};

// ⭐️ [추가] 토스트를 보여주는 함수 ⭐️
const showToast = (message, type = 'success') => {
  toastMessage.value = message;
  toastType.value = type;
  isToastVisible.value = true;
};

// ⭐️ [추가] 순차 번호를 계산하는 Computed Property나 함수 (템플릿에서 바로 사용) ⭐️
// 이 함수는 템플릿의 v-for 내부에서 index를 인수로 받습니다.
const getSequentialNumber = (index) => {
  const currentPage = boardStore.pagination.currentPage;
  const postsPerPage = boardStore.pagination.size;

  // (현재 페이지 번호 - 1) * 페이지당 글 수 = 오프셋 (이전 페이지까지의 글 수)
  const offset = (currentPage - 1) * postsPerPage;

  // 최종 순차 번호 = 오프셋 + 현재 페이지 배열 내 인덱스(0부터 시작) + 1
  return offset + index + 1;
};

</script>

<template>
  <div class="p-4 md:p-8">
    <!-- 헤더 및 버튼 -->
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-extrabold text-gray-800">
        전체 루틴 피드 (총 {{ boardStore.postCount }}개)
      </h1>
      <button
          @click="goToWrite"
          class="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 shadow-md"
      >
        루틴 기록하기
      </button>
    </div>

    <!-- 로딩 상태 -->
    <div v-if="boardStore.isLoading" class="flex justify-center items-center h-48">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500"></div>
      <p class="ml-4 text-lg text-gray-600">루틴을 불러오는 중...</p>
    </div>

    <!-- 게시글 목록 -->
    <div v-else class="space-y-4">
      <template v-if="boardStore.posts.length > 0">
        <div
            v-for="(post, index) in boardStore.posts"
            :key="post.id"
            @click="goToDetail(post.id)"
            class="bg-white p-5 rounded-xl shadow-lg hover:shadow-xl transition duration-300 cursor-pointer border-l-4 border-emerald-500 hover:border-emerald-700 flex items-center"
        >

          <!-- 1. Left Column: Number (글번호) -->
          <div class="w-16 flex-shrink-0 text-center text-gray-500">
            <span class="text-lg font-bold text-emerald-700">
              {{ getSequentialNumber(index) }}
            </span>
          </div>

          <!-- 2. Center Column: Title and Content (제목 및 내용) -->
          <!-- flex-grow로 남은 공간 대부분 차지, min-w-0으로 오버플로우 방지 -->
          <div class="flex-grow min-w-0 mx-4">
            <!-- Title -->
            <h2 class="text-xl font-bold text-gray-800 truncate mb-1">
              {{ post.title }}
            </h2>
            <!-- Content Preview -->
            <p class="text-gray-600 text-sm line-clamp-2">
              {{ post.content }}
            </p>
          </div>

          <!-- ⭐️ [수정] 3. Middle Column: Comment Count (댓글 수) - 게시글과 우측 정보 사이 ⭐️ -->
          <div class="hidden sm:flex w-15 flex-shrink-0 flex-col items-center justify-center text-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-cyan-600 mb-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 4v-4z" />
            </svg>
            <span class="text-md font-semibold" :class="{'text-gray-600': post.commentCount > 0, 'text-gray-400': post.commentCount === 0}">
                {{ post.commentCount || 0 }}
            </span>
          </div>

          <!-- 3. Right Column: Date, Author, Views (날짜, 작성자, 조회수) -->
          <!-- ⭐️ [수정] sm 사이즈(640px) 미만에서는 hidden으로 숨기고, sm 사이즈부터 다시 flex로 보이도록 설정 ⭐️ -->
          <div class="hidden sm:flex w-30 flex-shrink-0 flex-col items-end text-right text-gray-500 space-y-1">
            <!-- 1. 작성일 (날짜) -->
            <span class="font-medium text-sm text-gray-600">
              {{ formatDate(post.createdAt) }}
            </span>
            <!-- 2. 작성자 -->
            <span class="text-xs">작성자: {{ post.authorName || '익명' }}</span>
            <!-- 3. 조회수 -->
            <span class="text-xs">조회수: {{ post.viewCount || 0 }}</span>
          </div>
        </div>
      </template>
      <!-- 게시글이 없을 경우 -->
      <div v-else class="text-center py-10 bg-white rounded-xl shadow-lg">
        <p class="text-xl text-gray-500">아직 작성된 루틴이 없습니다.</p>
        <p class="text-md text-gray-400 mt-2">새 루틴을 작성해 보세요!</p>
      </div>
    </div>
    <Pagination
      :currentPage="boardStore.pagination.currentPage"
      :totalPages="boardStore.pagination.totalPages"
      @changePage="changePage"
    />
  </div>

  <!-- ⭐️ [필수] 토스트 컴포넌트 연결 ⭐️ -->
  <Toast
      :show="isToastVisible"
      :message="toastMessage"
      :type="toastType"
      @update:show="isToastVisible = $event"
  />
</template>