<template>
  <form class="card form" @submit.prevent="handleSubmit">
    <h2>Create Payment</h2>

    <div class="form-grid">
      <div>
        <label class="label" for="sourceAccountId">Source Account ID</label>
        <input
          id="sourceAccountId"
          v-model.number="form.sourceAccountId"
          class="input"
          type="number"
          min="1"
          placeholder="Enter source account ID"
        />
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
          placeholder="Enter destination account ID"
        />
        <p v-if="errors.destinationAccountId" class="error-text">{{ errors.destinationAccountId }}</p>
      </div>

      <div>
        <label class="label" for="amount">Amount</label>
        <input
          id="amount"
          v-model.number="form.amount"
          class="input"
          type="number"
          min="0"
          step="0.01"
          placeholder="Enter payment amount"
        />
        <p v-if="errors.amount" class="error-text">{{ errors.amount }}</p>
      </div>

      <div>
        <label class="label" for="currency">Currency</label>
        <input id="currency" v-model.trim="form.currency" class="input" maxlength="3" placeholder="USD" />
        <p v-if="errors.currency" class="error-text">{{ errors.currency }}</p>
      </div>

      <div>
        <label class="label" for="paymentPassword">Payment Password</label>
        <div class="password-field">
          <input
            id="paymentPassword"
            v-model.trim="form.paymentPassword"
            class="input password-input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="Enter payment password"
          />
          <button
            type="button"
            class="password-visibility-btn"
            :aria-label="showPassword ? 'Hide password' : 'Show password'"
            :title="showPassword ? 'Hide password' : 'Show password'"
            @click="showPassword = !showPassword"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true" class="eye-icon">
              <path
                d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="1.8" />
              <path
                v-if="showPassword"
                d="M4 4l16 16"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>
        <p v-if="errors.paymentPassword" class="error-text">{{ errors.paymentPassword }}</p>
      </div>
    </div>

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

    <button class="btn btn-primary" type="submit" :disabled="loading">
      {{ loading ? "Creating..." : "Create Payment" }}
    </button>
  </form>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
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
  currency: "USD",
  paymentPassword: ""
});

const showPassword = ref(false);

const errors = reactive<Record<keyof CreatePaymentRequest, string>>({
  sourceAccountId: "",
  destinationAccountId: "",
  amount: "",
  currency: "",
  paymentPassword: ""
});

function clearErrors(): void {
  errors.sourceAccountId = "";
  errors.destinationAccountId = "";
  errors.amount = "";
  errors.currency = "";
  errors.paymentPassword = "";
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

  if (!form.paymentPassword || !form.paymentPassword.trim()) {
    errors.paymentPassword = "Payment password is required.";
    ok = false;
  }

  return ok;
}

function handleSubmit(): void {
  if (!validate()) {
    return;
  }

  const payload = {
    sourceAccountId: form.sourceAccountId,
    destinationAccountId: form.destinationAccountId,
    amount: Number(form.amount),
    currency: form.currency.toUpperCase(),
    paymentPassword: form.paymentPassword
  };

  console.debug("[payment:create:submit]", {
    payload: {
      ...payload,
      paymentPassword: "***masked***"
    }
  });

  emit("submit", payload);
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

.password-field {
  position: relative;
}

.password-input {
  padding-right: 44px;
}

.password-visibility-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  border: 0;
  background: transparent;
  color: #6b7280;
  padding: 0;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.password-visibility-btn:hover {
  color: #1f2937;
}

.eye-icon {
  width: 20px;
  height: 20px;
}
</style>