import { useChat } from '../hooks/useChat';
import ChatSessionList from '../components/chat/ChatSessionList';
import ChatMessageList from '../components/chat/ChatMessageList';
import ChatInput from '../components/chat/ChatInput';

export default function ChatPage() {
  const {
    sessions,
    sessionId,
    messages,
    loading,
    sending,
    error,
    openSession,
    newSession,
    send,
    sendFeedback,
    removeSession,
  } = useChat();

  return (
    <>
      <header className="page-header">
        <div className="page-header-left">
          <h1>💬 Trò chuyện AI</h1>
          <p>Hỏi đáp về tồn kho, lô hàng, QC bằng tiếng Việt.</p>
        </div>
      </header>
      <section className="page-body">
        <div className="chat-layout">
          <ChatSessionList
            sessions={sessions}
            currentId={sessionId}
            onOpen={openSession}
            onNew={newSession}
            onDelete={removeSession}
          />
          <div className="chat-main card">
            {error && <div className="chat-error">{error}</div>}
            {loading ? (
              <div className="chat-loading">Đang tải...</div>
            ) : (
              <ChatMessageList
                messages={messages}
                sending={sending}
                onFeedback={sendFeedback}
              />
            )}
            <ChatInput onSend={send} disabled={sending || loading} />
          </div>
        </div>
      </section>
    </>
  );
}
