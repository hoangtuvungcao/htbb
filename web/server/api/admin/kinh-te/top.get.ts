/** Người giữ nhiều xu / lượng / lượng khoá nhất (túi + kho), ?n=20 (5..50). Chỉ đọc. */
export default defineAdminHandler(async (event) => {
  return docTopNamGiu(int(getQuery(event).n, 20))
})
