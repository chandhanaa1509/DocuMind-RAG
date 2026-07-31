import { useEffect, useRef, useState } from 'react'
import MessageBubble from './MessageBubble.jsx'

export default function ChatWindow({ messages, onAsk, asking, hasDocuments }) {
  const [input, setInput] = useState('')
  const scrollRef = useRef(null)

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages, asking])

  const submit = () => {
    const q = input.trim()
    if (!q || asking) return
    onAsk(q)
    setInput('')
  }

  const onKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      submit()
    }
  }

  return (
    <main className="chat">
      <div className="chat-scroll" ref={scrollRef}>
        {messages.length === 0 && (
          <div className="chat-empty">
            <div className="chat-empty-mark">◈</div>
            <h1>Ask your documents anything</h1>
            <p>Upload a file on the left, then ask a question. Every answer shows exactly which chunks it was grounded in.</p>
          </div>
        )}

        {messages.map((m, i) => (
          <MessageBubble key={i} {...m} />
        ))}

        {asking && (
          <MessageBubble role="assistant" content={<TypingDots />} scanning />
        )}
      </div>

      <div className="composer">
        <textarea
          className="composer-input"
          placeholder={hasDocuments ? 'Ask a question about your documents…' : 'Upload a document first, then ask a question…'}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={onKeyDown}
          rows={1}
        />
        <button className="composer-send" onClick={submit} disabled={asking || !input.trim()}>
          ↑
        </button>
      </div>
    </main>
  )
}

function TypingDots() {
  return (
    <span className="typing-dots">
      <span /><span /><span />
    </span>
  )
}
