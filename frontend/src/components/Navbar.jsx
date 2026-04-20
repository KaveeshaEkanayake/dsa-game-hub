import { Link } from "react-router-dom";

export default function Navbar() {
  return (
    <nav className="navbar">
      <Link to="/" className="nav-logo">
        PDSA Games
      </Link>
    </nav>
  );
}