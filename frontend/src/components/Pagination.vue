<script setup>
import { defineProps, defineEmits } from 'vue';

// 1. Props 정의: 부모 컴포넌트(BoardListView)로부터 페이지 정보 수신
const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  }
});

// 2. Emits 정의: 페이지 변경 이벤트를 부모 컴포넌트로 전달
const emit = defineEmits(['changePage']);

// 3. 페이지 변경 시 호출될 함수
const emitChangePage = (page) => {
  if (page >= 1 && page <= props.totalPages && page !== props.currentPage) {
    emit('changePage', page);
  }
}

</script>

<template>
  <div v-if="totalPages > 1" class="flex justify-center mt-8">
    <nav class="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">

      <!-- 이전 버튼 -->
      <button
          @click="emitChangePage(currentPage - 1)"
          :disabled="currentPage === 1"
          class="relative inline-flex items-center px-4 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition duration-150">
        <span class="sr-only">Previous</span>
        이전
      </button>

      <!-- 페이지 번호 -->
      <button
          v-for="page in totalPages" :key="page"
          @click="emitChangePage(page)"
          :class="[
          'relative inline-flex items-center px-4 py-2 border text-sm font-medium transition duration-150',
          page === currentPage
            ? 'z-10 bg-indigo-600 border-indigo-500 text-white font-bold'
            : 'bg-white border-gray-300 text-gray-700 hover:bg-indigo-50/50'
        ]">
        {{ page }}
      </button>

      <!-- 다음 버튼 -->
      <button
          @click="emitChangePage(currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="relative inline-flex items-center px-4 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition duration-150">
        <span class="sr-only">Next</span>
        다음
      </button>
    </nav>
  </div>
</template>