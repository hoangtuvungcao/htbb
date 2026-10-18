/**
 * Tín hiệu nhiều tài khoản / clone (rẻ, chỉ đọc): mã máy dùng chung bởi >= N tài khoản (tob_may_dang_nhap),
 * kèm ai là thành viên. Query: nguong (mặc định 3), tai_khoan (xem liên kết máy/IP của một tài khoản).
 */
export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const ten = str(query.tai_khoan).trim()
  if (ten) return { lienKet: await tvLienKet(ten) }
  return tvTinHieuNhieuTaiKhoan(Math.max(2, Math.min(50, int(query.nguong, 3))))
})
