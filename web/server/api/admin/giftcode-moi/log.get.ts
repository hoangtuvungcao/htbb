import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/** Nhật ký nhận mã (mới nhất trước). ?ma= lọc theo mã, ?thang=yyyyMM. */
export default defineAdminHandler(async (event) => {
  if (!(await coBang(dbGame(), 'tob_giftcode_log'))) return { ds: [] }
  const qr = getQuery(event)
  const ma = gcChuanHoaMa(qr.ma)
  const thang = int(qr.thang)
  const dk: string[] = []
  const ts: unknown[] = []
  if (ma) { dk.push('ma = ?'); ts.push(ma) }
  if (thang > 0) { dk.push('thang = ?'); ts.push(thang) }
  const ds = await q(dbGame(), `SELECT ma, user_id, id_char, charname, phan_thuong, gia_tri, thang, luc FROM tob_giftcode_log ${dk.length ? `WHERE ${dk.join(' AND ')}` : ''} ORDER BY luc DESC LIMIT 500`, ts)
  return { ds }
})
