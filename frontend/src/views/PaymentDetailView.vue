<template>
  <section class="detail-page">
    <div class="card">
      <h1>Payment Detail</h1>

      <p v-if="isLoading" class="muted">Loading payment...</p>
      <p v-else-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <PaymentCard v-else-if="store.payment" :payment="store.payment" :show-actions="false" />

      <p v-else class="muted">No payment data found.</p>

      <div v-if="store.payment" class="actions">
        <RouterLink :to="`/payments/${paymentId}/history`" class="btn btn-secondary">History</RouterLink>
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

async function loadPayment(currentPaymentId: number): Promise<void> {
  if (!Number.isInteger(currentPaymentId) || currentPaymentId <= 0) {
    store.payment = null;
    store.status = "error";
    store.errorMessage = "Invalid payment ID.";
    return;
  }

  try {
    await store.fetchPayment(currentPaymentId);
  } catch {
    // Error text is already stored.
  }
}

watch(
  () => [route.name, route.params.paymentId] as const,
  async ([routeName, routePaymentId]) => {
    if (routeName !== "payment-detail") {
      return;
    }

    await loadPayment(Number(routePaymentId));
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