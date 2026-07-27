<template>
  <form class="card form" @submit.prevent="handleSubmit">
    <h2>Create Payment</h2>

    <div class="form-grid">
      <div>
        <label class="label" for="sourceAccountId">Source Account ID</label>
        <input id="sourceAccountId" v-model.number="form.sourceAccountId" class="input" type="number" min="1" />
        <p v-if="errors.sourceAccountId" class="error-text">{{ errors.sourceAccountId }}</p>
      </div>

      <div>
        <label class="label" for="destinationAccountId">Destination Account ID</label>
        <input
          id="destinationAccountId"
          v-model.number="form.destinationAccountId"
          class="input"
          type="number"
          min="1"
        />
        <p v-if="errors.destinationAccountId" class="error-text">{{ errors.destinationAccountId }}</p>
      </div>

      <div>
        <label class="label" for="amount">Amount</label>
        <input id="amount" v-model.number="form.amount" class="input" type="number" min="0" step="0.01" />
        <p v-if="errors.amount" class="error-text">{{ errors.amount }}</p>
      </div>

      <div>
        <label class="label" for="currency">Currency</label>
        <input id="currency" v-model.trim="form.currency" class="input" maxlength="3" placeholder="USD" />
        <p v-if="errors.currency" class="error-text">{{ errors.currency }}</p>
      </div>
    </div>

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

    <button class="btn btn-primary" type="submit" :disabled="loading">
      {{ loading ? "Creating..." : "Create Payment" }}
    </button>
  </form>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import type { CreatePaymentRequest } from "../api/payments";

const props = defineProps<{
  loading: boolean;
  errorMessage: string;
}>();

const emit = defineEmits<{
  submit: [payload: CreatePaymentRequest];
}>();

const form = reactive<CreatePaymentRequest>({
  sourceAccountId: 0,
  destinationAccountId: 0,
  amount: 0,
  currency: "USD"
});

const errors = reactive<Record<keyof CreatePaymentRequest, string>>({
  sourceAccountId: "",
  destinationAccountId: "",
  amount: "",
  currency: ""
});

function clearErrors(): void {
  errors.sourceAccountId = "";
  errors.destinationAccountId = "";
  errors.amount = "";
  errors.currency = "";
}

function validate(): boolean {
  clearErrors();
  let ok = true;

  if (!Number.isInteger(form.sourceAccountId) || form.sourceAccountId <= 0) {
    errors.sourceAccountId = "Source account is required and must be a positive integer.";
    ok = false;
  }

  if (!Number.isInteger(form.destinationAccountId) || form.destinationAccountId <= 0) {
    errors.destinationAccountId = "Destination account is required and must be a positive integer.";
    ok = false;
  }

  if (!(form.amount > 0)) {
    errors.amount = "Amount must be greater than 0.";
    ok = false;
  }

  const normalized = form.currency.toUpperCase();
  const currencyRegex = /^[A-Z]{3}$/;
  if (!currencyRegex.test(normalized)) {
    errors.currency = "Currency must be a 3-letter code, e.g. USD.";
    ok = false;
  } else {
    form.currency = normalized;
  }

  if (form.sourceAccountId === form.destinationAccountId) {
    errors.destinationAccountId = "Destination account must be different from source account.";
    ok = false;
  }

  return ok;
}

function handleSubmit(): void {
  if (!validate()) {
    return;
  }

  emit("submit", {
    sourceAccountId: form.sourceAccountId,
    destinationAccountId: form.destinationAccountId,
    amount: Number(form.amount),
    currency: form.currency.toUpperCase()
  });
}

void props;
</script>

<style scoped>
.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

h2 {
  margin: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}
</style>