import { Activity, LayoutDashboard, ShieldAlert, Users, X } from "lucide-react";

const items = [
  { id: "overview", label: "Overview", icon: LayoutDashboard },
  { id: "users", label: "Users", icon: Users },
  { id: "security", label: "Security events", icon: ShieldAlert },
  { id: "system", label: "System health", icon: Activity },
];

export default function AdminSidebar({ active, onSelect, isOpen, onClose }) {
  return (
    <aside className={`admin-sidebar${isOpen ? " is-open" : ""}`}>
      <div className="admin-brand">
        <img src="/shield.svg" alt="" width="34" height="34" />
        <span>ScamShield <b>AI</b><small>ADMIN CONSOLE</small></span>
        <button className="admin-close-menu" type="button" onClick={onClose} aria-label="Close admin navigation"><X size={18} /></button>
      </div>
      <nav className="admin-nav" aria-label="Admin navigation">
        {items.map(({ id, label, icon: Icon }) => (
          <button key={id} type="button" className={`admin-nav-item${active === id ? " is-active" : ""}`} onClick={() => onSelect(id)}>
            <Icon size={17} aria-hidden="true" />{label}
          </button>
        ))}
      </nav>
      <p className="admin-readonly-note">Read-only console. Changes to accounts and platform settings are not enabled.</p>
    </aside>
  );
}
