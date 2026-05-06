import { marked } from 'marked'

marked.setOptions({
  breaks: true,
  gfm: true,
})

export function renderMarkdown(content: string): string {
  return marked.parse(content) as string
}
