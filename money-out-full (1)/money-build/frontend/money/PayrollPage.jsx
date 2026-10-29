import { useState } from "react";
import { moneyApi } from "./moneyApi";
import StaffSelect from "./StaffSelect";
import { Card, Table, Pill, Err, Field, inputStyle, btn, kes, useLoad, useAct } from "./ui";

/** Net salary per staff member, then a monthly payroll: bursar prepares, a different admin approves. */
export default function PayrollPage({ canApprove }) {
  const profiles = useLoad(moneyApi.profiles);
  const runs = useLoad(moneyApi.runs);
  const [open, setOpen] = useState(null);
  const slips = useLoad(() => (open ? moneyApi.payslips(open) : Promise.resolve([])), [open]);
  const reloadAll = async () => { await Promise.all([profiles.reload(), runs.reload(), slips.reload()]); };
  const { act, error, busy } = useAct(reloadAll);
  const [month, setMonth] = useState(new Date().toISOString().slice(0, 7));
  const [s, setS] = useState({ staffUsername: "", staffName: "", netSalary: "", effectiveFrom: new Date().toISOString().slice(0, 10) });
  const [leaver, setLeaver] = useState({ staffUsername: "", lastDay: "" });

  return (
    <div>
      <Err text={error || profiles.error || runs.error} />
      <Card title="Net monthly salary">
        {canApprove && (
          <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "end" }}>
            <Field label="Staff member"><StaffSelect value={s.staffUsername} onChange={(m) => setS({ ...s, staffUsername: m?.username || "", staffName: m?.name || "" })} /></Field>
            <Field label="Net salary"><input style={inputStyle} type="number" value={s.netSalary} onChange={(e) => setS({ ...s, netSalary: e.target.value })} /></Field>
            <Field label="From"><input style={inputStyle} type="date" value={s.effectiveFrom} onChange={(e) => setS({ ...s, effectiveFrom: e.target.value })} /></Field>
            <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.setSalary({ ...s, netSalary: Number(s.netSalary) }))}>Save salary</button>
          </div>
        )}
        <Table head={["Username", "Name", "Net salary", "Effective from"]} rows={(profiles.data || []).map((p) => [p.staffUsername, p.staffName, kes(p.netSalary), p.effectiveFrom])} />
        <p style={{ fontSize: 12, color: "#777" }}>Salary changes keep their history. Only an Admin can change what someone earns. Someone who starts, leaves or gets a raise part-way through a month is paid for the days at each salary.</p>
      </Card>

      {canApprove && (
        <Card title="Someone is leaving">
          <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "end" }}>
            <Field label="Staff member"><StaffSelect value={leaver.staffUsername} onChange={(m) => setLeaver({ ...leaver, staffUsername: m?.username || "" })} /></Field>
            <Field label="Last working day"><input style={inputStyle} type="date" value={leaver.lastDay} onChange={(e) => setLeaver({ ...leaver, lastDay: e.target.value })} /></Field>
            <button style={btn("danger")} disabled={busy || !leaver.staffUsername || !leaver.lastDay}
              onClick={() => window.confirm("Stop their pay after this day? Any advance they still owe stays on record.") && act(async () => { await moneyApi.endEmployment(leaver.staffUsername, leaver.lastDay); setLeaver({ staffUsername: "", lastDay: "" }); })}>Stop pay after this day</button>
          </div>
          <p style={{ fontSize: 12, color: "#777" }}>Their last payroll pays only up to this day and takes back what they still owe on an advance, up to that pay.</p>
        </Card>
      )}

      <Card title="Payroll runs" right={
        <span><input style={{ ...inputStyle, minWidth: 0 }} type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
          <button style={btn()} disabled={busy} onClick={() => act(() => moneyApi.prepareRun(month))}>Prepare / rebuild</button></span>}>
        <Table
          head={["Month", "Total net pay", "Prepared by", "Approved by", "Status", ""]}
          rows={(runs.data || []).map((r) => [r.month, kes(r.totalNetPay), r.preparedBy, r.approvedBy || "", <Pill s={r.status} />,
            <>
              <button style={btn("ghost")} onClick={() => setOpen(open === r.id ? null : r.id)}>{open === r.id ? "Hide" : "Payslips"}</button>
              {r.status === "DRAFT" && canApprove && <button style={btn()} disabled={busy} onClick={() => window.confirm(`Approve payroll for ${r.month}? Payment orders will be created.`) && act(() => moneyApi.approveRun(r.id))}>Approve</button>}
              {r.status === "DRAFT" && <button style={btn("danger")} onClick={() => window.confirm("Delete this draft?") && act(() => moneyApi.deleteRun(r.id))}>Delete</button>}
            </>])}
        />
      </Card>

      {open && (
        <Card title="Payslips">
          <Table
            head={["Staff", "Salary for the month", "Advance recovery", "Token", "Net pay", "Paid"]}
            rows={(slips.data || []).map((p) => [p.staffName, <>{kes(p.netSalary)}{p.fullSalary > 0 && Number(p.fullSalary) !== Number(p.netSalary) && <div style={{ fontSize: 12, color: "#b36b00" }}>part month (full {kes(p.fullSalary)})</div>}</>, p.advanceRecovery > 0 ? "- " + kes(p.advanceRecovery) : "", p.tokenAmount > 0 ? "+ " + kes(p.tokenAmount) : "", <b>{kes(p.netPay)}</b>, p.settled ? "Yes" : "No"])}
          />
        </Card>
      )}
    </div>
  );
}
