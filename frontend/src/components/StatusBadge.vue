<template>
  <span class="badge" :class="[className, sizeClass]">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PaymentStatus } from "../api/payments";
import { statusLabelMap } from "../utils/status";

const props = defineProps<{
  status: PaymentStatus;
  size?: "sm" | "md" | "lg";
}>();

const label = computed(() => statusLabelMap[props.status] ?? props.status);

const className = computed(() => {
  switch (props.status) {
    case "CREATED":
      return "created";
    case "VALIDATED":
      return "validated";
    case "SENT":
      return "sent";
    case "COMPLETED":
      return "completed";
    case "FAILED":
      return "failed";
    default:
      return "";
  }
});

const sizeClass = computed(() => {
  switch (props.size ?? "sm") {
    case "lg":
      return "badge-lg";
    case "md":
      return "badge-md";
    default:
      return "badge-sm";
  }
});
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  max-width: 100%;
  white-space: nowrap;
  border-radius: 999px;
  font-weight: 700;
}

.badge-sm {
  padding: 4px 10px;
  font-size: 12px;
}

.badge-md {
  padding: 8px 14px;
  font-size: 0.85rem;
}

.badge-lg {
  padding: 14px 24px;
  min-width: 170px;
  justify-content: center;
  font-size: 1rem;
  letter-spacing: 0.06em;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08);
}

.created {
  background: #dde7f2;
  color: #314a68;
}

.validated {
  background: #dbeafe;
  color: #1d4f8f;
}

.sent {
  background: #ffedd5;
  color: #995f00;
}

.completed {
  background: #dcfce7;
  color: #166534;
}

.failed {
  background: #fee2e2;
  color: #991b1b;
}
</style>