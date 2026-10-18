/**
 * Nguồn vào / nơi tiêu của xu (log nhanxu / truxu), mua shop NPC theo loại tiền, chi tiêu theo tuần.
 * ?ngay=1 (1..7 cho log xu; 1..30 cho shop). Mỗi truy vấn đọc tối đa KINH_TE_LOG_TOI_DA dòng mới nhất.
 */
export default defineAdminHandler(async (event) => {
  const ngay = int(getQuery(event).ngay, 1)
  const [xu, shop, tuan] = await Promise.all([
    docNguonXu(ngay),
    docMuaShop(ngay).catch(() => ({ co: false, ngay, theoTien: [], top: [] })),
    docChiTieuTuan().catch(() => ({ bang: null, tuan: '', dong: [] })),
  ])
  return { xu, shop, tuan, gioiHanLog: KINH_TE_LOG_TOI_DA }
})
