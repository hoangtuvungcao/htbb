/**
 * Dữ liệu trang Đua top (chỉ admin): hai bảng đang chờ /
 * đã nhận, danh mục bộ chọn (đã lọc nhóm cho phép), các trường của từng loại thưởng
 * và bảng "chèn nhanh". Lỗi đọc bảng không làm vỡ trang — trả về loiDoc.
 */
export default defineAdminHandler(async () => {
  let dangCho: DuaTopDong[] = []
  let daNhan: DuaTopDong[] = []
  let loiDoc = ''
  try {
    await duaTopTaoBang()
    ;({ dangCho, daNhan } = await duaTopDocDanhSach())
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
  }

  const dmuc = await duaTopDanhMuc()
  const truong: Record<string, TruongThuong[]> = {}
  for (const n of dmuc) truong[n.loai] = dmTruongCuaLoai(n.loai)

  return {
    soHang: DUA_TOP_SO_HANG,
    loiDoc,
    dangCho,
    daNhan,
    dmuc,
    truong,
    chenNhanh: duaTopChenNhanh(),
  }
})
