import { useEffect, useState } from "react";
import api from "../services/api";
import "../styles/Sixteen_queen_puzzle.css";

function SixteenQueens() {
  const [playerName, setPlayerName] = useState("");
  const [answerText, setAnswerText] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [theme, setTheme] = useState("light");

  const [sequentialResult, setSequentialResult] = useState(null);
  const [threadedResult, setThreadedResult] = useState(null);

  const [results, setResults] = useState([]);
  const [answers, setAnswers] = useState([]);

  const [loading, setLoading] = useState(false);

  const [board, setBoard] = useState(Array(16).fill(-1));
  const [conflictCells, setConflictCells] = useState([]);

  function getCoord(row, col) {
    return String.fromCharCode(65 + col) + row;
  }

  function getConflicts(boardArray) {
    const conflicts = [];
    const cells = [];

    for (let i = 0; i < 16; i++) {
      for (let j = i + 1; j < 16; j++) {
        if (boardArray[i] === -1 || boardArray[j] === -1) {
          continue;
        }

        if (boardArray[i] === boardArray[j]) {
          conflicts.push(
            getCoord(i, boardArray[i]) +
              " and " +
              getCoord(j, boardArray[j]) +
              " are in the same column"
          );

          cells.push(i + "-" + boardArray[i]);
          cells.push(j + "-" + boardArray[j]);
        }

        if (Math.abs(boardArray[i] - boardArray[j]) === Math.abs(i - j)) {
          conflicts.push(
            getCoord(i, boardArray[i]) +
              " and " +
              getCoord(j, boardArray[j]) +
              " are in the same diagonal"
          );

          cells.push(i + "-" + boardArray[i]);
          cells.push(j + "-" + boardArray[j]);
        }
      }
    }

    const uniqueCells = [...new Set(cells)];

    return {
      conflicts: conflicts,
      cells: uniqueCells,
    };
  }

  function updateBoardMessage(newBoard) {
    const result = getConflicts(newBoard);
    const conflicts = result.conflicts;
    const cells = result.cells;

    setConflictCells(cells);

    let count = 0;
    for (let i = 0; i < newBoard.length; i++) {
      if (newBoard[i] !== -1) {
        count++;
      }
    }

    if (count === 0) {
      setMessage("");
      setIsError(false);
      return;
    }

    if (conflicts.length > 0) {
      setMessage("Conflict found: " + conflicts.slice(0, 3).join(" | "));
      setIsError(true);
    } else {
      setMessage("No conflicts so far.");
      setIsError(false);
    }
  }

  function handleCellClick(row, col) {
    const newBoard = [...board];
    newBoard[row] = col;

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
      setMessage("Sequential completed");
      setIsError(false);
      await loadResults();
    } catch (error) {
      console.log(error);
      setMessage("Error running sequential");
      setIsError(true);
    }

    setLoading(false);
  }

  async function runThreaded() {
    setLoading(true);

    try {
      const res = await api.post("/run-threaded");
      setThreadedResult(res.data);
      setMessage("Threaded completed");
      setIsError(false);
      await loadResults();
    } catch (error) {
      console.log(error);
      setMessage("Error running threaded");
      setIsError(true);
    }

    setLoading(false);
  }

  async function resetAnswers() {
    setLoading(true);

    try {
      const res = await api.post("/reset");
      setMessage(res.data.message || "Reset completed");
      setIsError(false);
      setPlayerName("");
      setAnswerText("");
      setBoard(Array(16).fill(-1));
      setConflictCells([]);
      await loadAnswers();
      await loadResults();
    } catch (error) {
      console.log(error);
      setMessage("Error resetting answers");
      setIsError(true);
    }

    setLoading(false);
  }

  async function submitAnswer(e) {
    e.preventDefault();

    if (playerName.trim() === "" || answerText.trim() === "") {
      setMessage("Please fill player name and answer");
      setIsError(true);
      return;
    }

    try {
      const res = await api.post("/submit-answer", {
        playerName: playerName,
        answerText: answerText,
      });

      setMessage(res.data.message);
      setIsError(false);
      setPlayerName("");
      setAnswerText("");
      setBoard(Array(16).fill(-1));
      setConflictCells([]);
      await loadAnswers();
    } catch (error) {
      console.log(error);
      setMessage(error?.response?.data?.message || "Error");
      setIsError(true);
    }
  }

  function toggleTheme() {
    if (theme === "light") {
      setTheme("dark");
    } else {
      setTheme("light");
    }
  }

  useEffect(() => {
    const timer = setTimeout(() => {
      loadResults();
      loadAnswers();
    }, 0);

    return () => clearTimeout(timer);
  }, []);

  return (
    <div className={"page " + theme}>
      <div className="top-bar">
        <button className="theme-btn" onClick={toggleTheme}>
          {theme === "light" ? "Dark Mode" : "Light Mode"}
        </button>
      </div>

      <div className="main-box">
        <div className="title-box">
          <h1>Sixteen Queens Puzzle</h1>
          <p>
            Place 16 queens on the board so that no two queens attack each
            other.
          </p>
        </div>

        <div className="button-group">
          <button onClick={runSequential}>Run Sequential</button>
          <button onClick={runThreaded}>Run Threaded</button>
          <button onClick={resetAnswers}>Reset</button>
        </div>

        {loading && <div className="loader">Running...</div>}

        {message && (
          <div className={isError ? "message error" : "message success"}>
            {message}
          </div>
        )}

        <div className="main-content">
          <div className="left-side">
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
                    const isConflict = conflictCells.includes(row + "-" + col);

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

          <div className="right-side">
            <div className="form-box">
              <h2>Submit Answer</h2>
              <form onSubmit={submitAnswer}>
                <input
                  type="text"
                  placeholder="Enter player name"
                  value={playerName}
                  onChange={(e) => setPlayerName(e.target.value)}
                />

                <textarea
                  placeholder="Type like 0,2,4,1,..."
                  value={answerText}
                  onChange={handleAnswerTextChange}
                ></textarea>

                <button type="submit">Submit</button>
              </form>
            </div>

            <div className="cards-box">
              {sequentialResult && (
                <div className="result-card sequential-card">
                  <h3>Sequential Result</h3>
                  <p>Solutions: {sequentialResult.solutionCount}</p>
                  <p>Time: {sequentialResult.timeTakenMs} ms</p>
                </div>
              )}

              {threadedResult && (
                <div className="result-card threaded-card">
                  <h3>Threaded Result</h3>
                  <p>Solutions: {threadedResult.solutionCount}</p>
                  <p>Time: {threadedResult.timeTakenMs} ms</p>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="table-box">
          <h2>Runs</h2>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Algorithm</th>
                  <th>Solutions</th>
                  <th>Time</th>
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
          <h2>Answers</h2>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Player</th>
                  <th>Answer</th>
                  <th>Correct</th>
                  <th>Recognized</th>
                </tr>
              </thead>
              <tbody>
                {answers.map((a) => (
                  <tr key={a.id}>
                    <td>{a.playerName}</td>
                    <td>{a.answerText}</td>
                    <td>{a.correct ? "Yes" : "No"}</td>
                    <td>{a.recognized ? "Yes" : "No"}</td>
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