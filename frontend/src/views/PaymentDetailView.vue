<template>
  <section class="detail-page">
    <div class="card">
      <h1>Payment Detail</h1>

      <p v-if="isLoading" class="muted">Loading payment...</p>
      <p v-else-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <PaymentCard v-else-if="store.payment" :payment="store.payment" />

      <p v-else class="muted">No payment data found.</p>

      <div v-if="store.payment" class="actions">
        <RouterLink :to="`/payments/${paymentId}/history`" class="btn btn-secondary">View History</RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import PaymentCard from "../components/PaymentCard.vue";
import { usePaymentStore } from "../stores/payment";

const route = useRoute();
const store = usePaymentStore();

const paymentId = computed(() => Number(route.params.paymentId));
const isLoading = computed(() => store.status === "loading");

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

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>