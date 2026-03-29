import client, { unwrap } from "@/api/client";

export async function exportCvPdf(id) {
  const response = await client.post(`/me/cvs/${id}/export/pdf`, null, {
    responseType: "blob",
  });
  return response.data;
}

export async function getExportCv(id, token) {
  return unwrap(await client.get(`/export/print/cvs/${id}`, { params: { token } }));
}
