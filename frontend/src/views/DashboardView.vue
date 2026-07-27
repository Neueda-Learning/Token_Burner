<template>
  <section class="dashboard">
    <div class="card hero">
      <h1>Payment Lifecycle Dashboard</h1>
      <p class="muted">
        Track lifecycle from CREATED to COMPLETED, and inspect FAILED scenarios for training and review.
      </p>
      <div class="lifecycle">
        <span class="created">CREATED</span>
        <span class="validated">VALIDATED</span>
        <span class="sent">SENT</span>
        <span class="completed">COMPLETED</span>
        <span class="failed">FAILED</span>
      </div>
    </div>

    <div class="grid">
      <form class="card" @submit.prevent="goPaymentDetail">
        <h2>Find Payment by ID</h2>
        <label class="label" for="paymentId">Payment ID</label>
        <input id="paymentId" v-model.number="paymentId" class="input" min="1" type="number" />
        <button class="btn btn-primary" type="submit">Open Payment</button>
        <p class="muted hint">Update Payment Status is available inside Payment Detail.</p>
      </form>

      <form class="card" @submit.prevent="goPaymentHistory">
        <h2>Get Payment History</h2>
        <label class="label" for="historyPaymentId">Payment ID</label>
        <input id="historyPaymentId" v-model.number="historyPaymentId" class="input" min="1" type="number" />
        <button class="btn btn-primary" type="submit">Open Payment History</button>
      </form>

      <form class="card" @submit.prevent="goUserPayments">
        <h2>Find Payments by User</h2>
        <label class="label" for="userId">User ID</label>
        <input id="userId" v-model.number="userId" class="input" min="1" type="number" />
        <button class="btn btn-primary" type="submit">Open User Payments</button>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const paymentId = ref<number | null>(null);
const historyPaymentId = ref<number | null>(null);
const userId = ref<number | null>(null);

function goPaymentDetail(): void {
  if (!paymentId.value || paymentId.value <= 0) {
    return;
  }

  router.push(`/payments/${paymentId.value}`);
}

function goUserPayments(): void {
  if (!userId.value || userId.value <= 0) {
    return;
  }

  router.push(`/users/${userId.value}/payments`);
}

function goPaymentHistory(): void {
  if (!historyPaymentId.value || historyPaymentId.value <= 0) {
    return;
  }

  router.push(`/payments/${historyPaymentId.value}/history`);
}
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 16px;
}

.hero h1 {
  margin: 0 0 8px;
}

.lifecycle {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.lifecycle span {
  border: 1px solid #b7c9dd;
  border-radius: 999px;
  padding: 6px 10px;
  background: #eef4fb;
  font-size: 13px;
  font-weight: 600;
}

.lifecycle .created {
  background: #dde7f2;
  border-color: #c6d8ea;
  color: #314a68;
}

.lifecycle .validated {
  background: #dbeafe;
  border-color: #bfdbfe;
  color: #1d4f8f;
}

.lifecycle .sent {
  background: #ffedd5;
  border-color: #fed7aa;
  color: #995f00;
}

.lifecycle .completed {
  background: #dcfce7;
  border-color: #bbf7d0;
  color: #166534;
}

.lifecycle .failed {
  background: #fee2e2;
  border-color: #fecaca;
  color: #991b1b;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

form {
  display: grid;
  gap: 10px;
}

h2 {
  margin: 0;
}

.hint {
  margin: 0;
  font-size: 12px;
}
</style>
