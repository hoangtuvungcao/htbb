/**
 * Một bảng xếp hạng công khai: GET /api/xep-hang/<mã>?vung=&kieu=&ky=&ngay=&mua=
 * Bộ lọc lạ / sai -> giá trị mặc định (danh sách trắng trong shared/utils/xepHangThuan.ts).
 * Đệm phía server theo "Làm mới (giây)" ở /admin/xep-hang. Chỉ trả tên nhân vật / tên đội.
 */
export default defineEventHandler(async (event) => {
  const ma = String(getRouterParam(event, 'ma') ?? '')
  const kq = await xhLayBang(ma, getQuery(event))
  if (!kq) throw apiLoi(404, 'Bảng xếp hạng này không có hoặc đang ẩn.')
  return kq
})
