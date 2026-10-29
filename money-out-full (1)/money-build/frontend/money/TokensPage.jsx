import { useState } from "react";
import { moneyApi } from "./moneyApi";
import StaffSelect from "./StaffSelect";
import { Card, Table, Pill, Err, Field, inputStyle, btn, kes, when, useLoad, useAct } from "./ui";

/** Admins nominate and set the amount. Only a Super Admin approves. */
export default function TokensPage({ isAdmin, isSuperAdmin }) {
  const { data, error, reload } = useLoad(moneyApi.tokens);
  const { act, error: aerr, busy } = useAct(reload);
  const [f, setF] = useState({ staffUsername: "", amount: "", reason: "" });
  return (
    <div>
      <Err text={error || aerr} />
      {isAdmin && (
        <Card title="Nominate a teacher for an appreciation token">
          <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "end" }}>
            <Field label="Staff member"><StaffSelect value={f.staffUsername} onChange={(m) => setF({ ...f, staffUsername: m?.username || "" })} /></Field>
            <Field label="Amount"><input style={inputStyle} type="number" value={f.amount} onChange={(e) => setF({ ...f, amount: e.target.value })} /></Field>
            <Field label="Why they deserve it"><input style={{ ...inputStyle, width: 320 }} value={f.reason} onChange={(e) => setF({ ...f, reason: e.target.value })} /></Field>
            <button style={btn()} disabled={busy} onClick={() => act(async () => { await moneyApi.nominateToken({ ...f, amount: Number(f.amount) }); setF({ staffUsername: "", amount: "", reason: "" }); })}>Nominate</button>
          </div>
        </Card>
      )}
      <Card title="Tokens">
        <Table
          head={["Staff", "Amount", "Reason", "Nominated by", "Date", "Status", ""]}
          rows={(data || []).map((t) => [t.staffName, kes(t.amount), t.reason, t.nominatedBy, when(t.nominatedAt), <Pill s={t.status} />,
            t.status === "NOMINATED" && isSuperAdmin ? (<>
              <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.approveToken(t.id))}>Approve</button>
              <button style={btn("danger")} onClick={() => { const r = window.prompt("Reason?"); if (r) act(() => moneyApi.rejectToken(t.id, r)); }}>Reject</button>
            </>) : t.rejectReason || ""])}
        />
        <p style={{ fontSize: 12, color: "#777" }}>Approved tokens are paid with the next payroll and appear on their own line of the payslip.</p>
      </Card>
    </div>
  );
}
