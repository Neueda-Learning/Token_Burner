<template>
  <section class="dashboard">
    <div class="card hero">
      <div class="hero-copy">
        <p class="hero-eyebrow">Welcome to TransiPay</p>
        <h1>Your Payment Dashboard</h1>
        <p class="muted">
          Track your payment activity, monitor transaction progress, and stay informed throughout your
          payment journey.
        </p>
      </div>
    </div>

    <section class="card overview-panel">
      <div class="section-head overview-head">
        <div>
          <h2>Payment Overview</h2>
        </div>
      </div>

      <div class="overview-grid">
        <article v-for="item in overviewMetrics" :key="item.label" class="overview-metric">
          <p class="overview-value" :title="item.title">{{ item.value }}</p>
          <p class="overview-label">{{ item.label }}</p>
        </article>
      </div>
    </section>

    <section class="analytics-grid">
      <article class="card lifecycle-card">
        <div class="section-head">
          <div>
            <h2>Payment Lifecycle</h2>
            <p class="muted">Follow your payment journey from creation to successful completion.</p>
          </div>
        </div>

        <div class="lifecycle-flow">
          <div class="flow-main">
            <div class="flow-step created">CREATED</div>
            <span class="flow-arrow">→</span>
            <div class="flow-step validated">VALIDATED</div>
            <span class="flow-arrow">→</span>
            <div class="flow-step sent">SENT</div>
            <span class="flow-arrow">→</span>
            <div class="flow-step completed">COMPLETED</div>
          </div>

          <div class="flow-exception">
            <span class="exception-line"></span>
            <div class="flow-step failed">FAILED</div>
            <p class="muted">Payments requiring additional verification or support.</p>
          </div>
        </div>
      </article>

      <article class="card chart-card">
        <div class="section-head">
          <div>
            <h2>Payment Status Distribution</h2>
            <p class="muted">Overview of payment status distribution.</p>
          </div>
        </div>

        <div class="status-chart">
          <div v-for="item in statusDistribution" :key="item.status" class="status-row">
            <div class="status-meta">
              <span class="status-dot" :class="item.status.toLowerCase()"></span>
              <span>{{ item.status }}</span>
            </div>
            <div class="bar-track">
              <div class="bar-fill" :class="item.status.toLowerCase()" :style="{ width: `${item.percent}%` }"></div>
            </div>
            <div class="status-value">{{ item.count }}</div>
          </div>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getDashboardSummary, type DashboardSummaryResponse } from "../api/dashboard";
import type { PaymentStatus } from "../api/payments";
import { formatAmount } from "../utils/format";

interface OverviewMetric {
  label: string;
  value: string;
  title: string;
}

interface StatusDistributionItem {
  status: PaymentStatus;
  count: number;
  percent: number;
}

const isLoading = ref(true);
const errorMessage = ref("");
const dashboardSummary = ref<DashboardSummaryResponse | null>(null);

const statusOrder: PaymentStatus[] = ["CREATED", "VALIDATED", "SENT", "COMPLETED", "FAILED"];

function formatCompactCurrency(value: number): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    notation: "compact",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value);
}

const overviewMetrics = computed<OverviewMetric[]>(() => {
  const totalUsers = dashboardSummary.value?.totalUsers ?? 0;
  const totalPayments = dashboardSummary.value?.totalPayments ?? 0;
  const totalTransactionAmount = dashboardSummary.value?.totalTransactionAmount ?? 0;
  const completedPayments = dashboardSummary.value?.completedPayments ?? 0;
  const failedPayments = dashboardSummary.value?.failedPayments ?? 0;

  return [
    {
      label: "Registered Users",
      value: String(totalUsers),
      title: `${totalUsers} registered users`
    },
    {
      label: "Total Payments",
      value: String(totalPayments),
      title: `${totalPayments} total payments`
    },
    {
      label: "Transaction Volume",
      value: formatCompactCurrency(totalTransactionAmount),
      title: formatAmount(totalTransactionAmount, "USD")
    },
    {
      label: "Completed",
      value: String(completedPayments),
      title: `${completedPayments} completed payments`
    },
    {
      label: "Failed",
      value: String(failedPayments),
      title: `${failedPayments} failed payments`
    }
  ];
});

