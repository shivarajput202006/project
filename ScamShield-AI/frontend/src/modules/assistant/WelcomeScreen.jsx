import { Sparkles } from "lucide-react";
import QuickPrompts from "./QuickPrompts.jsx";
import SuggestionCards from "./SuggestionCards.jsx";

export default function WelcomeScreen({ onPrompt, disabled }) {
  return (
    <section className="assistant-welcome">
      <div className="assistant-welcome-icon"><Sparkles size={22} /></div>
      <span className="dashboard-eyebrow">YOUR CAREER COMPANION</span>
      <h2>Make your next move with confidence.</h2>
      <p>Ask about learning, interviews, resumes, job requirements, or safer job hunting. Guidance is informational—not a guarantee of hiring outcomes or job safety.</p>
      <QuickPrompts onSelect={onPrompt} disabled={disabled} />
      <SuggestionCards />
    </section>
  );
}
