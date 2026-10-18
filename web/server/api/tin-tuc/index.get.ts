/** Danh sách tin công khai (CSDL + bài tĩnh). ?ghim=1 chỉ bài ghim. Chỉ trả trường hiển thị. */
export default defineEventHandler(async (event) => {
  const ds = (await dsTinCongKhai()).map(({ nguon: _n, ...t }) => t)
  return getQuery(event).ghim === '1' ? ds.filter(t => t.ghim) : ds
})
