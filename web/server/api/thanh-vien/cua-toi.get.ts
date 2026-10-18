/** Trạng thái Thành viên của tài khoản đang đăng nhập + các đơn mua thành viên. */
import type { DonNap } from '../../utils/napTien'

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  if (!(await tvCoBang())) return { coBang: false, trangThai: null, dons: [] }
  const trangThai = await tvTrangThai(u.id)
  const dons = (await napCoBang())
    ? (await q<DonNap>(dbGame(), `SELECT ${NAP_DON_COT} FROM nap_don WHERE user_id = ? AND loai = 'thanh_vien' ORDER BY id DESC LIMIT 20`, [u.id])).map(napDonCongKhai)
    : []
  return { coBang: true, trangThai, dons }
})
