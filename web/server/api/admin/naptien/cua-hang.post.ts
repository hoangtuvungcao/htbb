/**
 * Thêm / sửa một món cửa hàng lượng (CMS). Luật y như server (kiemMon): chỉ trang_tri /
 * mo_rong_tui / doi_ten / tang_toc (5 mã cố định, giá + giới hạn tuần cố định, chỉ EXP), KHÔNG chỉ số, không từ cấm, giá 10–5.000 lượng. Server game nạp lại
 * danh mục sau tối đa 60 giây và tự bỏ qua dòng sai luật.
 */
import type { MonCuaHang } from '../../../utils/napTienThuan'

export default defineAdminHandler(async (event, admin) => {
  if (!(await napCoBang())) napThieuBang()
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const m: MonCuaHang = {
    ma: str(b?.ma).trim().toLowerCase().slice(0, 40),
    ten: str(b?.ten).trim().slice(0, 100),
    nhom: str(b?.nhom),
    trao: str(b?.trao),
    gia_luong: int(b?.gia_luong, 0),
    han_ngay: Math.max(0, int(b?.han_ngay, 0)),
    gioi_han: Math.max(0, int(b?.gioi_han, 0)),
    nhom_xoay: int(b?.nhom_xoay, 0),
    giao_dich_duoc: b?.giao_dich_duoc === true || Number(b?.giao_dich_duoc) === 1 ? 1 : 0,
    co_chi_so: 0,
  }
  const loi = kiemMon(m)
  if (loi) throw apiLoi(400, loi)
  const dangBan = b?.dang_ban === false || Number(b?.dang_ban) === 0 ? 0 : 1
  await exec(dbGame(), 'INSERT IGNORE INTO nap_cua_hang (ma, ten, nhom, trao, gia_luong) VALUES (?, ?, ?, ?, ?)', [m.ma, m.ten, m.nhom, m.trao, m.gia_luong])
  await exec(dbGame(),
    `UPDATE nap_cua_hang SET ten = ?, nhom = ?, trao = ?, gia_luong = ?, han_ngay = ?, gioi_han = ?, nhom_xoay = ?, giao_dich_duoc = ?,
       co_chi_so = 0, dang_ban = ?, thu_tu = ?, mo_ta = ? WHERE ma = ?`,
    [m.ten, m.nhom, m.trao, m.gia_luong, m.han_ngay, m.gioi_han, m.nhom_xoay, m.giao_dich_duoc, dangBan, int(b?.thu_tu, 0),
      str(b?.mo_ta).slice(0, 255), m.ma])
  await napNhatKy(null, `admin:${admin.username}`, 'sua_cua_hang', '', 0, `${m.ma} ${m.trao} ${m.gia_luong} lượng, bán=${dangBan}`, ipKhach(event))
  await ghiNhatKy(event, 'sua_cua_hang_nap', m.ma, `${m.ten}: ${m.gia_luong} lượng`)
  return { ok: true }
})
