<template>
  <section class="user-page">
    <div class="card search-card">
      <h1>User Payments</h1>
      <form class="search-form" @submit.prevent="handleSearch">
        <div>
          <label class="label" for="userId">User ID</label>
          <input id="userId" v-model.number="searchUserId" class="input" min="1" type="number" />
        </div>
        <button class="btn btn-primary" type="submit">Search</button>
      </form>

      <p class="muted">Current User ID: {{ userId }}</p>
      <p v-if="isLoading" class="muted">Loading user payments...</p>
      <p v-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>
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

h1 {
  margin: 0;
}

.search-card {
  display: grid;
  gap: 10px;
}

.search-form {
  display: flex;
  gap: 10px;
  align-items: end;
}

.search-form > div {
  min-width: 220px;
}

.list {
  display: grid;
  gap: 12px;
}

@media (max-width: 640px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form > div {
    min-width: 0;
  }
}
</style>