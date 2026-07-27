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

export interface PaymentHistoryItem {
  status: PaymentStatus;
  updatedAt: string;
}

export interface PaymentHistoryResponse {
  paymentId: number;
  history: PaymentHistoryItem[];
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  errorCode: string;
  message: string;
  path: string;
}

export interface CreatePaymentRequest {
  sourceAccountId: number;
  destinationAccountId: number;
  amount: number;
  currency: string;
}

export interface UpdatePaymentStatusRequest {
  status: PaymentStatus;
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

export async function getPaymentHistory(paymentId: number): Promise<PaymentHistoryResponse> {
  const { data } = await apiClient.get<PaymentHistoryResponse>(`/api/payments/${paymentId}/history`);
  return data;
}

export async function getPaymentsByUser(userId: number): Promise<UserPaymentsResponse | Payment[]> {
  const { data } = await apiClient.get<UserPaymentsResponse | Payment[]>(`/api/payments/user/${userId}`);
  return data;
}

export async function updatePaymentStatus(
  paymentId: number,
  payload: UpdatePaymentStatusRequest
): Promise<Payment> {
  const { data } = await apiClient.put<Payment>(`/api/payments/${paymentId}/status`, payload);
  return data;
}