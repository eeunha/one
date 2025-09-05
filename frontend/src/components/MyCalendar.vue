<script setup>
import { ref } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'

// 달력 옵션 정의
const calendarOptions = ref({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth', // 월별 달력
  locale: 'ko', // 한국어 표시
  events: [
    { title: '회의', date: '2025-09-05' },
    { title: '개발 마감', date: '2025-09-10' },
    { title: '팀 회식', date: '2025-09-15' },
  ],
})
</script>

<template>
  <div>
    <h2 class="text-xl font-bold mb-4">📅 내 달력</h2>
    <FullCalendar :options="calendarOptions" />
  </div>
</template>

<style scoped>
</style>