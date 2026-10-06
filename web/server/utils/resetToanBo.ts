/**
 * RESET TOÀN BỘ SERVER VỀ TRẮNG (trang /admin/reset-toanbo): xoá vĩnh viễn nhân vật của MỌI
 * tài khoản (trừ ADMIN_ACCOUNTS) VÀ huỷ kích hoạt (xoá khỏi 5h_active) — KHÁC với nút
 * "Reset về trắng" của một tài khoản ở trang Tài khoản (nút đó vẫn giữ nguyên 5h_active).
 * team_user (tài khoản đăng nhập) vẫn giữ nguyên — chỉ mất nhân vật và phải bấm "Kích hoạt
 * ngay" lại mới vào game được, y hệt tài khoản mới đăng ký.
 *
 * KHÁC VỚI reset TỪNG TÀI KHOẢN: không chờ kết quả từng nhân vật một (choKetQuaLenh) vì có
 * thể có hàng chục/hàng trăm nhân vật — chờ tuần tự sẽ treo trang nhiều phút. Thay vào đó
 * CHÈN THẲNG toàn bộ lệnh xoá vào web_lenh_nhan_vat trong một transaction rồi trả lời ngay;
 * luồng nền LenhWeb của server game tự xử lý dần (khoảng 1 nhân vật/giây).
 */
import { q } from './db'

/** Cụm phải gõ đúng để xác nhận — kiểm ở server, giao diện chỉ bật/tắt nút. */
export const RESET_TOANBO_CUM_TU = 'XOA TOAN BO'

/** id (team_user.id) của các tài khoản quản trị — không đụng tới khi reset hàng loạt. */
export async function resetIdTaiKhoanAdmin(): Promise<number[]> {
  const tens = adminAccounts()
  if (tens.length === 0) return []
  const cho = tens.map(() => '?').join(',')
  const rows = await q<{ id: number }>(dbAccount(), `SELECT id FROM team_user WHERE LOWER(username) IN (${cho})`, tens)
  return rows.map(r => Math.trunc(Number(r.id)))
}

/** Câu SQL + tham số loại trừ userId của admin, dùng chung cho đếm và cho xoá. */
export function resetDieuKienNgoaiAdmin(idsAdmin: number[]): [string, number[]] {
  if (idsAdmin.length === 0) return ['', []]
  const cho = idsAdmin.map(() => '?').join(',')
  return [` WHERE userId NOT IN (${cho})`, idsAdmin]
}
