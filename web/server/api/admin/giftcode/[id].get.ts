/** Một mã để sửa (trang ?sua=<id>). */
export default defineAdminHandler(async (event) => {
  const id = gcSoNguyen(getRouterParam(event, 'id'))
  const r = id > 0 ? await q1(dbGame(), 'SELECT id, giftcode, xu, luong, luongLock, item, limit_use, type FROM giftcode WHERE id = ?', [id]) : null
  if (!r) throw apiLoi(404, `Không tìm thấy mã #${id}.`)
  return {
    ma: {
      id: Number(r.id),
      giftcode: String(r.giftcode ?? ''),
      xu: Number(r.xu ?? 0),
      luong: Number(r.luong ?? 0),
      luongLock: Number(r.luongLock ?? 0),
      item: String(r.item ?? ''),
      limit_use: Number(r.limit_use ?? 0),
      type: Number(r.type ?? 0),
    },
  }
})
