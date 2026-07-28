<template>
  <section class="history-page">
    <div class="card page-shell">
      <div class="page-head">
        <div>
          <h1>Payment History</h1>
          <p class="muted">Review the complete lifecycle and status changes of a payment transaction.</p>
        </div>
      </div>

      <form class="search-bar" @submit.prevent="handleSearch">
        <div class="search-field">
          <label class="label" for="paymentId">Payment ID</label>
          <input
            id="paymentId"
            v-model.number="searchPaymentId"
            class="input"
            min="1"
            type="number"
            placeholder="Enter payment ID"
          />
        </div>

        <button class="btn btn-primary search-btn" type="submit">Search</button>
      </form>

      <div v-if="!isLoading && !store.errorMessage" class="summary-card">
        <div class="summary-item">
          <span class="summary-label">Payment ID</span>
          <strong>#{{ paymentId }}</strong>
        </div>

        <div class="summary-item">
          <span class="summary-label">Current Status</span>
          <StatusBadge :status="currentStatus" />
        </div>
      </div>

      <p v-if="isLoading" class="muted">Loading payment history...</p>
      <p v-else-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>
      <p v-else-if="!store.paymentHistory.length" class="muted">No lifecycle updates are available for this payment.</p>
    </div>

    <PaymentTimeline v-if="!isLoading && !store.errorMessage && store.paymentHistory.length" :history="store.paymentHistory" />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import PaymentTimeline from "../components/PaymentTimeline.vue";
import StatusBadge from "../components/StatusBadge.vue";
import { usePaymentStore } from "../stores/payment";

const route = useRoute();
const router = useRouter();
const store = usePaymentStore();

const paymentId = computed(() => Number(route.params.paymentId));
const searchPaymentId = ref<number | null>(null);
const isLoading = computed(() => store.status === "loading");
const currentStatus = computed(() => {
  const history = store.paymentHistory;
  return history.length ? history[history.length - 1].newStatus : "CREATED";
});

async function handleSearch(): Promise<void> {
  const value = searchPaymentId.value;
  if (!value || value <= 0) {
    return;
  }

  await router.push(`/payments/${value}/history`);
}

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

    const parsedPaymentId = Number(routePaymentId);
    searchPaymentId.value = Number.isInteger(parsedPaymentId) && parsedPaymentId > 0 ? parsedPaymentId : null;
    await loadHistory(parsedPaymentId);
  },
  { immediate: true }
);
</script>

<style scoped>
.history-page {
  display: grid;
  gap: 16px;
}

.page-shell {
  display: grid;
  gap: 24px;
}

.page-head h1,
.page-head p {
  margin: 0;
}

.page-head p {
  margin-top: 8px;
}

.search-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: end;
}

.search-field {
  min-width: 0;
}

.search-btn {
  min-width: 140px;
}

.summary-card {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 24px;
  padding: 0 0 8px;
}

.summary-item {
  display: grid;
  gap: 10px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(214, 226, 240, 0.68);
}

.summary-label {
  color: var(--muted);
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

@media (max-width: 768px) {
  .summary-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .search-bar {
    grid-template-columns: 1fr;
  }

  .search-btn {
    width: 100%;
  }
}
</style>