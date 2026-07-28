<template>
  <span class="badge" :class="className">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PaymentStatus } from "../api/payments";
import { statusLabelMap } from "../utils/status";

const props = defineProps<{
  status: PaymentStatus;
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
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  width: fit-content;
  max-width: 100%;
  white-space: nowrap;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
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