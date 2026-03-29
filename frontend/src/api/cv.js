import client, { unwrap } from "@/api/client";

export const listCvs = async () => unwrap(await client.get("/me/cvs"));
export const createCv = async (payload) => unwrap(await client.post("/me/cvs", payload));
export const getCv = async (id) => unwrap(await client.get(`/me/cvs/${id}`));
export const updateCv = async (id, payload) => unwrap(await client.put(`/me/cvs/${id}`, payload));
export const deleteCv = async (id) => unwrap(await client.delete(`/me/cvs/${id}`));
export const updateSections = async (id, payload) =>
  unwrap(await client.put(`/me/cvs/${id}/sections`, payload));
export const saveCvDraft = async (id, payload) =>
  unwrap(await client.put(`/me/cvs/${id}/draft`, payload));
