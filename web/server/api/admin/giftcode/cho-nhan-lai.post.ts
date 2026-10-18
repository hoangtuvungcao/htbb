/**
 * Cho một tài khoản nhận lại một mã. Body: { ma, userId, player }
 *
 * Chặn theo TÀI KHOẢN (userId) từ 09/09/2026, không còn theo tên nhân vật — xoá theo
 * userId để dọn sạch mọi dòng của tài khoản đó cho đúng một mã (kể cả khi tài khoản có
 * nhiều nhân vật cùng nhận trước đó). KHÔNG cộng lại lượt cho mã.
 */
export default defineAdminHandler(async (event) => {
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const ma = str(b?.ma)
  const userId = gcSoNguyen(b?.userId)
  const player = str(b?.player)
  if (ma === '') throw apiLoi(400, 'Thiếu tên mã.')
  const kq = await exec(dbGame(), 'DELETE FROM giftcode_log WHERE giftcode = ? AND userId = ?', [ma, userId])
  await ghiNhatKy(event, 'cho_nhan_lai_giftcode', ma, `tài khoản #${userId} (${player}), xoá ${kq.affectedRows} dòng`)
  return {
    ok: true,
    soDong: kq.affectedRows,
    message: `Tài khoản của ${player} có thể nhận lại mã ${ma} (${kq.affectedRows} dòng đã xoá).`,
  }
})
