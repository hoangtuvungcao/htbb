/**
 * NÂNG LEVEL TOÀN BỘ (trang /admin/nang-level-toanbo): đưa MỌI nhân vật còn hoạt động
 * (del = 1) lên ĐÚNG một cấp độ đích (0%), dùng cho sự kiện / bù đắp toàn server. Nhân
 * vật đã ở cấp đích trở lên được GIỮ NGUYÊN (không hạ cấp) — server tự báo lỗi riêng cho
 * dòng đó, không làm hỏng nhân vật khác.
 *
 * DÙNG LẠI y hệt loại lệnh dat_cap_do đã có (nút "Đặt cấp độ" của từng nhân vật) thay vì
 * tạo loại lệnh mới: dat_cap_do vốn CHỈ NÂNG cấp, đúng khớp yêu cầu mà không cần biết trước
 * cấp thật của từng nhân vật (nhân vật đang online chỉ sống trong RAM, web không đọc được).
 *
 * PHÂN BIỆT với lệnh dat_cap_do gửi từ trang Nhân vật: cột `tham_so` (không dùng cho
 * dat_cap_do ở nơi khác) được đánh dấu 'toanbo' để bảng "lệnh gần đây" chỉ liệt kê đúng
 * các lệnh hàng loạt của trang này.
 *
 * Với hàng nghìn nhân vật, KHÔNG đợi kết quả từng dòng mà CHÈN THẲNG toàn bộ lệnh trong
 * MỘT transaction rồi trả lời ngay; LenhWeb (đọc bảng mỗi giây, tối đa 50 dòng/vòng) tự
 * xử lý dần ở phía sau.
 */
export const TANG_LEVEL_TOANBO_DANH_DAU = 'toanbo'
