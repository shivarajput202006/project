import { Camera, LoaderCircle, Trash2, Upload } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { profileService } from "../services/profileService.js";
import { validatePhoto } from "../utils/profileValidation.js";

export default function ProfilePhotoUpload({ photoUrl, fullName, onPhotoChanged }) {
  const inputRef = useRef(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [previewUrl, setPreviewUrl] = useState("");

  useEffect(() => () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
  }, [previewUrl]);

  async function upload(file) {
    const validationError = validatePhoto(file);
    if (validationError) {
      setError(validationError);
      return;
    }
    const preview = URL.createObjectURL(file);
    setPreviewUrl(preview);
    setBusy(true);
    setError("");
    try {
      await profileService.uploadPhoto(file);
      await onPhotoChanged();
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setBusy(false);
      if (inputRef.current) inputRef.current.value = "";
      URL.revokeObjectURL(preview);
      setPreviewUrl("");
    }
  }

  async function remove() {
    setBusy(true);
    setError("");
    try {
      await profileService.deletePhoto();
      await onPhotoChanged();
    } catch (removeError) {
      setError(removeError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="profile-card profile-photo-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">PROFILE PHOTO</span><h2>Your photo</h2></div><Camera size={18} /></div>
      <div className="profile-photo-content">
        {previewUrl || photoUrl ? <img className="profile-photo-preview" src={previewUrl || photoUrl} alt={`${fullName || "Your"} profile`} />
          : <span className="profile-photo-placeholder" aria-hidden="true">{fullName?.trim()?.[0]?.toUpperCase() ?? "U"}</span>}
        <div>
          <p>Private image, visible only in your signed-in account.</p>
          <div className="profile-inline-actions">
            <button type="button" className="profile-primary-button" disabled={busy} onClick={() => inputRef.current?.click()}>
              {busy ? <LoaderCircle className="profile-spin" size={15} /> : <Upload size={15} />}
              {photoUrl ? "Replace photo" : "Upload photo"}
            </button>
            {photoUrl && <button type="button" className="profile-danger-quiet" disabled={busy} onClick={remove}><Trash2 size={15} /> Remove</button>}
          </div>
          <input ref={inputRef} className="profile-visually-hidden" type="file" accept="image/jpeg,image/png,image/webp"
            aria-label="Choose a profile photo" onChange={(event) => event.target.files?.[0] && upload(event.target.files[0])} />
        </div>
      </div>
      <p className="profile-help">JPEG, PNG, or WEBP · 5 MB maximum</p>
      {error && <p className="profile-field-error" role="alert">{error}</p>}
    </section>
  );
}
