/**
 * Nền móng khu quản trị: đăng nhập, phân quyền, nhật ký thao tác.
 *
 * AI VÀO ĐƯỢC? Đăng nhập bằng chính TÀI KHOẢN GAME (account.team_user, so khớp
 * bằng PASSWORD() y hệt login server), và tên đó phải nằm trong HTBB_ADMIN_ACCOUNTS.
 * Danh sách rỗng = KHÔNG AI vào được (khoá mặc định, không phải mở mặc định) —
 * quên cấu hình thì trang tự đóng lại chứ không mở toang.
 *
 * Không dựa vào tob_char.isAdmin: trong game quyền GM theo TÊN NHÂN VẬT trong
 * sv.adminAccounts của server.ini, còn cột isAdmin bị ghi đè mỗi lần nạp nhân
 * vật nên không đáng tin. Danh sách riêng cho web còn tách bạch được: làm GM trong
 * game chưa chắc được quyền khoá tài khoản trên web.
 *
 * PHIÊN: token 'admin' sống HTBB_ADMIN_IDLE_TIMEOUT giây (mặc định 60 phút) kể từ
 * lần thao tác cuối. Mỗi request hợp lệ trả về token mới trong header
 * `X-Htbb-Token` (client tự thay) — tức "hết hạn khi quá lâu không thao tác".
 * Tên bị gỡ khỏi danh sách -> mất quyền ngay ở request kế tiếp.
 * Thêm (2026-10-15): HẠN TUYỆT ĐỐI ADMIN_HAN_TOI_DA kể từ lúc đăng nhập (gia hạn trượt không kéo dài mãi),
 * token gắn dấu mật khẩu (đổi mật khẩu là token cũ hết hiệu lực) và tài khoản bị khoá (ban) là mất quyền ngay.
 */
import type { H3Event } from 'h3'

export interface AdminUser {
  id: number
  username: string
  /** Vai trò (server/utils/vaiTro.ts). Tên trong HTBB_ADMIN_ACCOUNTS = sieu_quan_tri. */
  vaiTro: VaiTro
}

export function adminAccounts(): string[] {
  return useHtbbConfig().admin.accounts
}

export function laTaiKhoanAdmin(username: string): boolean {
  return adminAccounts().includes(username.trim().toLowerCase())
}

/** Phiên admin dài nhất kể từ lúc đăng nhập, dù vẫn thao tác liên tục. */
export const ADMIN_HAN_TOI_DA = 12 * 3600

function taoTokenAdminTho(id: number, username: string, pw: string, l: number): string {
  return taoToken('admin', id, username, useHtbbConfig().admin.idleTimeout, { pw, l })
}

/** Token admin MỚI (vừa đăng nhập): đọc hash mật khẩu để gắn dấu, mốc đăng nhập = bây giờ. */
export async function taoTokenAdmin(id: number, username: string): Promise<string> {
  const h = await qv<string>(dbAccount(), 'SELECT `password` FROM team_user WHERE id = ? LIMIT 1', [Math.trunc(id)])
  return taoTokenAdminTho(id, username, dauMatKhau(h), nowSec())
}

/** Admin đang đăng nhập, hoặc null. */
export async function adminUser(event: H3Event): Promise<AdminUser | null> {
  if (event.context.htbbAdmin !== undefined) return event.context.htbbAdmin
  const p = docToken(bearer(event), 'admin')
  let a: AdminUser | null = null
  const vt = p && typeof p.l === 'number' && nowSec() - p.l <= ADMIN_HAN_TOI_DA ? await vaiTroCua(p.u) : null
  if (p && vt) {
    let row: { ban: number, password: string } | null = null
    try {
      row = await q1(dbAccount(), 'SELECT ban, `password` FROM team_user WHERE id = ? LIMIT 1', [Math.trunc(p.id)])
    } catch (e: any) {
      // Lỗi DB tạm thời: báo 503, KHÔNG coi là hết phiên (401 làm trình duyệt xoá phiên, đá admin ra)
      console.error('adminUser db:', e?.code ?? e)
      throw apiLoi(503, 'Chưa kiểm tra được phiên quản trị (MySQL chưa sẵn sàng). Thử lại sau ít giây.')
    }
    if (row && Number(row.ban) !== 1 && p.pw === dauMatKhau(row.password)) {
      a = { id: p.id, username: p.u, vaiTro: vt }
      // Gia hạn trượt: mỗi lần thao tác cấp token mới (giữ nguyên mốc đăng nhập l).
      setResponseHeader(event, 'X-Htbb-Token', taoTokenAdminTho(a.id, a.username, p.pw!, p.l as number))
    }
  }
  event.context.htbbAdmin = a
  return a
}

export async function requireAdmin(event: H3Event): Promise<AdminUser> {
  const a = await adminUser(event)
  if (!a) throw apiLoi(401, 'Phiên quản trị đã hết hạn. Hãy đăng nhập lại.', { code: 'admin_required' })
  return a
}

// ---------------------------------------------------------------------------
//  Nhật ký thao tác (bảng htbb_db.web_admin_log)
// ---------------------------------------------------------------------------

export async function coBangNhatKy(): Promise<boolean> {
  return coBang(dbGame(), 'web_admin_log')
}

/** Ghi một dòng nhật ký. Không bao giờ ném lỗi ra ngoài. */
export async function ghiNhatKy(event: H3Event, hanhDong: string, doiTuong = '', chiTiet = ''): Promise<void> {
  if (!(await coBangNhatKy())) return
  try {
    const ten = event.context.htbbAdmin?.username ?? ''
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
  // trả chỗ giữ (dangBiChanDoSai) SAU khi lượt sai đã được ghi
  try {
    await ghiSaiAdmin(ip, usernameGoVao, lyDo)
  } finally {
    traCho(`admin_dang_nhap|${ip}`)
  }
}

async function ghiSaiAdmin(ip: string, usernameGoVao: string, lyDo: string): Promise<void> {
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
  const cua = Math.trunc(useHtbbConfig().admin.failWindow)
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
  // Giữ chỗ trước khi đếm (gửi song song không vượt được hạn mức) - xem giuCho trong chanSpam.ts
  const dangChayTruoc = giuCho(`admin_dang_nhap|${ipKhach(event)}`)
  const max = useHtbbConfig().admin.maxFail
  if (dangChayTruoc >= max) return true
  return (await soLanGoSai(event)) + dangChayTruoc >= max
}

/**
 * Bọc handler của API admin: bắt buộc đăng nhập, và ghi lỗi không bắt được vào
 * nhật ký (hanh_dong = 'loi_web') để trang Chẩn đoán nhìn thấy.
 * Chỉ admin đã đăng nhập mới thấy nội dung lỗi.
 */
export function defineAdminHandler<T>(fn: (event: H3Event, admin: AdminUser) => Promise<T> | T) {
  return defineEventHandler(async (event) => {
    const admin = await requireAdmin(event)
    requireQuyen(event, admin.vaiTro)
    try {
      return await fn(event, admin)
    } catch (e: any) {
      if (e?.statusCode && e.statusCode < 500) throw e
      console.error('[htbb-admin]', event.path, e)
      await ghiNhatKy(event, 'loi_web', event.path.slice(0, 100), `${e?.name ?? 'Error'}: ${e?.message ?? e}`)
      throw apiLoi(500, `Trang quản trị gặp lỗi: ${e?.message ?? 'không rõ'}. Thường là do MySQL chưa chạy hoặc sai thông tin kết nối.`)
    }
  })
}
