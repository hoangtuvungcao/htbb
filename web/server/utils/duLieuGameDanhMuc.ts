/**
 * DANH SÁCH TRẮNG các bộ dữ liệu game xem được ở /admin/du-lieu-game (chỉ đọc).
 *
 * Tên bảng / cột ở đây là HẰNG SỐ trong mã — route chỉ nhận `ma` (khoá của DS_DU_LIEU_GAME), không
 * bao giờ ghép chuỗi người dùng gõ vào SQL. Tìm kiếm `q` đi qua tham số `?`.
 * Nhãn cột lấy từ tên cột + chú thích trong HTBB/sql/*.sql + mã Java đọc bảng.
 * Đơn vị tiền: xu (gold), lượng, lượng khoá.
 *
 * File thuần (không import gì) — tests/du-lieu-game.test.ts kiểm danh mục.
 */

export interface CotDuLieu {
  key: string
  label: string
  so?: boolean
  /** Đổi mã -> chữ khi hiển thị (giữ mã gốc trong ngoặc). */
  ma?: Record<string, string>
  /** Ẩn trên màn hình hẹp. */
  anMobile?: boolean
}

export interface BoDuLieu {
  ma: string
  tab: string
  ten: string
  moTa: string
  /** 'sql' = bảng htbb_db; 'ban_do' = shared/data/cam-nang.json + đếm từ CSDL. */
  loai: 'sql' | 'ban_do'
  bang?: string
  cot: CotDuLieu[]
  /** Cột dùng cho tìm kiếm phía server (LIKE). */
  tim?: string[]
  /** ORDER BY cố định (chỉ tên cột trong `cot`). */
  sapXep?: string
}

export const TAB_DU_LIEU_GAME: { ma: string, ten: string, icon: string }[] = [
  { ma: 'vat_pham', ten: 'Vật phẩm', icon: 'i-lucide-package' },
  { ma: 'quai', ten: 'Quái / Pokémon', icon: 'i-lucide-paw-print' },
  { ma: 'ban_do', ten: 'Bản đồ', icon: 'i-lucide-map' },
  { ma: 'nhiem_vu', ten: 'Nhiệm vụ', icon: 'i-lucide-scroll-text' },
  { ma: 'su_kien', ten: 'Sự kiện / lịch', icon: 'i-lucide-calendar-days' },
  { ma: 'cua_hang', ten: 'Cửa hàng / đổi', icon: 'i-lucide-store' },
  { ma: 'roi', ten: 'Rơi đồ', icon: 'i-lucide-gift' },
  { ma: 'gym', ten: 'Gym', icon: 'i-lucide-medal' },
  { ma: 'boss', ten: 'Boss thế giới', icon: 'i-lucide-skull' },
  { ma: 'khac', ten: 'Khác', icon: 'i-lucide-layers' },
]

const TIEN_CHU: Record<string, string> = { xu: 'Xu', luong: 'Lượng', luong_khoa: 'Lượng khoá', huy_chuong_gym: 'Huy chương gym', khong_ban: 'Không bán' }
const TIEN_SO: Record<string, string> = { 0: 'Lượng', 1: 'Xu', 2: 'Lượng khoá', 9: 'Ngừng bán' }
const CO: Record<string, string> = { 0: 'Không', 1: 'Có' }
/** Hệ Pokémon (shared/data/pokedex.json `he`): data_monster.he / he2. −1 = không có. */
const HE: Record<string, string> = Object.fromEntries(['Thường', 'Lửa', 'Nước', 'Điện', 'Cỏ', 'Băng', 'Giác đấu', 'Độc', 'Đất', 'Bay', 'Siêu linh', 'Bọ', 'Đá', 'Ma', 'Rồng', 'Bóng tối', 'Thép', 'Tiên'].map((t, i) => [String(i), t]))

const c = (key: string, label: string, them: Partial<CotDuLieu> = {}): CotDuLieu => ({ key, label, ...them })
const n = (key: string, label: string, them: Partial<CotDuLieu> = {}): CotDuLieu => ({ key, label, so: true, ...them })

