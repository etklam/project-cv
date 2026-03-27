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

export function unwrap(response) {
  if (response?.data?.data !== undefined) {
    return response.data.data;
  }
  return response?.data;
}

export default client;
