/**
 * Danh mục MỌI khoá của bảng cau_hinh: mô tả tiếng Việt + quyền sửa lẻ từng khoá.
 *
 * Ai đọc bảng này (đã đối chiếu mã Java, chỉ đọc):
 *   - HTBB/src/server/CauHinh.java#napLai — đọc lại TOÀN BỘ bảng mỗi 10 giây (CHU_KY_NAP_LAI_MS),
 *     áp vào biến tĩnh của server. Các khoá sv.* ở đây.
 *   - com.htbb.pokemon.LauDai#apDungCauHinh (gọi trong napLai) — mọi khoá có dấu chấm KHÔNG bắt đầu
 *     bằng sv./db./api./limit. trở thành "đè số liệu dài hạn"; đọc qua LauDai.so(khoa, macDinh) =
 *     Integer.parseInt, sai/thiếu -> mặc định trong ThongSoLauDai.java. Dùng ở KinhTeLauDai.java,
 *     LauDaiNhanVat.java, GiftcodeLauDai.java.
 *   - lau_dai.* do chính game ghi (trạng thái chuyển đổi dài hạn) — chỉ xem.
 *
 * `suaLe`: được sửa lẻ bằng POST /api/admin/cauhinh/khoa (whitelist). Khoá phức tạp (ngưỡng màu hàng,
 * giờ chiếm thành, danh sách GM...) chỉ sửa qua thẻ riêng trên trang (POST /api/admin/cauhinh/luu)
 * vì cần kiểm chéo nhiều khoá một lúc.
 *
 * File thuần (không import gì): dùng ở server, trang, và tests/cau-hinh-danh-muc.test.ts.
 */

export type KieuCauHinh = 'so' | 'co' | 'chuoi' | 'csv' | 'ngay'

export interface MucCauHinh {
  ten: string
  moTa: string
  nhom: string
  kieu: KieuCauHinh
  /** Chỉ với kieu 'so': khoảng hợp lệ (đóng). */
  min?: number
  max?: number
  /** Giá trị game dùng khi khoá vắng mặt. */
  macDinh?: string
  /** Cho sửa lẻ qua whitelist. */
  suaLe: boolean
  /** Tên thẻ trên trang để sửa (khi suaLe = false nhưng vẫn sửa được). */
  the?: string
  /** Nguồn đọc trong mã game. */
  nguon: string
}

const CH = 'server/CauHinh.java'
const KT = 'KinhTeLauDai.java'

