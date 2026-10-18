/**
 * Tắt toàn bộ sự kiện đang đặt bật (chỉ đụng các dòng bat = 1, để sua_luc của dòng
 * vốn đã tắt không bị đổi oan).
 */
export default defineAdminHandler(async (event, admin) => {
  await exec(dbGame(), `UPDATE ${SU_KIEN_BANG}
                           SET bat = 0, sua_boi = ?, sua_luc = NOW()
                         WHERE bat = 1`, [admin.username])
  await ghiNhatKy(event, 'su_kien_tat_het', '', '')
  return { ok: true, message: 'Đã tắt toàn bộ sự kiện. Phải khởi động lại server.' }
})
