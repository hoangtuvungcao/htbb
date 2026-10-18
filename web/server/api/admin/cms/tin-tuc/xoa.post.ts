export default defineAdminHandler(async (event) => {
  await canBangCms('web_tin_tuc')
  const id = int((await bodyObj(event)).id)
  const cu = await q1<{ slug: string }>(dbGame(), 'SELECT * FROM web_tin_tuc WHERE id = ? LIMIT 1', [id])
  if (!cu) throw apiLoi(404, 'Không tìm thấy bài viết.')
  await exec(dbGame(), 'DELETE FROM web_tin_tuc WHERE id = ?', [id])
  xoaDemCms()
  await ghiThayDoi(event, 'cms_xoa_tin', `tin:${cu.slug}`, cu, null)
  return { ok: true }
})
