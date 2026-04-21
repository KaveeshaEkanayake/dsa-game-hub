import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const POS = {
  A: [70, 220], B: [200, 100], C: [200, 220], D: [200, 340],
  E: [360, 100], F: [360, 260], G: [500, 140], H: [500, 300], T: [630, 220]
};

export default function TrafficSimulation() {
  const navigate = useNavigate();
  const graphCanvasRef = useRef(null);
  const bgCanvasRef = useRef(null);
  const hubRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });
  const carsRef = useRef([]);
  const carAnimRef = useRef(null);

  const [playerName, setPlayerName] = useState("");
  const [gameState, setGameState] = useState("setup");
  const [graph, setGraph] = useState([]);
  const [playerAnswer, setPlayerAnswer] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [roundId, setRoundId] = useState(null);
  const [correctAnswer, setCorrectAnswer] = useState(0);
  const [ffTime, setFfTime] = useState(null);
  const [ekTime, setEkTime] = useState(null);
  const [resultMessage, setResultMessage] = useState("");
  const [error, setError] = useState("");

  // ── galaxy starfield ──
  useEffect(() => {
    const canvas = bgCanvasRef.current;
    const hub = hubRef.current;
    if (!canvas || !hub) return;
    const ctx = canvas.getContext("2d");
    let animId;
    const NUM = 140, SPEED = 1.6, FOV = 300;
    let W, H, stars = [];

    function makeStar() {
      return { x: (Math.random() - 0.5) * W * 3, y: (Math.random() - 0.5) * H * 3, z: Math.random() * W, pz: 0 };
    }
    function resize() { W = canvas.width = hub.offsetWidth; H = canvas.height = hub.offsetHeight; }
    function draw() {
      ctx.fillStyle = "#0d0700"; ctx.fillRect(0, 0, W, H);
      const cx = W / 2 + mouse.current.x * 25, cy = H / 2 + mouse.current.y * 18;
      for (let s of stars) {
        s.pz = s.z; s.z -= SPEED;
        if (s.z <= 0) { Object.assign(s, makeStar()); s.z = W; s.pz = W; }
        const sx = (s.x / s.z) * FOV + cx, sy = (s.y / s.z) * FOV + cy;
        const px = (s.x / s.pz) * FOV + cx, py = (s.y / s.pz) * FOV + cy;
        const size = Math.max(0.3, (1 - s.z / W) * 2.8);
        const b = Math.floor((1 - s.z / W) * 200 + 55);
        const r = Math.min(255, b + 60), g = Math.floor(b * 0.6), bl = Math.floor(b * 0.1);
        ctx.beginPath(); ctx.moveTo(px, py); ctx.lineTo(sx, sy);
        ctx.strokeStyle = `rgba(${r},${g},${bl},0.85)`; ctx.lineWidth = size; ctx.stroke();
      }
      animId = requestAnimationFrame(draw);
    }
    const onMouseMove = (e) => {
      const r = hub.getBoundingClientRect();
      mouse.current.x = (e.clientX - r.left - W / 2) / W;
      mouse.current.y = (e.clientY - r.top - H / 2) / H;
    };
    resize(); stars = Array.from({ length: NUM }, makeStar); draw();
    window.addEventListener("resize", resize);
    hub.addEventListener("mousemove", onMouseMove);
    return () => { cancelAnimationFrame(animId); window.removeEventListener("resize", resize); hub.removeEventListener("mousemove", onMouseMove); };
  }, []);

  // ── draw road graph ──
  const drawGraph = (edges) => {
    const canvas = graphCanvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    const W = 720, H = 460;
    canvas.width = W; canvas.height = H;
    ctx.clearRect(0, 0, W, H);

    edges.forEach(({ from, to, capacity }) => {
      const [x1, y1] = POS[from], [x2, y2] = POS[to];
      const angle = Math.atan2(y2 - y1, x2 - x1);
      const r = 28;
      const sx = x1 + Math.cos(angle) * r, sy = y1 + Math.sin(angle) * r;
      const ex = x2 - Math.cos(angle) * r, ey = y2 - Math.sin(angle) * r;

      ctx.beginPath(); ctx.moveTo(sx, sy); ctx.lineTo(ex, ey);
      ctx.strokeStyle = "rgba(0,0,0,0.6)"; ctx.lineWidth = 12; ctx.stroke();
      ctx.beginPath(); ctx.moveTo(sx, sy); ctx.lineTo(ex, ey);
      ctx.strokeStyle = "#2a1500"; ctx.lineWidth = 9; ctx.stroke();
      ctx.beginPath(); ctx.moveTo(sx, sy); ctx.lineTo(ex, ey);
      ctx.strokeStyle = "rgba(255,120,20,0.12)"; ctx.lineWidth = 9; ctx.stroke();
      ctx.beginPath(); ctx.moveTo(sx, sy); ctx.lineTo(ex, ey);
      ctx.strokeStyle = "rgba(255,150,30,0.3)";
      ctx.lineWidth = 1.5; ctx.setLineDash([8, 9]); ctx.stroke(); ctx.setLineDash([]);

      const hw = 7, hl = 12;
      ctx.beginPath();
      ctx.moveTo(ex, ey);
      ctx.lineTo(ex - hl * Math.cos(angle) + hw * Math.sin(angle), ey - hl * Math.sin(angle) - hw * Math.cos(angle));
      ctx.lineTo(ex - hl * Math.cos(angle) - hw * Math.sin(angle), ey - hl * Math.sin(angle) + hw * Math.cos(angle));
      ctx.closePath(); ctx.fillStyle = "#f97316"; ctx.fill();

      const mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
      const edgeKey = `${from}-${to}`;
      const nudge = { "B-F": 28, "C-F": -28 };
      const extraNudge = nudge[edgeKey] || 0;
      const ox = -Math.sin(angle) * 18 + Math.cos(angle) * extraNudge;
      const oy = Math.cos(angle) * 18 + Math.sin(angle) * extraNudge;
      const bx = mx + ox, by = my + oy;
      ctx.beginPath(); ctx.roundRect(bx - 15, by - 11, 30, 22, 5);
      ctx.fillStyle = "rgba(15,8,0,0.9)"; ctx.strokeStyle = "rgba(249,115,22,0.6)";
      ctx.lineWidth = 1; ctx.fill(); ctx.stroke();
      ctx.fillStyle = "#fed7aa"; ctx.font = "bold 11px 'Courier New'";
      ctx.textAlign = "center"; ctx.textBaseline = "middle";
      ctx.fillText(capacity, bx, by);
    });

    Object.entries(POS).forEach(([node, [x, y]]) => {
      const isSource = node === "A", isSink = node === "T";
      const col = isSource ? "#22c55e" : isSink ? "#ef4444" : "#f97316";
      const glowCol = isSource ? "rgba(34,197,94,0.2)" : isSink ? "rgba(239,68,68,0.2)" : "rgba(249,115,22,0.2)";
      ctx.beginPath(); ctx.arc(x, y, 34, 0, Math.PI * 2); ctx.fillStyle = glowCol; ctx.fill();
      ctx.beginPath(); ctx.arc(x, y, 26, 0, Math.PI * 2);
      ctx.fillStyle = "#150a00"; ctx.strokeStyle = col; ctx.lineWidth = 2.5; ctx.fill(); ctx.stroke();
      ctx.beginPath(); ctx.arc(x, y, 21, 0, Math.PI * 2);
      ctx.fillStyle = isSource ? "rgba(34,197,94,0.2)" : isSink ? "rgba(239,68,68,0.2)" : "rgba(249,115,22,0.18)"; ctx.fill();
      ctx.fillStyle = col; ctx.font = "bold 15px 'Courier New'";
      ctx.textAlign = "center"; ctx.textBaseline = "middle"; ctx.fillText(node, x, y);
      if (isSource) { ctx.fillStyle = "rgba(34,197,94,0.65)"; ctx.font = "9px sans-serif"; ctx.fillText("SOURCE", x, y + 40); }
      if (isSink) { ctx.fillStyle = "rgba(239,68,68,0.65)"; ctx.font = "9px sans-serif"; ctx.fillText("SINK", x, y + 40); }
    });
  };

  const convertEdges = (backendEdges) => {
    if (!backendEdges || !backendEdges.length) return [];
    if (backendEdges[0].source !== undefined) {
      return backendEdges.map(e => ({ from: e.source, to: e.destination, capacity: e.capacity }));
    }
    return backendEdges;
  };

  // ── animated cars ──
  useEffect(() => {
    if (gameState !== "playing" || !graph.length) return;
    const canvas = graphCanvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    carsRef.current = graph.map((edge, i) => ({
      edge, t: i / graph.length,
      speed: 0.003 + Math.random() * 0.002,
      color: ["#f97316", "#fb923c", "#fdba74", "#fbbf24", "#f59e0b"][i % 5],
    }));

    function drawCar(x, y, angle, color) {
      ctx.save(); ctx.translate(x, y); ctx.rotate(angle);
      ctx.fillStyle = color; ctx.beginPath(); ctx.roundRect(-6, -3.5, 12, 7, 2); ctx.fill();
      ctx.fillStyle = "rgba(0,0,0,0.5)"; ctx.beginPath(); ctx.roundRect(-2, -2.5, 5, 5, 1); ctx.fill();
      ctx.fillStyle = "#150a00";
      [[-4, -4], [4, -4], [-4, 4], [4, 4]].forEach(([wx, wy]) => { ctx.beginPath(); ctx.arc(wx, wy, 2, 0, Math.PI * 2); ctx.fill(); });
      ctx.restore();
    }

    let animId;
    function animate() {
      drawGraph(graph);
      carsRef.current.forEach(car => {
        car.t += car.speed; if (car.t > 1) car.t = 0;
        const [x1, y1] = POS[car.edge.from], [x2, y2] = POS[car.edge.to];
        const angle = Math.atan2(y2 - y1, x2 - x1); const r = 28;
        const sx = x1 + Math.cos(angle) * r, sy = y1 + Math.sin(angle) * r;
        const ex = x2 - Math.cos(angle) * r, ey = y2 - Math.sin(angle) * r;
        drawCar(sx + (ex - sx) * car.t, sy + (ey - sy) * car.t, angle, car.color);
      });
      animId = requestAnimationFrame(animate);
    }
    animate(); carAnimRef.current = animId;
    return () => cancelAnimationFrame(animId);
  }, [gameState, graph]);

  useEffect(() => {
    if ((gameState === "playing" || gameState === "result") && graph.length) drawGraph(graph);
  }, [graph, gameState]);

  const startGame = async () => {
    if (!playerName.trim()) { setError("Please enter your name!"); return; }
    setError(""); setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/traffic/new-round`);
      if (!res.ok) throw new Error(`Server error: ${res.status}`);
      const data = await res.json();
      setGraph(convertEdges(data.edges));
      setRoundId(data.roundId);
      setCorrectAnswer(data.correctMaxFlow);
      setFfTime(data.fordFulkersonTimeMs);
      setEkTime(data.edmondsKarpTimeMs);
      setGameState("playing");
    } catch (e) { setError(`Failed to connect to server. ${e.message}`); }
    setLoading(false);
  };

  const submitAnswer = async () => {
    if (!playerAnswer.trim()) { setError("Please enter your answer!"); return; }
    if (isNaN(parseInt(playerAnswer)) || parseInt(playerAnswer) < 0) { setError("Please enter a valid positive number!"); return; }
    setError(""); setLoading(true);
    try {
      const res = await fetch(`${API_URL}/api/traffic/submit-answer`, {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ roundId, playerName, playerAnswer: parseInt(playerAnswer) }),
      });
      if (!res.ok) { const e = await res.json(); throw new Error(e.message || `Server error: ${res.status}`); }
      const data = await res.json();
      const diff = Math.abs(parseInt(playerAnswer) - data.correctMaxFlow);
      const resultStr = data.correct ? "win" : diff <= 1 ? "draw" : "lose";
      setResult(resultStr);
      setResultMessage(data.message);
      if (data.fordFulkersonTimeMs) setFfTime(data.fordFulkersonTimeMs);
      if (data.edmondsKarpTimeMs) setEkTime(data.edmondsKarpTimeMs);
      setCorrectAnswer(data.correctMaxFlow);
      cancelAnimationFrame(carAnimRef.current);
      setGameState("result");
    } catch (e) { setError(`Error: ${e.message}`); }
    setLoading(false);
  };

  const resetGame = () => {
    cancelAnimationFrame(carAnimRef.current);
    setGameState("setup"); setGraph([]); setPlayerAnswer("");
    setResult(null); setCorrectAnswer(0); setRoundId(null);
    setResultMessage(""); setError(""); setFfTime(null); setEkTime(null);
  };

  const resultConfig = {
    win: { emoji: "🏆", color: "#22c55e", label: "Correct! Well done!", bg: "rgba(34,197,94,0.05)", border: "rgba(34,197,94,0.2)" },
    draw: { emoji: "🤝", color: "#f59e0b", label: "So Close! Almost there!", bg: "rgba(245,158,11,0.05)", border: "rgba(245,158,11,0.2)" },
    lose: { emoji: "😞", color: "#ef4444", label: "Not quite right!", bg: "rgba(239,68,68,0.05)", border: "rgba(239,68,68,0.2)" },
  };

  const CARD = { background: "rgba(255,255,255,0.03)", border: "1px solid rgba(249,115,22,0.2)", borderRadius: 20, padding: "1.75rem", position: "relative", overflow: "hidden", backdropFilter: "blur(8px)" };
  const inp = { width: "100%", background: "rgba(249,115,22,0.07)", border: "1px solid rgba(249,115,22,0.3)", borderRadius: 10, color: "#fed7aa", fontSize: 15, padding: "12px 16px", outline: "none", fontFamily: "'Courier New', monospace" };
  const lbl = { fontSize: 11, color: "rgba(253,186,116,0.6)", textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: 8, display: "block", fontFamily: "'Courier New', monospace" };
  const Accent = () => <div style={{ position: "absolute", top: 0, left: 0, right: 0, height: 2, background: "linear-gradient(90deg,transparent,#f97316,transparent)" }} />;
  const PrimaryBtn = ({ onClick, disabled, children }) => (
    <button onClick={onClick} disabled={disabled} style={{ width: "100%", marginTop: 14, background: disabled ? "rgba(249,115,22,0.3)" : "linear-gradient(135deg,#ea580c,#f97316)", border: "1px solid rgba(249,115,22,0.5)", color: "#fff8f0", fontSize: 15, fontWeight: 700, padding: 14, borderRadius: 10, cursor: disabled ? "not-allowed" : "pointer", fontFamily: "'Courier New', monospace" }}>
      {children}
    </button>
  );
  const Pill = ({ color, label }) => (
    <div style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 11, color: "rgba(253,186,116,0.6)", background: "rgba(249,115,22,0.08)", border: "1px solid rgba(249,115,22,0.2)", borderRadius: 99, padding: "4px 10px", fontFamily: "'Courier New', monospace" }}>
      <div style={{ width: 7, height: 7, borderRadius: "50%", background: color }} />{label}
    </div>
  );

  return (
    <div ref={hubRef} style={{ background: "#0d0700", minHeight: "100vh", position: "relative", overflow: "hidden", fontFamily: "sans-serif" }}>
      <canvas ref={bgCanvasRef} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", pointerEvents: "none" }} />
      <div style={{ position: "absolute", inset: 0, pointerEvents: "none", backgroundImage: "repeating-linear-gradient(105deg,rgba(249,115,22,0.03) 0px,rgba(249,115,22,0.03) 1px,transparent 1px,transparent 60px)" }} />

      <div style={{ position: "relative", zIndex: 1, padding: "2rem 1.5rem" }}>
        <button onClick={() => navigate("/")} style={{ display: "inline-flex", alignItems: "center", gap: 6, background: "rgba(249,115,22,0.1)", border: "1px solid rgba(249,115,22,0.3)", color: "#fb923c", padding: "8px 16px", borderRadius: 99, cursor: "pointer", fontSize: 13, marginBottom: "2rem", fontFamily: "'Courier New', monospace" }}>← Back</button>

        <div style={{ textAlign: "center", marginBottom: "2.5rem" }}>
          <div style={{ display: "inline-block", margin: "0 auto 4px" }}>
            <svg width="100" height="130" viewBox="0 0 100 130" style={{ overflow: "visible", display: "block" }}>
              <defs>
                <radialGradient id="icon-rg-red" cx="50%" cy="35%" r="55%"><stop offset="0%" stopColor="#ff6666" /><stop offset="100%" stopColor="#cc0011" /></radialGradient>
                <radialGradient id="icon-rg-yellow" cx="50%" cy="35%" r="55%"><stop offset="0%" stopColor="#ffe066" /><stop offset="100%" stopColor="#cc9900" /></radialGradient>
                <clipPath id="icon-road-clip"><rect x="0" y="92" width="100" height="38" /></clipPath>
              </defs>
              <circle cx="50" cy="52" r="38" fill="none" stroke="#f97316" strokeWidth="0.8" opacity="0.18" className="tl-ring1" />
              <circle cx="50" cy="52" r="38" fill="none" stroke="#f97316" strokeWidth="0.8" opacity="0.18" className="tl-ring2" />
              <circle cx="50" cy="52" r="38" fill="none" stroke="#f97316" strokeWidth="0.8" opacity="0.18" className="tl-ring3" />
              <rect x="0" y="92" width="100" height="36" fill="#0f1e16" />
              <rect x="0" y="92" width="100" height="2" fill="#1e3028" opacity="0.9" />
              <g className="tl-stripe" clipPath="url(#icon-road-clip)">
                {[-60, 0, 60, 120, 180].map(x => <rect key={x} x={x} y="108" width="40" height="4" rx="2" fill="#1e3a28" opacity="0.7" />)}
              </g>
              <line x1="0" y1="110" x2="100" y2="110" stroke="#1a2e22" strokeWidth="0.5" opacity="0.5" />
              <g className="tl-car-a" style={{ transformOrigin: "50px 100px" }}>
                <g transform="translate(50,100)">
                  <ellipse cx="0" cy="3" rx="14" ry="3" fill="#000" opacity="0.3" />
                  <rect x="-13" y="-5" width="26" height="9" rx="3" fill="#f97316" />
                  <rect x="-5" y="-5" width="13" height="7" rx="1.5" fill="#1a4a2e" opacity="0.65" />
                  <circle cx="-8" cy="4" r="2.5" fill="#0d1810" /><circle cx="-8" cy="4" r="1.2" fill="#334433" />
                  <circle cx="8" cy="4" r="2.5" fill="#0d1810" /><circle cx="8" cy="4" r="1.2" fill="#334433" />
                  <rect x="11" y="-3.5" width="3" height="2.5" rx="0.8" fill="#ffe566" opacity="0.9" />
                  <rect x="11" y="1" width="3" height="2.5" rx="0.8" fill="#ffe566" opacity="0.9" />
                  <rect x="-14" y="-2.5" width="2" height="2" rx="0.5" fill="#ff2244" opacity="0.8" />
                  <rect x="-14" y="1" width="2" height="2" rx="0.5" fill="#ff2244" opacity="0.8" />
                </g>
              </g>
              <g className="tl-car-b" style={{ transformOrigin: "50px 120px" }}>
                <g transform="translate(50,120)">
                  <ellipse cx="0" cy="3" rx="12" ry="2.8" fill="#000" opacity="0.28" />
                  <rect x="-11" y="-4.5" width="22" height="8" rx="2.5" fill="#fbbf24" />
                  <rect x="-4" y="-4.5" width="11" height="6" rx="1.5" fill="#1a3a2e" opacity="0.6" />
                  <circle cx="-7" cy="3.5" r="2.2" fill="#0d1810" /><circle cx="-7" cy="3.5" r="1" fill="#334433" />
                  <circle cx="7" cy="3.5" r="2.2" fill="#0d1810" /><circle cx="7" cy="3.5" r="1" fill="#334433" />
                  <rect x="9.5" y="-3" width="2.5" height="2" rx="0.5" fill="#ffe566" opacity="0.9" />
                  <rect x="9.5" y="1" width="2.5" height="2" rx="0.5" fill="#ffe566" opacity="0.9" />
                  <rect x="-12" y="-2" width="2" height="1.8" rx="0.4" fill="#ff2244" opacity="0.8" />
                  <rect x="-12" y="1" width="2" height="1.8" rx="0.4" fill="#ff2244" opacity="0.8" />
                </g>
              </g>
              <g className="tl-car-c" style={{ transformOrigin: "50px 100px" }}>
                <g transform="translate(50,100) scale(-1,1)">
                  <ellipse cx="0" cy="3" rx="14" ry="3" fill="#000" opacity="0.28" />
                  <rect x="-13" y="-5" width="26" height="9" rx="3" fill="#22c55e" />
                  <rect x="-5" y="-5" width="13" height="7" rx="1.5" fill="#1a4a2e" opacity="0.6" />
                  <circle cx="-8" cy="4" r="2.5" fill="#0d1810" /><circle cx="-8" cy="4" r="1.2" fill="#334433" />
                  <circle cx="8" cy="4" r="2.5" fill="#0d1810" /><circle cx="8" cy="4" r="1.2" fill="#334433" />
                  <rect x="11" y="-3.5" width="3" height="2.5" rx="0.8" fill="#ffe566" opacity="0.9" />
                  <rect x="11" y="1" width="3" height="2.5" rx="0.8" fill="#ffe566" opacity="0.9" />
                  <rect x="-14" y="-2.5" width="2" height="2" rx="0.5" fill="#ff2244" opacity="0.8" />
                  <rect x="-14" y="1" width="2" height="2" rx="0.5" fill="#ff2244" opacity="0.8" />
                </g>
              </g>
              <g className="tl-car-d" style={{ transformOrigin: "50px 120px" }}>
                <g transform="translate(50,120) scale(-1,1)">
                  <ellipse cx="0" cy="3" rx="12" ry="2.8" fill="#000" opacity="0.25" />
                  <rect x="-11" y="-4.5" width="22" height="8" rx="2.5" fill="#60a5fa" />
                  <rect x="-4" y="-4.5" width="11" height="6" rx="1.5" fill="#1a2a4e" opacity="0.55" />
                  <circle cx="-7" cy="3.5" r="2.2" fill="#0d1810" /><circle cx="-7" cy="3.5" r="1" fill="#334433" />
                  <circle cx="7" cy="3.5" r="2.2" fill="#0d1810" /><circle cx="7" cy="3.5" r="1" fill="#334433" />
                  <rect x="9.5" y="-3" width="2.5" height="2" rx="0.5" fill="#ffe566" opacity="0.9" />
                  <rect x="9.5" y="1" width="2.5" height="2" rx="0.5" fill="#ffe566" opacity="0.9" />
                  <rect x="-12" y="-2" width="2" height="1.8" rx="0.4" fill="#ff2244" opacity="0.8" />
                  <rect x="-12" y="1" width="2" height="1.8" rx="0.4" fill="#ff2244" opacity="0.8" />
                </g>
              </g>
              <g className="tl-pole">
                <rect x="47.5" y="75" width="5" height="20" rx="2.5" fill="#1a2e22" />
                <rect x="31" y="8" width="38" height="70" rx="8" fill="#0d1c14" />
                <rect x="31" y="8" width="38" height="70" rx="8" fill="none" stroke="#f97316" strokeWidth="1.2" opacity="0.45" />
                <rect x="33" y="18" width="34" height="5" rx="2" fill="#0a1510" opacity="0.8" />
                <rect x="33" y="39" width="34" height="5" rx="2" fill="#0a1510" opacity="0.8" />
                <rect x="33" y="60" width="34" height="5" rx="2" fill="#0a1510" opacity="0.8" />
                <circle cx="50" cy="24" r="10" fill="#1a0005" opacity="0.6" />
                <circle cx="50" cy="45" r="10" fill="#1a1400" opacity="0.6" />
                <circle cx="50" cy="66" r="10" fill="#001a08" opacity="0.6" />
                <circle cx="50" cy="24" r="10" fill="url(#icon-rg-red)" style={{ animation: "tl-pulse-red 1s ease-in-out infinite" }} />
                <circle cx="50" cy="45" r="10" fill="url(#icon-rg-yellow)" style={{ animation: "tl-pulse-yellow 1.4s ease-in-out infinite", animationDelay: "0.28s" }} />
                <circle cx="50" cy="66" r="10" fill="#001a08" opacity="0.7" />
                <circle cx="50" cy="24" r="16" fill="#ff1122" opacity="0" style={{ animation: "tl-glow-red 1s ease-in-out infinite" }} />
                <circle cx="50" cy="45" r="16" fill="#ffcc00" opacity="0" style={{ animation: "tl-glow-yellow 1.4s ease-in-out infinite", animationDelay: "0.28s" }} />
                <circle cx="45.5" cy="20" r="3" fill="#fff" opacity="0.2" />
                <circle cx="45.5" cy="41" r="3" fill="#fff" opacity="0.16" />
                <rect x="38" y="5" width="24" height="6" rx="3" fill="#0d1c14" stroke="#f97316" strokeWidth="0.8" opacity="0.75" />
                <line x1="31" y1="35" x2="28" y2="35" stroke="#f97316" strokeWidth="1" opacity="0.25" />
                <line x1="69" y1="35" x2="72" y2="35" stroke="#f97316" strokeWidth="1" opacity="0.25" />
              </g>
            </svg>
          </div>
          <h1 style={{ fontSize: 30, fontWeight: 700, letterSpacing: "-0.5px", background: "linear-gradient(135deg,#f97316,#fbbf24)", WebkitBackgroundClip: "text", backgroundClip: "text", color: "transparent", fontFamily: "'Courier New', monospace" }}>Traffic Simulation</h1>
          <p style={{ fontSize: 14, color: "rgba(253,186,116,0.5)", marginTop: 6, fontFamily: "'Courier New', monospace" }}>Find the maximum vehicle flow from source A to sink T</p>
        </div>

        {/* SETUP */}
        {gameState === "setup" && (
          <div style={{ maxWidth: 420, margin: "0 auto", ...CARD }}>
            <Accent />
            <label style={lbl}>Your name</label>
            <input style={inp} placeholder="Enter your name..." value={playerName}
              onChange={e => { setPlayerName(e.target.value); setError(""); }}
              onKeyDown={e => e.key === "Enter" && startGame()} />
            {error && <p style={{ color: "#f87171", fontSize: 12, marginTop: 8, fontFamily: "'Courier New', monospace" }}>{error}</p>}
            <PrimaryBtn onClick={startGame} disabled={loading}>{loading ? "Generating network..." : "Start Game"}</PrimaryBtn>
            <div style={{ display: "flex", gap: 8, justifyContent: "center", marginTop: "1.5rem", flexWrap: "wrap" }}>
              <Pill color="#22c55e" label="Source (A)" />
              <Pill color="#ef4444" label="Sink (T)" />
              <Pill color="#f97316" label="Intersection" />
            </div>
            <div style={{ marginTop: "1.5rem", padding: "1rem", background: "rgba(0,0,0,0.3)", borderRadius: 12, border: "1px solid rgba(249,115,22,0.1)" }}>
              <p style={{ fontSize: 11, color: "rgba(253,186,116,0.4)", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 10, fontFamily: "'Courier New', monospace" }}>How to score</p>
              {[{ color: "#22c55e", label: "WIN", desc: "Exact correct answer" }, { color: "#f59e0b", label: "DRAW", desc: "Within ±1 of correct answer" }, { color: "#ef4444", label: "LOSE", desc: "More than ±1 away" }].map(({ color, label, desc }) => (
                <div key={label} style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                  <div style={{ width: 8, height: 8, borderRadius: "50%", background: color, flexShrink: 0 }} />
                  <span style={{ fontSize: 11, color, fontWeight: 700, fontFamily: "'Courier New', monospace", minWidth: 36 }}>{label}</span>
                  <span style={{ fontSize: 11, color: "rgba(253,186,116,0.4)", fontFamily: "'Courier New', monospace" }}>{desc}</span>
                </div>
              ))}
            </div>
            <p style={{ fontSize: 11, color: "rgba(253,186,116,0.3)", textAlign: "center", marginTop: 16, fontFamily: "'Courier New', monospace" }}>Uses Ford-Fulkerson + Edmonds-Karp algorithms</p>
          </div>
        )}

        {/* PLAYING */}
        {gameState === "playing" && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1.5rem" }}>
            <div style={{ background: "rgba(249,115,22,0.08)", border: "1px solid rgba(249,115,22,0.2)", borderRadius: 12, padding: "10px 24px", color: "rgba(253,186,116,0.85)", fontSize: 14, fontFamily: "'Courier New', monospace" }}>
              Find the <strong style={{ color: "#fed7aa" }}>maximum flow</strong> from <strong style={{ color: "#22c55e" }}>A</strong> to <strong style={{ color: "#ef4444" }}>T</strong> <span style={{ color: "rgba(253,186,116,0.4)", fontSize: 12 }}>(vehicles/min)</span>
            </div>
            <div style={{ display: "flex", gap: "1.5rem", alignItems: "flex-start", flexWrap: "wrap", justifyContent: "center" }}>
              <div style={{ ...CARD, padding: "1.25rem" }}>
                <Accent />
                <p style={{ fontSize: 11, color: "rgba(253,186,116,0.4)", textTransform: "uppercase", letterSpacing: "0.07em", marginBottom: "0.75rem", fontFamily: "'Courier New', monospace" }}>Road Network — capacity (vehicles/min)</p>
                <canvas ref={graphCanvasRef} style={{ display: "block", maxWidth: "100%", borderRadius: 8 }} />
                <div style={{ display: "flex", gap: 8, marginTop: 12, flexWrap: "wrap" }}>
                  <Pill color="#22c55e" label="Source (A)" /><Pill color="#ef4444" label="Sink (T)" /><Pill color="#f97316" label="Intersection" />
                  <div style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 11, color: "rgba(253,186,116,0.5)", background: "rgba(249,115,22,0.08)", border: "1px solid rgba(249,115,22,0.18)", borderRadius: 99, padding: "4px 10px", fontFamily: "'Courier New', monospace" }}>
                    <div style={{ width: 7, height: 7, borderRadius: 1, background: "#fb923c" }} /> Moving cars
                  </div>
                </div>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: "1rem", minWidth: 270 }}>
                <div style={CARD}>
                  <Accent />
                  <label style={lbl}>Max flow (vehicles/min)</label>
                  <input type="number" style={{ ...inp, fontSize: 24, fontWeight: 700, textAlign: "center" }} placeholder="Your answer"
                    value={playerAnswer} onChange={e => { setPlayerAnswer(e.target.value); setError(""); }}
                    onKeyDown={e => e.key === "Enter" && submitAnswer()} />
                  {error && <p style={{ color: "#f87171", fontSize: 12, marginTop: 8, fontFamily: "'Courier New', monospace" }}>{error}</p>}
                  <PrimaryBtn onClick={submitAnswer} disabled={loading}>{loading ? "Checking..." : "Submit Answer"}</PrimaryBtn>
                  <div style={{ marginTop: 12, display: "flex", gap: 6, justifyContent: "center", flexWrap: "wrap" }}>
                    {[{ color: "#22c55e", label: "WIN = exact" }, { color: "#f59e0b", label: "DRAW = ±1" }, { color: "#ef4444", label: "LOSE = wrong" }].map(({ color, label }) => (
                      <div key={label} style={{ display: "flex", alignItems: "center", gap: 4, fontSize: 10, color: "rgba(253,186,116,0.4)", fontFamily: "'Courier New', monospace" }}>
                        <div style={{ width: 6, height: 6, borderRadius: "50%", background: color }} />{label}
                      </div>
                    ))}
                  </div>
                </div>
                <div style={CARD}>
                  <Accent />
                  <p style={{ fontSize: 11, color: "rgba(253,186,116,0.4)", textTransform: "uppercase", letterSpacing: "0.07em", marginBottom: "1rem", fontFamily: "'Courier New', monospace" }}>Algorithm performance</p>
                  {[["Ford-Fulkerson", ffTime], ["Edmonds-Karp", ekTime]].map(([name, t], i) => (
                    <div key={name} style={{ display: "flex", justifyContent: "space-between", padding: "9px 0", borderBottom: i === 0 ? "1px solid rgba(249,115,22,0.15)" : "none" }}>
                      <span style={{ fontSize: 13, color: "rgba(253,186,116,0.65)", fontFamily: "'Courier New', monospace" }}>{name}</span>
                      <span style={{ fontSize: 13, fontWeight: 700, color: "#f97316", fontFamily: "'Courier New', monospace" }}>{t != null ? `${t} ns` : "— ns"}</span>
                    </div>
                  ))}
                </div>
                <div style={{ ...CARD, padding: "1rem 1.25rem" }}>
                  <Accent />
                  <div style={{ display: "flex", justifyContent: "space-between" }}>
                    <span style={{ fontSize: 12, color: "rgba(253,186,116,0.4)", fontFamily: "'Courier New', monospace" }}>Round ID</span>
                    <span style={{ fontSize: 12, color: "#f97316", fontFamily: "'Courier New', monospace" }}>#{roundId}</span>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between", marginTop: 6 }}>
                    <span style={{ fontSize: 12, color: "rgba(253,186,116,0.4)", fontFamily: "'Courier New', monospace" }}>Player</span>
                    <span style={{ fontSize: 12, color: "#fed7aa", fontFamily: "'Courier New', monospace" }}>{playerName}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* RESULT */}
        {gameState === "result" && result && (
          <div style={{ maxWidth: 480, margin: "0 auto", textAlign: "center" }}>
            <div style={{ fontSize: 70, marginBottom: "1rem" }}>{resultConfig[result]?.emoji}</div>
            <h2 style={{ fontSize: 28, fontWeight: 700, color: resultConfig[result]?.color, marginBottom: 8, fontFamily: "'Courier New', monospace" }}>{resultConfig[result]?.label}</h2>
            <p style={{ color: "rgba(253,186,116,0.6)", fontSize: 14, marginBottom: "1.5rem", fontFamily: "'Courier New', monospace" }}>{resultMessage}</p>
            <div style={{ display: "inline-block", padding: "8px 24px", borderRadius: 99, marginBottom: "1.5rem", background: resultConfig[result]?.bg, border: `1px solid ${resultConfig[result]?.border}`, color: resultConfig[result]?.color, fontSize: 18, fontWeight: 700, fontFamily: "'Courier New', monospace", letterSpacing: "0.1em" }}>
              {result.toUpperCase()}
            </div>

            {(result === "lose" || result === "draw") && (
              <div style={{ ...CARD, marginBottom: "1.5rem", background: resultConfig[result]?.bg, borderColor: resultConfig[result]?.border }}>
                <Accent />
                <p style={{ fontSize: 13, color: "rgba(253,186,116,0.6)", fontFamily: "'Courier New', monospace" }}>
                  Correct answer: <strong style={{ color: resultConfig[result]?.color, fontSize: 22 }}>{correctAnswer}</strong> vehicles/min
                </p>
                {result === "draw" && <p style={{ fontSize: 11, color: "rgba(253,186,116,0.4)", marginTop: 6, fontFamily: "'Courier New', monospace" }}>Your answer was within ±1 — great estimate!</p>}
              </div>
            )}

            <div style={{ ...CARD, marginBottom: "1.5rem", textAlign: "left" }}>
              <Accent />
              <p style={{ fontSize: 11, color: "rgba(253,186,116,0.4)", textTransform: "uppercase", letterSpacing: "0.07em", marginBottom: 12, fontFamily: "'Courier New', monospace" }}>Algorithm performance</p>
              {[["Ford-Fulkerson", ffTime], ["Edmonds-Karp", ekTime]].map(([name, t], i) => (
                <div key={name} style={{ display: "flex", justifyContent: "space-between", padding: "8px 0", borderBottom: i === 0 ? "1px solid rgba(249,115,22,0.15)" : "none" }}>
                  <span style={{ fontSize: 13, color: "rgba(253,186,116,0.65)", fontFamily: "'Courier New', monospace" }}>{name}</span>
                  <span style={{ fontSize: 13, fontWeight: 700, color: "#f97316", fontFamily: "'Courier New', monospace" }}>{t != null ? `${t} ns` : "—"}</span>
                </div>
              ))}
              {ffTime != null && ekTime != null && (
                <p style={{ fontSize: 11, color: "#22c55e", marginTop: 10, fontFamily: "'Courier New', monospace" }}>
                  {ffTime <= ekTime ? "Ford-Fulkerson" : "Edmonds-Karp"} was faster this round
                </p>
              )}
            </div>

            <div style={{ display: "flex", gap: 10 }}>
              <button onClick={resetGame} style={{ flex: 1, padding: 12, borderRadius: 10, border: "1px solid rgba(249,115,22,0.3)", background: "transparent", color: "#fed7aa", fontSize: 14, cursor: "pointer", fontFamily: "'Courier New', monospace" }}>Play again</button>
              <button onClick={() => navigate("/")} style={{ flex: 1, padding: 12, borderRadius: 10, border: "1px solid #f97316", background: "linear-gradient(135deg,#ea580c,#f97316)", color: "#fff8f0", fontSize: 14, fontWeight: 700, cursor: "pointer", fontFamily: "'Courier New', monospace" }}>Back to hub</button>
            </div>
          </div>
        )}
      </div>
      <style>{`
        input[type=number]::-webkit-inner-spin-button, input[type=number]::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
        input[type=number] { -moz-appearance: textfield; }
        input::placeholder { color: rgba(253,186,116,0.25); }
        @keyframes tl-pole-sway    { 0%,100%{transform:rotate(-0.8deg)} 50%{transform:rotate(0.8deg)} }
        @keyframes tl-pulse-red    { 0%,100%{opacity:1} 50%{opacity:0.75} }
        @keyframes tl-pulse-yellow { 0%,100%{opacity:1} 50%{opacity:0.72} }
        @keyframes tl-glow-red     { 0%,100%{opacity:0.55} 50%{opacity:0.18} }
        @keyframes tl-glow-yellow  { 0%,100%{opacity:0.45} 50%{opacity:0.12} }
        @keyframes tl-ring         { 0%{r:38;opacity:0.45} 100%{r:58;opacity:0} }
        @keyframes tl-car-a        { 0%{transform:translateX(-200px)} 100%{transform:translateX(200px)} }
        @keyframes tl-car-b        { 0%{transform:translateX(-200px)} 100%{transform:translateX(200px)} }
        @keyframes tl-car-c        { 0%{transform:translateX(200px)}  100%{transform:translateX(-200px)} }
        @keyframes tl-car-d        { 0%{transform:translateX(200px)}  100%{transform:translateX(-200px)} }
        @keyframes tl-stripe       { 0%{transform:translateX(0)} 100%{transform:translateX(-60px)} }
        .tl-pole   { transform-origin:50px 80px; animation:tl-pole-sway 4s ease-in-out infinite; }
        .tl-car-a  { animation:tl-car-a 3.2s linear infinite; }
        .tl-car-b  { animation:tl-car-b 4.6s linear infinite; animation-delay:-2s; }
        .tl-car-c  { animation:tl-car-c 3.9s linear infinite; animation-delay:-1s; }
        .tl-car-d  { animation:tl-car-d 5.1s linear infinite; animation-delay:-2.8s; }
        .tl-stripe { animation:tl-stripe 0.7s linear infinite; }
        .tl-ring1  { animation:tl-ring 1.8s ease-out infinite; }
        .tl-ring2  { animation:tl-ring 1.8s ease-out infinite; animation-delay:0.6s; }
        .tl-ring3  { animation:tl-ring 1.8s ease-out infinite; animation-delay:1.2s; }
      `}</style>
    </div>
  );
}