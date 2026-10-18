/** Xoá hẳn một mã. Nhật ký ai đã nhận (giftcode_log) vẫn giữ nguyên. Body: { id } */
export default defineAdminHandler(async (event) => {
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = gcSoNguyen(b?.id)
  const game = dbGame()
  const ten = String((await qv(game, 'SELECT giftcode FROM giftcode WHERE id = ?', [id])) ?? '')
  if (ten === '') throw apiLoi(404, `Không tìm thấy mã #${id}.`)
  await exec(game, 'DELETE FROM giftcode WHERE id = ?', [id])
  await ghiNhatKy(event, 'xoa_giftcode', ten, 'xoá hẳn khỏi bảng giftcode')
  return { ok: true, message: `Đã xoá mã ${ten}. Nhật ký ai đã nhận vẫn còn trong giftcode_log.` }
})
