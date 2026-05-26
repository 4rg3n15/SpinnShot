import { useEffect, useState } from "react";
import axios from "axios";

const API = `${process.env.REACT_APP_BACKEND_URL}/api`;

/**
 * Encapsulates the landing-page data fetching so App.js stays declarative.
 * Re-runs whenever `reloadTick` changes (used by the retry button).
 *
 * Returns `{ leaders, history, stats, error }`.
 */
export default function useLandingData(reloadTick) {
  const [leaders, setLeaders] = useState([]);
  const [history, setHistory] = useState([]);
  const [stats, setStats] = useState({});
  const [error, setError] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      axios.get(`${API}/leaderboard`, { signal: controller.signal }),
      axios.get(`${API}/games?limit=12`, { signal: controller.signal }),
      axios.get(`${API}/categories`, { signal: controller.signal }),
    ])
      .then(([lb, hist, cats]) => {
        const games = hist.data || [];
        const players = new Set();
        games.forEach((g) => g.players.forEach((p) => players.add(p.name)));
        setLeaders(lb.data || []);
        setHistory(games);
        setStats({
          players: players.size,
          games: games.length,
          categories: (cats.data || []).length,
          questions: "50+",
        });
        setError(null);
      })
      .catch((err) => {
        if (axios.isCancel?.(err) || err?.name === "CanceledError") return;
        setError(err?.message || "No pudimos contactar a SpinnShot Cloud.");
      });

    return () => controller.abort();
  }, [reloadTick]);

  return { leaders, history, stats, error };
}
