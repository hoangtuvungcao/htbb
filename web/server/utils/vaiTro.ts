/**
 * Phân quyền khu quản trị theo VAI TRÒ.
 *
 *   sieu_quan_tri : mọi thứ. Tên trong HTBB_ADMIN_ACCOUNTS luôn là siêu quản trị (không đổi
 *                   được từ web — muốn gỡ thì sửa biến môi trường).
 *   bien_tap      : sửa nội dung website (tin tức, cẩm nang, trang chủ, FAQ, menu, media, lịch).
 *   ho_tro        : hỗ trợ người chơi (khoá/mở, cấm chat, đổi mật khẩu, gửi thư, kích hoạt).
 *   xem           : chỉ xem (mọi GET trừ trang phân quyền / tải file chẩn đoán).
 *
 * Vai trò khác lưu ở htbb_db.web_admin_vai_tro (tools/luu_tru_sinh_du_lieu/sql_nguon/web_cms.sql). Quyền kiểm ở MỘT chỗ:
 * defineAdminHandler gọi kiemQuyen(đường dẫn, phương thức, vai trò) cho mọi /api/admin/**.
 */
import type { H3Event } from 'h3'
import { DS_VAI_TRO_QUYEN, TEN_VAI_TRO_QUYEN, kiemQuyenAdmin, type VaiTroQuyen } from '../../shared/utils/quyenAdmin'

export type VaiTro = VaiTroQuyen
export const DS_VAI_TRO_ADMIN: VaiTro[] = DS_VAI_TRO_QUYEN
export const TEN_VAI_TRO_ADMIN: Record<VaiTro, string> = TEN_VAI_TRO_QUYEN

const cache = new Map<string, { vt: VaiTro | null, luc: number }>()

/** Vai trò của một tài khoản; null = không có quyền vào khu quản trị. Nhớ 30 giây. */
export async function vaiTroCua(username: string): Promise<VaiTro | null> {
  const u = username.trim().toLowerCase()
  if (laTaiKhoanAdmin(u)) return 'sieu_quan_tri'
  const c = cache.get(u)
  if (c && Date.now() - c.luc < 30_000) return c.vt
  let vt: VaiTro | null = null
  try {
    if (await coBang(dbGame(), 'web_admin_vai_tro')) {
      const r = await qv<string>(dbGame(), 'SELECT vai_tro FROM web_admin_vai_tro WHERE username = ? LIMIT 1', [u])
      if (r && (DS_VAI_TRO_ADMIN as string[]).includes(r)) vt = r as VaiTro
    }
  }
  catch { vt = null }
  cache.set(u, { vt, luc: Date.now() })
  return vt
}

export function xoaCacheVaiTro(username?: string) {
  if (username) cache.delete(username.trim().toLowerCase())
  else cache.clear()
}

/** Có ít nhất một người được vào khu quản trị (env hoặc bảng vai trò)? */
export async function coAiLaAdmin(): Promise<boolean> {
  if (adminAccounts().length > 0) return true
  try {
    return await coBang(dbGame(), 'web_admin_vai_tro')
      && Number(await qv(dbGame(), 'SELECT COUNT(*) FROM web_admin_vai_tro')) > 0
  }
  catch { return false }
}

/** Ma trận quyền ở shared/utils/quyenAdmin.ts (nguồn duy nhất, có test liệt kê mọi route). */
export function kiemQuyen(path: string, method: string, vt: VaiTro): boolean {
  return kiemQuyenAdmin(path, method, vt)
}

export function requireQuyen(event: H3Event, vt: VaiTro) {
  if (!kiemQuyen(event.path, event.method, vt)) {
    throw apiLoi(403, `Vai trò "${TEN_VAI_TRO_ADMIN[vt]}" không được làm thao tác này.`, { code: 'khong_du_quyen' })
  }
}
