import client, { unwrap } from "@/api/client";

export const getRewardSummary = async () => unwrap(await client.get("/me/rewards/summary"));
export const redeemCode = async (payload) => unwrap(await client.post("/me/rewards/redeem", payload));
