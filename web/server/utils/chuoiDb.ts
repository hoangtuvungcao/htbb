/**
 * Chữ người dùng gõ → cột utf8 (utf8mb3) của bảng game.
 *
 * Bảng gốc của game (tob_inbox, tob_giftcode, tob_char, team_user, nap_*, tv_*... trong
 * HTBB/sql/htbb.sql) là CHARSET=utf8 = utf8mb3: chỉ chứa ký tự tối đa 3 byte. Ký tự
 * 4 byte (emoji 😀, chữ Hán hiếm...) ghi vào đó thì MariaDB strict mode (mặc định từ 10.2.4)
 * báo lỗi 1366 "Incorrect string value" → API 500; MariaDB không strict thì tự thay bằng '?'.
 * NovaDB (SQLite) nhận hết nên máy dev không thấy lỗi. Hàm này làm đúng như chế độ không
 * strict — thay mỗi ký tự 4 byte bằng '?' — để chạy giống nhau trên cả hai CSDL.
 *
 * Bảng web tự tạo (web_*, cau_hinh*) là utf8mb4: KHÔNG cần lọc.
 */
export function chuUtf8mb3(s: string): string {
  // Cặp surrogate hợp lệ (ký tự ngoài BMP) → '?'. Nửa surrogate lẻ loi: mysql2 tự mã hoá thành
  // U+FFFD (3 byte), cột utf8mb3 vẫn nhận — nhưng thay luôn cho gọn.
  return s.replace(/[\uD800-\uDBFF][\uDC00-\uDFFF]|[\uD800-\uDFFF]/g, '?')
}
