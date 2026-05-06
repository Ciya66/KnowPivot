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

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() ?? ''

        let currentEvent = ''

        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            const dataStr = line.slice(5).trim()
            if (!dataStr) continue

            try {
              const data = JSON.parse(dataStr)

              switch (currentEvent) {
                case 'message':
                  callbacks.onMessage(data.delta)
                  break
                case 'references':
                  callbacks.onReferences(data.sources)
                  break
                case 'done':
                  callbacks.onDone(data as SSEDone)
                  break
                default:
                  // Unknown event, ignore
                  break
              }
            } catch {
              // Skip malformed JSON
            }
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
