/**
 * Bật / tắt một sự kiện. Chỉ đổi cờ `bat`; game server áp dụng ở lần khởi động tới.
 * Body: { ma, viec: 'bat' | 'tat' }
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const viec = str(body?.viec)
  const ma = str(body?.ma)
  if (viec !== 'bat' && viec !== 'tat') throw apiLoi(400, 'Không hiểu thao tác.')

  // Chỉ nhận mã ĐÃ CÓ trong bảng. Bảng do server tự đăng lên, nên đây cũng là cách chặn
  // người gửi form tay tạo ra dòng rác mà game không bao giờ đọc.
  const ten = await suKienTenTheoMa(ma)

  // Sự kiện đang theo lịch năm: bật / tắt tay = chuyển sang thủ công (lịch không tự đặt lại nữa).
  const thuCong = await chuyenThuCongNeuTheoLich(ma)
  await exec(dbGame(), `UPDATE ${SU_KIEN_BANG}
                           SET bat = ?, sua_boi = ?, sua_luc = NOW()
                         WHERE ma = ?`, [viec === 'bat' ? 1 : 0, admin.username, ma])
  await ghiNhatKy(event, `su_kien_${viec}`, ma, ten + (thuCong ? ' (chuyển sang thủ công)' : ''))
  return {
    ok: true,
    message: `${viec === 'bat' ? 'Đã bật ' : 'Đã tắt '}${ten}. Server tự khởi động lại để áp dụng.`
      + (thuCong ? ' Sự kiện này đã chuyển sang THỦ CÔNG — bật lại "Tự động theo lịch" ở mục Lịch năm khi muốn.' : ''),
  }
})
