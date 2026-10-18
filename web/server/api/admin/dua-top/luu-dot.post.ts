/**
 * Lưu một đợt phát quà.
 * Body: { hang: [{ ten, item, itemchon }, ...] } — phần tử thứ i là hạng i+1 (tối đa 10).
 *
 * Kiểm HẾT các hạng trước: chỉ cần một hạng sai là KHÔNG lưu gì cả, để admin không phải
 * dò xem hạng nào đã lưu hạng nào chưa. Các dòng được chèn trong MỘT transaction vì cùng
 * lý do (lỗi CSDL giữa chừng cũng không để lại nửa đợt).
 * Chuỗi phần thưởng luôn đi qua kiemTraItem() — bộ chọn trên trang chỉ là tiện ích.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = (await readBody<{ hang?: unknown }>(event).catch(() => null)) ?? {}
  const ds = Array.isArray(body.hang) ? body.hang : []
  const game = dbGame()
  await duaTopTaoBang(game)

  const lam: [number, string, string, string][] = []
  const loi: string[] = []
  for (let hang = 1; hang <= DUA_TOP_SO_HANG; hang++) {
    const o = (ds[hang - 1] ?? {}) as Record<string, unknown>
    const ten = str(o.ten)
    if (ten === '') continue // hạng này bỏ trống — không tạo
    if (!DUA_TOP_TEN_RE.test(ten)) {
      loi.push(`Hạng ${hang}: tên Pokémon không hợp lệ ("${ten}").`)
      continue
    }
    const co = await qv(game, 'SELECT id FROM tob_char WHERE charname = ? AND del = 1 LIMIT 1', [ten])
    if (!co) {
      loi.push(`Hạng ${hang}: không tìm thấy Pokémon "${ten}" đang hoạt động.`)
      continue
    }
    const [item, loiItem] = kiemTraItem(str(o.item))
    if (loiItem.length) {
      loi.push(`Hạng ${hang} (${ten}): ${loiItem.join(' | ')}`)
      continue
    }
    const [itemChon, loiChon] = duaTopKiemTraItemChon(str(o.itemchon))
    if (loiChon.length) {
      loi.push(`Hạng ${hang} (${ten}, tự chọn): ${loiChon.join(' | ')}`)
      continue
    }
    if (item === '' && itemChon === '') {
      loi.push(`Hạng ${hang} (${ten}): chưa chọn phần thưởng nào.`)
      continue
    }
    lam.push([hang, ten, item, itemChon])
  }
  if (loi.length) throw apiLoi(400, `Chưa lưu gì cả, sửa lỗi rồi lưu lại: ${loi.join(' ')}`, { loi })
  if (!lam.length) throw apiLoi(400, 'Chưa điền hạng nào (cần tên Pokémon + phần thưởng).')

  await transaction(game, async (conn) => {
    for (const [hang, ten, item, itemChon] of lam) {
      await exec(conn,
        `INSERT INTO \`${DUA_TOP_BANG}\` (vi_tri, charname, item, item_chon, admin_user) VALUES (?, ?, ?, ?, ?)`,
        [hang, ten, item, itemChon, admin.username.slice(0, 50)])
    }
  })
  for (const [hang, ten, item, itemChon] of lam) {
    await ghiNhatKy(event, 'tao_thuong_duatop', ten,
      `hạng ${hang}: ${item}${itemChon !== '' ? ` | tự chọn: ${itemChon}` : ''}`)
  }
  return {
    ok: true,
    soHang: lam.length,
    message: `Đã tạo phần thưởng cho ${lam.length} hạng. Pokémon tương ứng sẽ `
      + 'thấy mục "Nhận thưởng đua top" ở NPC Tổng Quản.',
  }
})
