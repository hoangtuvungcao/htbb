/** Huỷ một lệnh còn chờ của nhân vật này. Body: { id_lenh }. */
export default defineAdminHandler(async (event, admin) => {
  const id = nvIdTuRoute(event)
  const body = await bodyObj(event)
  const nv = await nvLayDeThaoTac(id)
  const ten = String(nv.charname)
  const idLenh = int(body?.id_lenh)
  if (idLenh <= 0 || !Number.isSafeInteger(idLenh)) throw apiLoi(400, 'Mã lệnh không hợp lệ.')
  if (await huyLenh(dbGame(), idLenh, admin.username, { charId: Number(nv.id) })) {
    await ghiThayDoi(event, 'huy_lenh_nhan_vat', `nv:${ten}`, { lenh: idLenh, trangThai: 'chờ' }, { lenh: idLenh, trangThai: 'đã huỷ' })
    return { loai: 'success' as const, thongBao: `Đã huỷ lệnh #${idLenh} cho ${ten}.` }
  }
  throw apiLoi(409, `Không huỷ được lệnh #${idLenh}: server đã xử lý xong (hoặc lệnh không tồn tại).`)
})
