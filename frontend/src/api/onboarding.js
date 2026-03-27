import client, { unwrap } from "@/api/client";

export const getOnboardingStatus = async () => unwrap(await client.get("/onboarding/status"));
export const submitStep1 = async (payload) => unwrap(await client.put("/onboarding/step1", payload));
export const submitStep2 = async (payload) => unwrap(await client.put("/onboarding/step2", payload));
export const submitStep3 = async (payload) => unwrap(await client.put("/onboarding/step3", payload));
export const skipStep2 = async () => unwrap(await client.post("/onboarding/skip-step2"));
