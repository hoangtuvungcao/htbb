/**
 * Markdown -> HTML AN TOÀN (chống XSS) cho nội dung admin nhập: tin tức, trang cẩm nang, FAQ.
 *
 * Dùng `marked` nhưng:
 *   - HTML thô trong Markdown bị ESCAPE (không render thẻ <script>, <iframe>, onerror=...).
 *   - Link/ảnh chỉ nhận http(s)://, đường dẫn trong site (/...), #anchor, mailto: — còn lại bỏ.
 *   - Link ra ngoài thêm rel="noopener nofollow" target="_blank".
 *   - Tiêu đề ## có id (để mục lục / tìm kiếm nhảy tới).
 * Dùng chung server (SSR) và trình duyệt (xem trước trong trình soạn thảo), cho cùng kết quả.
 */
import { Marked } from 'marked'

const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;')

function urlAnToan(u: string | null | undefined): string {
  const s = (u ?? '').trim()
  if (/^(https?:\/\/|\/(?!\/)|#|mailto:)/i.test(s)) return s
  return ''
}

export function slugTieuDe(s: string): string {
  return s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D')
    .toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 80)
}

const md = new Marked({
  gfm: true,
  breaks: true,
  renderer: {
    html({ text }) {
      return esc(text)
    },
    link({ href, title, tokens }) {
      const u = urlAnToan(href)
      const noi = this.parser.parseInline(tokens)
      if (!u) return noi
      const ngoai = /^https?:\/\//i.test(u)
      return `<a href="${esc(u)}"${title ? ` title="${esc(title)}"` : ''}${ngoai ? ' target="_blank" rel="noopener nofollow"' : ''}>${noi}</a>`
    },
    image({ href, title, text }) {
      const u = urlAnToan(href)
      if (!u) return esc(text ?? '')
      return `<img src="${esc(u)}" alt="${esc(text ?? '')}"${title ? ` title="${esc(title)}"` : ''} loading="lazy">`
    },
    heading({ tokens, depth }) {
      const noi = this.parser.parseInline(tokens)
      const id = slugTieuDe(noi.replace(/<[^>]+>/g, ''))
      const d = Math.min(6, Math.max(2, depth))
      return `<h${d} id="${id}">${noi}</h${d}>\n`
    },
  },
})

export function markdownSangHtml(src: string | null | undefined): string {
  return md.parse(String(src ?? ''), { async: false }) as string
}

/** Các tiêu đề ## trong Markdown -> mục lục [{id, ten}]. */
export function mucLucMarkdown(src: string | null | undefined): { id: string, ten: string }[] {
  return [...String(src ?? '').matchAll(/^##\s+(.+?)\s*#*\s*$/gm)].map(m => ({ id: slugTieuDe(m[1]!.replace(/[*_`]/g, '')), ten: m[1]!.replace(/[*_`]/g, '') }))
}

/** Bỏ ký hiệu Markdown để làm đoạn tóm tắt / chỉ mục tìm kiếm. */
export function markdownSangChu(src: string | null | undefined, toiDa = 400): string {
  return String(src ?? '').replace(/!\[[^\]]*\]\([^)]*\)/g, '').replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_`~|-]+/g, ' ').replace(/\s+/g, ' ').trim().slice(0, toiDa)
}
