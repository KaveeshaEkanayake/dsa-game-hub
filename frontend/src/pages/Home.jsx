import Navbar from "../components/Navbar";
import GameCard from "../components/GameCard";

export default function Home() {
  const games = [
    {
      title: "Minimum Cost",
      description: "Solve the task assignment problem using two algorithms.",
      path: "/minimum-cost",
    },
    {
      title: "Snake and Ladder",
      description: "Find the minimum dice throws to reach the last cell.",
      path: "/snake-ladder",
    },
    {
      title: "Traffic Simulation",
      description: "Calculate maximum flow in the traffic network.",
      path: "/traffic-simulation",
    },
    {
      title: "Knight's Tour",
      description: "Find the sequence of knight moves across the board.",
      path: "/knights-tour",
    },
    {
      title: "Sixteen Queens",
      description: "Place queens on the board without threatening each other.",
      path: "/sixteen-queens",
    },
  ];

  return (
    <>
      <Navbar />
      <div className="home-container">
        <h1>PDSA Coursework Game Portal</h1>
        <p>Select a game to continue</p>

        <div className="games-grid">
          {games.map((game) => (
            <GameCard
              key={game.path}
              title={game.title}
              description={game.description}
              path={game.path}
            />
          ))}
        </div>
      </div>
    </>
  );
}