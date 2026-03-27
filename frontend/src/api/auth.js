import client, { unwrap } from "@/api/client";

export async function login(payload) {
  const response = await client.post("/auth/login", payload);
  return unwrap(response);
}

export async function register(payload) {
  const response = await client.post("/auth/register", payload);
  return unwrap(response);
}

export async function logout() {
  await client.post("/auth/logout");
}

export async function me() {
  const response = await client.get("/auth/me");
  return unwrap(response);
}

export async function changeLocale(payload) {
  const response = await client.put("/auth/change-locale", payload);
  return unwrap(response);
}
