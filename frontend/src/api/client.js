import axios from "axios";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api/v1",
  withCredentials: true,
});

client.interceptors.request.use((config) => {
  const locale = localStorage.getItem("locale") || "zh-TW";
  config.headers["Accept-Language"] = locale;
  return config;
});

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // Clear auth state and redirect to login
      localStorage.removeItem("auth_token");
      const path = window.location.pathname;
      if (path !== "/login" && path !== "/register") {
        window.location.replace("/login");
      }
    }

    if (status === 402) {
      // Emit event for credit insufficient - components can listen
      window.dispatchEvent(
        new CustomEvent("credit-insufficient", {
          detail: { message: error.response?.data?.message || "Insufficient credits" },
        })
      );
    }

    return Promise.reject(error);
  }
);

export function unwrap(response) {
  if (response?.data?.data !== undefined) {
    return response.data.data;
  }
  return response?.data;
}

export default client;
