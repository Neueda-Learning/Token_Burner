<template>
  <section class="search-landing-page">
    <div class="search-content">
      <p class="search-eyebrow">TransiPay Search</p>
      <h1>Customer Payments</h1>
      <p class="search-description muted">View all payment activity associated with an account.</p>

      <form class="search-bar" @submit.prevent="goUserPayments">
        <div class="search-field">
          <label class="label" for="userId">User ID</label>
          <input
            id="userId"
            v-model.number="userId"
            class="input"
            min="1"
            type="number"
            placeholder="Enter user ID"
          />
        </div>

        <button class="btn btn-primary search-btn" type="submit" :disabled="isLoading">
          {{ isLoading ? "Searching..." : "Search" }}
        </button>
      </form>

      <p v-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <p class="supporting-copy muted">
        Manage payment activity with a clear view of incoming and outgoing transactions.
      </p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { usePaymentStore } from "../stores/payment";

const router = useRouter();
const store = usePaymentStore();
const userId = ref<number | null>(null);
const isLoading = computed(() => store.status === "loading");

onMounted(() => {
  store.clearError();
});

async function goUserPayments(): Promise<void> {
  if (!userId.value || userId.value <= 0) {
    return;
  }

  const targetUserId = userId.value;

  store.clearError();
  store.clearUserPayments();

  try {
    await store.fetchPaymentsByUser(targetUserId);
    await router.push(`/users/${targetUserId}/payments`);
  } catch {
    // Error state stays on the search page; do not navigate using stale data.
  }
}
</script>

<style scoped>
.search-landing-page {
  display: grid;
  min-height: calc(100vh - 260px);
  align-items: center;
}

.search-content {
  display: grid;
  gap: 18px;
  width: min(760px, 100%);
  margin: 0 auto;
  text-align: center;
}

.search-eyebrow,
h1 {
  margin: 0;
}

.search-eyebrow {
  color: var(--brand);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.82rem;
  font-weight: 700;
}

.search-description,
.supporting-copy {
  margin: 0;
}

.search-description {
  max-width: 620px;
  margin: 0 auto;
}

.search-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: end;
  text-align: left;
}

.search-field {
  min-width: 0;
}

.search-btn {
  min-width: 148px;
}

.supporting-copy {
  font-size: 0.96rem;
}

@media (max-width: 768px) {
  .search-landing-page {
    min-height: auto;
  }
}

@media (max-width: 640px) {
  .search-bar {
    grid-template-columns: 1fr;
  }

  .search-btn {
    min-width: 0;
    justify-self: start;
  }
}
</style>

