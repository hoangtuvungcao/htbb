/**
 * Khởi động lại ngay.
 *
 * CƠ CHẾ KHÁC HẲN, đã có sẵn từ trước và CHỈ hoạt động khi server đang sống: cờ trong
 * 5h_systems, server tự đọc mỗi 5 giây rồi tự lưu dữ liệu + thoát, systemd
 * (Restart=always) tự bật lại trong ~10-15 giây. Không cần watcher, không cần server đã
 * tắt hẳn.
 */
export default defineAdminHandler(async (event) => {
  let n = 0
  try {
    const kq = await exec(dbGame(), "UPDATE 5h_systems SET status = 0 WHERE command = 'restart'")
    // mysql2 bật cờ FOUND_ROWS: affectedRows = số dòng KHỚP điều kiện, không phải số dòng
    // bị đổi. Nhờ vậy bấm lần hai khi cờ đã là 0 (server chưa kịp đọc) vẫn báo thành công,
    // chỉ báo "không tìm thấy dòng" khi bảng thật sự thiếu dòng command='restart'.
    n = kq.affectedRows
  } catch (e: any) {
    throw apiLoi(500, `Không thao tác được (kiểm tra bảng 5h_systems): ${e?.message ?? e}`)
  }
  if (n > 0) {
    await ghiNhatKy(event, 'server_restart_ngay', 'kpah-game', '')
    return {
      ok: true,
      message: 'Đã ghi cờ khởi động lại — server sẽ tự lưu dữ liệu và khởi động lại trong khoảng 10-15 giây (chỉ có tác dụng khi server đang chạy).',
    }
  }
  throw apiLoi(400, 'Không tìm thấy dòng command=\'restart\' trong bảng 5h_systems — kiểm tra lại dữ liệu gốc.')
})
