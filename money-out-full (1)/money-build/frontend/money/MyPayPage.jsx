import { useState } from "react";
import { moneyApi } from "./moneyApi";
import { Card, Table, Pill, Err, Field, inputStyle, btn, kes, when, useLoad, useAct } from "./ui";

/** For every staff member: own payslips, own advance requests, own tokens. */
/** Opens a clean payslip in a new window and prints it (the browser offers "Save as PDF"). */
const esc = (v) => String(v ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

function printSlip(s) {
  const row = (label, value, strong) => `<tr><td>${label}</td><td style="text-align:right;${strong ? "font-weight:700;border-top:2px solid #000" : ""}">${value}</td></tr>`;
  const part = Number(s.fullSalary) > 0 && Number(s.fullSalary) !== Number(s.netSalary);
  const w = window.open("", "_blank", "width=720,height=800");
  if (!w) return;
  w.document.write(`<!doctype html><title>Payslip ${esc(s.month)}</title>
    <style>body{font:14px/1.5 system-ui,sans-serif;max-width:560px;margin:40px auto}td{padding:6px 0}table{width:100%;border-collapse:collapse}h1{font-size:20px;margin:0}small{color:#555}</style>
    <h1>Payslip</h1><small>${esc(s.month)} · ${esc(s.staffName)}</small><hr>
    <table>${row("Net salary" + (part ? " (part month, full " + kes(s.fullSalary) + ")" : ""), kes(s.netSalary))}
    ${Number(s.advanceRecovery) > 0 ? row("Advance repayment", "- " + kes(s.advanceRecovery)) : ""}
    ${Number(s.tokenAmount) > 0 ? row("Appreciation token", "+ " + kes(s.tokenAmount)) : ""}
    ${row("Net pay", kes(s.netPay), true)}</table>
    <p><small>${s.paid ? "Paid." : "Approved, payment pending."}</small></p>`);
  w.document.close();
  w.focus();
  w.print();
}

export default function MyPayPage() {
  const slips = useLoad(moneyApi.myPayslips);
  const adv = useLoad(moneyApi.myAdvances);
  const toks = useLoad(moneyApi.myTokens);
  const { act, error, busy } = useAct(adv.reload);
  const [amount, setAmount] = useState("");
  const [n, setN] = useState(1);
  const [reason, setReason] = useState("");

  const limit = adv.data?.limit ?? 0;
  const list = adv.data?.advances ?? [];

  return (
    <div>
      <Card title="My payslips">
        <Err text={slips.error} />
        <Table
          head={["Month", "Net salary", "Advance recovery", "Appreciation token", "Net pay", "Status", ""]}
          rows={(slips.data || []).map((s) => [s.month, kes(s.netSalary), kes(s.advanceRecovery), kes(s.tokenAmount), <b>{kes(s.netPay)}</b>, <Pill s={s.paid ? "PAID" : "APPROVED"} />,
            <button style={btn("ghost")} onClick={() => printSlip(s)}>Print</button>])}
        />
      </Card>

      <Card title="AdvanceRequest a salary advance">
        {limit > 0 ? (
          <>
            <p>You can borrow up to <b>{kes(limit)}</b> (three quarters of one month's net salary). Repay once or over up to 4 months, taken from your pay.</p>
            <Err text={error} />
            <Field label="Amount (whole shillings)"><input style={inputStyle} type="number" min="1" max={limit} value={amount} onChange={(e) => setAmount(e.target.value)} /></Field>
            <Field label="Repay over">
              <select style={inputStyle} value={n} onChange={(e) => setN(Number(e.target.value))}>
                {[1, 2, 3, 4].map((k) => <option key={k} value={k}>{k === 1 ? "1 month (once)" : `${k} months`}</option>)}
              </select>
            </Field>
            {amount > 0 && <p>Monthly deduction: about <b>{kes(Math.ceil(amount / n))}</b></p>}
            <Field label="Reason"><input style={{ ...inputStyle, width: 320 }} value={reason} onChange={(e) => setReason(e.target.value)} /></Field>
            <button style={btn()} disabled={busy || !amount} onClick={() => act(async () => { await moneyApi.requestAdvance({ amount: Number(amount), instalments: n, reason }); setAmount(""); setReason(""); })}>Send request</button>
          </>
        ) : <p>You have no salary on record, so advances are not available. Ask the Admin.</p>}
      </Card>

      <Card title="My advances">
        <Table
          head={["Requested", "Amount", "Months", "Repaid", "Outstanding", "Status", ""]}
          rows={list.map((a) => [when(a.requestedAt), kes(a.amount), a.instalments, kes(a.repaid), kes(a.outstanding), <Pill s={a.status} />,
            a.status === "REQUESTED" ? <button style={btn("ghost")} onClick={() => act(() => moneyApi.cancelAdvance(a.id))}>Withdraw</button> : (a.rejectReason || "")])}
        />
      </Card>

      <Card title="Appreciation tokens">
        <Err text={toks.error} />
        <Table head={["Date", "Amount", "Reason", "Status"]} rows={(toks.data || []).map((t) => [when(t.nominatedAt), kes(t.amount), t.reason, <Pill s={t.status} />])} />
      </Card>
    </div>
  );
}
