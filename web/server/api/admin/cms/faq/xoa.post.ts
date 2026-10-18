export default defineAdminHandler(async (event) => {
  await canBangCms('web_faq')
  const id = int((await bodyObj(event)).id)
  const cu = await q1(dbGame(), 'SELECT * FROM web_faq WHERE id = ? LIMIT 1', [id])
  if (!cu) throw apiLoi(404, 'Không tìm thấy câu hỏi.')
  await exec(dbGame(), 'DELETE FROM web_faq WHERE id = ?', [id])
  xoaDemCms()
  await ghiThayDoi(event, 'cms_xoa_faq', `faq:${id}`, cu, null)
  return { ok: true }
})
