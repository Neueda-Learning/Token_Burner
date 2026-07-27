<template>
  <section>
    <PaymentForm :loading="isLoading" :error-message="store.errorMessage" @submit="handleCreatePayment" />
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import PaymentForm from "../components/PaymentForm.vue";
import { usePaymentStore } from "../stores/payment";
import type { CreatePaymentRequest } from "../api/payments";

const router = useRouter();
const store = usePaymentStore();

const isLoading = computed(() => store.status === "loading");

async function handleCreatePayment(payload: CreatePaymentRequest): Promise<void> {
  try {
    const created = await store.createPayment(payload);
    await router.push(`/payments/${created.paymentId}`);
  } catch {
    // Error state is handled in the store and shown by the form.
  }
}
</script>