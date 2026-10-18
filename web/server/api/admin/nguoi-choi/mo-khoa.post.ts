/** Mở khoá tài khoản. Body { username, lyDo? (tối đa 300 ký tự) }. Đóng mọi lần khoá đang hiệu lực trong web_khoa_tai_khoan. */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const u = ncTenTaiKhoan(b.username)
  const lyDo = chuoi(b.lyDo, 'Lý do', 300)
  const tk = await q1<{ id: number, username: string, ban: number }>(dbAccount(), 'SELECT id, username, ban FROM team_user WHERE username = ? LIMIT 1', [u])
  if (!tk) throw apiLoi(404, `Không có tài khoản "${u}".`)
  const ten = String(tk.username)
  if (Number(tk.ban) !== 1) throw apiLoi(409, `Tài khoản ${ten} không bị khoá.`)
  await exec(dbAccount(), 'UPDATE team_user SET ban = 0 WHERE id = ?', [tk.id])
  if (await coBangCms('web_khoa_tai_khoan')) {
    await exec(dbGame(), "UPDATE web_khoa_tai_khoan SET mo_luc = NOW() WHERE username = ? AND loai = 'khoa' AND mo_luc IS NULL", [ten])
  }
  await ghiThayDoi(event, 'mo_tai_khoan', `tk:${ten}`, { ban: Number(tk.ban) }, { ban: 0, lyDo })
  return { ok: true, message: `Đã mở khoá ${ten}.` }
})
