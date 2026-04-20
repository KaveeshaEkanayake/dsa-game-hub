import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import "../styles/Sixteen_queen_puzzle.css";

function SixteenQueens() {
  const navigate = useNavigate();
  const canvasRef = useRef(null);
  const pageRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });

  const [playerName, setPlayerName] = useState("");
  const [answerText, setAnswerText] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);

  const [sequentialResult, setSequentialResult] = useState(null);
  const [threadedResult, setThreadedResult] = useState(null);

  const [results, setResults] = useState([]);
  const [answers, setAnswers] = useState([]);

  const [loading, setLoading] = useState(false);

  const [board, setBoard] = useState(Array(16).fill(-1));
  const [conflictCells, setConflictCells] = useState([]);

  const [showResultPopup, setShowResultPopup] = useState(false);
  const [resultPopupData, setResultPopupData] = useState(null);

  function getCoord(row, col) {
    return String.fromCharCode(65 + col) + row;
  }

  function getConflicts(boardArray) {
    const conflicts = [];
    const cells = [];

    for (let i = 0; i < 16; i++) {
      for (let j = i + 1; j < 16; j++) {
        if (boardArray[i] === -1 || boardArray[j] === -1) continue;

        if (boardArray[i] === boardArray[j]) {
          conflicts.push(
            `${getCoord(i, boardArray[i])} and ${getCoord(j, boardArray[j])} are in the same column`
          );
          cells.push(`${i}-${boardArray[i]}`);
          cells.push(`${j}-${boardArray[j]}`);
        }

        if (Math.abs(boardArray[i] - boardArray[j]) === Math.abs(i - j)) {
          conflicts.push(
            `${getCoord(i, boardArray[i])} and ${getCoord(j, boardArray[j])} are in the same diagonal`
          );
          cells.push(`${i}-${boardArray[i]}`);
          cells.push(`${j}-${boardArray[j]}`);
        }
      }
    }

    return {
      conflicts,
      cells: [...new Set(cells)],
    };
  }

  function updateBoardMessage(newBoard) {
    const result = getConflicts(newBoard);
    const conflicts = result.conflicts;
    const cells = result.cells;

    setConflictCells(cells);

    let count = 0;
    for (let i = 0; i < newBoard.length; i++) {
      if (newBoard[i] !== -1) count++;
    }

    if (count === 0) {
      setMessage("");
      setIsError(false);
      return;
    }

    if (conflicts.length > 0) {
      setMessage("Conflict found: " + conflicts.slice(0, 3).join(" | "));
      setIsError(true);
    } else if (count < 16) {
      setMessage("Good move. No conflicts so far.");
      setIsError(false);
    } else {
      setMessage("Great! All 16 queens are placed without conflict.");
      setIsError(false);
    }
  }

  function handleCellClick(row, col) {
    const newBoard = [...board];

    if (newBoard[row] === col) {
      newBoard[row] = -1;
    } else {
      newBoard[row] = col;
    }

    setBoard(newBoard);
    setAnswerText(newBoard.join(","));
    updateBoardMessage(newBoard);
  }

  function handleAnswerTextChange(e) {
    const value = e.target.value;
    setAnswerText(value);

    const parts = value.split(",");
    const newBoard = Array(16).fill(-1);

    for (let i = 0; i < parts.length && i < 16; i++) {
      const num = parseInt(parts[i].trim(), 10);
      if (!Number.isNaN(num) && num >= 0 && num <= 15) {
        newBoard[i] = num;
      }
    }

    setBoard(newBoard);
    updateBoardMessage(newBoard);
  }

  function openResultPopup(responseData, submittedPlayerName) {
    setResultPopupData({
      ...responseData,
      playerName: submittedPlayerName,
    });
    setShowResultPopup(true);
  }

  async function loadResults() {
    try {
      const res = await api.get("/results");
      setResults(res.data);
    } catch (error) {
      console.log(error);
    }
  }

  async function loadAnswers() {
    try {
      const res = await api.get("/answers");
      setAnswers(res.data);
    } catch (error) {
      console.log(error);
    }
  }

  async function runSequential() {
    setLoading(true);

    try {
      const res = await api.post("/run-sequential");
      setSequentialResult(res.data);
      setMessage("Sequential completed successfully.");
      setIsError(false);
      await loadResults();
    } catch (error) {
      console.log(error);
      setMessage("Error running sequential.");
      setIsError(true);
    } finally {
      setLoading(false);
    }
  }

  async function runThreaded() {
    setLoading(true);

    try {
      const res = await api.post("/run-threaded");
      setThreadedResult(res.data);
      setMessage("Threaded completed successfully.");
      setIsError(false);
      await loadResults();
    } catch (error) {
      console.log(error);
      setMessage("Error running threaded.");
      setIsError(true);
    } finally {
      setLoading(false);
    }
  }

  async function resetAnswers() {
    setLoading(true);

    try {
      const res = await api.post("/reset");
      setMessage(res.data.message || "Reset completed.");
      setIsError(false);
      setPlayerName("");
      setAnswerText("");
      setBoard(Array(16).fill(-1));
      setConflictCells([]);
      setSequentialResult(null);
      setThreadedResult(null);
      setShowResultPopup(false);
      setResultPopupData(null);
      await loadAnswers();
      await loadResults();
    } catch (error) {
      console.log(error);
      setMessage("Error resetting answers.");
      setIsError(true);
    } finally {
      setLoading(false);
    }
  }

  async function submitAnswer(e) {
    e.preventDefault();

    if (playerName.trim() === "" || answerText.trim() === "") {
      setMessage("Please fill player name and answer.");
      setIsError(true);
      return;
    }

    const submittedPlayerName = playerName.trim();

    try {
      const res = await api.post("/submit-answer", {
        playerName: submittedPlayerName,
        answerText: answerText,
      });

      setMessage(res.data.message);
      setIsError(!res.data.success);
      openResultPopup(res.data, submittedPlayerName);

      setPlayerName("");
      setAnswerText("");
      setBoard(Array(16).fill(-1));
      setConflictCells([]);
      await loadAnswers();
      await loadResults();
    } catch (error) {
      console.log(error);
      setMessage(error?.response?.data?.message || "Error submitting answer.");
      setIsError(true);
    }
  }

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
      for (let i = 0; i < NUM; i++) stars.push(makeStar());
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

  useEffect(() => {
    let ignore = false;

    async function initData() {
      try {
        const [resultsRes, answersRes] = await Promise.all([
          api.get("/results"),
          api.get("/answers"),
        ]);

        if (!ignore) {
          setResults(resultsRes.data);
          setAnswers(answersRes.data);
        }
      } catch (error) {
        console.log(error);
      }
    }


    initData();

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <div className="page" ref={pageRef}>
      <canvas ref={canvasRef} className="game-bg" aria-hidden="true" />

      {showResultPopup && resultPopupData && (
        <div className="popup-overlay" onClick={() => setShowResultPopup(false)}>
          <div className="congrats-popup" onClick={(e) => e.stopPropagation()}>
            <div className="confetti-layer">
              <span className="confetti c1"></span>
              <span className="confetti c2"></span>
              <span className="confetti c3"></span>
              <span className="confetti c4"></span>
              <span className="confetti c5"></span>
              <span className="confetti c6"></span>
              <span className="confetti c7"></span>
              <span className="confetti c8"></span>
              <span className="confetti c9"></span>
              <span className="confetti c10"></span>
              <span className="confetti c11"></span>
              <span className="confetti c12"></span>
            </div>

            <button className="popup-close-btn" onClick={() => setShowResultPopup(false)}>
              ×
            </button>

            <div className="popup-trophy">
              {resultPopupData.correct && !resultPopupData.alreadyFound ? "🏆" : "👑"}
            </div>

            <h2>
              {resultPopupData.correct && !resultPopupData.alreadyFound
                ? "Congratulations!"
                : resultPopupData.alreadyFound
                ? "Already Found"
                : "Try Again"}
            </h2>

            <p className="popup-player-name">{resultPopupData.playerName}</p>
            <p className="popup-text">{resultPopupData.message}</p>

            <div className="result-card" style={{ marginTop: "16px", textAlign: "left" }}>
              <p>
                <strong>Sequential check:</strong>{" "}
                {resultPopupData.sequentialCheckTimeNs ?? 0} ns
              </p>
              <p>
                <strong>Threaded check:</strong>{" "}
                {resultPopupData.threadedCheckTimeNs ?? 0} ns
              </p>
              <p>
                <strong>Total check time:</strong>{" "}
                {resultPopupData.totalCheckTimeNs ?? 0} ns
              </p>
              <p>
                <strong>Best algorithm:</strong>{" "}
                {resultPopupData.bestAlgorithm || "-"}
              </p>
              <p>
                <strong>Comparison:</strong>{" "}
                {resultPopupData.comparisonMessage || "-"}
              </p>
              <p>
                <strong>Round:</strong> {resultPopupData.roundNumber ?? "-"}
              </p>
            </div>

            <button className="popup-action-btn" onClick={() => setShowResultPopup(false)}>
              OK
            </button>
          </div>
        </div>
      )}

      <div className="main-box">
        <div className="top-nav">
          <button className="back-btn" onClick={() => navigate("/")}>
            ← Back
          </button>
        </div>

        <div className="title-box">
          <div className="title-left">
            <span className="game-badge">Strategy Puzzle</span>
            <h1>Sixteen Queens Puzzle</h1>
            <p>
              Place 16 queens on the 16×16 board so that no two queens attack each other.
            </p>
          </div>
        </div>

        <div className="button-group">
          <button onClick={runSequential}>Run Sequential</button>
          <button onClick={runThreaded}>Run Threaded</button>
          <button onClick={resetAnswers} className="danger-btn" style={{ visibility: "hidden" }}>
            Reset
          </button>
        </div>

        {loading && <div className="loader">Running algorithm...</div>}

        {message && (
          <div className={isError ? "message error" : "message success"}>
            {message}
          </div>
        )}

        <div className="main-content">
          <div className="left-side">
            <div className="board-panel">
              <div className="section-title">
                <h2>Interactive Board</h2>
                <p>Click any square to place or remove a queen.</p>
              </div>

              <div className="board-wrapper">
                <div className="board-box">
                  <div className="board-header">
                    <div className="corner-cell"></div>
                    {Array.from({ length: 16 }).map((_, col) => (
                      <div key={col} className="header-cell">
                        {String.fromCharCode(65 + col)}
                      </div>
                    ))}
                  </div>

                  {board.map((colValue, row) => (
                    <div key={row} className="board-row">
                      <div className="side-label">{row}</div>

                      {Array.from({ length: 16 }).map((_, col) => {
                        const isConflict = conflictCells.includes(`${row}-${col}`);

                        return (
                          <div
                            key={col}
                            className={
                              "cell " +
                              ((row + col) % 2 === 0 ? "light-cell" : "dark-cell") +
                              (isConflict ? " conflict-cell" : "")
                            }
                            onClick={() => handleCellClick(row, col)}
                            title={getCoord(row, col)}
                          >
                            {colValue === col ? "♛" : ""}
                          </div>
                        );
                      })}
                    </div>
                  ))}

                  <div className="board-header">
                    <div className="corner-cell"></div>
                    {Array.from({ length: 16 }).map((_, col) => (
                      <div key={col} className="header-cell">
                        {String.fromCharCode(65 + col)}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="right-side">
            <div className="form-box">
              <div className="section-title">
                <h2>Submit Your Answer</h2>
                <p>Enter your name and 16 comma-separated values from 0 to 15.</p>
              </div>

              <form onSubmit={submitAnswer}>
                <input
                  type="text"
                  placeholder="Enter player name"
                  value={playerName}
                  onChange={(e) => setPlayerName(e.target.value)}
                />

                <textarea
                  placeholder="Example: 1,3,5,7,9,11,13,15,0,2,4,6,8,10,12,14"
                  value={answerText}
                  onChange={handleAnswerTextChange}
                ></textarea>

                <button type="submit">Submit Answer</button>
              </form>
            </div>

            <div className="cards-box">
              {sequentialResult && (
                <div className="result-card sequential-card">
                  <h3>Sequential Result</h3>
                  <p>
                    <strong>Solutions:</strong> {sequentialResult.solutionCount}
                  </p>
                  <p>
                    <strong>Time:</strong> {sequentialResult.timeTakenMs} ms
                  </p>
                </div>
              )}

              {threadedResult && (
                <div className="result-card threaded-card">
                  <h3>Threaded Result</h3>
                  <p>
                    <strong>Solutions:</strong> {threadedResult.solutionCount}
                  </p>
                  <p>
                    <strong>Time:</strong> {threadedResult.timeTakenMs} ms
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="table-box">
          <h2>Algorithm Runs</h2>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Algorithm</th>
                  <th>Solutions</th>
                  <th>Time (ms)</th>
                </tr>
              </thead>
              <tbody>
                {results.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.algorithmType}</td>
                    <td>{r.solutionCount}</td>
                    <td>{r.timeTakenMs}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="table-box">
          <h2>Player Answers</h2>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Player Name</th>
                  <th>Answer Found</th>
                  {/* <th>Round</th> */}
                  {/* <th>Sequential (ns)</th> */}
                  {/* <th>Threaded (ns)</th> */}
                  {/* <th>Total (ns)</th> */}
                  {/* <th>Best Algorithm</th> */}
                </tr>
              </thead>
              <tbody>
                {answers.map((a) => (
                  <tr key={a.id}>
                    <td>{a.playerName}</td>
                    <td>{a.answerText}</td>
                    {/* <td>{a.roundNumber}</td> */}
                    {/* <td>{a.sequentialCheckTimeNs}</td> */}
                    {/* <td>{a.threadedCheckTimeNs}</td> */}
                    {/* <td>{a.totalCheckTimeNs}</td> */}
                    {/* <td>{a.bestAlgorithm}</td> */}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SixteenQueens;