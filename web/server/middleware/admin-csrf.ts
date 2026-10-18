/**
 * Chống CSRF cho API quản trị. Token admin nằm trong header Authorization (không phải cookie),
 * nên trang lạ không tự gửi kèm được — đó là lớp bảo vệ chính. Thêm hai chốt:
 *   - Có header Origin thì phải cùng host với request.
 *   - Thân request ghi dữ liệu phải là JSON hoặc multipart (chặn form HTML tự gửi).
 */
export default defineEventHandler((event) => {
  if (!event.path.startsWith('/api/admin/') || event.method === 'GET' || event.method === 'HEAD') return
  const origin = getHeader(event, 'origin')
  if (origin) {
    let host = ''
    try { host = new URL(origin).host } catch { host = '?' }
    const ht = getRequestURL(event, { xForwardedHost: true }).host
    if (host !== ht) throw apiLoi(403, 'Yêu cầu bị chặn (khác nguồn).')
  }
  const ct = (getHeader(event, 'content-type') ?? '').toLowerCase()
  const coThan = Number(getHeader(event, 'content-length') ?? 0) > 0 || !!getHeader(event, 'transfer-encoding')
  // Riêng tải tệp game (lớn, ghi theo luồng): nhận application/octet-stream. Vẫn cần token ở header
  // Authorization — trang lạ không gửi được header đó nếu không qua CORS (web không bật CORS).
  const laTaiTepGame = event.path.split('?')[0] === '/api/admin/cms/tai-game/tai-len' && ct.startsWith('application/octet-stream')
  if (coThan && !laTaiTepGame && !ct.startsWith('application/json') && !ct.startsWith('multipart/form-data')) {
    throw apiLoi(415, 'Định dạng dữ liệu không được hỗ trợ.')
  }
})
