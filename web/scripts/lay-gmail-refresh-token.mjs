#!/usr/bin/env node
/**
 * ============================================================================
 *  LẤY REFRESH TOKEN GMAIL (CHẠY MỘT LẦN, TRÊN MÁY CÓ TRÌNH DUYỆT)
 * ============================================================================
 *
 *      node scripts/lay-gmail-refresh-token.mjs <client_id> <client_secret> [cổng]
 *
 *  hoặc đặt KPAH_GMAIL_CLIENT_ID / KPAH_GMAIL_CLIENT_SECRET trong môi trường / .env
 *  rồi chạy `node scripts/lay-gmail-refresh-token.mjs`. Cổng mặc định 8765.
 *
 *  Các bước script làm:
 *    1. Mở một máy chủ HTTP nhỏ ở http://127.0.0.1:<cổng>/ (chỉ nghe trên máy này).
 *    2. In ra đường link xin quyền của Google (quyền DUY NHẤT: gmail.send — chỉ gửi
 *       thư, không đọc được hộp thư). Bạn mở link, đăng nhập ĐÚNG tài khoản Gmail sẽ
 *       dùng để gửi thư, bấm Cho phép (gặp "Google chưa xác minh ứng dụng này" thì bấm
 *       Nâng cao -> Đi tới ... — ứng dụng của chính bạn nên an toàn).
 *    3. Google chuyển trình duyệt về http://127.0.0.1:<cổng>/?code=... ; script nhận
 *       mã đó, đổi lấy token ở https://oauth2.googleapis.com/token.
 *    4. In ra KPAH_GMAIL_REFRESH_TOKEN. Chép vào GitHub Secrets (VPS cũ) và vào
 *       /etc/kpah-web.env trên VPS mới 180.93.127.26, rồi khởi động lại kpah-web.
 *
 *  OAuth client loại "Desktop app": Google nhận mọi cổng 127.0.0.1, không cần khai.
 *  Loại "Web application": phải khai đúng redirect URI http://127.0.0.1:<cổng>/ .
 *
 *  Refresh token là BÍ MẬT (ai có nó + client secret là gửi thư được dưới tên bạn):
 *  không dán vào chat, không commit. Xem README mục "Gửi email (Gmail API)".
 *
 *  Không cần cài thêm gì: chỉ dùng thư viện có sẵn của Node (>= 20).
 *  Chữ in ra không dấu để đọc được trên mọi terminal.
 */
import { createServer } from 'node:http'
import { existsSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { randomBytes, createHash } from 'node:crypto'

const GOC = join(dirname(fileURLToPath(import.meta.url)), '..')
// Biến đã có trong môi trường không bị .env ghi đè.
if (existsSync(join(GOC, '.env'))) {
  try { process.loadEnvFile(join(GOC, '.env')) } catch { /* bỏ qua */ }
}

const [argId, argSecret, argCong] = process.argv.slice(2)
const clientId = (argId || process.env.KPAH_GMAIL_CLIENT_ID || '').trim()
const clientSecret = (argSecret || process.env.KPAH_GMAIL_CLIENT_SECRET || '').trim()
const cong = Number(argCong || 8765)

if (!clientId || !clientSecret) {
  console.error('Thieu client id / client secret.')
  console.error('Cach dung: node scripts/lay-gmail-refresh-token.mjs <client_id> <client_secret> [cong]')
  console.error('(hoac dat KPAH_GMAIL_CLIENT_ID va KPAH_GMAIL_CLIENT_SECRET trong moi truong / .env)')
  process.exit(1)
}
if (!Number.isInteger(cong) || cong < 1024 || cong > 65535) {
  console.error('Cong khong hop le (1024-65535).')
  process.exit(1)
}

const REDIRECT = `http://127.0.0.1:${cong}/`
const SCOPE = 'https://www.googleapis.com/auth/gmail.send'
// state chống giả mạo lời gọi về; PKCE (S256) để mã nhận được không dùng lại được ở nơi khác.
const state = randomBytes(16).toString('hex')
const verifier = randomBytes(48).toString('base64url')
const challenge = createHash('sha256').update(verifier).digest('base64url')

const linkXinQuyen = 'https://accounts.google.com/o/oauth2/v2/auth?' + new URLSearchParams({
  client_id: clientId,
  redirect_uri: REDIRECT,
  response_type: 'code',
  scope: SCOPE,
  access_type: 'offline', // để Google trả refresh token
  prompt: 'consent', // luôn hỏi lại -> luôn có refresh token mới
  state,
  code_challenge: challenge,
  code_challenge_method: 'S256',
}).toString()

/** Đổi mã (code) lấy token. */
async function doiMa(code) {
  const res = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'authorization_code',
      code,
      client_id: clientId,
      client_secret: clientSecret,
      redirect_uri: REDIRECT,
      code_verifier: verifier,
    }),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(`Google tra loi ${res.status}: ${data.error ?? ''} ${data.error_description ?? ''}`)
  return data
}

