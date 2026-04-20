import { Routes, Route } from 'react-router-dom'
import Dashboard from './components/Dashboard'
import KnightsTour from './pages/KnightsTour'
import SnakeLadder from './pages/SnakeLadder'
import SnakeLadder1 from './pages/SnakeLadder1'
import MinimumCost from './pages/MinimumCost'
import SixteenQueens from './pages/SixteenQueens'
import TrafficSimulation from './pages/TrafficSimulation'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Dashboard />} />
      <Route path="/knights-tour" element={<KnightsTour />} />
      <Route path="/snake-ladder" element={<SnakeLadder />} />
      <Route path="/snake-ladder/play" element={<SnakeLadder1 />} />
      <Route path="/mincost" element={<MinimumCost />} />
      <Route path="/queens" element={<SixteenQueens />} />
      <Route path="/traffic" element={<TrafficSimulation />} />
    </Routes>
  )
}

export default App