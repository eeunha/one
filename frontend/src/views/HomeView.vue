<script setup>
</script>

<template>
  <div class="space-y-8">
    <h1 class="text-4xl font-bold text-gray-800">환영합니다! 게시판 홈</h1>

    <section class="bg-white p-6 rounded-xl shadow-lg">
      <h2 class="text-2xl font-semibold mb-4 text-indigo-600">최신 공지사항</h2>
      <p class="text-gray-700">
        여기는 중앙 정렬된 메인 콘텐츠 영역입니다. 이 텍스트 블록은
        최상위 App.vue 파일에 의해 너비가 제한되고 중앙에 정확히 배치되어야 합니다.
        좌우 여백이 동일한지 확인해 보세요.
      </p>
    </section>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="bg-indigo-100 p-4 rounded-lg">게시물 요약 1</div>
      <div class="bg-indigo-100 p-4 rounded-lg">게시물 요약 2</div>
      <div class="bg-indigo-100 p-4 rounded-lg">게시물 요약 3</div>
    </div>

  </div>
</template>
