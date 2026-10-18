/**
 * Danh sách IP được vào API quản trị (HTBB_ADMIN_IP_CHO_PHEP) — phần thuần, có test.
 * Cú pháp: các mục cách dấu phẩy/khoảng trắng, mỗi mục là IPv4 ("203.0.113.5"), dải IPv4 CIDR
 * ("10.0.0.0/8") hoặc IPv6 viết đầy đủ/rút gọn so khớp nguyên văn ("::1"). Rỗng = không giới hạn.
 */
export interface LuatIp { v4: Array<{ mang: number, mat: number }>, v6: string[], loi: string[] }

function ipv4So(s: string): number | null {
  const p = s.split('.')
  if (p.length !== 4) return null
  let n = 0
  for (const x of p) {
    if (!/^\d{1,3}$/.test(x) || Number(x) > 255) return null
    n = n * 256 + Number(x)
  }
  return n >>> 0
}

export function docLuatIp(raw: string | null | undefined): LuatIp {
  const ra: LuatIp = { v4: [], v6: [], loi: [] }
  for (const t of String(raw ?? '').split(/[\s,;]+/).map(s => s.trim()).filter(Boolean)) {
    if (t.includes(':')) {
      if (/^[0-9a-f:.]+$/i.test(t)) ra.v6.push(t.toLowerCase())
      else ra.loi.push(t)
      continue
    }
    const [ip, bit] = t.split('/')
    const n = ipv4So(ip ?? '')
    const b = bit === undefined ? 32 : /^\d{1,2}$/.test(bit) && Number(bit) <= 32 ? Number(bit) : -1
    if (n === null || b < 0) { ra.loi.push(t); continue }
    const mat = b === 0 ? 0 : (0xFFFFFFFF << (32 - b)) >>> 0
    ra.v4.push({ mang: (n & mat) >>> 0, mat })
  }
  return ra
}

/** IP có được phép không. Không khai gì = cho tất cả; khai mà toàn mục sai = chặn hết (không mở toang vì gõ nhầm). */
export function ipDuocPhep(ip: string, luat: LuatIp): boolean {
  if (!luat.v4.length && !luat.v6.length) return luat.loi.length === 0
  const s = ip.trim().toLowerCase().replace(/^::ffff:(?=\d+\.)/, '')
  if (s.includes(':')) return luat.v6.includes(s)
  const n = ipv4So(s)
  if (n === null) return false
  return luat.v4.some(r => ((n & r.mat) >>> 0) === r.mang)
}
