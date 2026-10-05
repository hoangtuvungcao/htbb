/**
 * Huỷ một phần thưởng CHƯA nhận. Body: { id }.
 * Dòng đã nhận không huỷ được — DELETE còn kèm `AND da_nhan = 0` phòng trường hợp
 * người chơi vừa nhận giữa lúc kiểm và lúc xoá; khi đó không xoá được dòng nào và báo
 * lại cho admin thay vì báo "đã huỷ" sai sự thật.
 */
export default defineAdminHandler(async (event) => {
  const body = (await readBody<{ id?: unknown }>(event).catch(() => null)) ?? {}
  const id = int(body.id)
  const game = dbGame()
  await duaTopTaoBang(game)
  const row = await q1(game, `SELECT vi_tri, charname, da_nhan FROM \`${DUA_TOP_BANG}\` WHERE id = ?`, [id])
  if (!row) throw apiLoi(404, `Không tìm thấy dòng #${id}.`)
  if (Number(row.da_nhan) !== 0) throw apiLoi(400, 'Dòng này đã được nhận, không huỷ được.')

  const kq = await exec(game, `DELETE FROM \`${DUA_TOP_BANG}\` WHERE id = ? AND da_nhan = 0`, [id])
  if (kq.affectedRows === 0) throw apiLoi(409, 'Người chơi vừa nhận phần thưởng này, không huỷ được nữa.')
  await ghiNhatKy(event, 'xoa_thuong_duatop', String(row.charname), `huỷ hạng ${row.vi_tri} (chưa nhận)`)
  return { ok: true, message: `Đã huỷ phần thưởng hạng ${row.vi_tri} của ${row.charname}.` }
})
