/** Danh mục cửa hàng lượng trong game (nap_cua_hang) + kết quả kiểm luật từng dòng. */
import type { MonCuaHang } from '../../../utils/napTienThuan'
import { NHOM_CUA_HANG, TRAO_HOP_LE } from '../../../utils/napTienThuan'

export default defineAdminHandler(async () => {
  if (!(await napCoBang())) napThieuBang()
  const ds = await q<MonCuaHang & { dang_ban: number, thu_tu: number, mo_ta: string }>(dbGame(),
    'SELECT ma, ten, nhom, trao, gia_luong, han_ngay, gioi_han, nhom_xoay, giao_dich_duoc, co_chi_so, dang_ban, thu_tu, mo_ta FROM nap_cua_hang ORDER BY thu_tu, ma')
  return {
    ds: ds.map(m => ({ ...m, loi: kiemMon({ ...m, gia_luong: Number(m.gia_luong), han_ngay: Number(m.han_ngay), nhom_xoay: Number(m.nhom_xoay) }) })),
    nhom: NHOM_CUA_HANG,
    trao: TRAO_HOP_LE,
  }
})
