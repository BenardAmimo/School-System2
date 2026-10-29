// All Money-Out calls. If your axios instance is exported differently, change this one import.
import api from "../api";

const d = (p) => p.then((r) => r.data);

export const moneyApi = {
  // staff directory (the pickers)
  staff: () => d(api.get("/money/staff")),

  // payment orders
  checkStatus: (id) => d(api.post(`/money/orders/${id}/check-status`)),
  fees: (month) => d(api.get("/money/orders/fees", { params: { month } })),
  orders: (status) => d(api.get("/money/orders", { params: status ? { status } : {} })),
  sendOrder: (id) => d(api.post(`/money/orders/${id}/send`)),
  markOrderPaid: (id, reference) => d(api.post(`/money/orders/${id}/mark-paid`, { reference })),
  settleOrder: (id, paid, receipt) => d(api.post(`/money/orders/${id}/settle`, { paid, receipt })),
  retryOrder: (id) => d(api.post(`/money/orders/${id}/retry`)),
  bankFile: (ids) => api.post("/money/orders/bank-file", { ids }, { responseType: "blob" }),
  confirmBatch: (batchId, reference) => d(api.post(`/money/orders/batches/${batchId}/confirm`, { reference })),
  releaseBatch: (batchId) => d(api.post(`/money/orders/batches/${batchId}/release`)),

  // payees (how people want to be paid)
  payees: (type) => d(api.get("/money/payees", { params: type ? { type } : {} })),
  savePayee: (body) => d(api.put("/money/payees", body)),
  verifyPayee: (id) => d(api.post(`/money/payees/${id}/verify`)),

  // payroll
  profiles: () => d(api.get("/money/payroll/profiles")),
  setSalary: (body) => d(api.post("/money/payroll/profiles", body)),
  endEmployment: (staffUsername, lastDay) => d(api.post("/money/payroll/profiles/end", { staffUsername, lastDay })),
  runs: () => d(api.get("/money/payroll/runs")),
  payslips: (runId) => d(api.get(`/money/payroll/runs/${runId}/payslips`)),
  prepareRun: (month) => d(api.post("/money/payroll/runs", { month })),
  approveRun: (id) => d(api.post(`/money/payroll/runs/${id}/approve`)),
  deleteRun: (id) => d(api.delete(`/money/payroll/runs/${id}`)),
  myPayslips: () => d(api.get("/money/payroll/my-payslips")),

  // advances
  requestAdvance: (body) => d(api.post("/money/advances", body)),
  myAdvances: () => d(api.get("/money/advances/mine")),
  cancelAdvance: (id) => d(api.post(`/money/advances/${id}/cancel`)),
  advances: () => d(api.get("/money/advances")),
  repayAdvance: (id, amount, reference) => d(api.post(`/money/advances/${id}/repay`, { amount, reference })),
  approveAdvance: (id) => d(api.post(`/money/advances/${id}/approve`)),
  rejectAdvance: (id, reason) => d(api.post(`/money/advances/${id}/reject`, { reason })),

  // tokens
  tokens: () => d(api.get("/money/tokens")),
  myTokens: () => d(api.get("/money/tokens/mine")),
  nominateToken: (body) => d(api.post("/money/tokens", body)),
  approveToken: (id) => d(api.post(`/money/tokens/${id}/approve`)),
  rejectToken: (id, reason) => d(api.post(`/money/tokens/${id}/reject`, { reason })),

  // suppliers and expenses
  suppliers: () => d(api.get("/money/suppliers")),
  saveSupplier: (id, body) => d(id ? api.put(`/money/suppliers/${id}`, body) : api.post("/money/suppliers", body)),
  expenses: () => d(api.get("/money/expenses")),
  expenseRules: () => d(api.get("/money/expenses/rules")),
  createExpense: (body) => d(api.post("/money/expenses", body)),
  quotes: (id) => d(api.get(`/money/expenses/${id}/quotes`)),
  addQuote: (id, body) => d(api.post(`/money/expenses/${id}/quotes`, body)),
  award: (id, body) => d(api.post(`/money/expenses/${id}/award`, body)),
  approveExpense: (id) => d(api.post(`/money/expenses/${id}/approve`)),
  rejectExpense: (id, reason) => d(api.post(`/money/expenses/${id}/reject`, { reason })),
  receive: (id, body) => d(api.post(`/money/expenses/${id}/receive`, body)),
  orderExpense: (id) => d(api.post(`/money/expenses/${id}/order`)),

  // refunds
  scanRefunds: (termId) => d(api.post("/money/refunds/scan", { termId })),
  refunds: () => d(api.get("/money/refunds")),
  myRefunds: () => d(api.get("/money/refunds/mine")),
  chooseRefund: (id, choice) => d(api.post(`/money/refunds/${id}/choose`, { choice })),
  approveRefund: (id) => d(api.post(`/money/refunds/${id}/approve`)),
  rejectRefund: (id, reason) => d(api.post(`/money/refunds/${id}/reject`, { reason })),
};

export const errorText = (e) =>
  e?.response?.data?.message || e?.response?.data?.error || e?.message || "Something went wrong";
