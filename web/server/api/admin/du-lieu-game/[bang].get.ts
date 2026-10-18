/**
 * Một bộ dữ liệu game (chỉ đọc). `bang` = mã trong DS_DU_LIEU_GAME (danh sách trắng) — tên bảng/cột
 * thật nằm trong mã nguồn, không lấy từ người dùng.
 * Query: q (tìm, ≤ 100 ký tự, qua tham số), limit (≤ 5000, mặc định 5000), offset.
 */
export default defineAdminHandler(async (event) => {
  const b = timBoDuLieu(getRouterParam(event, 'bang'))
  if (!b) throw apiLoi(404, 'Không có bộ dữ liệu này.')
  const query = getQuery(event)
  const tim = str(query.q).slice(0, 100)
  const kq = await docBoDuLieu(b, tim, int(query.limit, DU_LIEU_GAME_TOI_DA), int(query.offset, 0))
  return { ...kq, ten: b.ten, moTa: b.moTa, bang: b.bang ?? null, cot: b.cot }
})
