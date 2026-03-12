<template>
  <footer class="site-footer">
    <div class="footer-content">
      <div class="footer-brand">
        <span class="brand-name">AI 智能体应用</span>
        <span class="brand-desc">恋语AI恋爱大师 · AI超级智能体</span>
      </div>
      <div class="footer-links">
        <router-link to="/" class="footer-link">主页</router-link>
        <router-link to="/love-app" class="footer-link">恋语AI恋爱大师</router-link>
        <router-link to="/manus" class="footer-link">AI超级智能体</router-link>
      </div>
      <div class="footer-legal">
        <a href="#" class="legal-link">《用户协议》</a>
        <a href="#" class="legal-link">《隐私政策》</a>
      </div>
      <div class="footer-copyright">
        <p>© {{ currentYear }} AI 智能体应用 版权所有</p>
        <p class="copyright-icp">基于 Spring AI + Vue3 构建</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.site-footer {
  margin-top: auto;
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-border);
  padding: 32px 24px 24px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-brand {
  margin-bottom: 16px;
}

.brand-name {
  display: block;
  font-family: var(--font-display);
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-accent);
  letter-spacing: 0.05em;
}

.brand-desc {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
}

.footer-link {
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s;
}

.footer-link:hover {
  color: var(--color-accent);
}

.footer-legal {
  margin-bottom: 12px;
}

.legal-link {
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.8rem;
  margin: 0 8px;
}

.legal-link:hover {
  color: var(--color-accent);
}

.footer-copyright {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  opacity: 0.8;
}

.footer-copyright p {
  margin: 4px 0;
}

.copyright-icp {
  font-size: 0.7rem;
  opacity: 0.7;
}

@media (max-width: 768px) {
  .site-footer {
    padding: 24px 16px 20px;
  }

  .footer-links {
    flex-wrap: wrap;
    gap: 16px;
    justify-content: center;
  }

  .footer-link {
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .footer-links {
    flex-direction: column;
  }
}
</style>
