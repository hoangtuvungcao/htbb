/** Huỷ một lệnh nâng level toàn bộ còn chờ. Body: { id_lenh }. */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = int(body?.id_lenh)
  const ok = await huyLenh(dbGame(), id, admin.username, { loai: LENH_LOAI_DAT_CAP_DO })
  if (!ok) throw apiLoi(409, 'Không huỷ được: lệnh đã xử lý.')
  await ghiNhatKy(event, 'huy_lenh_nang_level_toanbo', '', `lệnh #${id}`)
  return { loai: 'success' as const, thongBao: 'Đã huỷ lệnh.' }
})
