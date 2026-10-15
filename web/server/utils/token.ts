/**
 * Token đăng nhập ký HMAC-SHA256 — không dùng session phía máy chủ.
 *
 * VÌ SAO KHÔNG DÙNG COOKIE: Nova WAF (proxy trước domain kpah-private.site) không
 * chuyển tiếp Cookie/Set-Cookie về origin, nên mọi thứ dựa vào cookie phiên đều
 * hỏng qua domain ("Phiên làm việc hết hạn" mọi lần). Token đi trong header
 * `Authorization: Bearer ...` do trình duyệt tự gửi bằng JavaScript, không phụ
 * thuộc cookie. Vì không có "chứng chỉ tự động gửi kèm" như cookie, CSRF cũng
 * không còn là mối đe doạ nên không cần token CSRF cho các form nữa.
 */
import { createHmac, timingSafeEqual } from 'node:crypto'

export type LoaiToken = 'user' | 'admin' | 'cho2fa' | 'naptien'

export interface TokenPayload {
  /** loại token */
  k: LoaiToken
  /** team_user.id */
  id: number
  /** team_user.username */
  u: string
  /** phát hành lúc (giây) */
  iat: number
  /** hết hạn lúc (giây) */
  exp: number
  /** dấu mật khẩu (dauMatKhau) lúc cấp — đổi / quên mật khẩu là token cũ hết hiệu lực */
  pw?: string
  /** admin: lúc đăng nhập (giây) — giữ nguyên qua các lần gia hạn trượt, để có hạn tuyệt đối */
  l?: number
}

const b64 = (buf: Buffer) => buf.toString('base64url')

function kyTen(du: string): string {
  return b64(createHmac('sha256', useKpahConfig().secret).update(du).digest())
}

export function nowSec(): number {
  return Math.floor(Date.now() / 1000)
}

/**
 * Dấu của hash mật khẩu (cột team_user.password) để nhét vào token — không lộ hash thật (HMAC với khoá bí mật).
 * Mật khẩu đổi thì dấu đổi -> mọi token cũ bị từ chối ở requireUser / requireAdmin.
 */
export function dauMatKhau(hashMatKhau: unknown): string {
  return b64(createHmac('sha256', useKpahConfig().secret).update(`pw|${String(hashMatKhau ?? '')}`).digest()).slice(0, 16)
}

export function taoToken(k: LoaiToken, id: number, u: string, hanGiay: number, them: { pw?: string, l?: number } = {}): string {
  const iat = nowSec()
  const payload: TokenPayload = { k, id, u, iat, exp: iat + hanGiay, ...them }
  const du = b64(Buffer.from(JSON.stringify(payload), 'utf8'))
  return `${du}.${kyTen(`${k}|${du}`)}`
}

export function docToken(token: string | null | undefined, k: LoaiToken): TokenPayload | null {
  if (!token || typeof token !== 'string' || token.length > 2048) return null
  const m = token.match(/^([A-Za-z0-9_-]+)\.([A-Za-z0-9_-]{43})$/)
  if (!m) return null
  const [, du, chuKy] = m as unknown as [string, string, string]
  const dung = Buffer.from(kyTen(`${k}|${du}`))
  const goi = Buffer.from(chuKy)
  if (dung.length !== goi.length || !timingSafeEqual(dung, goi)) return null
  let p: TokenPayload
  try {
    p = JSON.parse(Buffer.from(du, 'base64url').toString('utf8'))
  } catch {
    return null
  }
  if (p.k !== k || typeof p.id !== 'number' || typeof p.u !== 'string') return null
  if (typeof p.exp !== 'number' || p.exp < nowSec()) return null
  return p
}

/** Lấy token từ header Authorization: Bearer <token>. */
export function bearer(event: import('h3').H3Event): string | null {
  const h = getRequestHeader(event, 'authorization') ?? ''
  const m = h.match(/^Bearer\s+(\S+)$/i)
  return m ? m[1]! : null
}
