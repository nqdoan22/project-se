import { useCallback, useEffect, useState } from 'react';
import { ragApi } from '../services/api';

export function useChat() {
  const [sessions, setSessions] = useState([]);
  const [sessionId, setSessionId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');

  const refreshSessions = useCallback(async () => {
    try {
      const data = await ragApi.listSessions();
      setSessions(data);
      return data;
    } catch (e) {
      setError(e.response?.data?.message ?? 'Không thể tải danh sách');
      return [];
    }
  }, []);

  useEffect(() => {
    refreshSessions();
  }, [refreshSessions]);

  const openSession = useCallback(async (id) => {
    setLoading(true);
    setError('');
    try {
      const msgs = await ragApi.getMessages(id);
      setMessages(msgs);
      setSessionId(id);
    } catch (e) {
      setError(e.response?.data?.message ?? 'Không thể mở cuộc trò chuyện');
    } finally {
      setLoading(false);
    }
  }, []);

  const newSession = useCallback(() => {
    setSessionId(null);
    setMessages([]);
    setError('');
  }, []);

  const send = useCallback(async (question) => {
    if (!question || !question.trim()) return;
    setSending(true);
    setError('');
    const userMsg = {
      messageId: `local-${Date.now()}`,
      role: 'user',
      content: question,
      sources: [],
      createdDate: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userMsg]);
    try {
      const res = await ragApi.chat({ question, sessionId });
      const asstMsg = {
        messageId: `asst-${Date.now()}`,
        role: 'assistant',
        content: res.answer,
        sources: res.sources || [],
        queryLogId: res.queryLogId,
        createdDate: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, asstMsg]);
      if (!sessionId && res.sessionId) {
        setSessionId(res.sessionId);
        refreshSessions();
      }
    } catch (e) {
      setError(e.response?.data?.message ?? 'Gửi câu hỏi thất bại. Vui lòng thử lại.');
      setMessages((prev) => prev.filter((m) => m.messageId !== userMsg.messageId));
    } finally {
      setSending(false);
    }
  }, [sessionId, refreshSessions]);

  const sendFeedback = useCallback(async (queryLogId, feedback) => {
    if (!queryLogId) return;
    try {
      await ragApi.feedback(queryLogId, feedback);
    } catch {
      // feedback failures are non-critical
    }
  }, []);

  const removeSession = useCallback(async (id) => {
    try {
      await ragApi.deleteSession(id);
      if (id === sessionId) newSession();
      await refreshSessions();
    } catch (e) {
      setError(e.response?.data?.message ?? 'Không thể xoá cuộc trò chuyện');
    }
  }, [sessionId, newSession, refreshSessions]);

  return {
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
  };
}
