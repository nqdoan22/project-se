import { useState } from 'react';

export default function ChatInput({ onSend, disabled }) {
  const [value, setValue] = useState('');

  const submit = (e) => {
    e.preventDefault();
    if (disabled) return;
    const q = value.trim();
    if (!q) return;
    onSend(q);
    setValue('');
  };

  const onKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      submit(e);
    }
  };

  return (
    <form className="chat-input" onSubmit={submit}>
      <textarea
        className="chat-textarea"
        placeholder="Hỏi về kho, lô hàng, QC..."
        rows={2}
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onKeyDown={onKeyDown}
        disabled={disabled}
      />
      <button type="submit" className="btn btn-primary" disabled={disabled || !value.trim()}>
        {disabled ? '...' : 'Gửi'}
      </button>
    </form>
  );
}
