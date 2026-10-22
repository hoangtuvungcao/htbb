/**
 * Số liệu các hệ thống 0.9.8 cho trang cẩm nang (thu phục, trao đổi / chợ Pokémon, phó bản, cúp, ngắm tay, thời tiết,
 * ngày cộng đồng, danh hiệu, kênh chat, kết đôi, tân thủ). MỌI SỐ lấy từ CSDL game (sql/htbb_day_du.sql: bảng data_* và
 * giá trị mặc định cau_hinh_danh_muc) hoặc hằng số máy chủ ghi trong ghi chú — tests/he-thong-098.test.ts đối chiếu với
 * tệp SQL, lệch là test hỏng (đổi số trong game thì sửa ở đây).
 */

/** data_bat_bong: hệ số tỉ lệ (%), trần tỉ lệ (‰), giá (‰ giờ thu nhập), cấp mở. */
export const BONG = [
  { ma: 'poke', ten: 'Poké Ball', heSo: 100, tran: 600, giaPhanNghinGio: 20, capMo: 1 },
  { ma: 'great', ten: 'Great Ball', heSo: 150, tran: 750, giaPhanNghinGio: 50, capMo: 20 },
  { ma: 'ultra', ten: 'Ultra Ball', heSo: 200, tran: 900, giaPhanNghinGio: 120, capMo: 40 },
] as const

/** Bậc hiếm loài (data_bat_loai.do_hiem) + tỉ lệ gốc phổ biến nhất của bậc (‰, data_bat_loai.ti_le_phan_nghin). */
export const BAC_LOAI = [
  { bac: 0, ten: 'Thường', tiLeGoc: 400 },
  { bac: 1, ten: 'Ít gặp', tiLeGoc: 250 },
  { bac: 2, ten: 'Hiếm', tiLeGoc: 120 },
  { bac: 3, ten: 'Rất hiếm', tiLeGoc: 60 },
] as const

/** data_bat_chuyen_doi: chuyển đổi con trùng loài -> Kẹo loài + xu (‰ giờ thu nhập) / lượng khoá. */
export const CHUYEN_DOI = [
  { bac: 0, keo: 2, xuPhanNghinGio: 10, luongKhoa: 0 },
  { bac: 1, keo: 3, xuPhanNghinGio: 30, luongKhoa: 0 },
  { bac: 2, keo: 5, xuPhanNghinGio: 100, luongKhoa: 0 },
  { bac: 3, keo: 8, xuPhanNghinGio: 0, luongKhoa: 1 },
] as const

/** cau_hinh_danh_muc bat.* (mặc định) + com.htbb.pokemon.BatPokemon. */
export const BAT = {
  nguongHpPhanTram: 35, tranNgay: 30, tranNemNgay: 150, cachMs: 1500, soLanBoChay: 4, giuMoiLoai: 2,
  tranXuChuyenDoiPhanNghinGio: 300, tranLuongChuyenDoiNgay: 1,
  /** BatPokemon.heSoCap: quái cao hơn bạn 1–5 cấp ×80%, 6–10 cấp ×50%, hơn 10 cấp không ném được. */
  heSoCap: [{ chenh: '≤ 0', pt: 100 }, { chenh: '1–5', pt: 80 }, { chenh: '6–10', pt: 50 }],
  /** BatPokemon: quái tinh anh (alpha) ×25%, mỗi lần trượt trên cùng con +25% tỉ lệ (tối đa +5 điểm %). */
  alphaPhanTram: 25,
} as const

/** cau_hinh_danh_muc trao_doi.* */
export const TRAO_DOI = { cap: 10, phiPhanNghinGio: 50, phiTranPhanNghinGio: 2000, khoaGio: 24, tranNgay: 10, cachGiay: 60 } as const

/** cau_hinh_danh_muc cho_pokemon.* + com.htbb.pokemon.ChoPokemon (khung giá ‰ giờ thu nhập ở cấp của con Pokémon). */
export const CHO_POKEMON = {
  cap: 20, hanGio: 48, thuePhanTram: 8, phiDangPhanTram: 1, heSo3Sao: 2, heSoSacMau: 3, khoaGio: 72, muaNgay: 5, toiDaDang: 3,
  san: [100, 300, 1000, 3000], tran: [1000, 3000, 10000, 30000],
} as const

/** Phó bản Hầm ngục (data_pho_ban_ngay theo thứ, data_pho_ban_tang, pho_ban.*, com.htbb.pokemon.PhoBanLogic). */
export const PHO_BAN = {
  cap: 20, luotNgay: 3, phutToiDa: 20, soTang: 5, nguoiToiDa: 4, chenhCap: 10, expPhanNghinGio: 200, luongKhoa: 5,
  ngay: [
    { thu: 1, ten: 'Hang Băng' }, { thu: 2, ten: 'Núi Lửa' }, { thu: 3, ten: 'Hồ Sâu' }, { thu: 4, ten: 'Rừng Rậm' },
    { thu: 5, ten: 'Nhà máy Điện' }, { thu: 6, ten: 'Tháp Ma' }, { thu: 7, ten: 'Hang Rồng' },
  ],
} as const

/** Cúp PvP tuần (data_cup_loai, cup.*, com.htbb.pokemon.CupLogic). */
export const CUP = {
  cap: 20, gioVongDau: 20, phutMoiVong: 10, soVong: 5, giayTran: 180, soDoiToiThieu: 4,
  diem: { thang: 3, hoa: 1, thua: 0 },
  luongKhoa: { voDich: 20, hang23: 10, hang48: 5, thamGia: 2 },
  loai: ['Cúp Nhí', 'Cúp hệ Lửa', 'Cúp hệ Nước', 'Cúp hệ Cỏ', 'Cúp hệ Điện', 'Cúp Đôi', 'Cúp Không trang bị'],
} as const

