/** Cấp / đổi vai trò. Body { username, vaiTro, ghiChu }. Tài khoản phải có trong account.team_user. */
export default defineAdminHandler(async (event, admin) => {
  await canBangCms('web_admin_vai_tro')
  const b = await bodyObj(event)
  const u = str(b.username).toLowerCase()
  const vt = str(b.vaiTro) as typeof DS_VAI_TRO_ADMIN[number]
  if (!DS_VAI_TRO_ADMIN.includes(vt)) throw apiLoi(400, 'Vai trò không hợp lệ.')
  if (!/^[a-z0-9_.]{1,50}$/.test(u)) throw apiLoi(400, 'Tên tài khoản không hợp lệ.')
  if (laTaiKhoanAdmin(u)) throw apiLoi(409, 'Tài khoản này nằm trong KPAH_ADMIN_ACCOUNTS (luôn là siêu quản trị).')
  const tk = await qv(dbAccount(), 'SELECT id FROM team_user WHERE username = ? LIMIT 1', [u])
  if (!tk) throw apiLoi(404, `Không có tài khoản game "${u}". Người đó cần đăng ký tài khoản trước.`)
  const cu = await q1(dbGame(), 'SELECT * FROM web_admin_vai_tro WHERE username = ? LIMIT 1', [u])
  const ghiChu = chuoi(b.ghiChu, 'Ghi chú', 200)
  if (cu) await exec(dbGame(), 'UPDATE web_admin_vai_tro SET vai_tro = ?, ghi_chu = ? WHERE username = ?', [vt, ghiChu, u])
  else await exec(dbGame(), 'INSERT INTO web_admin_vai_tro (username, vai_tro, ghi_chu, tao_boi, tao_luc) VALUES (?, ?, ?, ?, NOW())', [u, vt, ghiChu, admin.username])
  xoaCacheVaiTro(u)
  await ghiThayDoi(event, 'cap_vai_tro', `vai_tro:${u}`, cu, { vt, ghiChu })
  return { ok: true }
})
