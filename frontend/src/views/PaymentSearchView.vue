<template>
  <section class="search-landing-page">
    <div class="search-content">
      <p class="search-eyebrow">TransiPay Search</p>
      <h1>Search Payment</h1>
      <p class="search-description muted">Quickly find a payment transaction and view its details.</p>

      <form class="search-bar" @submit.prevent="goPaymentDetail">
        <div class="search-field">
          <label class="label" for="paymentId">Payment ID</label>
          <input
            id="paymentId"
            v-model.number="paymentId"
            class="input"
            min="1"
            type="number"
            placeholder="Enter payment ID"
          />
        </div>

        <button class="btn btn-primary search-btn" type="submit" :disabled="isLoading">
          {{ isLoading ? "Searching..." : "Search" }}
        </button>
      </form>

      <p v-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

      <p class="supporting-copy muted">Every transaction has a story. Track your payment journey with confidence.</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { usePaymentStore } from "../stores/payment";

const router = useRouter();
const store = usePaymentStore();
const paymentId = ref<number | null>(null);
const isLoading = computed(() => store.status === "loading");

onMounted(() => {
  store.clearError();
});

async function goPaymentDetail(): Promise<void> {
  if (!paymentId.value || paymentId.value <= 0) {
    return;
  }

  const targetPaymentId = paymentId.value;

  store.clearError();
  store.clearPayment();

  try {
    await store.fetchPayment(targetPaymentId);
    await router.push(`/payments/${targetPaymentId}`);
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

