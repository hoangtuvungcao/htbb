/** Trạng thái Thành viên của tài khoản đang đăng nhập + các đơn mua thành viên. */
import type { DonNap } from '../../utils/napTien'

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  if (napDemoBat() && napDemoXem(u.username)) {
    // DEMO: trạng thái Thành viên THẬT (chỉ đọc, demo không cấp gì) + đơn demo trong bộ nhớ.
    const trangThai = (await tvCoBang().catch(() => false)) ? await tvTrangThai(u.id).catch(() => null) : null
    return { coBang: true, trangThai, dons: napDemoKho().cuaToi(u.id, 'thanh_vien').map(donDemoCongKhai), demo: true }
  }
  if (!(await tvCoBang())) return { coBang: false, trangThai: null, dons: [] }
  const trangThai = await tvTrangThai(u.id)
  const dons = (await napCoBang())
    ? (await q<DonNap>(dbGame(), `SELECT ${NAP_DON_COT} FROM nap_don WHERE user_id = ? AND loai = 'thanh_vien' ORDER BY id DESC LIMIT 20`, [u.id])).map(napDonCongKhai)
    : []
  return { coBang: true, trangThai, dons }
})
