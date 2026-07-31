import { useRef, useState } from 'react'

export default function Sidebar({ documents, onUploadFile, onUploadText, onDelete, loading }) {
  const fileInputRef = useRef(null)
  const [pasteOpen, setPasteOpen] = useState(false)
  const [pasteName, setPasteName] = useState('')
  const [pasteContent, setPasteContent] = useState('')

  const handleFileChange = (e) => {
    const file = e.target.files?.[0]
    if (file) onUploadFile(file)
    e.target.value = ''
  }

  const submitPaste = () => {
    if (!pasteName.trim() || !pasteContent.trim()) return
    onUploadText(pasteName.trim(), pasteContent.trim())
    setPasteName('')
    setPasteContent('')
    setPasteOpen(false)
  }

  return (
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-mark">◈</span>
        <div>
          <div className="brand-title">DocuMind</div>
          <div className="brand-sub">retrieval-augmented chat</div>
        </div>
      </div>

      <div className="sidebar-section">
        <div className="sidebar-label">ingest</div>
        <button className="btn-primary" onClick={() => fileInputRef.current?.click()} disabled={loading}>
          + Upload .txt / .md file
        </button>
        <input
          ref={fileInputRef}
          type="file"
          accept=".txt,.md"
          hidden
          onChange={handleFileChange}
        />
        <button className="btn-ghost" onClick={() => setPasteOpen((v) => !v)}>
          {pasteOpen ? 'Cancel' : '+ Paste raw text instead'}
        </button>

        {pasteOpen && (
          <div className="paste-box">
            <input
              className="paste-input"
              placeholder="Source name, e.g. notes.txt"
              value={pasteName}
              onChange={(e) => setPasteName(e.target.value)}
            />
            <textarea
              className="paste-textarea"
              placeholder="Paste document content here…"
              value={pasteContent}
              onChange={(e) => setPasteContent(e.target.value)}
              rows={6}
            />
            <button className="btn-primary" onClick={submitPaste} disabled={loading}>
              Ingest text
            </button>
          </div>
        )}
      </div>

      <div className="sidebar-section grow">
        <div className="sidebar-label">library · {documents.length}</div>
        <div className="doc-list">
          {documents.length === 0 && (
            <div className="doc-empty">No documents yet. Upload one to start retrieval.</div>
          )}
          {documents.map((doc) => (
            <div className="doc-card" key={doc.sourceName}>
              <div className="doc-card-main">
                <div className="doc-name" title={doc.sourceName}>{doc.sourceName}</div>
                <div className="doc-meta">{doc.chunks} chunk{doc.chunks === 1 ? '' : 's'}</div>
              </div>
              <button className="doc-delete" title="Remove document" onClick={() => onDelete(doc.sourceName)}>
                ×
              </button>
            </div>
          ))}
        </div>
      </div>

      <div className="sidebar-footer">
        <div className="pipeline-note">
          <span className="dot" /> retrieval: TF cosine similarity
        </div>
        <div className="pipeline-note">
          <span className="dot dot-amber" /> generation: Groq · Llama 3.1
        </div>
      </div>
    </aside>
  )
}
