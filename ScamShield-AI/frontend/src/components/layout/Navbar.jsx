import { useState } from "react";
import { Link } from "react-router-dom";
import { Menu, X } from "lucide-react";
import PrimaryButton from "../ui/PrimaryButton.jsx";

const links = [
  { label: "Home", href: "#home" },
  { label: "Features", href: "#features" },
  { label: "How it works", href: "#how-it-works" },
  { label: "About", href: "#about" },
];

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false);

  function closeMenu() {
    setMenuOpen(false);
  }

  return (
    <header className="site-header">
      <nav className="navbar page-container" aria-label="Main navigation">
        <Link className="brand" to="/" aria-label="ScamShield AI home" onClick={closeMenu}>
          <img src="/shield.svg" alt="" width="34" height="34" />
          <span>ScamShield<span className="brand-ai"> AI</span></span>
        </Link>

        <button
          className="mobile-menu-toggle"
          type="button"
          aria-label={menuOpen ? "Close navigation menu" : "Open navigation menu"}
          aria-expanded={menuOpen}
          aria-controls="primary-navigation"
          onClick={() => setMenuOpen((open) => !open)}
        >
          {menuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <div id="primary-navigation" className={`nav-content${menuOpen ? " nav-open" : ""}`}>
          <div className="nav-links">
            {links.map((link) => (
              <a key={link.href} href={link.href} onClick={closeMenu}>{link.label}</a>
            ))}
          </div>
          <div className="nav-actions">
            <Link className="login-link" to="/login" onClick={closeMenu}>Login</Link>
            <PrimaryButton to="/register" className="nav-cta" onClick={closeMenu}>Get Started</PrimaryButton>
          </div>
        </div>
      </nav>
    </header>
  );
}
