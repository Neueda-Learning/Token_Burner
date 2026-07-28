<template>
  <section class="card">
    <h2>Payment History Timeline</h2>

    <p v-if="!history.length" class="muted">No history records found.</p>

    <ol v-else class="timeline">
      <li v-for="(item, index) in history" :key="`${item.historyId}-${index}`" class="item">
        <span class="dot" />
        <div class="content">
          <div class="status-block">
            <StatusBadge :status="item.newStatus" />
            <p v-if="item.previousStatus" class="transition muted">{{ item.previousStatus }} -> {{ item.newStatus }}</p>
          </div>
          <div class="meta">
            <p class="time">{{ formatDateTime(item.changedAt) }}</p>
            <p v-if="item.notes" class="notes">{{ item.notes }}</p>
          </div>
        </div>
      </li>
    </ol>
  </section>
</template>

<script setup lang="ts">
import type { PaymentHistoryResponse } from "../api/payments";
import { formatDateTime } from "../utils/format";
import StatusBadge from "./StatusBadge.vue";

defineProps<{
  history: PaymentHistoryResponse[];
}>();
</script>

<style scoped>
h2 {
  margin-top: 0;
  margin-bottom: 4px;
}

.timeline {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
  display: grid;
  gap: 18px;
}

.item {
  display: grid;
  grid-template-columns: 24px 1fr;
  gap: 12px;
  align-items: start;
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1768ac 0%, #3b82f6 100%);
  margin-top: 8px;
  box-shadow: 0 0 0 6px rgba(59, 130, 246, 0.12);
}

.content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid rgba(214, 226, 240, 0.82);
  border-radius: 16px;
  padding: 14px 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 8px 20px rgba(18, 55, 99, 0.05);
}

.status-block {
  display: grid;
  gap: 4px;
}

.transition {
  margin: 0;
  font-size: 12px;
}

.meta {
  display: grid;
  justify-items: end;
  gap: 4px;
}

.time {
  margin: 0;
  color: #5a6f88;
  font-size: 13px;
}

.notes {
  margin: 0;
  color: #374151;
  font-size: 12px;
  text-align: right;
}

@media (max-width: 640px) {
  .content {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .meta {
    justify-items: start;
  }
}
</style>