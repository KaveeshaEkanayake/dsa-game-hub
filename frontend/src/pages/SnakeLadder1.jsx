import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

const BOARD_SIZES = [6, 7, 8, 9, 10, 11, 12];

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
  
  // Player positions - Single player only
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
  
  // Game history
  const [gameHistory, setGameHistory] = useState([]);
  
  // Board dimensions - Dynamic based on board size
  const [boardSizePx, setBoardSizePx] = useState(500);

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

  // Update board size dynamically - Smaller for larger boards
  useEffect(() => {
    // Adjust canvas size based on board size to prevent overflow
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

  // Algorithm 1: BFS
  const findMinThrowsBFS = (snakes, ladders, totalCells) => {
    const startTime = performance.now();
    const visited = new Array(totalCells + 1).fill(false);
    const distance = new Array(totalCells + 1).fill(0);
    const queue = [];
    
    visited[1] = true;
    queue.push(1);
    
    while (queue.length > 0) {
      const current = queue.shift();
      for (let dice = 1; dice <= 6; dice++) {
        let next = current + dice;
        if (next > totalCells) continue;
        if (snakes[next]) next = snakes[next];
        if (ladders[next]) next = ladders[next];
        if (!visited[next]) {
          visited[next] = true;
          distance[next] = distance[current] + 1;
          queue.push(next);
        }
      }
    }
    const endTime = performance.now();
    return { throws: distance[totalCells], time: endTime - startTime };
  };
  
  // Algorithm 2: Dynamic Programming
  const findMinThrowsDP = (snakes, ladders, totalCells) => {
    const startTime = performance.now();
    const dp = new Array(totalCells + 1).fill(Infinity);
    dp[1] = 0;
    
    for (let i = 1; i <= totalCells; i++) {
      if (dp[i] === Infinity) continue;
      for (let dice = 1; dice <= 6; dice++) {
        let next = i + dice;
        if (next > totalCells) continue;
        if (snakes[next]) next = snakes[next];
        if (ladders[next]) next = ladders[next];
        if (dp[next] > dp[i] + 1) {
          dp[next] = dp[i] + 1;
        }
      }
    }
    const endTime = performance.now();
    return { throws: dp[totalCells], time: endTime - startTime };
  };

  // Generate random snakes and ladders
  const generateRandomBoard = (size) => {
    const totalCells = size * size;
    const numSnakes = size - 2;
    const numLadders = size - 2;
    
    const snakes = {};
    const ladders = {};
    const usedPositions = new Set();
    
    // Generate ladders
    let laddersCreated = 0;
    let maxAttempts = 100;
    while (laddersCreated < numLadders && maxAttempts > 0) {
      const bottom = Math.floor(Math.random() * (totalCells - 10)) + 2;
      const top = bottom + Math.floor(Math.random() * (totalCells - bottom)) + 3;
      if (top <= totalCells && !usedPositions.has(bottom) && !usedPositions.has(top) && top > bottom + 2) {
        ladders[bottom] = top;
        usedPositions.add(bottom);
        usedPositions.add(top);
        laddersCreated++;
      }
      maxAttempts--;
    }
    
    // Generate snakes
    let snakesCreated = 0;
    maxAttempts = 100;
    while (snakesCreated < numSnakes && maxAttempts > 0) {
      const head = Math.floor(Math.random() * (totalCells - 10)) + 15;
      const tail = head - Math.floor(Math.random() * (head - 5)) - 3;
      if (tail >= 1 && !usedPositions.has(head) && !usedPositions.has(tail) && head > tail + 2) {
        snakes[head] = tail;
        usedPositions.add(head);
        usedPositions.add(tail);
        snakesCreated++;
      }
      maxAttempts--;
    }
    
    return { snakes, ladders, totalCells };
  };

  // Start new game
  const startGame = () => {
    if (!playerName.trim()) {
      alert("Please enter your name!");
      return;
    }
    if (boardSize < 6 || boardSize > 12) {
      alert("Board size must be between 6 and 12!");
      return;
    }
    
    setLoading(true);
    
    const { snakes, ladders, totalCells } = generateRandomBoard(boardSize);
    const bfsResult = findMinThrowsBFS(snakes, ladders, totalCells);
    const dpResult = findMinThrowsDP(snakes, ladders, totalCells);
    const minThrows = Math.min(bfsResult.throws, dpResult.throws);
    
    setAlgo1Time(bfsResult.time);
    setAlgo2Time(dpResult.time);
    
    const newGameData = {
      boardSize: boardSize,
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
    
    const correct = minThrows;
    let option1 = correct + Math.floor(Math.random() * 3) + 1;
    let option2 = correct - (Math.floor(Math.random() * 2) + 1);
    if (option2 < 1) option2 = correct + 2;
    if (option1 === correct) option1 = correct + 1;
    if (option2 === correct) option2 = correct - 1;
    if (option2 < 1) option2 = correct + 1;
    
    setChoices([correct, option1, option2].sort(() => Math.random() - 0.5));
    setSelectedAnswer(null);
    setResultMessage("");
    
    setGameState("mcq");
    setLoading(false);
  };
  
  // Check MCQ answer
  const checkAnswer = () => {
    if (!selectedAnswer) {
      setResultMessage("Please select an answer!");
      return;
    }
    
    const isCorrect = selectedAnswer === gameData.minimumDiceThrows;
    if (isCorrect) {
      setResultMessage("✅ Correct! Starting game...");
      setGameHistory(prev => [...prev, {
        playerName, 
        boardSize: gameData.boardSize,
        answer: selectedAnswer, 
        correct: true,
        algo1TimeMs: algo1Time, 
        algo2TimeMs: algo2Time,
        timestamp: new Date().toISOString()
      }]);
      
      setTimeout(() => {
        setResultMessage("");
        setGameState("playing");
      }, 1000);
    } else {
      setResultMessage(`❌ Wrong! Correct answer is ${gameData.minimumDiceThrows}. Try again!`);
    }
  };

  // Dice roll
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
      setWinner(1);
      setGameOver(true);
      setGameState("result");
      return;
    }
  };

  // Get cell position
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
  // PROFESSIONAL SNAKE DRAWING (Scaled)
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
    
    ctx.shadowBlur = 3;
    ctx.shadowColor = "rgba(0,0,0,0.4)";
    
    // Snake body
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y);
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, tailPos.x, tailPos.y);
    ctx.strokeStyle = "#4CAF50";
    ctx.lineWidth = Math.max(5, cellSize * 0.11);
    ctx.stroke();
    
    // Snake outline
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y);
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, tailPos.x, tailPos.y);
    ctx.strokeStyle = "#1B5E20";
    ctx.lineWidth = Math.max(7, cellSize * 0.13);
    ctx.stroke();
    
    // Scale pattern
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y);
    ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, tailPos.x, tailPos.y);
    ctx.strokeStyle = "#2E7D32";
    ctx.lineWidth = Math.max(2.5, cellSize * 0.06);
    ctx.setLineDash([Math.max(5, cellSize * 0.1), Math.max(7, cellSize * 0.12)]);
    ctx.stroke();
    ctx.setLineDash([]);
    
    // Snake head
    ctx.beginPath();
    ctx.ellipse(headPos.x, headPos.y, Math.max(7, cellSize * 0.15), Math.max(6, cellSize * 0.12), 0, 0, 2 * Math.PI);
    ctx.fillStyle = "#4CAF50";
    ctx.fill();
    ctx.strokeStyle = "#1B5E20";
    ctx.lineWidth = 1.5;
    ctx.stroke();
    
    // Eyes
    ctx.fillStyle = "#FFFFFF";
    ctx.beginPath();
    ctx.arc(headPos.x - cellSize * 0.06, headPos.y - cellSize * 0.05, Math.max(3, cellSize * 0.045), 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(headPos.x + cellSize * 0.06, headPos.y - cellSize * 0.05, Math.max(3, cellSize * 0.045), 0, 2 * Math.PI);
    ctx.fill();
    
    ctx.fillStyle = "#000000";
    ctx.beginPath();
    ctx.arc(headPos.x - cellSize * 0.07, headPos.y - cellSize * 0.06, Math.max(2, cellSize * 0.03), 0, 2 * Math.PI);
    ctx.fill();
    ctx.beginPath();
    ctx.arc(headPos.x + cellSize * 0.05, headPos.y - cellSize * 0.06, Math.max(2, cellSize * 0.03), 0, 2 * Math.PI);
    ctx.fill();
    
    // Tongue
    ctx.beginPath();
    ctx.moveTo(headPos.x, headPos.y + cellSize * 0.07);
    ctx.lineTo(headPos.x - cellSize * 0.06, headPos.y + cellSize * 0.14);
    ctx.moveTo(headPos.x, headPos.y + cellSize * 0.07);
    ctx.lineTo(headPos.x + cellSize * 0.06, headPos.y + cellSize * 0.14);
    ctx.strokeStyle = "#FF4444";
    ctx.lineWidth = Math.max(1.5, cellSize * 0.035);
    ctx.stroke();
    
    ctx.shadowBlur = 0;
  };

  // ===============================
  // PROFESSIONAL LADDER DRAWING (Scaled)
  // ===============================
  const drawLadder = (ctx, bottomPos, topPos, cellSize) => {
    const dx = topPos.x - bottomPos.x;
    const dy = topPos.y - bottomPos.y;
    const railOffset = Math.max(8, cellSize * 0.12);
    const perpX = -Math.sin(Math.atan2(dy, dx)) * railOffset;
    const perpY = Math.cos(Math.atan2(dy, dx)) * railOffset;
    
    ctx.shadowBlur = 3;
    ctx.shadowColor = "rgba(0,0,0,0.3)";
    
    // Rails
    ctx.beginPath();
    ctx.moveTo(bottomPos.x + perpX, bottomPos.y + perpY);
    ctx.lineTo(topPos.x + perpX, topPos.y + perpY);
    ctx.strokeStyle = "#8B4513";
    ctx.lineWidth = Math.max(4, cellSize * 0.08);
    ctx.stroke();
    
    ctx.beginPath();
    ctx.moveTo(bottomPos.x - perpX, bottomPos.y - perpY);
    ctx.lineTo(topPos.x - perpX, topPos.y - perpY);
    ctx.stroke();
    
    // Rungs
    const numRungs = Math.min(5, Math.max(3, Math.floor(cellSize * 0.7)));
    for (let i = 1; i <= numRungs; i++) {
      const t = i / (numRungs + 1);
      const rungX = bottomPos.x + dx * t;
      const rungY = bottomPos.y + dy * t;
      
      ctx.beginPath();
      ctx.moveTo(rungX + perpX, rungY + perpY);
      ctx.lineTo(rungX - perpX, rungY - perpY);
      ctx.strokeStyle = "#CD853F";
      ctx.lineWidth = Math.max(2.5, cellSize * 0.06);
      ctx.stroke();
    }
    
    // Top decoration
    ctx.beginPath();
    ctx.ellipse(topPos.x, topPos.y, Math.max(6, cellSize * 0.12), Math.max(5, cellSize * 0.1), 0, 0, 2 * Math.PI);
    ctx.fillStyle = "#FFD700";
    ctx.fill();
    ctx.font = `${Math.max(10, Math.floor(cellSize * 0.22))}px "Segoe UI"`;
    ctx.fillStyle = "#B8860B";
    ctx.fillText("★", topPos.x, topPos.y);
    
    ctx.shadowBlur = 0;
  };

  // Draw board with improved colors
  const drawBoard = () => {
    if (!gameData || !boardCanvasRef.current) return;
    const canvas = boardCanvasRef.current;
    const ctx = canvas.getContext("2d");
    const size = gameData.boardSize;
    const totalCells = gameData.totalCells;
    const cellSize = boardSizePx / size;
    
    canvas.width = boardSizePx;
    canvas.height = boardSizePx;
    
    // Premium dark wood background (better contrast with starfield)
    const woodGradient = ctx.createLinearGradient(0, 0, boardSizePx, boardSizePx);
    woodGradient.addColorStop(0, "#5D3A1A");
    woodGradient.addColorStop(1, "#3E2510");
    ctx.fillStyle = woodGradient;
    ctx.fillRect(0, 0, boardSizePx, boardSizePx);
    
    // Golden border
    ctx.strokeStyle = "#DAA520";
    ctx.lineWidth = 3;
    ctx.strokeRect(3, 3, boardSizePx - 6, boardSizePx - 6);
    
    // Grid lines (golden, thinner for larger boards)
    ctx.beginPath();
    ctx.strokeStyle = "#DAA520";
    ctx.lineWidth = Math.max(1.2, boardSizePx / 450);
    for (let i = 0; i <= size; i++) {
      ctx.moveTo(i * cellSize, 0);
      ctx.lineTo(i * cellSize, boardSizePx);
      ctx.stroke();
      ctx.moveTo(0, i * cellSize);
      ctx.lineTo(boardSizePx, i * cellSize);
      ctx.stroke();
    }
    
    // Cell backgrounds (rich alternating colors)
    for (let cell = 1; cell <= totalCells; cell++) {
      const pos = getPosition(cell);
      const row = Math.floor((cell - 1) / size);
      const isEvenRow = row % 2 === 0;
      const isEvenCol = ((cell - 1) % size) % 2 === 0;
      const isLightCell = (isEvenRow === isEvenCol);
      
      ctx.beginPath();
      ctx.rect(pos.x - cellSize / 2, pos.y - cellSize / 2, cellSize, cellSize);
      ctx.fillStyle = isLightCell ? "#F5E6C8" : "#E8D5A8";
      ctx.fill();
      ctx.strokeStyle = "#DAA520";
      ctx.lineWidth = 0.8;
      ctx.stroke();
    }
    
    // Draw Snakes
    for (const [head, tail] of Object.entries(gameData.snakes)) {
      const headPos = getPosition(parseInt(head));
      const tailPos = getPosition(tail);
      drawSnake(ctx, headPos, tailPos, cellSize);
    }
    
    // Draw Ladders
    for (const [bottom, top] of Object.entries(gameData.ladders)) {
      const bottomPos = getPosition(parseInt(bottom));
      const topPos = getPosition(top);
      drawLadder(ctx, bottomPos, topPos, cellSize);
    }
    
    // Cell numbers (scaled properly)
    const fontSize = Math.max(10, Math.min(20, Math.floor(cellSize * 0.28)));
    for (let cell = 1; cell <= totalCells; cell++) {
      const pos = getPosition(cell);
      
      // Number background circle
      ctx.beginPath();
      ctx.arc(pos.x, pos.y, Math.max(7, cellSize * 0.22), 0, 2 * Math.PI);
      ctx.fillStyle = "#B22222";
      ctx.fill();
      ctx.strokeStyle = "#8B0000";
      ctx.lineWidth = Math.max(1, cellSize * 0.03);
      ctx.stroke();
      
      // Number text
      ctx.font = `bold ${fontSize}px "Segoe UI", "Poppins"`;
      ctx.fillStyle = "#FFFFFF";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(cell.toString(), pos.x, pos.y);
      
      // Gold star for special cells
      if (cell === 1 || cell === totalCells || cell % 10 === 0) {
        ctx.font = `${Math.max(8, fontSize - 4)}px "Segoe UI"`;
        ctx.fillStyle = "#FFD700";
        ctx.fillText("★", pos.x + cellSize * 0.3, pos.y - cellSize * 0.26);
      }
    }
  };
  
  // Draw player with glow
  const drawPlayers = () => {
    if (!gameData || !boardCanvasRef.current) return;
    const canvas = boardCanvasRef.current;
    const ctx = canvas.getContext("2d");
    const cellSize = boardSizePx / gameData.boardSize;
    const playerSize = Math.max(12, cellSize * 0.32);
    
    const playerPos = getPosition(playerPosition);
    
    ctx.shadowBlur = 15;
    ctx.shadowColor = "#FFD700";
    
    const gradient = ctx.createRadialGradient(
      playerPos.x - playerSize * 0.3, 
      playerPos.y - playerSize * 0.3, 
      5,
      playerPos.x, 
      playerPos.y, 
      playerSize
    );
    gradient.addColorStop(0, "#FFD700");
    gradient.addColorStop(0.5, "#FFA500");
    gradient.addColorStop(1, "#FF8C00");
    
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize, 0, 2 * Math.PI);
    ctx.fillStyle = gradient;
    ctx.fill();
    ctx.strokeStyle = "#FFFFFF";
    ctx.lineWidth = 2.5;
    ctx.stroke();
    
    ctx.shadowBlur = 0;
    
    ctx.font = `${Math.max(12, Math.floor(playerSize * 1.2))}px "Segoe UI"`;
    ctx.fillStyle = "#FFFFFF";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("👑", playerPos.x, playerPos.y);
    
    // Outer rings
    ctx.beginPath();
    ctx.arc(playerPos.x, playerPos.y, playerSize + 4, 0, 2 * Math.PI);
    ctx.strokeStyle = "rgba(255, 215, 0, 0.5)";
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
    setPlayerPosition(1);
    setDiceValue(null);
    setGameOver(false);
    setWinner(null);
    setSelectedAnswer(null);
    setResultMessage("");
    setPlayerName("");
    setAlgo1Time(null);
    setAlgo2Time(null);
  };

  return (
    <div ref={hubRef} style={{ background: "#050510", minHeight: "100vh", position: "relative", overflow: "hidden", fontFamily: "'Segoe UI', 'Poppins', sans-serif" }}>
      <canvas ref={canvasRef} style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%", pointerEvents: "none" }} />
      
      <div style={{ position: "relative", zIndex: 1, padding: "1.5rem" }}>
        <button onClick={() => navigate("/")} style={{ background: "rgba(255,255,255,0.1)", border: "1px solid rgba(255,255,255,0.2)", color: "#fff", padding: "8px 16px", borderRadius: "20px", cursor: "pointer", fontSize: "13px", marginBottom: "1.5rem" }}>
          ← Back
        </button>
        
        <div style={{ textAlign: "center", marginBottom: "1.5rem" }}>
          <div style={{ fontSize: "40px", marginBottom: "4px" }}>🐍</div>
          <h1 style={{ fontSize: "24px", fontWeight: 500, color: "#fff", letterSpacing: "-0.5px" }}>Snakes & Ladders</h1>
          <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.45)" }}>Roll the dice, climb ladders, avoid snakes!</p>
        </div>
        
        {/* SETUP SCREEN */}
        {gameState === "setup" && (
          <div style={{ maxWidth: "420px", margin: "0 auto", background: "rgba(13,59,110,0.6)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "1.5rem" }}>
            <div style={{ marginBottom: "1.2rem" }}>
              <label style={{ display: "block", color: "rgba(255,255,255,0.7)", fontSize: "12px", marginBottom: "6px" }}>Your Name</label>
              <input value={playerName} onChange={e => setPlayerName(e.target.value)} placeholder="Enter your name"
                style={{ width: "100%", padding: "8px 12px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "rgba(255,255,255,0.08)", color: "#fff", fontSize: "13px", outline: "none" }} />
            </div>
            
            <div style={{ marginBottom: "1.2rem" }}>
              <label style={{ display: "block", color: "rgba(255,255,255,0.7)", fontSize: "12px", marginBottom: "6px" }}>Board Size (N × N)</label>
              <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
                {BOARD_SIZES.map(size => (
                  <button key={size} onClick={() => setBoardSize(size)}
                    style={{ padding: "6px 12px", borderRadius: "8px", border: `1px solid ${boardSize === size ? "#378ADD" : "rgba(255,255,255,0.2)"}`, background: boardSize === size ? "rgba(55,138,221,0.3)" : "rgba(255,255,255,0.05)", color: "#fff", cursor: "pointer", fontSize: "12px" }}>
                    {size}×{size}
                  </button>
                ))}
              </div>
              <p style={{ fontSize: "10px", color: "rgba(255,255,255,0.4)", marginTop: "6px" }}>
                🐍 Snakes: {boardSize - 2} | 🪜 Ladders: {boardSize - 2}
              </p>
            </div>
            
            <button onClick={startGame} disabled={loading}
              style={{ width: "100%", padding: "10px", borderRadius: "8px", border: "none", background: loading ? "#0f3a5e" : "#185FA5", color: "#fff", fontSize: "14px", fontWeight: 500, cursor: loading ? "not-allowed" : "pointer", opacity: loading ? 0.6 : 1 }}>
              {loading ? "Generating Board..." : "🎮 Start Game"}
            </button>
          </div>
        )}
        
        {/* MCQ SCREEN */}
        {gameState === "mcq" && gameData && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
            {/* Scrollable board container for large boards */}
            <div style={{ 
              maxWidth: "100%", 
              overflowX: "auto", 
              overflowY: "auto",
              display: "flex",
              justifyContent: "center",
              padding: "8px",
              background: "rgba(0,0,0,0.3)",
              borderRadius: "20px"
            }}>
              <div style={{ border: "3px solid #DAA520", borderRadius: "16px", overflow: "hidden", boxShadow: "0 15px 30px rgba(0,0,0,0.5)" }}>
                <canvas ref={boardCanvasRef} width={boardSizePx} height={boardSizePx} style={{ width: boardSizePx, height: boardSizePx, display: "block" }} />
              </div>
            </div>
            
            <div style={{ maxWidth: "450px", width: "100%", background: "rgba(55,138,221,0.3)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "16px", padding: "1.2rem", textAlign: "center" }}>
              <div style={{ fontSize: "28px", marginBottom: "0.3rem" }}>🎯</div>
              <h2 style={{ color: "#fff", marginBottom: "0.3rem", fontSize: "1.1rem" }}>Puzzle Challenge</h2>
              <p style={{ fontSize: "12px", color: "rgba(255,255,255,0.7)" }}>
                Minimum dice throws to reach cell {gameData.totalCells}?
              </p>
              <p style={{ fontSize: "10px", color: "rgba(255,255,255,0.5)", marginBottom: "1rem" }}>
                {gameData.boardSize}×{gameData.boardSize} | 🐍 {Object.keys(gameData.snakes).length} | 🪜 {Object.keys(gameData.ladders).length}
              </p>
              
              <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap", marginBottom: "1rem" }}>
                {choices.map((c, i) => (
                  <button key={i} onClick={() => setSelectedAnswer(c)}
                    style={{ background: selectedAnswer === c ? "#FFD700" : "rgba(255,255,255,0.15)", border: "none", padding: "8px 20px", borderRadius: "25px", color: selectedAnswer === c ? "#333" : "#fff", fontSize: "1rem", fontWeight: "bold", cursor: "pointer" }}>
                    {c}
                  </button>
                ))}
              </div>
              
              <button onClick={checkAnswer}
                style={{ background: "#FFD700", border: "none", padding: "8px 25px", borderRadius: "25px", fontWeight: "bold", fontSize: "0.9rem", cursor: "pointer", marginBottom: "0.8rem" }}>
                Submit Answer
              </button>
              
              {resultMessage && (
                <p style={{ color: resultMessage.includes("✅") ? "#4ade80" : "#f87171", fontSize: "12px" }}>
                  {resultMessage}
                </p>
              )}
              
              <div style={{ marginTop: "0.8rem", padding: "0.4rem", background: "rgba(0,0,0,0.3)", borderRadius: "8px" }}>
                <p style={{ fontSize: "9px", color: "rgba(255,255,255,0.5)" }}>⚡ BFS: {algo1Time?.toFixed(2)}ms | 📊 DP: {algo2Time?.toFixed(2)}ms</p>
              </div>
            </div>
          </div>
        )}
        
        {/* PLAYING SCREEN */}
        {gameState === "playing" && gameData && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
            <div style={{ background: "rgba(255,255,255,0.07)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "12px", padding: "10px 20px", display: "flex", gap: "15px", alignItems: "center", flexWrap: "wrap", justifyContent: "center" }}>
              <div style={{ color: "#fff", fontSize: "13px" }}>
                🎮 Player: <strong style={{ color: "#FFD700" }}>{playerName}</strong>
              </div>
              <button onClick={rollDice} disabled={gameOver || isRolling}
                style={{ background: "#FFD700", border: "none", padding: "8px 25px", borderRadius: "35px", fontSize: "1rem", fontWeight: "bold", cursor: (gameOver || isRolling) ? "not-allowed" : "pointer", opacity: (gameOver || isRolling) ? 0.6 : 1 }}>
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
            
            {/* Scrollable board container */}
            <div style={{ 
              maxWidth: "100%", 
              overflowX: "auto", 
              overflowY: "auto",
              display: "flex",
              justifyContent: "center",
              padding: "8px",
              background: "rgba(0,0,0,0.3)",
              borderRadius: "20px"
            }}>
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
            {algo1Time !== null && (
              <div style={{ background: "rgba(255,255,255,0.07)", borderRadius: "10px", padding: "0.8rem", marginBottom: "1rem", textAlign: "left" }}>
                <p style={{ color: "rgba(255,255,255,0.5)", fontSize: "10px", marginBottom: "5px" }}>Algorithm Performance</p>
                <p style={{ color: "#fff", fontSize: "11px" }}>⚡ BFS: <strong>{algo1Time.toFixed(2)}ms</strong></p>
                <p style={{ color: "#fff", fontSize: "11px" }}>📊 DP: <strong>{algo2Time.toFixed(2)}ms</strong></p>
              </div>
            )}
            <div style={{ display: "flex", gap: "8px" }}>
              <button onClick={resetGame} style={{ flex: 1, padding: "10px", borderRadius: "8px", border: "1px solid rgba(255,255,255,0.2)", background: "transparent", color: "#fff", fontSize: "12px", cursor: "pointer" }}>
                Play again
              </button>
              <button onClick={() => navigate("/")} style={{ flex: 1, padding: "10px", borderRadius: "8px", border: "none", background: "#185FA5", color: "#fff", fontSize: "12px", cursor: "pointer" }}>
                Back to hub
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}