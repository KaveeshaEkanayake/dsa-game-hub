import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

export default function SnakeLadder() {
  const canvasRef = useRef(null);
  const pageRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });
  const navigate = useNavigate();

  useEffect(() => {
    const canvas = canvasRef.current;
    const page = pageRef.current;
    if (!canvas || !page) return;

    const ctx = canvas.getContext("2d");
    let animId;
    const NUM = 180;
    const SPEED = 2.5;
    const FOV = 300;
    let W;
    let H;
    let stars = [];

    function makeStar() {
      return {
        x: (Math.random() - 0.5) * W * 3,
        y: (Math.random() - 0.5) * H * 3,
        z: Math.random() * W,
        pz: 0,
      };
    }

    function resize() {
      W = canvas.width = page.offsetWidth;
      H = canvas.height = page.offsetHeight;
    }

    function init() {
      resize();
      stars = [];
      for (let i = 0; i < NUM; i++) {
        stars.push(makeStar());
      }
    }

    function draw() {
      ctx.fillStyle = "#050510";
      ctx.fillRect(0, 0, W, H);

      const cx = W / 2 + mouse.current.x * 30;
      const cy = H / 2 + mouse.current.y * 20;

      for (const s of stars) {
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
      const r = page.getBoundingClientRect();
      mouse.current.x = (e.clientX - r.left - W / 2) / W;
      mouse.current.y = (e.clientY - r.top - H / 2) / H;
    };

    init();
    draw();

    window.addEventListener("resize", init);
    page.addEventListener("mousemove", handleMouseMove);

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener("resize", init);
      page.removeEventListener("mousemove", handleMouseMove);
    };
  }, []);

  return (
    <div
      ref={pageRef}
      style={{
        background: "#050510",
        minHeight: "100vh",
        position: "relative",
        overflow: "hidden",
        padding: "2rem 1.5rem",
        fontFamily: "sans-serif",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
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

      <div
        style={{
          position: "relative",
          zIndex: 1,
          width: "100%",
          maxWidth: "920px",
          textAlign: "center",
        }}
      >
        <span
          style={{
            display: "inline-block",
            background: "rgba(255,255,255,0.07)",
            color: "rgba(255,255,255,0.65)",
            fontSize: "12px",
            padding: "6px 16px",
            borderRadius: "20px",
            marginBottom: "18px",
            border: "0.5px solid rgba(255,255,255,0.12)",
            letterSpacing: "1px",
            textTransform: "uppercase",
          }}
        >
          Challenge 2
        </span>

        <div
          style={{
            background: "rgba(7,61,46,0.28)",
            borderRadius: "24px",
            padding: "42px 28px",
            border: "1px solid rgba(255,255,255,0.1)",
            backdropFilter: "blur(6px)",
            boxShadow: "0 10px 40px rgba(0,0,0,0.35)",
            position: "relative",
            overflow: "hidden",
          }}
        >
          <div
            style={{
              position: "absolute",
              top: "-18px",
              right: "-12px",
              fontSize: "110px",
              opacity: 0.11,
              pointerEvents: "none",
            }}
          >
            🎲
          </div>

          <h1
            style={{
              fontSize: "clamp(2rem, 5vw, 3.4rem)",
              fontWeight: 700,
              color: "#fff",
              marginBottom: "14px",
              letterSpacing: "-0.8px",
            }}
          >
            Snake & Ladder
          </h1>

          <p
            style={{
              maxWidth: "700px",
              margin: "0 auto",
              fontSize: "15px",
              lineHeight: 1.8,
              color: "rgba(255,255,255,0.72)",
              marginBottom: "26px",
            }}
          >
            Dodge the snakes, climb the ladders, and solve the challenge by finding
            the minimum number of dice throws needed to win. Start the game to enter
            the puzzle screen and play the full Snake & Ladder challenge.
          </p>

          <div
            style={{
              display: "flex",
              justifyContent: "center",
              gap: "14px",
              flexWrap: "wrap",
              marginBottom: "28px",
            }}
          >
            <div
              style={{
                padding: "10px 18px",
                borderRadius: "999px",
                background: "rgba(255,255,255,0.08)",
                border: "1px solid rgba(255,255,255,0.12)",
                color: "#fff",
                fontSize: "13px",
              }}
            >
              BFS
            </div>
            <div
              style={{
                padding: "10px 18px",
                borderRadius: "999px",
                background: "rgba(255,255,255,0.08)",
                border: "1px solid rgba(255,255,255,0.12)",
                color: "#fff",
                fontSize: "13px",
              }}
            >
              Dynamic Programming
            </div>
            <div
              style={{
                padding: "10px 18px",
                borderRadius: "999px",
                background: "rgba(255,255,255,0.08)",
                border: "1px solid rgba(255,255,255,0.12)",
                color: "#fff",
                fontSize: "13px",
              }}
            >
              Interactive Board
            </div>
          </div>

          <button
            onClick={() => navigate("/snake-ladder/play")}
            style={{
              display: "inline-block",
              padding: "13px 30px",
              borderRadius: "999px",
              fontSize: "15px",
              fontWeight: 600,
              background: "rgba(255,255,255,0.13)",
              border: "1px solid rgba(255,255,255,0.22)",
              color: "#fff",
              cursor: "pointer",
              transition: "transform 0.15s ease",
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = "translateY(-3px)";
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = "translateY(0)";
            }}
          >
            Start Game
          </button>
        </div>
      </div>
    </div>
  );
}