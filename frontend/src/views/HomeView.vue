<script setup>
import TopPostList from '@/components/top/TopPostList.vue';
</script>

<template>
  <div class="space-y-12">
    <section class="text-center py-16 px-6 bg-emerald-50/50 rounded-2xl shadow-xl border border-emerald-100">
      <h1 class="text-5xl font-extrabold text-emerald-800 mb-4 animate-fade-in">
        성장을 위한 나의 루틴 노트, Routinote
      </h1>
      <p class="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
        당신의 요리, 운동, 학습, 재테크 등 모든 갓생 활동을 루티노트에 자유롭게 기록하고, 응원을 받으며 꾸준함을 유지하세요.
      </p>

      <RouterLink
          to="/write"
          class="inline-flex items-center justify-center
           px-8 py-3   /* 여백 및 크기 증가 */
           border border-transparent text-xl font-semibold /* 글자 크기 증가 */
           rounded-full shadow-2xl /* 그림자 강화 */
           text-white
           bg-emerald-600 hover:bg-emerald-800 /* 배경색 진하게 */
           transition duration-300 transform hover:scale-105"
      >
        <svg class="w-7 h-7 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
        지금 갓생 기록 시작하기
      </RouterLink>
    </section>

    <section>
<!--      <h2 class="text-3xl font-bold text-gray-800 mb-6 border-b-2 pb-2">🔥 실시간 인기 루틴 Top 5</h2>-->
      <TopPostList />
    </section>

  </div>
</template>
