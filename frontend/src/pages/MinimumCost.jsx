import { useEffect, useRef, useState } from "react";
import Navbar from "../components/Navbar";
import minimumCostApi from "../services/minimumCostApi";
import "../styles/minimum_cost.css";

export default function MinimumCost() {
  const [taskCount, setTaskCount] = useState("50");
  const [roundData, setRoundData] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);

  const canvasRef = useRef(null);
  const pageRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });

  async function loadHistory() {
    try {
      const response = await minimumCostApi.get("/history");
      setHistory(response.data || []);
    } catch (error) {
      console.error(error);
    }
  }

  useEffect(() => {
    loadHistory();
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    const page = pageRef.current;

    if (!canvas || !page) return;

    const ctx = canvas.getContext("2d");
    let animationId;
    const NUM_STARS = 180;
    const SPEED = 2.5;
    const FOV = 300;
    let width;
    let height;
    let stars = [];

    function createStar() {
      return {
        x: (Math.random() - 0.5) * width * 3,
        y: (Math.random() - 0.5) * height * 3,
        z: Math.random() * width,
        pz: 0,
      };
    }

    function resizeCanvas() {
      width = canvas.width = page.offsetWidth;
      height = canvas.height = page.offsetHeight;
    }

    function initStars() {
      resizeCanvas();
      stars = [];
      for (let i = 0; i < NUM_STARS; i++) {
        stars.push(createStar());
      }
    }

    function animate() {
      ctx.clearRect(0, 0, width, height);

      const gradient = ctx.createLinearGradient(0, 0, 0, height);
      gradient.addColorStop(0, "#04030b");
      gradient.addColorStop(0.45, "#070b1b");
      gradient.addColorStop(1, "#050510");
      ctx.fillStyle = gradient;
      ctx.fillRect(0, 0, width, height);

      const glow1 = ctx.createRadialGradient(width * 0.2, height * 0.2, 0, width * 0.2, height * 0.2, width * 0.28);
      glow1.addColorStop(0, "rgba(95, 76, 255, 0.18)");
      glow1.addColorStop(1, "rgba(95, 76, 255, 0)");
      ctx.fillStyle = glow1;
      ctx.fillRect(0, 0, width, height);

      const glow2 = ctx.createRadialGradient(width * 0.8, height * 0.18, 0, width * 0.8, height * 0.18, width * 0.22);
      glow2.addColorStop(0, "rgba(0, 212, 255, 0.12)");
      glow2.addColorStop(1, "rgba(0, 212, 255, 0)");
      ctx.fillStyle = glow2;
      ctx.fillRect(0, 0, width, height);

      const glow3 = ctx.createRadialGradient(width * 0.3, height * 0.78, 0, width * 0.3, height * 0.78, width * 0.22);
      glow3.addColorStop(0, "rgba(255, 0, 153, 0.08)");
      glow3.addColorStop(1, "rgba(255, 0, 153, 0)");
      ctx.fillStyle = glow3;
      ctx.fillRect(0, 0, width, height);

      const centerX = width / 2 + mouse.current.x * 30;
      const centerY = height / 2 + mouse.current.y * 20;

      for (let star of stars) {
        star.pz = star.z;
        star.z -= SPEED;

        if (star.z <= 0) {
          Object.assign(star, createStar());
          star.z = width;
          star.pz = width;
        }

        const sx = (star.x / star.z) * FOV + centerX;
        const sy = (star.y / star.z) * FOV + centerY;
        const px = (star.x / star.pz) * FOV + centerX;
        const py = (star.y / star.pz) * FOV + centerY;

        const size = Math.max(0.3, (1 - star.z / width) * 2.5);
        const brightness = Math.floor((1 - star.z / width) * 220 + 35);
        const alpha = (1 - star.z / width) * 0.9 + 0.1;

        ctx.beginPath();
        ctx.moveTo(px, py);
        ctx.lineTo(sx, sy);
        ctx.strokeStyle = `rgba(${brightness}, ${brightness}, ${Math.min(255, brightness + 40)}, ${alpha})`;
        ctx.lineWidth = size;
        ctx.stroke();
      }

      animationId = requestAnimationFrame(animate);
    }

    const handleMouseMove = (e) => {
      const rect = page.getBoundingClientRect();
      mouse.current.x = (e.clientX - rect.left - width / 2) / width;
      mouse.current.y = (e.clientY - rect.top - height / 2) / height;
    };

    initStars();
    animate();

    window.addEventListener("resize", initStars);
    page.addEventListener("mousemove", handleMouseMove);

    return () => {
      cancelAnimationFrame(animationId);
      window.removeEventListener("resize", initStars);
      page.removeEventListener("mousemove", handleMouseMove);
    };
  }, []);

  async function startRandomRound() {
    setLoading(true);
    setMessage("");
    setIsError(false);

    try {
      const response = await minimumCostApi.post("/run-round/random");
      setRoundData(response.data);
      setMessage("Random round completed successfully.");
      await loadHistory();
    } catch (error) {
      console.error(error);
      setMessage(error?.response?.data?.message || "Failed to run random round.");
      setIsError(true);
    } finally {
      setLoading(false);
    }
  }

  async function startManualRound() {
    const value = Number(taskCount);

    if (!value || value < 50 || value > 100) {
      setMessage("Please enter a task count between 50 and 100.");
      setIsError(true);
      return;
    }

    setLoading(true);
    setMessage("");
    setIsError(false);

    try {
      const response = await minimumCostApi.post("/run-round", {
        taskCount: value,
      });
      setRoundData(response.data);
      setMessage("Manual round completed successfully.");
      await loadHistory();
    } catch (error) {
      console.error(error);
      setMessage(error?.response?.data?.message || "Failed to run manual round.");
      setIsError(true);
    } finally {
      setLoading(false);
    }
  }

  function formatDate(value) {
    if (!value) return "-";
    return new Date(value).toLocaleString();
  }

  function renderAssignments(assignments) {
    if (!assignments || assignments.length === 0) {
      return <p>No assignments available.</p>;
    }

    return (
      <div className="assignment-list">
        {assignments.slice(0, 15).map((item, index) => (
          <div
            key={`${item.taskNumber}-${item.employeeNumber}-${index}`}
            className="assignment-chip"
          >
            Task {item.taskNumber} → Employee {item.employeeNumber} (${item.cost})
          </div>
        ))}
        {assignments.length > 15 && (
          <div className="assignment-note">
            Showing first 15 assignments out of {assignments.length}.
          </div>
        )}
      </div>
    );
  }

  return (
    <>
      <Navbar />

      <div ref={pageRef} className="minimum-cost-shell">
        <canvas ref={canvasRef} className="minimum-cost-canvas" />

        <div className="minimum-cost-page">
          <div className="minimum-cost-header">
            <span className="minimum-cost-badge">Challenge 5 · Assignment Problem</span>
            <h1>Minimum Cost Game</h1>
            <p>
              Generate tasks and employees between 50 and 100, solve the assignment
              problem using two algorithms, compare time taken, and save every round.
            </p>
          </div>

          <div className="minimum-cost-controls card">
            <div className="panel-glow" />
            <h2>Start New Round</h2>

            <div className="control-row">
              <div className="control-group">
                <label htmlFor="taskCount">Task Count</label>
                <input
                  id="taskCount"
                  type="number"
                  min="50"
                  max="100"
                  value={taskCount}
                  onChange={(e) => setTaskCount(e.target.value)}
                  placeholder="Enter task count"
                />
              </div>
            </div>

            <div className="button-row">
              <button onClick={startManualRound} disabled={loading}>
                {loading ? "Running..." : "Run Manual Round"}
              </button>

              <button onClick={startRandomRound} disabled={loading}>
                {loading ? "Running..." : "Run Random Round"}
              </button>
            </div>

            {message && (
              <div className={isError ? "error-box" : "success-box"}>
                {message}
              </div>
            )}
          </div>

          {roundData && (
            <div className="current-round-grid">
              <div className="card round-summary-card">
                <div className="panel-glow" />
                <h2>Current Round</h2>

                <div className="summary-grid">
                  <div>
                    <span>Round ID</span>
                    <strong>{roundData.roundId}</strong>
                  </div>
                  <div>
                    <span>Round Number</span>
                    <strong>{roundData.roundNumber}</strong>
                  </div>
                  <div>
                    <span>Task Count</span>
                    <strong>{roundData.taskCount}</strong>
                  </div>
                  <div>
                    <span>Faster Algorithm</span>
                    <strong>{roundData.fasterAlgorithm}</strong>
                  </div>
                </div>
              </div>

              <div className="card algorithm-card">
                <div className="panel-glow" />
                <h2>Algorithm 1</h2>
                <p>Cost: ${roundData.algorithm1?.minimumTotalCost}</p>
                <p>Time: {roundData.algorithm1?.timeTakenMs} ms</p>
                {renderAssignments(roundData.algorithm1?.assignments)}
              </div>

              <div className="card algorithm-card">
                <div className="panel-glow" />
                <h2>Algorithm 2</h2>
                <p>Cost: ${roundData.algorithm2?.minimumTotalCost}</p>
                <p>Time: {roundData.algorithm2?.timeTakenMs} ms</p>
                {renderAssignments(roundData.algorithm2?.assignments)}
              </div>
            </div>
          )}

          <div className="card history-card">
            <div className="panel-glow" />
            <h2>Round History</h2>

            <div className="history-table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Round</th>
                    <th>Tasks</th>
                    <th>Hungarian Cost</th>
                    <th>Flow Cost</th>
                    <th>Faster</th>
                    <th>Date</th>
                  </tr>
                </thead>

                <tbody>
                  {history.length === 0 ? (
                    <tr>
                      <td colSpan="6">No rounds saved yet.</td>
                    </tr>
                  ) : (
                    history.map((h) => (
                      <tr key={h.id}>
                        <td>{h.roundNumber}</td>
                        <td>{h.taskCount}</td>
                        <td>${h.hungarianMinimumCost}</td>
                        <td>${h.minCostFlowMinimumCost}</td>
                        <td>{h.fasterAlgorithm}</td>
                        <td>{formatDate(h.createdAt)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}