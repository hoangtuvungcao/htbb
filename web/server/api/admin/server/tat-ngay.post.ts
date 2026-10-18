/**
 * Ghi yêu cầu TẮT HẲN server.
 * Body: { xac_nhan } — phải gõ đúng cụm DIEU_KHIEN_XAC_NHAN_TAT. Văng TẤT CẢ người chơi
 * đang online và giữ tắt cho tới khi có người bấm Bật lại (khác Khởi động lại).
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  // So đúng từng ký tự (chỉ bỏ khoảng trắng hai đầu): gõ thường/thiếu dấu cách là không qua.
  if (str(body?.xac_nhan) !== DIEU_KHIEN_XAC_NHAN_TAT) {
    throw apiLoi(400, `Gõ đúng cụm "${DIEU_KHIEN_XAC_NHAN_TAT}" để xác nhận.`)
  }
  try {
    await exec(dbGame(), `UPDATE dieu_khien_server SET muon_chay=0, gio_hen=NULL, da_ap_dung=0,
                          yeu_cau_boi=?, yeu_cau_luc=NOW() WHERE id=1`, [admin.username])
  } catch (e: any) {
    throw apiLoi(500, `Không thao tác được (có thể chưa có bảng dieu_khien_server): ${e?.message ?? e}`)
  }
  await ghiNhatKy(event, 'server_tat', 'htbb-game', 'tắt ngay')
  return {
    ok: true,
    message: 'Đã ghi yêu cầu TẮT server. Watcher trên VPS sẽ áp dụng trong vòng 1 phút — mọi người chơi đang online sẽ bị văng ra.',
  }
})
