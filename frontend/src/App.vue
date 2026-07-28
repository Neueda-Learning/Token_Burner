<template>
  <div class="app-shell">
    <AppSidebar />

    <div class="app-main">
      <AppHeader />

      <main class="content-wrap">
        <RouterView v-slot="{ Component }">
          <component v-if="Component" :is="Component" />
        </RouterView>
      </main>

      <AppFooter />
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";
import AppHeader from "./components/AppHeader.vue";
import AppFooter from "./components/AppFooter.vue";
import AppSidebar from "./components/AppSidebar.vue";
</script>

<style>
:root {
  --bg: #eef4fb;
  --bg-deep: #e5eef9;
  --surface: #ffffff;
  --surface-soft: #f7fbff;
  --text: #16263d;
  --muted: #5f748f;
  --border: #d6e2f0;
  --border-strong: #c3d5e8;
  --brand: #165dbe;
  --brand-strong: #10498f;
  --brand-soft: #e8f1fe;
  --accent: #3b82f6;
  --danger: #b42318;
  --success: #0f766e;
  --shadow-sm: 0 10px 24px rgba(18, 55, 99, 0.06);
  --shadow-md: 0 18px 44px rgba(16, 54, 100, 0.1);
  --shadow-lg: 0 24px 64px rgba(10, 38, 74, 0.12);
  --radius-lg: 20px;
  --radius-md: 14px;
  --radius-sm: 10px;
}

* {
  box-sizing: border-box;
}

html {
  scroll-behavior: smooth;
}

body {
  margin: 0;
  font-family: "Segoe UI", "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: var(--text);
  background:
    radial-gradient(circle at 12% 10%, rgba(120, 176, 255, 0.26) 0%, transparent 32%),
    radial-gradient(circle at 90% 0%, rgba(128, 199, 255, 0.22) 0%, transparent 28%),
    linear-gradient(180deg, #f8fbff 0%, var(--bg) 55%, var(--bg-deep) 100%),
    var(--bg);
}

a {
  color: inherit;
}

.app-shell {
  min-height: 100vh;
  display: flex;
  background:
    radial-gradient(circle at 18% 10%, rgba(120, 176, 255, 0.22) 0%, transparent 32%),
    radial-gradient(circle at 92% 3%, rgba(145, 211, 255, 0.16) 0%, transparent 26%),
    linear-gradient(180deg, #f9fbff 0%, var(--bg) 58%, var(--bg-deep) 100%),
    var(--bg);
}

.app-main {
  flex: 1;
  min-width: 0;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
}

.content-wrap {
  flex: 1;
  width: min(1200px, calc(100vw - 260px - 48px));
  margin: 28px auto 48px;
}

.card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, var(--surface) 100%);
  border: 1px solid rgba(214, 226, 240, 0.9);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(8px);
}

.btn {
  border: 0;
  border-radius: 999px;
  padding: 11px 18px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

.btn-primary {
  background: linear-gradient(135deg, var(--brand) 0%, #2478e5 100%);
  color: #fff;
  box-shadow: 0 12px 26px rgba(22, 93, 190, 0.22);
}

.btn-primary:hover {
  background: linear-gradient(135deg, var(--brand-strong) 0%, var(--brand) 100%);
  transform: translateY(-1px);
  box-shadow: 0 16px 28px rgba(16, 73, 143, 0.24);
}

.btn-secondary {
  background: linear-gradient(180deg, #f4f8fd 0%, #e8f0fa 100%);
  color: var(--text);
  border: 1px solid var(--border-strong);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
}

.input,
.select {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 12px 14px;
  background: #fff;
  color: var(--text);
  box-shadow: inset 0 1px 2px rgba(14, 42, 76, 0.03);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.input:focus,
.select:focus {
  outline: none;
  border-color: rgba(22, 93, 190, 0.45);
  box-shadow: 0 0 0 4px rgba(22, 93, 190, 0.12);
}

.label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--muted);
  margin-bottom: 8px;
}

.error-text {
  color: var(--danger);
  font-size: 13px;
  margin: 0;
}

.muted {
  color: var(--muted);
}

@media (max-width: 768px) {
  .app-shell {
    display: block;
  }

  .app-main {
    margin-left: 0;
  }

  .content-wrap {
    width: 92vw;
    margin: 18px auto 32px;
  }

  .card {
    padding: 20px;
  }
}
</style>