export const DANH_MUC_CAU_HINH: Record<string, MucCauHinh> = {
  // ---- Rơi đồ (Monster.PC_TILE_*, kẹp 0..100 bên Java) ----
  'sv.tileXu': { ten: 'Tỉ lệ rơi xu (%)', moTa: '% so với tỉ lệ gốc. 100 = như cũ, 0 = tắt.', nhom: 'Rơi đồ', kieu: 'so', min: 0, max: 100, suaLe: true, the: 'Tỷ lệ rơi đồ', nguon: CH },
  'sv.tileLuong': { ten: 'Tỉ lệ rơi lượng (%)', moTa: '% so với tỉ lệ gốc.', nhom: 'Rơi đồ', kieu: 'so', min: 0, max: 100, suaLe: true, the: 'Tỷ lệ rơi đồ', nguon: CH },
  'sv.tileItemSuKien': { ten: 'Tỉ lệ rơi đồ sự kiện (%)', moTa: '% so với tỉ lệ gốc.', nhom: 'Rơi đồ', kieu: 'so', min: 0, max: 100, suaLe: true, the: 'Tỷ lệ rơi đồ', nguon: CH },
  'sv.tilePotion': { ten: 'Tỉ lệ rơi bình thuốc (%)', moTa: '% so với tỉ lệ gốc.', nhom: 'Rơi đồ', kieu: 'so', min: 0, max: 100, suaLe: true, the: 'Tỷ lệ rơi đồ', nguon: CH },
  'sv.tileNgoc': { ten: 'Tỉ lệ rơi ngọc (%)', moTa: '% so với tỉ lệ gốc.', nhom: 'Rơi đồ', kieu: 'so', min: 0, max: 100, suaLe: true, the: 'Tỷ lệ rơi đồ', nguon: CH },
  'sv.tileItem': { ten: 'Tỉ lệ rơi đồ thường (%)', moTa: '% so với tỉ lệ gốc.', nhom: 'Rơi đồ', kieu: 'so', min: 0, max: 100, suaLe: true, the: 'Tỷ lệ rơi đồ', nguon: CH },
  'sv.chiaXuRoi': { ten: 'Chia số xu rơi', moTa: 'Xu rơi = xu gốc chia số này (Map.DIV_DROP_XU, tối thiểu 1).', nhom: 'Rơi đồ', kieu: 'so', min: 1, max: 1000000, macDinh: '10', suaLe: true, the: 'Tỷ lệ rơi đồ', nguon: CH },
  // ---- EXP ----
  'sv.tileExpLv50': { ten: 'EXP quái từ cấp 50 (%)', moTa: 'Monster.PC_TILE_EXP_LV50.', nhom: 'EXP', kieu: 'so', min: 0, max: 100, macDinh: '10', suaLe: true, the: 'EXP quái', nguon: CH },
  'sv.tileExpDuoiLv50': { ten: 'EXP quái dưới cấp 50 (%)', moTa: 'Monster.PC_TILE_EXP_DUOI_LV50.', nhom: 'EXP', kieu: 'so', min: 0, max: 100, macDinh: '50', suaLe: true, the: 'EXP quái', nguon: CH },
  'sv.expvantieu': { ten: 'EXP giao hàng', moTa: '1 = bật (TeamServer.ExpVantieu).', nhom: 'EXP', kieu: 'co', macDinh: '0', suaLe: true, the: 'EXP phụ', nguon: CH },
  'sv.Luckyexp': { ten: 'EXP may mắn', moTa: '1 = bật (TeamServer.LuckyExp).', nhom: 'EXP', kieu: 'co', macDinh: '0', suaLe: true, the: 'EXP phụ', nguon: CH },
  'sv.ExpQua': { ten: 'EXP quà', moTa: '1 = bật (TeamServer.ExpQua).', nhom: 'EXP', kieu: 'co', macDinh: '0', suaLe: true, the: 'EXP phụ', nguon: CH },
  // ---- Giao hàng (CSV kiểm chéo) ----
  'sv.tieuNguongMau': { ten: 'Ngưỡng màu hàng (nhận mới)', moTa: '5 ngưỡng cộng dồn trên thang 100000 (Trắng..Vàng, còn lại Tím).', nhom: 'Giao hàng', kieu: 'csv', suaLe: false, the: 'Giao Hàng', nguon: CH },
  'sv.tieuHeSoExp': { ten: 'Hệ số EXP giao hàng chung', moTa: '% nhân, 200 = ×2.', nhom: 'Giao hàng', kieu: 'so', suaLe: false, the: 'Giao Hàng', nguon: CH },
  'sv.tieuPcExpTheoMau': { ten: 'Hệ số EXP theo màu', moTa: '7 số ×100.', nhom: 'Giao hàng', kieu: 'csv', suaLe: false, the: 'Giao Hàng', nguon: CH },
  'sv.tieuPermilleCaoCap': { ten: 'Tỉ lệ hàng cao cấp', moTa: '9 số phần nghìn (3 khung cấp × 3 màu).', nhom: 'Giao hàng', kieu: 'csv', suaLe: false, the: 'Giao Hàng', nguon: CH },
  'sv.doiTieuNguongMauXu': { ten: 'Ngưỡng màu đổi hàng (xu)', moTa: '5 ngưỡng cộng dồn.', nhom: 'Giao hàng', kieu: 'csv', suaLe: false, the: 'Giao Hàng', nguon: CH },
  'sv.doiTieuNguongMauLuong': { ten: 'Ngưỡng màu đổi hàng (lượng)', moTa: '5 ngưỡng cộng dồn.', nhom: 'Giao hàng', kieu: 'csv', suaLe: false, the: 'Giao Hàng', nguon: CH },
  // ---- Chiếm thành ----
  'sv.gioChiemThanh': { ten: 'Giờ chiếm Gym', moTa: '0–23, −1 = theo lịch mặc định.', nhom: 'Chiếm Gym', kieu: 'so', suaLe: false, the: 'Giờ chiếm Gym', nguon: CH },
  'sv.phutChiemThanh': { ten: 'Phút chiếm Gym', moTa: '0–59.', nhom: 'Chiếm Gym', kieu: 'so', suaLe: false, the: 'Giờ chiếm Gym', nguon: CH },
  'sv.ngayChiemThanh': { ten: 'Ngày chiếm Gym', moTa: 'Mon,Tue... hoặc yyyy-MM-dd.', nhom: 'Chiếm Gym', kieu: 'chuoi', suaLe: false, the: 'Giờ chiếm Gym', nguon: CH },
  // ---- Sự kiện ----
  'sv.suKienTuKhoiDongLai': { ten: 'Sự kiện tự bật lại sau khởi động', moTa: '1 = bật lại các sự kiện đang bật khi server khởi động.', nhom: 'Sự kiện', kieu: 'co', macDinh: '1', suaLe: true, the: 'Lịch hẹn sự kiện', nguon: CH },
  'sv.suKienGomGiay': { ten: 'Gom lệnh sự kiện (giây)', moTa: 'Java kẹp 30–3600.', nhom: 'Sự kiện', kieu: 'so', min: 30, max: 3600, macDinh: '120', suaLe: true, the: 'Lịch hẹn sự kiện', nguon: CH },
  'sv.suKienBaoTruocGiay': { ten: 'Báo trước sự kiện (giây)', moTa: 'Java kẹp 10–600.', nhom: 'Sự kiện', kieu: 'so', min: 10, max: 600, macDinh: '60', suaLe: true, the: 'Lịch hẹn sự kiện', nguon: CH },
  'sv.worldCupBatDau': { ten: 'World Cup bắt đầu', moTa: 'Để trống = tắt sự kiện World Cup (SuKienWorldCup.datThoiGian).', nhom: 'Sự kiện', kieu: 'chuoi', suaLe: false, nguon: CH },
  'sv.worldCupKetThuc': { ten: 'World Cup kết thúc', moTa: 'Để trống = tắt.', nhom: 'Sự kiện', kieu: 'chuoi', suaLe: false, nguon: CH },
  'sv.worldCupHanNhanQua': { ten: 'World Cup hạn nhận quà', moTa: 'Để trống = tắt.', nhom: 'Sự kiện', kieu: 'chuoi', suaLe: false, nguon: CH },
  // ---- Khác ----
  'sv.maxCharPerAccount': { ten: 'Số Pokémon tối đa / tài khoản', moTa: 'Java kẹp 1–3.', nhom: 'Khác', kieu: 'so', min: 1, max: 3, macDinh: '3', suaLe: true, the: 'Khác', nguon: CH },
  'sv.adminAccounts': { ten: 'Danh sách GM trong game', moTa: 'Tên Pokémon có quyền GM, cách nhau dấu phẩy. Chỉ sửa ở thẻ "Khác".', nhom: 'Khác', kieu: 'chuoi', suaLe: false, the: 'Khác', nguon: CH },
  'sv.website': { ten: 'Địa chỉ website', moTa: 'Hiện trong game.', nhom: 'Khác', kieu: 'chuoi', suaLe: false, the: 'Khác', nguon: CH },
  'sv.linkTaiGame': { ten: 'Link tải game', moTa: 'Hiện trong game.', nhom: 'Khác', kieu: 'chuoi', suaLe: false, the: 'Khác', nguon: CH },
  'sv.choPhepTaoTenGM': { ten: 'Cho tạo tên trùng GM', moTa: 'Chỉ bật tạm thời.', nhom: 'Khác', kieu: 'co', macDinh: '0', suaLe: true, the: 'Khác', nguon: CH },
  'sv.testMode': { ten: 'Chế độ thử (testMode)', moTa: 'Chỉ bật tạm thời — mã test cấp tài nguyên miễn phí.', nhom: 'Khác', kieu: 'co', macDinh: '0', suaLe: true, the: 'Khác', nguon: CH },
  'sv.testCode': { ten: 'Mã test', moTa: 'Chuỗi ASCII gõ trong game.', nhom: 'Khác', kieu: 'chuoi', suaLe: false, the: 'Khác', nguon: CH },

  // ---- Kinh tế dài hạn (LauDai.so, đè ThongSoLauDai) ----
  'roi.xu.phan_tram': { ten: 'Tỉ lệ một con quái rơi xu (%)', moTa: 'Xác suất rơi xu; số xu mỗi lần rơi tự chia ngược để kỳ vọng xu/quái không đổi. Java kẹp 0–100.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 100, macDinh: '30', suaLe: true, nguon: KT },
  'roi.da_nang_cap.phan_nghin': { ten: 'Tỉ lệ rơi đá nâng cấp (‰)', moTa: 'Phần nghìn mỗi quái khi |lệch cấp| ≤ 5.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 200, macDinh: '15', suaLe: true, nguon: KT },
  'exp.nang_luong_phut': { ten: 'Phút năng lượng 100% / ngày', moTa: 'LauDaiNhanVat: số phút EXP/rơi đầy đủ mỗi ngày.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 1440, macDinh: '90', suaLe: true, nguon: 'LauDaiNhanVat.java' },
  'exp.nang_luong_tich_phut': { ten: 'Năng lượng tích luỹ tối đa (phút)', moTa: 'Phút năng lượng dồn tối đa khi không chơi.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 4320, macDinh: '180', suaLe: true, nguon: 'LauDaiNhanVat.java' },
  'thue.cho': { ten: 'Thuế chợ ký gửi (%)', moTa: 'Đốt khỏi nền kinh tế khi bán được.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 50, macDinh: '6', suaLe: true, nguon: KT },
  'thue.dang_ban': { ten: 'Phí đăng bán (%)', moTa: 'Đốt, không hoàn; tối thiểu 1 xu.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 20, macDinh: '1', suaLe: true, nguon: KT },
  'thue.chuyen_luong': { ten: 'Phí chuyển lượng qua NPC (%)', moTa: 'Làm tròn lên, tối thiểu 1 khi > 0.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 50, macDinh: '5', suaLe: true, nguon: KT },
  'thue.tran_chuyen_xu_gio': { ten: 'Trần chuyển xu trực tiếp / ngày (giờ thu nhập)', moTa: 'Bội số thu nhập 1 giờ ở cấp người gửi.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 100, macDinh: '5', suaLe: true, nguon: KT },
  'phi.dich_chuyen_phan_tram_gio': { ten: 'Phí dịch chuyển (% thu nhập 1 giờ)', moTa: 'Tối thiểu 1 xu.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 100, macDinh: '3', suaLe: true, nguon: KT },
  'phi.hoi_sinh_phan_tram_gio': { ten: 'Phí hồi sinh (% thu nhập 1 giờ)', moTa: 'Tối thiểu 1 xu.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 100, macDinh: '5', suaLe: true, nguon: KT },
  'phi.ban_npc_phan_tram': { ten: 'Giá bán đồ cho NPC (% giá)', moTa: 'Không cộng theo cấp cường hoá.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 100, macDinh: '5', suaLe: true, nguon: KT },
  'tieu_hao.sua_do_phan_tram_thu_nhap': { ten: 'Trần phí sửa đồ (% thu nhập 1 giờ / món)', moTa: 'Phí sửa = min(công thức cũ, trần này × số món).', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 100, macDinh: '8', suaLe: true, nguon: KT },
  'giftcode.toi_da_thang': { ten: 'Giftcode tối đa / tháng', moTa: 'Số giftcode dài hạn một Pokémon nhập được mỗi tháng.', nhom: 'Kinh tế dài hạn', kieu: 'so', min: 0, max: 20, macDinh: '2', suaLe: true, nguon: 'GiftcodeLauDai.java' },

  // ---- Trạng thái do game ghi ----
  'lau_dai.ngay_khai_truong': { ten: 'Ngày khai trương', moTa: 'Ngày 0 của lịch mùa PvP và lịch sự kiện (và của trần cấp theo mùa nếu bật sv.tran_cap_theo_mua). Game tự ghi.', nhom: 'Trạng thái game', kieu: 'ngay', suaLe: false, nguon: 'LauDai.java' },
  'lau_dai.dang_chuyen': { ten: 'Đang chuyển đổi dài hạn', moTa: 'Game tự ghi.', nhom: 'Trạng thái game', kieu: 'chuoi', suaLe: false, nguon: 'LauDai.java' },
  'lau_dai.thang_exp': { ten: 'Thang EXP đang dùng', moTa: 'moi / cu. Game tự ghi.', nhom: 'Trạng thái game', kieu: 'chuoi', suaLe: false, nguon: 'LauDai.java' },
  'lau_dai.lan_chuyen': { ten: 'Số lần chuyển đổi', moTa: 'Game tự ghi.', nhom: 'Trạng thái game', kieu: 'so', suaLe: false, nguon: 'LauDai.java' },
}

/** Khoá được sửa lẻ (whitelist). */
export function khoaSuaLeDuoc(khoa: string): boolean {
  return Object.prototype.hasOwnProperty.call(DANH_MUC_CAU_HINH, khoa) && DANH_MUC_CAU_HINH[khoa]!.suaLe
}

export type KetQuaKiemKhoa = { ok: true, giaTri: string } | { ok: false, loi: string }

/**
 * Kiểm giá trị cho một khoá sửa lẻ. KHÔNG kẹp ngầm: ngoài khoảng là báo lỗi.
 * Số nguyên viết chuẩn (không số 0 đứng đầu, không thập phân) vì game đọc bằng Integer.parseInt.
 */
export function kiemGiaTriKhoa(khoa: unknown, giaTri: unknown): KetQuaKiemKhoa {
  if (typeof khoa !== 'string' || !khoaSuaLeDuoc(khoa)) return { ok: false, loi: 'Khoá này không được sửa lẻ.' }
  const m = DANH_MUC_CAU_HINH[khoa]!
  const s = typeof giaTri === 'number' && Number.isFinite(giaTri) ? String(giaTri) : typeof giaTri === 'boolean' ? (giaTri ? '1' : '0') : typeof giaTri === 'string' ? giaTri.trim() : ''
  if (m.kieu === 'co') {
    if (s !== '0' && s !== '1') return { ok: false, loi: `${m.ten}: chỉ nhận 0 (tắt) hoặc 1 (bật).` }
    return { ok: true, giaTri: s }
  }
  if (m.kieu === 'so') {
    if (!/^-?(0|[1-9]\d{0,9})$/.test(s)) return { ok: false, loi: `${m.ten}: phải là số nguyên.` }
    const n = Number(s)
    if (m.min !== undefined && n < m.min) return { ok: false, loi: `${m.ten}: nhỏ nhất ${m.min}.` }
    if (m.max !== undefined && n > m.max) return { ok: false, loi: `${m.ten}: lớn nhất ${m.max}.` }
    return { ok: true, giaTri: String(n) }
  }
  return { ok: false, loi: 'Kiểu khoá chưa hỗ trợ sửa lẻ.' }
}
