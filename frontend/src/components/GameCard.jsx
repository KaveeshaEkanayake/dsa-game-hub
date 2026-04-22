import { Link } from "react-router-dom";

export default function GameCard({ title, description, path }) {
  return (
    <div className="game-card">
      <h3>{title}</h3>
      <p>{description}</p>
      <Link to={path}>
        <button>Open Game</button>
      </Link>
    </div>
  );
}