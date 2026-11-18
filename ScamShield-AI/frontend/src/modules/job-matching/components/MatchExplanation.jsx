import { Info } from "lucide-react";

export default function MatchExplanation({ explanation }) {
  return (
    <div className="match-explanation">
      <Info size={15} aria-hidden="true" />
      <div><strong>How this estimate was calculated</strong><p>{explanation.summary}</p><small>{explanation.disclaimer} Scoring model: {explanation.scoringModelVersion}.</small></div>
    </div>
  );
}
