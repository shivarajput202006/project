import { Mail } from "lucide-react";

export default function EmailInputPanel({ value, onChange }) {
  function update(field, fieldValue) {
    onChange((current) => ({ ...current, [field]: fieldValue }));
  }

  return (
    <div className="scam-input-panel scam-email-fields">
      <label className="scam-field-label" htmlFor="scam-email-subject">
        <Mail size={15} /> Email details
      </label>
      <div className="scam-form-field">
        <label htmlFor="scam-email-subject">Subject</label>
        <input
          id="scam-email-subject"
          className="scam-text-input"
          value={value.subject}
          onChange={(event) => update("subject", event.target.value)}
          maxLength={300}
          placeholder="Job opportunity at Example Company"
          required
        />
      </div>
      <div className="scam-form-field">
        <label htmlFor="scam-email-sender">Sender email address</label>
        <input
          id="scam-email-sender"
          className="scam-text-input"
          type="email"
          value={value.senderEmail}
          onChange={(event) => update("senderEmail", event.target.value)}
          maxLength={254}
          placeholder="recruiter@example.com"
          required
        />
      </div>
      <div className="scam-form-field">
        <label htmlFor="scam-email-body">Email body</label>
        <textarea
          id="scam-email-body"
          className="scam-textarea"
          value={value.body}
          onChange={(event) => update("body", event.target.value)}
          maxLength={20000}
          rows={7}
          placeholder="Paste the email body you want to review…"
          required
        />
      </div>
      <p className="scam-field-help">Avoid including passwords, payment details, or identity-document numbers.</p>
    </div>
  );
}
