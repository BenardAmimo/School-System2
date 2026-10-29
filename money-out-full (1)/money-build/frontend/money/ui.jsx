import { useCallback, useEffect, useState } from "react";
import { errorText } from "./moneyApi";

export const kes = (n) =>
  n === null || n === undefined ? "" : "Ksh " + Number(n).toLocaleString("en-KE", { maximumFractionDigits: 2 });

export const when = (s) => (s ? new Date(s).toLocaleString() : "");

/** Loads data and exposes reload + error. */
export function useLoad(fn, deps = []) {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const reload = useCallback(async () => {
    setLoading(true);
    try {
      setData(await fn());
      setError("");
    } catch (e) {
      setError(errorText(e));
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
  useEffect(() => { reload(); }, [reload]);
  return { data, error, loading, reload, setError };
}

/** Runs an action, shows its error, then reloads. */
export function useAct(reload) {
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const act = async (fn) => {
    setBusy(true);
    setError("");
    try {
      const r = await fn();
      await reload?.();
      return r;
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  };
  return { act, error, busy, setError };
}

export const Err = ({ text }) =>
  text ? <div role="alert" style={{ background: "#fdecea", color: "#8a1f11", padding: "8px 12px", borderRadius: 6, margin: "8px 0" }}>{text}</div> : null;

const COLORS = {
  PAID: "#1b7f3b", COMPLETED: "#1b7f3b", ACTIVE: "#1b7f3b", APPROVED: "#1b6fb3",
  FAILED: "#b3261e", REJECTED: "#b3261e", NEEDS_REVIEW: "#b36b00", SENT: "#6b4fbb", IN_BATCH: "#6b4fbb",
};
export const Pill = ({ s }) => (
  <span style={{ background: (COLORS[s] || "#666") + "22", color: COLORS[s] || "#444", padding: "2px 8px", borderRadius: 10, fontSize: 12, whiteSpace: "nowrap" }}>
    {String(s || "").replaceAll("_", " ")}
  </span>
);

export const Card = ({ title, children, right }) => (
  <section style={{ border: "1px solid #ddd", borderRadius: 8, padding: 16, margin: "12px 0" }}>
    {(title || right) && (
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
        <h3 style={{ margin: 0 }}>{title}</h3>
        <div>{right}</div>
      </div>
    )}
    {children}
  </section>
);

export const Table = ({ head, rows, empty = "Nothing here yet." }) => (
  <div style={{ overflowX: "auto" }}>
    <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 14 }}>
      <thead>
        <tr>{head.map((h) => <th key={h} style={{ textAlign: "left", borderBottom: "2px solid #ddd", padding: "6px 8px" }}>{h}</th>)}</tr>
      </thead>
      <tbody>
        {rows.length === 0 && <tr><td colSpan={head.length} style={{ padding: 12, color: "#777" }}>{empty}</td></tr>}
        {rows.map((r, i) => (
          <tr key={i}>{r.map((c, j) => <td key={j} style={{ borderBottom: "1px solid #eee", padding: "6px 8px", verticalAlign: "top" }}>{c}</td>)}</tr>
        ))}
      </tbody>
    </table>
  </div>
);

export const Field = ({ label, children }) => (
  <label style={{ display: "block", margin: "6px 0" }}>
    <div style={{ fontSize: 12, color: "#555" }}>{label}</div>
    {children}
  </label>
);

export const inputStyle = { padding: "6px 8px", border: "1px solid #bbb", borderRadius: 6, minWidth: 180 };
export const btn = (kind = "primary") => ({
  padding: "6px 12px", borderRadius: 6, border: "1px solid transparent", cursor: "pointer", marginRight: 6,
  background: kind === "primary" ? "#1b6fb3" : kind === "danger" ? "#b3261e" : "#eee",
  color: kind === "ghost" ? "#222" : "#fff",
});

export function download(blob, name) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url; a.download = name; a.click();
  URL.revokeObjectURL(url);
}
