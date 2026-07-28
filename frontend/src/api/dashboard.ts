import { apiClient } from "./client";
import type { PaymentStatus } from "./payments";

export interface DashboardSummaryResponse {
  totalUsers: number;
  totalPayments: number;
  totalTransactionAmount: number;
  completedPayments: number;
  failedPayments: number;
  statusDistribution: Record<PaymentStatus, number>;
}

export async function getDashboardSummary(): Promise<DashboardSummaryResponse> {
  const { data } = await apiClient.get<DashboardSummaryResponse>("/api/dashboard/summary");
  return data;
}

