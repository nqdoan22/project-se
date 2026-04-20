export default function ChatSessionList({ sessions, currentId, onOpen, onNew, onDelete }) {
  return (
    <aside className="chat-sidebar card">
      <div className="chat-sidebar-header">
        <button className="btn btn-primary btn-sm" onClick={onNew}>
          + Cuộc trò chuyện mới
        </button>
      </div>
      <div className="chat-session-list">
        {sessions.length === 0 && (
          <div className="chat-empty">Chưa có cuộc trò chuyện.</div>
        )}
        {sessions.map((s) => (
          <div
            key={s.sessionId}
            className={`chat-session-item ${s.sessionId === currentId ? 'active' : ''}`}
            onClick={() => onOpen(s.sessionId)}
          >
            <div className="chat-session-title" title={s.title || 'Untitled'}>
              {s.title || 'Cuộc trò chuyện'}
            </div>
            <button
              className="btn btn-icon btn-sm"
              title="Xoá"
              onClick={(e) => {
                e.stopPropagation();
                if (confirm('Xoá cuộc trò chuyện này?')) onDelete(s.sessionId);
              }}
            >
              🗑
            </button>
          </div>
        ))}
      </div>
    </aside>
  );
}
