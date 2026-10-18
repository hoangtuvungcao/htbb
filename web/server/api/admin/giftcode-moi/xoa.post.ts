import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/** Xoá mã CHƯA ai dùng (mã đã dùng chỉ tắt được, để giữ nhật ký khớp). Body { ma }. */
export default defineAdminHandler(async (event) => {
  const ma = gcChuanHoaMa((await bodyObj(event)).ma)
  if (!ma) throw apiLoi(400, 'Mã không hợp lệ.')
  const cu = await q1<Record<string, any>>(dbGame(), 'SELECT * FROM tob_giftcode WHERE ma = ? LIMIT 1', [ma])
  if (!cu) throw apiLoi(404, 'Không tìm thấy mã.')
  if (Number(cu.da_dung) > 0) throw apiLoi(409, 'Mã đã có người dùng — hãy Tắt thay vì xoá.')
  await exec(dbGame(), 'DELETE FROM tob_giftcode WHERE ma = ?', [ma])
  await ghiThayDoi(event, 'xoa_giftcode', `giftcode:${ma}`, cu, null)
  return { ok: true }
})
