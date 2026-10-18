export default defineEventHandler(async (event) => {
  const t = await tinCongKhai(String(getRouterParam(event, 'slug') ?? ''))
  if (!t) throw apiLoi(404, 'Không có bài viết này.')
  const { nguon: _n, ...ra } = t
  return ra
})
