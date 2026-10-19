/**
 * Danh mục trang cẩm nang + tiện ích nhỏ — KHÔNG import JSON dữ liệu, để thanh đầu trang, tìm kiếm,
 * sitemap... dùng được mà không kéo cả shared/data/cam-nang.json vào mọi trang.
 * Dữ liệu số nằm ở camNang.ts (CAM_NANG) và camNangNoiDung.ts (CAM_NANG_ND).
 *
 * Thêm trang mới ở app/pages/cam-nang/<slug>.vue thì thêm một dòng vào DS_CAM_NANG.
 */
// ---------------------------------------------------------------- danh mục trang

export interface TrangCamNang {
  slug: string
  ten: string
  nhom: string
  icon: string
  moTa: string
  /** Mục con (h2) — để tìm kiếm nhảy thẳng tới mục. id = anchor. */
  muc?: { id: string, ten: string }[]
  /** Từ khoá thêm cho tìm kiếm. */
  tuKhoa?: string
}

export const NHOM_CAM_NANG = ['Chiến đấu', 'Phát triển', 'Kinh tế & vật phẩm', 'Cộng đồng', 'Nội dung', 'Thế giới'] as const

export const DS_CAM_NANG: TrangCamNang[] = [
  {
    slug: 'he-khac-che', ten: 'Hệ & khắc hệ', nhom: 'Chiến đấu', icon: 'i-lucide-flame',
    moTa: '18 hệ, bảng khắc hệ 18×18 tương tác, hệ số đã làm dịu, STAB và Pokémon hai hệ.',
    muc: [{ id: 'bang', ten: 'Bảng khắc hệ 18×18' }, { id: 'tinh-thu', ten: 'Tính thử hệ số' }, { id: 'lam-diu', ten: 'Vì sao hệ số được làm dịu' }],
    tuKhoa: 'type chart khac che lua nuoc co dien stab hai he mien nhiem',
  },
  {
    slug: 'chi-so-sat-thuong', ten: 'Chỉ số & sát thương', nhom: 'Chiến đấu', icon: 'i-lucide-swords',
    moTa: 'Điểm tiềm năng, chỉ số trên trang bị và mức tối đa, cách tính sát thương từng bước và máy tính thử.',
    muc: [{ id: 'chi-so', ten: 'Chỉ số & điểm tiềm năng' }, { id: 'chi-so-trang-bi', ten: 'Chỉ số trên trang bị' }, { id: 'gioi-han', ten: 'Giới hạn chỉ số' }, { id: 'cong-thuc', ten: 'Sát thương tính thế nào' }, { id: 'may-tinh', ten: 'Máy tính sát thương' }, { id: 'guc-nga', ten: 'Gục ngã & hồi sinh' }],
    tuKhoa: 'stat damage cong diem suc manh nhanh nhen tinh than the luc chi mang ne tranh hut mau khang he gioi han tran',
  },
  {
    slug: 'chieu-thuc', ten: 'Chiêu thức', nhom: 'Chiến đấu', icon: 'i-lucide-zap',
    moTa: 'Bảng chiêu của 5 dòng khởi đầu: hệ chiêu, STAB và mẹo xoay chiêu.',
    muc: [{ id: 'bang-chieu', ten: 'Chiêu theo dòng' }, { id: 'meo', ten: 'Mẹo dùng chiêu' }],
    tuKhoa: 'skill chieu ky nang diem ky nang bang chieu',
  },
  {
    slug: 'gym', ten: 'Gym & huy hiệu', nhom: 'Chiến đấu', icon: 'i-lucide-medal',
    moTa: '16 Gym Kanto – Johto: huy hiệu, cấp, Trưởng Gym, luật trận 1 đấu 1, thưởng đồ cầm tay Hoàn mỹ và Huy chương Gym hằng tuần.',
    muc: [{ id: 'thach-dau', ten: 'Thách đấu Gym' }, { id: 'kanto', ten: 'Gym Kanto' }, { id: 'johto', ten: 'Gym Johto' }, { id: 'danh-lai', ten: 'Đánh lại hằng tuần' }],
    tuKhoa: 'gym huy hieu badge truong gym leader golem gyarados luxray huy chuong ao choang hoan my',
  },
  {
    slug: 'cap-do', ten: 'Cấp độ & EXP', nhom: 'Phát triển', icon: 'i-lucide-trending-up',
    moTa: 'Cấp tối đa (không trần cấp theo mùa), năng lượng huấn luyện, bắt kịp, tinh thông và bảng EXP.',
    muc: [{ id: 'tran-cap', ten: 'Không trần cấp theo mùa' }, { id: 'nang-luong', ten: 'Năng lượng huấn luyện' }, { id: 'bat-kip', ten: 'Bắt kịp & tân thủ' }, { id: 'tinh-thong', ten: 'Tinh thông' }, { id: 'bang-exp', ten: 'Bảng EXP' }],
    tuKhoa: 'level exp kinh nghiem len cap tran cap mua met moi nang luong',
  },
  {
    slug: 'tu-dong', ten: 'Tự động đánh', nhom: 'Phát triển', icon: 'i-lucide-bot',
    moTa: 'Tự động đánh chính thức: giờ mỗi ngày, cài đặt chiêu / thuốc / nhặt / vùng, khi nào tự dừng và khác gì vé treo máy.',
    muc: [{ id: 'la-gi', ten: 'Tự động đánh là gì' }, { id: 'cach-dung', ten: 'Cách dùng' }, { id: 'gioi-han', ten: 'Giờ mỗi ngày & giới hạn' }, { id: 'tu-dung', ten: 'Khi nào tự dừng' }, { id: 'treo-may', ten: 'Khác vé treo máy' }],
    tuKhoa: 'auto tu dong danh treo may afk luyen cap tu danh quai tu uong thuoc mod',
  },
  {
    slug: 'so-tay', ten: 'Sổ tay huấn luyện', nhom: 'Phát triển', icon: 'i-lucide-notebook-pen',
    moTa: 'Menu việc dài hạn: năng lượng, nhiệm vụ tuần, lịch đăng nhập, vé chương, quà hoạt động, sư phạm, Gym, Hầm ngục, PvP mùa.',
    muc: [{ id: 'mo', ten: 'Mở sổ tay' }, { id: 'nang-luong', ten: 'Năng lượng & tinh thông' }, { id: 'nhiem-vu', ten: 'Nhiệm vụ tuần' }, { id: 'dang-nhap', ten: 'Lịch đăng nhập & vé chương' }, { id: 'qua-hoat-dong', ten: 'Quà hoạt động' }, { id: 'su-pham', ten: 'Sư phạm' }],
    tuKhoa: 'so tay huan luyen nang luong tinh thong lich dang nhap ve chuong qua hoat dong diem danh',
  },
  {
    slug: 'tien-hoa', ten: 'Tiến hoá & đá tiến hoá', nhom: 'Phát triển', icon: 'i-lucide-sparkles',
    moTa: 'Mốc tiến hoá của từng dòng, thưởng chỉ số theo giai đoạn, 8 nhánh Eevee và cách kiếm đá.',
    muc: [{ id: 'theo-cap', ten: 'Tiến hoá theo cấp' }, { id: 'eevee', ten: 'Eevee & đá tiến hoá' }, { id: 'kiem-da', ten: 'Cách kiếm đá' }],
    tuKhoa: 'evolution da tien hoa eevee vaporeon jolteon flareon espeon umbreon leafeon glaceon sylveon',
  },
  {
    slug: 'trang-bi', ten: 'Trang bị & nâng cấp', nhom: 'Phát triển', icon: 'i-lucide-shield',
    moTa: 'Ô trang bị, 11 bậc, bảng tỉ lệ +1..+15, cơ chế bù may mắn, bùa bảo hộ, ngọc hệ và bộ trang bị.',
    muc: [{ id: 'o', ten: 'Ô trang bị' }, { id: 'bac', ten: 'Bậc trang bị' }, { id: 'nang-cap', ten: 'Nâng cấp +1..+15' }, { id: 'chi-phi', ten: 'Chi phí & số lần thử' }, { id: 'ngoc-he', ten: 'Ngọc hệ' }, { id: 'bo', ten: 'Thưởng bộ' }],
    tuKhoa: 'equipment cuong hoa nang cap ti le bao hiem bua bao ho pity da nang cap ngoc kham',
  },
  {
    slug: 'vat-pham', ten: 'Vật phẩm', nhom: 'Kinh tế & vật phẩm', icon: 'i-lucide-backpack',
    moTa: 'Thuốc, Berry, đá, trứng, pet, thú cưỡi, vé chương và đồ trang trí — dùng vào việc gì.',
    muc: [{ id: 'nhom', ten: 'Các nhóm vật phẩm' }, { id: 'bang', ten: 'Bảng vật phẩm' }, { id: 'khoa', ten: 'Vật phẩm khoá & hết hạn' }],
    tuKhoa: 'item thuoc ether elixir hoi sinh berry qua mong keo hiem do giu bua bao ho bui sao trung pet hanh trang',
  },
  {
    slug: 'cua-hang', ten: 'Cửa hàng & chế tạo', nhom: 'Kinh tế & vật phẩm', icon: 'i-lucide-store',
    moTa: 'Giá bán ở Chansey, Kecleon, Bronzong, cửa hàng trang trí, đổi Huy chương Gym và công thức chế tạo ở Machamp.',
    muc: [{ id: 'tien', ten: 'Trả bằng gì?' }, { id: 'chansey-thuoc', ten: 'Chansey (thuốc)' }, { id: 'kecleon-luong-khoa', ten: 'Kecleon (lượng khoá)' }, { id: 'bronzong-trang-bi', ten: 'Bronzong (trang bị)' }, { id: 'che-tao', ten: 'Chế tạo' }],
    tuKhoa: 'shop cua hang gia mua ban chansey kecleon bronzong trang tri machamp che tao xu luong khoa',
  },
  {
    slug: 'kinh-te', ten: 'Kinh tế & tiền tệ', nhom: 'Kinh tế & vật phẩm', icon: 'i-lucide-coins',
    moTa: 'Xu, lượng khoá, lượng: kiếm ở đâu, tiêu vào đâu, thuế chợ, chốt chống clone khi chuyển xu và vì sao giá cả ổn định.',
    muc: [{ id: 'tien', ten: 'Ba loại tiền' }, { id: 'kiem', ten: 'Cách kiếm tiền' }, { id: 'tieu', ten: 'Tiêu tiền' }, { id: 'cho', ten: 'Chợ & giao dịch' }, { id: 'lam-phat', ten: 'Vì sao giá cả ổn định?' }],
    tuKhoa: 'xu luong luong khoa tien te thue cho giao dich lam phat chong clone chuyen xu nap khoi dau 5000 xu',
  },
  {
    slug: 'pokedex-shiny', ten: 'Pokédex & Shiny', nhom: 'Nội dung', icon: 'i-lucide-book-open',
    moTa: 'Đã gặp / đã hạ, mốc thưởng, nhiệm vụ nghiên cứu và săn Pokémon Shiny.',
    muc: [{ id: 'ghi-nhan', ten: 'Pokédex ghi nhận gì' }, { id: 'moc', ten: 'Mốc thưởng' }, { id: 'nghien-cuu', ten: 'Nhiệm vụ nghiên cứu' }, { id: 'shiny', ten: 'Pokémon Shiny' }],
    tuKhoa: 'pokedex shiny sang mau suu tam nghien cuu',
  },
  {
    slug: 'thu-cuoi-pet', ten: 'Thú cưỡi & pet', nhom: 'Phát triển', icon: 'i-lucide-rabbit',
    moTa: 'Pokémon cưỡi được, pet đi theo, trứng Pokémon và chuỗi tiến hoá của pet.',
    muc: [{ id: 'thu-cuoi', ten: 'Thú cưỡi' }, { id: 'pet', ten: 'Pet' }],
    tuKhoa: 'mount pet trung ponyta rapidash lapras',
  },
  {
    slug: 'doi-su-pham', ten: 'Đội, vùng & sư phạm', nhom: 'Cộng đồng', icon: 'i-lucide-users',
    moTa: 'Lập đội, nhiệm vụ đội, ba vùng Kanto/Johto/Hoenn, chế độ thầy–trò.',
    muc: [{ id: 'to-doi', ten: 'Tổ đội' }, { id: 'doi', ten: 'Đội' }, { id: 'vung', ten: 'Vùng' }, { id: 'su-pham', ten: 'Sư phạm (thầy – trò)' }],
    tuKhoa: 'doi guild doi truong to doi thay tro hoc tro vung kanto johto hoenn',
  },
  {
    slug: 'pvp', ten: 'PvP xếp hạng mùa', nhom: 'Cộng đồng', icon: 'i-lucide-trophy',
    moTa: 'Thang điểm mùa 13 tuần, bậc Poké Ball → Master Ball, cách tính trận, Giải Liên minh, luật PK và đua top.',
    muc: [{ id: 'mua', ten: 'Thang PvP mùa' }, { id: 'che-do', ten: 'Các chế độ PvP' }, { id: 'pk', ten: 'Luật PK' }, { id: 'xep-hang-tuan', ten: 'Xếp hạng & đua top' }],
    tuKhoa: 'pvp pk elo dau truong lien dau xep hang mua giai master ball great ultra dua top',
  },
  {
    slug: 'boss-dot-kich', ten: 'Boss & đột kích', nhom: 'Nội dung', icon: 'i-lucide-skull',
    moTa: 'Boss huyền thoại trên bản đồ, boss thế giới tối thứ Bảy, lịch đột kích theo tuần và cách chuẩn bị.',
    muc: [{ id: 'boss', ten: 'Danh sách boss' }, { id: 'boss-the-gioi', ten: 'Boss thế giới hằng tuần' }, { id: 'dot-kich', ten: 'Đột kích huyền thoại' }, { id: 'chuan-bi', ten: 'Chuẩn bị' }],
    tuKhoa: 'boss raid dot kich zapdos rayquaza kyogre groudon darkrai entei huyen thoai',
  },
  {
    slug: 'ham-nguc', ten: 'Hầm ngục bí ẩn', nhom: 'Nội dung', icon: 'i-lucide-castle',
    moTa: 'Thử thách leo tầng mỗi ngày: cách vào từ Sổ tay, luật tính quái, tối đa tầng và phần thưởng EXP + lượng khoá.',
    muc: [{ id: 'gioi-thieu', ten: 'Hầm ngục là gì' }, { id: 'thuong', ten: 'Phần thưởng' }, { id: 'tang', ten: 'Nhiệm vụ Hầm ngục' }, { id: 'meo', ten: 'Mẹo' }],
    tuKhoa: 'mystery dungeon ham nguc tang leo thap thu thach hang ngay',
  },
  {
    slug: 'su-kien', ten: 'Sự kiện năm', nhom: 'Nội dung', icon: 'i-lucide-calendar-days',
    moTa: 'Lễ hội theo ngày thật (cả âm lịch), tuần Pokémon, Ngày cộng đồng, Tuần Shiny, đột kích, mùa PvP.',
    muc: [{ id: 'dang-dien-ra', ten: 'Đang diễn ra' }, { id: 'lich', ten: 'Lịch cả năm' }, { id: 'loai', ten: 'Các loại sự kiện' }],
    tuKhoa: 'event su kien tet trung thu halloween noel gio to vu lan am lich cong dong tuan he lich',
  },
  {
    slug: 'nhiem-vu', ten: 'Nhiệm vụ', nhom: 'Nội dung', icon: 'i-lucide-scroll-text',
    moTa: 'Hướng dẫn tân thủ, chính tuyến 104 chương (2 năm × 52), phụ tuyến, nhiệm vụ ngày/tuần và thành tựu.',
    muc: [{ id: 'tong-quan', ten: 'Tổng quan' }, { id: 'chinh-tuyen', ten: 'Chính tuyến' }, { id: 'phu-tuyen', ten: 'Phụ tuyến' }, { id: 'ngay-tuan', ten: 'Ngày & tuần' }, { id: 'thanh-tuu', ten: 'Thành tựu' }],
    tuKhoa: 'quest nhiem vu chinh tuyen phu tuyen hang ngay hang tuan ve chuong thanh tuu danh hieu',
  },
  {
    slug: 'ban-do', ten: 'Bản đồ thế giới', nhom: 'Thế giới', icon: 'i-lucide-map',
    moTa: 'Các vùng, thị trấn, NPC và chức năng, khu luyện cấp theo cấp độ và Pokémon hoang dã ở từng nơi.',
    muc: [{ id: 'vung', ten: 'Các vùng' }, { id: 'npc', ten: 'NPC & chức năng' }, { id: 'luyen-cap', ten: 'Khu luyện cấp' }, { id: 'danh-sach', ten: 'Mọi bản đồ' }],
    tuKhoa: 'map ban do khu vuc luyen cap pallet viridian kanto johto npc chansey kecleon kangaskhan meowth ngan hang lapras pidgeot cho bay cho do',
  },
  {
    slug: 'he-sinh-thai', ten: 'Hệ sinh thái', nhom: 'Thế giới', icon: 'i-lucide-leaf',
    moTa: 'Ngày đêm, thời tiết, bùng phát, kỹ năng sống (câu cá, trồng trọt, khảo cổ, nấu ăn, chế thuốc), nhà trẻ, chợ, đố vui, đua và album.',
    muc: [{ id: 'the-gioi', ten: 'Ngày đêm & thời tiết' }, { id: 'bung-phat', ten: 'Bùng phát' }, { id: 'ky-nang', ten: 'Kỹ năng sống' }, { id: 'cong-thuc', ten: 'Công thức' }, { id: 'nha-tre', ten: 'Nhà trẻ & trứng' }, { id: 'cho', ten: 'Chợ sinh thái' }, { id: 'giai-tri', ten: 'Đố vui, đua & giải tuần' }, { id: 'suu-tam', ten: 'Album & vùng' }, { id: 'lenh', ten: 'Lệnh nhanh' }],
    tuKhoa: 'he sinh thai thoi tiet mua nang tuyet ban dem bung phat ky nang song trong trot khao co dao nau an che thuoc nha tre trung iv cho do vui dua album',
  },
  {
    slug: 'cau-ca', ten: 'Câu cá', nhom: 'Thế giới', icon: 'i-lucide-fish',
    moTa: 'Chỉ câu được ở hồ: cách nhận biết hồ, hồ lớn và ao, bảng cá câu được và danh sách bản đồ có hồ.',
    muc: [{ id: 'luat', ten: 'Chỉ câu ở hồ' }, { id: 'cach-cau', ten: 'Cách câu' }, { id: 'bang-cau', ten: 'Bảng câu được' }, { id: 'ban-do', ten: 'Bản đồ có hồ' }],
    tuKhoa: 'cau ca fishing ho ao magikarp feebas dratini lapras moi thom cauca',
  },
  {
    slug: 'tai-khoan', ten: 'Tài khoản & đăng ký', nhom: 'Thế giới', icon: 'i-lucide-user-plus',
    moTa: 'Đăng ký trên web hoặc ngay trong game, quy tắc tên và mật khẩu, email, kích hoạt và lấy lại mật khẩu.',
    muc: [{ id: 'hai-cach', ten: 'Hai cách đăng ký' }, { id: 'quy-tac', ten: 'Quy tắc tên & mật khẩu' }, { id: 'kich-hoat', ten: 'Kích hoạt & bảo mật' }],
    tuKhoa: 'dang ky tai khoan register mat khau email quen mat khau kich hoat trong game',
  },
  {
    slug: 'faq', ten: 'Câu hỏi thường gặp', nhom: 'Thế giới', icon: 'i-lucide-circle-help',
    moTa: 'Những câu hỏi hay gặp về tài khoản, cài đặt, lối chơi và chính sách phi thương mại.',
    tuKhoa: 'faq hoi dap loi dang nhap nap tien',
  },
]

