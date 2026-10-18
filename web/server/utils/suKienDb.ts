/**
 * Phần truy vấn CSDL dùng chung cho các API /api/admin/sukien/* (logic thuần ở suKien.ts).
 */

/**
 * Giờ HIỆN TẠI THEO ĐỒNG HỒ CỦA CSDL, không phải của Node.
 *
 * Bắt buộc phải như vậy: `real/LichSuKien` so mốc hẹn bằng `NOW()` của MySQL. Web server và
 * MySQL có thể khác múi giờ, và khi lệch thì web sẽ nhận một mốc mà nó tưởng là tương lai
 * còn server thì chạy ngay — hoặc ngược lại, chặn oan một mốc hợp lệ. Lấy chung một đồng
 * hồ là hết chuyện.
 */
export async function suKienBayGio(): Promise<string> {
  try {
    const v = await qv<string>(dbGame(), 'SELECT NOW()')
    if (v) return String(v)
  } catch { /* lùi về đồng hồ của web */ }
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/**
 * Tên của sự kiện mã `ma`; ném 400 nếu không có trong bảng.
 * Chỉ nhận mã ĐÃ CÓ trong bảng — bảng do server tự đăng lên, nên đây cũng là cách chặn
 * người gửi request tay tạo ra dòng rác mà game không bao giờ đọc.
 */
export async function suKienTenTheoMa(ma: string): Promise<string> {
  const rows = await q<{ ten: string | null }>(dbGame(), `SELECT ten FROM ${SU_KIEN_BANG} WHERE ma = ?`, [ma])
  if (rows.length === 0) throw apiLoi(400, `Không có sự kiện mã "${ma}" trong bảng.`)
  return String(rows[0]!.ten ?? '')
}
