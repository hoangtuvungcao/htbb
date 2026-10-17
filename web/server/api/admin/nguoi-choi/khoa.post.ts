/** Khoá tài khoản có lý do + thời hạn. Body { username, lyDo, soGio (0 = vĩnh viễn) }. */
export default defineAdminHandler(async (event, admin) => {
  const b = await bodyObj(event)
  const u = str(b.username).toLowerCase()
  const lyDo = chuoi(b.lyDo, 'Lý do', 300, 3)
  const soGio = Math.max(0, Math.min(24 * 3650, int(b.soGio)))
  const tk = await q1<{ id: number, ban: number }>(dbAccount(), 'SELECT id, ban FROM team_user WHERE username = ? LIMIT 1', [u])
  if (!tk) throw apiLoi(404, `Không có tài khoản "${u}".`)
  if (laTaiKhoanAdmin(u) || u === admin.username.toLowerCase()) throw apiLoi(403, 'Không khoá được tài khoản quản trị.')
  await exec(dbAccount(), 'UPDATE team_user SET ban = 1 WHERE id = ?', [tk.id])
  const hetHan = soGio > 0 ? gioSau(soGio) : null
  if (await coBangCms('web_khoa_tai_khoan')) {
    await exec(dbGame(), "INSERT INTO web_khoa_tai_khoan (username, loai, ly_do, het_han, admin_user, tao_luc) VALUES (?, 'khoa', ?, ?, ?, NOW())", [u, lyDo, hetHan, admin.username])
  }
  await ghiThayDoi(event, 'khoa_tai_khoan', `tk:${u}`, { ban: Number(tk.ban) }, { ban: 1, lyDo, hetHan: hetHan ?? 'vĩnh viễn' })
  return { ok: true, message: `Đã khoá ${u}${hetHan ? ` tới ${hetHan}` : ' vĩnh viễn'}. Người đang online chơi tiếp tới khi thoát.` }
})
