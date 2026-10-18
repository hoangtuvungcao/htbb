/** Xem trước: nhân vật đang bị ẩn khỏi mọi bảng xếp hạng theo cài đặt hiện tại (tối đa 500, cấp cao trước). */
export default defineAdminHandler(async () => {
  const cd = await xhDocCaiDatTuDb()
  return { ds: await xhDanhSachBiAn(cd) }
})
