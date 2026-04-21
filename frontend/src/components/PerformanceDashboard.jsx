// src/components/PerformanceDashboard.jsx
import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export default function PerformanceDashboard({ playerName, onClose }) {
  const [performanceData, setPerformanceData] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [chartType, setChartType] = useState('line'); // 'line' or 'bar'

  useEffect(() => {
    fetchPerformanceData();
  }, [playerName]);

  const fetchPerformanceData = async () => {
    setLoading(true);
    try {
      // Fetch history
      const historyUrl = playerName 
        ? `${API_URL}/api/snake-ladder/performance/history?playerName=${encodeURIComponent(playerName)}`
        : `${API_URL}/api/snake-ladder/performance/history`;
      
      const historyResponse = await fetch(historyUrl);
      const historyData = await historyResponse.json();
      
      // Format data for charts
      const formattedData = historyData.map((record, idx) => ({
        round: idx + 1,
        bfsTime: record.bfsTimeMs,
        dpTime: record.dpTimeMs,
        boardSize: record.boardSize,
        winner: record.winner,
        timestamp: new Date(record.timestamp).toLocaleString()
      }));
      
      setPerformanceData(formattedData);
      
      // Fetch stats
      const statsUrl = playerName
        ? `${API_URL}/api/snake-ladder/performance/stats?playerName=${encodeURIComponent(playerName)}`
        : `${API_URL}/api/snake-ladder/performance/stats`;
      
      const statsResponse = await fetch(statsUrl);
      const statsData = await statsResponse.json();
      setStats(statsData);
      
    } catch (error) {
      console.error("Error fetching performance data:", error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={styles.modal}>
        <div style={styles.modalContent}>
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <div style={styles.spinner}></div>
            <p style={{ color: '#fff', marginTop: '20px' }}>Loading performance data...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!stats || performanceData.length === 0) {
    return (
      <div style={styles.modal}>
        <div style={styles.modalContent}>
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <h3 style={{ color: '#FFD700' }}>📊 No Performance Data Yet</h3>
            <p style={{ color: '#aaa', marginTop: '10px' }}>
              Play {playerName ? `${playerName}` : 'some'} games to see performance charts!
            </p>
            <button onClick={onClose} style={styles.closeButton}>
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.modal}>
      <div style={styles.modalContent}>
        {/* Header */}
        <div style={styles.header}>
          <div>
            <span style={{ fontSize: '24px', marginRight: '10px' }}>📊</span>
            <span style={styles.title}>Algorithm Performance Analysis</span>
            {playerName && <span style={styles.subtitle}>Player: {playerName}</span>}
          </div>
          <button onClick={onClose} style={styles.closeBtn}>✕</button>
        </div>

        {/* Statistics Cards */}
        <div style={styles.statsGrid}>
          <div style={styles.statCard}>
            <p style={styles.statLabel}>Total Rounds</p>
            <p style={styles.statValue}>{stats.totalRounds}</p>
            <p style={styles.statSub}>Games completed</p>
          </div>
          <div style={styles.statCard}>
            <p style={styles.statLabel}>Avg BFS Time</p>
            <p style={{ ...styles.statValue, color: '#FFD700' }}>{stats.avgBfsMs} ms</p>
            <p style={styles.statSub}>Best: {stats.bestBfsTime} ms</p>
          </div>
          <div style={styles.statCard}>
            <p style={styles.statLabel}>Avg DP Time</p>
            <p style={{ ...styles.statValue, color: '#4ade80' }}>{stats.avgDpMs} ms</p>
            <p style={styles.statSub}>Best: {stats.bestDpTime} ms</p>
          </div>
          <div style={styles.statCard}>
            <p style={styles.statLabel}>DP Faster</p>
            <p style={styles.statValue}>{stats.dpFasterPercentage}%</p>
            <p style={styles.statSub}>({stats.dpFasterCount} of {stats.totalRounds} rounds)</p>
          </div>
        </div>

        {/* Chart Type Selector */}
        <div style={styles.chartSelector}>
          <button 
            onClick={() => setChartType('line')}
            style={{ ...styles.chartBtn, background: chartType === 'line' ? '#FFD700' : 'rgba(255,255,255,0.1)', color: chartType === 'line' ? '#000' : '#fff' }}
          >
            📈 Line Chart
          </button>
          <button 
            onClick={() => setChartType('bar')}
            style={{ ...styles.chartBtn, background: chartType === 'bar' ? '#FFD700' : 'rgba(255,255,255,0.1)', color: chartType === 'bar' ? '#000' : '#fff' }}
          >
            📊 Bar Chart
          </button>
        </div>

        {/* Main Chart */}
        <div style={styles.chartContainer}>
          <ResponsiveContainer width="100%" height="100%">
            {chartType === 'line' ? (
              <LineChart data={performanceData}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                <XAxis 
                  dataKey="round" 
                  stroke="#fff" 
                  label={{ value: 'Game Round', position: 'bottom', fill: '#fff', offset: 0 }}
                />
                <YAxis 
                  stroke="#fff" 
                  label={{ value: 'Time (milliseconds)', angle: -90, position: 'insideLeft', fill: '#fff' }}
                />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#1a1a2e', border: '1px solid #FFD700' }}
                  formatter={(value, name) => [`${value} ms`, name === 'bfsTime' ? 'BFS Algorithm' : 'DP Algorithm']}
                  labelFormatter={(label) => `Round ${label}`}
                />
                <Legend />
                <Line 
                  type="monotone" 
                  dataKey="bfsTime" 
                  stroke="#FFD700" 
                  name="BFS Algorithm"
                  strokeWidth={2}
                  dot={{ r: 4, fill: '#FFD700' }}
                  activeDot={{ r: 6 }}
                />
                <Line 
                  type="monotone" 
                  dataKey="dpTime" 
                  stroke="#4ade80" 
                  name="DP Algorithm"
                  strokeWidth={2}
                  dot={{ r: 4, fill: '#4ade80' }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            ) : (
              <BarChart data={performanceData}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                <XAxis dataKey="round" stroke="#fff" />
                <YAxis stroke="#fff" />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#1a1a2e', border: '1px solid #FFD700' }}
                  formatter={(value, name) => [`${value} ms`, name]}
                />
                <Legend />
                <Bar dataKey="bfsTime" fill="#FFD700" name="BFS Algorithm" />
                <Bar dataKey="dpTime" fill="#4ade80" name="DP Algorithm" />
              </BarChart>
            )}
          </ResponsiveContainer>
        </div>

        {/* Detailed Data Table */}
        <div style={styles.tableContainer}>
          <h4 style={styles.tableTitle}>📋 Round-by-Round Performance Data</h4>
          <div style={styles.tableWrapper}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th style={styles.th}>Round</th>
                  <th style={styles.th}>Board Size</th>
                  <th style={styles.th}>BFS Time (ms)</th>
                  <th style={styles.th}>DP Time (ms)</th>
                  <th style={styles.th}>Difference</th>
                  <th style={styles.th}>Winner</th>
                </tr>
              </thead>
              <tbody>
                {performanceData.map((data) => {
                  const diff = (data.bfsTime - data.dpTime).toFixed(3);
                  const isDpFaster = data.dpTime < data.bfsTime;
                  return (
                    <tr key={data.round} style={styles.tr}>
                      <td style={styles.td}>{data.round}</td>
                      <td style={styles.td}>{data.boardSize}×{data.boardSize}</td>
                      <td style={{ ...styles.td, color: '#FFD700' }}>{data.bfsTime.toFixed(3)}</td>
                      <td style={{ ...styles.td, color: '#4ade80' }}>{data.dpTime.toFixed(3)}</td>
                      <td style={{ ...styles.td, color: isDpFaster ? '#4ade80' : '#FFD700' }}>
                        {isDpFaster ? `-${Math.abs(diff)}` : `+${diff}`}
                      </td>
                      <td style={{ ...styles.td, fontWeight: 'bold', color: data.winner === 'DP' ? '#4ade80' : '#FFD700' }}>
                        {data.winner}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Summary Analysis */}
        <div style={styles.analysisBox}>
          <h4 style={styles.analysisTitle}>📈 Performance Analysis</h4>
          <div style={styles.analysisContent}>
            <p>• <strong>BFS Algorithm</strong> average time: <span style={{ color: '#FFD700' }}>{stats.avgBfsMs} ms</span></p>
            <p>• <strong>DP Algorithm</strong> average time: <span style={{ color: '#4ade80' }}>{stats.avgDpMs} ms</span></p>
            <p>• DP was faster in <strong>{stats.dpFasterPercentage}%</strong> of the games ({stats.dpFasterCount}/{stats.totalRounds})</p>
            <p>• Best BFS performance: <strong>{stats.bestBfsTime} ms</strong> | Best DP performance: <strong>{stats.bestDpTime} ms</strong></p>
            <p style={{ marginTop: '10px', paddingTop: '10px', borderTop: '1px solid rgba(255,255,255,0.2)' }}>
              <strong>💡 Conclusion:</strong> {
                stats.avgBfsMs < stats.avgDpMs 
                  ? "BFS Algorithm performs better on average for this board configuration."
                  : "DP Algorithm performs better on average for this board configuration."
              } The difference is more noticeable on larger board sizes.
            </p>
          </div>
        </div>

        <button onClick={onClose} style={styles.footerButton}>
          Close Dashboard
        </button>
      </div>
    </div>
  );
}

// Styles
const styles = {
  modal: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0,0,0,0.95)',
    zIndex: 2000,
    overflowY: 'auto',
    padding: '20px'
  },
  modalContent: {
    maxWidth: '1200px',
    margin: '0 auto',
    background: 'linear-gradient(135deg, #0d3b6e, #0a2a4a)',
    borderRadius: '20px',
    padding: '20px',
    border: '2px solid #FFD700',
    boxShadow: '0 20px 40px rgba(0,0,0,0.5)'
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
    paddingBottom: '15px',
    borderBottom: '1px solid rgba(255,255,255,0.2)'
  },
  title: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#FFD700'
  },
  subtitle: {
    fontSize: '14px',
    color: '#aaa',
    marginLeft: '10px'
  },
  closeBtn: {
    background: 'rgba(255,255,255,0.1)',
    border: 'none',
    color: '#fff',
    fontSize: '20px',
    width: '36px',
    height: '36px',
    borderRadius: '18px',
    cursor: 'pointer',
    transition: 'all 0.2s'
  },
  statsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(4, 1fr)',
    gap: '15px',
    marginBottom: '25px'
  },
  statCard: {
    background: 'rgba(0,0,0,0.4)',
    borderRadius: '12px',
    padding: '15px',
    textAlign: 'center',
    border: '1px solid rgba(255,215,0,0.3)'
  },
  statLabel: {
    fontSize: '12px',
    color: '#aaa',
    marginBottom: '5px'
  },
  statValue: {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: '5px'
  },
  statSub: {
    fontSize: '10px',
    color: '#888'
  },
  chartSelector: {
    display: 'flex',
    gap: '10px',
    marginBottom: '20px',
    justifyContent: 'center'
  },
  chartBtn: {
    padding: '8px 20px',
    borderRadius: '20px',
    border: 'none',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: 'bold',
    transition: 'all 0.2s'
  },
  chartContainer: {
    height: '400px',
    marginBottom: '25px',
    background: 'rgba(0,0,0,0.2)',
    borderRadius: '12px',
    padding: '15px'
  },
  tableContainer: {
    marginBottom: '25px'
  },
  tableTitle: {
    color: '#FFD700',
    marginBottom: '10px',
    fontSize: '16px'
  },
  tableWrapper: {
    maxHeight: '300px',
    overflowY: 'auto',
    borderRadius: '8px'
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    color: '#fff',
    fontSize: '12px'
  },
  th: {
    padding: '10px',
    background: 'rgba(0,0,0,0.5)',
    position: 'sticky',
    top: 0,
    borderBottom: '1px solid #FFD700',
    textAlign: 'center'
  },
  td: {
    padding: '8px',
    textAlign: 'center',
    borderBottom: '1px solid rgba(255,255,255,0.1)'
  },
  tr: {
    transition: 'background 0.2s'
  },
  analysisBox: {
    background: 'rgba(0,0,0,0.4)',
    borderRadius: '12px',
    padding: '15px',
    marginBottom: '20px'
  },
  analysisTitle: {
    color: '#FFD700',
    marginBottom: '10px',
    fontSize: '16px'
  },
  analysisContent: {
    color: '#ddd',
    fontSize: '13px',
    lineHeight: '1.6'
  },
  footerButton: {
    width: '100%',
    padding: '12px',
    background: '#FFD700',
    border: 'none',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: 'bold',
    cursor: 'pointer',
    color: '#000',
    transition: 'all 0.2s'
  },
  spinner: {
    border: '4px solid rgba(255,215,0,0.3)',
    borderTop: '4px solid #FFD700',
    borderRadius: '50%',
    width: '40px',
    height: '40px',
    animation: 'spin 1s linear infinite',
    margin: '0 auto'
  }
};

// Add keyframes for spinner
const styleSheet = document.createElement("style");
styleSheet.textContent = `
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`;
document.head.appendChild(styleSheet);