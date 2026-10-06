/** Xoá nhiều mã một lượt. Body: { ids: number[] } */
export default defineAdminHandler(async (event) => {
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  let tho: unknown = b?.ids ?? []
  if (!Array.isArray(tho)) tho = [tho]
  // Lọc kỹ: chỉ giữ số nguyên dương, bỏ trùng. Danh sách đi vào SQL dưới dạng placeholder
  // nên không có đường tiêm SQL, nhưng vẫn lọc để một ô rỗng hay chữ không biến thành id 0.
  const ids = [...new Set((tho as unknown[])
    .map(x => (typeof x === 'number' ? x : (typeof x === 'string' && /^\s*[+-]?\d+\s*$/.test(x) ? Number(x) : Number.NaN)))
    .filter(n => Number.isSafeInteger(n) && n > 0))]
  if (!ids.length) throw apiLoi(400, 'Chưa chọn mã nào để xoá.')

  const game = dbGame()
  const cho = ids.map(() => '?').join(',')
  const ten = (await q(game, `SELECT id, giftcode FROM giftcode WHERE id IN (${cho})`, ids)).map(d => String(d.giftcode))
  if (!ten.length) throw apiLoi(404, 'Không tìm thấy mã nào trong số đã chọn.')

  const kq = await exec(game, `DELETE FROM giftcode WHERE id IN (${cho})`, ids)
  const soXoa = kq.affectedRows
  // Ghi nhật ký TỪNG mã chứ không gộp một dòng: sau này tra "mã X biến đi đâu" theo tên vẫn ra.
  for (const t of ten) {
    await ghiNhatKy(event, 'xoa_giftcode', t, `xoá hàng loạt (${soXoa} mã một lượt)`)
  }
  return { ok: true, message: `Đã xoá ${soXoa} mã: ${ten.join(', ')}. Nhật ký ai đã nhận vẫn còn trong giftcode_log.` }
})
