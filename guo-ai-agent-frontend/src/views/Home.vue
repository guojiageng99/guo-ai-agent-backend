<template>
  <div class="home">
    <header class="header">
      <h1>AI 应用中心</h1>
      <p class="subtitle">选择您要使用的 AI 应用</p>
    </header>

    <div class="app-cards">
      <router-link to="/love-app" class="app-card">
        <div class="card-icon">💕</div>
        <h2>AI 恋爱大师</h2>
        <p>专业的恋爱顾问，为您的情感问题提供贴心建议</p>
      </router-link>

      <router-link to="/manus" class="app-card">
        <div class="card-icon">🤖</div>
        <h2>AI 超级智能体</h2>
        <p>强大的多功能智能体，助您解决各类问题</p>
      </router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding: 48px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  text-align: center;
  margin-bottom: 48px;
  color: #fff;
}

.header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
}

.app-cards {
  display: flex;
  gap: 24px;
  justify-content: center;
  flex-wrap: wrap;
  max-width: 800px;
  margin: 0 auto;
}

.app-card {
  flex: 1;
  min-width: 280px;
  max-width: 360px;
  padding: 32px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  text-decoration: none;
  color: #333;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.app-card h2 {
  font-size: 1.5rem;
  margin-bottom: 12px;
  color: #333;
}

.app-card p {
  font-size: 0.95rem;
  color: #666;
  line-height: 1.6;
}
</style>
