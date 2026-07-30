import { createRouter, createWebHashHistory } from "vue-router";
import DashboardView from "../views/DashboardView.vue";
import CreatePaymentView from "../views/CreatePaymentView.vue";
import PaymentSearchView from "../views/PaymentSearchView.vue";
import PaymentHistorySearchView from "../views/PaymentHistorySearchView.vue";
import PaymentDetailView from "../views/PaymentDetailView.vue";
import PaymentHistoryView from "../views/PaymentHistoryView.vue";
import UserPaymentsSearchView from "../views/UserPaymentsSearchView.vue";
import UserPaymentsView from "../views/UserPaymentsView.vue";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", redirect: { name: "dashboard" } },
    { path: "/dashboard", name: "dashboard", component: DashboardView },
    { path: "/payments/create", name: "create-payment", component: CreatePaymentView },
      { path: "/payments/search", name: "payment-search", component: PaymentSearchView },
      { path: "/payments/history/search", name: "payment-history-search", component: PaymentHistorySearchView },
    { path: "/payments/:paymentId", name: "payment-detail", component: PaymentDetailView },
    { path: "/payments/:paymentId/history", name: "payment-history", component: PaymentHistoryView },
      { path: "/users/payments/search", name: "user-payments-search", component: UserPaymentsSearchView },
    { path: "/users/:userId/payments", name: "user-payments", component: UserPaymentsView }
  ]
});

export default router;