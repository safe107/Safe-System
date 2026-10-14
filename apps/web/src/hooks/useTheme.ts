import { useState } from "react";
export function useTheme() {
  const [theme, setTheme] = useState(
    document.documentElement.dataset.theme || "dark",
  );
  function toggle() {
    const next = theme === "dark" ? "light" : "dark";
    document.documentElement.dataset.theme = next;
    try {
      localStorage.setItem("safe-theme", next);
    } catch {
      /* Storage may be unavailable */
    }
    setTheme(next);
  }
  return { theme, toggle };
}
