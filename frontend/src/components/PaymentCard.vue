<template>
  <article class="card payment-card">
    <div class="card-head">
      <h3>Payment #{{ payment.paymentId }}</h3>
      <StatusBadge :status="payment.status" />
    </div>

    <div class="grid">
      <p><span class="muted">Source Account:</span> {{ payment.sourceAccountId }}</p>
      <p><span class="muted">Destination Account:</span> {{ payment.destinationAccountId }}</p>
      <p><span class="muted">Amount:</span> {{ formattedAmount }}</p>
      <p><span class="muted">Currency:</span> {{ payment.currency }}</p>
      <p><span class="muted">Created:</span> {{ formatDateTime(payment.createdAt) }}</p>
      <p><span class="muted">Updated:</span> {{ formatDateTime(payment.updatedAt) }}</p>
    </div>

    <div class="actions">
      <RouterLink :to="`/payments/${payment.paymentId}`" class="btn btn-secondary">Details</RouterLink>
      <RouterLink :to="`/payments/${payment.paymentId}/history`" class="btn btn-secondary">History</RouterLink>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";
import type { Payment } from "../api/payments";
import StatusBadge from "./StatusBadge.vue";
import { formatAmount, formatDateTime } from "../utils/format";

const props = defineProps<{
  payment: Payment;
}>();

const formattedAmount = computed(() => formatAmount(props.payment.amount, props.payment.currency));
</script>

<style scoped>
.payment-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-head h3 {
  margin: 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 8px 16px;
}

.grid p {
  margin: 0;
}

.actions {
  display: flex;
  gap: 10px;
}
</style>