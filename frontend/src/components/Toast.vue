<script setup>
import { defineProps, defineEmits, watch, ref } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    required: true,
  },
  message: {
    type: String,
    required: true,
  },
  // 토스트 종류 (success, error, info 등)에 따라 스타일 변경 가능
  type: {
    type: String,
    default: 'success', // 'success', 'error', 'info'
  },
  duration: {
    type: Number,
    default: 3000, // 3초 후에 자동으로 닫힘
  }
});

const emit = defineEmits([ 'update:show' ]);

// 토스트 메시지의 CSS 클래스를 계산하는 함수
const toastClasses = ref('');

const calculateClasses = (type) => {
  switch (type) {
    case 'error':
      return 'bg-red-500 text-white shadow-xl';
    case 'info':
      return 'bg-blue-500 text-white shadow-xl';
    case 'success':
    default:
      return 'bg-green-500 text-white shadow-xl';
  }
};

watch (() => props.type, (newType) => {
  toastClasses.value = calculateClasses(newType);
}, { immediate: true });

// ⭐️ [핵심 로직] show 상태가 변경될 때마다 타이머를 설정/해제합니다. ⭐️
let timeoutId = null;

watch (() => props.show, (newShow) => {
  if (newShow) {
    // 이미 타이머가 있다면 초기화
    if (timeoutId) {
      clearTimeout(timeoutId);
    }

    // duration(3초) 후 자동으로 닫히도록 설정
    timeoutId = setTimeout(() => {
      emit('update:show', false);
    }, props.duration);
  } else {
    clearTimeout(timeoutId);
    timeoutId = null;
  }
});

const close = () => {
  emit('update:show', false);
};
</script>

<template>
  <Transition
      enter-active-class="transition ease-out duration-300 transform"
      enter-from-class="opacity-0 translate-y-full"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-200 transform"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-full"
  >
    <div
        v-if="show"
        :class="[
            'fixed bottom-5 right-5 p-4 rounded-lg flex items-center justify-between min-w-[250px] z-[110] transition-all duration-300',
            toastClasses
        ]"
    >
      <p class="font-medium mr-4">{{ message }}</p>

      <!-- 닫기 버튼 (선택 사항) -->
      <button
          @click="close"
          class="ml-3 p-1 rounded-full hover:bg-white hover:bg-opacity-20 transition"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  </Transition>
</template>