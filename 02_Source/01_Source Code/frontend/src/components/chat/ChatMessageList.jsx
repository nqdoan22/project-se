import { useEffect, useRef } from 'react';
import SourceCitations from './SourceCitations';
import FeedbackButtons from './FeedbackButtons';

export default function ChatMessageList({ messages, sending, onFeedback }) {
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, sending]);

  if (messages.length === 0 && !sending) {
    return (
      <div className="chat-empty-state">
        <h3>Xin chào 👋</h3>
        <p>Hỏi về lô hàng, vật tư, QC, hoặc sản xuất. Ví dụ:</p>
        <ul>
          <li>Còn bao nhiêu lô API còn hạn?</li>
          <li>Lô LOT-001 hiện ở trạng thái nào?</li>
          <li>QC test nào fail tháng này?</li>
        </ul>
      </div>
    );
  }

  return (
    <div className="chat-messages">
      {messages.map((m) => (
        <div key={m.messageId} className={`chat-message chat-message-${m.role}`}>
          <div className="chat-bubble">
            <div className="chat-bubble-content">{m.content}</div>
            {m.role === 'assistant' && (
              <>
                <SourceCitations sources={m.sources} />
                <FeedbackButtons queryLogId={m.queryLogId} onFeedback={onFeedback} />
              </>
            )}
          </div>
        </div>
      ))}
      {sending && (
        <div className="chat-message chat-message-assistant">
          <div className="chat-bubble">
            <div className="chat-typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      )}
      <div ref={endRef} />
    </div>
  );
}
