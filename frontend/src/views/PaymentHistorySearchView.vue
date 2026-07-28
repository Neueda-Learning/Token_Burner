<template>
  <section class="search-landing-page">
    <div class="search-content">
      <p class="search-eyebrow">TransiPay Search</p>
      <h1>Payment History</h1>
      <p class="search-description muted">Review every stage of your payment lifecycle.</p>

      <form class="search-bar" @submit.prevent="goPaymentHistory">
        <div class="search-field">
          <label class="label" for="historyPaymentId">Payment ID</label>
          <input
            id="historyPaymentId"
            v-model.number="paymentId"
            class="input"
            min="1"
            type="number"
            placeholder="Enter payment ID"
          />
        </div>

        <button class="btn btn-primary search-btn" type="submit">Search</button>
      </form>

      <p class="supporting-copy muted">From creation to completion, every status change is recorded for transparency.</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const paymentId = ref<number | null>(null);

async function goPaymentHistory(): Promise<void> {
  if (!paymentId.value || paymentId.value <= 0) {
    return;
  }

  await router.push(`/payments/${paymentId.value}/history`);
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

.search-description,
.supporting-copy {
  margin: 0;
}

.search-description {
  max-width: 620px;
  margin: 0 auto;
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

