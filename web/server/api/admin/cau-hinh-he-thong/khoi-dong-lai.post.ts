/**
 * Yêu cầu khởi động lại AN TOÀN: máy chủ game đếm ngược (báo người chơi), lưu hết dữ liệu, thoát; systemd
 * (Restart=always) / chay_lap.sh bật lại. Body: { giay: 10..1800, lyDo }. Chỉ siêu quản trị.
 * Từ chối khi máy chủ không gửi nhịp > 120 giây (không có ai nhận — tránh tự khởi động lại bất ngờ lúc bật sau).
 */
export default defineAdminHandler(async (event, admin) => {
  const kq = kiemYeuCauKhoiDongLai(await readBody(event).catch(() => null))
  if (!kq.ok) throw apiLoi(400, kq.loi)
  await chTaoBang()
  const nhip = await chGiayTuNhip()
  if (nhip === null || nhip > 120) {
    throw apiLoi(409, 'Máy chủ game không phản hồi (không có nhịp xác nhận cấu hình trong 2 phút) — dùng trang Bật/tắt máy chủ.')
  }
  const r = await exec(dbGame(),
    `UPDATE cau_hinh_khoi_dong_lai SET trang_thai = 'cho', giay = ?, ly_do = ?, yeu_cau_boi = ?, yeu_cau_luc = ?, bat_dau_luc = NULL, thoat_luc = NULL, xong_luc = NULL
     WHERE id = 1 AND trang_thai NOT IN ('cho', 'dang_dem', 'huy', 'dang_thoat')`,
    [kq.giay, kq.lyDo, admin.username.slice(0, 50), gioDb()])
  if (!r.affectedRows) throw apiLoi(409, 'Đang có một yêu cầu khởi động lại — huỷ nó trước nếu muốn đổi.')
  await ghiNhatKy(event, 'khoi_dong_lai_an_toan', 'htbb-game', `${kq.giay}s: ${kq.lyDo}`)
  return { ok: true, message: `Đã gửi yêu cầu. Máy chủ nhận trong ≤ 5 giây, báo người chơi và khởi động lại sau ${kq.giay} giây.` }
})
