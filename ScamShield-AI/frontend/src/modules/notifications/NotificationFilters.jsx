import NotificationTabs from "./NotificationTabs.jsx";

export default function NotificationFilters({ filter, onChange }) {
  return <NotificationTabs value={filter} onChange={onChange} />;
}
