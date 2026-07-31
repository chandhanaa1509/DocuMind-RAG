import { useEffect, useState } from 'react'
import Sidebar from './components/Sidebar.jsx'
import ChatWindow from './components/ChatWindow.jsx'
import { listDocuments, uploadFile, uploadText, deleteDocument, askQuestion } from './api.js'

export default function App() {
  const [documents, setDocuments] = useState([])
  const [messages, setMessages] = useState([])
  const [busy, setBusy] = useState(false)
  const [asking, setAsking] = useState(false)
  const [error, setError] = useState(null)

  const refreshDocuments = async () => {
    try {
      const docs = await listDocuments()
      setDocuments(docs)
    } catch (e) {
      setError('Could not reach the backend on http://localhost:8080 — is it running?')
    }
  }

  useEffect(() => {
    refreshDocuments()
  }, [])

  const handleUploadFile = async (file) => {
    setBusy(true)
    setError(null)
    try {
      await uploadFile(file)
      await refreshDocuments()
    } catch (e) {
      setError('Upload failed. Check the backend logs.')
    } finally {
      setBusy(false)
    }
  }

  const handleUploadText = async (name, content) => {
    setBusy(true)
    setError(null)
    try {
      await uploadText(name, content)
      await refreshDocuments()
    } catch (e) {
      setError('Upload failed. Check the backend logs.')
    } finally {
      setBusy(false)
    }
  }

  const handleDelete = async (sourceName) => {
    try {
      await deleteDocument(sourceName)
      await refreshDocuments()
    } catch (e) {
      setError('Delete failed.')
    }
  }

  const handleAsk = async (question) => {
    setMessages((prev) => [...prev, { role: 'user', content: question }])
    setAsking(true)
    setError(null)
    try {
      const res = await askQuestion(question)
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: res.answer, sources: res.sources },
      ])
    } catch (e) {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: 'Something went wrong reaching the backend or Groq. Check that both are running and the API key is set.' },
      ])
    } finally {
      setAsking(false)
    }
  }

  return (
    <div className="app">
      <Sidebar
        documents={documents}
        onUploadFile={handleUploadFile}
        onUploadText={handleUploadText}
        onDelete={handleDelete}
        loading={busy}
      />
      <ChatWindow
        messages={messages}
        onAsk={handleAsk}
        asking={asking}
        hasDocuments={documents.length > 0}
      />
      {error && <div className="toast">{error}</div>}
    </div>
  )
}
