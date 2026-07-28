import type { PaymentStatus } from "../api/payments";

export const statusLabelMap: Record<PaymentStatus, string> = {
  CREATED: "Created",
  VALIDATED: "Validated",
  SENT: "Sent",
  COMPLETED: "Completed",
  FAILED: "Failed"
};

export const allowedTransitions: Record<PaymentStatus, PaymentStatus[]> = {
  CREATED: ["VALIDATED", "FAILED"],
  VALIDATED: ["SENT", "FAILED"],
  SENT: ["COMPLETED", "FAILED"],
  COMPLETED: [],
  FAILED: []
};

export function getAllowedNextStatuses(status: PaymentStatus): PaymentStatus[] {
  return allowedTransitions[status] ?? [];
}

export function isTransitionAllowed(currentStatus: PaymentStatus, nextStatus: PaymentStatus): boolean {
  return getAllowedNextStatuses(currentStatus).includes(nextStatus);
}