/**
 * Gửi thư trong game: một dòng tob_inbox — người chơi thấy ở lần vào game kế tiếp (MessageCreator getInbox).
 * Body { charId, noiDung (1–800 ký tự), nguoiGui? (tối đa 45 ký tự, mặc định "Ban quản trị") }.
 * An toàn cả khi nhân vật online (game chỉ ĐỌC hộp thư lúc vào game, không ghi đè bảng này).
 */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const id = ncIdNhanVat(b.charId)
  // tob_inbox là utf8 (3 byte): emoji làm MariaDB strict báo lỗi 1366 — thay bằng '?' (xem chuoiDb.ts)
  const noiDung = chuUtf8mb3(chuoi(b.noiDung, 'Nội dung', 800, 1))
  const gui = chuUtf8mb3(chuoi(b.nguoiGui, 'Người gửi', 45)) || 'Ban quản trị'
  const c = await qv<string>(dbGame(), 'SELECT charname FROM tob_char WHERE id = ? LIMIT 1', [id])
  if (!c) throw apiLoi(404, 'Không tìm thấy Pokémon.')
  const r = await exec(dbGame(), 'INSERT INTO tob_inbox (charname, sender, info, dateSend) VALUES (?, ?, ?, NOW())', [c, gui, noiDung])
  await ghiThayDoi(event, 'gui_thu', `nv:${c}`, null, { idThu: Number(r.insertId), gui, noiDung })
  return { ok: true, message: `Đã gửi thư cho ${c}. Người chơi thấy thư ở lần vào game kế tiếp.` }
})
