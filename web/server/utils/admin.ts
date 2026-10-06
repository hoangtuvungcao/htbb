/**
 * Nền móng khu quản trị: đăng nhập, phân quyền, nhật ký thao tác.
 *
 * AI VÀO ĐƯỢC? Đăng nhập bằng chính TÀI KHOẢN GAME (account.team_user, so khớp
 * bằng PASSWORD() y hệt login server), và tên đó phải nằm trong KPAH_ADMIN_ACCOUNTS.
 * Danh sách rỗng = KHÔNG AI vào được (khoá mặc định, không phải mở mặc định) —
 * quên cấu hình thì trang tự đóng lại chứ không mở toang.
 *
 * Không dựa vào tob_char.isAdmin: trong game quyền GM theo TÊN NHÂN VẬT trong
 * sv.adminAccounts của server.ini, còn cột isAdmin bị ghi đè mỗi lần nạp nhân
 * vật nên không đáng tin. Danh sách riêng cho web còn tách bạch được: làm GM trong
 * game chưa chắc được quyền khoá tài khoản trên web.
 *
 * PHIÊN: token 'admin' sống KPAH_ADMIN_IDLE_TIMEOUT giây (mặc định 60 phút) kể từ
 * lần thao tác cuối. Mỗi request hợp lệ trả về token mới trong header
 * `X-Kpah-Token` (client tự thay) — tức "hết hạn khi quá lâu không thao tác".
 * Tên bị gỡ khỏi danh sách -> mất quyền ngay ở request kế tiếp.
 */
import type { H3Event } from 'h3'

export interface AdminUser {
  id: number
  username: string
}

export function adminAccounts(): string[] {
  return useKpahConfig().admin.accounts
}

export function laTaiKhoanAdmin(username: string): boolean {
  return adminAccounts().includes(username.trim().toLowerCase())
}

export function taoTokenAdmin(id: number, username: string): string {
  return taoToken('admin', id, username, useKpahConfig().admin.idleTimeout)
}

/** Admin đang đăng nhập, hoặc null. */
export function adminUser(event: H3Event): AdminUser | null {
  if (event.context.kpahAdmin !== undefined) return event.context.kpahAdmin
  const p = docToken(bearer(event), 'admin')
  let a: AdminUser | null = null
  if (p && laTaiKhoanAdmin(p.u)) {
    a = { id: p.id, username: p.u }
    // Gia hạn trượt: mỗi lần thao tác cấp token mới.
    setResponseHeader(event, 'X-Kpah-Token', taoTokenAdmin(a.id, a.username))
  }
  event.context.kpahAdmin = a
  return a
}

export function requireAdmin(event: H3Event): AdminUser {
  const a = adminUser(event)
  if (!a) throw apiLoi(401, 'Phiên quản trị đã hết hạn. Hãy đăng nhập lại.', { code: 'admin_required' })
  return a
}

// ---------------------------------------------------------------------------
//  Nhật ký thao tác (bảng kpah_db.web_admin_log)
// ---------------------------------------------------------------------------

export async function coBangNhatKy(): Promise<boolean> {
  return coBang(dbGame(), 'web_admin_log')
}

/** Ghi một dòng nhật ký. Không bao giờ ném lỗi ra ngoài. */
export async function ghiNhatKy(event: H3Event, hanhDong: string, doiTuong = '', chiTiet = ''): Promise<void> {
  if (!(await coBangNhatKy())) return
  try {
    const ten = event.context.kpahAdmin?.username ?? ''
    await exec(
      dbGame(),
      'INSERT INTO web_admin_log (admin_user, hanh_dong, doi_tuong, chi_tiet, ip) VALUES (?, ?, ?, ?, ?)',
      [ten.slice(0, 50), hanhDong.slice(0, 50), doiTuong.slice(0, 100), chiTiet.slice(0, 1000), ipKhach(event)],
    )
  } catch { /* im lặng */ }
}

// Lùi về đếm trong bộ nhớ khi chưa có bảng web_admin_log: yếu hơn (mất khi khởi
// động lại) nhưng còn hơn không chặn dò mật khẩu gì cả.
const saiTrongBoNho = new Map<string, number[]>()

/** Riêng lần đăng nhập thất bại: chưa có admin nên ghi tên người đã gõ vào. */
export async function ghiNhatKyDangNhapThatBai(event: H3Event, usernameGoVao: string, lyDo: string): Promise<void> {
  const ip = ipKhach(event)
  if (!(await coBangNhatKy())) {
    const ds = saiTrongBoNho.get(ip) ?? []
    ds.push(nowSec())
    saiTrongBoNho.set(ip, ds.filter(t => t > nowSec() - 86400))
    if (saiTrongBoNho.size > 5000) {
      for (const [k, v] of saiTrongBoNho) {
        if (!v.some(t => t > nowSec() - 86400)) saiTrongBoNho.delete(k)
      }
    }
    return
  }
  try {
    await exec(
      dbGame(),
      'INSERT INTO web_admin_log (admin_user, hanh_dong, doi_tuong, chi_tiet, ip) VALUES (?, ?, ?, ?, ?)',
      ['', 'dang_nhap_that_bai', usernameGoVao.slice(0, 100), lyDo.slice(0, 1000), ip],
    )
  } catch { /* im lặng */ }
}

/** Số lần gõ sai gần đây từ IP này. */
export async function soLanGoSai(event: H3Event): Promise<number> {
  const ip = ipKhach(event)
  const cua = Math.trunc(useKpahConfig().admin.failWindow)
  if (!(await coBangNhatKy())) {
    return (saiTrongBoNho.get(ip) ?? []).filter(t => t > nowSec() - cua).length
  }
  try {
    const n = await qv(
      dbGame(),
      `SELECT COUNT(*) FROM web_admin_log
        WHERE hanh_dong = 'dang_nhap_that_bai' AND ip = ?
          AND thoi_gian > DATE_SUB(NOW(), INTERVAL ${cua} SECOND)`,
      [ip],
    )
    return Number(n ?? 0)
  } catch {
    return 0
  }
}

export async function dangBiChanDoSai(event: H3Event): Promise<boolean> {
  return (await soLanGoSai(event)) >= useKpahConfig().admin.maxFail
}

/**
 * Bọc handler của API admin: bắt buộc đăng nhập, và ghi lỗi không bắt được vào
 * nhật ký (hanh_dong = 'loi_web') để trang Chẩn đoán nhìn thấy.
 * Chỉ admin đã đăng nhập mới thấy nội dung lỗi.
 */
export function defineAdminHandler<T>(fn: (event: H3Event, admin: AdminUser) => Promise<T> | T) {
  return defineEventHandler(async (event) => {
    const admin = requireAdmin(event)
    try {
      return await fn(event, admin)
    } catch (e: any) {
      if (e?.statusCode && e.statusCode < 500) throw e
      console.error('[kpah-admin]', event.path, e)
      await ghiNhatKy(event, 'loi_web', event.path.slice(0, 100), `${e?.name ?? 'Error'}: ${e?.message ?? e}`)
      throw apiLoi(500, `Trang quản trị gặp lỗi: ${e?.message ?? 'không rõ'}. Thường là do MySQL chưa chạy hoặc sai thông tin kết nối.`)
    }
  })
}
