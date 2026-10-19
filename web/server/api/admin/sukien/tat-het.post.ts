/**
 * Tắt toàn bộ sự kiện đang đặt bật (chỉ đụng các dòng bat = 1, để sua_luc của dòng
 * vốn đã tắt không bị đổi oan).
 */
export default defineAdminHandler(async (event, admin) => {
  // Khẩn cấp: các sự kiện đang bật cũng thôi theo lịch năm (không thì 15 giây sau lịch bật lại).
  if (await coCotTuDong()) {
    await exec(dbGame(), `UPDATE ${SU_KIEN_BANG} SET tu_dong = 0 WHERE bat = 1`)
  }
  await exec(dbGame(), `UPDATE ${SU_KIEN_BANG}
                           SET bat = 0, sua_boi = ?, sua_luc = NOW()
                         WHERE bat = 1`, [admin.username])
  await ghiNhatKy(event, 'su_kien_tat_het', '', '')
  return { ok: true, message: 'Đã tắt toàn bộ sự kiện và chuyển chúng sang thủ công. Server tự khởi động lại để áp dụng.' }
})
