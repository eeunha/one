<script setup>
const props = defineProps({
  post: {
    type: Object,
    required: true,
  }
});

const truncate = (text, length) => {
  if (!text) return '';
  if (text.length <= length) {
    return text;
  }
  return text.substring(0, length) + "...";
}
</script>

<template>
  <router-link :to="{ name: 'BoardDetail', params: { id: post.id } }"
               class="block bg-white border border-gray-200 rounded-xl
                      shadow-md hover:shadow-lg transition-all duration-300
                      hover:-translate-y-1 overflow-hidden">

    <div class="p-4 flex flex-col h-full">
      <div class="border-b pb-3 mb-3 border-gray-100">
        <h3 class="text-lg font-semibold text-gray-900 leading-snug truncate">
          {{ truncate(post.title, 25) }}
        </h3>
      </div>

      <div class="flex-grow min-h-[60px]">
        <p class="text-sm text-gray-600 line-clamp-3">
          {{ truncate(post.content, 80) }}
        </p>
      </div>

      <div class="pt-3 border-t border-gray-100 mt-auto flex justify-between items-center text-xs text-gray-500">

        <div class="flex space-x-3">
          <span class="flex items-center">
            <font-awesome-icon :icon="['fas', 'heart']" class="mr-1 text-red-400" /> {{ post.likeCount }}
          </span>
          <span class="flex items-center">
            <font-awesome-icon :icon="['fas', 'comment']" class="mr-1 text-blue-400" /> {{ post.commentCount }}
          </span>
          <span class="flex items-center">
            <font-awesome-icon :icon="['fas', 'eye']" class="mr-1 text-emerald-400" /> {{ post.viewCount }}
          </span>
        </div>

        <span class="font-medium text-gray-700">
          By. {{ post.authorName }}
        </span>
      </div>
    </div>
  </router-link>
</template>

<style scoped>

</style>