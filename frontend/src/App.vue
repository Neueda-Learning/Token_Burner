<template>
  <div class="app-shell">
    <AppHeader />
    <main class="content-wrap">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";
import AppHeader from "./components/AppHeader.vue";
</script>

<style>
:root {
  --bg: #f3f6fb;
  --surface: #ffffff;
  --text: #203147;
  --muted: #5a6f88;
  --border: #d8e2ef;
  --brand: #1768ac;
  --brand-strong: #124d80;
  --danger: #b42318;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: "Segoe UI", "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: var(--text);
  background:
    radial-gradient(circle at 20% 10%, #d8ecff 0%, transparent 35%),
    radial-gradient(circle at 90% 5%, #e7f7ef 0%, transparent 30%),
    var(--bg);
}

a {
  color: inherit;
}

.app-shell {
  min-height: 100vh;
}

.content-wrap {
  width: min(1100px, 94vw);
  margin: 24px auto 40px;
}

.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(12, 38, 69, 0.06);
}

.btn {
  border: 0;
  border-radius: 8px;
  padding: 10px 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-primary {
  background: var(--brand);
  color: #fff;
}

.btn-primary:hover {
  background: var(--brand-strong);
}

.btn-secondary {
  background: #e6edf5;
  color: var(--text);
}

.input,
.select {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 10px 12px;
  background: #fff;
}

.label {
  display: block;
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 6px;
}

.error-text {
  color: var(--danger);
  font-size: 13px;
}

.muted {
  color: var(--muted);
}

@media (max-width: 768px) {
  .content-wrap {
    width: 92vw;
    margin-top: 16px;
  }
}
</style>