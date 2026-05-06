<script setup lang="ts">
import { ref, computed, reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import type { FormInstance, FormRules } from "element-plus";

const router = useRouter();
const authStore = useAuthStore();

const mode = ref<"login" | "register">("login");
const formRef = ref<FormInstance>();
const form = reactive({
  username: "",
  password: "",
  nickname: "",
});
const error = ref("");
const loading = ref(false);

const title = computed(() => (mode.value === "login" ? "登录" : "注册"));

const rules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 4, max: 64, message: "用户名需要 4-64 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 8, max: 32, message: "密码需要 8-32 个字符", trigger: "blur" },
  ],
};

const handleSubmit = async () => {
  error.value = "";
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    if (mode.value === "login") {
      await authStore.login(form.username, form.password);
    } else {
      await authStore.register(
        form.username,
        form.password,
        form.nickname || undefined,
      );
      await authStore.login(form.username, form.password);
    }
    console.log("登录成功，准备跳转到首页");
    window.location.href = "/";
  } catch (e) {
    error.value = (e as Error).message;
    console.error("登录失败：", e);
  } finally {
    loading.value = false;
  }
};

const toggleMode = () => {
  mode.value = mode.value === "login" ? "register" : "login";
  error.value = "";
};
</script>

<template>
  <div class="login-page">
    <!-- Left: Brand -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="14" fill="var(--color-primary)" />
            <path
              d="M14 24L20 30L34 16"
              stroke="white"
              stroke-width="3.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>
        <h1 class="brand-name">KnowPivot</h1>
        <p class="brand-slogan">企业级 AI 智能知识库平台</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-dot" />
            Agentic RAG 多轮推理问答
          </div>
          <div class="feature-item">
            <span class="feature-dot" />
            文档智能解析与向量检索
          </div>
          <div class="feature-item">
            <span class="feature-dot" />
            私有化部署 · 数据安全
          </div>
        </div>
      </div>
    </div>

    <!-- Right: Form -->
    <div class="login-form-wrap">
      <div class="form-card">
        <h2 class="form-title">{{ title }}</h2>
        <p class="form-subtitle">
          {{
            mode === "login"
              ? "欢迎回来，请登录你的账户"
              : "创建一个新账户开始使用"
          }}
        </p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleSubmit"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名（4-64 字符）"
              autocomplete="username"
            />
          </el-form-item>

          <el-form-item v-if="mode === 'register'">
            <template #label>
              昵称 <span class="optional">可选</span>
            </template>
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码（8-32 字符）"
              autocomplete="current-password"
            />
          </el-form-item>

          <el-alert
            v-if="error"
            :title="error"
            type="error"
            show-icon
            :closable="false"
            class="form-alert"
          />

          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="
              width: 100%;
              height: 44px;
              font-size: var(--font-size-base);
              font-weight: var(--font-weight-semibold);
            "
          >
            {{ title }}
          </el-button>
        </el-form>

        <p class="form-switch">
          {{ mode === "login" ? "还没有账户？" : "已有账户？" }}
          <button class="switch-link" @click="toggleMode">
            {{ mode === "login" ? "立即注册" : "去登录" }}
          </button>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: var(--color-bg);
}

/* Brand */
.login-brand {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    135deg,
    var(--color-primary) 0%,
    var(--color-accent) 100%
  );
  color: white;
  position: relative;
  overflow: hidden;
}

.login-brand::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at 30% 50%,
    rgba(255, 255, 255, 0.1) 0%,
    transparent 60%
  );
}

.brand-content {
  position: relative;
  text-align: center;
  max-width: 400px;
}

.brand-logo {
  margin-bottom: var(--space-5);
}

.brand-name {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  letter-spacing: -1px;
  margin-bottom: var(--space-2);
}

.brand-slogan {
  font-size: var(--font-size-lg);
  opacity: 0.85;
  margin-bottom: var(--space-8);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--font-size-base);
  opacity: 0.8;
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.7);
  flex-shrink: 0;
}

/* Form */
.login-form-wrap {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  flex-shrink: 0;
}

.form-card {
  width: 100%;
  max-width: 380px;
}

.form-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}

.form-subtitle {
  font-size: var(--font-size-base);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-8);
}

.optional {
  font-weight: var(--font-weight-regular);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
}

.form-alert {
  margin-bottom: var(--space-4);
}

.form-switch {
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  margin-top: var(--space-6);
}

.switch-link {
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
  text-decoration: none;
  cursor: pointer;
  background: none;
  border: none;
  font-size: inherit;
}

.switch-link:hover {
  color: var(--color-primary-hover);
}

/* Dark theme */
[data-theme="dark"] .login-brand {
  background: linear-gradient(135deg, #1e3a5f 0%, #2d1b69 100%);
}
</style>
