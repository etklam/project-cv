import client, { unwrap } from "@/api/client";

export const listTemplates = async () => unwrap(await client.get("/templates"));
