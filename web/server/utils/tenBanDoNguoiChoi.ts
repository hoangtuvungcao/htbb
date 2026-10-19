/**
 * Tên bản đồ theo id để hiện "vị trí đã lưu" trong hồ sơ người chơi. File THUẦN (không import).
 * Nguồn: bảng data_hst_vung trong HTBB/tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_he_sinh_thai.sql (tên đã thêm dấu tiếng Việt).
 * Khi nhân vật đang online, tên thật lấy từ tob_vi_tri_online.map_ten (game tự ghi) — bảng này chỉ là dự phòng.
 * id lạ (bản sao theo khu, ví dụ 301, 1701) -> "Bản đồ #id".
 */
const TEN: Record<number, string> = {
  0: 'Pewter', 1: 'Pallet — đồng cỏ', 2: 'Viridian — bờ biển', 3: 'Vermilion — hồ lớn', 4: 'Cerulean — rừng',
  5: 'Lavender — đồng cỏ', 6: 'Celadon — rừng nhỏ', 7: 'Saffron — đầm lầy', 8: 'Fuchsia — bờ biển', 9: 'Cinnabar — đồng bằng',
  10: 'New Bark — rừng lớn', 11: 'Rừng Viridian', 12: 'Khu mua bán', 13: 'Núi Mặt Trăng', 14: 'Vùng Safari', 15: 'Cherrygrove — hồ',
  17: 'Khu mỏ (sa mạc)', 18: 'Goldenrod — tuyến đường', 19: 'Ecruteak — ven biển', 20: 'Violet — rừng', 21: 'Đồng cỏ phía tây',
  22: 'Tàu S.S. Anne — đảo', 23: 'Azalea — tuyến đường', 24: 'Olivine — đồi đá', 25: 'Tháp luyện tập', 26: 'Rừng Cổ',
  28: 'Mahogany — đồng cỏ', 29: 'Rừng Ilex', 30: 'Rừng Ilex 2', 31: 'Rừng Ilex 3', 40: 'Chiến trường',
  41: 'Chân núi Silver (tuyết)', 42: 'Núi Silver (tuyết)', 43: 'Littleroot', 44: 'Rừng Petalburg', 45: 'Rustboro',
  46: 'Bờ biển Dewford', 47: 'Slateport', 48: 'Route 110', 49: 'Mauville', 50: 'Sa mạc Route 111', 51: 'Lavaridge',
  52: 'Núi Chimney', 53: 'Rừng mưa Route 119', 54: 'Fortree', 55: 'Route 120 — Viện Thời tiết', 56: 'Lilycove',
  57: 'Núi Pyre', 58: 'Biển Route 124', 59: 'Mossdeep', 60: 'Sootopolis', 61: 'Biển Route 131', 62: 'Ever Grande',
  70: 'Blackthorn', 72: 'Nhà (Blackthorn)', 73: 'Trung tâm Pokémon', 74: 'Poké Mart', 78: 'Nhà kho', 79: 'Xưởng chế tạo',
  80: 'Cianwood', 82: 'Nhà (Cianwood)', 83: 'Trung tâm Pokémon', 84: 'Poké Mart', 88: 'Nhà kho', 89: 'Xưởng chế tạo',
  100: 'Nhà (Pewter)', 101: 'Khoảng rừng nhỏ', 102: 'Nhà (Pewter) 2', 103: 'Trung tâm Pokémon', 104: 'Poké Mart',
  105: 'Sân nhà khởi đầu', 106: 'Bến tàu', 107: 'Thuyền', 108: 'Nhà kho', 109: 'Xưởng chế tạo', 110: 'Hang động',
  111: 'Hang Darkrai', 112: 'Hang Salamence', 113: 'Hang Tối', 114: 'Hồ Phẫn Nộ (hang)', 115: 'Hang nhỏ', 116: 'Hang cổ',
  117: 'Ngục tối', 118: 'Đường Victory', 122: 'Hang trống', 160: 'Hang Granite', 161: 'Thác Meteor', 162: 'Hang dưới biển',
  163: 'Hang Khởi Nguyên', 164: 'Tháp Trời', 165: 'Đường Victory Hoenn', 201: 'Cao nguyên Indigo', 202: 'Đấu trường',
  203: 'Đấu trường La Mã', 204: 'Đấu trường 2', 205: 'Đấu trường 3', 206: 'Lãnh địa đội', 207: 'Lãnh địa đội 2',
  208: 'Tháp luyện tập', 225: 'Lễ cưới 1', 226: 'Lễ cưới 2', 227: 'Sàn đấu', 228: 'Sảnh chờ', 280: 'Đỉnh Tháp Trời',
  281: 'Liên minh Hoenn', 282: 'Sảnh Nhà vô địch',
}

export function hsTenBanDo(id: number): string {
  return TEN[id] ?? (id === -1 ? 'Chưa có (Pokémon mới)' : `Bản đồ #${id}`)
}
