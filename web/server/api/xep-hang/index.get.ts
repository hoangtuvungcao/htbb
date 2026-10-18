/**
 * Bảng xếp hạng công khai: danh sách bảng đang hiện (theo /admin/xep-hang, bỏ bảng thiếu CSDL /
 * tính năng tắt), bộ lọc của từng bảng, số dòng mỗi trang. Không cần đăng nhập.
 * Dữ liệu từng bảng: GET /api/xep-hang/<mã>.
 */
export default defineEventHandler(async () => {
  try {
    return await xhDanhSachCongKhai()
  }
  catch (e: any) {
    console.error('[xep-hang] danh sach:', e?.code ?? e?.message ?? e)
    return { bang: [], moiTrang: 10, lamMoiGiay: 60, loi: true }
  }
})
