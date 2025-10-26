<script setup>
const emit = defineEmits(['open-withdrawal-modal']);

const openWithdrawalModal = () => {
  // 부모 컴포넌트(Profile.vue)에게 모달을 열어달라고 요청
  emit('open-withdrawal-modal');
};

</script>

<template>
  <div>
    <button
      @click="openWithdrawalModal"
      class="mt-4 w-full py-2 px-4 bg-gray-400 text-white font-semibold rounded-lg shadow-md hover:bg-gray-500 focus:outline-none focus:ring-2 focus:ring-gray-300 transition duration-300"
    >
      회원 탈퇴
    </button>
  </div>
</template>

<style scoped>

</style>