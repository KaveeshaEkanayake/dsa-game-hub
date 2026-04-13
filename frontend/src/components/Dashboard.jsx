import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

const games = [
  {
    id: "knights-tour",
    challenge: "Challenge 1",
    title: "Knight's tour",
    desc: "Move the knight across every square exactly once. Can you find the path?",
    icon: "♟",
    color: "rgba(13,59,110,0.75)",
    route: "/knights-tour",
  },
  {
    id: "snake-ladder",
    challenge: "Challenge 2",
    title: "Snake & ladder",
    desc: "Dodge the snakes, climb the ladders. Find the minimum throws to win.",
    icon: "🎲",
    color: "rgba(7,61,46,0.75)",
    route: "/snake-ladder",
  },
  {
    id: "traffic",
    challenge: "Challenge 3",
    title: "Traffic rush",
    desc: "Control the city flow. Find the maximum throughput from A to T.",
    icon: "🚦",
    color: "rgba(74,42,3,0.75)",
    route: "/traffic",
  },
  {
    id: "queens",
    challenge: "Challenge 4",
    title: "Sixteen queens",
    desc: "Place 16 queens so none threaten each other. Sounds easy. It's not.",
    icon: "👑",
    color: "rgba(42,36,102,0.75)",
    route: "/queens",
  },
  {
    id: "mincost",
    challenge: "Challenge 5",
    title: "Minimum cost",
    desc: "Assign tasks to employees at the lowest possible cost. Every penny counts.",
    icon: "💰",
    color: "rgba(61,18,8,0.75)",
    route: "/mincost",
  },
];

export default function Dashboard() {
  const canvasRef = useRef(null);
  const hubRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });
  const navigate = useNavigate();

  useEffect(() => {
    const canvas = canvasRef.current;
    const hub = hubRef.current;
    const ctx = canvas.getContext("2d");
    let animId;
    const NUM = 180;
    const SPEED = 2.5;
    const FOV = 300;
    let W, H, stars = [];

    function makeStar() {
      return {
        x: (Math.random() - 0.5) * W * 3,
        y: (Math.random() - 0.5) * H * 3,
        z: Math.random() * W,
        pz: 0,
      };
    }

    function resize() {
      W = canvas.width = hub.offsetWidth;
      H = canvas.height = hub.offsetHeight;
    }

    function init() {
      resize();
      stars = [];
      for (let i = 0; i < NUM; i++) stars.push(makeStar());
    }

    function draw() {
      ctx.fillStyle = "#050510";
      ctx.fillRect(0, 0, W, H);
      const cx = W / 2 + mouse.current.x * 30;
      const cy = H / 2 + mouse.current.y * 20;

      for (let s of stars) {
        s.pz = s.z;
        s.z -= SPEED;
        if (s.z <= 0) {
          Object.assign(s, makeStar());
          s.z = W;
          s.pz = W;
        }
        const sx = (s.x / s.z) * FOV + cx;
        const sy = (s.y / s.z) * FOV + cy;
        const px = (s.x / s.pz) * FOV + cx;
        const py = (s.y / s.pz) * FOV + cy;
        const size = Math.max(0.3, (1 - s.z / W) * 2.5);
        const bright = Math.floor((1 - s.z / W) * 220 + 35);
        const alpha = (1 - s.z / W) * 0.9 + 0.1;
        ctx.beginPath();
        ctx.moveTo(px, py);
        ctx.lineTo(sx, sy);
        ctx.strokeStyle = `rgba(${bright},${bright},${Math.min(255, bright + 40)},${alpha})`;
        ctx.lineWidth = size;
        ctx.stroke();
      }
      animId = requestAnimationFrame(draw);
    }

    const handleMouseMove = (e) => {
      const r = hub.getBoundingClientRect();
      mouse.current.x = (e.clientX - r.left - W / 2) / W;
      mouse.current.y = (e.clientY - r.top - H / 2) / H;
    };

    init();
    draw();
    window.addEventListener("resize", init);
    hub.addEventListener("mousemove", handleMouseMove);

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener("resize", init);
      hub.removeEventListener("mousemove", handleMouseMove);
    };
  }, []);

  return (
    <div
      ref={hubRef}
      style={{
        background: "#050510",
        minHeight: "100vh",
        position: "relative",
        overflow: "hidden",
        padding: "2rem 1.5rem",
        fontFamily: "sans-serif",
      }}
    >
      <canvas
        ref={canvasRef}
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          width: "100%",
          height: "100%",
          pointerEvents: "none",
        }}
      />

      <div style={{ position: "relative", zIndex: 1 }}>
        <div style={{ textAlign: "center", marginBottom: "2.5rem" }}>
          <span
            style={{
              display: "inline-block",
              background: "rgba(255,255,255,0.07)",
              color: "rgba(255,255,255,0.5)",
              fontSize: "12px",
              padding: "4px 14px",
              borderRadius: "20px",
              marginBottom: "14px",
              border: "0.5px solid rgba(255,255,255,0.12)",
            }}
          >
            5 games · 10 algorithms
          </span>
          <h1
            style={{
              fontSize: "30px",
              fontWeight: 500,
              color: "#fff",
              letterSpacing: "-0.5px",
              display: "block",
            }}
          >
            DSA game hub
          </h1>
          <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.45)", marginTop: "6px" }}>
            Pick a challenge. Beat the algorithm.
          </p>
        </div>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
            gap: "14px",
          }}
        >
          {games.map((game) => (
            <div
              key={game.id}
              onClick={() => navigate(game.route)}
              style={{
                background: game.color,
                borderRadius: "16px",
                padding: "1.4rem",
                cursor: "pointer",
                position: "relative",
                overflow: "hidden",
                border: "1px solid rgba(255,255,255,0.1)",
                backdropFilter: "blur(4px)",
                transition: "transform 0.15s",
              }}
              onMouseEnter={(e) => (e.currentTarget.style.transform = "translateY(-5px)")}
              onMouseLeave={(e) => (e.currentTarget.style.transform = "translateY(0)")}
            >
              <div
                style={{
                  position: "absolute",
                  top: "-8px",
                  right: "-8px",
                  fontSize: "68px",
                  opacity: 0.13,
                  pointerEvents: "none",
                }}
              >
                {game.icon}
              </div>
              <div
                style={{
                  fontSize: "10px",
                  opacity: 0.55,
                  marginBottom: "7px",
                  letterSpacing: "1.2px",
                  textTransform: "uppercase",
                  color: "#fff",
                }}
              >
                {game.challenge}
              </div>
              <div
                style={{
                  fontSize: "17px",
                  fontWeight: 500,
                  marginBottom: "5px",
                  color: "#fff",
                }}
              >
                {game.title}
              </div>
              <div
                style={{
                  fontSize: "12px",
                  lineHeight: 1.5,
                  color: "rgba(255,255,255,0.7)",
                  marginBottom: "16px",
                }}
              >
                {game.desc}
              </div>
              <div
                style={{
                  display: "inline-block",
                  padding: "7px 18px",
                  borderRadius: "20px",
                  fontSize: "12px",
                  fontWeight: 500,
                  background: "rgba(255,255,255,0.13)",
                  border: "1px solid rgba(255,255,255,0.22)",
                  color: "#fff",
                }}
              >
                Play now
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}