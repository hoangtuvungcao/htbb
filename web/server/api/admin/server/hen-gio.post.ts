/**
 * Đặt giờ watcher tự BẬT server.
 * Body: { gio_hen_dat } — chuỗi datetime-local, theo giờ hệ thống VPS.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const gio = dieuKhienDocMoc(body?.gio_hen_dat ?? '')
  if (gio === false) throw apiLoi(400, 'Giờ hẹn không hợp lệ.')
  if (gio === null) throw apiLoi(400, 'Chưa chọn giờ hẹn bật.')
  try {
    await exec(dbGame(), `UPDATE dieu_khien_server SET muon_chay=1, gio_hen=?, da_ap_dung=0,
                          yeu_cau_boi=?, yeu_cau_luc=NOW() WHERE id=1`, [gio, admin.username])
  } catch (e: any) {
    throw apiLoi(500, `Không thao tác được (có thể chưa có bảng dieu_khien_server): ${e?.message ?? e}`)
  }
  await ghiNhatKy(event, 'server_hen_gio', 'htbb-game', `hẹn bật lúc ${gio}`)
  return {
    ok: true,
    message: `Đã đặt lịch: watcher sẽ tự BẬT server lúc ${gio} (sai lệch tối đa khoảng 1 phút, theo giờ hệ thống VPS).`,
  }
})
