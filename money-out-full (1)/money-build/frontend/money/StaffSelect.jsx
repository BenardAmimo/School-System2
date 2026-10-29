import { useEffect, useState } from "react";
import { moneyApi, errorText } from "./moneyApi";
import { inputStyle } from "./ui";

let cache = null;

/** Drop-down of everyone who works at the school. value = login email. onChange gets the whole staff member. */
export default function StaffSelect({ value, onChange, placeholder = "Choose a staff member…", style }) {
  const [list, setList] = useState(cache || []);
  const [error, setError] = useState("");

  useEffect(() => {
    if (cache) return;
    moneyApi.staff().then((s) => { cache = s; setList(s); }).catch((e) => setError(errorText(e)));
  }, []);

  return (
    <>
      <select style={{ ...inputStyle, ...style }} value={value || ""} onChange={(e) => onChange(list.find((s) => s.username === e.target.value) || null)}>
        <option value="">{placeholder}</option>
        {list.map((s) => <option key={s.username} value={s.username}>{s.name} ({String(s.role).replaceAll("_", " ").toLowerCase()})</option>)}
      </select>
      {error && <div role="alert" style={{ color: "#8a1f11", fontSize: 12 }}>{error}</div>}
    </>
  );
}
