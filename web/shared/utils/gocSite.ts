/**
 * Địa chỉ gốc của website (canonical, og:url, og:image, sitemap, robots.txt, JSON-LD).
 *
 * NGUỒN DUY NHẤT: biến môi trường HTBB_SITE_URL (server/utils/config.ts đọc, gửi xuống trình
 * duyệt qua /api/site -> useSite().siteUrl). KHÔNG lấy theo Host của request (giả được Host /
 * X-Forwarded-Host -> canonical/sitemap trỏ sang tên miền lạ) và KHÔNG ghi cứng tên miền nào
 * trong mã nguồn.
 *
 * Chưa có tên miền thật (để trống, localhost, 127.x, địa chỉ IP, tên một chữ không có dấu chấm,
 * .local/.localhost/.test/.invalid) -> CHẶN lập chỉ mục: robots.txt "Disallow: /", mọi trang có
 * <meta name="robots" content="noindex, nofollow"> và header X-Robots-Tag.
 */

export const GOC_SITE_MAC_DINH = 'http://localhost:3000'

export interface GocSite {
  /** Origin không có dấu / ở cuối, vd. "https://vi-du.vn" hoặc "http://localhost:3000". */
  goc: string
  /** true = tên miền thật -> cho máy tìm kiếm lập chỉ mục. */
  choIndex: boolean
}

/** Host không phải tên miền công khai (máy dev, IP trần, tên nội bộ). */
export function laHostNoiBo(hostname: string): boolean {
  const h = hostname.toLowerCase().replace(/^\[|\]$/g, '').replace(/\.$/, '')
  if (h === '' || h === 'localhost' || h.endsWith('.localhost')) return true
  if (/^\d{1,3}(\.\d{1,3}){3}$/.test(h)) return true // IPv4 bất kỳ (chưa có tên miền)
  if (h.includes(':')) return true // IPv6
  if (!h.includes('.')) return true // tên một chữ trong mạng nội bộ
  return /\.(local|test|invalid|internal|lan|home\.arpa)$/.test(h)
}

/** Nắn giá trị HTBB_SITE_URL: chỉ http(s), bỏ đường dẫn/query; sai hoặc trống -> mặc định localhost. */
export function chuanHoaGocSite(raw: unknown): GocSite {
  const s = typeof raw === 'string' ? raw.trim() : ''
  let u: URL | null = null
  if (s !== '') {
    try { u = new URL(/^[a-z][a-z0-9+.-]*:\/\//i.test(s) ? s : `https://${s}`) }
    catch { u = null }
  }
  if (!u || (u.protocol !== 'http:' && u.protocol !== 'https:') || u.username || u.password || !u.hostname) {
    return { goc: GOC_SITE_MAC_DINH, choIndex: false }
  }
  return { goc: u.origin, choIndex: !laHostNoiBo(u.hostname) }
}

/** Ghép gốc + đường dẫn trong site ("/news/abc"); URL tuyệt đối thì giữ nguyên. */
export function urlTuyetDoi(goc: string, duongDan: string): string {
  try { return new URL(duongDan || '/', `${goc}/`).href }
  catch { return `${goc}/` }
}
