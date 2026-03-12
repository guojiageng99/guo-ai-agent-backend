<template>
  <div class="home">
    <div class="grid-bg" aria-hidden="true"></div>
    <header class="header">
      <div class="logo-badge">
        <span class="logo-icon">&lt;/&gt;</span>
        <span class="logo-text">AI_AGENT</span>
      </div>
      <h1 class="title">
        <span class="title-line">AI 应用中心</span>
      </h1>
      <p class="subtitle">选择您的智能体，开启极客之旅</p>
    </header>

    <div class="typewriter-banner">
      <p class="typewriter-text">
        {{ displayedText }}<span v-if="!typewriterDone" class="cursor">|</span>
      </p>
    </div>

    <div class="app-cards">
      <router-link to="/love-app" class="app-card app-card-love">
        <div class="card-glow"></div>
        <div class="card-icon">💕</div>
        <h2>恋语AI恋爱大师</h2>
        <p>专业的恋爱顾问，为您的情感问题提供贴心建议</p>
        <span class="card-arrow">→</span>
      </router-link>

      <router-link to="/manus" class="app-card app-card-manus">
        <div class="card-glow"></div>
        <div class="card-icon">🤖</div>
        <h2>AI 超级智能体</h2>
        <p>强大的多功能智能体，助您解决各类问题</p>
        <span class="card-arrow">→</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const typewriterText = '探索AI的无限可能，体验智能对话的魅力'
const displayedText = ref('')
const typewriterDone = ref(false)
const typingSpeed = 100

function runTypewriter() {
  let index = 0

  function tick() {
    displayedText.value = typewriterText.slice(0, index + 1)
    index++
    if (index < typewriterText.length) {
      setTimeout(tick, typingSpeed)
    } else {
      typewriterDone.value = true
    }
  }

  tick()
}

onMounted(() => {
  runTypewriter()
})
</script>

<style scoped>
.home {
  position: relative;
  min-height: 100vh;
  padding: 48px 24px 64px;
  overflow: hidden;
}

.grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

.header {
  position: relative;
  text-align: center;
  margin-bottom: 48px;
}

.logo-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: var(--radius-sm);
  margin-bottom: 24px;
  font-family: var(--font-display);
  font-size: 0.85rem;
  letter-spacing: 0.1em;
}

.logo-icon {
  color: var(--color-accent);
}

.logo-text {
  color: var(--color-text-muted);
}

.title {
  font-family: var(--font-display);
  font-size: clamp(2rem, 5vw, 3rem);
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 12px;
  letter-spacing: 0.05em;
  text-shadow: 0 0 30px rgba(0, 212, 255, 0.2);
}

.subtitle {
  font-size: 1rem;
  color: var(--color-text-muted);
}

.typewriter-banner {
  position: relative;
  text-align: center;
  margin-bottom: 40px;
  padding: 20px 24px;
}

.typewriter-text {
  font-size: 1.1rem;
  color: var(--color-text);
  line-height: 1.8;
  min-height: 2em;
}

.cursor {
  display: inline-block;
  color: var(--color-accent);
  animation: blink 1s step-end infinite;
  margin-left: 2px;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.app-cards {
  position: relative;
  display: flex;
  gap: 24px;
  justify-content: center;
  flex-wrap: wrap;
  max-width: 900px;
  margin: 0 auto;
}

.app-card {
  position: relative;
  flex: 1;
  min-width: 280px;
  max-width: 420px;
  padding: 32px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  text-decoration: none;
  color: var(--color-text);
  transition: all 0.3s ease;
  overflow: hidden;
}

.app-card:hover {
  transform: translateY(-6px);
  border-color: var(--color-accent);
  box-shadow: var(--shadow-glow);
}

.app-card-love:hover {
  border-color: var(--color-accent-love);
  box-shadow: 0 0 30px rgba(236, 72, 153, 0.2);
}

.app-card-manus:hover {
  border-color: var(--color-accent-manus);
  box-shadow: 0 0 30px rgba(6, 182, 212, 0.2);
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.app-card-love .card-glow {
  background: radial-gradient(circle, rgba(236, 72, 153, 0.1) 0%, transparent 70%);
}

.app-card-manus .card-glow {
  background: radial-gradient(circle, rgba(6, 182, 212, 0.1) 0%, transparent 70%);
}

.app-card:hover .card-glow {
  opacity: 1;
}

.card-icon {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.app-card h2 {
  font-family: var(--font-display);
  font-size: 1.35rem;
  margin-bottom: 12px;
  color: var(--color-text);
}

.app-card-love h2 {
  color: var(--color-accent-love);
}

.app-card-manus h2 {
  color: var(--color-accent-manus);
}

.app-card p {
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.6;
  text-align: left;
}

.card-arrow {
  position: absolute;
  bottom: 24px;
  right: 24px;
  font-size: 1.2rem;
  color: var(--color-text-muted);
  opacity: 0;
  transform: translateX(-8px);
  transition: all 0.3s;
}

.app-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(0);
}

.app-card-love:hover .card-arrow {
  color: var(--color-accent-love);
}

.app-card-manus:hover .card-arrow {
  color: var(--color-accent-manus);
}

@media (max-width: 768px) {
  .home {
    padding: 32px 16px 48px;
  }

  .header {
    margin-bottom: 36px;
  }

  .typewriter-banner {
    margin-bottom: 32px;
    padding: 16px;
  }

  .typewriter-text {
    font-size: 1rem;
  }

  .app-cards {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }

  .app-card {
    min-width: 100%;
    max-width: 100%;
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .home {
    padding: 24px 12px 40px;
  }

  .typewriter-text {
    font-size: 0.95rem;
  }

  .logo-badge {
    font-size: 0.75rem;
    padding: 6px 12px;
  }

  .app-card {
    padding: 20px;
  }
}
</style>
