/** Tắt mã: đặt số lượt còn lại về 0 (cột expire không có tác dụng — xem index.get.ts). Body: { id } */
export default defineAdminHandler(async (event) => {
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = gcSoNguyen(b?.id)
  const game = dbGame()
  const gc = await q1(game, 'SELECT giftcode, limit_use FROM giftcode WHERE id = ?', [id])
  if (!gc) throw apiLoi(404, `Không tìm thấy mã #${id}.`)
  await exec(game, 'UPDATE giftcode SET limit_use = 0 WHERE id = ?', [id])
  await ghiNhatKy(event, 'tat_giftcode', String(gc.giftcode), `limit_use: ${Math.trunc(Number(gc.limit_use))} -> 0`)
  return { ok: true, message: `Đã tắt mã ${gc.giftcode} (đặt số lượt còn lại về 0).` }
})
