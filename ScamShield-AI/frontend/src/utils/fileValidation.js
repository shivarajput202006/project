const MAX_FILE_BYTES = 5 * 1024 * 1024;
const FILE_TYPES = {
  png: { label: "PNG image", signature: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a] },
  jpg: { label: "JPEG image", signature: [0xff, 0xd8, 0xff] },
  jpeg: { label: "JPEG image", signature: [0xff, 0xd8, 0xff] },
  webp: { label: "WebP image", signature: [0x52, 0x49, 0x46, 0x46], webp: true },
  pdf: { label: "PDF document", signature: [0x25, 0x50, 0x44, 0x46, 0x2d] },
  docx: { label: "Word document", signature: [0x50, 0x4b, 0x03, 0x04] },
};

function matchesSignature(bytes, signature) {
  return signature.every((byte, index) => bytes[index] === byte);
}

export async function validateSelectedFile(file) {
  if (!file) throw new Error("Choose a file to continue.");
  if (file.size === 0) throw new Error("The selected file is empty.");
  if (file.size > MAX_FILE_BYTES) throw new Error("Files must be 5 MB or smaller.");

  const extension = file.name.split(".").pop()?.toLowerCase();
  const type = FILE_TYPES[extension];
  if (!type || file.name.includes("..") || /[\\/]/.test(file.name)) {
    throw new Error("Choose a PNG, JPG, JPEG, WEBP, PDF, or DOCX file.");
  }

  const bytes = new Uint8Array(await file.slice(0, 16).arrayBuffer());
  if (!matchesSignature(bytes, type.signature)) {
    throw new Error("The file content does not match its file type.");
  }
  if (type.webp) {
    const format = String.fromCharCode(...bytes.slice(8, 12));
    const chunk = String.fromCharCode(...bytes.slice(12, 16));
    const declaredSize = bytes[4] | (bytes[5] << 8) | (bytes[6] << 16) | (bytes[7] << 24);
    if (
      format !== "WEBP"
      || declaredSize < 12
      || declaredSize + 8 > file.size
      || !["VP8 ", "VP8L", "VP8X"].includes(chunk)
    ) {
      throw new Error("The selected WebP image is malformed.");
    }
  }

  return { label: type.label };
}

export function formatFileSize(size) {
  if (size < 1024 * 1024) return `${Math.max(1, Math.round(size / 1024))} KB`;
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}
