import client from "@/api/client";

export async function exportCvPdf(id) {
  const response = await client.post(`/me/cvs/${id}/export/pdf`, null, {
    responseType: "blob",
  });
  return response.data;
}
