<template>
  <article class="card payment-card">
    <div class="card-head">
      <h3>Payment #{{ payment.paymentId }}</h3>
      <StatusBadge :status="payment.status" />
    </div>

    <div class="grid">
      <div class="field">
        <span class="field-label">Source Account</span>
        <p>{{ payment.sourceAccountId }}</p>
      </div>
      <div class="field">
        <span class="field-label">Destination Account</span>
        <p>{{ payment.destinationAccountId }}</p>
      </div>
      <div class="field">
        <span class="field-label">Amount</span>
        <p>{{ formattedAmount }}</p>
      </div>
      <div class="field">
        <span class="field-label">Currency</span>
        <p>{{ payment.currency }}</p>
      </div>
      <div class="field time-row">
        <div class="time-item">
          <span class="field-label">Created At</span>
          <p>{{ formatDateTime(payment.createdAt) }}</p>
        </div>
        <div class="time-item">
          <span class="field-label">Updated At</span>
          <p>{{ formatDateTime(payment.updatedAt) }}</p>
        </div>
      </div>
    </div>

    <div v-if="showActions" class="actions">
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
  showActions?: boolean;
}>();

const formattedAmount = computed(() => formatAmount(props.payment.amount, props.payment.currency));
const showActions = computed(() => props.showActions ?? true);
</script>

<style scoped>
.payment-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(214, 226, 240, 0.8);
}

.card-head h3 {
  margin: 0;
  font-size: 1.15rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px 22px;
}

.field {
  display: grid;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(214, 226, 240, 0.68);
}

.field p {
  margin: 0;
  color: var(--text);
  font-weight: 600;
}

.field-label {
  color: var(--muted);
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.time-row {
  grid-column: 1 / -1;
  display: flex;
  align-items: start;
  gap: 22px;
  flex-wrap: wrap;
}

.time-item {
  display: grid;
  gap: 8px;
  min-width: 220px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
  padding-top: 4px;
}

.actions .btn {
  text-decoration: none;
}
</style>