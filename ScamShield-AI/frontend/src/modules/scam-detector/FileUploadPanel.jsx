import { useEffect, useState } from "react";
import { FileText, Image, LoaderCircle, Trash2, UploadCloud } from "lucide-react";
import { formatFileSize } from "../../utils/fileValidation.js";

export default function FileUploadPanel({ file, fileError, uploadProgress, onSelect, onRemove }) {
  const [previewUrl, setPreviewUrl] = useState("");
  const [dragging, setDragging] = useState(false);

  useEffect(() => {
    if (!file || !file.type.startsWith("image/")) {
      setPreviewUrl("");
      return undefined;
    }
    const url = URL.createObjectURL(file);
    setPreviewUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);

  return (
    <div className="scam-input-panel">
      <label
        className={`scam-upload-dropzone${file ? " has-file" : ""}${dragging ? " is-dragging" : ""}`}
        htmlFor="scam-file"
        onDragOver={(event) => { event.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={(event) => {
          event.preventDefault();
          setDragging(false);
          onSelect(event.dataTransfer.files?.[0] ?? null);
        }}
      >
        <input
          id="scam-file"
          className="scam-visually-hidden"
          type="file"
          accept=".png,.jpg,.jpeg,.webp,.pdf,.docx"
          onChange={(event) => {
            onSelect(event.target.files?.[0] ?? null);
            event.target.value = "";
          }}
        />
        {file ? (
          previewUrl ? (
            <img className="scam-image-preview" src={previewUrl} alt="Selected image preview" />
          ) : (
            <span className="scam-upload-icon"><FileText size={22} /></span>
          )
        ) : (
          <>
            <span className="scam-upload-icon"><UploadCloud size={22} /></span>
            <strong>Choose a file or drag it here</strong>
            <span>PNG, JPG, JPEG, WEBP, PDF, or DOCX · 5 MB max</span>
          </>
        )}
      </label>
      {file && (
        <div className="scam-selected-file">
          <span className="scam-selected-file-icon">
            {file.type.startsWith("image/") ? <Image size={17} /> : <FileText size={17} />}
          </span>
          <span className="scam-selected-file-copy"><strong>{file.name}</strong><small>{formatFileSize(file.size)}</small></span>
          <button type="button" className="scam-remove-file" aria-label="Remove selected file" onClick={onRemove}>
            <Trash2 size={16} />
          </button>
        </div>
      )}
      {fileError && <p className="scam-inline-error" role="alert">{fileError}</p>}
      {uploadProgress !== null && (
        <div className="scam-upload-progress" role="status">
          <div className="scam-progress-copy">
            <span><LoaderCircle size={14} className="scam-spin" /> Uploading file</span>
            <strong>{uploadProgress}%</strong>
          </div>
          <div className="scam-progress-track"><span style={{ width: `${uploadProgress}%` }} /></div>
        </div>
      )}
      <p className="scam-field-help">Files are inspected in memory and are not saved as uploads. Avoid identity documents or sensitive personal data.</p>
    </div>
  );
}
