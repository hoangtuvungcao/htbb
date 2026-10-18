/**
 * Khoá / mở khoá nhân vật (tob_char.ban). Body: { viec: 'khoa' | 'mo_khoa' }.
 *
 * Ghi thẳng cột `ban` là AN TOÀN cả khi người chơi đang online: cột này không nằm
 * trong câu UPDATE lưu nhân vật định kỳ nên server không bao giờ ghi đè lại — chỉ là
 * khoá "ăn từ lần vào sau" (người đang online không bị đá ra).
 * Không khoá được nhân vật thuộc tài khoản quản trị.
 */
export default defineAdminHandler(async (event) => {
  const id = nvIdTuRoute(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const viec = str(body?.viec)
  const nv = await nvLayDeThaoTac(id)
  const ten = String(nv.charname)

  if (viec !== 'khoa' && viec !== 'mo_khoa') throw apiLoi(400, 'Không hiểu thao tác.')

  if (viec === 'khoa') {
    const chu = await nvChuNhan(Number(nv.userId))
    if (chu !== '' && laTaiKhoanAdmin(chu)) {
      throw apiLoi(403, `Nhân vật này thuộc tài khoản quản trị (${chu}) nên không khoá được.`)
    }
  }

  const banMoi = viec === 'khoa' ? 1 : 0
  await exec(dbGame(), 'UPDATE tob_char SET ban = ? WHERE id = ?', [banMoi, id])

  await ghiNhatKy(event, viec === 'khoa' ? 'khoa_nhan_vat' : 'mo_nhan_vat', ten,
    `ban: ${Math.trunc(Number(nv.ban))} -> ${banMoi}`)

  return {
    loai: 'success' as const,
    thongBao: viec === 'khoa'
      ? `Đã khoá nhân vật ${ten}. Nhân vật sẽ biến mất khỏi màn chọn nhân vật của người chơi.`
      : `Đã mở khoá nhân vật ${ten}.`,
  }
})
