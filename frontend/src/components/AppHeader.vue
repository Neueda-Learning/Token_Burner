<template>
  <header class="header">
    <div class="inner">
      <div class="heading">
        <p class="eyebrow">Payment Processing System</p>
        <h1 class="title">TransiPay Workspace</h1>
      </div>

      <RouterLink :to="{ name: 'dashboard' }" class="brand-shortcut">
        <img :src="logoUrl" alt="Logo" class="logo" />
        <span>Dashboard</span>
      </RouterLink>
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
  border-bottom: 1px solid rgba(214, 226, 240, 0.8);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px rgba(18, 55, 99, 0.05);
}

.inner {
  min-height: 86px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 26px;
}

.heading {
  display: grid;
  gap: 6px;
}

.eyebrow {
  margin: 0;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #5a6f88;
}

.title {
  margin: 0;
  font-size: 1.55rem;
  font-weight: 700;
  color: #203147;
}

.brand-shortcut {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #203147;
  border: 1px solid rgba(214, 226, 240, 0.9);
  border-radius: 999px;
  padding: 9px 15px 9px 11px;
  background: linear-gradient(180deg, #ffffff 0%, #f5f9ff 100%);
  box-shadow: 0 10px 24px rgba(18, 55, 99, 0.08);
  font-weight: 600;
}

.logo {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: block;
}

@media (max-width: 768px) {
  .inner {
    padding: 14px 16px;
    min-height: unset;
  }

  .title {
    font-size: 1.15rem;
  }

  .brand-shortcut span {
    display: none;
  }
}
</style>