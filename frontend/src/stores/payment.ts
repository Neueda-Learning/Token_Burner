import { defineStore } from "pinia";
import type {
  CreatePaymentRequest,
  Payment,
  PaymentStatus,
  PaymentHistoryResponse
} from "../api/payments";
import {
  createPayment,
  getPaymentById,
  getPaymentHistory,
  getPaymentsByUser,
  updatePaymentStatus
} from "../api/payments";
import type { ApiError } from "../api/client";

type LoadState = "idle" | "loading" | "success" | "error";

interface PaymentStoreState {
  payment: Payment | null;
  loadedPaymentId: number | null;
  paymentHistory: PaymentHistoryResponse[];
  loadedPaymentHistoryId: number | null;
  userPayments: Payment[];
  loadedUserPaymentsUserId: number | null;
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
    loadedPaymentId: null,
    paymentHistory: [],
    loadedPaymentHistoryId: null,
    userPayments: [],
    loadedUserPaymentsUserId: null,
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

    clearPayment() {
      this.payment = null;
      this.loadedPaymentId = null;
    },

    clearPaymentHistory() {
      this.paymentHistory = [];
      this.loadedPaymentHistoryId = null;
    },

    clearUserPayments() {
      this.userPayments = [];
      this.loadedUserPaymentsUserId = null;
    },

    async createPayment(payload: CreatePaymentRequest): Promise<Payment> {
      this.status = "loading";
      this.errorMessage = "";
      try {
        const created = await createPayment(payload);
        this.payment = created;
        this.loadedPaymentId = created.paymentId;
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
      this.clearPayment();
      this.errorMessage = "";
      try {
        this.payment = await getPaymentById(paymentId);
        this.loadedPaymentId = paymentId;
        this.status = "success";
      } catch (error) {
        this.clearPayment();
        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },

    async fetchPaymentHistory(paymentId: number): Promise<void> {
      this.status = "loading";
      this.clearPaymentHistory();
      this.errorMessage = "";
      try {
        const data = await getPaymentHistory(paymentId);
        this.paymentHistory = data.history ?? [];
        this.loadedPaymentHistoryId = paymentId;
        this.status = "success";
      } catch (error) {
        this.clearPaymentHistory();
        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },

    async fetchPaymentsByUser(userId: number): Promise<void> {
      this.status = "loading";
      this.errorMessage = "";
      this.clearUserPayments();
      try {
        this.userPayments = await getPaymentsByUser(userId);
        this.loadedUserPaymentsUserId = userId;
        this.status = "success";
      } catch (error) {
        this.clearUserPayments();
        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },

    async reloadPaymentAndHistory(paymentId: number): Promise<void> {
      const [paymentData, historyData] = await Promise.all([
        getPaymentById(paymentId),
        getPaymentHistory(paymentId)
      ]);

      this.payment = paymentData;
      this.loadedPaymentId = paymentId;
      this.paymentHistory = historyData.history ?? [];
      this.loadedPaymentHistoryId = paymentId;
      this.status = "success";
    },

    async updatePaymentLifecycleStatus(paymentId: number, nextStatus: PaymentStatus): Promise<Payment> {
      this.status = "loading";
      this.errorMessage = "";

      try {
        const updatedPayment = await updatePaymentStatus(paymentId, { status: nextStatus });
        this.payment = updatedPayment;
        this.loadedPaymentId = paymentId;
        this.status = "success";
        return updatedPayment;
      } catch (error) {

        this.status = "error";
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    }
  }
});