import { useState } from "react";
import { moneyApi } from "./moneyApi";
import { Card, Table, Pill, Err, inputStyle, btn, kes, when, useLoad, useAct, download } from "./ui";

const CHANNEL = { MPESA_B2C: "M-Pesa (phone)", MPESA_PAYBILL: "M-Pesa Paybill", MPESA_TILL: "M-Pesa Till", BANK: "Bank", CASH: "Cash", CHEQUE: "Cheque" };

function where(o) {
  switch (o.channel) {
    case "MPESA_B2C": return o.phone;
    case "MPESA_PAYBILL": return `${o.businessNumber} / ${o.accountReference}`;
    case "MPESA_TILL": return `Till ${o.businessNumber}`;
    case "BANK": return `${o.bankName} ${o.bankAccountNumber}`;
    default: return "";
  }
}

/** Where approved payments are actually made. The approver of a payment cannot be the one who pays it. */
export default function OrdersPage() {
  const { data, error, reload } = useLoad(() => moneyApi.orders());
  const { act, error: aerr, busy } = useAct(reload);
  const [picked, setPicked] = useState([]);
  const [ref, setRef] = useState({});
  const [batchRef, setBatchRef] = useState({});
  const orders = data || [];
  const [month, setMonth] = useState(new Date().toISOString().slice(0, 7));
  const feeInfo = useLoad(() => moneyApi.fees(month), [month]);
  const toggle = (id) => setPicked((p) => (p.includes(id) ? p.filter((x) => x !== id) : [...p, id]));
  const batches = [...new Set(orders.filter((o) => o.status === "IN_BATCH").map((o) => o.batchId))];

  const actions = (o) => {
    if (o.status === "APPROVED" && o.channel.startsWith("MPESA")) return <button style={btn()} disabled={busy} onClick={() => window.confirm(`Send ${kes(o.amount)} to ${o.payeeName}?`) && act(() => moneyApi.sendOrder(o.id))}>Send now</button>;
    if ((o.status === "APPROVED") && (o.channel === "CASH" || o.channel === "CHEQUE"))
      return (<>
        <input style={{ ...inputStyle, minWidth: 120 }} placeholder={o.channel === "CHEQUE" ? "Cheque no." : "Voucher no."} value={ref[o.id] || ""} onChange={(e) => setRef({ ...ref, [o.id]: e.target.value })} />
        <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.markOrderPaid(o.id, ref[o.id]))}>Mark paid</button>
      </>);
    if (o.status === "APPROVED" && o.channel === "BANK") return <label><input type="checkbox" checked={picked.includes(o.id)} onChange={() => toggle(o.id)} /> add to bank file</label>;
    if (o.status === "FAILED") return <button style={btn("ghost")} onClick={() => act(() => moneyApi.retryOrder(o.id))}>Retry</button>;
    if (o.status === "NEEDS_REVIEW" || o.status === "SENT")
      return (<>
        <input style={{ ...inputStyle, minWidth: 120 }} placeholder="M-Pesa receipt" value={ref[o.id] || ""} onChange={(e) => setRef({ ...ref, [o.id]: e.target.value })} />
        <button style={btn("ghost")} disabled={busy} onClick={() => act(() => moneyApi.checkStatus(o.id))} title="Safaricom answers in a minute or two; refresh the list">Ask Safaricom</button>
        <button style={btn()} onClick={() => act(() => moneyApi.settleOrder(o.id, true, ref[o.id]))}>It was paid</button>
        <button style={btn("danger")} onClick={() => window.confirm("Only choose this after checking the M-Pesa portal. Continue?") && act(() => moneyApi.settleOrder(o.id, false))}>It was not paid</button>
      </>);
    return null;
  };

  const makeFile = () => act(async () => {
    const res = await moneyApi.bankFile(picked);
    download(res.data, (res.headers["x-batch-id"] || "bank-file") + ".csv");
    setPicked([]);
  });

  return (
    <div>
      <Err text={error || aerr} />
      <Card title="Payment orders" right={picked.length > 0 && <button style={btn()} onClick={makeFile}>Download bank file ({picked.length})</button>}>
        <Table
          head={["#", "For", "Pay to", "How", "Amount", "Status", "Reference / reason", "Action"]}
          rows={orders.map((o) => [o.id, <>{o.purpose}<div style={{ fontSize: 12, color: "#777" }}>{o.description}</div></>, <>{o.payeeName}<div style={{ fontSize: 12, color: "#777" }}>{where(o)}</div></>,
            CHANNEL[o.channel], kes(o.amount), <Pill s={o.status} />, o.reference || o.failureReason || "", actions(o)])}
        />
      </Card>

      <Card title="Safaricom charges (for the expense report)" right={<input style={{ ...inputStyle, minWidth: 0 }} type="month" value={month} onChange={(e) => setMonth(e.target.value)} />}>
        <Err text={feeInfo.error} />
        {feeInfo.data && (
          <p>
            <b>{kes(feeInfo.data.totalFees)}</b> in charges on <b>{feeInfo.data.payments}</b> M-Pesa payment(s) in {month}.
            {Number(feeInfo.data.payments) === 0 && " Enter Safaricom's tariff in application.properties (app.money.fees.tiers) to have charges counted."}
          </p>
        )}
      </Card>

      {batches.map((b) => (
        <Card key={b} title={`Bank file ${b} is waiting for the bank`}>
          <input style={inputStyle} placeholder="Bank batch reference" value={batchRef[b] || ""} onChange={(e) => setBatchRef({ ...batchRef, [b]: e.target.value })} />
          <button style={btn()} onClick={() => act(() => moneyApi.confirmBatch(b, batchRef[b]))}>Bank confirmed it</button>
          <button style={btn("danger")} onClick={() => act(() => moneyApi.releaseBatch(b))}>Bank rejected it</button>
        </Card>
      ))}
    </div>
  );
}
