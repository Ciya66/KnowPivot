import { createRouter, createWebHistory } from "vue-router";
import { TOKEN_KEY } from "@/utils/request";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("@/views/LoginView.vue"),
      meta: { requiresAuth: false },
    },
    {
      path: "/",
      name: "chat",
      component: () => import("@/views/ChatView.vue"),
    },
    {
      path: "/knowledge",
      name: "knowledge",
      component: () => import("@/views/KnowledgeView.vue"),
    },
    {
      path: "/knowledge/:kbId",
      name: "knowledgeDetail",
      component: () => import("@/views/KnowledgeDetailView.vue"),
    },
    {
      path: "/settings",
      name: "settings",
      component: () => import("@/views/SettingsView.vue"),
    },
  ],
});

// Route guard
router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY);
  const publicPages = ["login"];

  if (!token && !publicPages.includes(to.name as string)) {
    return { name: "login" };
  }

  if (token && to.name === "login") {
    return { name: "chat" };
  }
});

export default router;
