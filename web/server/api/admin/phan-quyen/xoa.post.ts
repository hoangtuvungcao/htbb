export default defineAdminHandler(async (event) => {
  await canBangCms('web_admin_vai_tro')
  const u = str((await bodyObj(event)).username).toLowerCase()
  const cu = await q1(dbGame(), 'SELECT * FROM web_admin_vai_tro WHERE username = ? LIMIT 1', [u])
  if (!cu) throw apiLoi(404, 'Không có người này trong danh sách.')
  await exec(dbGame(), 'DELETE FROM web_admin_vai_tro WHERE username = ?', [u])
  xoaCacheVaiTro(u)
  await ghiThayDoi(event, 'go_vai_tro', `vai_tro:${u}`, cu, null)
  return { ok: true }
})
