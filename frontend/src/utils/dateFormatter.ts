export function formatDateTime(isoString?: string | null): string {
  if (!isoString) return "—";
  try {
    const date = new Date(isoString);
    return date.toLocaleString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: true,
    });
  } catch {
    return isoString;
  }
}

export function formatTimeOnly(isoString?: string | null): string {
  if (!isoString) return "—";
  try {
    const date = new Date(isoString);
    return date.toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: true,
    });
  } catch {
    return isoString;
  }
}

export function formatDateOnly(dateString?: string | null): string {
  if (!dateString) return "—";
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  } catch {
    return dateString;
  }
}

export function formatSecondsToTimer(totalSeconds: number): {
  days: number;
  hours: number;
  minutes: number;
  seconds: number;
  formatted: string;
} {
  const safeSeconds = Math.max(0, Math.floor(totalSeconds));
  const days = Math.floor(safeSeconds / (3600 * 24));
  const hours = Math.floor((safeSeconds % (3600 * 24)) / 3600);
  const minutes = Math.floor((safeSeconds % 3600) / 60);
  const seconds = safeSeconds % 60;

  const pad = (n: number) => n.toString().padStart(2, "0");

  let formatted = `${pad(minutes)}:${pad(seconds)}`;
  if (hours > 0 || days > 0) {
    formatted = `${pad(hours)}:${formatted}`;
  }
  if (days > 0) {
    formatted = `${days}d ${formatted}`;
  }

  return { days, hours, minutes, seconds, formatted };
}
