import React, { useEffect, useState } from "react";
import { formatSecondsToTimer } from "../../utils/dateFormatter";
import { Clock, AlertTriangle, ShieldAlert } from "lucide-react";

interface CountdownTimerProps {
  targetDate: string;
  label: string;
  subtext?: string;
  isForcedClose?: boolean;
  onExpire?: () => void;
  isTriggerWindowActive?: boolean;
}

export const CountdownTimer: React.FC<CountdownTimerProps> = ({
  targetDate,
  label,
  subtext,
  isForcedClose = false,
  onExpire,
  isTriggerWindowActive = false,
}) => {
  const [secondsRemaining, setSecondsRemaining] = useState<number>(() => {
    const diff = Math.floor(
      (new Date(targetDate).getTime() - Date.now()) / 1000,
    );
    return Math.max(0, diff);
  });

  useEffect(() => {
    const interval = setInterval(() => {
      const diff = Math.floor(
        (new Date(targetDate).getTime() - Date.now()) / 1000,
      );
      const remaining = Math.max(0, diff);
      setSecondsRemaining(remaining);
      if (remaining === 0 && onExpire) {
        onExpire();
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [targetDate, onExpire]);

  const { formatted } = formatSecondsToTimer(secondsRemaining);
  const isExpired = secondsRemaining <= 0;
  const isWarning = !isExpired && isTriggerWindowActive && !isForcedClose;
  const isDanger = !isExpired && isForcedClose && secondsRemaining <= 300;

  return (
    <div
      className={`timer-box ${isWarning ? "warning" : ""} ${isDanger ? "danger" : ""}`}
    >
      <div className="timer-label">
        {isForcedClose ? (
          <ShieldAlert
            size={16}
            className={isDanger ? "text-red-400" : "text-slate-400"}
          />
        ) : isWarning ? (
          <AlertTriangle size={16} className="text-amber-400" />
        ) : (
          <Clock size={16} className="text-blue-400" />
        )}
        {label}
      </div>

      <div className="timer-digits">
        {isExpired ? "00:00 (EXPIRED)" : formatted}
      </div>

      {subtext && <div className="timer-subtext">{subtext}</div>}
    </div>
  );
};