export const DS_DU_LIEU_GAME: BoDuLieu[] = [
  // ------------------------------------------------------------------ Vật phẩm
  {
    ma: 'vat-pham', tab: 'vat_pham', ten: 'Vật phẩm gốc', moTa: 'data_item — mọi mẫu vật phẩm/trang bị client biết. Giá = xu bán ở NPC.', loai: 'sql', bang: 'data_item',
    cot: [n('id', 'ID'), c('name', 'Tên'), n('type', 'Loại'), n('level', 'Cấp'), n('he', 'Hệ', { anMobile: true }), n('clazz', 'Nghề', { anMobile: true }), n('durable', 'Độ bền', { anMobile: true }), n('price', 'Giá (xu)'), n('pricecu', 'Giá cũ (xu)', { anMobile: true }), n('idUpLevel', 'ID khi nâng cấp', { anMobile: true })],
    tim: ['name', 'id'], sapXep: 'id',
  },
  {
    ma: 'vat-pham-dai-han', tab: 'vat_pham', ten: 'Vật phẩm dài hạn', moTa: 'data_vat_pham_lau_dai — công dụng & giới hạn theo thiết kế dài hạn.', loai: 'sql', bang: 'data_vat_pham_lau_dai',
    cot: [n('id_item', 'ID vật phẩm'), c('ma', 'Mã'), c('ten', 'Tên'), c('nhom', 'Nhóm'), c('hieu_ung', 'Hiệu ứng'), c('gioi_han', 'Giới hạn'), n('cap_tu', 'Từ cấp'), n('cap_den', 'Đến cấp')],
    tim: ['ten', 'ma', 'nhom'], sapXep: 'id_item',
  },
  {
    ma: 'noi-dung-vat-pham', tab: 'vat_pham', ten: 'Nội dung vật phẩm tiêu hao', moTa: 'data_noi_dung_vat_pham — giá, nguồn, ngân sách giờ thu nhập.', loai: 'sql', bang: 'data_noi_dung_vat_pham',
    cot: [c('ma', 'Mã'), c('ten', 'Tên'), c('nhom', 'Nhóm'), c('hieu_ung', 'Hiệu ứng'), n('cap_tu', 'Từ cấp'), n('cap_den', 'Đến cấp'), n('gia', 'Giá'), c('tien', 'Tiền', { ma: TIEN_CHU }), n('gia_tren_thu_nhap_gio', 'Giá / thu nhập 1 giờ', { anMobile: true }), c('nguon', 'Nguồn', { anMobile: true }), c('gioi_han', 'Giới hạn', { anMobile: true }), c('trang_thai', 'Trạng thái')],
    tim: ['ten', 'ma', 'nhom', 'nguon'],
  },
  {
    ma: 'thuoc', tab: 'vat_pham', ten: 'Bình thuốc', moTa: 'data_potion — thuốc hồi phục.', loai: 'sql', bang: 'data_potion',
    cot: [n('id', 'ID'), c('name', 'Tên'), c('name2', 'Tên phụ', { anMobile: true }), n('price', 'Giá (xu)'), n('recovered', 'Hồi phục'), n('delay', 'Hồi chiêu', { anMobile: true }), n('isTrade', 'Giao dịch được', { ma: CO })],
    tim: ['name', 'name2', 'id'], sapXep: 'id',
  },
  {
    ma: 'trang-bi', tab: 'vat_pham', ten: 'Trang bị dài hạn', moTa: 'data_noi_dung_trang_bi — bậc, chỉ số, giá xu, phân rã.', loai: 'sql', bang: 'data_noi_dung_trang_bi',
    cot: [c('ma', 'Mã'), c('ten', 'Tên'), c('ten_bac', 'Bậc'), c('o', 'Ô'), n('id_item', 'ID vật phẩm', { anMobile: true }), n('cap_yeu_cau', 'Cấp yêu cầu'), n('ngay_mo', 'Mở từ ngày', { anMobile: true }), n('tan_cong', 'Tấn công'), n('phong_thu', 'Phòng thủ'), n('hp', 'HP'), c('chi_so_phu', 'Chỉ số phụ', { anMobile: true }), n('gia_xu', 'Giá (xu)'), n('ban_npc_xu', 'Bán NPC (xu)', { anMobile: true }), c('nguon', 'Nguồn', { anMobile: true }), c('trang_thai', 'Trạng thái')],
    tim: ['ten', 'ma', 'ten_bac', 'nguon'],
  },
  {
    ma: 'nguyen-lieu', tab: 'vat_pham', ten: 'Nguyên liệu', moTa: 'data_noi_dung_nguyen_lieu.', loai: 'sql', bang: 'data_noi_dung_nguyen_lieu',
    cot: [c('ma', 'Mã'), c('ten', 'Tên'), c('nhom', 'Nhóm'), c('dung_cho', 'Dùng cho'), c('nguon', 'Nguồn'), n('gia', 'Giá'), c('tien', 'Tiền', { ma: TIEN_CHU })],
    tim: ['ten', 'ma', 'nhom'],
  },
  {
    ma: 'vat-pham-nhiem-vu', tab: 'vat_pham', ten: 'Vật phẩm nhiệm vụ', moTa: 'data_vat_pham_nhiem_vu.', loai: 'sql', bang: 'data_vat_pham_nhiem_vu',
    cot: [n('id', 'ID'), c('ten', 'Tên')], tim: ['ten', 'id'], sapXep: 'id',
  },
  // ------------------------------------------------------------------ Quái
  {
    ma: 'quai', tab: 'quai', ten: 'Quái gốc', moTa: 'data_monster — chỉ số gốc; EXP & xu rơi mỗi con (rcvxp / rcvgold).', loai: 'sql', bang: 'data_monster',
    cot: [n('id', 'ID'), c('name', 'Tên'), n('level', 'Cấp'), n('he', 'Hệ', { ma: HE }), n('maxhp', 'HP'), n('attack', 'Tấn công'), n('defent', 'Phòng thủ'), n('accurate', 'Chính xác', { anMobile: true }), n('dodge', 'Né', { anMobile: true }), n('rcvxp', 'EXP nhận'), n('rcvgold', 'Xu nhận'), n('dex', 'Số Pokédex', { anMobile: true }), n('isuse', 'Đang dùng', { ma: CO, anMobile: true })],
    tim: ['name', 'id'], sapXep: 'id',
  },
  {
    ma: 'quai-dai-han', tab: 'quai', ten: 'Quái dài hạn (cũ → mới)', moTa: 'data_monster_lau_dai — chỉ số sau khi cân bằng lại.', loai: 'sql', bang: 'data_monster_lau_dai',
    cot: [n('id', 'ID'), c('ten', 'Tên'), n('cap', 'Cấp'), n('maxhp_cu', 'HP cũ', { anMobile: true }), n('maxhp', 'HP'), n('attack_cu', 'Tấn công cũ', { anMobile: true }), n('attack', 'Tấn công'), n('defent_cu', 'Phòng thủ cũ', { anMobile: true }), n('defent', 'Phòng thủ'), n('rcvxp_cu', 'EXP cũ', { anMobile: true }), n('rcvxp', 'EXP'), n('rcvgold_cu', 'Xu cũ', { anMobile: true }), n('rcvgold', 'Xu')],
    tim: ['ten', 'id'], sapXep: 'id',
  },
  {
    ma: 'quai-moi', tab: 'quai', ten: 'Quái mới', moTa: 'data_noi_dung_quai_moi — Pokémon thêm ở thiết kế mới.', loai: 'sql', bang: 'data_noi_dung_quai_moi',
    cot: [n('id', 'ID'), c('name', 'Tên'), c('phan_loai', 'Phân loại'), n('level', 'Cấp'), n('he', 'Hệ', { ma: HE }), n('maxhp', 'HP'), n('attack', 'Tấn công'), n('defent', 'Phòng thủ'), n('rcvxp', 'EXP nhận'), n('rcvgold', 'Xu nhận'), n('dex', 'Số Pokédex', { anMobile: true })],
    tim: ['name', 'phan_loai', 'id'], sapXep: 'id',
  },
  {
    ma: 'quai-tinh-anh', tab: 'quai', ten: 'Quái tinh anh', moTa: 'data_noi_dung_quai_tinh_anh — biến thể mạnh, nhân rơi.', loai: 'sql', bang: 'data_noi_dung_quai_tinh_anh',
    cot: [n('id_goc', 'ID gốc'), c('ten', 'Tên'), n('cap', 'Cấp'), c('ti_le_xuat_hien', 'Tỉ lệ xuất hiện'), n('kich_thuoc_phan_tram', 'Kích thước (%)', { anMobile: true }), n('hp', 'HP'), n('tan_cong', 'Tấn công'), n('phong_thu', 'Phòng thủ'), n('exp', 'EXP'), n('nhan_roi', 'Nhân rơi'), n('ttk_giay', 'Thời gian hạ (giây)', { anMobile: true })],
    tim: ['ten', 'id_goc'],
  },
  {
    ma: 'quai-dai-cap', tab: 'quai', ten: 'Quái theo dải cấp', moTa: 'data_noi_dung_quai_dai_cap — loài có sẵn theo dải cấp & môi trường.', loai: 'sql', bang: 'data_noi_dung_quai_dai_cap',
    cot: [c('dai', 'Dải'), n('cap_tu', 'Từ cấp'), n('cap_den', 'Đến cấp'), c('loai_co_san', 'Loài có sẵn'), n('so_loai', 'Số loài'), c('he', 'Hệ'), c('moi_truong', 'Môi trường'), c('de_xuat_moi', 'Đề xuất mới', { anMobile: true })],
    tim: ['dai', 'loai_co_san', 'moi_truong'],
  },
  {
    ma: 'cap-do', tab: 'quai', ten: 'Bảng cấp độ', moTa: 'data_cap_do_lau_dai — EXP lên cấp, quái/giờ, xu/quái (cơ sở của chỉ số giá).', loai: 'sql', bang: 'data_cap_do_lau_dai',
    cot: [n('cap', 'Cấp'), n('exp_len_cap', 'EXP lên cấp'), n('gio_tich_luy_phan_nghin', 'Giờ tích luỹ (‰)'), n('mob_exp', 'EXP/quái'), n('quai_moi_gio', 'Quái/giờ'), n('xu_moi_quai', 'Xu/quái'), n('exp_cu', 'EXP cũ', { anMobile: true })],
    tim: ['cap'], sapXep: 'cap',
  },
  // ------------------------------------------------------------------ Bản đồ
  {
    ma: 'ban-do', tab: 'ban_do', ten: 'Bản đồ', moTa: 'Danh sách bản đồ (docs/pokemon/thiet_ke_map.csv, đồng bộ vào cẩm nang) + số nhiệm vụ đặt ở map + số Pokémon đang lưu vị trí ở map.', loai: 'ban_do',
    cot: [n('id', 'ID map'), c('ten', 'Tên'), c('loai', 'Loại'), c('vung', 'Vùng'), n('soNhiemVu', 'Nhiệm vụ đặt ở đây'), n('soNhanVat', 'Pokémon đang ở'), c('anh', 'Có ảnh xem trước', { ma: { true: 'Có', false: 'Không' }, anMobile: true })],
  },
  // ------------------------------------------------------------------ Nhiệm vụ
  {
    ma: 'nhiem-vu', tab: 'nhiem_vu', ten: 'Nhiệm vụ dài hạn', moTa: 'data_quest_lau_dai — mọi nhiệm vụ, mục tiêu, thưởng (EXP, xu, lượng khoá, lượng).', loai: 'sql', bang: 'data_quest_lau_dai',
    cot: [c('ma', 'Mã'), c('loai', 'Loại'), c('chuong', 'Chương', { anMobile: true }), c('ten', 'Tên'), c('ten_npc', 'NPC giao'), c('dia_danh', 'Địa danh', { anMobile: true }), n('cap_toi_thieu', 'Cấp tối thiểu'), n('ngay_mo', 'Mở từ ngày', { anMobile: true }), c('kieu_muc_tieu', 'Kiểu mục tiêu', { anMobile: true }), c('muc_tieu_ten', 'Mục tiêu'), n('so_luong', 'Số lượng'), c('lap_lai', 'Lặp lại', { anMobile: true }), n('exp', 'EXP'), n('xu', 'Xu'), n('luong_khoa', 'Lượng khoá'), n('luong', 'Lượng'), c('vat_pham', 'Vật phẩm', { anMobile: true })],
    tim: ['ma', 'ten', 'ten_npc', 'dia_danh', 'muc_tieu_ten', 'loai'],
  },
  {
    ma: 'nhiem-vu-noi-dung', tab: 'nhiem_vu', ten: 'Lời thoại & gợi ý nhiệm vụ', moTa: 'data_nhiem_vu_noi_dung — mô tả, mục tiêu, gợi ý, dòng thưởng.', loai: 'sql', bang: 'data_nhiem_vu_noi_dung',
    cot: [c('ma', 'Mã'), c('loai', 'Loại'), c('nhom', 'Nhóm', { anMobile: true }), c('map_id', 'Map'), c('muc_tieu_text', 'Mục tiêu'), c('goi_y', 'Gợi ý', { anMobile: true }), c('dong_thuong', 'Thưởng'), n('phut', 'Phút ước tính', { anMobile: true }), n('an', 'Ẩn', { ma: CO, anMobile: true })],
    tim: ['ma', 'muc_tieu_text', 'goi_y', 'loai'],
  },
  {
    ma: 'nhiem-vu-kieu', tab: 'nhiem_vu', ten: 'Kiểu nhiệm vụ', moTa: 'data_nhiem_vu_kieu — cơ chế từng kiểu mục tiêu.', loai: 'sql', bang: 'data_nhiem_vu_kieu',
    cot: [c('ma', 'Mã'), c('ten', 'Tên'), c('co_che', 'Cơ chế'), c('trang_thai', 'Trạng thái'), n('so_dong', 'Số nhiệm vụ')],
    tim: ['ma', 'ten', 'co_che'],
  },
  {
    ma: 'nhiem-vu-cu', tab: 'nhiem_vu', ten: 'Nhiệm vụ cũ (data_quest)', moTa: 'data_quest — hệ nhiệm vụ cũ; gold = xu, luongLock = lượng khoá.', loai: 'sql', bang: 'data_quest',
    cot: [n('id', 'ID'), c('name', 'Tên'), n('lv', 'Cấp'), n('npcrcv', 'NPC nhận', { anMobile: true }), n('npcres', 'NPC trả', { anMobile: true }), n('gold', 'Xu'), n('exp', 'EXP'), n('luong', 'Lượng'), n('luongLock', 'Lượng khoá')],
    tim: ['name', 'id'], sapXep: 'id',
  },
  {
    ma: 'nhiem-vu-moi-cu', tab: 'nhiem_vu', ten: 'Nhiệm vụ data_quest_moi', moTa: 'data_quest_moi — bản mở rộng cùng định dạng data_quest.', loai: 'sql', bang: 'data_quest_moi',
    cot: [n('id', 'ID'), c('name', 'Tên'), n('lv', 'Cấp'), n('npcrcv', 'NPC nhận', { anMobile: true }), n('npcres', 'NPC trả', { anMobile: true }), n('gold', 'Xu'), n('exp', 'EXP'), n('luong', 'Lượng'), n('luongLock', 'Lượng khoá')],
    tim: ['name', 'id'], sapXep: 'id',
  },
  // ------------------------------------------------------------------ Sự kiện
  {
    ma: 'su-kien-nam', tab: 'su_kien', ten: 'Lịch sự kiện 52 tuần', moTa: 'data_su_kien_nam — năm 1 / năm 2, ngân sách giờ thu nhập, phần xu.', loai: 'sql', bang: 'data_su_kien_nam',
    cot: [n('tuan', 'Tuần'), n('thang', 'Tháng'), c('ten_nam1', 'Tên năm 1'), c('ten_nam2', 'Tên năm 2', { anMobile: true }), c('loai', 'Loại'), c('co_che', 'Cơ chế', { anMobile: true }), n('so_ngay', 'Số ngày'), c('ma_su_kien', 'Mã sự kiện', { anMobile: true }), n('ngan_sach_gio', 'Ngân sách (giờ)'), n('phan_xu_phan_tram', 'Phần xu (%)'), c('phan_thuong', 'Phần thưởng', { anMobile: true })],
    tim: ['ten_nam1', 'ten_nam2', 'loai', 'ma_su_kien'], sapXep: 'tuan',
  },
  {
    ma: 'su-kien-vat-pham', tab: 'su_kien', ten: 'Vật phẩm sự kiện', moTa: 'data_noi_dung_su_kien_vat_pham — trần mỗi tuần, đổi lấy gì.', loai: 'sql', bang: 'data_noi_dung_su_kien_vat_pham',
    cot: [n('tuan', 'Tuần'), c('su_kien', 'Sự kiện'), c('loai', 'Loại'), c('vat_pham', 'Vật phẩm'), n('so_luong_toi_da_tuan', 'Tối đa / tuần'), n('gio_thu_nhap_moi_cai', 'Giờ thu nhập / cái', { anMobile: true }), n('ngan_sach_gio', 'Ngân sách (giờ)', { anMobile: true }), n('phan_xu_phan_tram', 'Phần xu (%)', { anMobile: true }), n('khoa', 'Khoá', { ma: CO }), c('doi_lay', 'Đổi lấy', { anMobile: true })],
    tim: ['su_kien', 'vat_pham', 'loai'],
  },
  {
    ma: 'su-kien-dang-co', tab: 'su_kien', ten: 'Công tắc sự kiện (tob_su_kien)', moTa: 'Trạng thái bật/tắt hiện tại — sửa ở trang Sự kiện.', loai: 'sql', bang: 'tob_su_kien',
    cot: [c('ma', 'Mã'), c('ten', 'Tên'), c('nhom', 'Nhóm'), n('bat', 'Muốn bật', { ma: CO }), n('dang_bat', 'Game đang bật', { ma: CO }), c('mo_luc', 'Mở lúc', { anMobile: true }), c('dong_luc', 'Đóng lúc', { anMobile: true }), c('sua_boi', 'Sửa bởi', { anMobile: true }), c('sua_luc', 'Sửa lúc', { anMobile: true })],
    tim: ['ma', 'ten', 'nhom'],
  },
  // ------------------------------------------------------------------ Cửa hàng
  {
    ma: 'shop', tab: 'cua_hang', ten: 'Shop đặc biệt (gốc)', moTa: 'data_shop — giá gốc. Khi bật dài hạn, giá thật lấy từ "Giá shop dài hạn".', loai: 'sql', bang: 'data_shop',
    cot: [n('id', 'ID'), c('name', 'Tên'), c('decript', 'Mô tả', { anMobile: true }), n('price', 'Giá'), n('priceSale', 'Giá giảm'), n('type', 'Kiểu', { anMobile: true }), n('tab', 'Thẻ'), n('sell', 'Đang bán', { ma: CO }), n('value', 'Giá trị', { anMobile: true })],
    tim: ['name', 'decript', 'id'], sapXep: 'id',
  },
  {
    ma: 'shop-gia-dai-han', tab: 'cua_hang', ten: 'Giá shop dài hạn', moTa: 'data_shop_gia_lau_dai — giá & tiền cũ → mới (0 lượng, 1 xu, 2 lượng khoá, 9 ngừng bán).', loai: 'sql', bang: 'data_shop_gia_lau_dai',
    cot: [n('id_shop', 'ID shop'), c('ten', 'Tên'), n('gia_cu', 'Giá cũ'), n('tien_cu', 'Tiền cũ', { ma: TIEN_SO }), n('gia_moi', 'Giá mới'), n('tien_moi', 'Tiền mới', { ma: TIEN_SO }), n('gioi_han_tuan', 'Giới hạn / tuần'), c('ly_do', 'Lý do', { anMobile: true })],
    tim: ['ten', 'ly_do', 'id_shop'], sapXep: 'id_shop',
  },
  {
    ma: 'cua-hang-dai-han', tab: 'cua_hang', ten: 'Cửa hàng NPC dài hạn', moTa: 'data_cua_hang_lau_dai — chi phí "tiền:số", thưởng "ITEM:id:số".', loai: 'sql', bang: 'data_cua_hang_lau_dai',
    cot: [n('id', 'ID'), c('nhom', 'Nhóm'), c('ten', 'Tên'), c('chi_phi', 'Chi phí'), c('thuong', 'Nhận'), n('cap', 'Cấp'), n('ngay_mo', 'Mở từ ngày'), n('lop', 'Lớp', { anMobile: true }), n('gioi_han_tuan', 'Giới hạn / tuần')],
    tim: ['ten', 'nhom', 'chi_phi', 'thuong'], sapXep: 'id',
  },
  {
    ma: 'noi-dung-cua-hang', tab: 'cua_hang', ten: 'Bảng giá cửa hàng (thiết kế)', moTa: 'data_noi_dung_cua_hang — giá theo giờ thu nhập.', loai: 'sql', bang: 'data_noi_dung_cua_hang',
    cot: [n('stt', 'STT'), c('cua_hang', 'Cửa hàng'), c('vat_pham', 'Mã vật phẩm', { anMobile: true }), c('ten', 'Tên'), n('gia', 'Giá'), c('tien', 'Tiền', { ma: TIEN_CHU }), n('cap', 'Cấp'), n('ngay_mo', 'Mở từ ngày', { anMobile: true }), c('gioi_han', 'Giới hạn'), n('gio_thu_nhap', 'Giờ thu nhập', { anMobile: true })],
    tim: ['ten', 'cua_hang', 'vat_pham'], sapXep: 'stt',
  },
  {
    ma: 'che-tao', tab: 'cua_hang', ten: 'Chế tạo', moTa: 'data_noi_dung_che_tao — thành phẩm, đá, nguyên liệu phụ, xu.', loai: 'sql', bang: 'data_noi_dung_che_tao',
    cot: [c('thanh_pham', 'Thành phẩm'), c('ten', 'Tên'), n('bac', 'Bậc'), n('cap', 'Cấp'), n('da', 'Đá'), c('nguyen_lieu_phu', 'Nguyên liệu phụ'), n('so_nl_phu', 'Số NL phụ'), n('xu', 'Xu'), c('npc', 'NPC', { anMobile: true })],
    tim: ['ten', 'thanh_pham', 'npc'],
  },
  {
    ma: 'phan-ra', tab: 'cua_hang', ten: 'Phân rã', moTa: 'data_noi_dung_phan_ra — đổi đồ thừa ra đá + xu.', loai: 'sql', bang: 'data_noi_dung_phan_ra',
    cot: [c('nguon', 'Nguồn'), n('bac', 'Bậc'), n('da', 'Đá nhận'), n('xu', 'Xu nhận'), c('them', 'Thêm')],
    tim: ['nguon', 'them'],
  },
  {
    ma: 'nang-cap', tab: 'cua_hang', ten: 'Nâng cấp trang bị', moTa: 'data_nang_cap_lau_dai — tỉ lệ, pity, xu, đá, bùa lượng khoá.', loai: 'sql', bang: 'data_nang_cap_lau_dai',
    cot: [n('bac', 'Bậc'), c('ten_bac', 'Tên bậc'), n('cap_bac', 'Cấp bậc', { anMobile: true }), n('cong', 'Lên +'), n('ti_le', 'Tỉ lệ (%)'), n('pity_moi_lan', 'Pity / lần', { anMobile: true }), n('xu', 'Xu'), n('da', 'Đá'), n('bua_luong_khoa', 'Bùa (lượng khoá)'), n('mat_cap', 'Rớt cấp khi hỏng', { ma: CO }), n('chi_so_phan_tram', 'Chỉ số (%)', { anMobile: true })],
    tim: ['ten_bac', 'bac'],
  },
  {
    ma: 'trang-tri', tab: 'cua_hang', ten: 'Trang trí theo mùa', moTa: 'data_noi_dung_trang_tri.', loai: 'sql', bang: 'data_noi_dung_trang_tri',
    cot: [n('stt', 'STT'), n('mua', 'Mùa'), c('ten_mua', 'Tên mùa'), c('kieu', 'Kiểu'), c('ten', 'Tên'), n('gia', 'Giá'), c('tien', 'Tiền', { ma: TIEN_CHU }), c('nguon', 'Nguồn', { anMobile: true }), c('trang_thai', 'Trạng thái')],
    tim: ['ten', 'ten_mua', 'kieu'], sapXep: 'stt',
  },
  // ------------------------------------------------------------------ Rơi đồ
  {
    ma: 'roi', tab: 'roi', ten: 'Bảng rơi đồ', moTa: 'data_noi_dung_roi — ppm = phần triệu mỗi lần hạ quái.', loai: 'sql', bang: 'data_noi_dung_roi',
    cot: [n('stt', 'STT'), c('nguon', 'Nguồn'), c('khoa_nguon', 'Khoá nguồn', { anMobile: true }), c('vat_pham', 'Mã vật phẩm', { anMobile: true }), c('ten', 'Tên'), n('ppm', 'Tỉ lệ (ppm)'), n('sl_min', 'SL tối thiểu'), n('sl_max', 'SL tối đa'), c('do_hiem', 'Độ hiếm'), n('khoa', 'Khoá', { ma: CO }), n('het_han', 'Hết hạn', { anMobile: true })],
    tim: ['nguon', 'ten', 'vat_pham', 'do_hiem'], sapXep: 'stt',
  },
  {
    ma: 'roi-quai', tab: 'roi', ten: 'EXP & xu rơi theo quái', moTa: 'data_monster: rcvxp / rcvgold mỗi con (trước khi nhân tỉ lệ cau_hinh).', loai: 'sql', bang: 'data_monster',
    cot: [n('id', 'ID'), c('name', 'Tên'), n('level', 'Cấp'), n('rcvxp', 'EXP nhận'), n('rcvgold', 'Xu nhận'), n('isuse', 'Đang dùng', { ma: CO })],
    tim: ['name', 'id'], sapXep: 'level',
  },
  // ------------------------------------------------------------------ Gym / Boss
  {
    ma: 'gym', tab: 'gym', ten: 'Gym', moTa: 'data_noi_dung_gym — huy hiệu, boss, thưởng lần đầu / tuần.', loai: 'sql', bang: 'data_noi_dung_gym',
    cot: [n('stt', 'STT'), c('huy_hieu', 'Huy hiệu'), c('dia_danh', 'Địa danh'), c('he', 'Hệ'), n('cap', 'Cấp'), c('ten_boss', 'Boss'), n('template_quai', 'Mẫu quái', { anMobile: true }), n('hp', 'HP'), n('tan_cong', 'Tấn công', { anMobile: true }), n('phong_thu', 'Phòng thủ', { anMobile: true }), n('ttk_giay', 'Thời gian hạ (giây)', { anMobile: true }), n('no_sau_phut', 'Nổ sau (phút)', { anMobile: true }), c('thuong_lan_dau', 'Thưởng lần đầu'), c('thuong_tuan', 'Thưởng tuần'), c('ma_nhiem_vu', 'Mã nhiệm vụ', { anMobile: true })],
    tim: ['huy_hieu', 'dia_danh', 'ten_boss', 'he'], sapXep: 'stt',
  },
  {
    ma: 'boss-the-gioi', tab: 'boss', ten: 'Boss thế giới', moTa: 'data_noi_dung_boss_the_gioi — lịch theo tuần, chỉ số, đặc sản.', loai: 'sql', bang: 'data_noi_dung_boss_the_gioi',
    cot: [n('tuan', 'Tuần'), c('loai', 'Loại'), c('ten_nam1', 'Tên năm 1'), c('ten_nam2', 'Tên năm 2', { anMobile: true }), c('he', 'Hệ'), n('cap', 'Cấp'), n('hp', 'HP'), n('tan_cong', 'Tấn công', { anMobile: true }), n('phong_thu', 'Phòng thủ', { anMobile: true }), n('so_nguoi_chuan', 'Số người chuẩn'), n('phut_muc_tieu', 'Phút mục tiêu', { anMobile: true }), n('lan_moi_ngay', 'Lần / ngày'), c('vat_pham_dac_trung', 'Vật phẩm đặc trưng', { anMobile: true })],
    tim: ['ten_nam1', 'ten_nam2', 'loai', 'he'], sapXep: 'tuan',
  },
  // ------------------------------------------------------------------ Khác
  {
    ma: 'kinh-te-dai-han', tab: 'khac', ten: 'Thông số kinh tế dài hạn', moTa: 'data_kinh_te_lau_dai — giá trị thiết kế (cũ → mới). Đè được bằng khoá cùng tên trong cau_hinh.', loai: 'sql', bang: 'data_kinh_te_lau_dai',
    cot: [c('khoa', 'Khoá'), c('gia_tri', 'Giá trị'), c('gia_tri_cu', 'Giá trị cũ'), c('ghi_chu', 'Ghi chú')],
    tim: ['khoa', 'ghi_chu'],
  },
  {
    ma: 'ngoc-he', tab: 'khac', ten: 'Ngọc hệ', moTa: 'data_noi_dung_ngoc_he.', loai: 'sql', bang: 'data_noi_dung_ngoc_he',
    cot: [n('he', 'Hệ'), c('ten', 'Tên'), n('cap', 'Cấp'), n('chi_so_phan_tram', 'Chỉ số (%)'), n('gio_thu_nhap', 'Giờ thu nhập'), n('xu_vi_du_cap60', 'Xu (ví dụ cấp 60)', { anMobile: true }), n('phan_ra_da', 'Phân rã ra đá', { anMobile: true }), c('trang_thai', 'Trạng thái')],
    tim: ['ten'],
  },
  {
    ma: 'bo-suu-tap', tab: 'khac', ten: 'Bộ sưu tập (set)', moTa: 'data_noi_dung_bo.', loai: 'sql', bang: 'data_noi_dung_bo',
    cot: [n('bac', 'Bậc'), c('ten_bo', 'Tên bộ'), c('vung', 'Vùng'), n('so_mon', 'Số món'), c('chi_so', 'Chỉ số'), n('phan_tram', '%'), c('mo_ta', 'Mô tả', { anMobile: true })],
    tim: ['ten_bo', 'vung'],
  },
  {
    ma: 'chieu', tab: 'khac', ten: 'Chiêu thức', moTa: 'data_noi_dung_chieu.', loai: 'sql', bang: 'data_noi_dung_chieu',
    cot: [n('dong', 'Dòng'), c('ten_dong', 'Tên dòng'), n('so_hieu', 'Số hiệu', { anMobile: true }), c('ten', 'Tên chiêu'), c('he', 'Hệ'), c('kieu', 'Kiểu'), c('vai_tro', 'Vai trò', { anMobile: true }), n('cap_mo', 'Cấp mở'), n('phan_tram_cap1', '% cấp 1', { anMobile: true }), n('phan_tram_cap10', '% cấp 10', { anMobile: true }), n('hoi_chieu_giay', 'Hồi chiêu (giây)'), n('pp', 'PP', { anMobile: true }), c('trang_thai', 'Trạng thái', { anMobile: true })],
    tim: ['ten', 'ten_dong', 'he'],
  },
  {
    ma: 'thuoc-tinh', tab: 'khac', ten: 'Thuộc tính trang bị', moTa: 'data_attribute — mã thuộc tính dòng phụ.', loai: 'sql', bang: 'data_attribute',
    cot: [n('id', 'ID'), c('name', 'Tên'), c('decript', 'Mô tả'), n('ispercent', 'Là %', { ma: CO }), n('idcolor', 'Màu', { anMobile: true })],
    tim: ['name', 'decript', 'id'], sapXep: 'id',
  },
]

