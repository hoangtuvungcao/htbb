/** Gửi thư trong game (hộp thư tob_inbox, người chơi thấy ở lần vào game kế tiếp). Body { charname, noiDung, nguoiGui? }. */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const ten = str(b.charname)
  const noiDung = chuoi(b.noiDung, 'Nội dung', 800, 1)
  const gui = chuoi(b.nguoiGui, 'Người gửi', 45) || 'Ban quản trị'
  const c = await qv<string>(dbGame(), 'SELECT charname FROM tob_char WHERE charname = ? LIMIT 1', [ten])
  if (!c) throw apiLoi(404, `Không có nhân vật "${ten}".`)
  await exec(dbGame(), 'INSERT INTO tob_inbox (charname, sender, info, dateSend) VALUES (?, ?, ?, NOW())', [c, gui, noiDung])
  await ghiThayDoi(event, 'gui_thu', `nv:${c}`, null, { gui, noiDung })
  return { ok: true, message: `Đã gửi thư cho ${c}. Người chơi thấy thư ở lần vào game kế tiếp.` }
})
