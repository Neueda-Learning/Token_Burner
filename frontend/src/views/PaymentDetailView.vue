<template>
  <section class="detail-page">
    <div class="card detail-card">
      <div class="detail-head">
        <div>
          <h1>Payment Detail</h1>
          <p class="muted">Review key information for this payment and navigate to its full lifecycle history.</p>
        </div>
      </div>

      <p v-if="isLoading" class="muted">Loading payment...</p>
      <p v-else-if="!store.payment" class="muted">No payment data found.</p>

      <div v-else class="detail-content">
        <section class="status-hero" aria-labelledby="payment-status-heading">
          <div class="status-hero-copy">
            <p class="status-hero-label">Payment #{{ store.payment.paymentId }}</p>
            <h2 id="payment-status-heading">Current Status</h2>
          </div>
          <div class="status-hero-badge">
            <StatusBadge :status="store.payment.status" size="lg" />
          </div>
        </section>

        <p v-if="store.errorMessage" class="error-text">{{ store.errorMessage }}</p>

        <section v-if="showProgressPanel" class="progress-panel" aria-live="polite" aria-atomic="true">
          <div class="progress-panel-head">
            <div>
              <p class="progress-eyebrow">Processing Flow</p>
              <h3>{{ progressMessage }}</h3>
            </div>
            <div class="progress-stage-badge">
              <StatusBadge :status="currentProgressStatus" size="md" />
            </div>
          </div>

          <div class="progress-bar-track" role="progressbar" :aria-valuenow="progressPercent" aria-valuemin="0" aria-valuemax="100">
            <div class="progress-bar-fill" :style="{ width: `${progressPercent}%` }" />
          </div>

          <div class="progress-meta">
            <p class="progress-stage-text">Current lifecycle stage: {{ currentProgressStatus }}</p>
            <p class="progress-note muted">SENT means the payment has been sent to downstream processing for transaction handling and confirmation.</p>
          </div>
        </section>

        <dl class="detail-grid">
          <div class="detail-item">
            <dt>Payment ID</dt>
            <dd>#{{ store.payment.paymentId }}</dd>
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
          <button
            v-if="canProcessPayment"
            class="btn btn-primary"
            type="button"
            :disabled="isProcessing"
            @click="handleProcessPayment"
          >
            {{ isProcessing ? "Processing..." : "Process Payment" }}
          </button>
          <RouterLink :to="`/payments/${paymentId}/history`" class="btn btn-secondary">History</RouterLink>
          <RouterLink :to="{ name: 'payment-search' }" class="btn btn-primary">Back</RouterLink>
        </div>
      </div>

    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import StatusBadge from "../components/StatusBadge.vue";
import type { PaymentStatus } from "../api/payments";
import { usePaymentStore } from "../stores/payment";
import { formatAmount, formatDateTime } from "../utils/format";

const route = useRoute();
const store = usePaymentStore();

type ProgressStage = {
  status: PaymentStatus;
  message: string;
  progressPercent: number;
};

const progressStages: ProgressStage[] = [
  {
    status: "VALIDATED",
    message: "Validating payment details...",
    progressPercent: 24
  },
  {
    status: "VALIDATED",
    message: "Sending payment for processing...",
    progressPercent: 58
  },
  {
    status: "VALIDATED",
    message: "Waiting for transaction confirmation...",
    progressPercent: 84
  },
  {
    status: "SENT",
    message: "Payment sent for downstream processing.",
    progressPercent: 92
  },
  {
    status: "COMPLETED",
    message: "Transaction confirmed successfully.",
    progressPercent: 100
  }
];

const processingPauseMs = 700;

const paymentId = computed(() => Number(route.params.paymentId));
const isLoading = computed(() => store.status === "loading" && !store.payment);
const isProcessing = ref(false);
const progressMessage = ref("");
const progressPercent = ref(0);
const currentProgressStatus = ref<PaymentStatus>("CREATED");
const hasProgressState = ref(false);
let activeTimer: ReturnType<typeof setTimeout> | null = null;

const canProcessPayment = computed(() => store.payment?.status === "CREATED" && !isProcessing.value);
const showProgressPanel = computed(() => hasProgressState.value || isProcessing.value);

function clearProcessingTimer(): void {
  if (activeTimer !== null) {
    clearTimeout(activeTimer);
    activeTimer = null;
  }
}

function resetProgressState(): void {
  clearProcessingTimer();
  hasProgressState.value = false;
  progressMessage.value = "";
  progressPercent.value = 0;
  currentProgressStatus.value = store.payment?.status ?? "CREATED";
}

function wait(ms: number): Promise<void> {
  return new Promise((resolve) => {
    clearProcessingTimer();
    activeTimer = setTimeout(() => {
      activeTimer = null;
      resolve();
    }, ms);
  });
}

