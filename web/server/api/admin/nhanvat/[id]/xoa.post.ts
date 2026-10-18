/**
 * XOÁ VĨNH VIỄN nhân vật — body: { xac_nhan_ten }. Không hoàn tác được, khác hẳn
 * "Khoá" (chỉ ẩn đi). Chặn tài khoản quản trị giống hệt "khoá", cộng thêm gõ đúng tên
 * để xác nhận (giao diện chỉ bật nút, kiểm tra thật ở đây — phòng khi ai đó gọi thẳng API).
 * Không DELETE thẳng từ web vì nhân vật có thể đang online: đi qua lệnh xoa_nhan_vat.
 */
export default defineAdminHandler(async (event, admin) => {
  const id = nvIdTuRoute(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const nv = await nvLayDeThaoTac(id)
  const ten = String(nv.charname)

  const chu = await nvChuNhan(Number(nv.userId))
  if (chu !== '' && laTaiKhoanAdmin(chu)) {
    throw apiLoi(403, `Nhân vật này thuộc tài khoản quản trị (${chu}) nên không xoá được.`)
  }
  // Bỏ khoảng trắng hai đầu (lỡ dán thừa dấu cách), rồi so khớp đúng từng ký tự.
  if (str(body?.xac_nhan_ten) !== ten) {
    throw apiLoi(400, `Gõ đúng tên nhân vật "${ten}" để xác nhận xoá.`)
  }

  const idLenh = await guiLenhXoaNhanVat(dbGame(), id, ten, admin.username)
  const lenh = await choKetQuaLenh(dbGame(), idLenh)
  const [loai, thongBao] = moTaKetQuaLenh(lenh, ten, 'xoá vĩnh viễn', LENH_LOAI_XOA_NHAN_VAT)

  await ghiNhatKy(event, 'xoa_nhan_vat', ten,
    `lệnh #${idLenh}: ${tenTrangThaiLenh(lenh ? Number(lenh.trang_thai) : -1)}`
    + (lenh && lenh.ket_qua !== '' ? ` - ${lenh.ket_qua}` : ''))

  // Xoá xong thì nhân vật không còn nữa — trang về danh sách thay vì mở lại chi tiết
  // id này (sẽ ra "không tìm thấy nhân vật").
  return { loai, thongBao, idLenh, daXoa: lenh !== null && Number(lenh.trang_thai) === LENH_XONG_ONLINE }
})
