<template>
  <footer class="footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <h3>TransiPay</h3>
        <p class="muted">Reliable payment lifecycle tracking for operations, support, and audit reviews.</p>
      </div>

      <div class="footer-meta">
        <p class="muted">Version 1.0.0</p>
        <p class="muted">© 2026 Token Burner</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts"></script>

<style scoped>
.footer {
  margin-top: auto;
  border-top: 1px solid rgba(79, 132, 182, 0.22);
  background: linear-gradient(180deg, rgba(16, 73, 143, 0.96) 0%, rgba(14, 57, 111, 0.98) 100%);
}

.footer-inner {
  width: min(1100px, 94vw);
  margin: 0 auto;
  padding: 18px 0 22px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  align-items: center;
}

.footer-brand h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #f4f9ff;
}

.footer-brand p {
  margin: 0;
  font-size: 13px;
  color: #d7e8fb;
}

.footer-meta {
  text-align: right;
}

.footer-meta p {
  margin: 0;
  font-size: 12px;
  color: #d7e8fb;
}

@media (max-width: 820px) {
  .footer-inner {
    width: 92vw;
    grid-template-columns: 1fr;
    gap: 12px;
    align-items: start;
  }

  .footer-meta {
    text-align: left;
  }
}
</style>
