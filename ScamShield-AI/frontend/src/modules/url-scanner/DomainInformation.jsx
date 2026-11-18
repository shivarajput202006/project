import { Globe2, LockKeyhole, UnlockKeyhole } from "lucide-react";

export default function DomainInformation({ result }) {
  const isHttps = result.domainInformation?.scheme === "https";
  const TransportIcon = isHttps ? LockKeyhole : UnlockKeyhole;

  return (
    <section className="url-result-panel">
      <div className="url-panel-heading"><Globe2 size={17} /><h3>Domain information</h3></div>
      <dl className="url-domain-grid">
        <div><dt>Normalized domain</dt><dd>{result.normalizedDomain}</dd></div>
        <div><dt>Hostname type</dt><dd>{result.domainInformation?.hostnameType ?? "Unknown"}</dd></div>
        <div>
          <dt>Transport</dt>
          <dd className={isHttps ? "url-transport-https" : "url-transport-http"}>
            <TransportIcon size={14} /> {isHttps ? "HTTPS" : "HTTP (not encrypted)"}
          </dd>
        </div>
        <div><dt>Registration lookup</dt><dd>{result.domainInformation?.registrationStatus ?? "Unavailable"}</dd></div>
      </dl>
      <p className="url-panel-note">
        HTTPS encrypts a connection but does not confirm the operator's identity or make a site trustworthy.
      </p>
    </section>
  );
}
