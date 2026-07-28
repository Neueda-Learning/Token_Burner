<template>
  <header class="header">
    <div class="inner">
      <RouterLink :to="{ name: 'dashboard' }" class="brand">
        <img :src="logoUrl" alt="Logo" class="logo" />
        <span>Payment Processing System</span>
      </RouterLink>
      <nav class="nav">
        <RouterLink :to="{ name: 'dashboard' }" class="nav-link">Dashboard</RouterLink>
        <RouterLink :to="{ name: 'create-payment' }" class="nav-link">Create Payment</RouterLink>
      </nav>
    </div>
  </header>
</template>

<script setup lang="ts">
import { RouterLink } from "vue-router";
import logoUrl from "../assets/logo.jpg";
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 1000;
  pointer-events: auto;
  border-bottom: 1px solid #d8e2ef;
  background: linear-gradient(90deg, #0f548d, #2f7dbb);
  color: #fff;
}

.inner {
  width: min(1100px, 94vw);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
}

.brand {
  display: inline-flex;
  text-decoration: none;
  color: #fff;
  align-items: center;
  gap: 26px;
}

.brand span {
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1.1;
}

.logo {
  width: 80px;
  height: 80px;
  margin-left: -28px;
  display: block;
}

.nav {
  display: flex;
  gap: 12px;
}

.nav-link {
  text-decoration: none;
  color: rgba(255, 255, 255, 0.95);
  padding: 8px 10px;
  border-radius: 8px;
}

.nav-link.router-link-exact-active {
  background: rgba(255, 255, 255, 0.18);
}

@media (max-width: 768px) {
  .inner {
    width: 92vw;
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>