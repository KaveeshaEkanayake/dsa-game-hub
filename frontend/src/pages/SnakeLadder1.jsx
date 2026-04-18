
import React, { useState } from 'react';

const SnakeLadder = () => {
  const [boardSize, setBoardSize] = useState(8);
  const [playerName, setPlayerName] = useState('');
  const [gameStarted, setGameStarted] = useState(false);
  const [playerPosition, setPlayerPosition] = useState(1);
  const [diceValue, setDiceValue] = useState(null);
  const [rolling, setRolling] = useState(false);
  const [message, setMessage] = useState('');
  const [moveHistory, setMoveHistory] = useState([]);
  const [gameWon, setGameWon] = useState(false);
  const [showQuestion, setShowQuestion] = useState(false);
  const [minThrows, setMinThrows] = useState(null);
  const [gameResult, setGameResult] = useState(null);
  
  // Snakes and Ladders positions
  const snakes = {
    16: 6,
    47: 26,
    49: 11,
    56: 53,
    62: 19,
    64: 60,
  };
  
  const ladders = {
    2: 38,
    7: 14,
    8: 31,
    15: 26,
    21: 42,
    28: 84,
    36: 44,
    51: 67,
    71: 91,
    78: 98,
  };
  
  // Calculate minimum throws using BFS
  const calculateMinThrows = () => {
    const target = boardSize * boardSize;
    const visited = new Array(target + 1).fill(false);
    const queue = [[1, 0]];
    visited[1] = true;
    
    while (queue.length > 0) {
      const current = queue.shift();
      const position = current[0];
      const throwsCount = current[1];
      
      if (position === target) return throwsCount;
      
      for (let dice = 1; dice <= 6; dice++) {
        let next = position + dice;
        if (next > target) continue;
        
        if (snakes[next]) next = snakes[next];
        if (ladders[next]) next = ladders[next];
        
        if (!visited[next]) {
          visited[next] = true;
          queue.push([next, throwsCount + 1]);
        }
      }
    }
    return -1;
  };
  
  const startGame = () => {
    if (!playerName.trim()) {
      setMessage('Please enter your name!');
      return;
    }
    setGameStarted(true);

    setPlayerPosition(1);
    setGameWon(false);
    setMoveHistory([]);
    setMessage('Welcome ' + playerName + '! Roll the dice to start.');
    const min = calculateMinThrows();
    setMinThrows(min);
  };
  
  const rollDice = () => {
    if (rolling || gameWon) return;
    
    setRolling(true);
    let count = 0;
    const interval = setInterval(function() {
      const random = Math.floor(Math.random() * 6) + 1;
      setDiceValue(random);
      count = count + 1;
      if (count > 10) {
        clearInterval(interval);
        setRolling(false);
        processMove(random);
      }
    }, 50);
  };
  
  const processMove = (dice) => {
    const totalCells = boardSize * boardSize;
    let newPosition = playerPosition + dice;
    let moveMsg = 'Rolled ' + dice + '! ';
    
    if (newPosition === totalCells) {
      moveMsg = moveMsg + 'You reached the end! 🎉';
      setPlayerPosition(newPosition);
      const newHistory = [moveMsg].concat(moveHistory).slice(0, 10);
      setMoveHistory(newHistory);
      setGameWon(true);
      setShowQuestion(true);
      return;
    }
    
    if (newPosition > totalCells) {
      moveMsg = moveMsg + 'Need ' + (totalCells - playerPosition) + ' to win. Stay at ' + playerPosition + '.';
      const newHistory = [moveMsg].concat(moveHistory).slice(0, 10);
      setMoveHistory(newHistory);
      setRolling(false);
      return;
    }
    
    if (snakes[newPosition]) {
      moveMsg = moveMsg + '🐍 Snake! ' + newPosition + ' → ' + snakes[newPosition];
      newPosition = snakes[newPosition];
    } else if (ladders[newPosition]) {
      moveMsg = moveMsg + '🪜 Ladder! ' + newPosition + ' → ' + ladders[newPosition];
      newPosition = ladders[newPosition];
    } else {
      moveMsg = moveMsg + 'Moved to ' + newPosition;
    }
    
    setPlayerPosition(newPosition);
    const newHistory = [moveMsg].concat(moveHistory).slice(0, 10);
    setMoveHistory(newHistory);
    
    if (newPosition === totalCells) {
      setGameWon(true);
      setShowQuestion(true);
    }
  };
  
  const handleGuess = (guess) => {
    const isCorrect = (guess === minThrows);
    setGameResult({
      correct: isCorrect,
      actual: minThrows,
      message: isCorrect ? 'Correct! You win bonus points!' : 'Wrong! The answer was ' + minThrows
    });
    setShowQuestion(false);
  };
  
  const resetGame = () => {
    setGameStarted(false);
    setGameWon(false);
    setShowQuestion(false);
    setGameResult(null);
    setPlayerPosition(1);
    setDiceValue(null);
    setMoveHistory([]);
    setMessage('');
  };
  
  // Build board display
  const buildBoard = () => {
    const totalCells = boardSize * boardSize;
    const cells = [];
    
    for (let row = boardSize - 1; row >= 0; row--) {
      for (let col = 0; col < boardSize; col++) {
        let cellNumber;
        if (row % 2 === 0) {
          cellNumber = (row * boardSize) + col + 1;
        } else {
          cellNumber = (row * boardSize) + (boardSize - col);
        }
        
        const isSnake = !!snakes[cellNumber];
        const isLadder = !!ladders[cellNumber];
        const isPlayer = (playerPosition === cellNumber);
        
        let bgColor = '#fff8f0';
        if (isSnake) bgColor = '#ffcccc';
        if (isLadder) bgColor = '#ccffcc';
        if (isPlayer) bgColor = '#ffe066';
        
        cells.push(
          React.createElement('div', {
            key: cellNumber,
            style: {
              width: '60px',
              height: '60px',
              backgroundColor: bgColor,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              position: 'relative',
              fontSize: '12px',
              fontWeight: 'bold',
              border: '1px solid #b8956e'
            }
          },
            React.createElement('span', { style: { fontSize: '11px', color: '#8b6914' } }, cellNumber),
            isSnake ? React.createElement('span', { style: { fontSize: '10px' } }, '🐍→' + snakes[cellNumber]) : null,
            isLadder ? React.createElement('span', { style: { fontSize: '10px' } }, '🪜→' + ladders[cellNumber]) : null,
            isPlayer ? React.createElement('span', { style: { position: 'absolute', bottom: '2px', right: '2px', fontSize: '16px' } }, '👤') : null
          )
        );
      }
    }
    return cells;
  };
  
  // Setup Screen
  if (!gameStarted) {
    return React.createElement('div', { style: { minHeight: '100vh', background: 'linear-gradient(135deg, #1a472a, #0d2818)', padding: '20px' } },
      React.createElement('div', { style: { maxWidth: '600px', margin: '0 auto', background: 'rgba(255, 255, 255, 0.95)', borderRadius: '20px', padding: '40px', textAlign: 'center' } },
        React.createElement('h1', { style: { color: '#2c5f2d', marginBottom: '30px' } }, '🐍 SNAKES & LADDERS 🪜'),
        React.createElement('div', { style: { display: 'flex', flexDirection: 'column', gap: '20px' } },
          React.createElement('input', {
            type: 'text',
            placeholder: 'Enter your name',
            value: playerName,
            onChange: function(e) { setPlayerName(e.target.value); },
            style: { padding: '12px', fontSize: '16px', border: '2px solid #ddd', borderRadius: '10px' }
          }),
          React.createElement('div', { style: { textAlign: 'left' } },
            React.createElement('label', {}, 'Board Size: ' + boardSize + 'x' + boardSize),
            React.createElement('input', {
              type: 'range',
              min: '6',
              max: '10',
              value: boardSize,
              onChange: function(e) { setBoardSize(parseInt(e.target.value)); },
              style: { width: '100%', marginTop: '10px' }
            })
          ),
          React.createElement('button', {
            onClick: startGame,
            style: { padding: '15px', fontSize: '18px', background: '#28a745', color: 'white', border: 'none', borderRadius: '10px', cursor: 'pointer' }
          }, 'START GAME')
        ),
        React.createElement('div', { style: { marginTop: '30px', textAlign: 'left', background: '#f0f0f0', padding: '20px', borderRadius: '10px' } },
          React.createElement('h3', {}, 'Rules:'),
          React.createElement('ul', { style: { marginTop: '10px', paddingLeft: '20px' } },
            React.createElement('li', {}, '🎲 Roll the dice to move your token'),
            React.createElement('li', {}, '🐍 If you land on a snake\'s head, slide down'),
            React.createElement('li', {}, '🪜 If you land on a ladder\'s bottom, climb up'),
            React.createElement('li', {}, '🏆 First to reach cell ' + (boardSize * boardSize) + ' wins')
          )
        )
      )
    );
  }
  
  // Question Screen
  if (showQuestion) {
    let options = [minThrows, minThrows + 1];
    const below = minThrows - 1;
    if (below > 0) {
      options.push(below);
    } else {
      options.push(minThrows + 2);
    }
    options = options.sort(function() { return Math.random() - 0.5; });
    
    return React.createElement('div', { style: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0, 0, 0, 0.9)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 } },
      React.createElement('div', { style: { background: 'white', padding: '40px', borderRadius: '20px', textAlign: 'center', maxWidth: '450px' } },
        React.createElement('h2', {}, '🎯 BONUS QUESTION! 🎯'),
        React.createElement('p', {}, 'What was the minimum number of dice throws needed to win?'),
        React.createElement('div', { style: { display: 'flex', gap: '15px', justifyContent: 'center', marginTop: '20px' } },
          options.map(function(opt) {
            return React.createElement('button', {
              key: opt,
              onClick: function() { handleGuess(opt); },
              style: { padding: '12px 25px', fontSize: '16px', background: '#007bff', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer' }
            }, opt + ' throws');
          })
        )
      )
    );
  }
  
  // Result Screen
  if (gameResult) {
    const bgColor = gameResult.correct ? '#d4edda' : '#f8d7da';
    return React.createElement('div', { style: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0, 0, 0, 0.9)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 } },
      React.createElement('div', { style: { background: bgColor, padding: '40px', borderRadius: '20px', textAlign: 'center', maxWidth: '450px' } },
        React.createElement('h1', {}, gameResult.correct ? '🎉 CORRECT! 🎉' : '😢 GAME OVER 😢'),
        React.createElement('p', {}, gameResult.message),
        React.createElement('p', { style: { marginTop: '10px', fontWeight: 'bold' } }, 'Actual minimum throws: ' + gameResult.actual),
        React.createElement('button', {
          onClick: resetGame,
          style: { marginTop: '20px', padding: '10px 30px', background: '#28a745', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer' }
        }, 'PLAY AGAIN')
      )
    );
  }
  
  // Game Screen
  return React.createElement('div', { style: { minHeight: '100vh', background: 'linear-gradient(135deg, #1a472a, #0d2818)', padding: '20px' } },
    React.createElement('div', { style: { background: 'rgba(255, 255, 255, 0.95)', borderRadius: '15px', padding: '15px 25px', marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap' } },
      React.createElement('h1', { style: { margin: 0, color: '#2c5f2d' } }, '🐍 SNAKES & LADDERS 🪜'),
      React.createElement('div', { style: { display: 'flex', gap: '20px', fontWeight: 'bold' } },
        React.createElement('span', {}, '👤 ' + playerName),
        React.createElement('span', {}, '📍 Position: ' + playerPosition),
        React.createElement('span', {}, '🎯 Target: ' + (boardSize * boardSize))
      )
    ),
    React.createElement('div', { style: { display: 'flex', gap: '30px', flexWrap: 'wrap', justifyContent: 'center' } },
      React.createElement('div', { style: { background: '#f5e6ca', padding: '20px', borderRadius: '15px', border: '4px solid #c4a35a' } },
        React.createElement('div', { style: { display: 'grid', gridTemplateColumns: 'repeat(' + boardSize + ', 1fr)', gap: '2px', backgroundColor: '#c4a35a' } },
          buildBoard()
        )
      ),
      React.createElement('div', { style: { width: '280px', display: 'flex', flexDirection: 'column', gap: '20px' } },
        React.createElement('div', { style: { background: 'rgba(255, 255, 255, 0.95)', padding: '20px', borderRadius: '15px', textAlign: 'center' } },
          React.createElement('div', { style: { width: '80px', height: '80px', background: 'white', border: '3px solid #333', borderRadius: '15px', margin: '0 auto 15px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '36px', fontWeight: 'bold', animation: rolling ? 'rollDice 0.1s linear infinite' : 'none' } },
            diceValue || '?'
          ),
          React.createElement('button', {
            onClick: rollDice,
            disabled: rolling || gameWon,
            style: { padding: '12px 20px', fontSize: '16px', background: '#ff6b6b', color: 'white', border: 'none', borderRadius: '10px', cursor: 'pointer' }
          }, rolling ? 'ROLLING...' : '🎲 ROLL DICE 🎲')
        ),
        React.createElement('div', { style: { background: 'rgba(0, 0, 0, 0.8)', color: 'white', padding: '15px', borderRadius: '10px', textAlign: 'center' } },
          React.createElement('p', {}, message)
        ),
        React.createElement('div', { style: { background: 'rgba(0, 0, 0, 0.8)', color: 'white', padding: '15px', borderRadius: '10px', maxHeight: '300px', overflowY: 'auto' } },
          React.createElement('h3', { style: { marginBottom: '10px' } }, 'Move History'),
          moveHistory.map(function(move, idx) {
            return React.createElement('div', { key: idx, style: { padding: '5px', borderBottom: '1px solid #444', fontSize: '12px' } }, move);
          })
        ),
        React.createElement('button', {
          onClick: resetGame,
          style: { padding: '12px', background: '#dc3545', color: 'white', border: 'none', borderRadius: '10px', cursor: 'pointer' }
        }, 'QUIT GAME')
      )
    ),
    React.createElement('style', {}, `
      @keyframes rollDice {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    `)
  );
};
/* hi*/

export default SnakeLadder;