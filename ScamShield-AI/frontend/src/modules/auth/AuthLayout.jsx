import { Link, Outlet } from "react-router-dom";
import { ShieldCheck } from "lucide-react";
import "./auth.css";

export default function AuthLayout() {
  return (
    <main className="auth-page">
      <div className="auth-orb auth-orb-one" aria-hidden="true" />
      <div className="auth-orb auth-orb-two" aria-hidden="true" />
      <header className="auth-header page-container">
        <Link className="brand" to="/" aria-label="ScamShield AI home">
          <img src="/shield.svg" alt="" width="34" height="34" />
          <span>ScamShield<span className="brand-ai"> AI</span></span>
        </Link>
        <Link to="/" className="auth-back-link">Back to home</Link>
      </header>
      <section className="auth-main page-container">
        <aside className="auth-promo">
          <span className="auth-promo-icon"><ShieldCheck size={25} /></span>
          <span className="eyebrow">YOUR CAREER. YOUR CALL.</span>
          <h1>Make your next move with more confidence.</h1>
          <p>Tools to help you spot warning signs, explore opportunities, and make informed decisions at every step.</p>
          <div className="auth-promo-foot">Thoughtful guidance. You stay in control.</div>
        </aside>
        <div className="auth-form-wrap"><Outlet /></div>
      </section>
      <footer className="auth-footer page-container">
        <span><ShieldCheck size={14} /> ScamShield AI</span>
        <span>Career safety tools, with your privacy in mind.</span>
      </footer>
    </main>
  );
}
