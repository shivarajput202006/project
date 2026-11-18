import { Lightbulb } from "lucide-react";

export default function URLRecommendations({ recommendations }) {
  return (
    <section className="url-result-panel">
      <div className="url-panel-heading"><Lightbulb size={17} /><h3>Recommendations</h3></div>
      <ul className="url-recommendation-list">
        {recommendations.map((recommendation, index) => <li key={`${index}-${recommendation}`}>{recommendation}</li>)}
      </ul>
    </section>
  );
}
