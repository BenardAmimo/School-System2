import { useState } from "react";
import { moneyApi } from "./moneyApi";
import { Card, Table, Pill, Err, inputStyle, btn, kes, when, useLoad, useAct } from "./ui";

export default function AdvancesAdminPage({ canApprove }) {
  const { data, error, reload } = useLoad(moneyApi.advances);
  const { act, error: aerr, busy } = useAct(reload);
  const [pay, setPay] = useState({});   // per advance: { amount, reference }
  const field = (id, k) => (e) => setPay({ ...pay, [id]: { ...pay[id], [k]: e.target.value } });
  return (
    <Card title="Salary advances">
      <Err text={error || aerr} />
      <Table
        head={["Staff", "Requested", "Salary", "Amount", "Months", "Repaid", "Outstanding", "Status", ""]}
        rows={(data || []).map((a) => [a.staffName, when(a.requestedAt), kes(a.salaryAtRequest), kes(a.amount), a.instalments, kes(a.repaid), kes(a.outstanding), <Pill s={a.status} />,
          a.status === "REQUESTED" && canApprove ? (<>
            <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.approveAdvance(a.id))}>Approve</button>
            <button style={btn("danger")} onClick={() => { const r = window.prompt("Reason for rejecting?"); if (r) act(() => moneyApi.rejectAdvance(a.id, r)); }}>Reject</button>
          </>) : a.status === "ACTIVE" ? (
            <div style={{ minWidth: 260 }}>
              <input style={{ ...inputStyle, minWidth: 0, width: 90 }} type="number" placeholder="Amount" value={pay[a.id]?.amount || ""} onChange={field(a.id, "amount")} />
              <input style={{ ...inputStyle, minWidth: 0, width: 130 }} placeholder="Receipt / ref" value={pay[a.id]?.reference || ""} onChange={field(a.id, "reference")} />
              <button style={btn()} disabled={busy} onClick={() => act(async () => { await moneyApi.repayAdvance(a.id, Number(pay[a.id]?.amount), pay[a.id]?.reference); setPay({ ...pay, [a.id]: {} }); })}>Record repayment</button>
              {a.repaymentNotes && <div style={{ fontSize: 12, color: "#777" }}>{a.repaymentNotes}</div>}
            </div>
          ) : a.rejectReason || a.repaymentNotes || ""])}
      />
      <p style={{ fontSize: 12, color: "#777" }}>Approval creates a payment order. Repayment is taken automatically from payroll, one instalment a month. Use "Record repayment" when the staff member pays back in cash, by M-Pesa or at the bank; this is refused while an unfinished payroll already takes an instalment.</p>
    </Card>
  );
}
