/**
 * GỬI EMAIL QUA GMAIL API (OAuth2, refresh token).
 *
 *   if (mailDaCauHinh()) await guiMail({ to, subject, text, html })
 *
 * Không dùng SMTP/thư viện ngoài: chỉ hai lời gọi HTTPS bằng fetch.
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
 * Cấu hình: KPAH_GMAIL_* (xem server/utils/config.ts và README mục "Gửi email (Gmail API)").
 */
import { randomBytes } from 'node:crypto'

const TOKEN_URL = 'https://oauth2.googleapis.com/token'
const SEND_URL = 'https://gmail.googleapis.com/gmail/v1/users/me/messages/send'
/** Chờ Google tối đa bao lâu cho mỗi lời gọi. */
const HET_GIO_MS = 15_000

export interface ThuGui {
  to: string
  subject: string
  text: string
  html: string
}

/** Đủ cấu hình để gửi mail chưa (đủ 4 giá trị KPAH_GMAIL_*). */
export function mailDaCauHinh(): boolean {
  const m = useHtbbConfig().mail
  return m.gmailClientId !== '' && m.gmailClientSecret !== ''
    && m.gmailRefreshToken !== '' && m.gmailSender !== ''
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
//  Dựng thư MIME
// ---------------------------------------------------------------------------

/** Bỏ CR/LF: giá trị đưa vào header mà có xuống dòng là chèn được header lạ. */
function motDong(s: string): string {
  return s.replace(/[\r\n]+/g, ' ').trim()
}

/**
 * Mã hoá RFC 2047 cho header có chữ không phải ASCII. Một "encoded-word" tối đa 75
 * ký tự, nên chia chuỗi thành nhiều đoạn (không cắt đôi ký tự UTF-8), nối bằng
 * xuống dòng + khoảng trắng (folding). Chuỗi ASCII thuần giữ nguyên.
 */
function maHoaHeader(s: string): string {
  // eslint-disable-next-line no-control-regex
  if (/^[\x20-\x7E]*$/.test(s)) return s
  const doan: string[] = []
  let hienTai = ''
  for (const kyTu of s) {
    // 45 byte UTF-8 -> 60 ký tự base64, cộng 12 ký tự "=?UTF-8?B?" "?=" = 72 < 75.
    if (Buffer.byteLength(hienTai + kyTu, 'utf8') > 45) {
      doan.push(hienTai)
      hienTai = ''
    }
    hienTai += kyTu
  }
  if (hienTai !== '') doan.push(hienTai)
  return doan.map(d => `=?UTF-8?B?${Buffer.from(d, 'utf8').toString('base64')}?=`).join('\r\n ')
}

/** Nội dung base64, ngắt dòng 76 ký tự theo chuẩn MIME. */
function base64Dong(s: string): string {
  return (Buffer.from(s, 'utf8').toString('base64').match(/.{1,76}/g) ?? []).join('\r\n')
}

function dungThu(thu: ThuGui): string {
  const m = useHtbbConfig().mail
  const ranh = `htbb_${randomBytes(12).toString('hex')}`
  const tenGui = motDong(m.fromName).replace(/["\\]/g, '')
  const tu = /^[\x20-\x7E]*$/.test(tenGui) ? `"${tenGui}"` : maHoaHeader(tenGui)
  const dong = [
    `From: ${tu} <${motDong(m.gmailSender)}>`,
    `To: ${motDong(thu.to)}`,
    `Subject: ${maHoaHeader(motDong(thu.subject))}`,
    `Date: ${new Date().toUTCString().replace('GMT', '+0000')}`,
    'MIME-Version: 1.0',
    `Content-Type: multipart/alternative; boundary="${ranh}"`,
    '',
    `--${ranh}`,
    'Content-Type: text/plain; charset="UTF-8"',
    'Content-Transfer-Encoding: base64',
    '',
    base64Dong(thu.text),
    `--${ranh}`,
    'Content-Type: text/html; charset="UTF-8"',
    'Content-Transfer-Encoding: base64',
    '',
    base64Dong(thu.html),
    `--${ranh}--`,
    '',
  ]
  return dong.join('\r\n')
}

// ---------------------------------------------------------------------------
//  Gửi
// ---------------------------------------------------------------------------

/**
 * Gửi một thư. Lỗi (chưa cấu hình, Google từ chối, mạng hỏng...) thì NÉM lỗi —
 * người gọi tự quyết định báo gì cho người dùng.
 */
export async function guiMail(thu: ThuGui): Promise<void> {
  if (!mailDaCauHinh()) throw new Error('mail chua cau hinh')
  const to = motDong(thu.to)
  if (!dangKyEmailHopLe(to)) throw new Error('dia chi nhan khong hop le')

  const raw = Buffer.from(dungThu({ ...thu, to }), 'utf8').toString('base64url')

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
