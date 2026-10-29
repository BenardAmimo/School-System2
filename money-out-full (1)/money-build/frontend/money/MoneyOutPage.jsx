import { useState } from "react";
import MyPayPage from "./MyPayPage";
import OrdersPage from "./OrdersPage";
import PayeesPage from "./PayeesPage";
import PayrollPage from "./PayrollPage";
import AdvancesAdminPage from "./AdvancesAdminPage";
import TokensPage from "./TokensPage";
import ExpensesPage from "./ExpensesPage";
import RefundsPage, { ParentRefunds } from "./RefundsPage";

/**
 * One page for everything money-out. Pass the signed-in user's roles, e.g.
 *   <MoneyOutPage roles={["ADMIN"]} />
 * The server enforces every rule; the roles here only decide which tabs and buttons appear.
 */
export default function MoneyOutPage({ roles = [] }) {
  const has = (r) => roles.some((x) => x === r || x === "ROLE_" + r);
  const superAdmin = has("SUPER_ADMIN");
  const admin = has("ADMIN") || superAdmin;
  const bursar = has("BURSAR");
  const finance = admin || bursar;

  const tabs = [
    ["mypay", "My pay", true, () => <MyPayPage />],
    ["refunds-parent", "My refunds", has("PARENT"), () => <ParentRefunds />],
    ["orders", "Payments", finance, () => <OrdersPage />],
    ["payees", "Payment details", finance, () => <PayeesPage />],
    ["payroll", "Payroll", finance, () => <PayrollPage canApprove={admin} />],
    ["advances", "Advances", finance, () => <AdvancesAdminPage canApprove={admin} />],
    ["tokens", "Tokens", admin, () => <TokensPage isAdmin={has("ADMIN")} isSuperAdmin={superAdmin} />],
    ["expenses", "Purchases", finance, () => <ExpensesPage canApprove={admin} isSuperAdmin={superAdmin} />],
    ["refunds", "Refunds", finance, () => <RefundsPage canApprove={admin} />],
  ].filter((t) => t[2]);

  const [tab, setTab] = useState(tabs[0][0]);
  const current = tabs.find((t) => t[0] === tab) || tabs[0];

  return (
    <div style={{ padding: 16 }}>
      <h2>Money out</h2>
      <nav style={{ display: "flex", gap: 8, flexWrap: "wrap", borderBottom: "1px solid #ddd", marginBottom: 12 }}>
        {tabs.map(([key, label]) => (
          <button key={key} onClick={() => setTab(key)}
            style={{ padding: "8px 14px", border: "none", cursor: "pointer", background: "none", fontWeight: key === current[0] ? 700 : 400, borderBottom: key === current[0] ? "3px solid #1b6fb3" : "3px solid transparent" }}>
            {label}
          </button>
        ))}
      </nav>
      {current[3]()}
    </div>
  );
}
