<template>
  <section class="detail-page">
    <div class="card">
      <h1>Payment Detail</h1>

      <p v-if="isLoading" class="muted">Loading payment...</p>
      <p v-else-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <PaymentCard v-else-if="store.payment" :payment="store.payment" />

      <p v-else class="muted">No payment data found.</p>
    </div>

    <div v-if="store.payment" class="card update-card">
      <h2>Update Payment Status</h2>

      <p class="muted">Current Status: <StatusBadge :status="store.payment.status" /></p>

      <label class="label" for="nextStatus">Target Status</label>
      <select id="nextStatus" v-model="nextStatus" class="select">
        <option value="">Select next status</option>
        <option v-for="status in allowedNextStatuses" :key="status" :value="status">
          {{ status }}
        </option>
      </select>

      <p v-if="transitionError" class="error-text">{{ transitionError }}</p>
      <p v-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <div class="actions">
        <button class="btn btn-primary" :disabled="isLoading" @click="handleUpdateStatus">
          {{ isLoading ? "Updating..." : "Update Status" }}
        </button>
        <RouterLink :to="`/payments/${paymentId}/history`" class="btn btn-secondary">View History</RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import PaymentCard from "../components/PaymentCard.vue";
import StatusBadge from "../components/StatusBadge.vue";
import type { PaymentStatus } from "../api/payments";
import { usePaymentStore } from "../stores/payment";
import { getAllowedNextStatuses, isTransitionAllowed } from "../utils/status";

const route = useRoute();
const store = usePaymentStore();

const nextStatus = ref<PaymentStatus | "">("");
const transitionError = ref("");

const paymentId = computed(() => Number(route.params.paymentId));
const isLoading = computed(() => store.status === "loading");

const allowedNextStatuses = computed<PaymentStatus[]>(() => {
  if (!store.payment) {
    return [];
  }

  return getAllowedNextStatuses(store.payment.status);
});

async function loadPayment(): Promise<void> {
  if (!Number.isInteger(paymentId.value) || paymentId.value <= 0) {
    store.errorMessage = "Invalid payment ID.";
    return;
  }

  try {
    await store.fetchPayment(paymentId.value);
  } catch {
    // Error text is already stored.
  }
}

async function handleUpdateStatus(): Promise<void> {
  transitionError.value = "";

  if (!store.payment) {
    transitionError.value = "No payment loaded.";
    return;
  }

  if (!nextStatus.value) {
    transitionError.value = "Please select a target status.";
    return;
  }

  if (!isTransitionAllowed(store.payment.status, nextStatus.value)) {
    transitionError.value = "当前状态不允许流转到目标状态";
    return;
  }

  try {
    await store.changePaymentStatus(paymentId.value, nextStatus.value);
    nextStatus.value = "";
  } catch {
    // Store keeps backend error details such as INVALID_STATUS_TRANSITION.
  }
}

watch(
  () => route.params.paymentId,
  async () => {
    await loadPayment();
  },
  { immediate: true }
);
</script>

<style scoped>
.detail-page {
  display: grid;
  gap: 16px;
}

h1,
h2 {
  margin-top: 0;
}

.update-card {
  display: grid;
  gap: 10px;
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>