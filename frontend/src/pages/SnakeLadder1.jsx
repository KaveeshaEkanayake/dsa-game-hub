/*import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const BOARD_SIZES = [6, 7, 8, 9, 10, 11, 12];
const API_URL = "http://localhost:8080";

export default function SnakeLadderGame() {
  const navigate = useNavigate();
  const canvasRef = useRef(null);
  const hubRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });
  const boardCanvasRef = useRef(null);

  // Game state
  const [playerName, setPlayerName] = useState("");
  const [boardSize, setBoardSize] = useState(8);
  const [gameState, setGameState] = useState("setup");
  const [gameData, setGameData] = useState(null);
  const [roundId, setRoundId] = useState(null);
  
  // Player positions
  const [playerPosition, setPlayerPosition] = useState(1);
  const [diceValue, setDiceValue] = useState(null);
  const [gameOver, setGameOver] = useState(false);
  const [winner, setWinner] = useState(null);
  const [isRolling, setIsRolling] = useState(false);
  
  // MCQ
  const [choices, setChoices] = useState([]);
  const [selectedAnswer, setSelectedAnswer] = useState(null);
  const [resultMessage, setResultMessage] = useState("");
  const [loading, setLoading] = useState(false);
  
  // Algorithm times
  const [algo1Time, setAlgo1Time] = useState(null);
  const [algo2Time, setAlgo2Time] = useState(null);
  
  // Board dimensions
  const [boardSizePx, setBoardSizePx] = useState(500);
  const [chartData, setChartData] = useState([]);
  const [showChart, setShowChart] = useState(false);
  const [roundCounter, setRoundCounter] = useState(0);

  // Starfield Background
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
      return { x: (Math.random() - 0.5) * W * 3, y: (Math.random() - 0.5) * H * 3, z: Math.random() * W, pz: 0 };
    }
    function resize() {
      W = canvas.width = hub.offsetWidth;
      H = canvas.height = hub.offsetHeight;
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
        ctx.beginPath();
        ctx.moveTo(px, py);
        ctx.lineTo(sx, sy);
        ctx.strokeStyle = `rgba(${bright},${bright},${Math.min(255, bright + 40)},0.9)`;
        ctx.lineWidth = size;
        ctx.stroke();
      }
      animId = requestAnimationFrame(draw);
    }
    const onMouseMove = (e) => {
      const r = hub.getBoundingClientRect();
      mouse.current.x = (e.clientX - r.left - W / 2) / W;
      mouse.current.y = (e.clientY - r.top - H / 2) / H;
    };
    resize();
    stars = Array.from({ length: NUM }, makeStar);
    draw();
    window.addEventListener("resize", resize);
    hub.addEventListener("mousemove", onMouseMove);
    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener("resize", resize);
      hub.removeEventListener("mousemove", onMouseMove);
    };
  }, []);

  // Update board size dynamically
  useEffect(() => {
    let newSize;
    if (boardSize <= 8) {
      newSize = 520;
    } else if (boardSize <= 10) {
      newSize = 560;
    } else {
      newSize = 600;
    }
    setBoardSizePx(newSize);
  }, [boardSize]);

  // ===============================
  // BACKEND API CALLS
  // ===============================
  
  const startGame = async () => {
    if (!playerName.trim()) {
      alert("Please enter your name!");
      return;
    }
    if (boardSize < 6 || boardSize > 12) {
      alert("Board size must be between 6 and 12!");
      return;
    }
    
    setLoading(true);
    setResultMessage("");
    
    try {
      const response = await fetch(`${API_URL}/api/snake-ladder/start`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ playerName: playerName, boardSize: boardSize }),
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }
      
      const data = await response.json();
      console.log("Backend start response:", data);
      
      setRoundId(data.roundId);
      const snakes = data.snakes || {};
      const ladders = data.ladders || {};
      const totalCells = data.boardSize * data.boardSize;
      const minThrows = data.correctAnswer;
      const options = data.options || [];
      
      setAlgo1Time(data.bfsTimeMs || 0);
      setAlgo2Time(data.dpTimeMs || 0);
      
      const newGameData = {
        boardSize: data.boardSize,
        totalCells: totalCells,
        snakes: snakes,
        ladders: ladders,
        minimumDiceThrows: minThrows,
      };
      
      setGameData(newGameData);
      setPlayerPosition(1);
      setDiceValue(null);
      setGameOver(false);
      setWinner(null);
      setChoices(options);
      setSelectedAnswer(null);
      setGameState("mcq");
      
    } catch (error) {
      console.error("Error starting game:", error);
      setResultMessage(`❌ Failed to connect to server. Make sure backend is running on ${API_URL}. Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };
  
  const checkAnswer = async () => {
    if (!selectedAnswer) {
      setResultMessage("Please select an answer!");
      return;
    }
    if (!roundId) {
      setResultMessage("No active game round. Please start a new game!");
      return;
    }
    
    setLoading(true);
    
    try {
      const response = await fetch(`${API_URL}/api/snake-ladder/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          playerName: playerName,
          answerText: selectedAnswer.toString(),
          roundId: roundId,
        }),
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }
      
      const data = await response.json();
      console.log("Submit response:", data);
      
      const isCorrect = data.correct;
      const message = data.message;
      setResultMessage(message);
      
      if (isCorrect) {
        if (data.sequentialCheckTimeMs) setAlgo1Time(data.sequentialCheckTimeMs);
        if (data.threadedCheckTimeMs) setAlgo2Time(data.threadedCheckTimeMs);
        
        setTimeout(() => {
          setResultMessage("");
          setGameState("playing");
        }, 1500);
      } else {
        setTimeout(() => setResultMessage(""), 2000);
      }
    } catch (error) {
      console.error("Error submitting answer:", error);
      setResultMessage(`❌ Failed to submit answer. Error: ${error.message}`);
      setTimeout(() => setResultMessage(""), 3000);
    } finally {
      setLoading(false);
    }
  };

  const rollDice = () => {
    if (!gameData || gameOver || isRolling) return;
    setIsRolling(true);
    
    let rollCount = 0;
    const maxRolls = 10;
    const interval = setInterval(() => {
      const randomDice = Math.floor(Math.random() * 6) + 1;
      setDiceValue(randomDice);
      rollCount++;
      if (rollCount >= maxRolls) {
        clearInterval(interval);
        const finalDice = Math.floor(Math.random() * 6) + 1;
        setDiceValue(finalDice);
        processMove(finalDice);
        setIsRolling(false);
      }
    }, 60);
  };
  
  const processMove = (dice) => {
    let newPos = playerPosition + dice;
    if (newPos > gameData.totalCells) newPos = gameData.totalCells;
    
    let message = "";
    if (gameData.snakes[newPos]) {
      message = `🐍 Snake! ${newPos} → ${gameData.snakes[newPos]}`;
      newPos = gameData.snakes[newPos];
    } else if (gameData.ladders[newPos]) {
      message = `🪜 Ladder! ${newPos} → ${gameData.ladders[newPos]}`;
      newPos = gameData.ladders[newPos];
    }
    
    if (message) {
      setResultMessage(message);
      setTimeout(() => setResultMessage(""), 1500);
    }
    
    setPlayerPosition(newPos);
    
    if (newPos === gameData.totalCells) {
      const newRoundNumber = roundCounter + 1;
      setRoundCounter(newRoundNumber);
      setChartData(prevData => [...prevData, {
        round: newRoundNumber,
        boardSize: gameData.boardSize,
        bfsTime: algo1Time,
        dpTime: algo2Time,
        winner: algo1Time <= algo2Time ? "BFS" : "DP"
      }]);
      setShowChart(true);
      setWinner(1);
      setGameOver(true);
      setGameState("result");
      return;
    }
  };

  const getPosition = (cell) => {
    if (!gameData) return { x: 0, y: 0 };
    const size = gameData.boardSize;
    const rowIndex = Math.floor((cell - 1) / size);
    const colIndex = (cell - 1) % size;
    const row = size - 1 - rowIndex;
    const col = rowIndex % 2 === 0 ? colIndex : size - 1 - colIndex;
    const cellSize = boardSizePx / size;
    return { x: col * cellSize + cellSize / 2, y: row * cellSize + cellSize / 2 };
  };

  // ===============================
  // IMPROVED SNAKE DRAWING with 3D Head
  // ===============================
  const drawSnake = (ctx, headPos, tailPos, cellSize) => {
    const midX = (headPos.x + tailPos.x) / 2;
    const midY = (headPos.y + tailPos.y) / 2;
    const distance = Math.hypot(headPos.x - tailPos.x, headPos.y - tailPos.y);
    const curveOffset = Math.min(distance * 0.35, cellSize * 1.2);
    
    let cp1x = (headPos.x + midX) / 2;
    let cp1y = (headPos.y + midY) / 2;
    let cp2x = (midX + tailPos.x) / 2;
    let cp2y = (midY + tailPos.y) / 2;
    
    if (Math.abs(headPos.x - tailPos.x) > Math.abs(headPos.y - tailPos.y)) {
      cp1y += curveOffset;
      cp2y -= curveOffset;
    } else {
      cp1x += curveOffset;
      cp2x -= curveOffset;
    }
    
    ctx.shadowBlur = 4;
    ctx.shadowColor = "rgba(0,0,0,0.5)";
    
    // Snake body with gradient
    const bodyGradient = ctx.createLinearGradient(headPos.x, headPos.y, tailPos.x, tailPos.y);
    bodyGradient.addColorStop(0, "#4CAF50");
    bodyGradient.addColorStop(1, "#2E7D32");
    
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y);
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, tailPos.x, tailPos.y);
    ctx.strokeStyle = bodyGradient;
    ctx.lineWidth = Math.max(8, cellSize * 0.14);
    ctx.stroke();
    
    // Scale pattern
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y);
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, tailPos.x, tailPos.y);
    ctx.strokeStyle = "#1B5E20";
    ctx.lineWidth = Math.max(3, cellSize * 0.07);
    ctx.setLineDash([Math.max(6, cellSize * 0.12), Math.max(8, cellSize * 0.14)]);
    ctx.stroke();
    ctx.setLineDash([]);
    
    // 3D Snake Head
    ctx.shadowBlur = 3;
    ctx.beginPath();
    ctx.ellipse(headPos.x, headPos.y, Math.max(9, cellSize * 0.18), Math.max(7, cellSize * 0.14), 0, 0, 2 * Math.PI);
    
    const headGradient = ctx.createRadialGradient(headPos.x - 3, headPos.y - 3, 3, headPos.x, headPos.y, 10);
    headGradient.addColorStop(0, "#66BB6A");
    headGradient.addColorStop(1, "#388E3C");
    ctx.fillStyle = headGradient;
    ctx.fill();
    ctx.strokeStyle = "#1B5E20";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // 3D Eyes
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.ellipse(headPos.x - cellSize * 0.08, headPos.y - cellSize * 0.06, Math.max(3.5, cellSize * 0.055), Math.max(4, cellSize * 0.06), 0, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.ellipse(headPos.x + cellSize * 0.08, headPos.y - cellSize * 0.06, Math.max(3.5, cellSize * 0.055), Math.max(4, cellSize * 0.06), 0, 0, 2 * Math.PI);
    ctx.fill();
    
    // Pupils
    ctx.fillStyle = "#000000";
    ctx.beginPath();
    ctx.ellipse(headPos.x - cellSize * 0.09, headPos.y - cellSize * 0.07, Math.max(2, cellSize * 0.035), Math.max(2.5, cellSize * 0.04), 0, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.ellipse(headPos.x + cellSize * 0.07, headPos.y - cellSize * 0.07, Math.max(2, cellSize * 0.035), Math.max(2.5, cellSize * 0.04), 0, 0, 2 * Math.PI);
    ctx.fill();
    
    // Eye shine
    ctx.fillStyle = "white";
    ctx.beginPath();
    ctx.arc(headPos.x - cellSize * 0.11, headPos.y - cellSize * 0.09, Math.max(1, cellSize * 0.02), 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(headPos.x + cellSize * 0.05, headPos.y - cellSize * 0.09, Math.max(1, cellSize * 0.02), 0, 2 * Math.PI);
    ctx.fill();
    
    // Nostrils
    ctx.fillStyle = "#1B5E20";
    ctx.beginPath();
    ctx.arc(headPos.x - cellSize * 0.04, headPos.y + cellSize * 0.03, Math.max(1.5, cellSize * 0.025), 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(headPos.x + cellSize * 0.04, headPos.y + cellSize * 0.03, Math.max(1.5, cellSize * 0.025), 0, 2 * Math.PI);
    ctx.fill();
    
    // Tongue
    ctx.beginPath();
    ctx.moveTo(headPos.x + cellSize * 0.1, headPos.y + cellSize * 0.05);
    ctx.lineTo(headPos.x + cellSize * 0.18, headPos.y + cellSize * 0.02);
    ctx.moveTo(headPos.x + cellSize * 0.1, headPos.y + cellSize * 0.05);
    ctx.lineTo(headPos.x + cellSize * 0.18, headPos.y + cellSize * 0.09);
    ctx.strokeStyle = "#FF4444";
    ctx.lineWidth = Math.max(2, cellSize * 0.04);
    ctx.stroke();
    
    ctx.shadowBlur = 0;
  };

  // ===============================
  // IMPROVED LADDER DRAWING with 3D effect
  // ===============================
  const drawLadder = (ctx, bottomPos, topPos, cellSize) => {
    const dx = topPos.x - bottomPos.x;
    const dy = topPos.y - bottomPos.y;
    const railOffset = Math.max(10, cellSize * 0.15);
    const perpX = -Math.sin(Math.atan2(dy, dx)) * railOffset;
    const perpY = Math.cos(Math.atan2(dy, dx)) * railOffset;
    
    ctx.shadowBlur = 4;
    ctx.shadowColor = "rgba(0,0,0,0.4)";
    
    // Left rail with gradient
    const railGradient1 = ctx.createLinearGradient(bottomPos.x, bottomPos.y, topPos.x, topPos.y);
    railGradient1.addColorStop(0, "#8B4513");
    railGradient1.addColorStop(1, "#654321");
    ctx.beginPath();
    ctx.moveTo(bottomPos.x + perpX, bottomPos.y + perpY);
    ctx.lineTo(topPos.x + perpX, topPos.y + perpY);
    ctx.strokeStyle = railGradient1;
    ctx.lineWidth = Math.max(6, cellSize * 0.1);
    ctx.stroke();
    
    // Right rail
    ctx.beginPath();
    ctx.moveTo(bottomPos.x - perpX, bottomPos.y - perpY);
    ctx.lineTo(topPos.x - perpX, topPos.y - perpY);
    ctx.stroke();
    
    // Rail highlights
    ctx.beginPath();
    ctx.moveTo(bottomPos.x + perpX - 2, bottomPos.y + perpY - 2);
    ctx.lineTo(topPos.x + perpX - 2, topPos.y + perpY - 2);
    ctx.strokeStyle = "#A0522D";
    ctx.lineWidth = Math.max(2, cellSize * 0.04);
    ctx.stroke();
    
    // Rungs with 3D effect
    const numRungs = Math.min(6, Math.max(4, Math.floor(cellSize * 0.8)));
    for (let i = 1; i <= numRungs; i++) {
      const t = i / (numRungs + 1);
      const rungX = bottomPos.x + dx * t;
      const rungY = bottomPos.y + dy * t;
      
      ctx.beginPath();
      ctx.moveTo(rungX + perpX, rungY + perpY);
      ctx.lineTo(rungX - perpX, rungY - perpY);
      ctx.strokeStyle = "#CD853F";
      ctx.lineWidth = Math.max(4, cellSize * 0.07);
      ctx.stroke();
      
      // Rung highlight
      ctx.beginPath();
      ctx.moveTo(rungX + perpX - 1.5, rungY + perpY - 1.5);
      ctx.lineTo(rungX - perpX - 1.5, rungY - perpY - 1.5);
      ctx.strokeStyle = "#DEB887";
      ctx.lineWidth = Math.max(1.5, cellSize * 0.03);
      ctx.stroke();
    }
    
    // Gold top decoration with glow
    ctx.shadowBlur = 8;
    ctx.shadowColor = "#FFD700";
    ctx.beginPath();
    ctx.ellipse(topPos.x, topPos.y, Math.max(8, cellSize * 0.14), Math.max(7, cellSize * 0.12), 0, 0, 2 * Math.PI);
    ctx.fillStyle = "#FFD700";
    ctx.fill();
    ctx.fillStyle = "#B8860B";
    ctx.font = `bold ${Math.max(14, Math.floor(cellSize * 0.28))}px "Segoe UI"`;
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("★", topPos.x, topPos.y);
    
    ctx.shadowBlur = 0;
  };

  // ===============================
  // IMPROVED BOARD DRAWING
  // ===============================
  const drawBoard = () => {
    if (!gameData || !boardCanvasRef.current) return;
    const canvas = boardCanvasRef.current;
    const ctx = canvas.getContext("2d");
    const size = gameData.boardSize;
    const totalCells = gameData.totalCells;
    const cellSize = boardSizePx / size;
    
    canvas.width = boardSizePx;
    canvas.height = boardSizePx;
    
    // Premium dark wood background with gradient
    const woodGradient = ctx.createLinearGradient(0, 0, boardSizePx, boardSizePx);
    woodGradient.addColorStop(0, "#5D3A1A");
    woodGradient.addColorStop(0.5, "#4A2E12");
    woodGradient.addColorStop(1, "#3E2510");
    ctx.fillStyle = woodGradient;
    ctx.fillRect(0, 0, boardSizePx, boardSizePx);
    
    // Golden border with 3D effect
    ctx.shadowBlur = 5;
    ctx.shadowColor = "rgba(0,0,0,0.5)";
    ctx.strokeStyle = "#DAA520";
    ctx.lineWidth = 4;
    ctx.strokeRect(4, 4, boardSizePx - 8, boardSizePx - 8);
    ctx.strokeStyle = "#FFD700";
    ctx.lineWidth = 1.5;
    ctx.strokeRect(2, 2, boardSizePx - 4, boardSizePx - 4);
    ctx.shadowBlur = 0;
    
    // Grid lines with gold color
    ctx.beginPath();
    ctx.strokeStyle = "#DAA520";
    ctx.lineWidth = Math.max(1.5, boardSizePx / 400);
    for (let i = 0; i <= size; i++) {
      ctx.moveTo(i * cellSize, 0);
      ctx.lineTo(i * cellSize, boardSizePx);
      ctx.stroke();
      ctx.moveTo(0, i * cellSize);
      ctx.lineTo(boardSizePx, i * cellSize);
      ctx.stroke();
    }
    
    // Cell backgrounds with alternating colors
    for (let cell = 1; cell <= totalCells; cell++) {
      const pos = getPosition(cell);
      const row = Math.floor((cell - 1) / size);
      const isEvenRow = row % 2 === 0;
      const isEvenCol = ((cell - 1) % size) % 2 === 0;
      const isLightCell = (isEvenRow === isEvenCol);
      
      ctx.beginPath();
      ctx.rect(pos.x - cellSize / 2, pos.y - cellSize / 2, cellSize, cellSize);
      
      const cellGradient = ctx.createLinearGradient(
        pos.x - cellSize / 2, pos.y - cellSize / 2,
        pos.x + cellSize / 2, pos.y + cellSize / 2
      );
      
      if (isLightCell) {
        cellGradient.addColorStop(0, "#F5E6C8");
        cellGradient.addColorStop(1, "#E8D5A8");
      } else {
        cellGradient.addColorStop(0, "#D4C4A0");
        cellGradient.addColorStop(1, "#C4B490");
      }
      ctx.fillStyle = cellGradient;
      ctx.fill();
      ctx.strokeStyle = "#DAA520";
      ctx.lineWidth = 0.8;
      ctx.stroke();
    }
    
    // Draw Snakes
    if (gameData.snakes) {
      for (const [head, tail] of Object.entries(gameData.snakes)) {
        const headPos = getPosition(parseInt(head));
        const tailPos = getPosition(tail);
        drawSnake(ctx, headPos, tailPos, cellSize);
      }
    }
    
    // Draw Ladders
    if (gameData.ladders) {
      for (const [bottom, top] of Object.entries(gameData.ladders)) {
        const bottomPos = getPosition(parseInt(bottom));
        const topPos = getPosition(top);
        drawLadder(ctx, bottomPos, topPos, cellSize);
      }
    }
    
    // Cell numbers with 3D effect
    const fontSize = Math.max(12, Math.min(22, Math.floor(cellSize * 0.3)));
    for (let cell = 1; cell <= totalCells; cell++) {
      const pos = getPosition(cell);
      
      // Number background circle
      ctx.beginPath();
      ctx.arc(pos.x, pos.y, Math.max(9, cellSize * 0.25), 0, 2 * Math.PI);
      ctx.fillStyle = "#8B0000";
      ctx.fill();
      ctx.strokeStyle = "#FFD700";
      ctx.lineWidth = 1.5;
      ctx.stroke();
      
      ctx.font = `bold ${fontSize}px "Segoe UI", "Poppins"`;
      ctx.fillStyle = "#FFFFFF";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(cell.toString(), pos.x, pos.y);
      
      // Special cells decoration
      if (cell === 1) {
        ctx.font = `${Math.max(10, fontSize - 2)}px "Segoe UI"`;
        ctx.fillStyle = "#FFD700";
        ctx.fillText("🏁", pos.x + cellSize * 0.28, pos.y - cellSize * 0.25);
      }
      if (cell === totalCells) {
        ctx.font = `${Math.max(10, fontSize - 2)}px "Segoe UI"`;
        ctx.fillStyle = "#FFD700";
        ctx.fillText("🏆", pos.x + cellSize * 0.28, pos.y - cellSize * 0.25);
      }
    }
  };
  
  // ===============================
  // IMPROVED 3D PLAYER CHARACTER
  // ===============================
  const drawPlayers = () => {
    if (!gameData || !boardCanvasRef.current) return;
    const canvas = boardCanvasRef.current;
    const ctx = canvas.getContext("2d");
    const cellSize = boardSizePx / gameData.boardSize;
    const playerSize = Math.max(16, cellSize * 0.38);
    
    const playerPos = getPosition(playerPosition);
    
    ctx.shadowBlur = 12;
    ctx.shadowColor = "rgba(255,215,0,0.6)";
    
    // 3D Player Body (Circle with gradient)
    const playerGradient = ctx.createRadialGradient(
      playerPos.x - playerSize * 0.25, 
      playerPos.y - playerSize * 0.25, 
      5,
      playerPos.x, 
      playerPos.y, 
      playerSize
    );
    playerGradient.addColorStop(0, "#FFD700");
    playerGradient.addColorStop(0.4, "#FFA500");
    playerGradient.addColorStop(1, "#FF8C00");
    
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize, 0, 2 * Math.PI);
    ctx.fillStyle = playerGradient;
    ctx.fill();
    
    // Inner glow
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize - 3, 0, 2 * Math.PI);
    ctx.fillStyle = "rgba(255,255,255,0.2)";
    ctx.fill();
    
    ctx.strokeStyle = "#FFFFFF";
    ctx.lineWidth = 2.5;
    ctx.stroke();
    
    // 3D Eyes
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.25, playerPos.y - playerSize * 0.15, playerSize * 0.18, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.25, playerPos.y - playerSize * 0.15, playerSize * 0.18, 0, 2 * Math.PI);
    ctx.fill();
    
    // Pupils
    ctx.fillStyle = "#1a1a2e";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.28, playerPos.y - playerSize * 0.18, playerSize * 0.09, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.22, playerPos.y - playerSize * 0.18, playerSize * 0.09, 0, 2 * Math.PI);
    ctx.fill();
    
    // Eye shine
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.32, playerPos.y - playerSize * 0.22, playerSize * 0.04, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.18, playerPos.y - playerSize * 0.22, playerSize * 0.04, 0, 2 * Math.PI);
    ctx.fill();
    
    // Happy Mouth
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y + playerSize * 0.1, playerSize * 0.12, 0.1, Math.PI - 0.1);
    ctx.strokeStyle = "#8B4513";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // Crown
    ctx.fillStyle = "#FFD700";
    ctx.shadowBlur = 5;
    ctx.beginPath();
    ctx.moveTo(playerPos.x - playerSize * 0.35, playerPos.y - playerSize * 0.45);
    ctx.lineTo(playerPos.x - playerSize * 0.2, playerPos.y - playerSize * 0.55);
    ctx.lineTo(playerPos.x, playerPos.y - playerSize * 0.48);
    ctx.lineTo(playerPos.x + playerSize * 0.2, playerPos.y - playerSize * 0.55);
    ctx.lineTo(playerPos.x + playerSize * 0.35, playerPos.y - playerSize * 0.45);
    ctx.fill();
    
    ctx.fillStyle = "#FF4444";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.1, playerPos.y - playerSize * 0.52, playerSize * 0.05, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.1, playerPos.y - playerSize * 0.52, playerSize * 0.05, 0, 2 * Math.PI);
    ctx.fill();
    ctx.fillStyle = "#00FF00";
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y - playerSize * 0.55, playerSize * 0.05, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.shadowBlur = 0;
    
    // Outer glow ring
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize + 6, 0, 2 * Math.PI);
    ctx.strokeStyle = "rgba(255, 215, 0, 0.4)";
    ctx.lineWidth = 2;
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize + 10, 0, 2 * Math.PI);
    ctx.strokeStyle = "rgba(255, 215, 0, 0.2)";
    ctx.lineWidth = 1.5;
    ctx.stroke();
  };
  
  // Draw board whenever gameData changes
  useEffect(() => {
    if (gameData && (gameState === "mcq" || gameState === "playing")) {
      drawBoard();
      drawPlayers();
    }
  }, [gameData, playerPosition, boardSizePx, gameState]);
  
  const resetGame = () => {
    setGameState("setup");
    setGameData(null);
    setRoundId(null);
    setPlayerPosition(1);
    setDiceValue(null);
    setGameOver(false);
    setWinner(null);
    setSelectedAnswer(null);
    setResultMessage("");
    setPlayerName("");
    setAlgo1Time(null);
    setAlgo2Time(null);
    setChoices([]);
  };

  const clearChartHistory = () => {
    setChartData([]);
    setRoundCounter(0);
    setShowChart(false);
  };

  const PerformanceChart = () => {
    if (!showChart || chartData.length === 0) return null;
    
    const avgBfsTime = (chartData.reduce((sum, d) => sum + d.bfsTime, 0) / chartData.length).toFixed(2);
    const avgDpTime = (chartData.reduce((sum, d) => sum + d.dpTime, 0) / chartData.length).toFixed(2);
    const dpFasterCount = chartData.filter(d => d.dpTime < d.bfsTime).length;
    
    return (
      <div style={{
        position: "fixed",
        top: "20px",
        right: "20px",
        width: "380px",
        maxHeight: "85vh",
        background: "rgba(13,59,110,0.95)",
        backdropFilter: "blur(10px)",
        borderRadius: "16px",
        border: "1px solid #FFD700",
        boxShadow: "0 10px 30px rgba(0,0,0,0.5)",
        zIndex: 1000,
        overflowY: "auto",
        padding: "12px"
      }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "10px", borderBottom: "1px solid rgba(255,255,255,0.2)", paddingBottom: "8px" }}>
          <div>
            <span style={{ fontSize: "16px", marginRight: "5px" }}>📊</span>
            <span style={{ color: "#FFD700", fontSize: "14px", fontWeight: "bold" }}>Performance History</span>
            <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "10px", marginLeft: "5px" }}>({chartData.length} games)</span>
          </div>
          <div style={{ display: "flex", gap: "8px" }}>
            <button onClick={clearChartHistory} style={{ background: "rgba(255,255,255,0.1)", border: "none", color: "#fff", fontSize: "10px", padding: "3px 8px", borderRadius: "4px", cursor: "pointer" }}>🗑️ Clear</button>
            <button onClick={() => setShowChart(false)} style={{ background: "rgba(255,255,255,0.1)", border: "none", color: "#fff", fontSize: "16px", width: "24px", height: "24px", borderRadius: "4px", cursor: "pointer" }}>✕</button>
          </div>
        </div>
        
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "8px", marginBottom: "12px", padding: "8px", background: "rgba(0,0,0,0.4)", borderRadius: "8px" }}>
          <div style={{ textAlign: "center" }}><p style={{ fontSize: "9px", color: "rgba(255,255,255,0.6)" }}>Avg BFS</p><p style={{ fontSize: "14px", fontWeight: "bold", color: "#FFD700" }}>{avgBfsTime}ms</p></div>
          <div style={{ textAlign: "center" }}><p style={{ fontSize: "9px", color: "rgba(255,255,255,0.6)" }}>Avg DP</p><p style={{ fontSize: "14px", fontWeight: "bold", color: "#4ade80" }}>{avgDpTime}ms</p></div>
          <div style={{ textAlign: "center" }}><p style={{ fontSize: "9px", color: "rgba(255,255,255,0.6)" }}>DP Faster</p><p style={{ fontSize: "14px", fontWeight: "bold", color: "#378ADD" }}>{chartData.length > 0 ? ((dpFasterCount / chartData.length) * 100).toFixed(0) : 0}%</p></div>
        </div>
        
        <div style={{ height: "180px", marginBottom: "12px" }}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData} margin={{ top: 5, right: 5, left: 0, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
              <XAxis dataKey="round" stroke="#fff" fontSize={10} tick={{ fill: '#fff' }} />
              <YAxis stroke="#fff" fontSize={10} tick={{ fill: '#fff' }} />
              <Tooltip contentStyle={{ backgroundColor: '#1a1a2e', border: '1px solid #FFD700', fontSize: '10px' }} labelStyle={{ color: '#FFD700' }} formatter={(value, name) => [`${value.toFixed(2)} ms`, name]} />
              <Line type="monotone" dataKey="bfsTime" stroke="#FFD700" name="BFS" strokeWidth={2} dot={{ r: 2 }} />
              <Line type="monotone" dataKey="dpTime" stroke="#4ade80" name="DP" strokeWidth={2} dot={{ r: 2 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
        
        <div style={{ maxHeight: "200px", overflowY: "auto", fontSize: "10px" }}>
          <table style={{ width: "100%", color: "#fff", borderCollapse: "collapse" }}>
            <thead style={{ position: "sticky", top: 0, background: "#0d3b6e" }}>
              <tr><th style={{ padding: "4px" }}>#</th><th style={{ padding: "4px" }}>Size</th><th style={{ padding: "4px" }}>BFS</th><th style={{ padding: "4px" }}>DP</th><th style={{ padding: "4px" }}>🏆</th></tr>
            </thead>
            <tbody>
              {chartData.map((row) => (
                <tr key={row.round} style={{ textAlign: "center", borderBottom: "1px solid rgba(255,255,255,0.1)" }}>
                  <td style={{ padding: "3px" }}>{row.round}</td>
                  <td style={{ padding: "3px" }}>{row.boardSize}</td>
                  <td style={{ padding: "3px", color: "#FFD700" }}>{row.bfsTime.toFixed(1)}</td>
                  <td style={{ padding: "3px", color: "#4ade80" }}>{row.dpTime.toFixed(1)}</td>
                  <td style={{ padding: "3px", color: row.dpTime < row.bfsTime ? "#4ade80" : "#FFD700" }}>{row.dpTime < row.bfsTime ? "DP" : "BFS"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  return (
    <div ref={hubRef} style={{ background: "#050510", minHeight: "100vh", position: "relative", overflow: "hidden", fontFamily: "'Segoe UI', 'Poppins', sans-serif" }}>
      <canvas ref={canvasRef} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", pointerEvents: "none" }} />
      <PerformanceChart />
      
      <div style={{ position: "relative", zIndex: 1, padding: "1.5rem" }}>
        <button onClick={() => navigate("/")} style={{ background: "rgba(255,255,255,0.1)", border: "1px solid rgba(255,255,255,0.2)", color: "#fff", padding: "8px 16px", borderRadius: "20px", cursor: "pointer", fontSize: "13px", marginBottom: "1.5rem" }}>← Back</button>
        
        <div style={{ textAlign: "center", marginBottom: "1.5rem" }}>
          <img src="https://png.pngtree.com/png-clipart/20240822/original/pngtree-lovable-green-snake-with-big-eyes-png-image_15828447.png" alt="Snake" style={{ width: "65px", height: "65px", marginBottom: "8px", filter: "drop-shadow(3px 5px 8px rgba(0,0,0,0.4)) drop-shadow(0 0 10px rgba(76,175,80,0.6))", animation: "float 3s ease-in-out infinite", cursor: "pointer" }} />
          <h1 style={{ fontSize: "26px", fontWeight: "bold", background: "linear-gradient(135deg, #FFD700, #FFA500)", WebkitBackgroundClip: "text", backgroundClip: "text", color: "transparent", letterSpacing: "-0.5px" }}>Snakes & Ladders</h1>
          <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.45)" }}>Roll the dice, climb ladders, avoid snakes!</p>
        </div>
        
        {gameState === "setup" && (
  <div style={{ 
    maxWidth: "420px", 
    margin: "0 auto", 
    background: "linear-gradient(135deg, rgba(7,61,46,0.9), rgba(5,45,35,0.95))", 
    border: "2px solid rgba(7,61,46,0.75)", 
    borderRadius: "16px", 
    padding: "1.5rem", 
    boxShadow: "0 10px 30px rgba(7,61,46,0.5)" 
  }}>
    <div style={{ marginBottom: "1.2rem" }}>
      <label style={{ display: "block", color: "#A5D6A7", fontSize: "12px", marginBottom: "6px", fontWeight: "bold" }}>Your Name</label>
      <input 
        value={playerName} 
        onChange={e => setPlayerName(e.target.value)} 
        placeholder="Enter your name" 
        style={{ 
          width: "100%", 
          padding: "10px 14px", 
          borderRadius: "8px", 
          border: "1px solid rgba(7,61,46,0.75)", 
          background: "rgba(0,0,0,0.3)", 
          color: "#fff", 
          fontSize: "14px", 
          outline: "none" 
        }} 
      />
    </div>
    
    <div style={{ marginBottom: "1.2rem" }}>
      <label style={{ display: "block", color: "#A5D6A7", fontSize: "12px", marginBottom: "6px", fontWeight: "bold" }}>Board Size (N × N)</label>
      <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
        {BOARD_SIZES.map(size => (
          <button 
            key={size} 
            onClick={() => setBoardSize(size)} 
            style={{ 
              padding: "6px 12px", 
              borderRadius: "8px", 
              border: `1px solid ${boardSize === size ? "rgba(7,61,46,0.75)" : "rgba(255,255,255,0.2)"}`, 
              background: boardSize === size ? "rgba(128, 202, 181, 0.5)" : "rgba(255,255,255,0.05)", 
              color: "#fff", 
              cursor: "pointer", 
              fontSize: "12px",
              transition: "all 0.2s ease"
            }}>
            {size}×{size}
          </button>
        ))}
      </div>
      <p style={{ fontSize: "10px", color: "#A5D6A7", marginTop: "8px" }}>
        🐍 Snakes: <span style={{ color: "#FF8A80" }}>{boardSize - 2}</span> | 🪜 Ladders: <span style={{ color: "#FFD54F" }}>{boardSize - 2}</span>
      </p>
    </div>
    
    <button 
      onClick={startGame} 
      disabled={loading} 
      style={{ 
        width: "100%", 
        padding: "12px", 
        borderRadius: "8px", 
        border: "none", 
        background: loading ? "rgba(7,61,46,0.6)" : "rgba(7,61,46,0.85)", 
        color: "#fff", 
        fontSize: "14px", 
        fontWeight: "bold", 
        cursor: loading ? "not-allowed" : "pointer", 
        opacity: loading ? 0.6 : 1, 
        boxShadow: "0 4px 15px rgba(7,61,46,0.4)",
        transition: "all 0.3s ease"
      }}>
      {loading ? "Connecting..." : "Start Game"}
    </button>
    
    {resultMessage && (
      <p style={{ color: "#f87171", fontSize: "12px", textAlign: "center", marginTop: "1rem" }}>
        {resultMessage}
      </p>
    )}
  </div>
)}
        
        {gameState === "mcq" && gameData && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
            <div style={{ maxWidth: "100%", overflowX: "auto", overflowY: "auto", display: "flex", justifyContent: "center", padding: "8px", background: "rgba(0,0,0,0.3)", borderRadius: "20px" }}>
              <div style={{ border: "3px solid #DAA520", borderRadius: "16px", overflow: "hidden", boxShadow: "0 15px 30px rgba(0,0,0,0.5)" }}>
                <canvas ref={boardCanvasRef} width={boardSizePx} height={boardSizePx} style={{ width: boardSizePx, height: boardSizePx, display: "block" }} />
              </div>
            </div>
            <div style={{ maxWidth: "450px", width: "100%", background: "rgba(55,138,221,0.3)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "1.2rem", textAlign: "center" }}>
              <div style={{ fontSize: "28px", marginBottom: "0.3rem" }}>🎯</div>
              <h2 style={{ color: "#fff", marginBottom: "0.3rem", fontSize: "1.1rem" }}>Puzzle Challenge</h2>
              <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.7)" }}>Minimum dice throws to reach cell {gameData.totalCells}?</p>
              <p style={{ fontSize: "10px", color: "rgba(255,255,255,0.5)", marginBottom: "1rem" }}>{gameData.boardSize}×{gameData.boardSize} | 🐍 {Object.keys(gameData.snakes || {}).length} | 🪜 {Object.keys(gameData.ladders || {}).length}</p>
              <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap", marginBottom: "1rem" }}>
                {choices.map((c, i) => (
                  <button key={i} onClick={() => setSelectedAnswer(c)} style={{ background: selectedAnswer === c ? "#FFD700" : "rgba(255,255,255,0.15)", border: "none", padding: "8px 20px", borderRadius: "25px", color: selectedAnswer === c ? "#333" : "#fff", fontSize: "1rem", fontWeight: "bold", cursor: "pointer" }}>{c}</button>
                ))}
              </div>
              <button onClick={checkAnswer} disabled={loading} style={{ background: "#FFD700", border: "none", padding: "8px 25px", borderRadius: "25px", fontWeight: "bold", fontSize: "0.9rem", cursor: loading ? "not-allowed" : "pointer", marginBottom: "0.8rem", opacity: loading ? 0.6 : 1 }}>{loading ? "Submitting..." : "Submit Answer"}</button>
              {resultMessage && <p style={{ color: resultMessage.includes("✅") ? "#4ade80" : "#f87171", fontSize: "12px" }}>{resultMessage}</p>}
              <div style={{ marginTop: "0.8rem", padding: "0.4rem", background: "rgba(0,0,0,0.3)", borderRadius: "8px" }}><p style={{ fontSize: "9px", color: "rgba(255,255,255,0.5)" }}>🎲 Answer correctly to start playing!</p></div>
            </div>
          </div>
        )}
        
        {gameState === "playing" && gameData && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
            <div style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "12px", padding: "10px 20px", display: "flex", gap: "15px", alignItems: "center", flexWrap: "wrap", justifyContent: "center" }}>
              <div style={{ color: "#fff", fontSize: "13px" }}>🎮 Player: <strong style={{ color: "#FFD700" }}>{playerName}</strong></div>
              <button onClick={rollDice} disabled={gameOver || isRolling} style={{ background: "#FFD700", border: "none", padding: "8px 25px", borderRadius: "35px", fontSize: "1rem", fontWeight: "bold", cursor: (gameOver || isRolling) ? "not-allowed" : "pointer", opacity: (gameOver || isRolling) ? 0.6 : 1 }}>🎲 Roll Dice</button>
              {diceValue && <div style={{ background: "rgba(255,255,255,0.15)", padding: "4px 15px", borderRadius: "25px", fontSize: "1.1rem", fontWeight: "bold", color: "#FFD700" }}>🎲 {diceValue}</div>}
            </div>
            {resultMessage && <div style={{ background: "rgba(0,0,0,0.7)", padding: "6px 16px", borderRadius: "25px", color: "#FFD700", fontSize: "12px" }}>{resultMessage}</div>}
            <div style={{ maxWidth: "100%", overflowX: "auto", overflowY: "auto", display: "flex", justifyContent: "center", padding: "8px", background: "rgba(0,0,0,0.3)", borderRadius: "20px" }}>
              <div style={{ border: "3px solid #DAA520", borderRadius: "16px", overflow: "hidden", boxShadow: "0 15px 30px rgba(0,0,0,0.5)" }}>
                <canvas ref={boardCanvasRef} width={boardSizePx} height={boardSizePx} style={{ width: boardSizePx, height: boardSizePx, display: "block" }} />
              </div>
            </div>
          </div>
        )}
        
        {gameState === "result" && (
          <div style={{ maxWidth: "350px", margin: "0 auto", textAlign: "center" }}>
            <div style={{ fontSize: "50px", marginBottom: "0.8rem" }}>🏆</div>
            <h2 style={{ fontSize: "20px", fontWeight: 500, color: "#4ade80", marginBottom: "6px" }}>Congratulations {playerName}!</h2>
            <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "12px", marginBottom: "1rem" }}>You reached the top! Great job!</p>
            {algo1Time !== null && algo2Time !== null && (
              <div style={{ background: "rgba(255,255,255,0.07)", borderRadius: "10px", padding: "0.8rem", marginBottom: "1rem", textAlign: "left" }}>
                <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "10px", marginBottom: "5px" }}>This Game's Performance</p>
                <p style={{ color: "#fff", fontSize: "11px" }}>⚡ BFS: <strong>{algo1Time}ms</strong></p>
                <p style={{ color: "#fff", fontSize: "11px" }}>📊 Dynamic Programming: <strong>{algo2Time}ms</strong></p>
                <p style={{ color: "#4ade80", fontSize: "10px", marginTop: "5px" }}>{algo1Time <= algo2Time ? "🏆 BFS was faster" : "🏆 DP was faster"}</p>
              </div>
            )}
            <div style={{ display: "flex", gap: "8px" }}>
              <button onClick={resetGame} style={{ flex: 1, padding: "10px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", fontSize: "12px", cursor: "pointer" }}>Play again</button>
              <button onClick={() => navigate("/")} style={{ flex: 1, padding: "10px", borderRadius: "8px", border: "none", background: "#185FA5", color: "#fff", fontSize: "12px", cursor: "pointer" }}>Back to hub</button>
            </div>
          </div>
        )}
      </div>
      
      <style>{`
        @keyframes float { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-8px); } }
      `}</style>
    </div>
  );
}*/

