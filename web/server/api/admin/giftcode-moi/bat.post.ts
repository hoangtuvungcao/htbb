import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/** Bật / tắt mã. Body { ma, bat }. */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const ma = gcChuanHoaMa(b.ma)
  if (!ma) throw apiLoi(400, 'Mã không hợp lệ.')
  const bat = b.bat === true || int(b.bat) === 1 ? 1 : 0
  const r = await exec(dbGame(), 'UPDATE tob_giftcode SET bat = ? WHERE ma = ?', [bat, ma])
  if (!r.affectedRows) throw apiLoi(404, 'Không tìm thấy mã.')
  await ghiNhatKy(event, bat ? 'bat_giftcode' : 'tat_giftcode', ma)
  return { ok: true }
})
