import { Link } from "react-router-dom";
import { ArrowUpRight } from "lucide-react";

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="page-container">
        <div className="footer-main">
          <div className="footer-brand-block">
            <Link className="brand" to="/">
              <img src="/shield.svg" alt="" width="32" height="32" />
              <span>ScamShield<span className="brand-ai"> AI</span></span>
            </Link>
            <p>Helping you make your next career move with more confidence and care.</p>
          </div>
          <div className="footer-column">
            <h2>Explore</h2>
            <a href="#features">Features</a>
            <a href="#how-it-works">How it works</a>
            <a href="#about">About us</a>
          </div>
          <div className="footer-column">
            <h2>Resources</h2>
            <a href="#contact">Contact (placeholder) <ArrowUpRight size={13} /></a>
            <a href="#privacy">Privacy Policy (placeholder)</a>
            <a href="#terms">Terms (placeholder)</a>
          </div>
        </div>
        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} ScamShield AI. All rights reserved.</span>
          <span>Information is guidance, not a guarantee of safety.</span>
        </div>
      </div>
    </footer>
  );
}
