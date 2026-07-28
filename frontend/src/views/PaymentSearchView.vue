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

        <button class="btn btn-primary search-btn" type="submit">Search</button>
      </form>

      <p class="supporting-copy muted">Every transaction has a story. Track your payment journey with confidence.</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const paymentId = ref<number | null>(null);

async function goPaymentDetail(): Promise<void> {
  if (!paymentId.value || paymentId.value <= 0) {
    return;
  }

  await router.push(`/payments/${paymentId.value}`);
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

