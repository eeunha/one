<script setup>
import {onMounted} from 'vue';
import {useBoardStore} from '@/stores/useBoardStore.js';
import {storeToRefs} from 'pinia';
import TopPostItem from '@/components/TopPostItem.vue';

// Store 가져오기
const boardStore = useBoardStore();

// 상태 추출
const {topPosts, isLoading} = storeToRefs(boardStore);

onMounted(() => {
  if (!topPosts.value.length) {
    boardStore.fetchTop8Posts();
  }
});
</script>

<template>
  <div class="mt-8">
    <h2 class="text-2xl font-bold text-gray-800 mb-4 border-b-2 border-gray-500 pb-2">
      🔥 인기 게시글
    </h2>

    <div v-if="isLoading" class="text-center text-gray-500 py-10">
      게시글을 불러오는 중입니다...
    </div>

    <div v-else-if="topPosts.length"
         class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">

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