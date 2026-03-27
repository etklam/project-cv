import client, { unwrap } from "@/api/client";

export const getPublicProfile = async (username) =>
  unwrap(await client.get(`/public/${encodeURIComponent(username)}`));

export const getPublicCv = async (username, slug) =>
  unwrap(await client.get(`/public/${encodeURIComponent(username)}/${encodeURIComponent(slug)}`));
