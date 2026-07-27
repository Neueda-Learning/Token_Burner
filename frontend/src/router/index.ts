import { createRouter, createWebHashHistory } from "vue-router";
import DashboardView from "../views/DashboardView.vue";
import CreatePaymentView from "../views/CreatePaymentView.vue";
import PaymentDetailView from "../views/PaymentDetailView.vue";
import PaymentHistoryView from "../views/PaymentHistoryView.vue";
import UserPaymentsView from "../views/UserPaymentsView.vue";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", name: "dashboard", component: DashboardView, alias: ["/dashboard"] },
    { path: "/payments/create", name: "create-payment", component: CreatePaymentView },
    { path: "/payments/:paymentId", name: "payment-detail", component: PaymentDetailView },
    { path: "/payments/:paymentId/history", name: "payment-history", component: PaymentHistoryView },
    { path: "/users/:userId/payments", name: "user-payments", component: UserPaymentsView }
  ]
});

export default router;