const statusDistribution = computed<StatusDistributionItem[]>(() => {
  const distribution: Partial<Record<PaymentStatus, number>> = dashboardSummary.value?.statusDistribution ?? {};
  const total = dashboardSummary.value?.totalPayments || 1;

  return statusOrder.map((status) => {
    const count = distribution[status] ?? 0;
    return {
      status,
      count,
      percent: count === 0 ? 0 : Math.max(12, Math.round((count / total) * 100))
    };
  });
});

async function loadDashboard(): Promise<void> {
  isLoading.value = true;
  errorMessage.value = "";

  try {
    dashboardSummary.value = await getDashboardSummary();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : "Failed to load dashboard summary.";
    dashboardSummary.value = null;
  } finally {
    isLoading.value = false;
  }
}

onMounted(async () => {
  await loadDashboard();
});
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 22px;
}

.hero {
  padding: 28px;
  display: grid;
  gap: 12px;
}

.hero-copy {
  display: grid;
  gap: 10px;
}

.hero h1 {
  margin: 0 0 8px;
  font-size: clamp(1.7rem, 2vw, 2.2rem);
}

.hero-eyebrow {
  margin: 0;
  color: var(--brand);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  font-size: 0.82rem;
}

.overview-panel {
  display: grid;
  gap: 24px;
}

.overview-head {
  margin-bottom: 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18px;
}

.overview-metric {
  display: grid;
  gap: 10px;
  align-content: start;
  min-width: 0;
}

.overview-label {
  margin: 0;
  font-size: 0.9rem;
  color: var(--muted);
  font-weight: 700;
  line-height: 1.4;
}

.overview-value {
  margin: 0;
  font-size: clamp(1.75rem, 2.4vw, 2.35rem);
  font-weight: 800;
  color: var(--text);
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analytics-grid {
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: 18px;
  align-items: start;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.section-head h2,
.section-head p {
  margin: 0;
}

.lifecycle-card,
.chart-card {
  display: grid;
}

.lifecycle-flow {
  display: grid;
  gap: 22px;
}

.flow-main {
  display: grid;
  grid-template-columns: repeat(7, auto);
  gap: 10px;
  align-items: center;
  justify-content: start;
}

.flow-step {
  padding: 12px 16px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.9rem;
  border: 1px solid transparent;
  text-align: center;
}

.flow-arrow {
  color: var(--brand);
  font-size: 1.2rem;
  font-weight: 800;
}

.created {
  background: #dde7f2;
  border-color: #c6d8ea;
  color: #314a68;
}

.validated {
  background: #dbeafe;
  border-color: #bfdbfe;
  color: #1d4f8f;
}

.sent {
  background: #ffedd5;
  border-color: #fed7aa;
  color: #995f00;
}

.completed {
  background: #dcfce7;
  border-color: #bbf7d0;
  color: #166534;
}

.failed {
  background: #fee2e2;
  border-color: #fecaca;
  color: #991b1b;
}

.flow-exception {
  display: grid;
  grid-template-columns: auto auto 1fr;
  gap: 14px;
  align-items: center;
}

.flow-exception p {
  margin: 0;
}

.exception-line {
  width: 2px;
  height: 42px;
  background: linear-gradient(180deg, rgba(22, 93, 190, 0.2) 0%, rgba(180, 35, 24, 0.6) 100%);
  justify-self: center;
}

.status-chart {
  display: grid;
  gap: 14px;
}

.status-row {
  display: grid;
  grid-template-columns: 140px 1fr 40px;
  gap: 12px;
  align-items: center;
}

.status-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.created,
.bar-fill.created {
  background: #6c8dae;
}

.status-dot.validated,
.bar-fill.validated {
  background: #3b82f6;
}

.status-dot.sent,
.bar-fill.sent {
  background: #f59e0b;
}

.status-dot.completed,
.bar-fill.completed {
  background: #22c55e;
}

.status-dot.failed,
.bar-fill.failed {
  background: #ef4444;
}

.bar-track {
  height: 12px;
  border-radius: 999px;
  background: #eaf0f8;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: inherit;
}

.status-value {
  text-align: right;
  font-weight: 700;
}


@media (max-width: 1180px) {
  .overview-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .analytics-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .flow-main {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .flow-arrow {
    transform: rotate(90deg);
    margin-left: 10px;
  }

  .flow-exception {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .exception-line {
    width: 42px;
    height: 2px;
  }
}

@media (max-width: 640px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .status-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .status-value {
    text-align: left;
  }
}
</style>
