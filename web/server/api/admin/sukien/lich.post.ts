/**
 * Lưu lịch hẹn mở/đóng của một sự kiện.
 * Body: { ma, mo_luc, dong_luc } — mỗi mốc là chuỗi datetime-local hoặc '' (bỏ trống).
 *
 * Một lần gửi cho cả hai mốc chứ không tách: đặt mốc mở mà mốc đóng bị bỏ trống do form
 * kia không gửi kèm thì hoá ra vừa đặt vừa xoá.
 */
export default defineAdminHandler(async (event) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const ma = str(body?.ma)
  const ten = await suKienTenTheoMa(ma)

  const mo = suKienDocMoc(body?.mo_luc ?? '')
  const dong = suKienDocMoc(body?.dong_luc ?? '')
  if (mo === false || dong === false) {
    throw apiLoi(400, 'Mốc thời gian không đọc được. Định dạng: 2026-10-25 00:00.')
  }

  const loi = suKienKiemLich(mo, dong, await suKienBayGio())
  if (loi) throw apiLoi(400, loi)

  await exec(dbGame(), `UPDATE ${SU_KIEN_BANG}
                           SET mo_luc = ?, dong_luc = ?
                         WHERE ma = ?`, [mo, dong, ma])
  await ghiNhatKy(event, 'su_kien_lich', ma, `${ten} | mở: ${mo ?? '—'} | đóng: ${dong ?? '—'}`)
  return { ok: true, message: `Đã đặt lịch cho ${ten}. Tới giờ server sẽ tự đổi rồi tự khởi động lại.` }
})
