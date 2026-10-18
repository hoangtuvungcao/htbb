/** Lịch sử thay đổi cấu hình (cau_hinh_lich_su). Query: khoa?, trang (1..), soDong (≤ 100). */
export default defineAdminHandler(async (event) => {
  const qy = getQuery(event)
  const khoa = str(qy.khoa).trim().slice(0, 64)
  const soDong = Math.min(100, Math.max(1, int(qy.soDong, 50)))
  const trang = Math.max(1, int(qy.trang, 1))
  await chTaoBang()
  const dk = khoa ? 'WHERE khoa = ?' : ''
  const ts = khoa ? [khoa] : []
  const dong = await q(dbGame(), `SELECT id, khoa, bang, gia_tri_cu, gia_tri_moi, nguoi_sua, nguon, ly_do, phien_ban, luc FROM cau_hinh_lich_su ${dk}
    ORDER BY id DESC LIMIT ${soDong} OFFSET ${(trang - 1) * soDong}`, ts)
  const tong = Number(await qv(dbGame(), `SELECT COUNT(*) FROM cau_hinh_lich_su ${dk}`, ts) ?? 0)
  return { dong, tong, trang, soDong }
})
