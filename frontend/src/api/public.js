import client, { unwrap } from "@/api/client";

export const getPublicProfile = async (email) =>
  unwrap(await client.get(`/public/${encodeURIComponent(email)}`));

export const getPublicCv = async (email, slug) =>
  unwrap(await client.get(`/public/${encodeURIComponent(email)}/${encodeURIComponent(slug)}`));