import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const BOARD_SIZES = [6, 7, 8, 9, 10, 11, 12];
const API_URL = "http://localhost:8080";

export default function SnakeLadderGame() {
  const navigate = useNavigate();
  const canvasRef = useRef(null);
  const hubRef = useRef(null);
  const mouse = useRef({ x: 0, y: 0 });
  const boardCanvasRef = useRef(null);

  // Game state
  const [playerName, setPlayerName] = useState("");
  const [boardSize, setBoardSize] = useState(8);
  const [gameState, setGameState] = useState("setup");
  const [gameData, setGameData] = useState(null);
  const [roundId, setRoundId] = useState(null);
  
  // Player positions
  const [playerPosition, setPlayerPosition] = useState(1);
  const [diceValue, setDiceValue] = useState(null);
  const [gameOver, setGameOver] = useState(false);
  const [winner, setWinner] = useState(null);
  const [isRolling, setIsRolling] = useState(false);
  
  // MCQ
  const [choices, setChoices] = useState([]);
  const [selectedAnswer, setSelectedAnswer] = useState(null);
  const [resultMessage, setResultMessage] = useState("");
  const [loading, setLoading] = useState(false);
  
  // Algorithm times (stored as microseconds from backend)
  const [algo1TimeMicro, setAlgo1TimeMicro] = useState(null);
  const [algo2TimeMicro, setAlgo2TimeMicro] = useState(null);
  
  // Board dimensions
  const [boardSizePx, setBoardSizePx] = useState(500);
  const [chartData, setChartData] = useState([]);
  const [showChart, setShowChart] = useState(false);
  const [roundCounter, setRoundCounter] = useState(0);

  // Starfield Background
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
      return { x: (Math.random() - 0.5) * W * 3, y: (Math.random() - 0.5) * H * 3, z: Math.random() * W, pz: 0 };
    }
    function resize() {
      W = canvas.width = hub.offsetWidth;
      H = canvas.height = hub.offsetHeight;
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
        ctx.beginPath();
        ctx.moveTo(px, py);
        ctx.lineTo(sx, sy);
        ctx.strokeStyle = `rgba(${bright},${bright},${Math.min(255, bright + 40)},0.9)`;
        ctx.lineWidth = size;
        ctx.stroke();
      }
      animId = requestAnimationFrame(draw);
    }
    const onMouseMove = (e) => {
      const r = hub.getBoundingClientRect();
      mouse.current.x = (e.clientX - r.left - W / 2) / W;
      mouse.current.y = (e.clientY - r.top - H / 2) / H;
    };
    resize();
    stars = Array.from({ length: NUM }, makeStar);
    draw();
    window.addEventListener("resize", resize);
    hub.addEventListener("mousemove", onMouseMove);
    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener("resize", resize);
      hub.removeEventListener("mousemove", onMouseMove);
    };
  }, []);

  // Update board size dynamically
  useEffect(() => {
    let newSize;
    if (boardSize <= 8) {
      newSize = 520;
    } else if (boardSize <= 10) {
      newSize = 560;
    } else {
      newSize = 600;
    }
    setBoardSizePx(newSize);
  }, [boardSize]);

  // ===============================
  // BACKEND API CALLS
  // ===============================
  
  const startGame = async () => {
    if (!playerName.trim()) {
      alert("Please enter your name!");
      return;
    }
    if (boardSize < 6 || boardSize > 12) {
      alert("Board size must be between 6 and 12!");
      return;
    }
    
    setLoading(true);
    setResultMessage("");
    
    try {
      const response = await fetch(`${API_URL}/api/snake-ladder/start`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ boardSize: boardSize }),
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }
      
      const data = await response.json();
      console.log("Backend start response:", data);
      
      setRoundId(data.roundId);
      const snakes = data.snakes || {};
      const ladders = data.ladders || {};
      const totalCells = data.boardSize * data.boardSize;
      const options = data.options || [];
      
      // Store times in microseconds (what backend sends)
      setAlgo1TimeMicro(data.bfsTimeMs || 0);
      setAlgo2TimeMicro(data.dpTimeMs || 0);
      
      const newGameData = {
        boardSize: data.boardSize,
        totalCells: totalCells,
        snakes: snakes,
        ladders: ladders,
        minimumDiceThrows: data.correctAnswer,
      };
      
      setGameData(newGameData);
      setPlayerPosition(1);
      setDiceValue(null);
      setGameOver(false);
      setWinner(null);
      setChoices(options);
      setSelectedAnswer(null);
      setGameState("mcq");
      
    } catch (error) {
      console.error("Error starting game:", error);
      setResultMessage(`❌ Failed to connect to server. Make sure backend is running on ${API_URL}. Error: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };
  
  const checkAnswer = async () => {
    if (!selectedAnswer) {
      setResultMessage("Please select an answer!");
      return;
    }
    if (!roundId) {
      setResultMessage("No active game round. Please start a new game!");
      return;
    }
    
    setLoading(true);
    
    try {
      const response = await fetch(`${API_URL}/api/snake-ladder/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          playerName: playerName,
          answerText: selectedAnswer.toString(),
          roundId: roundId,
        }),
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }
      
      const data = await response.json();
      console.log("Submit response:", data);
      
      const isCorrect = data.correct;
      const message = data.message;
      setResultMessage(message);
      
      if (isCorrect) {
        if (data.sequentialCheckTimeMs) setAlgo1TimeMicro(data.sequentialCheckTimeMs);
        if (data.threadedCheckTimeMs) setAlgo2TimeMicro(data.threadedCheckTimeMs);
        
        setTimeout(() => {
          setResultMessage("");
          setGameState("playing");
        }, 1500);
      } else {
        setTimeout(() => setResultMessage(""), 2000);
      }
    } catch (error) {
      console.error("Error submitting answer:", error);
      setResultMessage(`❌ Failed to submit answer. Error: ${error.message}`);
      setTimeout(() => setResultMessage(""), 3000);
    } finally {
      setLoading(false);
    }
  };

  const rollDice = () => {
    if (!gameData || gameOver || isRolling) return;
    setIsRolling(true);
    
    let rollCount = 0;
    const maxRolls = 10;
    const interval = setInterval(() => {
      const randomDice = Math.floor(Math.random() * 6) + 1;
      setDiceValue(randomDice);
      rollCount++;
      if (rollCount >= maxRolls) {
        clearInterval(interval);
        const finalDice = Math.floor(Math.random() * 6) + 1;
        setDiceValue(finalDice);
        processMove(finalDice);
        setIsRolling(false);
      }
    }, 60);
  };
  
  const processMove = (dice) => {
    let newPos = playerPosition + dice;
    if (newPos > gameData.totalCells) newPos = gameData.totalCells;
    
    let message = "";
    if (gameData.snakes[newPos]) {
      message = `🐍 Snake! ${newPos} → ${gameData.snakes[newPos]}`;
      newPos = gameData.snakes[newPos];
    } else if (gameData.ladders[newPos]) {
      message = `🪜 Ladder! ${newPos} → ${gameData.ladders[newPos]}`;
      newPos = gameData.ladders[newPos];
    }
    
    if (message) {
      setResultMessage(message);
      setTimeout(() => setResultMessage(""), 1500);
    }
    
    setPlayerPosition(newPos);
    
    if (newPos === gameData.totalCells) {
      const newRoundNumber = roundCounter + 1;
      setRoundCounter(newRoundNumber);
      
      // Store in MICROSECONDS (no conversion)
      const bfsTimeMicro = algo1TimeMicro || 0;
      const dpTimeMicro = algo2TimeMicro || 0;
      
      setChartData(prevData => [...prevData, {
        round: newRoundNumber,
        boardSize: gameData.boardSize,
        bfsTime: bfsTimeMicro,
        dpTime: dpTimeMicro,
        winner: bfsTimeMicro <= dpTimeMicro ? "BFS" : "DP"
      }]);
      setShowChart(true);
      setWinner(1);
      setGameOver(true);
      setGameState("result");
      return;
    }
  };

  const getPosition = (cell) => {
    if (!gameData) return { x: 0, y: 0 };
    const size = gameData.boardSize;
    const rowIndex = Math.floor((cell - 1) / size);
    const colIndex = (cell - 1) % size;
    const row = size - 1 - rowIndex;
    const col = rowIndex % 2 === 0 ? colIndex : size - 1 - colIndex;
    const cellSize = boardSizePx / size;
    return { x: col * cellSize + cellSize / 2, y: row * cellSize + cellSize / 2 };
  };

  // ===============================
  // SNAKE DRAWING
  // ===============================
  const drawSnake = (ctx, headPos, tailPos, cellSize) => {
    const midX = (headPos.x + tailPos.x) / 2;
    const midY = (headPos.y + tailPos.y) / 2;
    const distance = Math.hypot(headPos.x - tailPos.x, headPos.y - tailPos.y);
    const curveOffset = Math.min(distance * 0.35, cellSize * 1.2);
    
    let cp1x = (headPos.x + midX) / 2;
    let cp1y = (headPos.y + midY) / 2;
    let cp2x = (midX + tailPos.x) / 2;
    let cp2y = (midY + tailPos.y) / 2;
    
    if (Math.abs(headPos.x - tailPos.x) > Math.abs(headPos.y - tailPos.y)) {
      cp1y += curveOffset;
      cp2y -= curveOffset;
    } else {
      cp1x += curveOffset;
      cp2x -= curveOffset;
    }
    
    ctx.shadowBlur = 4;
    ctx.shadowColor = "rgba(0,0,0,0.5)";
    
    const bodyGradient = ctx.createLinearGradient(headPos.x, headPos.y, tailPos.x, tailPos.y);
    bodyGradient.addColorStop(0, "#4CAF50");
    bodyGradient.addColorStop(1, "#2E7D32");
    
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y);
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, tailPos.x, tailPos.y);
    ctx.strokeStyle = bodyGradient;
    ctx.lineWidth = Math.max(8, cellSize * 0.14);
    ctx.stroke();
    
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y);
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, tailPos.x, tailPos.y);
    ctx.strokeStyle = "#1B5E20";
    ctx.lineWidth = Math.max(3, cellSize * 0.07);
    ctx.setLineDash([Math.max(6, cellSize * 0.12), Math.max(8, cellSize * 0.14)]);
    ctx.stroke();
    ctx.setLineDash([]);
    
    ctx.beginPath();
    ctx.ellipse(headPos.x, headPos.y, Math.max(9, cellSize * 0.18), Math.max(7, cellSize * 0.14), 0, 0, 2 * Math.PI);
    const headGradient = ctx.createRadialGradient(headPos.x - 3, headPos.y - 3, 3, headPos.x, headPos.y, 10);
    headGradient.addColorStop(0, "#66BB6A");
    headGradient.addColorStop(1, "#388E3C");
    ctx.fillStyle = headGradient;
    ctx.fill();
    ctx.strokeStyle = "#1B5E20";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.ellipse(headPos.x - cellSize * 0.08, headPos.y - cellSize * 0.06, Math.max(3.5, cellSize * 0.055), Math.max(4, cellSize * 0.06), 0, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.ellipse(headPos.x + cellSize * 0.08, headPos.y - cellSize * 0.06, Math.max(3.5, cellSize * 0.055), Math.max(4, cellSize * 0.06), 0, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.fillStyle = "#000000";
    ctx.beginPath();
    ctx.ellipse(headPos.x - cellSize * 0.09, headPos.y - cellSize * 0.07, Math.max(2, cellSize * 0.035), Math.max(2.5, cellSize * 0.04), 0, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.ellipse(headPos.x + cellSize * 0.07, headPos.y - cellSize * 0.07, Math.max(2, cellSize * 0.035), Math.max(2.5, cellSize * 0.04), 0, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.fillStyle = "white";
    ctx.beginPath();
    ctx.arc(headPos.x - cellSize * 0.11, headPos.y - cellSize * 0.09, Math.max(1, cellSize * 0.02), 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(headPos.x + cellSize * 0.05, headPos.y - cellSize * 0.09, Math.max(1, cellSize * 0.02), 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.fillStyle = "#1B5E20";
    ctx.beginPath();
    ctx.arc(headPos.x - cellSize * 0.04, headPos.y + cellSize * 0.03, Math.max(1.5, cellSize * 0.025), 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(headPos.x + cellSize * 0.04, headPos.y + cellSize * 0.03, Math.max(1.5, cellSize * 0.025), 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.beginPath();
    ctx.moveTo(headPos.x + cellSize * 0.1, headPos.y + cellSize * 0.05);
    ctx.lineTo(headPos.x + cellSize * 0.18, headPos.y + cellSize * 0.02);
    ctx.moveTo(headPos.x + cellSize * 0.1, headPos.y + cellSize * 0.05);
    ctx.lineTo(headPos.x + cellSize * 0.18, headPos.y + cellSize * 0.09);
    ctx.strokeStyle = "#FF4444";
    ctx.lineWidth = Math.max(2, cellSize * 0.04);
    ctx.stroke();
    
    ctx.shadowBlur = 0;
  };

  // ===============================
  // LADDER DRAWING
  // ===============================
  const drawLadder = (ctx, bottomPos, topPos, cellSize) => {
    const dx = topPos.x - bottomPos.x;
    const dy = topPos.y - bottomPos.y;
    const railOffset = Math.max(10, cellSize * 0.15);
    const perpX = -Math.sin(Math.atan2(dy, dx)) * railOffset;
    const perpY = Math.cos(Math.atan2(dy, dx)) * railOffset;
    
    ctx.shadowBlur = 4;
    ctx.shadowColor = "rgba(0,0,0,0.4)";
    
    const railGradient1 = ctx.createLinearGradient(bottomPos.x, bottomPos.y, topPos.x, topPos.y);
    railGradient1.addColorStop(0, "#8B4513");
    railGradient1.addColorStop(1, "#654321");
    ctx.beginPath();
    ctx.moveTo(bottomPos.x + perpX, bottomPos.y + perpY);
    ctx.lineTo(topPos.x + perpX, topPos.y + perpY);
    ctx.strokeStyle = railGradient1;
    ctx.lineWidth = Math.max(6, cellSize * 0.1);
    ctx.stroke();
    
    ctx.beginPath();
    ctx.moveTo(bottomPos.x - perpX, bottomPos.y - perpY);
    ctx.lineTo(topPos.x - perpX, topPos.y - perpY);
    ctx.stroke();
    
    ctx.beginPath();
    ctx.moveTo(bottomPos.x + perpX - 2, bottomPos.y + perpY - 2);
    ctx.lineTo(topPos.x + perpX - 2, topPos.y + perpY - 2);
    ctx.strokeStyle = "#A0522D";
    ctx.lineWidth = Math.max(2, cellSize * 0.04);
    ctx.stroke();
    
    const numRungs = Math.min(6, Math.max(4, Math.floor(cellSize * 0.8)));
    for (let i = 1; i <= numRungs; i++) {
      const t = i / (numRungs + 1);
      const rungX = bottomPos.x + dx * t;
      const rungY = bottomPos.y + dy * t;
      
      ctx.beginPath();
      ctx.moveTo(rungX + perpX, rungY + perpY);
      ctx.lineTo(rungX - perpX, rungY - perpY);
      ctx.strokeStyle = "#CD853F";
      ctx.lineWidth = Math.max(4, cellSize * 0.07);
      ctx.stroke();
      
      ctx.beginPath();
      ctx.moveTo(rungX + perpX - 1.5, rungY + perpY - 1.5);
      ctx.lineTo(rungX - perpX - 1.5, rungY - perpY - 1.5);
      ctx.strokeStyle = "#DEB887";
      ctx.lineWidth = Math.max(1.5, cellSize * 0.03);
      ctx.stroke();
    }
    
    ctx.shadowBlur = 8;
    ctx.shadowColor = "#FFD700";
    ctx.beginPath();
    ctx.ellipse(topPos.x, topPos.y, Math.max(8, cellSize * 0.14), Math.max(7, cellSize * 0.12), 0, 0, 2 * Math.PI);
    ctx.fillStyle = "#FFD700";
    ctx.fill();
    ctx.fillStyle = "#B8860B";
    ctx.font = `bold ${Math.max(14, Math.floor(cellSize * 0.28))}px "Segoe UI"`;
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("★", topPos.x, topPos.y);
    
    ctx.shadowBlur = 0;
  };

  // ===============================
  // BOARD DRAWING
  // ===============================
  const drawBoard = () => {
    if (!gameData || !boardCanvasRef.current) return;
    const canvas = boardCanvasRef.current;
    const ctx = canvas.getContext("2d");
    const size = gameData.boardSize;
    const totalCells = gameData.totalCells;
    const cellSize = boardSizePx / size;
    
    canvas.width = boardSizePx;
    canvas.height = boardSizePx;
    
    const woodGradient = ctx.createLinearGradient(0, 0, boardSizePx, boardSizePx);
    woodGradient.addColorStop(0, "#5D3A1A");
    woodGradient.addColorStop(0.5, "#4A2E12");
    woodGradient.addColorStop(1, "#3E2510");
    ctx.fillStyle = woodGradient;
    ctx.fillRect(0, 0, boardSizePx, boardSizePx);
    
    ctx.shadowBlur = 5;
    ctx.shadowColor = "rgba(0,0,0,0.5)";
    ctx.strokeStyle = "#DAA520";
    ctx.lineWidth = 4;
    ctx.strokeRect(4, 4, boardSizePx - 8, boardSizePx - 8);
    ctx.strokeStyle = "#FFD700";
    ctx.lineWidth = 1.5;
    ctx.strokeRect(2, 2, boardSizePx - 4, boardSizePx - 4);
    ctx.shadowBlur = 0;
    
    ctx.beginPath();
    ctx.strokeStyle = "#DAA520";
    ctx.lineWidth = Math.max(1.5, boardSizePx / 400);
    for (let i = 0; i <= size; i++) {
      ctx.moveTo(i * cellSize, 0);
      ctx.lineTo(i * cellSize, boardSizePx);
      ctx.stroke();
      ctx.moveTo(0, i * cellSize);
      ctx.lineTo(boardSizePx, i * cellSize);
      ctx.stroke();
    }
    
    for (let cell = 1; cell <= totalCells; cell++) {
      const pos = getPosition(cell);
      const row = Math.floor((cell - 1) / size);
      const isEvenRow = row % 2 === 0;
      const isEvenCol = ((cell - 1) % size) % 2 === 0;
      const isLightCell = (isEvenRow === isEvenCol);
      
      ctx.beginPath();
      ctx.rect(pos.x - cellSize / 2, pos.y - cellSize / 2, cellSize, cellSize);
      
      const cellGradient = ctx.createLinearGradient(
        pos.x - cellSize / 2, pos.y - cellSize / 2,
        pos.x + cellSize / 2, pos.y + cellSize / 2
      );
      
      if (isLightCell) {
        cellGradient.addColorStop(0, "#F5E6C8");
        cellGradient.addColorStop(1, "#E8D5A8");
      } else {
        cellGradient.addColorStop(0, "#D4C4A0");
        cellGradient.addColorStop(1, "#C4B490");
      }
      ctx.fillStyle = cellGradient;
      ctx.fill();
      ctx.strokeStyle = "#DAA520";
      ctx.lineWidth = 0.8;
      ctx.stroke();
    }
    
    if (gameData.snakes) {
      for (const [head, tail] of Object.entries(gameData.snakes)) {
        const headPos = getPosition(parseInt(head));
        const tailPos = getPosition(tail);
        drawSnake(ctx, headPos, tailPos, cellSize);
      }
    }
    
    if (gameData.ladders) {
      for (const [bottom, top] of Object.entries(gameData.ladders)) {
        const bottomPos = getPosition(parseInt(bottom));
        const topPos = getPosition(top);
        drawLadder(ctx, bottomPos, topPos, cellSize);
      }
    }
    
    const fontSize = Math.max(12, Math.min(22, Math.floor(cellSize * 0.3)));
    for (let cell = 1; cell <= totalCells; cell++) {
      const pos = getPosition(cell);
      
      ctx.beginPath();
      ctx.arc(pos.x, pos.y, Math.max(9, cellSize * 0.25), 0, 2 * Math.PI);
      ctx.fillStyle = "#8B0000";
      ctx.fill();
      ctx.strokeStyle = "#FFD700";
      ctx.lineWidth = 1.5;
      ctx.stroke();
      
      ctx.font = `bold ${fontSize}px "Segoe UI", "Poppins"`;
      ctx.fillStyle = "#FFFFFF";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(cell.toString(), pos.x, pos.y);
      
      if (cell === 1) {
        ctx.font = `${Math.max(10, fontSize - 2)}px "Segoe UI"`;
        ctx.fillStyle = "#FFD700";
        ctx.fillText("🏁", pos.x + cellSize * 0.28, pos.y - cellSize * 0.25);
      }
      if (cell === totalCells) {
        ctx.font = `${Math.max(10, fontSize - 2)}px "Segoe UI"`;
        ctx.fillStyle = "#FFD700";
        ctx.fillText("🏆", pos.x + cellSize * 0.28, pos.y - cellSize * 0.25);
      }
    }
  };
  
  // ===============================
  // PLAYER DRAWING
  // ===============================
  const drawPlayers = () => {
    if (!gameData || !boardCanvasRef.current) return;
    const canvas = boardCanvasRef.current;
    const ctx = canvas.getContext("2d");
    const cellSize = boardSizePx / gameData.boardSize;
    const playerSize = Math.max(16, cellSize * 0.38);
    
    const playerPos = getPosition(playerPosition);
    
    ctx.shadowBlur = 12;
    ctx.shadowColor = "rgba(255,215,0,0.6)";
    
    const playerGradient = ctx.createRadialGradient(
      playerPos.x - playerSize * 0.25, 
      playerPos.y - playerSize * 0.25, 
      5,
      playerPos.x, 
      playerPos.y, 
      playerSize
    );
    playerGradient.addColorStop(0, "#FFD700");
    playerGradient.addColorStop(0.4, "#FFA500");
    playerGradient.addColorStop(1, "#FF8C00");
    
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize, 0, 2 * Math.PI);
    ctx.fillStyle = playerGradient;
    ctx.fill();
    
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize - 3, 0, 2 * Math.PI);
    ctx.fillStyle = "rgba(255,255,255,0.2)";
    ctx.fill();
    
    ctx.strokeStyle = "#FFFFFF";
    ctx.lineWidth = 2.5;
    ctx.stroke();
    
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.25, playerPos.y - playerSize * 0.15, playerSize * 0.18, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.25, playerPos.y - playerSize * 0.15, playerSize * 0.18, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.fillStyle = "#1a1a2e";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.28, playerPos.y - playerSize * 0.18, playerSize * 0.09, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.22, playerPos.y - playerSize * 0.18, playerSize * 0.09, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.32, playerPos.y - playerSize * 0.22, playerSize * 0.04, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.18, playerPos.y - playerSize * 0.22, playerSize * 0.04, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y + playerSize * 0.1, playerSize * 0.12, 0.1, Math.PI - 0.1);
    ctx.strokeStyle = "#8B4513";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    ctx.fillStyle = "#FFD700";
    ctx.shadowBlur = 5;
    ctx.beginPath();
    ctx.moveTo(playerPos.x - playerSize * 0.35, playerPos.y - playerSize * 0.45);
    ctx.lineTo(playerPos.x - playerSize * 0.2, playerPos.y - playerSize * 0.55);
    ctx.lineTo(playerPos.x, playerPos.y - playerSize * 0.48);
    ctx.lineTo(playerPos.x + playerSize * 0.2, playerPos.y - playerSize * 0.55);
    ctx.lineTo(playerPos.x + playerSize * 0.35, playerPos.y - playerSize * 0.45);
    ctx.fill();
    
    ctx.fillStyle = "#FF4444";
    ctx.beginPath();
    ctx.arc(playerPos.x - playerSize * 0.1, playerPos.y - playerSize * 0.52, playerSize * 0.05, 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(playerPos.x + playerSize * 0.1, playerPos.y - playerSize * 0.52, playerSize * 0.05, 0, 2 * Math.PI);
    ctx.fill();
    ctx.fillStyle = "#00FF00";
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y - playerSize * 0.55, playerSize * 0.05, 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.shadowBlur = 0;
    
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize + 6, 0, 2 * Math.PI);
    ctx.strokeStyle = "rgba(255, 215, 0, 0.4)";
    ctx.lineWidth = 2;
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize + 10, 0, 2 * Math.PI);
    ctx.strokeStyle = "rgba(255, 215, 0, 0.2)";
    ctx.lineWidth = 1.5;
    ctx.stroke();
  };
  
  // Draw board whenever gameData changes
  useEffect(() => {
    if (gameData && (gameState === "mcq" || gameState === "playing")) {
      drawBoard();
      drawPlayers();
    }
  }, [gameData, playerPosition, boardSizePx, gameState]);
  
  const resetGame = () => {
    setGameState("setup");
    setGameData(null);
    setRoundId(null);
    setPlayerPosition(1);
    setDiceValue(null);
    setGameOver(false);
    setWinner(null);
    setSelectedAnswer(null);
    setResultMessage("");
    setPlayerName("");
    setAlgo1TimeMicro(null);
    setAlgo2TimeMicro(null);
    setChoices([]);
  };

  const clearChartHistory = () => {
    setChartData([]);
    setRoundCounter(0);
    setShowChart(false);
  };

  // ===============================
  // PERFORMANCE CHART COMPONENT
  // ===============================
  const PerformanceChart = () => {
    if (!showChart || chartData.length === 0) return null;
    
    // Calculate averages in MICROSECONDS
    const avgBfsTime = (chartData.reduce((sum, d) => sum + d.bfsTime, 0) / chartData.length).toFixed(0);
    const avgDpTime = (chartData.reduce((sum, d) => sum + d.dpTime, 0) / chartData.length).toFixed(0);
    const dpFasterCount = chartData.filter(d => d.dpTime < d.bfsTime).length;
    
    return (
      <div style={{
        position: "fixed",
        top: "20px",
        right: "20px",
        width: "400px",
        maxHeight: "85vh",
        background: "rgba(13,59,110,0.95)",
        backdropFilter: "blur(10px)",
        borderRadius: "16px",
        border: "1px solid #FFD700",
        boxShadow: "0 10px 30px rgba(0,0,0,0.5)",
        zIndex: 1000,
        overflowY: "auto",
        padding: "12px"
      }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "10px", borderBottom: "1px solid rgba(255,255,255,0.2)", paddingBottom: "8px" }}>
          <div>
            <span style={{ fontSize: "16px", marginRight: "5px" }}>📊</span>
            <span style={{ color: "#FFD700", fontSize: "14px", fontWeight: "bold" }}>Performance History</span>
            <span style={{ color: "rgba(255,255,255,0.5)", fontSize: "10px", marginLeft: "5px" }}>({chartData.length} games)</span>
          </div>
          <div style={{ display: "flex", gap: "8px" }}>
            <button onClick={clearChartHistory} style={{ background: "rgba(255,255,255,0.1)", border: "none", color: "#fff", fontSize: "10px", padding: "3px 8px", borderRadius: "4px", cursor: "pointer" }}>🗑️ Clear</button>
            <button onClick={() => setShowChart(false)} style={{ background: "rgba(255,255,255,0.1)", border: "none", color: "#fff", fontSize: "16px", width: "24px", height: "24px", borderRadius: "4px", cursor: "pointer" }}>✕</button>
          </div>
        </div>
        
        {/* Stats Display - Show in MICROSECONDS */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "8px", marginBottom: "12px", padding: "8px", background: "rgba(0,0,0,0.4)", borderRadius: "8px" }}>
          <div style={{ textAlign: "center" }}>
            <p style={{ fontSize: "9px", color: "rgba(255,255,255,0.6)" }}>Avg BFS</p>
            <p style={{ fontSize: "14px", fontWeight: "bold", color: "#FFD700" }}>{avgBfsTime}µs</p>
          </div>
          <div style={{ textAlign: "center" }}>
            <p style={{ fontSize: "9px", color: "rgba(255,255,255,0.6)" }}>Avg DP</p>
            <p style={{ fontSize: "14px", fontWeight: "bold", color: "#4ade80" }}>{avgDpTime}µs</p>
          </div>
          <div style={{ textAlign: "center" }}>
            <p style={{ fontSize: "9px", color: "rgba(255,255,255,0.6)" }}>DP Faster</p>
            <p style={{ fontSize: "14px", fontWeight: "bold", color: "#378ADD" }}>
              {chartData.length > 0 ? ((dpFasterCount / chartData.length) * 100).toFixed(0) : 0}%
            </p>
          </div>
        </div>
        
        {/* Chart - Y-axis in MICROSECONDS */}
        <div style={{ height: "200px", marginBottom: "12px" }}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData} margin={{ top: 5, right: 5, left: 0, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
              <XAxis dataKey="round" stroke="#fff" fontSize={10} tick={{ fill: '#fff' }} />
              <YAxis 
                stroke="#fff" 
                fontSize={10} 
                tick={{ fill: '#fff' }}
                label={{ value: 'Time (µs)', angle: -90, position: 'insideLeft', style: { fill: '#fff', fontSize: 10 } }}
              />
              <Tooltip 
                contentStyle={{ backgroundColor: '#1a1a2e', border: '1px solid #FFD700', fontSize: '10px' }} 
                labelStyle={{ color: '#FFD700' }} 
                formatter={(value, name) => [`${Number(value).toFixed(0)} µs`, name]} 
              />
              <Line type="monotone" dataKey="bfsTime" stroke="#FFD700" name="BFS (µs)" strokeWidth={2} dot={{ r: 3 }} />
              <Line type="monotone" dataKey="dpTime" stroke="#4ade80" name="DP (µs)" strokeWidth={2} dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
        
        {/* Table - Show in MICROSECONDS */}
        <div style={{ maxHeight: "200px", overflowY: "auto", fontSize: "10px" }}>
          <table style={{ width: "100%", color: "#fff", borderCollapse: "collapse" }}>
            <thead style={{ position: "sticky", top: 0, background: "#0d3b6e" }}>
              <tr>
                <th style={{ padding: "4px" }}>#</th>
                <th style={{ padding: "4px" }}>Size</th>
                <th style={{ padding: "4px" }}>BFS (µs)</th>
                <th style={{ padding: "4px" }}>DP (µs)</th>
                <th style={{ padding: "4px" }}>🏆</th>
              </tr>
            </thead>
            <tbody>
              {chartData.map((row) => (
                <tr key={row.round} style={{ textAlign: "center", borderBottom: "1px solid rgba(255,255,255,0.1)" }}>
                  <td style={{ padding: "3px" }}>{row.round}</td>
                  <td style={{ padding: "3px" }}>{row.boardSize}</td>
                  <td style={{ padding: "3px", color: "#FFD700" }}>{row.bfsTime.toFixed(0)}</td>
                  <td style={{ padding: "3px", color: "#4ade80" }}>{row.dpTime.toFixed(0)}</td>
                  <td style={{ padding: "3px", color: row.dpTime < row.bfsTime ? "#4ade80" : "#FFD700" }}>
                    {row.dpTime < row.bfsTime ? "DP" : "BFS"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  return (
    <div ref={hubRef} style={{ background: "#050510", minHeight: "100vh", position: "relative", overflow: "hidden", fontFamily: "'Segoe UI', 'Poppins', sans-serif" }}>
      <canvas ref={canvasRef} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", pointerEvents: "none" }} />
      <PerformanceChart />
      
      <div style={{ position: "relative", zIndex: 1, padding: "1.5rem" }}>
        <button onClick={() => navigate("/")} style={{ background: "rgba(255,255,255,0.1)", border: "1px solid rgba(255,255,255,0.2)", color: "#fff", padding: "8px 16px", borderRadius: "20px", cursor: "pointer", fontSize: "13px", marginBottom: "1.5rem" }}>
          ← Back
        </button>
        
        <div style={{ textAlign: "center", marginBottom: "1.5rem" }}>
          <img src="https://png.pngtree.com/png-clipart/20240822/original/pngtree-lovable-green-snake-with-big-eyes-png-image_15828447.png" alt="Snake" style={{ width: "65px", height: "65px", marginBottom: "8px", filter: "drop-shadow(3px 5px 8px rgba(0,0,0,0.4)) drop-shadow(0 0 10px rgba(76,175,80,0.6))", animation: "float 3s ease-in-out infinite", cursor: "pointer" }} />
          <h1 style={{ fontSize: "26px", fontWeight: "bold", background: "linear-gradient(135deg, #FFD700, #FFA500)", WebkitBackgroundClip: "text", backgroundClip: "text", color: "transparent", letterSpacing: "-0.5px" }}>
            Snakes & Ladders
          </h1>
          <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.45)" }}>Roll the dice, climb ladders, avoid snakes!</p>
        </div>
        
        {/* SETUP SCREEN */}
        {gameState === "setup" && (
          <div style={{ 
            maxWidth: "420px", 
            margin: "0 auto", 
            background: "linear-gradient(135deg, rgba(7,61,46,0.9), rgba(5,45,35,0.95))", 
            border: "2px solid rgba(7,61,46,0.75)", 
            borderRadius: "16px", 
            padding: "1.5rem", 
            boxShadow: "0 10px 30px rgba(7,61,46,0.5)" 
          }}>
            <div style={{ marginBottom: "1.2rem" }}>
              <label style={{ display: "block", color: "#A5D6A7", fontSize: "12px", marginBottom: "6px", fontWeight: "bold" }}>Your Name</label>
              <input 
                value={playerName} 
                onChange={e => setPlayerName(e.target.value)} 
                placeholder="Enter your name" 
                style={{ 
                  width: "100%", 
                  padding: "10px 14px", 
                  borderRadius: "8px", 
                  border: "1px solid rgba(7,61,46,0.75)", 
                  background: "rgba(0,0,0,0.3)", 
                  color: "#fff", 
                  fontSize: "14px", 
                  outline: "none" 
                }} 
              />
            </div>
            
            <div style={{ marginBottom: "1.2rem" }}>
              <label style={{ display: "block", color: "#A5D6A7", fontSize: "12px", marginBottom: "6px", fontWeight: "bold" }}>Board Size (N × N)</label>
              <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
                {BOARD_SIZES.map(size => (
                  <button 
                    key={size} 
                    onClick={() => setBoardSize(size)} 
                    style={{ 
                      padding: "6px 12px", 
                      borderRadius: "8px", 
                      border: `1px solid ${boardSize === size ? "rgba(7,61,46,0.75)" : "rgba(255,255,255,0.2)"}`, 
                      background: boardSize === size ? "rgba(128, 202, 181, 0.5)" : "rgba(255,255,255,0.05)", 
                      color: "#fff", 
                      cursor: "pointer", 
                      fontSize: "12px",
                      transition: "all 0.2s ease"
                    }}>
                    {size}×{size}
                  </button>
                ))}
              </div>
              <p style={{ fontSize: "10px", color: "#A5D6A7", marginTop: "8px" }}>
                🐍 Snakes: <span style={{ color: "#FF8A80" }}>{boardSize - 2}</span> | 🪜 Ladders: <span style={{ color: "#FFD54F" }}>{boardSize - 2}</span>
              </p>
            </div>
            
            <button 
              onClick={startGame} 
              disabled={loading} 
              style={{ 
                width: "100%", 
                padding: "12px", 
                borderRadius: "8px", 
                border: "none", 
                background: loading ? "rgba(7,61,46,0.6)" : "rgba(7,61,46,0.85)", 
                color: "#fff", 
                fontSize: "14px", 
                fontWeight: "bold", 
                cursor: loading ? "not-allowed" : "pointer", 
                opacity: loading ? 0.6 : 1, 
                boxShadow: "0 4px 15px rgba(7,61,46,0.4)",
                transition: "all 0.3s ease"
              }}>
              {loading ? "Connecting..." : "Start Game"}
            </button>
            
            {resultMessage && (
              <p style={{ color: "#f87171", fontSize: "12px", textAlign: "center", marginTop: "1rem" }}>
                {resultMessage}
              </p>
            )}
          </div>
        )}
        
        {/* MCQ SCREEN */}
        {gameState === "mcq" && gameData && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
            <div style={{ maxWidth: "100%", overflowX: "auto", overflowY: "auto", display: "flex", justifyContent: "center", padding: "8px", background: "rgba(0,0,0,0.3)", borderRadius: "20px" }}>
              <div style={{ border: "3px solid #DAA520", borderRadius: "16px", overflow: "hidden", boxShadow: "0 15px 30px rgba(0,0,0,0.5)" }}>
                <canvas ref={boardCanvasRef} width={boardSizePx} height={boardSizePx} style={{ width: boardSizePx, height: boardSizePx, display: "block" }} />
              </div>
            </div>
            <div style={{ maxWidth: "450px", width: "100%", background: "rgba(55,138,221,0.3)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "1.2rem", textAlign: "center" }}>
              <div style={{ fontSize: "28px", marginBottom: "0.3rem" }}>🎯</div>
              <h2 style={{ color: "#fff", marginBottom: "0.3rem", fontSize: "1.1rem" }}>Puzzle Challenge</h2>
              <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.7)" }}>Minimum dice throws to reach cell {gameData.totalCells}?</p>
              <p style={{ fontSize: "10px", color: "rgba(255,255,255,0.5)", marginBottom: "1rem" }}>
                {gameData.boardSize}×{gameData.boardSize} | 🐍 {Object.keys(gameData.snakes || {}).length} | 🪜 {Object.keys(gameData.ladders || {}).length}
              </p>
              <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap", marginBottom: "1rem" }}>
                {choices.map((c, i) => (
                  <button 
                    key={i} 
                    onClick={() => setSelectedAnswer(c)} 
                    style={{ 
                      background: selectedAnswer === c ? "#FFD700" : "rgba(255,255,255,0.15)", 
                      border: "none", 
                      padding: "8px 20px", 
                      borderRadius: "25px", 
                      color: selectedAnswer === c ? "#333" : "#fff", 
                      fontSize: "1rem", 
                      fontWeight: "bold", 
                      cursor: "pointer" 
                    }}>
                    {c}
                  </button>
                ))}
              </div>
              <button 
                onClick={checkAnswer} 
                disabled={loading} 
                style={{ 
                  background: "#FFD700", 
                  border: "none", 
                  padding: "8px 25px", 
                  borderRadius: "25px", 
                  fontWeight: "bold", 
                  fontSize: "0.9rem", 
                  cursor: loading ? "not-allowed" : "pointer", 
                  marginBottom: "0.8rem", 
                  opacity: loading ? 0.6 : 1 
                }}>
                {loading ? "Submitting..." : "Submit Answer"}
              </button>
              {resultMessage && (
                <p style={{ color: resultMessage.includes("✅") ? "#4ade80" : "#f87171", fontSize: "12px" }}>
                  {resultMessage}
                </p>
              )}
              <div style={{ marginTop: "0.8rem", padding: "0.4rem", background: "rgba(0,0,0,0.3)", borderRadius: "8px" }}>
                <p style={{ fontSize: "9px", color: "rgba(255,255,255,0.5)" }}>🎲 Answer correctly to start playing!</p>
              </div>
            </div>
          </div>
        )}
        
        {/* PLAYING SCREEN */}
        {gameState === "playing" && gameData && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
            <div style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "12px", padding: "10px 20px", display: "flex", gap: "15px", alignItems: "center", flexWrap: "wrap", justifyContent: "center" }}>
              <div style={{ color: "#fff", fontSize: "13px" }}>🎮 Player: <strong style={{ color: "#FFD700" }}>{playerName}</strong></div>
              <button 
                onClick={rollDice} 
                disabled={gameOver || isRolling} 
                style={{ 
                  background: "#FFD700", 
                  border: "none", 
                  padding: "8px 25px", 
                  borderRadius: "35px", 
                  fontSize: "1rem", 
                  fontWeight: "bold", 
                  cursor: (gameOver || isRolling) ? "not-allowed" : "pointer", 
                  opacity: (gameOver || isRolling) ? 0.6 : 1 
                }}>
                🎲 Roll Dice
              </button>
              {diceValue && (
                <div style={{ background: "rgba(255,255,255,0.15)", padding: "4px 15px", borderRadius: "25px", fontSize: "1.1rem", fontWeight: "bold", color: "#FFD700" }}>
                  🎲 {diceValue}
                </div>
              )}
            </div>
            {resultMessage && (
              <div style={{ background: "rgba(0,0,0,0.7)", padding: "6px 16px", borderRadius: "25px", color: "#FFD700", fontSize: "12px" }}>
                {resultMessage}
              </div>
            )}
            <div style={{ maxWidth: "100%", overflowX: "auto", overflowY: "auto", display: "flex", justifyContent: "center", padding: "8px", background: "rgba(0,0,0,0.3)", borderRadius: "20px" }}>
              <div style={{ border: "3px solid #DAA520", borderRadius: "16px", overflow: "hidden", boxShadow: "0 15px 30px rgba(0,0,0,0.5)" }}>
                <canvas ref={boardCanvasRef} width={boardSizePx} height={boardSizePx} style={{ width: boardSizePx, height: boardSizePx, display: "block" }} />
              </div>
            </div>
          </div>
        )}
        
        {/* RESULT SCREEN */}
        {gameState === "result" && (
          <div style={{ maxWidth: "350px", margin: "0 auto", textAlign: "center" }}>
            <div style={{ fontSize: "50px", marginBottom: "0.8rem" }}>🏆</div>
            <h2 style={{ fontSize: "20px", fontWeight: 500, color: "#4ade80", marginBottom: "6px" }}>
              Congratulations {playerName}!
            </h2>
            <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "12px", marginBottom: "1rem" }}>
              You reached the top! Great job!
            </p>
            
            {/* Display in MICROSECONDS */}
            {algo1TimeMicro !== null && algo2TimeMicro !== null && (
              <div style={{ background: "rgba(255,255,255,0.07)", borderRadius: "10px", padding: "0.8rem", marginBottom: "1rem", textAlign: "left" }}>
                <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "10px", marginBottom: "5px" }}>This Game's Performance</p>
                <p style={{ color: "#fff", fontSize: "11px" }}>
                  ⚡ BFS: <strong>{algo1TimeMicro} µs</strong>
                </p>
                <p style={{ color: "#fff", fontSize: "11px" }}>
                  📊 Dynamic Programming: <strong>{algo2TimeMicro} µs</strong>
                </p>
                <p style={{ color: "#4ade80", fontSize: "10px", marginTop: "5px" }}>
                  {algo1TimeMicro <= algo2TimeMicro ? "🏆 BFS was faster" : "🏆 DP was faster"}
                </p>
              </div>
            )}
            
            <div style={{ display: "flex", gap: "8px" }}>
              <button 
                onClick={resetGame} 
                style={{ 
                  flex: 1, 
                  padding: "10px", 
                  borderRadius: "8px", 
                  border: "1px solid rgba(255,255,255,0.2)", 
                  background: "transparent", 
                  color: "#fff", 
                  fontSize: "12px", 
                  cursor: "pointer" 
                }}>
                Play again
              </button>
              <button 
                onClick={() => navigate("/")} 
                style={{ 
                  flex: 1, 
                  padding: "10px", 
                  borderRadius: "8px", 
                  border: "none", 
                  background: "#185FA5", 
                  color: "#fff", 
                  fontSize: "12px", 
                  cursor: "pointer" 
                }}>
                Back to hub
              </button>
            </div>
          </div>
        )}
      </div>
      
      <style>{`
        @keyframes float { 
          0%, 100% { transform: translateY(0px); } 
          50% { transform: translateY(-8px); } 
        }
      `}</style>
    </div>
  );
}