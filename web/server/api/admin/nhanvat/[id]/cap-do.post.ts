/**
 * Đặt cấp độ / Buff level kèm % — body: { viec: 'dat_cap_do' | 'buff_level', cap_do, phan_tram }.
 *
 * Cả hai đều là MỘT lệnh "dat_cap_do" gửi cho server game (server/utils/lenhNhanVat.ts);
 * "Buff level kèm %" chỉ khác ở chỗ cho nhập thêm % vào cấp. Cùng tính năng với
 * server/Panel/ButtonEditor.java showBuffExpDialog ("Lên Level/Phần trăm"), nhưng
 * AdminPanel desktop chỉ dùng được khi nhân vật đang online — ở đây server tự biết
 * online hay offline và xử lý tương ứng, web KHÔNG còn ghi thẳng tob_char.
 */
export default defineAdminHandler(async (event, admin) => {
  const id = nvIdTuRoute(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const viec = str(body?.viec)
  if (viec !== 'dat_cap_do' && viec !== 'buff_level') throw apiLoi(400, 'Không hiểu thao tác.')
  const nv = await nvLayDeThaoTac(id)
  const ten = String(nv.charname)

  const laBuff = viec === 'buff_level'
  const capDoMoi = int(body?.cap_do)
  // Ô % để trống hoặc gõ rác thì coi như 0%, không báo lỗi.
  const phanTramTho = laBuff ? Number.parseFloat(String(body?.phan_tram ?? '0')) : 0
  const phanTram = Number.isFinite(phanTramTho) ? phanTramTho : 0
  if (capDoMoi < 1 || capDoMoi > CAPDO_TOI_DA) {
    throw apiLoi(400, `Cấp độ phải từ 1 đến ${CAPDO_TOI_DA}.`)
  }
  // Bảng lưu phần nghìn (bước 0,1%) nên chỉ nhận tới 99,9 — nhập 99,95 sẽ bị làm tròn
  // thành 100,0% = sang cấp sau, không đúng ý người bấm.
  if (phanTram < 0 || phanTram > 99.9) {
    throw apiLoi(400, '% vào cấp phải từ 0 đến 99,9 (bước 0,1).')
  }
  const phanNghin = phanTramRaPhanNghin(phanTram)

  const cu = await q1<{ lastLv: number, xp: number | string }>(dbGame(), 'SELECT lastLv, xp FROM tob_char WHERE id = ? LIMIT 1', [id])

  const idLenh = await guiLenhCapDo(dbGame(), id, ten, capDoMoi, phanNghin, admin.username)
  const lenh = await choKetQuaLenh(dbGame(), idLenh)
  const muc = `cấp ${capDoMoi}${phanNghin > 0 ? ` (${phanNghinRaChuoi(phanNghin)})` : ''}`
  const [loai, thongBao] = moTaKetQuaLenh(lenh, ten, muc)

  // Nhật ký ghi đúng số server dùng (phần nghìn), không ghi số thô người dùng gõ.
  await ghiNhatKy(event, laBuff ? 'buff_level_nhan_vat' : 'dat_cap_do_nhan_vat', ten,
    `lastLv: ${cu ? Math.trunc(Number(cu.lastLv)) : '?'} -> ${capDoMoi}`
    + (phanNghin > 0 ? ` (${phanNghinRaChuoi(phanNghin)})` : '')
    + ` | xp cũ: ${cu ? String(cu.xp) : '?'}`
    + ` | lệnh #${idLenh}: ${tenTrangThaiLenh(lenh ? Number(lenh.trang_thai) : -1)}`
    + (lenh && lenh.ket_qua !== '' ? ` - ${lenh.ket_qua}` : ''))

  return { loai, thongBao, idLenh }
})
