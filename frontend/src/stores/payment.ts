import { defineStore } from "pinia";
import type {
  CreatePaymentRequest,
  Payment,
  PaymentHistoryResponse,
  UserPaymentsResponse
} from "../api/payments";
import {
  createPayment,
  getPaymentById,
  getPaymentHistory,
  getPaymentsByUser
} from "../api/payments";
import type { ApiError } from "../api/client";

type LoadState = "idle" | "loading" | "success" | "error";

interface PaymentStoreState {
  payment: Payment | null;
  paymentHistory: PaymentHistoryResponse[];
  userPayments: Payment[];
  status: LoadState;
  errorMessage: string;
}

function toErrorMessage(error: unknown): string {
  if (typeof error === "string") {
    return error;
  }

  if (error && typeof error === "object") {
    const typed = error as Partial<ApiError>;
    return typed.message ?? "Request failed";
  }

  return "Request failed";
}

export const usePaymentStore = defineStore("payment", {
  state: (): PaymentStoreState => ({
    payment: null,
    paymentHistory: [],
    userPayments: [],
    status: "idle",
    errorMessage: ""
  }),
  actions: {
    clearError() {
      this.errorMessage = "";
      if (this.status === "error") {
        this.status = "idle";
      }
    },

    async createPayment(payload: CreatePaymentRequest): Promise<Payment> {
      this.status = "loading";
      this.errorMessage = "";
      try {
        const created = await createPayment(payload);
        this.payment = created;
        this.status = "success";
        return created;
      } catch (error) {
        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },

    async fetchPayment(paymentId: number): Promise<void> {
      this.status = "loading";
      this.errorMessage = "";
      try {
        this.payment = await getPaymentById(paymentId);
        this.status = "success";
      } catch (error) {
        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },

    async fetchPaymentHistory(paymentId: number): Promise<void> {
      this.status = "loading";
      this.errorMessage = "";
      try {
        const data = await getPaymentHistory(paymentId);
        this.paymentHistory = data.history ?? [];
        this.status = "success";
      } catch (error) {
        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },

    async fetchPaymentsByUser(userId: number): Promise<void> {
      this.status = "loading";
      this.errorMessage = "";
      try {
        const data = await getPaymentsByUser(userId);
        if (Array.isArray(data)) {
          this.userPayments = data;
        } else {
          this.userPayments = (data as UserPaymentsResponse).payments ?? [];
        }
        this.status = "success";
      } catch (error) {
        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    }
  }
});