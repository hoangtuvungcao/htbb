export default defineAdminHandler(async (event) => {
  await canBangCms('web_trang')
  const r = await q1(dbGame(), 'SELECT * FROM web_trang WHERE id = ? LIMIT 1', [int(getRouterParam(event, 'id'))])
  if (!r) throw apiLoi(404, 'Không tìm thấy trang.')
  return r
})
