import client, { unwrap } from "@/api/client";

export const getCreditBalance = async () => unwrap(await client.get("/me/credits/balance"));
export const getCreditTransactions = async (params) =>
  unwrap(await client.get("/me/credits/transactions", { params }));
