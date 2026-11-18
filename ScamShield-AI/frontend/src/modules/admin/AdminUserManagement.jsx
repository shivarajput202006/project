import { ChevronLeft, ChevronRight, Search } from "lucide-react";
import { formatAdminDate } from "./adminHelpers.js";

export default function AdminUserManagement({ data, search, loading, error, onSearch, onPage }) {
  return (
    <section className="admin-panel">
      <div className="admin-panel-heading">
        <div><span className="admin-eyebrow">ACCOUNTS</span><h2>User directory</h2></div>
        <label className="admin-search"><Search size={16} /><input value={search} onChange={(event) => onSearch(event.target.value)} placeholder="Search name or email" /></label>
      </div>
      {loading ? (
        <p className="admin-empty" role="status">Loading accounts…</p>
      ) : error ? (
        <p className="admin-empty">Account data could not be loaded.</p>
      ) : !data?.content?.length ? (
        <p className="admin-empty">No users match this search.</p>
      ) : (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead><tr><th>User</th><th>Email</th><th>Role</th><th>Joined</th></tr></thead>
            <tbody>{data.content.map((user) => (
              <tr key={user.id}>
                <td>{user.fullName}</td>
                <td>{user.email}</td>
                <td><span className={`admin-role-badge role-${user.role?.toLowerCase()}`}>{user.role}</span></td>
                <td>{formatAdminDate(user.createdAt)}</td>
              </tr>
            ))}</tbody>
          </table>
        </div>
      )}
      <div className="admin-pagination">
        <span>{data ? `${data.totalElements} total accounts` : ""}</span>
        <div>
          <button type="button" aria-label="Previous page" disabled={!data || data.page <= 0} onClick={() => onPage(data.page - 1)}><ChevronLeft size={17} /></button>
          <span>{data ? `${data.page + 1} / ${Math.max(1, data.totalPages)}` : "—"}</span>
          <button type="button" aria-label="Next page" disabled={!data || data.page + 1 >= data.totalPages} onClick={() => onPage(data.page + 1)}><ChevronRight size={17} /></button>
        </div>
      </div>
    </section>
  );
}
