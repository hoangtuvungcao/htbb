/**
 * Tổng quan dữ liệu game (chỉ đọc): các thẻ, từng bộ dữ liệu, bảng có tồn tại không, số dòng.
 * Mọi vai trò quản trị đều đọc được (biên tập cần số liệu đúng để viết bài).
 */
export default defineAdminHandler(async () => {
  const ds = await tongQuanDuLieuGame()
  return {
    tab: TAB_DU_LIEU_GAME,
    ds,
    nhiemVuTheoLoai: CAM_NANG.nhiemVu.demLoai ?? {},
    toiDa: DU_LIEU_GAME_TOI_DA,
  }
})
