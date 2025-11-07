<script setup>
import {ref, onMounted, computed} from "vue";
import {useRouter, useRoute} from "vue-router";
import {useBoardStore} from "@/stores/useBoardStore.js";
import {useAuthStore} from "@/stores/useAuthStore.js";
import {useCommentStore} from '@/stores/useCommentStore.js';
import ConfirmationModal from '@/components/ConfirmationModal.vue';
import Toast from '@/components/Toast.vue';
import CommentList from '@/components/comment/CommentList.vue';
import LikeButton from '@/components/button/LikeButton.vue';

const route = useRoute();
const router = useRouter();
const boardStore = useBoardStore();
const authStore = useAuthStore();
const commentStore = useCommentStore();

const postId = computed(() => Number(route.params.id));

// --- 상태 관리 ---

// 게시글 삭제 모달 상태
const isPostDeleteModalOpen = ref(false);
const postDeleteError = ref('');

// 2. ⭐️ 댓글 삭제 모달 상태 ⭐️
const isCommentDeleteModalOpen = ref(false);
const commentToDeleteId = ref(null); // 삭제할 댓글 ID 저장
const commentDeleteError = ref(''); // 댓글 삭제 에러 메시지

// 1. 좋아요 모달 상태
const showLikeModal = ref(false);
// 2. 좋아요 모달 처리 중 로딩 상태 (모달의 isLoading props에 전달)
const isProcessing = ref(false);
// 3. 좋아요 에러 메시지 (모달의 error props에 전달)
const likeError = ref('');

// 로컬 토스트 상태
const isToastVisible = ref(false);
const toastMessage = ref('');
const toastType = ref('success');

// 컴포넌트 마운트 시 상세 정보 로드
onMounted(async () => { // onMounted 훅을 async로 선언합니다.

  if (postId.value && !isNaN(postId.value)) {
    try {
      // await을 사용하여 fetchPostDetail 액션이 완료될 때까지 기다립니다.
      // 이 시점에 currentPost.value에는 조회수가 1 증가된 최신 데이터가 들어옵니다.
      await boardStore.fetchPostDetail(postId.value);

      handleTransientToast();

      // ⭐️ (4) Comment Store에 현재 postId 설정 ⭐️
      // CommentList의 watch 훅이 이 값을 감시하며 댓글을 로드합니다.
      commentStore.setCurrentPostId(postId.value);

    } catch (error) {
      // API 호출 실패 시 (e.g., 404), postNotFound 상태가 활성화됩니다.
      console.error("게시글 상세 정보 로드 중 오류 발생: ", error);
    }
  } else {
    // ID가 유효하지 않은 경우, 로딩 상태를 false로 설정하여 '찾을 수 없음' 표시
    boardStore.isLoading = false;
    console.error("게시글 ID가 유효하지 않습니다:", route.params.id);
  }
});

const handleTransientToast = () => {
  if (boardStore.transientToast) {
    const {message, type} = boardStore.transientToast;

    showToast(message, type);

    boardStore.clearTransientToast();
  }
};

const showToast = (message, type = 'success') => {
  toastMessage.value = message;
  toastType.value = type;
  isToastVisible.value = true;
};

const handleCommentToast = (message, type) => {
  showToast(message, type);
};

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

// 게시글 수정 버튼 클릭 핸들러
const handleEditPost = () => {
  console.log('수정 버튼 클릭: ', postId.value);
  router.push({name: 'BoardUpdate', params: {id: postId.value}});
};

// 게시글 삭제 버튼 클릭 핸들러
const handleDeletePost = () => {
  console.log('삭제 버튼 클릭: ', postId.value);

  isPostDeleteModalOpen.value = true;
  postDeleteError.value = ''; // 모달을 열 때 이전 에러 초기화
};

// ⭐️ [수정] 모달에서 '삭제 확인' 버튼 클릭 시 최종 로직 실행 (토스트 활성화) ⭐️
const confirmDeletePost = async () => {
  if (!postId.value) return;

  // Pinia Store의 isLoading 상태가 true가 되면, 모달의 확인 버튼이 자동으로 비활성화됩니다.
  try {
    await boardStore.deletePost(postId.value);

    // 1. 삭제 성공: 모달 닫기
    isPostDeleteModalOpen.value = false;

    // 2. ⭐️ [핵심] 토스트 메시지 상태를 Pinia Store에 저장하고 이동합니다. ⭐️
    // BoardList.vue에서 이 상태를 확인하고 토스트를 띄웁니다.
    boardStore.setTransientToast('게시글이 성공적으로 삭제되었습니다.', 'success');

    // 3. 목록으로 이동
    router.push({name: 'BoardList'});

    console.log('게시글 삭제 성공 및 목록 이동');

  } catch (error) {
    // 3. 삭제 실패: 에러 메시지를 모달에 표시
    const errorMessage = error.response?.data?.message || "게시글 삭제에 실패했습니다. 권한을 확인해 주세요.";
    postDeleteError.value = errorMessage;

    console.error("삭제 실패:", error);
  }
};

// --- 댓글 삭제 로직 ---

// 1. CommentList로부터 삭제 요청 수신 시
const handleOpenCommentDeleteModal = (commentId) => {
  commentToDeleteId.value = commentId; // 삭제할 댓글 ID 저장
  commentDeleteError.value = ''; // 에러 초기화
  isCommentDeleteModalOpen.value = true; // 모달 열기
}

