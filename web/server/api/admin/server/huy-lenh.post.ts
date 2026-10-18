/** Huỷ yêu cầu/lịch hẹn đang chờ watcher: đánh dấu đã áp dụng để watcher bỏ qua. */
export default defineAdminHandler(async (event) => {
  try {
    await exec(dbGame(), 'UPDATE dieu_khien_server SET da_ap_dung=1 WHERE id=1')
  } catch (e: any) {
    throw apiLoi(500, `Không thao tác được (có thể chưa có bảng dieu_khien_server): ${e?.message ?? e}`)
  }
  await ghiNhatKy(event, 'server_huy_lenh', 'htbb-game', '')
  return { ok: true, message: 'Đã huỷ yêu cầu/lịch hẹn đang chờ xử lý.' }
})
