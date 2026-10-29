import { useState } from "react";
import { moneyApi } from "./moneyApi";
import StaffSelect from "./StaffSelect";
import { Card, Table, Pill, Err, Field, inputStyle, btn, useLoad, useAct } from "./ui";

const EMPTY = { payeeType: "STAFF", payeeRef: "", displayName: "", channel: "MPESA_B2C", phone: "", businessNumber: "", accountReference: "", bankName: "", bankAccountNumber: "", bankAccountName: "" };

/** How each staff member and supplier wants to be paid. Someone else must verify every change. */
export default function PayeesPage() {
  const { data, error, reload } = useLoad(() => moneyApi.payees());
  const { act, error: aerr, busy } = useAct(reload);
  const [f, setF] = useState(EMPTY);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  return (
    <div>
      <Err text={error || aerr} />
      <Card title="Add or change how someone is paid">
        <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
          <Field label="Who"><select style={inputStyle} value={f.payeeType} onChange={set("payeeType")}><option value="STAFF">Staff member</option><option value="SUPPLIER">Supplier</option></select></Field>
          {f.payeeType === "STAFF" ? (
            <Field label="Staff member"><StaffSelect value={f.payeeRef} onChange={(m) => setF({ ...f, payeeRef: m?.username || "", displayName: m?.name || "" })} /></Field>
          ) : (<>
            <Field label="Supplier id (from the Suppliers list)"><input style={inputStyle} value={f.payeeRef} onChange={set("payeeRef")} /></Field>
            <Field label="Name to show"><input style={inputStyle} value={f.displayName} onChange={set("displayName")} /></Field>
          </>)}
          <Field label="Paid by">
            <select style={inputStyle} value={f.channel} onChange={set("channel")}>
              <option value="MPESA_B2C">M-Pesa to phone</option><option value="MPESA_PAYBILL">M-Pesa Paybill</option><option value="MPESA_TILL">M-Pesa Till</option>
              <option value="BANK">Bank transfer</option><option value="CASH">Cash</option><option value="CHEQUE">Cheque</option>
            </select>
          </Field>
        </div>
        {f.channel === "MPESA_B2C" && <Field label="M-Pesa phone"><input style={inputStyle} placeholder="0712345678" value={f.phone} onChange={set("phone")} /></Field>}
        {f.channel === "MPESA_PAYBILL" && <div style={{ display: "flex", gap: 12 }}>
          <Field label="Paybill number"><input style={inputStyle} value={f.businessNumber} onChange={set("businessNumber")} /></Field>
          <Field label="Account number"><input style={inputStyle} value={f.accountReference} onChange={set("accountReference")} /></Field></div>}
        {f.channel === "MPESA_TILL" && <Field label="Till number"><input style={inputStyle} value={f.businessNumber} onChange={set("businessNumber")} /></Field>}
        {f.channel === "BANK" && <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
          <Field label="Bank"><input style={inputStyle} value={f.bankName} onChange={set("bankName")} /></Field>
          <Field label="Account number"><input style={inputStyle} value={f.bankAccountNumber} onChange={set("bankAccountNumber")} /></Field>
          <Field label="Account name"><input style={inputStyle} value={f.bankAccountName} onChange={set("bankAccountName")} /></Field></div>}
        <button style={btn()} disabled={busy} onClick={() => act(async () => { await moneyApi.savePayee(f); setF(EMPTY); })}>Save (needs verification)</button>
      </Card>

      <Card title="Payment details on file">
        <Table
          head={["Type", "Reference", "Name", "How", "Details", "Verified", ""]}
          rows={(data || []).map((p) => [p.payeeType, p.payeeRef, p.displayName, p.channel.replaceAll("_", " "),
            p.phone || [p.businessNumber, p.accountReference].filter(Boolean).join(" / ") || [p.bankName, p.bankAccountNumber].filter(Boolean).join(" "),
            p.verified ? <Pill s="APPROVED" /> : <Pill s="NEEDS_REVIEW" />,
            !p.verified && <button style={btn()} onClick={() => act(() => moneyApi.verifyPayee(p.id))}>Verify</button>])}
        />
        <p style={{ fontSize: 12, color: "#777" }}>The person who entered the details cannot verify them. Phone-call the supplier or see the cheque book / bank letter first.</p>
      </Card>
    </div>
  );
}
