<script setup>
import {onMounted} from 'vue';
import {useBoardStore} from '@/stores/useBoardStore.js';
import {storeToRefs} from 'pinia';
import TopPostItem from '@/components/top/TopPostItem.vue';
import router from '@/router/index.js';

// Store 가져오기
const boardStore = useBoardStore();

// 상태 추출
const {topPosts, isLoading} = storeToRefs(boardStore);

onMounted(() => {
  if (!topPosts.value.length) {
    boardStore.fetchTop4Posts();
  }
});

const goToBoard = () => {
  router.push({ name: 'BoardList' });
}
</script>

<template>
  <div class="mt-8">
    <!-- 제목 및 더보기 버튼 섹션: Flexbox로 양쪽 정렬 -->
    <div class="flex items-center justify-between mb-4 border-b-2 border-gray-500 pb-2">
      <!-- 제목 -->
      <h2 class="text-3xl font-bold text-gray-800 pb-2">🔥 베스트 갓생 Top 4</h2>

      <!-- 더보기 링크 -->
      <a
          @click.prevent="goToBoard"
          href="/board"
          class="text-sm text-emerald-600 hover:text-emerald-800 transition duration-150 ease-in-out
               font-medium cursor-pointer"
      >
        더보기 &rarr;
      </a>
    </div>

    <div v-if="isLoading" class="text-center text-gray-500 py-10">
      게시글을 불러오는 중입니다...
    </div>

    <div v-else-if="topPosts.length"
         class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-6">

      <TopPostItem
          v-for="post in topPosts"
          :key="post.id"
          :post="post"
      />
    </div>

    <div v-else class="text-center text-gray-500 py-10 border-2 border-dashed border-gray-300 rounded-lg">
      아직 인기 게시글이 없습니다.
    </div>
  </div>
</template>