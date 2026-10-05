/**
 * Xoá nhiều tài khoản đã chọn ở danh sách trang Tài khoản.
 * Body: { ids: number[] | string[], xac_nhan_xoa: 'XOA DA CHON' }
 * Bỏ qua tài khoản quản trị và chính mình; tài khoản còn nhân vật thì chỉ gửi lệnh xoá
 * nhân vật rồi để "chờ" — chọn lại và bấm xoá lần nữa để hoàn tất.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  if (str(body?.xac_nhan_xoa) !== TK_XAC_NHAN_XOA_NHIEU) {
    throw apiLoi(400, `Gõ ${TK_XAC_NHAN_XOA_NHIEU} để xác nhận xoá các tài khoản đã chọn.`)
  }
  let ids: number[]
  try {
    ids = tkIdsDaChon(body?.ids ?? null)
  } catch (e: any) {
    throw apiLoi(400, e?.message ?? 'Danh sách tài khoản không hợp lệ.')
  }
  const kho = tkKhoXoaMysql((hanhDong, doiTuong, chiTiet) => ghiNhatKy(event, hanhDong, doiTuong, chiTiet))
  const kq = await tkXoaNhieuTaiKhoan(kho, ids, admin.username)
  return { ...tkMoTaKetQuaXoaNhieu(kq), ketQua: kq }
})
