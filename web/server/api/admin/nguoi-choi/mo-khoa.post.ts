/** Mở khoá tài khoản. Body { username }. */
export default defineAdminHandler(async (event) => {
  const u = str((await bodyObj(event)).username).toLowerCase()
  const tk = await q1<{ id: number, ban: number }>(dbAccount(), 'SELECT id, ban FROM team_user WHERE username = ? LIMIT 1', [u])
  if (!tk) throw apiLoi(404, `Không có tài khoản "${u}".`)
  await exec(dbAccount(), 'UPDATE team_user SET ban = 0 WHERE id = ?', [tk.id])
  if (await coBangCms('web_khoa_tai_khoan')) {
    await exec(dbGame(), "UPDATE web_khoa_tai_khoan SET mo_luc = NOW() WHERE username = ? AND loai = 'khoa' AND mo_luc IS NULL", [u])
  }
  await ghiThayDoi(event, 'mo_tai_khoan', `tk:${u}`, { ban: Number(tk.ban) }, { ban: 0 })
  return { ok: true, message: `Đã mở khoá ${u}.` }
})
