<template>
  <section class="history-page">
    <div class="card">
      <h1>Payment History</h1>
      <p class="muted">Payment ID: {{ paymentId }}</p>
      <p v-if="isLoading" class="muted">Loading payment history...</p>
      <p v-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>
    </div>

    <PaymentTimeline v-if="!isLoading && !store.errorMessage" :history="store.paymentHistory" />
  </section>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { useRoute } from "vue-router";
import PaymentTimeline from "../components/PaymentTimeline.vue";
import { usePaymentStore } from "../stores/payment";

const route = useRoute();
const store = usePaymentStore();

const paymentId = computed(() => Number(route.params.paymentId));
const isLoading = computed(() => store.status === "loading");

async function loadHistory(currentPaymentId: number): Promise<void> {
  if (!Number.isInteger(currentPaymentId) || currentPaymentId <= 0) {
    store.paymentHistory = [];
    store.status = "error";
    store.errorMessage = "Invalid payment ID.";
    return;
  }

  try {
    await store.fetchPaymentHistory(currentPaymentId);
  } catch {
    // Error state is shown from store.
  }
}

watch(
  () => [route.name, route.params.paymentId] as const,
  async ([routeName, routePaymentId]) => {
    if (routeName !== "payment-history") {
      return;
    }

    await loadHistory(Number(routePaymentId));
  },
  { immediate: true }
);
</script>

<style scoped>
.history-page {
  display: grid;
  gap: 16px;
}

h1 {
  margin: 0;
}
</style>