const confirmDeleteComment = async () => {
  if (!commentToDeleteId.value) return;

  try {
    await commentStore.deleteComment(commentToDeleteId.value);

    isCommentDeleteModalOpen.value = false;
    commentToDeleteId.value = null;

    showToast('댓글이 성공적으로 삭제되었습니다.', 'success');

  } catch (error) {
    // 삭제 실패 시 모달에 에러 표시
    const errorMessage = commentStore.error || "댓글 삭제에 실패했습니다. 권한을 확인해 주세요.";
    commentDeleteError.value = errorMessage;
    console.error("댓글 삭제 실패:", error);
  }
};

// --- 기타 유틸리티 ---
const formatDate = (dateString) => {
  if (!dateString) return '날짜 없음';
  const options = {year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'};
  return new Date(dateString).toLocaleDateString('ko-KR', options);
};

// 게시글 로드 완료 후 데이터가 없는지 확인하는 Computed 속성
const postNotFound = computed(() => !boardStore.isLoading && !boardStore.currentPost);

const handleLikeConfirm = async () => {

  console.log('handleLikeConfirm');

  isProcessing.value = true;
  likeError.value = '';

  try {
    showLikeModal.value = false;
    await router.push({ name: 'Login' });

  } catch (error) {
    console.error('로그인 페이지 이동 실패: ', error);
    isProcessing.value = false;

    // 사용자에게 이동 실패를 알립니다.
    likeError.value = '로그인 페이지로 이동 중 오류가 발생했습니다.';
    showToast(likeError.value, error);

    // 실패했으므로 모달은 닫지 않고 에러 메시지 유지
  }
};

</script>

<template>
  <div class="board-detail-view">
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
          <h1 class="text-4xl font-extrabold text-gray-900 mb-4 break-words">
            {{ boardStore.currentPost.title }}
          </h1>
          <!-- ⭐️ 변경: flex-col을 사용하여 모바일에서 세로로 쌓고, md:flex-row로 가로 배치합니다. ⭐️ -->
          <div class="flex flex-col md:flex-row md:items-center text-sm text-gray-500 space-y-1 md:space-y-0">
          <span class="md:mr-4">
            작성자: <strong class="text-gray-700">{{ boardStore.currentPost.authorName || '익명' }}</strong>
          </span>
            <span class="md:mr-4">
            작성일: {{ formatDate(boardStore.currentPost.createdAt) }}
          </span>
            <span>
            조회수: {{ boardStore.currentPost.viewCount || 0 }}
          </span>
          </div>
        </header>

        <!-- 본문 내용 -->
        <section class="min-h-[150px] text-lg text-gray-700 leading-relaxed whitespace-pre-wrap mb-10">
          {{ boardStore.currentPost.content }}
        </section>

        <!-- ⭐️ 수정: 본문 아래, 액션 버튼 위에 좋아요 버튼 추가 ⭐️ -->
        <div class="flex justify-center mb-6 pb-2">
          <LikeButton
              :post-id="postId"
              @open-like-modal="showLikeModal = true"
          />
        </div>

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
                @click="handleEditPost"
                class="bg-yellow-500 hover:bg-yellow-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 shadow"
            >
              수정
            </button>
            <button
                @click="handleDeletePost"
                class="bg-red-500 hover:bg-red-600 text-white font-semibold py-2 px-4 rounded-lg transition duration-200 shadow"
            >
              삭제
            </button>
          </div>
        </footer>
      </div>

      <!-- ⭐️ 2. 댓글 섹션 통합 (CommentList 컴포넌트 추가) ⭐️ -->
      <div class="mt-10">
        <CommentList
            :post-id="postId"
            @open-delete-modal="handleOpenCommentDeleteModal"
            @comment-submitted="handleCommentToast"
        />
      </div>
    </div>

    <!-- 커스텀 모달 컴포넌트 연결 -->
    <ConfirmationModal
        :show="isPostDeleteModalOpen"
        :title="'게시글 삭제 확인'"
        :message="'정말로 이 게시글을 삭제하시겠습니까? 삭제된 게시글은 복구할 수 없습니다.'"
        :is-loading="boardStore.isLoading"
        :error="postDeleteError"
        @update:show="isPostDeleteModalOpen = $event"
        @confirm="confirmDeletePost"
    />

    <!-- 2. ⭐️ 댓글 삭제 커스텀 모달 컴포넌트 ⭐️ -->
    <ConfirmationModal
        :show="isCommentDeleteModalOpen"
        :title="'댓글 삭제 확인'"
        :message="'정말로 이 댓글을 삭제하시겠습니까? 삭제된 댓글은 복구할 수 없습니다.'"
        :is-loading="commentStore.isLoading"
        :error="commentDeleteError"
        @update:show="isCommentDeleteModalOpen = $event"
        @confirm="confirmDeleteComment"
    />

    <Toast
        :show="isToastVisible"
        :message="toastMessage"
        :type="toastType"
        @update:show="isToastVisible = $event"
    />
  </div>

  <ConfirmationModal
      :show="showLikeModal"
      @update:show="showLikeModal = $event"
      @confirm="handleLikeConfirm"

      title="경고: 로그인 필요"
      message="좋아요를 누르려면 로그인이 필요합니다."
      :is-loading="isProcessing"
      :error="likeError"
      confirm-button-text="로그인하기"
      confirm-button-class="bg-red-600 hover:bg-red-700"
  />
</template>