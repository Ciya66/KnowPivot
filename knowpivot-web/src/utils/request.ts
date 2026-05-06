import axios from "axios";
import type { ApiResponse } from "@/types/api";

const TOKEN_KEY = "knowpivot_token";

const http = axios.create({
  timeout: 30000,
});

// Request interceptor: inject token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: unwrap ApiResponse or pass through direct data
http.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse<unknown>;

    // If backend returns wrapped ApiResponse { code, data, message }
    if (res && typeof res === "object" && "code" in res) {
      if (res.code === 200) {
        return res.data as never;
      }

      if (res.code === 401) {
        localStorage.removeItem(TOKEN_KEY);
        window.location.href = "/login";
        return Promise.reject(new Error(res.message || "未登录或登录已过期"));
      }

      const errorMap: Record<number, string> = {
        400: "请求参数错误",
        403: "无权访问",
        404: "资源不存在",
        409: "数据冲突",
        429: "请求过于频繁，请稍后再试",
        500: "服务器内部错误",
        50001: "Token 配额不足",
        50002: "业务处理失败",
      };

      const message = res.message || errorMap[res.code] || "请求失败";
      return Promise.reject(new Error(message));
    }

    // If backend returns data directly (no wrapper), pass through
    return response.data as never;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY);
      window.location.href = "/login";
    }
    const message =
      error.response?.data?.message || error.message || "网络错误";
    return Promise.reject(new Error(message));
  },
);

export { TOKEN_KEY };
export default http;
