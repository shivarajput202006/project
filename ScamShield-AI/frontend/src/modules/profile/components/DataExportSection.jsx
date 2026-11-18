import { Download, FileArchive } from "lucide-react";
import { useState } from "react";
import { profileService } from "../services/profileService.js";

export default function DataExportSection() {
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function exportData() {
    setBusy(true);
    setError("");
    setMessage("");
    try {
      const { blob, filename } = await profileService.exportData();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
      setMessage("Your private account export has been downloaded.");
    } catch (exportError) {
      setError(exportError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">YOUR ACCOUNT DATA</span><h2>Export my data</h2></div><FileArchive size={18} /></div>
      <p className="profile-section-description">Download a private archive containing your profile, settings, saved-job references, analysis history, and uploaded resume files.</p>
      <button className="profile-secondary-button" type="button" disabled={busy} onClick={exportData}><Download size={15} />{busy ? "Preparing export…" : "Download my account data"}</button>
      {message && <p className="profile-success" role="status">{message}</p>}
      {error && <p className="profile-field-error" role="alert">{error}</p>}
    </section>
  );
}
