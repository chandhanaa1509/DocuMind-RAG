const BASE_URL = 'http://localhost:8080/api'

export async function listDocuments() {
  const res = await fetch(`${BASE_URL}/documents`)
  if (!res.ok) throw new Error('Failed to load documents')
  return res.json()
}

export async function uploadFile(file) {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch(`${BASE_URL}/documents/upload-file`, {
    method: 'POST',
    body: form,
  })
  if (!res.ok) throw new Error('Upload failed')
  return res.json()
}

export async function uploadText(sourceName, content) {
  const res = await fetch(`${BASE_URL}/documents/upload-text`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sourceName, content }),
  })
  if (!res.ok) throw new Error('Upload failed')
  return res.json()
}

export async function deleteDocument(sourceName) {
  const res = await fetch(`${BASE_URL}/documents/${encodeURIComponent(sourceName)}`, {
    method: 'DELETE',
  })
  if (!res.ok) throw new Error('Delete failed')
}

export async function askQuestion(question) {
  const res = await fetch(`${BASE_URL}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question }),
  })
  if (!res.ok) throw new Error('Chat request failed')
  return res.json()
}
