/**
 * Xoá một vật phẩm — body: { cot, vi_tri, template, plus }.
 *
 * Web KHÔNG sửa cột nữa (nhân vật online sẽ bị lưu đè). Chỉ đọc chuỗi hiện tại để lấy
 * ĐÚNG mẩu thô của món đồ ở vị trí đã bấm, rồi gửi cho server; server tìm món khớp
 * từng ký tự trong RAM (online) hay trong CSDL (offline).
 */
export default defineAdminHandler(async (event, admin) => {
  const id = nvIdTuRoute(event)
  const body = await bodyObj(event)
  const nv = await nvLayDeThaoTac(id)
  const ten = String(nv.charname)

  // Cột phải nằm trong danh sách trắng — tên cột được ghép thẳng vào SQL bên dưới
  // (không tham số hoá được tên cột) nên KHÔNG được nhận trực tiếp từ input.
  const cot = str(body?.cot)
  const viTri = int(body?.vi_tri)
  if (!laCotVatPham(cot)) throw apiLoi(400, 'Cột vật phẩm không hợp lệ.')

  const tho = await q1<Record<string, string | null>>(dbGame(), `SELECT ${cot} FROM tob_char WHERE id = ? LIMIT 1`, [id])
  let chuoiCu = String(tho?.[cot] ?? '').trim()
  if (chuoiCu !== '' && chuoiCu[0] === '>') chuoiCu = chuoiCu.slice(1)
  const cacMau = chuoiCu.split('>')
  if (viTri < 0 || viTri >= cacMau.length || cacMau[viTri]!.trim() === '') {
    throw apiLoi(409, `Không tìm thấy vật phẩm ở vị trí ${viTri} — dữ liệu đã đổi, tải lại trang rồi bấm lại.`)
  }
  const mau = cacMau[viTri]!

  // Đối chiếu với món người bấm đã NHÌN THẤY (template + plus gửi kèm): nếu server vừa
  // lưu lại giữa lúc mở trang và bấm, vị trí này có thể đã là món khác.
  let moTa = `vị trí ${viTri}`
  let khop = false
  const vp = docChuoiVatPham(mau)[0]
  if (vp) {
    const tenId = await tenVatPhamTheoId(dbGame(), [vp.template])
    moTa = (tenId[vp.template] ?? `#${vp.template}`) + (vp.plus > 0 ? ` +${vp.plus}` : '')
    khop = int(body?.template, -1) === vp.template && int(body?.plus, 0) === vp.plus
  }
  if (!khop) {
    throw apiLoi(409, `Vật phẩm ở vị trí ${viTri} hiện là "${moTa}", không còn là món bạn bấm — dữ liệu đã đổi, tải lại trang rồi bấm lại.`)
  }

  const idLenh = await guiLenhXoaVatPham(dbGame(), id, ten, cot, mau, admin.username)
  const lenh = await choKetQuaLenh(dbGame(), idLenh)
  const muc = `xoá "${moTa}" (${cot})`
  const [loai, thongBao] = moTaKetQuaLenh(lenh, ten, muc)

  await ghiThayDoi(event, 'xoa_vat_pham_nhan_vat', `nv:${ten}`, { cot, viTri, vatPham: moTa, chuoiTho: mau.slice(0, 2000) },
    { daXoa: moTa, lenh: idLenh, trangThai: tenTrangThaiLenh(lenh ? Number(lenh.trang_thai) : -1), ketQua: lenh?.ket_qua ?? '' })

  return { loai, thongBao, idLenh }
})