export function trangCamNang(slug: string): TrangCamNang | undefined {
  return DS_CAM_NANG.find(t => t.slug === slug)
}

// ---------------------------------------------------------------- sự kiện

export const LOAI_SU_KIEN: Record<string, { ten: string, mau: string, icon: string }> = {
  le_vn: { ten: 'Lễ Việt Nam', mau: '#e3350d', icon: 'i-lucide-party-popper' },
  tuan_he: { ten: 'Tuần hệ', mau: '#3d7dca', icon: 'i-lucide-cloud-sun' },
  cong_dong: { ten: 'Ngày cộng đồng', mau: '#5aad34', icon: 'i-lucide-users' },
  pokemon: { ten: 'Pokémon', mau: '#efb000', icon: 'i-lucide-sparkles' },
  dot_kich: { ten: 'Đột kích', mau: '#7038f8', icon: 'i-lucide-skull' },
  pvp: { ten: 'Mùa PvP', mau: '#c03028', icon: 'i-lucide-trophy' },
  chien_vung: { ten: 'Thế chiến vùng', mau: '#705848', icon: 'i-lucide-flag' },
  mua: { ten: 'Theo mùa', mau: '#f08030', icon: 'i-lucide-sun' },
}

/**
 * "Tuần Pokémon" (1..52) đang chạy lúc `d` — CÙNG cách tính với máy chủ game (com.htbb.pokemon.LichNam):
 * tuần ISO (bắt đầu thứ Hai, tuần 1 chứa ngày thứ Năm đầu năm), đổi tuần lúc 04:00 giờ Việt Nam.
 * Tuần ISO 53 (một số năm) dùng chung dòng tuần 52 của lịch.
 */