/** Đấu xếp hạng PvP (com.htbb.pokemon.XepHangPvp): ghép ở NPC Đấu trường, Elo CHỈ đổi trong trận do máy chủ ghép. */
export const XEP_HANG = { cap: 20, lechCap: 10, giayToiThieu: 20, giayToiDa: 180, demNguoc: 5, tranToiThieuMua: 10, stToiThieuPhanTram: 10, chanLapPhut: 10 } as const

/** Ngắm tay PvP (pvp.ngam_tay.*, data_chieu_ban). */
export const NGAM_TAY = { buTreMs: 150, giayGiaoChien: 10, soChieu: 50 } as const

/** data_thoi_tiet (tên, gợi ý) + com.htbb.pokemon.ThoiTietLich (4 khung 6 giờ, giữ 55%, trần ±10% sát thương). */
export const THOI_TIET = {
  soKhung: 4, gioKhung: 6, giuPhanTram: 55, tranSatThuong: 10,
  loai: [
    { ten: 'Quang đãng', goiY: 'Trời đẹp, không ảnh hưởng chiêu.' },
    { ten: 'Nắng gắt', goiY: 'Pokémon hệ Lửa, Cỏ xuất hiện nhiều.' },
    { ten: 'Mưa', goiY: 'Pokémon hệ Nước, Điện xuất hiện nhiều.' },
    { ten: 'Tuyết', goiY: 'Pokémon hệ Băng xuất hiện nhiều.' },
    { ten: 'Bão cát', goiY: 'Pokémon hệ Đá, Đất, Thép xuất hiện nhiều.' },
    { ten: 'Sương mù', goiY: 'Pokémon hệ Ma, Siêu linh xuất hiện nhiều.' },
    { ten: 'Gió mạnh', goiY: 'Pokémon hệ Bay, Rồng xuất hiện nhiều.' },
  ],
} as const

/** data_ngay_cong_dong (mặc định từng dòng) + com.htbb.pokemon.NgayCongDong. */
export const NGAY_CONG_DONG = { gioBatDau: 14, soGio: 3, tiLePhanTram: 40, expPhanTram: 20, soHa: 30, nhanShiny: 5 } as const

/** data_danh_hieu (số mẫu theo bậc), data_danh_hieu_do_hiem, danh_hieu.tran_phan_nghin. */
export const DANH_HIEU = {
  tranPhanNghin: 10,
  bac: [
    { ten: 'Thường', mau: '#5F6B7A', so: 253 },
    { ten: 'Hiếm', mau: '#23803A', so: 125 },
    { ten: 'Sử thi', mau: '#7B45D6', so: 33 },
    { ten: 'Huyền thoại', mau: '#B95A0E', so: 9 },
  ],
} as const

/** sv.chat.* (cau_hinh_danh_muc) — bảng chat 0.9.8. */
export const CHAT = { capGan: 5, capTheGioi: 10, xuTheGioi: 100, giayTheGioi: 30, capRieng: 3, doDai: 80, chanToiDa: 100 } as const

/** Lễ kết đôi (com.htbb.pokemon.KetDoi + data_nhom_trung_ten: 15 nhóm trứng, "Chưa khám phá" không kết đôi). */
export const KET_DOI = {
  cap: 40, soNhomTrung: 15, huyLuong: 150, donPhuongLuong: 300,
  nhom: ['Quái vật', 'Nước 1', 'Côn trùng', 'Bay', 'Đồng cỏ', 'Tiên', 'Thực vật', 'Dạng người', 'Nước 3', 'Khoáng vật', 'Vô định hình', 'Nước 2', 'Ditto', 'Rồng'],
  khongKetDoi: 'Chưa khám phá',
} as const

/** Hướng dẫn tân thủ: data_quest_moi 901..910 (name, scontent) — giống nhau ở làng khởi đầu Kanto và Johto. */
export const TAN_THU = [
  { ten: 'Chào trưởng làng', viec: 'Nói chuyện với Xatu ở làng' },
  { ten: 'Thư gửi Blaziken', viec: 'Mang thư cho Blaziken, người có dấu nhiệm vụ trên đầu' },
  { ten: 'Bình thuốc khi máu thấp', viec: 'Đánh quái ở Pallet, máu vơi thì dùng 1 bình thuốc' },
  { ten: 'Bài học chiến đấu', viec: 'Hạ 8 Caterpie ở Pallet' },
  { ten: 'Chiêu thức đầu tiên', viec: 'Cộng 1 điểm kỹ năng, rồi gặp Chansey ở Trung tâm Pokémon' },
  { ten: 'Đơn thuốc cho Kecleon', viec: 'Mang đơn thuốc cho Kecleon ở tiệm tạp hoá' },
  { ten: 'Mặc đồ mới', viec: 'Mặc Ruy băng, rồi gặp Machamp thợ rèn' },
  { ten: 'Ghé ngân hàng Meowth', viec: 'Mang phiếu cho Meowth ở ngân hàng' },
  { ten: 'Thả câu ở ao làng', viec: 'Thả câu 1 lần ở ao trong làng, rồi gặp Pidgeot' },
  { ten: 'Về báo trưởng làng', viec: 'Mang Lông vũ Pidgeot về cho Xatu' },
] as const

/** Phần nghìn -> "1,5" (phần trăm). */
export const phanTram098 = (pn: number) => (pn / 10).toLocaleString('vi-VN', { maximumFractionDigits: 1 })
