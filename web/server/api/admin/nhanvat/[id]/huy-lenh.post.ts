/** Huỷ một lệnh còn chờ của nhân vật này. Body: { id_lenh }. */
export default defineAdminHandler(async (event, admin) => {
  const id = nvIdTuRoute(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const nv = await nvLayDeThaoTac(id)
  const ten = String(nv.charname)
  const idLenh = int(body?.id_lenh)
  if (await huyLenh(dbGame(), idLenh, admin.username, { charId: Number(nv.id) })) {
    await ghiNhatKy(event, 'huy_lenh_nhan_vat', ten, `lệnh #${idLenh}`)
    return { loai: 'success' as const, thongBao: `Đã huỷ lệnh #${idLenh} cho ${ten}.` }
  }
  throw apiLoi(409, `Không huỷ được lệnh #${idLenh}: server đã xử lý xong (hoặc lệnh không tồn tại).`)
})
