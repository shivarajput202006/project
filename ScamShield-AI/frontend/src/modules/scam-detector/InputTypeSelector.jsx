import { FileText, Image, Link2, Mail } from "lucide-react";

const inputTypes = [
  { id: "TEXT", label: "Paste text", icon: FileText },
  { id: "EMAIL", label: "Email", icon: Mail },
  { id: "URL", label: "URL", icon: Link2 },
  { id: "FILE", label: "Image or document", icon: Image },
];

export default function InputTypeSelector({ value, onChange }) {
  return (
    <div className="scam-input-type-selector" role="tablist" aria-label="Choose what to analyze">
      {inputTypes.map(({ id, label, icon: Icon }) => (
        <button
          key={id}
          className={`scam-input-type${value === id ? " is-selected" : ""}`}
          type="button"
          role="tab"
          aria-selected={value === id}
          onClick={() => onChange(id)}
        >
          <Icon size={16} aria-hidden="true" />
          <span>{label}</span>
        </button>
      ))}
    </div>
  );
}