/** Số dòng tối đa một lần trả về. Bảng lớn nhất hiện tại ~1 900 dòng. */
export const DU_LIEU_GAME_TOI_DA = 5000
/** Chuỗi dài hơn thì cắt (lời thoại, mô tả). */
export const DU_LIEU_GAME_CAT_CHUOI = 300

export function timBoDuLieu(ma: unknown): BoDuLieu | null {
  if (typeof ma !== 'string') return null
  return DS_DU_LIEU_GAME.find(b => b.ma === ma) ?? null
}

/** Tên định danh SQL an toàn (chỉ để chốt chặn lần nữa — danh mục vốn là hằng số). */
export function laTenSqlAnToan(s: string): boolean {
  return /^[A-Za-z_][A-Za-z0-9_]{0,63}$/.test(s)
}

/**
 * Dựng câu SELECT cho một bộ dữ liệu SQL. Trả về sql + tham số (q đi qua `?`).
 * Ném Error khi danh mục có tên không an toàn (không bao giờ xảy ra nếu danh mục đúng).
 */
export function dungSqlBoDuLieu(b: BoDuLieu, tim: string, gioiHan: number, boQua: number): { sql: string, thamSo: unknown[], sqlDem: string, thamSoDem: unknown[] } {
  if (b.loai !== 'sql' || !b.bang) throw new Error('Không phải bảng SQL')
  const ten = [b.bang, ...b.cot.map(x => x.key), ...(b.tim ?? []), ...(b.sapXep ? [b.sapXep] : [])]
  for (const t of ten) if (!laTenSqlAnToan(t)) throw new Error(`Tên không an toàn: ${t}`)
  const cot = b.cot.map(x => `\`${x.key}\``).join(', ')
  const t = tim.trim().slice(0, 100)
  const timCot = t && b.tim?.length ? b.tim : []
  const where = timCot.length ? ` WHERE ${timCot.map(x => `CAST(\`${x}\` AS CHAR) LIKE ?`).join(' OR ')}` : ''
  // Bỏ % và \ (không phụ thuộc ESCAPE — MySQL và NovaDB/SQLite hiểu khác nhau); '_' còn lại chỉ khớp 1 ký tự bất kỳ, vô hại.
  const thamTim = timCot.map(() => `%${t.replace(/[\\%]/g, '')}%`)
  const order = b.sapXep ? ` ORDER BY \`${b.sapXep}\`` : ''
  const lim = Math.max(1, Math.min(DU_LIEU_GAME_TOI_DA, Math.trunc(gioiHan) || DU_LIEU_GAME_TOI_DA))
  const off = Math.max(0, Math.trunc(boQua) || 0)
  return {
    sql: `SELECT ${cot} FROM \`${b.bang}\`${where}${order} LIMIT ${lim} OFFSET ${off}`,
    thamSo: thamTim,
    sqlDem: `SELECT COUNT(*) FROM \`${b.bang}\`${where}`,
    thamSoDem: thamTim,
  }
}
