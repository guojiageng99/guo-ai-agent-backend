<template>
  <footer class="site-footer">
    <div class="footer-content">
      <div class="footer-section footer-brand">
        <span class="brand-name">AI 智能体应用</span>
        <span class="brand-desc">恋语AI恋爱大师 · AI超级智能体</span>
      </div>

      <div class="footer-section footer-quick-links">
        <h4 class="section-title">快速链接</h4>
        <router-link to="/" class="footer-link">主页</router-link>
        <router-link to="/love-app" class="footer-link">恋语AI恋爱大师</router-link>
        <router-link to="/manus" class="footer-link">AI超级智能体</router-link>
      </div>

      <div class="footer-section footer-contact">
        <h4 class="section-title">联系我们</h4>
        <p class="contact-item">公众号：烤冷面</p>
        <p class="contact-item">
          <a href="mailto:guojiageng1024@163.com" class="contact-link">邮箱：guojiageng1024@163.com</a>
        </p>
      </div>

      <div class="footer-section footer-legal">
        <h4 class="section-title">法律信息</h4>
        <a href="#" class="legal-link">《用户协议》</a>
        <a href="#" class="legal-link">《隐私政策》</a>
      </div>
    </div>

    <div class="footer-bottom">
      <p class="copyright">© {{ currentYear }} AI 智能体应用 版权所有</p>
      <p class="copyright-tech">基于 Spring AI + Vue3 构建</p>
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
  padding: 48px 24px 32px;
}

.footer-content {
  max-width: 1000px;
  margin: 0 auto 40px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  text-align: left;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-brand {
  gap: 8px;
}

.brand-name {
  font-family: var(--font-display);
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-accent);
  letter-spacing: 0.05em;
}

.brand-desc {
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.section-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
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

.contact-item {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  margin: 0;
}

.contact-link {
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color 0.2s;
}

.contact-link:hover {
  color: var(--color-accent);
}

.legal-link {
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.9rem;
  display: block;
  transition: color 0.2s;
}

.legal-link:hover {
  color: var(--color-accent);
}

.footer-bottom {
  padding-top: 32px;
  border-top: 1px solid var(--color-border);
  text-align: center;
}

.copyright {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  margin: 0 0 8px;
}

.copyright-tech {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  opacity: 0.8;
  margin: 0;
}

@media (max-width: 768px) {
  .site-footer {
    padding: 36px 20px 28px;
  }

  .footer-content {
    grid-template-columns: repeat(2, 1fr);
    gap: 32px;
    margin-bottom: 32px;
  }

  .footer-bottom {
    padding-top: 24px;
  }
}

@media (max-width: 480px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 28px;
    text-align: center;
  }

  .footer-section {
    align-items: center;
  }
}
</style>
