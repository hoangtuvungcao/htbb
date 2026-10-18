export default defineAdminHandler(async (event) => {
  await canBangCms('web_su_kien_lich')
  const tuan = int((await bodyObj(event)).tuan)
  const cu = await q1(dbGame(), 'SELECT * FROM web_su_kien_lich WHERE tuan = ? LIMIT 1', [tuan])
  await exec(dbGame(), 'DELETE FROM web_su_kien_lich WHERE tuan = ?', [tuan])
  xoaDemCms()
  await ghiThayDoi(event, 'cms_xoa_lich', `lich:tuan${tuan}`, cu, null)
  return { ok: true }
})
