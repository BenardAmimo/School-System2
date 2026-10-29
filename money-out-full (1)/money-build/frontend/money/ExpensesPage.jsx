import { useState } from "react";
import { moneyApi } from "./moneyApi";
import { Card, Table, Pill, Err, Field, inputStyle, btn, kes, when, useLoad, useAct } from "./ui";

const TIER = { DIRECT: "One approval", QUOTES: "Quotes needed", TENDER: "Formal tender" };

function ExpenseRow({ e, suppliers, canApprove, isSuperAdmin, act, busy }) {
  const [open, setOpen] = useState(false);
  const quotes = useLoad(() => (open ? moneyApi.quotes(e.id) : Promise.resolve([])), [open, e.status, e.id]);
  const [q, setQ] = useState({ supplierId: "", amount: "", notes: "" });
  const [pick, setPick] = useState({ quoteId: "", justification: "", supplierId: "", amount: "" });
  const [inv, setInv] = useState({ invoiceNumber: "", invoiceAmount: "" });
  const sup = (
    <select style={inputStyle} value={q.supplierId} onChange={(ev) => setQ({ ...q, supplierId: ev.target.value })}>
      <option value="">Supplier…</option>{suppliers.filter((s) => s.active).map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
    </select>
  );
  return (
    <Card title={`${e.title} · ${kes(e.estimatedAmount)}`} right={<Pill s={e.status} />}>
      <div style={{ fontSize: 13, color: "#555" }}>
        {TIER[e.tier]} · requested by {e.requestedBy}
        {e.tier === "TENDER" && <> · bidding closes {when(e.biddingClosesAt)}</>}
        {e.supplierName && <> · supplier <b>{e.supplierName}</b> at <b>{kes(e.awardedAmount)}</b></>}
        {e.firstApprover && <> · approved by {e.firstApprover}{e.finalApprover && e.finalApprover !== e.firstApprover ? ` and ${e.finalApprover}` : ""}</>}
        {e.rejectReason && <> · rejected: {e.rejectReason}</>}
      </div>
      <button style={btn("ghost")} onClick={() => setOpen(!open)}>{open ? "Hide steps" : "Steps"}</button>

      {open && (
        <div style={{ marginTop: 8 }}>
          {e.tier !== "DIRECT" && (
            <>
              <Table head={["Supplier", "Quote", "Notes"]} rows={(quotes.data || []).map((x) => [x.supplierName, kes(x.amount), x.notes])} empty="No quotes yet." />
              {e.status === "REQUESTED" && (
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap", margin: "8px 0" }}>
                  {sup}
                  <input style={inputStyle} type="number" placeholder="Quote amount" value={q.amount} onChange={(ev) => setQ({ ...q, amount: ev.target.value })} />
                  <input style={inputStyle} placeholder="Notes" value={q.notes} onChange={(ev) => setQ({ ...q, notes: ev.target.value })} />
                  <button style={btn()} disabled={busy} onClick={() => act(async () => { await moneyApi.addQuote(e.id, { ...q, amount: Number(q.amount) }); setQ({ supplierId: "", amount: "", notes: "" }); quotes.reload(); })}>Add {e.tier === "TENDER" ? "bid" : "quote"}</button>
                </div>
              )}
            </>
          )}

          {e.status === "REQUESTED" && (
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap", margin: "8px 0" }}>
              {e.tier === "DIRECT" ? (<>
                <select style={inputStyle} value={pick.supplierId} onChange={(ev) => setPick({ ...pick, supplierId: ev.target.value })}>
                  <option value="">Supplier…</option>{suppliers.filter((s) => s.active).map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
                <input style={inputStyle} type="number" placeholder="Agreed price" value={pick.amount} onChange={(ev) => setPick({ ...pick, amount: ev.target.value })} />
              </>) : (
                <select style={inputStyle} value={pick.quoteId} onChange={(ev) => setPick({ ...pick, quoteId: ev.target.value })}>
                  <option value="">Choose winning {e.tier === "TENDER" ? "bid" : "quote"}…</option>
                  {(quotes.data || []).map((x) => <option key={x.id} value={x.id}>{x.supplierName} — {kes(x.amount)}</option>)}
                </select>
              )}
              {e.tier !== "DIRECT" && <input style={{ ...inputStyle, width: 280 }} placeholder="Why, if not the cheapest" value={pick.justification} onChange={(ev) => setPick({ ...pick, justification: ev.target.value })} />}
              <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.award(e.id, { quoteId: pick.quoteId ? Number(pick.quoteId) : null, supplierId: pick.supplierId ? Number(pick.supplierId) : null, amount: pick.amount ? Number(pick.amount) : null, justification: pick.justification }))}>Choose supplier</button>
            </div>
          )}

          {e.status === "AWAITING_APPROVAL" && canApprove && (
            <div>
              <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.approveExpense(e.id))}>
                {e.tier === "TENDER" ? (e.firstApprover ? (isSuperAdmin ? "Give final approval" : "Waiting for a Super Admin") : "Give first approval") : "Approve"}
              </button>
              <button style={btn("danger")} onClick={() => { const r = window.prompt("Reason?"); if (r) act(() => moneyApi.rejectExpense(e.id, r)); }}>Reject</button>
            </div>
          )}

          {e.status === "APPROVED" && (
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              <input style={inputStyle} placeholder="Invoice number" value={inv.invoiceNumber} onChange={(ev) => setInv({ ...inv, invoiceNumber: ev.target.value })} />
              <input style={inputStyle} type="number" placeholder="Invoice amount" value={inv.invoiceAmount} onChange={(ev) => setInv({ ...inv, invoiceAmount: ev.target.value })} />
              <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.receive(e.id, { invoiceNumber: inv.invoiceNumber, invoiceAmount: Number(inv.invoiceAmount) }))}>Goods received, invoice matches</button>
            </div>
          )}

          {e.status === "RECEIVED" && <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.orderExpense(e.id))}>Create payment order</button>}
          {e.status === "PAYMENT_ORDERED" && <p>Payment order created. Pay it from the Payments tab.</p>}
        </div>
      )}
    </Card>
  );
}

