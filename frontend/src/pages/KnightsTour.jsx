import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

const BOARD_SIZES = [8, 16];
const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export default function KnightsTour() {
  const navigate = useNavigate();
  const canvasRef = useRef(null);
  const hubRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });

  const [playerName, setPlayerName] = useState("");
  const [boardSize, setBoardSize] = useState(8);
  const [gameState, setGameState] = useState("setup");
  const [startPos, setStartPos] = useState({ row: 0, col: 0 });
  const [solution, setSolution] = useState([]);
  const [playerAnswer, setPlayerAnswer] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [algo1Time, setAlgo1Time] = useState(null);
  const [algo2Time, setAlgo2Time] = useState(null);
  const [correctAnswer, setCorrectAnswer] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [showLeaderboard, setShowLeaderboard] = useState(false);
  const [leaderboardLoading, setLeaderboardLoading] = useState(false);

  // Visualization states
  const [showVisualization, setShowVisualization] = useState(false);
  const [vizStep, setVizStep] = useState(0);
  const [vizPlaying, setVizPlaying] = useState(false);
  const [vizSpeed, setVizSpeed] = useState(300);
  const vizIntervalRef = useRef(null);

  // Dashboard states
  const [showDashboard, setShowDashboard] = useState(false);
  const [dashboardData, setDashboardData] = useState([]);
  const [dashboardLoading, setDashboardLoading] = useState(false);

  useEffect(() => {
    const canvas = canvasRef.current;
    const hub = hubRef.current;
    if (!canvas || !hub) return;
    const ctx = canvas.getContext("2d");
    let animId;
    const NUM = 120;
    const SPEED = 2;
    const FOV = 300;
    let W, H, stars = [];

    function makeStar() {
      return { x: (Math.random()-0.5)*W*3, y: (Math.random()-0.5)*H*3, z: Math.random()*W, pz: 0 };
    }
    function resize() {
      W = canvas.width = hub.offsetWidth;
      H = canvas.height = hub.offsetHeight;
    }
    function draw() {
      ctx.fillStyle = "#050510";
      ctx.fillRect(0, 0, W, H);
      const cx = W/2 + mouse.current.x * 30;
      const cy = H/2 + mouse.current.y * 20;
      for (let s of stars) {
        s.pz = s.z; s.z -= SPEED;
        if (s.z <= 0) { Object.assign(s, makeStar()); s.z = W; s.pz = W; }
        const sx = (s.x/s.z)*FOV+cx, sy = (s.y/s.z)*FOV+cy;
        const px = (s.x/s.pz)*FOV+cx, py = (s.y/s.pz)*FOV+cy;
        const size = Math.max(0.3, (1-s.z/W)*2.5);
        const bright = Math.floor((1-s.z/W)*220+35);
        const alpha = (1-s.z/W)*0.9+0.1;
        ctx.beginPath(); ctx.moveTo(px, py); ctx.lineTo(sx, sy);
        ctx.strokeStyle = `rgba(${bright},${bright},${Math.min(255,bright+40)},${alpha})`;
        ctx.lineWidth = size; ctx.stroke();
      }
      animId = requestAnimationFrame(draw);
    }
    const onMouseMove = (e) => {
      const r = hub.getBoundingClientRect();
      mouse.current.x = (e.clientX-r.left-W/2)/W;
      mouse.current.y = (e.clientY-r.top-H/2)/H;
    };
    resize();
    stars = Array.from({length: NUM}, makeStar);
    draw();
    window.addEventListener("resize", resize);
    hub.addEventListener("mousemove", onMouseMove);
    return () => { cancelAnimationFrame(animId); window.removeEventListener("resize", resize); hub.removeEventListener("mousemove", onMouseMove); };
  }, []);

  // Visualization auto-play
  useEffect(() => {
    if (vizPlaying) {
      vizIntervalRef.current = setInterval(() => {
        setVizStep(prev => {
          if (prev >= solution.length - 1) {
            setVizPlaying(false);
            return prev;
          }
          return prev + 1;
        });
      }, vizSpeed);
    }
    return () => clearInterval(vizIntervalRef.current);
  }, [vizPlaying, vizSpeed, solution.length]);

  const startGame = async () => {
    if (!playerName.trim()) return alert("Please enter your name!");
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/knights-tour/generate?boardSize=${boardSize}`);
      const data = await res.json();
      if (data.error) return alert(data.error);
      setStartPos({ row: data.startRow, col: data.startCol });
      setSolution(data.solution);
      setAlgo1Time(data.algo1TimeMs);
      setAlgo2Time(data.algo2TimeMs);
      setCorrectAnswer(data.totalMoves);
      setGameState("playing");
    } catch {
      alert("Could not connect to server. Make sure Spring Boot is running!");
    }
    setLoading(false);
  };

  const submitAnswer = async () => {
    if (!playerAnswer.trim()) return alert("Please enter your answer!");
    if (isNaN(playerAnswer.trim())) return alert("Please enter a valid number!");
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/knights-tour/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          playerName, boardSize,
          startRow: startPos.row, startCol: startPos.col,
          playerAnswer, algo1TimeMs: algo1Time, algo2TimeMs: algo2Time,
        }),
      });
      const data = await res.json();
      if (data.error) return alert(data.error);
      setResult(data.gameResult);
      setAlgo1Time(data.algo1TimeMs);
      setAlgo2Time(data.algo2TimeMs);
      setCorrectAnswer(data.correctAnswer);
      setGameState("result");
    } catch {
      alert("Could not connect to server!");
    }
    setLoading(false);
  };

  const giveUp = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/knights-tour/draw`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          playerName, boardSize,
          startRow: startPos.row, startCol: startPos.col,
          algo1TimeMs: algo1Time, algo2TimeMs: algo2Time,
        }),
      });
      const data = await res.json();
      if (data.error) return alert(data.error);
      setResult("draw");
      setCorrectAnswer(data.correctAnswer);
      setGameState("result");
    } catch {
      alert("Could not connect to server!");
    }
    setLoading(false);
  };

  const fetchLeaderboard = async () => {
    setLeaderboardLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/knights-tour/leaderboard`);
      const data = await res.json();
      setLeaderboard(data);
      setShowLeaderboard(true);
    } catch {
      alert("Could not fetch leaderboard!");
    }
    setLeaderboardLoading(false);
  };

  const fetchDashboard = async () => {
    setDashboardLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/knights-tour/results`);
      const data = await res.json();
      setDashboardData(data);
      setShowDashboard(true);
    } catch {
      alert("Could not fetch dashboard data!");
    }
    setDashboardLoading(false);
  };

  const openVisualization = () => {
    setVizStep(0);
    setVizPlaying(false);
    setShowVisualization(true);
  };

  const resetGame = () => {
    setGameState("setup");
    setPlayerAnswer("");
    setResult(null);
    setSolution([]);
    setShowLeaderboard(false);
    setShowVisualization(false);
    setShowDashboard(false);
    setVizStep(0);
    setVizPlaying(false);
  };

  // Get explanation for current viz step
  const getVizExplanation = (step) => {
    if (!solution[step]) return "";
    const [row, col] = solution[step];
    if (step === 0) return `🟦 Knight starts at position (${row + 1}, ${col + 1}). Warnsdorff's rule: always move to the square with the fewest onward moves.`;
    if (step === solution.length - 1) return `🏁 Knight completes the tour! All ${solution.length} squares visited exactly once. Warnsdorff's algorithm succeeded!`;
    const prev = solution[step - 1];
    return `♞ Move ${step + 1}: Knight moves from (${prev[0]+1}, ${prev[1]+1}) → (${row+1}, ${col+1}). Warnsdorff's picks the square with minimum onward moves to avoid dead ends.`;
  };

  const cellSize = boardSize === 8 ? 44 : 24;
  const vizCellSize = boardSize === 8 ? 48 : 26;

  const resultConfig = {
    win:  { emoji: "🏆", color: "#4ade80", title: "Correct! Well done!", msg: `Great job, ${playerName}! Your answer has been saved.` },
    lose: { emoji: "😞", color: "#f87171", title: "Not quite right!", msg: `The correct answer was ${correctAnswer} moves. Keep trying!` },
    draw: { emoji: "🤝", color: "#facc15", title: "You gave up!", msg: `No worries, ${playerName}! The correct answer was ${correctAnswer} moves.` },
  };

  // Max algo time for bar chart scaling
  const maxTime = dashboardData.length > 0 ? Math.max(...dashboardData.map(d => Math.max(d.algo1TimeMs || 0, d.algo2TimeMs || 0)), 1) : 1;

  return (
    <div ref={hubRef} style={{ background: "#050510", minHeight: "100vh", position: "relative", overflow: "hidden", fontFamily: "sans-serif" }}>
      <canvas ref={canvasRef} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", pointerEvents: "none" }} />

      <div style={{ position: "relative", zIndex: 1, padding: "2rem 1.5rem" }}>

        {/* Header */}
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "2rem", flexWrap: "wrap", gap: "8px" }}>
          <button onClick={() => navigate("/")} style={{ background: "rgba(255,255,255,0.1)", border: "1px solid rgba(255,255,255,0.2)", color: "#fff", padding: "8px 16px", borderRadius: "20px", cursor: "pointer", fontSize: "13px" }}>
            ← Back
          </button>
          <div style={{ display: "flex", gap: "8px" }}>
            <button onClick={fetchDashboard} disabled={dashboardLoading}
              style={{ background: "rgba(255,255,255,0.1)", border: "1px solid rgba(255,255,255,0.2)", color: "#fff", padding: "8px 16px", borderRadius: "20px", cursor: "pointer", fontSize: "13px" }}>
              {dashboardLoading ? "Loading..." : "📊 Dashboard"}
            </button>
            <button onClick={fetchLeaderboard} disabled={leaderboardLoading}
              style={{ background: "rgba(255,255,255,0.1)", border: "1px solid rgba(255,255,255,0.2)", color: "#fff", padding: "8px 16px", borderRadius: "20px", cursor: "pointer", fontSize: "13px" }}>
              {leaderboardLoading ? "Loading..." : "🏅 Leaderboard"}
            </button>
          </div>
        </div>

        <div style={{ textAlign: "center", marginBottom: "2rem" }}>
          <div style={{ fontSize: "48px", marginBottom: "8px" }}>♟</div>
          <h1 style={{ fontSize: "28px", fontWeight: 500, color: "#fff", letterSpacing: "-0.5px" }}>Knight's Tour</h1>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.45)", marginTop: "6px" }}>Visit every square exactly once</p>
        </div>

        {/* ─── LEADERBOARD MODAL ─── */}
        {showLeaderboard && (
          <div style={{ position: "fixed", top: 0, left: 0, width: "100%", height: "100%", background: "rgba(0,0,0,0.85)", zIndex: 100, display: "flex", alignItems: "center", justifyContent: "center" }}>
            <div style={{ background: "#0d1b2a", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "2rem", maxWidth: "500px", width: "90%", maxHeight: "80vh", overflowY: "auto" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
                <h2 style={{ color: "#fff", fontSize: "20px", fontWeight: 500 }}>🏅 Leaderboard</h2>
                <button onClick={() => setShowLeaderboard(false)} style={{ background: "transparent", border: "none", color: "rgba(255,255,255,0.5)", fontSize: "20px", cursor: "pointer" }}>✕</button>
              </div>
              {leaderboard.length === 0 ? (
                <p style={{ color: "rgba(255,255,255,0.5)", textAlign: "center" }}>No correct answers yet!</p>
              ) : leaderboard.map((entry, i) => (
                <div key={i} style={{ background: i === 0 ? "rgba(250,204,21,0.1)" : "rgba(255,255,255,0.05)", border: i === 0 ? "1px solid rgba(250,204,21,0.3)" : "none", borderRadius: "10px", padding: "12px 16px", marginBottom: "8px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <div>
                    <p style={{ color: i === 0 ? "#facc15" : "#fff", fontSize: "14px", fontWeight: 500 }}>{i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : `#${i+1}`} {entry.playerName}</p>
                    <p style={{ color: "rgba(255,255,255,0.4)", fontSize: "12px" }}>{entry.boardSize}×{entry.boardSize} board</p>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <p style={{ color: "#4ade80", fontSize: "12px" }}>Warnsdorff: {entry.algo1TimeMs}ms</p>
                    <p style={{ color: "#60a5fa", fontSize: "12px" }}>Backtracking: {entry.algo2TimeMs > 0 ? `${entry.algo2TimeMs}ms` : "N/A"}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ─── DASHBOARD MODAL ─── */}
        {showDashboard && (
          <div style={{ position: "fixed", top: 0, left: 0, width: "100%", height: "100%", background: "rgba(0,0,0,0.85)", zIndex: 100, display: "flex", alignItems: "center", justifyContent: "center" }}>
            <div style={{ background: "#0d1b2a", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "2rem", maxWidth: "700px", width: "95%", maxHeight: "85vh", overflowY: "auto" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
                <div>
                  <h2 style={{ color: "#fff", fontSize: "20px", fontWeight: 500 }}>📊 Algorithm Performance Dashboard</h2>
                  <p style={{ color: "rgba(255,255,255,0.4)", fontSize: "12px", marginTop: "4px" }}>All {dashboardData.length} game rounds</p>
                  </div>
                <button onClick={() => setShowDashboard(false)} style={{ background: "transparent", border: "none", color: "rgba(255,255,255,0.5)", fontSize: "20px", cursor: "pointer" }}>✕</button>
              </div>

              {/* Legend */}
              <div style={{ display: "flex", gap: "16px", marginBottom: "1rem" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <div style={{ width: "12px", height: "12px", borderRadius: "2px", background: "#4ade80" }} />
                  <span style={{ color: "rgba(255,255,255,0.7)", fontSize: "12px" }}>Warnsdorff's (Iterative)</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <div style={{ width: "12px", height: "12px", borderRadius: "2px", background: "#60a5fa" }} />
                  <span style={{ color: "rgba(255,255,255,0.7)", fontSize: "12px" }}>Backtracking (Recursive)</span>
                </div>
              </div>

              {/* Bar Chart */}
              {dashboardData.length === 0 ? (
                <p style={{ color: "rgba(255,255,255,0.5)", textAlign: "center", padding: "2rem" }}>No game data yet! Play some rounds first.</p>
              ) : (
                <div style={{ overflowX: "auto" }}>
                  <div style={{ display: "flex", alignItems: "flex-end", gap: "8px", minWidth: `${dashboardData.length * 50}px`, height: "200px", padding: "0 8px", borderBottom: "1px solid rgba(255,255,255,0.1)", borderLeft: "1px solid rgba(255,255,255,0.1)" }}>
                    {dashboardData.map((round, i) => (
                      <div key={i} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "2px", flex: 1, minWidth: "40px" }}>
                        <div style={{ display: "flex", alignItems: "flex-end", gap: "2px", height: "180px" }}>
                          {/* Warnsdorff bar */}
                          <div title={`Warnsdorff: ${round.algo1TimeMs}ms`}
                            style={{ width: "14px", height: `${Math.max(4, (round.algo1TimeMs / maxTime) * 180)}px`, background: "#4ade80", borderRadius: "2px 2px 0 0", transition: "height 0.3s" }} />
                          {/* Backtracking bar */}
                          <div title={`Backtracking: ${round.algo2TimeMs}ms`}
                            style={{ width: "14px", height: `${Math.max(4, (round.algo2TimeMs / maxTime) * 180)}px`, background: "#60a5fa", borderRadius: "2px 2px 0 0", transition: "height 0.3s" }} />
                        </div>
                        <span style={{ color: "rgba(255,255,255,0.3)", fontSize: "10px" }}>R{i + 1}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Summary Stats */}
              {dashboardData.length > 0 && (
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px", marginTop: "1.5rem" }}>
                  <div style={{ background: "rgba(74,222,128,0.1)", border: "1px solid rgba(74,222,128,0.2)", borderRadius: "10px", padding: "12px" }}>
                    <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "11px", marginBottom: "4px" }}>Avg Warnsdorff's Time</p>
                    <p style={{ color: "#4ade80", fontSize: "20px", fontWeight: 600 }}>
                      {Math.round(dashboardData.reduce((a, b) => a + (b.algo1TimeMs || 0), 0) / dashboardData.length)}ms
                    </p>
                    <p style={{ color: "rgba(255,255,255,0.3)", fontSize: "11px", marginTop: "4px" }}>Iterative — always completes</p>
                  </div>
                  <div style={{ background: "rgba(96,165,250,0.1)", border: "1px solid rgba(96,165,250,0.2)", borderRadius: "10px", padding: "12px" }}>
                    <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "11px", marginBottom: "4px" }}>Avg Backtracking Time</p>
                    <p style={{ color: "#60a5fa", fontSize: "20px", fontWeight: 600 }}>
                      {Math.round(dashboardData.filter(d => d.algo2TimeMs > 0).reduce((a, b) => a + (b.algo2TimeMs || 0), 0) / Math.max(1, dashboardData.filter(d => d.algo2TimeMs > 0).length))}ms
                    </p>
                    <p style={{ color: "rgba(255,255,255,0.3)", fontSize: "11px", marginTop: "4px" }}>Recursive — 5s timeout on 8×8</p>
                  </div>
                  <div style={{ background: "rgba(255,255,255,0.05)", borderRadius: "10px", padding: "12px" }}>
                    <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "11px", marginBottom: "4px" }}>Total Rounds</p>
                    <p style={{ color: "#fff", fontSize: "20px", fontWeight: 600 }}>{dashboardData.length}</p>
                  </div>
                  <div style={{ background: "rgba(255,255,255,0.05)", borderRadius: "10px", padding: "12px" }}>
                    <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "11px", marginBottom: "4px" }}>Correct Answers</p>
                    <p style={{ color: "#facc15", fontSize: "20px", fontWeight: 600 }}>{dashboardData.filter(d => d.correct).length}</p>
                  </div>
                </div>
              )}

              {/* Algorithm Explanation */}
              <div style={{ marginTop: "1.5rem", background: "rgba(255,255,255,0.03)", borderRadius: "10px", padding: "1rem" }}>
                <p style={{ color: "rgba(255,255,255,0.7)", fontSize: "13px", fontWeight: 500, marginBottom: "8px" }}>📚 Why is Warnsdorff's faster?</p>
                <p style={{ color: "rgba(255,255,255,0.4)", fontSize: "12px", lineHeight: "1.6" }}>
                  Warnsdorff's is a <strong style={{ color: "rgba(255,255,255,0.6)" }}>heuristic iterative</strong> algorithm — it picks the next move greedily based on the minimum number of onward moves (degree). This avoids dead ends and runs in <strong style={{ color: "#4ade80" }}>O(n²)</strong> time.
                  Backtracking is <strong style={{ color: "rgba(255,255,255,0.6)" }}>recursive</strong> and explores all possible paths, giving it a worst-case complexity of <strong style={{ color: "#f87171" }}>O(8^n)</strong> — exponential. That's why we limit it to 5 seconds.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* ─── VISUALIZATION MODAL ─── */}
        {showVisualization && (
          <div style={{ position: "fixed", top: 0, left: 0, width: "100%", height: "100%", background: "rgba(0,0,0,0.92)", zIndex: 100, display: "flex", alignItems: "center", justifyContent: "center", padding: "1rem" }}>
            <div style={{ background: "#0a1628", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "1.5rem", maxWidth: "600px", width: "100%", maxHeight: "95vh", overflowY: "auto" }}>

              {/* Viz Header */}
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                <div>
                  <h2 style={{ color: "#fff", fontSize: "18px", fontWeight: 500 }}>🎯 Warnsdorff's Algorithm — Step by Step</h2>
                  <p style={{ color: "rgba(255,255,255,0.4)", fontSize: "12px", marginTop: "2px" }}>
                    Move {vizStep + 1} of {solution.length}
                  </p>
                </div>
                <button onClick={() => { setShowVisualization(false); setVizPlaying(false); }}
                  style={{ background: "transparent", border: "none", color: "rgba(255,255,255,0.5)", fontSize: "20px", cursor: "pointer" }}>✕</button>
              </div>

              {/* Explanation Box */}
              <div style={{ background: "rgba(55,138,221,0.15)", border: "1px solid rgba(55,138,221,0.3)", borderRadius: "10px", padding: "12px", marginBottom: "1rem", minHeight: "60px" }}>
                <p style={{ color: "rgba(255,255,255,0.9)", fontSize: "13px", lineHeight: "1.6" }}>
                  {getVizExplanation(vizStep)}
                </p>
              </div>

              {/* Progress Bar */}
              <div style={{ background: "rgba(255,255,255,0.1)", borderRadius: "4px", height: "6px", marginBottom: "1rem" }}>
                <div style={{ background: "#378ADD", height: "100%", borderRadius: "4px", width: `${((vizStep + 1) / solution.length) * 100}%`, transition: "width 0.3s" }} />
              </div>

              {/* Visualization Board */}
              <div style={{ display: "flex", justifyContent: "center", marginBottom: "1rem" }}>
                <div style={{ border: "2px solid rgba(255,255,255,0.2)", borderRadius: "8px", overflow: "hidden" }}>
                  {Array.from({ length: boardSize }, (_, row) => (
                    <div key={row} style={{ display: "flex" }}>
                      {Array.from({ length: boardSize }, (_, col) => {
                        const visitedIndex = solution.slice(0, vizStep + 1).findIndex(pos => pos[0] === row && pos[1] === col);
                        const isCurrent = solution[vizStep] && solution[vizStep][0] === row && solution[vizStep][1] === col;
                        const isVisited = visitedIndex !== -1;
                        const isStart = row === startPos.row && col === startPos.col;
                        const isLight = (row + col) % 2 === 0;

                        // Color logic
                        let bg = isLight ? "#f0d9b5" : "#b58863";
                        if (isVisited && !isCurrent) {
                          const progress = visitedIndex / solution.length;
                          bg = `rgba(${Math.round(96 + (74-96)*progress)}, ${Math.round(165 + (222-165)*progress)}, ${Math.round(250 + (128-250)*progress)}, 0.8)`;
                        }
                        if (isCurrent) bg = "#facc15";
                        if (isStart && vizStep === 0) bg = "#378ADD";

                        return (
                          <div key={col} style={{
                            width: vizCellSize, height: vizCellSize,
                            background: bg,
                            display: "flex", alignItems: "center", justifyContent: "center",
                            position: "relative",
                            border: isCurrent ? "2px solid #fff" : "none",
                            transition: "background 0.2s",
                          }}>
                            {isCurrent && <span style={{ fontSize: boardSize === 8 ? "20px" : "12px" }}>♞</span>}
                            {isVisited && !isCurrent && (
                              <span style={{ fontSize: boardSize === 8 ? "10px" : "7px", color: "rgba(0,0,0,0.7)", fontWeight: 600 }}>
                                {visitedIndex + 1}
                              </span>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  ))}
                </div>
              </div>

              {/* Controls */}
              <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "1rem", flexWrap: "wrap" }}>
                <button onClick={() => { setVizPlaying(false); setVizStep(0); }}
                  style={{ padding: "8px 12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", cursor: "pointer", fontSize: "16px" }}>
                  ⏮
                </button>
                <button onClick={() => setVizStep(prev => Math.max(0, prev - 1))} disabled={vizStep === 0}
                  style={{ padding: "8px 12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", cursor: "pointer", fontSize: "16px" }}>
                  ◀
                </button>
                <button onClick={() => setVizPlaying(p => !p)}
                  style={{ flex: 1, padding: "8px 16px", borderRadius: "8px", border: "none", background: vizPlaying ? "#f87171" : "#4ade80", color: "#000", cursor: "pointer", fontSize: "14px", fontWeight: 600 }}>
                  {vizPlaying ? "⏸ Pause" : vizStep >= solution.length - 1 ? "↺ Replay" : "▶ Play"}
                </button>
                <button onClick={() => setVizStep(prev => Math.min(solution.length - 1, prev + 1))} disabled={vizStep >= solution.length - 1}
                  style={{ padding: "8px 12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", cursor: "pointer", fontSize: "16px" }}>
                  ▶
                </button>
                <button onClick={() => { setVizPlaying(false); setVizStep(solution.length - 1); }}
                  style={{ padding: "8px 12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", cursor: "pointer", fontSize: "16px" }}>
                  ⏭
                </button>
              </div>

              {/* Speed Control */}
              <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "1rem" }}>
                <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "12px" }}>🐢 Slow</span>
                <input type="range" min="50" max="800" value={800 - vizSpeed + 50}
                  onChange={e => setVizSpeed(800 - parseInt(e.target.value) + 50)}
                  style={{ flex: 1, accentColor: "#378ADD" }} />
                <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "12px" }}>⚡ Fast</span>
              </div>

              {/* Color Legend */}
              <div style={{ display: "flex", gap: "12px", flexWrap: "wrap" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <div style={{ width: "14px", height: "14px", background: "#facc15", borderRadius: "3px" }} />
                  <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "11px" }}>Current position</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <div style={{ width: "14px", height: "14px", background: "#60a5fa", borderRadius: "3px" }} />
                  <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "11px" }}>Visited squares</span>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                  <div style={{ width: "14px", height: "14px", background: "#f0d9b5", borderRadius: "3px" }} />
                  <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "11px" }}>Unvisited squares</span>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ─── SETUP SCREEN ─── */}
        {gameState === "setup" && (
          <div style={{ maxWidth: "400px", margin: "0 auto", background: "rgba(13,59,110,0.6)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "2rem" }}>
            <div style={{ marginBottom: "1.5rem" }}>
              <label style={{ display: "block", color: "rgba(255,255,255,0.7)", fontSize: "13px", marginBottom: "8px" }}>Your name</label>
              <input value={playerName} onChange={e => setPlayerName(e.target.value)} placeholder="Enter your name"
                style={{ width: "100%", padding: "10px 14px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "rgba(255,255,255,0.08)", color: "#fff", fontSize: "14px", outline: "none", boxSizing: "border-box" }} />
            </div>
            <div style={{ marginBottom: "1.5rem" }}>
              <label style={{ display: "block", color: "rgba(255,255,255,0.7)", fontSize: "13px", marginBottom: "8px" }}>Board size</label>
              <div style={{ display: "flex", gap: "10px" }}>
                {BOARD_SIZES.map(size => (
                  <button key={size} onClick={() => setBoardSize(size)}
                    style={{ flex: 1, padding: "10px", borderRadius: "8px", border: `1px solid ${boardSize === size ? "#378ADD" : "rgba(255,255,255,0.2)"}`, background: boardSize === size ? "rgba(55,138,221,0.3)" : "rgba(255,255,255,0.05)", color: "#fff", cursor: "pointer", fontSize: "14px", fontWeight: boardSize === size ? 500 : 400 }}>
                    {size}×{size}
                  </button>
                ))}
              </div>
            </div>
            <button onClick={startGame} disabled={loading}
              style={{ width: "100%", padding: "12px", borderRadius: "8px", border: "none", background: "#185FA5", color: "#fff", fontSize: "15px", fontWeight: 500, cursor: "pointer" }}>
              {loading ? "Loading..." : "Start Game"}
            </button>
          </div>
        )}

        {/* ─── PLAYING SCREEN ─── */}
        {gameState === "playing" && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1.5rem" }}>
            <div style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "12px", padding: "12px 24px", color: "rgba(255,255,255,0.8)", fontSize: "14px" }}>
              Knight starts at row <strong style={{ color: "#fff" }}>{startPos.row + 1}</strong>, column <strong style={{ color: "#fff" }}>{startPos.col + 1}</strong>
            </div>

            <div style={{ border: "2px solid rgba(255,255,255,0.15)", borderRadius: "8px", overflow: "hidden" }}>
              {Array.from({ length: boardSize }, (_, row) => (
                <div key={row} style={{ display: "flex" }}>
                  {Array.from({ length: boardSize }, (_, col) => {
                    const isStart = row === startPos.row && col === startPos.col;
                    const isLight = (row + col) % 2 === 0;
                    return (
                      <div key={col} style={{ width: cellSize, height: cellSize, background: isStart ? "#378ADD" : isLight ? "#f0d9b5" : "#b58863", display: "flex", alignItems: "center", justifyContent: "center", fontSize: isStart ? (boardSize === 8 ? "22px" : "14px") : "0" }}>
                        {isStart ? "♞" : ""}
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>

            <div style={{ maxWidth: "400px", width: "100%", background: "rgba(13,59,110,0.6)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "1.5rem" }}>
              <label style={{ display: "block", color: "rgba(255,255,255,0.7)", fontSize: "13px", marginBottom: "8px" }}>
                How many squares can the knight visit? (Total moves)
              </label>
              <input value={playerAnswer} onChange={e => setPlayerAnswer(e.target.value)}
                placeholder="Enter number of moves" type="number" min="1"
                style={{ width: "100%", padding: "10px 14px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "rgba(255,255,255,0.08)", color: "#fff", fontSize: "14px", outline: "none", marginBottom: "12px", boxSizing: "border-box" }} />
              <div style={{ display: "flex", gap: "10px" }}>
                <button onClick={giveUp} disabled={loading}
                  style={{ flex: 1, padding: "12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "rgba(255,255,255,0.7)", fontSize: "14px", cursor: "pointer" }}>
                  {loading ? "..." : "Give Up 🤝"}
                </button>
                <button onClick={submitAnswer} disabled={loading}
                  style={{ flex: 2, padding: "12px", borderRadius: "8px", border: "none", background: "#185FA5", color: "#fff", fontSize: "15px", fontWeight: 500, cursor: "pointer" }}>
                  {loading ? "Checking..." : "Submit Answer"}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ─── RESULT SCREEN ─── */}
        {gameState === "result" && result && (
          <div style={{ maxWidth: "450px", margin: "0 auto", textAlign: "center" }}>
            <div style={{ fontSize: "64px", marginBottom: "1rem" }}>{resultConfig[result].emoji}</div>
            <h2 style={{ fontSize: "24px", fontWeight: 500, color: resultConfig[result].color, marginBottom: "8px" }}>
              {resultConfig[result].title}
            </h2>
            <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "14px", marginBottom: "1.5rem" }}>
              {resultConfig[result].msg}
            </p>

            {/* Algorithm Performance */}
            <div style={{ background: "rgba(255,255,255,0.07)", borderRadius: "12px", padding: "1rem", marginBottom: "1rem", textAlign: "left" }}>
              <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "12px", marginBottom: "12px" }}>Algorithm Performance</p>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                <span style={{ color: "rgba(255,255,255,0.7)", fontSize: "13px" }}>Warnsdorff's (Iterative)</span>
                <span style={{ color: "#4ade80", fontSize: "13px", fontWeight: 500 }}>{algo1Time}ms</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ color: "rgba(255,255,255,0.7)", fontSize: "13px" }}>Backtracking (Recursive)</span>
                <span style={{ color: "#60a5fa", fontSize: "13px", fontWeight: 500 }}>
                  {boardSize === 16 ? "N/A (16×16)" : `${algo2Time}ms`}
                </span>
              </div>
              {boardSize === 8 && (
                <p style={{ color: "rgba(255,255,255,0.3)", fontSize: "11px", marginTop: "8px" }}>
                  * Backtracking has a 5 second timeout due to O(8^n) complexity
                </p>
              )}
            </div>

            {/* Watch Solution Button */}
            <button onClick={openVisualization}
              style={{ width: "100%", padding: "12px", borderRadius: "8px", border: "1px solid rgba(55,138,221,0.5)", background: "rgba(55,138,221,0.15)", color: "#fff", fontSize: "14px", cursor: "pointer", marginBottom: "1rem" }}>
              🎯 Watch Algorithm Solution Step by Step

            </button>

            <div style={{ display: "flex", gap: "10px" }}>
              <button onClick={resetGame}
                style={{ flex: 1, padding: "12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", fontSize: "14px", cursor: "pointer" }}>
                Play Again
              </button>
              <button onClick={fetchLeaderboard}
                style={{ flex: 1, padding: "12px", borderRadius: "8px", border: "1px solid rgba(55,138,221,0.5)", background: "rgba(55,138,221,0.2)", color: "#fff", fontSize: "14px", cursor: "pointer" }}>
                🏅 Leaderboard
              </button>
              <button onClick={() => navigate("/")}
                style={{ flex: 1, padding: "12px", borderRadius: "8px", border: "none", background: "#185FA5", color: "#fff", fontSize: "14px", cursor: "pointer" }}>
                Back to Hub
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}