<template>
  <section class="user-page">
    <div class="card page-shell">
      <div class="page-head">
        <div>
          <h1>User Payments</h1>
          <p class="muted">View all payment activities associated with a customer account.</p>
        </div>
      </div>

      <form class="search-bar" @submit.prevent="handleSearch">
        <div class="search-field">
          <label class="label" for="userId">User ID</label>
          <input
            id="userId"
            v-model.number="searchUserId"
            class="input"
            min="1"
            type="number"
            placeholder="Enter user ID"
          />
        </div>

        <button class="btn btn-primary search-btn" type="submit">Search</button>
      </form>

      <p v-if="isLoading" class="muted">Loading user payments...</p>
      <p v-else-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <div v-else class="summary-grid">
        <div class="summary-item">
          <span class="summary-label">User ID</span>
          <strong>{{ userId }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">Total Payments</span>
          <strong>{{ totalPayments }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">Completed Payments</span>
          <strong>{{ completedPayments }}</strong>
        </div>
        <div class="summary-item">
          <span class="summary-label">Failed Payments</span>
          <strong>{{ failedPayments }}</strong>
        </div>
      </div>
    </div>

    <p v-if="!isLoading && !store.errorMessage && !store.userPayments.length" class="card muted">
      No payments found for this user.
    </p>

    <div class="list">
      <PaymentCard v-for="payment in store.userPayments" :key="payment.paymentId" :payment="payment" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import PaymentCard from "../components/PaymentCard.vue";
import { usePaymentStore } from "../stores/payment";

const route = useRoute();
const router = useRouter();
const store = usePaymentStore();

const userId = computed(() => Number(route.params.userId));
const searchUserId = ref<number | null>(null);
const isLoading = computed(() => store.status === "loading");
const totalPayments = computed(() => store.userPayments.length);
const completedPayments = computed(() => store.userPayments.filter((payment) => payment.status === "COMPLETED").length);
const failedPayments = computed(() => store.userPayments.filter((payment) => payment.status === "FAILED").length);

async function loadUserPayments(currentUserId: number): Promise<void> {
  if (!Number.isInteger(currentUserId) || currentUserId <= 0) {
    store.userPayments = [];
    store.status = "error";
    store.errorMessage = "Invalid user ID.";
    return;
  }

  searchUserId.value = currentUserId;

  try {
    await store.fetchPaymentsByUser(currentUserId);
  } catch {
    // Error state is shown from store.
  }
}

async function handleSearch(): Promise<void> {
  if (!searchUserId.value || searchUserId.value <= 0) {
    return;
  }

  await router.push(`/users/${searchUserId.value}/payments`);
}

watch(
  () => [route.name, route.params.userId] as const,
  async ([routeName, routeUserId]) => {
    if (routeName !== "user-payments") {
      return;
    }

    await loadUserPayments(Number(routeUserId));
  },
  { immediate: true }
);
</script>

<style scoped>
.user-page {
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

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px 20px;
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

.list {
  display: grid;
  gap: 16px;
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .search-bar {
    grid-template-columns: 1fr;
  }

  .search-btn {
    width: 100%;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>