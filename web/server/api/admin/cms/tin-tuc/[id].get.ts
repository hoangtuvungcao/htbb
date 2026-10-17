export default defineAdminHandler(async (event) => {
  await canBangCms('web_tin_tuc')
  const r = await q1(dbGame(), 'SELECT * FROM web_tin_tuc WHERE id = ? LIMIT 1', [int(getRouterParam(event, 'id'))])
  if (!r) throw apiLoi(404, 'Không tìm thấy bài viết.')
  return r
})