export default function ExpensesPage({ canApprove, isSuperAdmin }) {
  const exps = useLoad(moneyApi.expenses);
  const sups = useLoad(moneyApi.suppliers);
  const rules = useLoad(moneyApi.expenseRules);
  const { act, error, busy } = useAct(async () => { await Promise.all([exps.reload(), sups.reload()]); });
  const [f, setF] = useState({ title: "", description: "", category: "", estimatedAmount: "" });
  const [s, setS] = useState({ name: "", contactPerson: "", phone: "", email: "", kraPin: "", active: true });
  const r = rules.data;
  const est = Number(f.estimatedAmount);
  const tier = !r || !est ? "" : est >= r.tenderFrom ? `Formal tender: open for ${r.tenderOpenDays} days, at least ${r.minQuotes} bids, Admin then Super Admin approve.`
    : est >= r.quotesFrom ? `Needs at least ${r.minQuotes} written quotes and one approval.` : "One approval, no quotes needed.";

  return (
    <div>
      <Err text={error || exps.error || sups.error} />
      <Card title="New purchase request">
        <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "end" }}>
          <Field label="What is needed"><input style={inputStyle} value={f.title} onChange={(e) => setF({ ...f, title: e.target.value })} /></Field>
          <Field label="Category"><input style={inputStyle} value={f.category} onChange={(e) => setF({ ...f, category: e.target.value })} /></Field>
          <Field label="Estimated cost"><input style={inputStyle} type="number" value={f.estimatedAmount} onChange={(e) => setF({ ...f, estimatedAmount: e.target.value })} /></Field>
          <button style={btn()} disabled={busy} onClick={() => act(async () => { await moneyApi.createExpense({ ...f, estimatedAmount: est }); setF({ title: "", description: "", category: "", estimatedAmount: "" }); })}>Raise request</button>
        </div>
        {tier && <p><b>Rule for this amount:</b> {tier}</p>}
        {r && <p style={{ fontSize: 12, color: "#777" }}>Quotes from {kes(r.quotesFrom)} · formal tender from {kes(r.tenderFrom)}.</p>}
      </Card>

      {(exps.data || []).map((e) => <ExpenseRow key={e.id} e={e} suppliers={sups.data || []} canApprove={canApprove} isSuperAdmin={isSuperAdmin} act={act} busy={busy} />)}

      <Card title="Suppliers">
        <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "end" }}>
          <Field label="Name"><input style={inputStyle} value={s.name} onChange={(e) => setS({ ...s, name: e.target.value })} /></Field>
          <Field label="Contact"><input style={inputStyle} value={s.contactPerson} onChange={(e) => setS({ ...s, contactPerson: e.target.value })} /></Field>
          <Field label="Phone"><input style={inputStyle} value={s.phone} onChange={(e) => setS({ ...s, phone: e.target.value })} /></Field>
          <Field label="KRA PIN"><input style={inputStyle} value={s.kraPin} onChange={(e) => setS({ ...s, kraPin: e.target.value })} /></Field>
          <button style={btn()} disabled={busy} onClick={() => act(async () => { await moneyApi.saveSupplier(null, s); setS({ name: "", contactPerson: "", phone: "", email: "", kraPin: "", active: true }); })}>Add supplier</button>
        </div>
        <Table head={["Id", "Name", "Contact", "Phone", "KRA PIN"]} rows={(sups.data || []).map((x) => [x.id, x.name, x.contactPerson, x.phone, x.kraPin])} />
        <p style={{ fontSize: 12, color: "#777" }}>After adding a supplier, set how they want to be paid in the Payment details tab (type Supplier, reference = the Id above).</p>
      </Card>
    </div>
  );
}
