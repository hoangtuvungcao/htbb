/** Danh sách thành viên (tìm theo tên tài khoản, trang). */
export default defineAdminHandler(async (event) => {
  if (!(await tvCoBang())) tvThieuBang()
  const query = getQuery(event)
  return tvDanhSach(str(query.q).trim().slice(0, 50), Math.max(1, int(query.page, 1)))
})
