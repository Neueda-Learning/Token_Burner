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
  userId?: number;
  payments: Payment[];
}

function isPayment(value: unknown): value is Payment {
  return Boolean(
    value &&
    typeof value === "object" &&
    "paymentId" in value &&
    "sourceAccountId" in value &&
    "destinationAccountId" in value &&
    "amount" in value &&
    "currency" in value &&
    "status" in value &&
    "createdAt" in value &&
    "updatedAt" in value
  );
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

export async function getPaymentsByUser(userId: number): Promise<Payment[]> {
  const { data } = await apiClient.get<UserPaymentsResponse | Payment[] | Payment>(`/api/payments/user/${userId}`);

  if (Array.isArray(data)) {
    return data;
  }

  if (isPayment(data)) {
    return [data];
  }

  return data.payments ?? [];
}
