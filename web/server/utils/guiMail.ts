/**
 * GỬI EMAIL — hai đường, chọn theo cấu hình (không thêm thư viện ngoài):
 *
 *   if (mailDaCauHinh()) await guiMail({ to, subject, text, html })
 *
 *   A. Gmail API (OAuth2, refresh token) — dùng khi đủ 4 biến HTBB_GMAIL_*. Ưu tiên.
 *   B. SMTP + mật khẩu ứng dụng Gmail — dùng khi có HTBB_SMTP_USER + HTBB_SMTP_PASS (và A chưa đủ).
 *      Máy khách SMTP tối giản ở mailThuan.ts (node:tls, cổng 465 TLS ngầm / 587 STARTTLS).
 *
 * Đường A chỉ là hai lời gọi HTTPS bằng fetch:
 *   1. Đổi refresh token lấy access token ở https://oauth2.googleapis.com/token.
 *      Access token sống ~1 giờ; nhớ trong bộ nhớ tới khoảng 60 giây trước khi hết hạn.
 *   2. Gửi thư ở https://gmail.googleapis.com/gmail/v1/users/me/messages/send, thân là
 *      { raw: base64url(thư dạng RFC 2822) }.
 *
 * Thư dựng bằng tay theo chuẩn MIME, chịu được tiếng Việt: tên người gửi và tiêu đề
 * mã hoá RFC 2047 (=?UTF-8?B?...?=), nội dung multipart/alternative gồm text/plain +
 * text/html, cả hai UTF-8 + base64.
 *
 * KHÔNG BAO GIỜ ghi token (refresh/access) hay client secret ra log — chỉ ghi mã lỗi
 * và trạng thái Google trả về.
 *
 * Cấu hình: HTBB_GMAIL_* hoặc HTBB_SMTP_* (xem server/utils/config.ts và docs/van-hanh.md).
 */
import { hostname } from 'node:os'
import { dungThuMime, guiSmtp, motDongMail, type ThuGui } from './mailThuan'

const TOKEN_URL = 'https://oauth2.googleapis.com/token'
const SEND_URL = 'https://gmail.googleapis.com/gmail/v1/users/me/messages/send'
/** Chờ Google tối đa bao lâu cho mỗi lời gọi. */
const HET_GIO_MS = 15_000

/** Đường gửi đang dùng: Gmail API (đủ 4 biến HTBB_GMAIL_*) trước, rồi SMTP (HTBB_SMTP_USER + PASS). */
export function duongGuiMail(): 'gmail-api' | 'smtp' | null {
  const m = useHtbbConfig().mail
  if (m.gmailClientId !== '' && m.gmailClientSecret !== '' && m.gmailRefreshToken !== '' && m.gmailSender !== '') {
    return 'gmail-api'
  }
  if (m.smtpUser !== '' && m.smtpPass !== '') return 'smtp'
  return null
}

/** Đủ cấu hình để gửi mail chưa (một trong hai đường). */
export function mailDaCauHinh(): boolean {
  return duongGuiMail() !== null
}

/** Thoát ký tự đặc biệt trước khi chèn giá trị người dùng nhập vào HTML. */
export function maHoaHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// ---------------------------------------------------------------------------
//  Access token (nhớ trong bộ nhớ tiến trình)
// ---------------------------------------------------------------------------

let tokenNho: { token: string, hetHanLuc: number } | null = null
/** Nhiều request cùng lúc chỉ đổi token một lần. */
let dangLayToken: Promise<string> | null = null

async function layAccessToken(): Promise<string> {
  if (tokenNho && Date.now() < tokenNho.hetHanLuc) return tokenNho.token
  if (!dangLayToken) {
    dangLayToken = doiRefreshToken().finally(() => {
      dangLayToken = null
    })
  }
  return dangLayToken
}

async function doiRefreshToken(): Promise<string> {
  const m = useHtbbConfig().mail
  const res = await fetch(TOKEN_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'refresh_token',
      client_id: m.gmailClientId,
      client_secret: m.gmailClientSecret,
      refresh_token: m.gmailRefreshToken,
    }),
    signal: AbortSignal.timeout(HET_GIO_MS),
  })
  const data = await res.json().catch(() => ({})) as Record<string, any>
  if (!res.ok || typeof data.access_token !== 'string') {
    // invalid_grant = refresh token hết hạn/bị thu hồi (ứng dụng còn ở chế độ "Testing"
    // thì token chỉ sống 7 ngày) -> chạy lại scripts/lay-gmail-refresh-token.mjs.
    console.error('[mail] Không lấy được access token Gmail:', res.status, data.error ?? '', data.error_description ?? '')
    throw new Error(`gmail token ${res.status} ${data.error ?? ''}`.trim())
  }
  const songGiay = Number(data.expires_in) > 0 ? Number(data.expires_in) : 3600
  tokenNho = {
    token: data.access_token,
    // Bỏ trước 60 giây để không dùng token sắp hết hạn giữa chừng.
    hetHanLuc: Date.now() + Math.max(0, songGiay - 60) * 1000,
  }
  return data.access_token
}

// ---------------------------------------------------------------------------
//  Gửi
// ---------------------------------------------------------------------------

/**
 * Gửi một thư. Lỗi (chưa cấu hình, Google từ chối, mạng hỏng...) thì NÉM lỗi —
 * người gọi tự quyết định báo gì cho người dùng.
 */
export async function guiMail(thu: ThuGui): Promise<void> {
  const duong = duongGuiMail()
  if (duong === null) throw new Error('mail chua cau hinh')
  const to = motDongMail(thu.to)
  if (!dangKyEmailHopLe(to)) throw new Error('dia chi nhan khong hop le')
  const m = useHtbbConfig().mail

  if (duong === 'smtp') {
    const from = m.smtpFrom
    try {
      await guiSmtp(
        { host: m.smtpHost, port: m.smtpPort, user: m.smtpUser, pass: m.smtpPass, tenMay: hostname() },
        { from, to: [to], thu: dungThuMime({ ...thu, to }, m.fromName, from) },
      )
    } catch (e: any) {
      // Thông báo lỗi của guiSmtp không bao giờ chứa mật khẩu / chuỗi AUTH. 535 = sai mật khẩu ứng dụng
      // (hoặc tài khoản chưa bật Xác minh 2 bước) -> tạo lại ở https://myaccount.google.com/apppasswords.
      console.error('[mail] SMTP gửi thư thất bại:', e?.message ?? String(e))
      throw e
    }
    return
  }

  const raw = Buffer.from(dungThuMime({ ...thu, to }, m.fromName, m.gmailSender), 'utf8').toString('base64url')

  // Thử tối đa 2 lần: lần đầu bị 401 (access token bị thu hồi trước hạn) thì bỏ
  // token đang nhớ, lấy token mới rồi gửi lại.
  for (let lan = 1; lan <= 2; lan++) {
    const token = await layAccessToken()
    const res = await fetch(SEND_URL, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({ raw }),
      signal: AbortSignal.timeout(HET_GIO_MS),
    })
    if (res.ok) return
    const data = await res.json().catch(() => ({})) as Record<string, any>
    if (res.status === 401 && lan === 1) {
      tokenNho = null
      continue
    }
    console.error('[mail] Gmail API từ chối gửi thư:', res.status, data?.error?.code ?? '', data?.error?.status ?? '')
    throw new Error(`gmail send ${res.status} ${data?.error?.status ?? ''}`.trim())
  }
}
