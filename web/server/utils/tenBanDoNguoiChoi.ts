/**
 * Tên bản đồ DỰ PHÒNG khi không có dòng trong bảng SQL `data_map_ten` (nguồn duy nhất của tên bản đồ — xem
 * server/utils/tenHien.ts, sửa ở /admin/ten-hien). Khi nhân vật đang online, tên lấy từ tob_vi_tri_online.map_ten (game
 * ghi theo data_map_ten, cả bản sao theo khu như 2424 = "Bến tàu phía bắc"). File THUẦN (không import).
 */
export function hsTenBanDo(id: number): string {
  return id === -1 ? 'Chưa có (Pokémon mới)' : `Bản đồ #${id}`
}
