import client, { unwrap } from "@/api/client";

export const checkUsername = async (username) =>
  unwrap(await client.get("/users/check-username", { params: { username } }));
