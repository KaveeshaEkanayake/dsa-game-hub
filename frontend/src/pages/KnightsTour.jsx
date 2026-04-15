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

  const startGame = async () => {
    if (!playerName.trim()) return alert("Please enter your name!");
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/knights-tour/generate?boardSize=${boardSize}`);
      const data = await res.json();
      setStartPos({ row: data.startRow, col: data.startCol });
      setSolution(data.solution);
      setAlgo1Time(data.algo1TimeMs);
      setAlgo2Time(data.algo2TimeMs);
      setGameState("playing");
    } catch {
      alert("Could not connect to server. Make sure Spring Boot is running!");
    }
    setLoading(false);
  };

  const submitAnswer = async () => {
    if (!playerAnswer.trim()) return alert("Please enter your answer!");
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/knights-tour/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          playerName,
          boardSize,
          startRow: startPos.row,
          startCol: startPos.col,
          playerAnswer,
        }),
      });
      const data = await res.json();
      setResult(data.correct ? "win" : "lose");
      setGameState("result");
    } catch {
      alert("Could not connect to server!");
    }
    setLoading(false);
  };

  const resetGame = () => {
    setGameState("setup");
    setPlayerAnswer("");
    setResult(null);
    setSolution([]);
  };

  const cellSize = boardSize === 8 ? 44 : 24;

  return (
    <div ref={hubRef} style={{ background: "#050510", minHeight: "100vh", position: "relative", overflow: "hidden", fontFamily: "sans-serif" }}>
      <canvas ref={canvasRef} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", pointerEvents: "none" }} />

      <div style={{ position: "relative", zIndex: 1, padding: "2rem 1.5rem" }}>
        <button onClick={() => navigate("/")} style={{ background: "rgba(255,255,255,0.1)", border: "1px solid rgba(255,255,255,0.2)", color: "#fff", padding: "8px 16px", borderRadius: "20px", cursor: "pointer", fontSize: "13px", marginBottom: "2rem" }}>
          ← Back
        </button>

        <div style={{ textAlign: "center", marginBottom: "2rem" }}>
          <div style={{ fontSize: "48px", marginBottom: "8px" }}>♟</div>
          <h1 style={{ fontSize: "28px", fontWeight: 500, color: "#fff", letterSpacing: "-0.5px" }}>Knight's tour</h1>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.45)", marginTop: "6px" }}>
            Visit every square exactly once
          </p>
        </div>

        {gameState === "setup" && (
          <div style={{ maxWidth: "400px", margin: "0 auto", background: "rgba(13,59,110,0.6)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "2rem" }}>
            <div style={{ marginBottom: "1.5rem" }}>
              <label style={{ display: "block", color: "rgba(255,255,255,0.7)", fontSize: "13px", marginBottom: "8px" }}>Your name</label>
              <input
                value={playerName}
                onChange={e => setPlayerName(e.target.value)}
                placeholder="Enter your name"
                style={{ width: "100%", padding: "10px 14px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "rgba(255,255,255,0.08)", color: "#fff", fontSize: "14px", outline: "none" }}
              />
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
              {loading ? "Loading..." : "Start game"}
            </button>
          </div>
        )}

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
                      <div key={col} style={{
                        width: cellSize, height: cellSize,
                        background: isStart ? "#378ADD" : isLight ? "#f0d9b5" : "#b58863",
                        display: "flex", alignItems: "center", justifyContent: "center",
                        fontSize: isStart ? (boardSize === 8 ? "22px" : "14px") : "0",
                      }}>
                        {isStart ? "♞" : ""}
                      </div>
                    );
                  })}
                </div>
              ))}
            </div>

            <div style={{ maxWidth: "400px", width: "100%", background: "rgba(13,59,110,0.6)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "1.5rem" }}>
              <label style={{ display: "block", color: "rgba(255,255,255,0.7)", fontSize: "13px", marginBottom: "8px" }}>
                Enter the number of moves in the knight's tour
              </label>
              <input
                value={playerAnswer}
                onChange={e => setPlayerAnswer(e.target.value)}
                placeholder={`e.g. ${boardSize * boardSize}`}
                style={{ width: "100%", padding: "10px 14px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "rgba(255,255,255,0.08)", color: "#fff", fontSize: "14px", outline: "none", marginBottom: "12px" }}
              />
              <button onClick={submitAnswer} disabled={loading}
                style={{ width: "100%", padding: "12px", borderRadius: "8px", border: "none", background: "#185FA5", color: "#fff", fontSize: "15px", fontWeight: 500, cursor: "pointer" }}>
                {loading ? "Checking..." : "Submit answer"}
              </button>
            </div>
          </div>
        )}

        {gameState === "result" && (
          <div style={{ maxWidth: "400px", margin: "0 auto", textAlign: "center" }}>
            <div style={{ fontSize: "64px", marginBottom: "1rem" }}>{result === "win" ? "🏆" : "😞"}</div>
            <h2 style={{ fontSize: "24px", fontWeight: 500, color: result === "win" ? "#4ade80" : "#f87171", marginBottom: "8px" }}>
              {result === "win" ? "Correct! Well done!" : "Not quite right!"}
            </h2>
            <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "14px", marginBottom: "1.5rem" }}>
              {result === "win" ? `Great job, ${playerName}! Your answer has been saved.` : `Keep trying, ${playerName}! The correct answer was ${boardSize * boardSize} moves.`}
            </p>
            {algo1Time !== null && (
              <div style={{ background: "rgba(255,255,255,0.07)", borderRadius: "12px", padding: "1rem", marginBottom: "1.5rem", textAlign: "left" }}>
                <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "12px", marginBottom: "8px" }}>Algorithm performance</p>
                <p style={{ color: "#fff", fontSize: "13px" }}>Warnsdorff's: <strong>{algo1Time}ms</strong></p>
                <p style={{ color: "#fff", fontSize: "13px" }}>Backtracking: <strong>{algo2Time}ms</strong></p>
              </div>
            )}
            <div style={{ display: "flex", gap: "10px" }}>
              <button onClick={resetGame} style={{ flex: 1, padding: "12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", fontSize: "14px", cursor: "pointer" }}>
                Play again
              </button>
              <button onClick={() => navigate("/")} style={{ flex: 1, padding: "12px", borderRadius: "8px", border: "none", background: "#185FA5", color: "#fff", fontSize: "14px", cursor: "pointer" }}>
                Back to hub
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}