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
  const body = await bodyObj(event)
  const viec = str(body?.viec)
  const nv = await nvLayDeThaoTac(id)
  const ten = String(nv.charname)

  if (viec !== 'khoa' && viec !== 'mo_khoa') throw apiLoi(400, 'Không hiểu thao tác.')

  if (viec === 'khoa') {
    const chu = await nvChuNhan(Number(nv.userId))
    if (chu !== '' && await ncLaQuanTri(chu)) {
      throw apiLoi(403, `Pokémon này thuộc tài khoản quản trị (${chu}) nên không khoá được.`)
    }
  }

  const banMoi = viec === 'khoa' ? 1 : 0
  await exec(dbGame(), 'UPDATE tob_char SET ban = ? WHERE id = ?', [banMoi, id])

  await ghiThayDoi(event, viec === 'khoa' ? 'khoa_nhan_vat' : 'mo_nhan_vat', `nv:${ten}`, { ban: Math.trunc(Number(nv.ban)) }, { ban: banMoi })

  return {
    loai: 'success' as const,
    thongBao: viec === 'khoa'
      ? `Đã khoá Pokémon ${ten}. Pokémon sẽ biến mất khỏi màn chọn Pokémon của người chơi.`
      : `Đã mở khoá Pokémon ${ten}.`,
  }
})
