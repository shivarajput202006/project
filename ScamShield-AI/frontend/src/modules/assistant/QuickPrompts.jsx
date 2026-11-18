import { Code2, FileText, ShieldAlert, Sparkles } from "lucide-react";

const PROMPTS = [
  { icon: Code2, title: "Learn backend development", text: "Create a beginner-friendly roadmap for learning Java and Spring Boot." },
  { icon: Sparkles, title: "Prepare for interviews", text: "Give me a practical plan to prepare for a junior Java developer interview." },
  { icon: FileText, title: "Improve my resume", text: "What are some general ways to make a technical resume clearer and stronger?" },
  { icon: ShieldAlert, title: "Stay safe while job hunting", text: "What warning signs should I look for before accepting a job offer?" },
];

export default function QuickPrompts({ onSelect, disabled }) {
  return (
    <div className="assistant-quick-prompts">
      {PROMPTS.map(({ icon: Icon, title, text }) => (
        <button type="button" key={title} onClick={() => onSelect(text)} disabled={disabled}>
          <Icon size={16} /><span><strong>{title}</strong><small>{text}</small></span>
        </button>
      ))}
    </div>
  );
}
