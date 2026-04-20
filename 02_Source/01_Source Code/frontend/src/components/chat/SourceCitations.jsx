import { useState } from 'react';

export default function SourceCitations({ sources }) {
  const [open, setOpen] = useState(false);
  if (!sources || sources.length === 0) return null;
  return (
    <div className="chat-sources">
      <button
        className="chat-sources-toggle"
        onClick={() => setOpen((o) => !o)}
      >
        {open ? '▾' : '▸'} Nguồn ({sources.length})
      </button>
      {open && (
        <ul className="chat-sources-list">
          {sources.map((s, i) => (
            <li key={i}>
              <span className="badge badge-quarantine">{s.sourceTable}</span>
              <span className="chat-source-pk">#{s.sourcePk}</span>
              <span className="chat-source-score">
                {typeof s.score === 'number' ? s.score.toFixed(3) : ''}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
