import { useState } from 'react';

export default function FeedbackButtons({ queryLogId, onFeedback }) {
  const [choice, setChoice] = useState(null);
  if (!queryLogId) return null;
  const handle = (v) => {
    setChoice(v);
    onFeedback(queryLogId, v);
  };
  return (
    <div className="chat-feedback">
      <button
        className={`btn btn-icon btn-sm ${choice === 'up' ? 'active' : ''}`}
        disabled={choice !== null}
        onClick={() => handle('up')}
        title="Trả lời hữu ích"
      >
        👍
      </button>
      <button
        className={`btn btn-icon btn-sm ${choice === 'down' ? 'active' : ''}`}
        disabled={choice !== null}
        onClick={() => handle('down')}
        title="Trả lời chưa tốt"
      >
        👎
      </button>
    </div>
  );
}
