<template>
  <section class="card">
    <h2>Payment History Timeline</h2>

    <p v-if="!history.length" class="muted">No history records found.</p>

    <ol v-else class="timeline">
      <li v-for="(item, index) in history" :key="`${item.status}-${item.updatedAt}-${index}`" class="item">
        <span class="dot" />
        <div class="content">
          <StatusBadge :status="item.status" />
          <p class="time">{{ formatDateTime(item.updatedAt) }}</p>
        </div>
      </li>
    </ol>
  </section>
</template>

<script setup lang="ts">
import type { PaymentHistoryItem } from "../api/payments";
import { formatDateTime } from "../utils/format";
import StatusBadge from "./StatusBadge.vue";

defineProps<{
  history: PaymentHistoryItem[];
}>();
</script>

<style scoped>
h2 {
  margin-top: 0;
}

.timeline {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: grid;
  gap: 14px;
}

.item {
  display: grid;
  grid-template-columns: 20px 1fr;
  gap: 10px;
  align-items: start;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #1768ac;
  margin-top: 5px;
  box-shadow: 0 0 0 4px #dbeafe;
}

.content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #d8e2ef;
  border-radius: 10px;
  padding: 10px 12px;
}

.time {
  margin: 0;
  color: #5a6f88;
  font-size: 13px;
}

@media (max-width: 640px) {
  .content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>