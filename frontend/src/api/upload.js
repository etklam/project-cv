import client, { unwrap } from "@/api/client";

export async function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);
  const response = await client.post("/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return unwrap(response);
}