function traTrang(res, ma, noiDung) {
  res.writeHead(ma, { 'Content-Type': 'text/html; charset=utf-8' })
  res.end(`<!doctype html><meta charset="utf-8"><title>KPAH - Gmail</title><body style="font-family:sans-serif;padding:40px"><h2>${noiDung}</h2><p>Quay lại cửa sổ dòng lệnh để xem kết quả. Có thể đóng tab này.</p></body>`)
}

const server = createServer(async (req, res) => {
  const url = new URL(req.url ?? '/', REDIRECT)
  if (url.pathname !== '/') {
    res.writeHead(404).end()
    return
  }
  const loi = url.searchParams.get('error')
  const code = url.searchParams.get('code')
  if (loi) {
    traTrang(res, 400, 'Google báo lỗi: ' + loi.replace(/[^\w.-]/g, ''))
    console.error('\n[LOI] Google bao loi:', loi)
    server.close()
    process.exitCode = 1
    return
  }
  if (!code) {
    traTrang(res, 400, 'Thiếu mã xác nhận.')
    return
  }
  if (url.searchParams.get('state') !== state) {
    traTrang(res, 400, 'Sai state — hãy mở lại đúng link script vừa in ra.')
    return
  }
  try {
    const tk = await doiMa(code)
    if (!tk.refresh_token) {
      traTrang(res, 500, 'Google không trả refresh token.')
      console.error('\n[LOI] Google khong tra refresh_token. Vao https://myaccount.google.com/permissions go quyen cua ung dung roi chay lai.')
      process.exitCode = 1
    } else {
      traTrang(res, 200, 'Xong! Đã lấy được refresh token.')
      if (tk.scope && !String(tk.scope).includes('gmail.send')) {
        console.warn('\n[CANH BAO] Token khong co quyen gmail.send (scope:', tk.scope, ')')
      }
      console.log('\n================ KET QUA ================')
      console.log('KPAH_GMAIL_CLIENT_ID=' + clientId)
      console.log('KPAH_GMAIL_CLIENT_SECRET=' + clientSecret)
      console.log('KPAH_GMAIL_REFRESH_TOKEN=' + tk.refresh_token)
      console.log('KPAH_GMAIL_SENDER=<dia chi Gmail ban vua dang nhap>')
      console.log('=========================================')
      console.log('Chep 4 dong tren vao GitHub Secrets (VPS cu) va /etc/kpah-web.env (VPS moi),')
      console.log('roi: systemctl restart kpah-web. KHONG dan cac gia tri nay vao chat / commit.')
    }
  } catch (e) {
    traTrang(res, 500, 'Đổi mã lấy token thất bại.')
    console.error('\n[LOI]', e instanceof Error ? e.message : e)
    process.exitCode = 1
  }
  server.close()
})

server.on('error', (e) => {
  console.error('[LOI] Khong mo duoc cong', cong, '-', e.message, '(thu cong khac: them so cong vao cuoi lenh)')
  process.exit(1)
})

// Chỉ nghe trên 127.0.0.1: máy khác trong mạng không gọi vào được.
server.listen(cong, '127.0.0.1', () => {
  console.log('Dang cho Google tra ve tai', REDIRECT)
  console.log('\nMo link sau trong trinh duyet (dang nhap DUNG tai khoan Gmail se dung de gui thu):\n')
  console.log(linkXinQuyen)
  console.log('\n(Ctrl+C de huy)')
})
