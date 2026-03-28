import client, { unwrap } from "./client";

export const adminApi = {
  // Users
  listUsers: (params) => client.get("/admin/users", { params }).then(unwrap),
  getUserDetail: (id) => client.get(`/admin/users/${id}`).then(unwrap),
  updateUserRole: (id, role) => client.put(`/admin/users/${id}/role`, { role }).then(unwrap),
  adjustCredits: (id, delta, reason) =>
    client.post(`/admin/users/${id}/credits/adjust`, { delta, reason }).then(unwrap),

  // PromoCodes
  listPromoCodes: (params) => client.get("/admin/promocodes", { params }).then(unwrap),
  createPromoCode: (data) => client.post("/admin/promocodes", data).then(unwrap),
  updatePromoCode: (id, data) => client.put(`/admin/promocodes/${id}`, data).then(unwrap),
  deletePromoCode: (id) => client.delete(`/admin/promocodes/${id}`).then(unwrap),
  getPromoCodeStats: (id) => client.get(`/admin/promocodes/${id}/stats`).then(unwrap),

  // Templates
  listAllTemplates: () => client.get("/admin/templates").then(unwrap),
  createTemplate: (data) => client.post("/admin/templates", data).then(unwrap),
  updateTemplate: (id, data) => client.put(`/admin/templates/${id}`, data).then(unwrap),
  deleteTemplate: (id) => client.delete(`/admin/templates/${id}`).then(unwrap),
  toggleTemplateStatus: (id, active) => client.put(`/admin/templates/${id}/status`, { active }).then(unwrap),

  // Credits
  listTransactions: (params) => client.get("/admin/credits/transactions", { params }).then(unwrap),
};
