import { apiClient } from "./client";

export type PaymentStatus = "CREATED" | "VALIDATED" | "SENT" | "COMPLETED" | "FAILED";

export interface Payment {
  paymentId: number;
  sourceAccountId: number;
  destinationAccountId: number;
  amount: number;
  currency: string;
  status: PaymentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentHistoryResponse {
  historyId: number;
  paymentId?: number;
  previousStatus: PaymentStatus | null;
  newStatus: PaymentStatus;
  changedAt: string;
  notes: string | null;
}

export interface PaymentHistoryListResponse {
  paymentId: number;
  history: PaymentHistoryResponse[];
}

export interface ErrorResponse {
  errorCode: string;
  message: string;
  timestamp: string;
  path: string;
}

export interface CreatePaymentRequest {
  sourceAccountId: number;
  destinationAccountId: number;
  amount: number;
  currency: string;
  paymentPassword: string;
}

export interface UserPaymentsResponse {
  userId: number;
  payments: Payment[];
}

export async function createPayment(payload: CreatePaymentRequest): Promise<Payment> {
  const { data } = await apiClient.post<Payment>("/api/payments", payload);
  return data;
}

export async function getPaymentById(paymentId: number): Promise<Payment> {
  const { data } = await apiClient.get<Payment>(`/api/payments/${paymentId}`);
  return data;
}

export async function getPaymentHistory(paymentId: number): Promise<PaymentHistoryListResponse> {
  const { data } = await apiClient.get<PaymentHistoryListResponse | PaymentHistoryResponse[]>(
    `/api/payments/${paymentId}/history`
  );

  if (Array.isArray(data)) {
    return {
      paymentId,
      history: data
    };
  }

  return {
    paymentId: data.paymentId ?? paymentId,
    history: data.history ?? []
  };
}

export async function getPaymentsByUser(userId: number): Promise<UserPaymentsResponse | Payment[]> {
  const { data } = await apiClient.get<UserPaymentsResponse | Payment[]>(`/api/payments/user/${userId}`);
  return data;
}