function setProgressState(stage: ProgressStage): void {
  hasProgressState.value = true;
  progressMessage.value = stage.message;
  progressPercent.value = stage.progressPercent;
  currentProgressStatus.value = stage.status;
}

async function loadPayment(currentPaymentId: number): Promise<void> {
  if (!Number.isInteger(currentPaymentId) || currentPaymentId <= 0) {
    resetProgressState();
    store.clearPayment();
    store.status = "error";
    store.errorMessage = "Invalid payment ID.";
    return;
  }

  try {
    await store.fetchPayment(currentPaymentId);
    currentProgressStatus.value = store.payment?.status ?? "CREATED";
  } catch {
    // Error text is already stored.
  }
}

async function reloadPersistedState(currentPaymentId: number): Promise<void> {
  try {
    await store.reloadPaymentAndHistory(currentPaymentId);
  } finally {
    currentProgressStatus.value = store.payment?.status ?? currentProgressStatus.value;
  }
}

async function handleProcessPayment(): Promise<void> {
  const currentPaymentId = paymentId.value;

  if (!Number.isInteger(currentPaymentId) || currentPaymentId <= 0 || !canProcessPayment.value) {
    return;
  }

  isProcessing.value = true;
  store.clearError();
  hasProgressState.value = true;
  progressPercent.value = 0;
  progressMessage.value = "Starting payment processing...";
  currentProgressStatus.value = "CREATED";

  try {
    const validatedPayment = await store.updatePaymentLifecycleStatus(currentPaymentId, "VALIDATED");
    currentProgressStatus.value = validatedPayment.status;

    for (const stage of progressStages.slice(0, 3)) {
      setProgressState(stage);
      await wait(processingPauseMs);
    }

    const sentPayment = await store.updatePaymentLifecycleStatus(currentPaymentId, "SENT");
    setProgressState({
      status: sentPayment.status,
      message: progressStages[3].message,
      progressPercent: progressStages[3].progressPercent
    });

    const completedPayment = await store.updatePaymentLifecycleStatus(currentPaymentId, "COMPLETED");
    setProgressState({
      status: completedPayment.status,
      message: progressStages[4].message,
      progressPercent: progressStages[4].progressPercent
    });

    await reloadPersistedState(currentPaymentId);
  } catch {
    clearProcessingTimer();
    await reloadPersistedState(currentPaymentId);
  } finally {
    clearProcessingTimer();
    isProcessing.value = false;
  }
}

onBeforeUnmount(() => {
  clearProcessingTimer();
});

watch(
  () => [route.name, route.params.paymentId] as const,
  async ([routeName, routePaymentId]) => {
    if (routeName !== "payment-detail") {
      resetProgressState();
      store.clearPayment();
      return;
    }

    const parsedPaymentId = Number(routePaymentId);

    if (store.loadedPaymentId === parsedPaymentId && store.payment?.paymentId === parsedPaymentId) {
      currentProgressStatus.value = store.payment.status;
      return;
    }

    resetProgressState();
    store.clearError();
    store.clearPayment();
    await loadPayment(parsedPaymentId);
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

.status-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 22px 24px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(23, 104, 172, 0.07), rgba(59, 130, 246, 0.03));
  border: 1px solid rgba(176, 205, 233, 0.7);
}

.status-hero-copy {
  display: grid;
  gap: 8px;
}

.status-hero-label,
.status-hero-copy h2 {
  margin: 0;
}

.status-hero-label {
  color: var(--muted);
  font-size: 0.84rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.status-hero-copy h2 {
  color: var(--text);
  font-size: clamp(1.5rem, 2vw, 2rem);
  font-weight: 800;
}

.progress-panel {
  display: grid;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  border: 1px solid rgba(214, 226, 240, 0.88);
  box-shadow: 0 10px 24px rgba(18, 55, 99, 0.05);
}

.progress-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 16px;
}

.progress-eyebrow,
.progress-panel-head h3,
.progress-stage-text,
.progress-note {
  margin: 0;
}

.progress-eyebrow {
  color: var(--brand);
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.progress-panel-head h3 {
  margin-top: 6px;
  color: var(--text);
  font-size: 1.1rem;
}


.progress-bar-track {
  position: relative;
  width: 100%;
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(203, 213, 225, 0.55);
}

.progress-bar-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2563eb 0%, #38bdf8 100%);
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.08);
  transition: width 0.45s ease;
}

.progress-meta {
  display: grid;
  gap: 8px;
}

.progress-stage-text {
  color: var(--text);
  font-weight: 700;
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

  .status-hero,
  .progress-panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>