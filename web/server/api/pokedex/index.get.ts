/**
 * Danh sách 1.025 loài cho trang /pokedex: số, tên, hệ, bậc, thế hệ, đã mở trong game chưa.
 * Không cần đăng nhập. CSDL chưa có bảng data_loai -> { coDuLieu: false, loai: [] } (trang dùng Pokédex tĩnh).
 */
export default defineEventHandler(async (event) => {
  const { ds, coDuLieu } = await dsLoaiCoDem()
  setHeader(event, 'Cache-Control', 'public, max-age=300')
  return { coDuLieu, loai: ds }
})
