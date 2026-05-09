import type { SSEDone, SSEReferenceSource } from '@/types/api'
import { TOKEN_KEY } from '@/utils/request'

interface SSECallbacks {
  onMessage: (delta: string) => void
  onReferences: (sources: SSEReferenceSource[]) => void
  onDone: (data: SSEDone) => void
  onError: (error: Error) => void
}

/**
 * Send a message via SSE (Server-Sent Events) using fetch + ReadableStream.
 * Uses POST method with custom headers (EventSource only supports GET).
 */
export function sendSSEMessage(
  body: { conversationId: string; content: string },
  callbacks: SSECallbacks,
): AbortController {
  const controller = new AbortController()
  const token = localStorage.getItem(TOKEN_KEY)

  ;(async () => {
    try {
      const response = await fetch('/api/v1/chat/messages', {

        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(body),
        signal: controller.signal,
      })

      if (!response.ok) {
        const text = await response.text()
        throw new Error(`HTTP ${response.status}: ${text}`)
      }

      const reader = response.body?.getReader()
      if (!reader) throw new Error('No response body')

      const decoder = new TextDecoder()
      let buffer = ''
      let currentEvent = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() ?? ''

        for (const rawLine of lines) {
          const line = rawLine.trim()
          if (!line) {
            currentEvent = ''
            continue
          }

          // Standard SSE: "event: message"
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim()
            continue
          }

          // Standard SSE: "data: {...}"
          if (line.startsWith('data:')) {
            let dataStr = line.slice(5)

            // Backend wraps lines: "data:event: null" or "data:data: {...}"
            // Detect inner event:/data: and unwrap
            if (dataStr.startsWith('event:')) {
              currentEvent = dataStr.slice(6).trim()
              continue
            }
            if (dataStr.startsWith('data:')) {
              dataStr = dataStr.slice(5)
            }

            dataStr = dataStr.trim()
            if (!dataStr) continue

            try {
              const data = JSON.parse(dataStr)
              handleEvent(currentEvent, data, callbacks)
            } catch {
              // Skip malformed JSON
            }
            continue
          }
        }
      }
    } catch (err) {
      if ((err as Error).name !== 'AbortError') {
        callbacks.onError(err as Error)
      }
    }
  })()

  return controller
}

function handleEvent(
  event: string,
  data: Record<string, unknown>,
  callbacks: SSECallbacks,
) {
  switch (event) {
    case 'message':
      callbacks.onMessage(data.delta as string)
      break
    case 'references':
      callbacks.onReferences(data.sources as SSEReferenceSource[])
      break
    case 'done':
      callbacks.onDone(data as unknown as SSEDone)
      break
    default:
      break
  }
}
