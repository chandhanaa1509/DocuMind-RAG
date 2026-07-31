export default function RetrievalTrace({ sources, scanning }) {
  if (scanning) {
    return (
      <div className="trace">
        <div className="trace-header">
          <span className="trace-title">scanning corpus</span>
          <div className="scan-bar">
            <div className="scan-sweep" />
          </div>
        </div>
      </div>
    )
  }

  if (!sources || sources.length === 0) return null

  const maxScore = Math.max(...sources.map((s) => s.score), 0.0001)

  return (
    <div className="trace">
      <div className="trace-header">
        <span className="trace-title">retrieval trace</span>
        <span className="trace-count">{sources.length} chunks used</span>
      </div>
      <div className="trace-list">
        {sources.map((s, idx) => (
          <div className="trace-item" key={`${s.sourceName}-${s.chunkIndex}`} style={{ animationDelay: `${idx * 90}ms` }}>
            <div className="trace-item-top">
              <span className="trace-badge">[{idx + 1}]</span>
              <span className="trace-source">{s.sourceName}</span>
              <span className="trace-chunk">chunk #{s.chunkIndex}</span>
              <span className="trace-score">{(s.score * 100).toFixed(1)}%</span>
            </div>
            <div className="trace-bar-track">
              <div
                className="trace-bar-fill"
                style={{ width: `${(s.score / maxScore) * 100}%` }}
              />
            </div>
            <div className="trace-snippet">{s.snippet}</div>
          </div>
        ))}
      </div>
    </div>
  )
}
