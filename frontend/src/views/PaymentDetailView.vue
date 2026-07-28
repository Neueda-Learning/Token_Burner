<template>
  <section class="detail-page">
    <div class="card detail-card">
      <div class="detail-head">
        <div>
          <h1>Payment Detail</h1>
          <p class="muted">Review key information for this payment and navigate to its full lifecycle history.</p>
        </div>

        <StatusBadge v-if="store.payment" :status="store.payment.status" />
      </div>

      <p v-if="isLoading" class="muted">Loading payment...</p>
      <p v-else-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <div v-else-if="store.payment" class="detail-content">
        <dl class="detail-grid">
          <div class="detail-item">
            <dt>Payment ID</dt>
            <dd>#{{ store.payment.paymentId }}</dd>
          </div>
          <div class="detail-item">
            <dt>Status</dt>
            <dd>{{ store.payment.status }}</dd>
          </div>
          <div class="detail-item">
            <dt>Source Account</dt>
            <dd>{{ store.payment.sourceAccountId }}</dd>
          </div>
          <div class="detail-item">
            <dt>Destination Account</dt>
            <dd>{{ store.payment.destinationAccountId }}</dd>
          </div>
          <div class="detail-item">
            <dt>Amount</dt>
            <dd>{{ formatAmount(store.payment.amount, store.payment.currency) }}</dd>
          </div>
          <div class="detail-item">
            <dt>Currency</dt>
            <dd>{{ store.payment.currency }}</dd>
          </div>
          <div class="detail-item">
            <dt>Created At</dt>
            <dd>{{ formatDateTime(store.payment.createdAt) }}</dd>
          </div>
          <div class="detail-item">
            <dt>Updated At</dt>
            <dd>{{ formatDateTime(store.payment.updatedAt) }}</dd>
          </div>
        </dl>

        <div class="actions">
          <RouterLink :to="`/payments/${paymentId}/history`" class="btn btn-secondary">History</RouterLink>
          <RouterLink :to="{ name: 'payment-search' }" class="btn btn-primary">Back</RouterLink>
        </div>
      </div>

      <p v-else class="muted">No payment data found.</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import StatusBadge from "../components/StatusBadge.vue";
import { usePaymentStore } from "../stores/payment";
import { formatAmount, formatDateTime } from "../utils/format";

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

.detail-card {
  display: grid;
  gap: 24px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(214, 226, 240, 0.84);
}

.detail-head h1,
.detail-head p {
  margin: 0;
}

.detail-head p {
  margin-top: 8px;
}

.detail-content {
  display: grid;
  gap: 22px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 22px;
  margin: 0;
}

.detail-item {
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(214, 226, 240, 0.68);
}

.detail-item dt {
  margin: 0 0 8px;
  color: var(--muted);
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.detail-item dd {
  margin: 0;
  color: var(--text);
  font-size: 1rem;
  font-weight: 600;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 6px;
}

.actions .btn {
  text-decoration: none;
}

@media (max-width: 768px) {
  .detail-head {
    flex-direction: column;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>