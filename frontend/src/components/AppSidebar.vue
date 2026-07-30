<template>
  <aside class="sidebar">
    <div class="sidebar-inner">
      <RouterLink :to="{ name: 'dashboard' }" class="sidebar-brand">
        <img :src="logoUrl" alt="Logo" class="sidebar-logo" />
        <div>
          <p class="sidebar-title">TransiPay</p>
          <p class="sidebar-subtitle">Payment Console</p>
        </div>
      </RouterLink>

      <nav class="sidebar-nav" aria-label="Primary navigation">
        <RouterLink :to="{ name: 'dashboard' }" class="nav-link">Dashboard</RouterLink>
        <RouterLink :to="{ name: 'create-payment' }" class="nav-link">Create Payment</RouterLink>
        <RouterLink :to="{ name: 'payment-search' }" class="nav-link">Search Payment</RouterLink>
        <RouterLink :to="{ name: 'payment-history-search' }" class="nav-link">Search History</RouterLink>
        <RouterLink :to="{ name: 'user-payments-search' }" class="nav-link">Search User Payments</RouterLink>
      </nav>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { RouterLink } from "vue-router";
import logoUrl from "../assets/logo.jpg";
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1100;
  width: 260px;
  height: 100vh;
  border-right: 1px solid rgba(214, 226, 240, 0.2);
  background:
    radial-gradient(circle at top left, rgba(96, 165, 250, 0.22) 0%, transparent 28%),
    linear-gradient(180deg, #0a3c73 0%, #0f548d 34%, #145a98 70%, #10477d 100%);
  color: #fff;
  box-shadow: 10px 0 30px rgba(12, 38, 69, 0.16);
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 28px 20px 22px;
}

.sidebar-brand {
  display: grid;
  grid-template-columns: 64px 1fr;
  gap: 14px;
  align-items: center;
  text-decoration: none;
  color: #fff;
  margin-bottom: 32px;
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.sidebar-logo {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 18px rgba(4, 17, 39, 0.2);
}

.sidebar-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.sidebar-subtitle {
  margin: 4px 0 0;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.78);
}

.sidebar-nav {
  display: grid;
  gap: 12px;
}

.nav-link {
  display: block;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.94);
  padding: 13px 15px;
  border-radius: 16px;
  font-weight: 600;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.03);
  transition: background-color 0.2s ease, transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.14);
  transform: translateX(3px);
}

.nav-link.router-link-exact-active {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.14) 100%);
  border-color: rgba(255, 255, 255, 0.18);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.16), 0 8px 18px rgba(10, 33, 65, 0.18);
}

@media (max-width: 900px) {
  .sidebar {
    width: 220px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: auto;
    position: sticky;
    border-right: 0;
    border-bottom: 1px solid rgba(214, 226, 240, 0.35);
  }

  .sidebar-inner {
    padding: 16px;
  }

  .sidebar-brand {
    grid-template-columns: 48px 1fr;
    margin-bottom: 16px;
  }

  .sidebar-logo {
    width: 48px;
    height: 48px;
  }

  .sidebar-title {
    font-size: 1.25rem;
  }

  .sidebar-nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