export function tuanTrongNam(d: Date): number {
  // giờ Việt Nam (UTC+7), lùi 4 giờ: 00:00–03:59 thứ Hai vẫn thuộc tuần trước
  const t = new Date(d.getTime() + 7 * 3600_000 - 4 * 3600_000)
  const ngay = Date.UTC(t.getUTCFullYear(), t.getUTCMonth(), t.getUTCDate())
  const thu = (t.getUTCDay() + 6) % 7 // 0 = thứ Hai
  const thuNam = ngay + (3 - thu) * 86400000 // thứ Năm cùng tuần
  const nam = new Date(thuNam).getUTCFullYear()
  const tuan = Math.floor((thuNam - Date.UTC(nam, 0, 1)) / (7 * 86400000)) + 1
  return Math.min(52, Math.max(1, tuan))
}

// ---------------------------------------------------------------- tiện ích số

export function soVn(v: number, phanLe = 0): string {
  return v.toLocaleString('vi-VN', { maximumFractionDigits: phanLe, minimumFractionDigits: 0 })
}

/** "1,5 giờ" / "45 phút" từ số giờ. */
export function hienGio(h: number): string {
  if (h < 1) return `${Math.round(h * 60)} phút`
  if (h < 48) return `${soVn(h, 1)} giờ`
  return `${soVn(h / 24, 1)} ngày chơi`
}

/** Ngày từ khai trương -> "tháng thứ N". */
export function thangTuKhaiTruong(ngay: number): string {
  if (ngay === 0) return 'Khai trương'
  return `Ngày ${ngay} (≈ tháng ${Math.round(ngay / 30.4) + 1})`
}

/** Số lần thử kỳ vọng để lên +k từ +(k-1), có cơ chế bù (pity) — chưa tính tụt cấp. */
export function soLanThuKyVong(tiLeGoc: number, pityChia: number): number {
  const them = Math.ceil(tiLeGoc / pityChia)
  let conLai = 1
  let kyVong = 0
  for (let lan = 1; lan <= 200 && conLai > 1e-9; lan++) {
    const p = Math.min(100, tiLeGoc + (lan - 1) * them) / 100
    kyVong += lan * conLai * p
    conLai *= 1 - p
  }
  return kyVong
}
