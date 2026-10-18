/**
 * Header bảo mật cho MỌI phản hồi (bổ sung cho routeRules trong nuxt.config.ts, nơi đã có
 * X-Frame-Options / X-Content-Type-Options / Referrer-Policy).
 *
 * VÌ SAO CẦN CSP: token đăng nhập nằm trong localStorage/sessionStorage (không dùng cookie, xem
 * server/utils/token.ts), nên một lỗ XSS bất kỳ là lộ token. CSP chặn script/kết nối ra host lạ.
 *
 * CSP thực dụng cho Nuxt SSR:
 *   - script-src có 'unsafe-inline': Nuxt chèn script inline (window.__NUXT__ config, script
 *     color-mode chống nháy màu). Hash/nonce thì đổi theo mỗi bản build -> để sau.
 *   - style-src 'unsafe-inline': Vue/Nuxt UI/motion-v dùng style inline.
 *   - img-src https: : ảnh trong tin tức (Markdown) có thể là link https ngoài; QR VietQR (nạp).
 *   - connect-src 'self': trình duyệt chỉ gọi /api của chính site (trạng thái ngoài do server gọi).
 *   - frame-ancestors 'self': thay/hỗ trợ X-Frame-Options.
 *
 * Biến môi trường:
 *   HTBB_CSP  = enforce (mặc định khi chạy thật) | report-only | off
 *   HTBB_HSTS = 1 bắt buộc gửi HSTS | 0 không bao giờ | (trống) tự động: chỉ khi chạy thật VÀ
 *               request đến qua HTTPS (X-Forwarded-Proto: https từ proxy tin cậy — Apache trong
 *               deploy/apache-htbb-web.conf đặt header này khi env HTTPS).
 *   HTBB_SITE_URL chưa phải tên miền thật (trống / localhost / IP) -> mọi phản hồi có
 *               X-Robots-Tag: noindex, nofollow (cùng với robots.txt chặn hết và thẻ meta robots).
 * Header x-powered-by do Nuxt tự gắn bị gỡ ở server/plugins/bo-x-powered-by.ts.
 * Khi `nuxt dev` không gửi CSP (Vite HMR cần websocket + eval) và không gửi HSTS.
 */
const CSP = [
  "default-src 'self'",
  "script-src 'self' 'unsafe-inline'",
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data: blob: https:",
  "font-src 'self' data:",
  "connect-src 'self'",
  "media-src 'self'",
  "worker-src 'self' blob:",
  "frame-src 'self'",
  "object-src 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "frame-ancestors 'self'",
].join('; ')

const PERMISSIONS = 'camera=(), microphone=(), geolocation=(), payment=(), usb=(), serial=(), bluetooth=(), magnetometer=(), gyroscope=(), accelerometer=()'

function cheDoCsp(): 'enforce' | 'report-only' | 'off' {
  const v = ((process.env.HTBB_CSP ?? process.env.KPAH_CSP) ?? '').trim().toLowerCase() // TUONG-THICH-TEN-CU
  if (v === 'off' || v === '0' || v === 'false') return 'off'
  if (v === 'report-only' || v === 'report') return 'report-only'
  return 'enforce'
}

function quaHttps(event: import('h3').H3Event): boolean {
  const remote = event.node.req.socket?.remoteAddress ?? ''
  if (useHtbbConfig().trustedProxies.includes(remote)) {
    const p = String(event.node.req.headers['x-forwarded-proto'] ?? '').split(',')[0]!.trim().toLowerCase()
    if (p === 'https') return true
  }
  return Boolean((event.node.req.socket as { encrypted?: boolean } | undefined)?.encrypted)
}

export default defineEventHandler((event) => {
  setResponseHeader(event, 'Permissions-Policy', PERMISSIONS)
  setResponseHeader(event, 'Cross-Origin-Opener-Policy', 'same-origin')
  setResponseHeader(event, 'X-Content-Type-Options', 'nosniff')
  if (!useHtbbConfig().site.choIndex) setResponseHeader(event, 'X-Robots-Tag', 'noindex, nofollow')

  if (import.meta.dev) return

  // Route nào đã tự đặt CSP chặt hơn (vd. /uploads/*) sẽ ghi đè sau, vì handler chạy sau middleware.
  const che = cheDoCsp()
  if (che === 'enforce') setResponseHeader(event, 'Content-Security-Policy', CSP)
  else if (che === 'report-only') setResponseHeader(event, 'Content-Security-Policy-Report-Only', CSP)

  const hsts = ((process.env.HTBB_HSTS ?? process.env.KPAH_HSTS) ?? '').trim() // TUONG-THICH-TEN-CU
  if (hsts === '1' || (hsts === '' && quaHttps(event))) {
    // 180 ngày, KHÔNG includeSubDomains/preload: subdomain khác có thể chưa có HTTPS.
    setResponseHeader(event, 'Strict-Transport-Security', 'max-age=15552000')
  }
})
