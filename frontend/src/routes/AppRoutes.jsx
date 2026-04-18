import { Routes, Route } from "react-router-dom";
import Home from "../pages/Home";
import MinimumCost from "../pages/MinimumCost";
import SnakeLadder from "../pages/SnakeLadder1";
import TrafficSimulation from "../pages/TrafficSimulation";
import KnightsTour from "../pages/KnightsTour";
import SixteenQueens from "../pages/SixteenQueens";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/minimum-cost" element={<MinimumCost />} />
      <Route path="/snake-ladder1" element={<SnakeLadder />} />
      <Route path="/traffic-simulation" element={<TrafficSimulation />} />
      <Route path="/knights-tour" element={<KnightsTour />} />
      <Route path="/sixteen-queens" element={<SixteenQueens />} />
    </Routes>
  );
}