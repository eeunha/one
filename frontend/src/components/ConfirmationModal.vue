<script setup>
import {defineProps, defineEmits, watch, onUnmounted} from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    required: true,
  },
  title: {
    type: String,
    default: '작업 확인',
  },
  message: {
    type: String,
    default: '이 작업을 실행하시겠습니까?',
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
  confirmButtonText: {
    type: String,
    default: '확인',
  },
  cancelButtonText: {
    type: String,
    default: '취소',
  },
  confirmButtonClass: {
    type: String,
    default: 'bg-red-600 hover:bg-red-700', // 기본 삭제 버튼 스타일
  }
});

const emit = defineEmits([ 'update:show', 'confirm' ]);

// 취소 버튼이나 모달 외부 클릭 시 모달 닫기
const close = () => {
  if (!props.isLoading) { // 처리 중이 아닐 때만 닫을 수 있도록 제어
    emit('update:show', false);
  }
};

const handleConfirm = () => {
  if (!props.isLoading) { // 처리 중이 아닐 때만 호출
    emit('confirm');
  }
  // confirm 로직은 부모 컴포넌트(BoardDetailView)가 처리하므로, 여기서는 닫지 않습니다.
  // 부모 컴포넌트에서 삭제 성공 시 페이지를 이동하면서 모달이 자연스럽게 사라집니다.
  // 실패 시에는 모달을 유지하고 에러를 표시합니다.
};

// ⭐️ [추가] ESC 키로 모달 닫기 핸들러 ⭐️
const handleKeydown = (e) => {
  if (props.show && e.key === 'Escape') {
    close();
  }
};

watch( () => props.show, (newShow) => {
  if (newShow) {
    document.addEventListener('keydown', handleKeydown);
  } else {
    document.removeEventListener('keydown', handleKeydown);
  }
}, { immediate: true });

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown);
});

</script>

<template>
  <!-- 모달 오버레이 -->
  <div
      v-if="show"
      class="fixed inset-0 backdrop-brightness-40 flex items-center justify-center z-50 transition-opacity"
      @click.self="close"
  >
    <!-- 모달 컨테이너 -->
    <div class="bg-white p-6 rounded-xl shadow-2xl w-full max-w-md transform transition-all duration-300 scale-100 opacity-100">

      <!-- 제목 -->
      <h2 class="text-xl font-bold mb-4 text-gray-900">{{ title }}</h2>

      <!-- 메시지 -->
      <p class="mb-6 text-gray-700 break-words">{{ message }}</p>

      <!-- 에러 메시지 -->
      <p v-if="error" class="text-red-500 text-sm mb-4 font-medium bg-red-50 p-2 rounded border border-red-200">
        {{ error }}
      </p>

      <!-- 액션 버튼 -->
      <div class="flex justify-center space-x-4">
        <!-- 취소 버튼 -->
        <button
            v-if="cancelButtonText && cancelButtonText.trim() !== ''" @click="close"
            :disabled="isLoading"
            class="bg-gray-300 hover:bg-gray-400 text-gray-800 font-semibold py-2 px-4 rounded-lg transition duration-200 disabled:opacity-50"
        >
          {{ cancelButtonText }}
        </button>

        <!-- 확인 버튼 -->
        <button
            @click="handleConfirm"
            :disabled="isLoading"
            :class="[
            'text-white font-semibold py-2 px-4 rounded-lg transition duration-200 shadow-md disabled:opacity-50',
            confirmButtonClass
          ]"
        >
          {{ isLoading ? '처리 중...' : confirmButtonText }}
        </button>
      </div>
    </div>
  </div>
</template>