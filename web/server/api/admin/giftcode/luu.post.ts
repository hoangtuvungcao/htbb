/**
 * Tạo mã (không có id) hoặc sửa mã (có id > 0).
 * Body: { id?, giftcode, xu, luong, luongLock, limit_use, type, item }
 *
 * VÌ SAO PHẢI LỌC KÝ TỰ CHẶT: server game ghép thẳng cả tên mã lẫn chuỗi phần thưởng
 * vào câu SQL (`insert into giftcode_log set ... giftcode = '<mã>', item = '<item>'`),
 * không dùng tham số. Một dấu nháy đơn lọt vào đây là hỏng câu lệnh của server.
 * Bộ chọn vật phẩm trên trang chỉ GHÉP CHUỖI vào ô "item" — chuỗi đó vẫn qua đúng
 * kiemTraItem() ở đây y như khi gõ tay, không có đường dữ liệu thứ hai.
 */
export default defineAdminHandler(async (event) => {
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const id = gcSoNguyen(b?.id)
  const laSua = id > 0
  const ma = str(b?.giftcode)
  const xu = gcSoNguyen(b?.xu)
  const luong = gcSoNguyen(b?.luong)
  const lock = gcSoNguyen(b?.luongLock)
  const limit = gcSoNguyen(b?.limit_use)
  const type = gcSoNguyen(b?.type)
  const [item, loiItem] = kiemTraItem(typeof b?.item === 'string' ? b.item : '')

  if (!GIFTCODE_TEN_RE.test(ma)) {
    throw apiLoi(400, 'Tên mã chỉ được dùng chữ, số và gạch dưới, dài 2–50 ký tự.')
  }
  if (loiItem.length) {
    throw apiLoi(400, `Phần thưởng chưa hợp lệ: ${loiItem.join(' | ')}`, { loi: loiItem })
  }
  if (xu < 0 || luong < 0 || lock < 0) {
    throw apiLoi(400, 'Xu và lượng không được âm.')
  }
  // Cột INT(11): số vượt dải thì MySQL hoặc báo lỗi hoặc lặng lẽ kẹp — chặn trước cho rõ.
  for (const n of [xu, luong, lock, limit, type]) {
    if (n < GC_INT_MIN || n > GC_INT_MAX) throw apiLoi(400, 'Số quá lớn (tối đa 2.147.483.647).')
  }
  // Cột item là VARCHAR(255): chuỗi dài hơn bị cắt cụt -> phần thưởng cuối hỏng cú pháp.
  if (item.length > 255) {
    throw apiLoi(400, `Chuỗi phần thưởng dài ${item.length} ký tự, cột item chỉ chứa được 255. Tách bớt sang mã khác.`)
  }

  const game = dbGame()
  // Sửa một id không còn tồn tại (vừa bị xoá ở tab khác) thì UPDATE khớp 0 dòng mà vẫn
  // báo "đã lưu" và ghi nhật ký sai — chặn trước cho rõ.
  if (laSua && !(await qv(game, 'SELECT id FROM giftcode WHERE id = ?', [id]))) {
    throw apiLoi(404, `Không tìm thấy mã #${id} — có thể vừa bị xoá.`)
  }
  // Trùng tên mã: server luôn lấy dòng đầu tiên (`limit 1`) nên dòng thứ hai vĩnh viễn
  // không ai nhận được.
  const trung = await qv(game, 'SELECT id FROM giftcode WHERE giftcode = ? AND id <> ? LIMIT 1', [ma, id])
  if (trung) {
    throw apiLoi(409, `Đã có mã tên “${ma}”. Đặt tên khác, vì server chỉ đọc dòng đầu tiên.`)
  }

  const chiTiet = `xu=${xu} luong=${luong} luongLock=${lock} limit=${limit} type=${type} item=${item}`
  if (!laSua) {
    const kq = await exec(game,
      `INSERT INTO giftcode (giftcode, xu, luong, luongLock, item, expire, limit_use, type)
       VALUES (?, ?, ?, ?, ?, 0, ?, ?)`,
      [ma, xu, luong, lock, item, limit, type])
    await ghiNhatKy(event, 'tao_giftcode', ma, chiTiet)
    return { ok: true, id: kq.insertId, message: `Đã tạo mã ${ma}.` }
  }
  await exec(game,
    `UPDATE giftcode SET giftcode = ?, xu = ?, luong = ?, luongLock = ?,
                         item = ?, limit_use = ?, type = ? WHERE id = ?`,
    [ma, xu, luong, lock, item, limit, type, id])
  await ghiNhatKy(event, 'sua_giftcode', ma, chiTiet)
  return { ok: true, id, message: `Đã lưu mã ${ma}.` }
})
