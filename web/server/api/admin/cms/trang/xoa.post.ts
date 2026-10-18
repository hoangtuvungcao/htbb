export default defineAdminHandler(async (event) => {
  await canBangCms('web_trang')
  const id = int((await bodyObj(event)).id)
  const cu = await q1<{ slug: string }>(dbGame(), 'SELECT * FROM web_trang WHERE id = ? LIMIT 1', [id])
  if (!cu) throw apiLoi(404, 'Không tìm thấy trang.')
  await exec(dbGame(), 'DELETE FROM web_trang WHERE id = ?', [id])
  xoaDemCms()
  await ghiThayDoi(event, 'cms_xoa_trang', `trang:${cu.slug}`, cu, null)
  return { ok: true }
})
