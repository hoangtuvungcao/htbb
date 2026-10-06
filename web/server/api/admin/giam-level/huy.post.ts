/** Huỷ một lệnh giảm Level còn chờ. Body: { id_lenh }. */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id_lenh)
  const ok = await huyLenh(dbGame(), id, admin.username, { loai: LENH_LOAI_GIAM_CAP_DO })
  if (!ok) throw apiLoi(409, 'Không huỷ được: lệnh đã xử lý.')
  await ghiNhatKy(event, 'huy_lenh_nhan_vat', '', `lệnh #${id}`)
  return { loai: 'success' as const, thongBao: 'Đã huỷ lệnh.' }
})
