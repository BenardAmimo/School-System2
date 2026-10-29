import { useState } from "react";
import { moneyApi } from "./moneyApi";
import { Card, Table, Pill, Err, Field, inputStyle, btn, kes, when, useLoad, useAct } from "./ui";

/** Parent view: choose between a refund and carrying the money to the next term. */
export function ParentRefunds() {
  const { data, error, reload } = useLoad(moneyApi.myRefunds);
  const { act, error: aerr, busy } = useAct(reload);
  const open = (data || []).filter((r) => r.status === "AWAITING_PARENT");
  return (
    <div>
      <Err text={error || aerr} />
      {open.map((r) => (
        <Card key={r.id} title={`${r.studentName} has an overpayment of ${kes(r.amount)}`}>
          <p>Please choose what to do with this money. If you do not choose by <b>{when(r.decisionDeadline)}</b>, it will be carried forward to next term's fees.</p>
          <button style={btn()} disabled={busy} onClick={() => window.confirm(`Refund ${kes(r.amount)} to the M-Pesa number(s) that paid?`) && act(() => moneyApi.chooseRefund(r.id, "REFUND"))}>Refund me (to the number that paid)</button>
          <button style={btn("ghost")} disabled={busy} onClick={() => act(() => moneyApi.chooseRefund(r.id, "CARRY_FORWARD"))}>Use it for next term's fees</button>
        </Card>
      ))}
      <Card title="My refund history">
        <Table head={["Student", "Amount", "Choice", "Status"]} rows={(data || []).map((r) => [r.studentName, kes(r.amount), r.choice ? r.choice.replaceAll("_", " ") : "-", <Pill s={r.status} />])} />
      </Card>
    </div>
  );
}

/** Staff view: find overpayments, approve what parents chose. */
export default function RefundsPage({ canApprove }) {
  const { data, error, reload } = useLoad(moneyApi.refunds);
  const { act, error: aerr, busy } = useAct(reload);
  const [termId, setTermId] = useState("");
  const [msg, setMsg] = useState("");
  return (
    <div>
      <Err text={error || aerr} />
      <Card title="Find overpaid students">
        <Field label="Term id"><input style={inputStyle} type="number" value={termId} onChange={(e) => setTermId(e.target.value)} /></Field>
        <button style={btn()} disabled={busy || !termId} onClick={() => act(async () => { const r = await moneyApi.scanRefunds(Number(termId)); setMsg(`${r.created} new overpayment(s) found.`); })}>Scan</button> {msg}
        <p style={{ fontSize: 12, color: "#777" }}>Parents then choose a refund or carry-forward. After the deadline the credit carries forward automatically. Every outcome still needs your approval.</p>
      </Card>
      <Card title="Overpayments">
        <Table
          head={["Student", "Amount", "Parent's choice", "Deadline", "Status", ""]}
          rows={(data || []).map((r) => [r.studentName, kes(r.amount), r.choice ? <>{r.choice.replaceAll("_", " ")}{r.autoDecided && " (automatic)"}</> : "waiting", when(r.decisionDeadline), <Pill s={r.status} />,
            r.status === "AWAITING_APPROVAL" && canApprove ? (<>
              <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.approveRefund(r.id))}>Approve</button>
              <button style={btn("danger")} onClick={() => { const x = window.prompt("Reason?"); if (x) act(() => moneyApi.rejectRefund(r.id, x)); }}>Reject</button>
            </>) : r.rejectReason || ""])}
        />
      </Card>
    </div>
  );
}
