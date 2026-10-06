import type { H3Event } from 'h3'

/**
 * IP thật của khách.
 *
 * Apache đứng trước dùng mod_remoteip để khôi phục IP thật khi đi qua Nova WAF.
 * Node chạy SAU Apache/Nginx trên cùng máy, nên kết nối luôn đến từ 127.0.0.1 — lúc đó lấy phần tử CUỐI của X-Forwarded-For, tức địa chỉ
 * mà chính proxy của mình ghi vào (proxy đã khôi phục IP thật từ trước).
 *
 * KHÔNG tin X-Forwarded-For khi kết nối đến thẳng từ Internet: tin nhầm = kẻ tấn
 * công tự khai IP mới cho mỗi lần gửi để thoát bộ đếm chặn spam / dò mật khẩu.
 */
export function ipKhach(event: H3Event): string {
  // remoteAddress rỗng khi `nuxt dev` chuyển request vào worker qua IPC (và tự ghi
  // X-Forwarded-For). Chỉ tin trường hợp đó khi DEV: lúc chạy thật, socket đã bị
  // đóng giữa chừng cũng cho remoteAddress rỗng, và tin XFF lúc ấy = cho kẻ tấn
  // công tự khai IP.
  const remote = event.node.req.socket?.remoteAddress ?? ''
  const { trustedProxies } = useKpahConfig()
  if ((import.meta.dev && remote === '') || trustedProxies.includes(remote)) {
    const xff = event.node.req.headers['x-forwarded-for']
    const chuoi = Array.isArray(xff) ? xff.join(',') : (xff ?? '')
    const ds = chuoi.split(',').map(s => s.trim()).filter(Boolean)
    const cuoi = ds[ds.length - 1]
    if (cuoi) return cuoi.slice(0, 45)
    const real = event.node.req.headers['x-real-ip']
    if (typeof real === 'string' && real.trim() !== '') return real.trim().slice(0, 45)
  }
  return remote.replace(/^::ffff:/, '').slice(0, 45)
}
