import RetrievalTrace from './RetrievalTrace.jsx'

export default function MessageBubble({ role, content, sources, scanning }) {
  const isUser = role === 'user'
  return (
    <div className={`msg-row ${isUser ? 'msg-row-user' : 'msg-row-assistant'}`}>
      <div className="msg-avatar">{isUser ? 'you' : '◈'}</div>
      <div className="msg-col">
        <div className={`msg-bubble ${isUser ? 'msg-bubble-user' : 'msg-bubble-assistant'}`}>
          {content}
        </div>
        {!isUser && (sources || scanning) && (
          <RetrievalTrace sources={sources} scanning={scanning} />
        )}
      </div>
    </div>
  )
}
