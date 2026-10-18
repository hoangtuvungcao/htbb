/** Huỷ yêu cầu khởi động lại an toàn (đang chờ hoặc đang đếm ngược). Chỉ siêu quản trị. */
export default defineAdminHandler(async (event) => {
  await chTaoBang()
  const a = await exec(dbGame(), "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'da_huy', xong_luc = ? WHERE id = 1 AND trang_thai = 'cho'", [gioDb()])
  const b = await exec(dbGame(), "UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'huy' WHERE id = 1 AND trang_thai = 'dang_dem'")
  if (!a.affectedRows && !b.affectedRows) throw apiLoi(409, 'Không có yêu cầu nào đang chờ / đếm ngược.')
  await ghiNhatKy(event, 'huy_khoi_dong_lai', 'htbb-game', '')
  return { ok: true, message: b.affectedRows ? 'Đã gửi lệnh huỷ — máy chủ dừng đếm trong ≤ 5 giây và báo người chơi.' : 'Đã huỷ yêu cầu.' }
})
