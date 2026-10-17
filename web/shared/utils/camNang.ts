/**
 * Cẩm nang (wiki cơ chế) — dữ liệu + danh mục trang.
 *
 * - Dữ liệu số: shared/data/cam-nang.json, sinh bởi `node scripts/dong-bo-cam-nang.mjs` từ repo
 *   game HTBB (CSV thiết kế + ThongSoLauDai.java). KHÔNG sửa tay JSON.
 * - Danh mục trang (DS_CAM_NANG): dùng cho thanh bên, tìm kiếm, sitemap, menu. Thêm trang mới ở
 *   app/pages/cam-nang/<slug>.vue thì thêm một dòng ở đây.
 */
import raw from '../data/cam-nang.json'

export interface DongCap {
  cap: number
  expLenCap: number
  gioTichLuy: number
  gioCap: number
  quaiMoiGio: number
  expMoiQuai: number
  xuMoiQuai: number
  xuMoiGio: number
}
export interface ChuongNV { so: number, ten: string, diaDanh: string, cap: number, soBuoc: number, ngayMo: number }
export interface CungNV { ma: string, ten: string, vung: string, nam: number, chuong: ChuongNV[], capTu: number, capDen: number }
export interface NhomNV { ten: string, vung: string, capTu: number, soBuoc: number }
export interface NhiemVuDong {
  ten: string
  cap: number
  npc: string
  diaDanh: string
  mucTieu: string
  soLuong: number | null
  lapLai: string
}
export interface SuKienTuan {
  tuan: number
  thang: number
  ten1: string
  ten2: string
  phan: string
  loai: string
  coChe: string
  soNgay: number
  phanThuong: string
  mocGiuChan: string
}
export interface BanDoInfo { id: number, loai: string, ten: string, vung: string, anh: boolean }

export interface CamNangData {
  capDo: {
    capToiDa: number
    tranCap: { ngay: number, cap: number }[]
    bang: DongCap[]
    nangLuong: { phutNgay: number, phutTichToiDa: number, bacMet: { phut: number, tocDo: number }[] }
    batKip: { heSoPhanTram: number, khoangCap: number }
    tanThu: { duoiCap: number, heSoPhanTram: number }
    tinhThong: { phanTramCap: number, diemMoiMua: number, toiDa: number, phanNghinMoiDiem: number }
  }
  trangBi: {
    soO: number
    o: { loai: 'mac' | 'vat-cam' | 'huy-hieu' | 'ruy-bang', ten: string, vai: string, soMon: number }[]
    bac: { bac: number, ten: string, cap: number, ngayMo: number, gia: number }[]
    soMonMuaMoiBac: number
    nangCap: { plus: number, tiLe: number, da: number, buaLuongKhoa: number, chiSoPhanTram: number }[]
    chiPhiNangCap: number[][]
    pityChia: number
    matCapTu: number
    giuCapKhiDoiBac: number
    daRoiPhanNghin: number
    daTuPhanRa: number
    boTrangBi: { soMon: number, chiSo: string, phanTram: number }[]
    ngocHe: { capToiDa: number, giaGioPhanTram: number, tangPhanTram: number }
  }
  kinhTe: {
    tiLeRoiXu: number
    thueCho: number
    phiDangBan: number
    tranGiaoDichGio: number
    tieuHaoPhanTram: number
    suaDoPhanTram: number
    dichChuyenLanNgay: number
    dichChuyenPhanTramGio: number
    suKienPhanXu: number
    giftcodeToiDaThang: number
    trangTri: { phanNghinNgay: number, nguongGio: number }
    lichDangNhap: number[]
    luongXepHangTuan: { top: number, luong: number }[]
  }
  nhiemVu: {
    demLoai: Record<string, number>
    gioiHanNgay: number
    gioiHanTuan: number
    veChuongTuan: number
    veChuongToiDa: number
    veChuongBanDau: number
    huongDan: NhiemVuDong[]
    cung: CungNV[]
    phu: NhomNV[]
    tuyen: NhomNV[]
    hamNguc: NhiemVuDong[]
    suPham: NhiemVuDong[]
    doi: NhiemVuDong[]
    quayLai: NhiemVuDong[]
    thanhTuu: { ten: string, soLuong: number, kieu: string, luongKhoa: number, luong: number }[]
    ngay: string[]
    tuan: string[]
    pokedexSoLoai: number
  }
  suKien: SuKienTuan[]
  banDo: BanDoInfo[]
}

export const CAM_NANG = raw as unknown as CamNangData

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
    moTa: '18 hệ, bảng khắc chế 18×18 tương tác, hệ số đã làm dịu, STAB và Pokémon hai hệ.',
    muc: [{ id: 'bang', ten: 'Bảng khắc chế 18×18' }, { id: 'tinh-thu', ten: 'Tính thử hệ số' }, { id: 'lam-diu', ten: 'Vì sao hệ số được làm dịu' }],
    tuKhoa: 'type chart khac che lua nuoc co dien stab hai he mien nhiem',
  },
  {
    slug: 'chi-so-sat-thuong', ten: 'Chỉ số & sát thương', nhom: 'Chiến đấu', icon: 'i-lucide-swords',
    moTa: 'Điểm tiềm năng, chỉ số chính của từng dòng, cách tính sát thương từng bước và máy tính thử.',
    muc: [{ id: 'chi-so', ten: 'Chỉ số & điểm tiềm năng' }, { id: 'cong-thuc', ten: 'Sát thương tính thế nào' }, { id: 'may-tinh', ten: 'Máy tính sát thương' }, { id: 'guc-nga', ten: 'Gục ngã & hồi sinh' }],
    tuKhoa: 'stat damage cong diem suc manh nhanh nhen tinh than the luc chi mang',
  },
  {
    slug: 'chieu-thuc', ten: 'Chiêu thức', nhom: 'Chiến đấu', icon: 'i-lucide-zap',
    moTa: 'Bảng chiêu của 5 dòng khởi đầu: hệ chiêu, STAB và mẹo xoay chiêu.',
    muc: [{ id: 'bang-chieu', ten: 'Chiêu theo dòng' }, { id: 'meo', ten: 'Mẹo dùng chiêu' }],
    tuKhoa: 'skill chieu ky nang diem ky nang bang chieu',
  },
  {
    slug: 'cap-do', ten: 'Cấp độ & trần cấp mùa', nhom: 'Phát triển', icon: 'i-lucide-trending-up',
    moTa: 'Cấp tối đa, trần cấp mở theo mùa, năng lượng huấn luyện, bắt kịp, tinh thông và bảng EXP.',
    muc: [{ id: 'tran-cap', ten: 'Trần cấp theo mùa' }, { id: 'nang-luong', ten: 'Năng lượng huấn luyện' }, { id: 'bat-kip', ten: 'Bắt kịp & tân thủ' }, { id: 'tinh-thong', ten: 'Tinh thông' }, { id: 'bang-exp', ten: 'Bảng EXP' }],
    tuKhoa: 'level exp kinh nghiem len cap tran cap mua met moi nang luong',
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
    muc: [{ id: 'nhom', ten: 'Các nhóm vật phẩm' }, { id: 'khoa', ten: 'Vật phẩm khoá & hết hạn' }],
    tuKhoa: 'item thuoc ether berry trung pet hanh trang',
  },
  {
    slug: 'kinh-te', ten: 'Kinh tế & tiền tệ', nhom: 'Kinh tế & vật phẩm', icon: 'i-lucide-coins',
    moTa: 'Xu, lượng khoá, lượng: kiếm ở đâu, tiêu vào đâu, thuế chợ và vì sao giá cả ổn định.',
    muc: [{ id: 'tien', ten: 'Ba loại tiền' }, { id: 'kiem', ten: 'Kiếm tiền' }, { id: 'tieu', ten: 'Tiêu tiền' }, { id: 'cho', ten: 'Chợ & giao dịch' }, { id: 'lam-phat', ten: 'Chống lạm phát' }],
    tuKhoa: 'xu luong luong khoa tien te thue cho giao dich lam phat khong nap',
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
    tuKhoa: 'bang hoi guild doi truong to doi su phu hoc tro vung kanto johto hoenn',
  },
  {
    slug: 'pvp', ten: 'PvP xếp hạng mùa', nhom: 'Cộng đồng', icon: 'i-lucide-trophy',
    moTa: 'Mùa giải PvP, bậc Poké → Master, đấu trường, chế độ PK và bảng xếp hạng tuần.',
    muc: [{ id: 'mua', ten: 'Mùa giải' }, { id: 'che-do', ten: 'Các chế độ PvP' }, { id: 'pk', ten: 'PK ngoài bản đồ' }, { id: 'xep-hang-tuan', ten: 'Xếp hạng tuần' }],
    tuKhoa: 'pvp pk dau truong loi dai lien dau xep hang mua giai master ball',
  },
  {
    slug: 'boss-dot-kich', ten: 'Boss & đột kích', nhom: 'Nội dung', icon: 'i-lucide-skull',
    moTa: 'Boss huyền thoại trên bản đồ, lịch đột kích theo tuần và cách chuẩn bị.',
    muc: [{ id: 'boss', ten: 'Danh sách boss' }, { id: 'dot-kich', ten: 'Đột kích huyền thoại' }, { id: 'chuan-bi', ten: 'Chuẩn bị' }],
    tuKhoa: 'boss raid dot kich zapdos rayquaza kyogre groudon darkrai entei huyen thoai',
  },
  {
    slug: 'ham-nguc', ten: 'Hầm Ngục Bí Ẩn', nhom: 'Nội dung', icon: 'i-lucide-castle',
    moTa: 'Chế độ hầm ngục tầng ngẫu nhiên, hạt giống theo ngày và bảng điểm.',
    muc: [{ id: 'gioi-thieu', ten: 'Hầm ngục là gì' }, { id: 'tang', ten: 'Các chặng' }, { id: 'meo', ten: 'Mẹo' }],
    tuKhoa: 'mystery dungeon ham nguc tang ngau nhien',
  },
  {
    slug: 'su-kien', ten: 'Sự kiện năm', nhom: 'Nội dung', icon: 'i-lucide-calendar-days',
    moTa: 'Lịch 52 tuần: lễ Việt Nam, Ngày cộng đồng, tuần hệ, Tuần Shiny, đột kích, mùa PvP.',
    muc: [{ id: 'dang-dien-ra', ten: 'Đang diễn ra' }, { id: 'lich', ten: 'Lịch cả năm' }, { id: 'loai', ten: 'Các loại sự kiện' }],
    tuKhoa: 'event su kien tet trung thu halloween noel cong dong tuan he lich',
  },
  {
    slug: 'nhiem-vu', ten: 'Nhiệm vụ', nhom: 'Nội dung', icon: 'i-lucide-scroll-text',
    moTa: 'Hướng dẫn tân thủ, chính tuyến 52 chương, phụ tuyến, nhiệm vụ ngày/tuần và thành tựu.',
    muc: [{ id: 'tong-quan', ten: 'Tổng quan' }, { id: 'chinh-tuyen', ten: 'Chính tuyến' }, { id: 'phu-tuyen', ten: 'Phụ tuyến & tuyến đường' }, { id: 'ngay-tuan', ten: 'Ngày & tuần' }, { id: 'thanh-tuu', ten: 'Thành tựu' }],
    tuKhoa: 'quest nhiem vu chinh tuyen phu tuyen hang ngay hang tuan ve chuong thanh tuu danh hieu',
  },
  {
    slug: 'ban-do', ten: 'Bản đồ thế giới', nhom: 'Thế giới', icon: 'i-lucide-map',
    moTa: 'Các vùng, thị trấn, khu luyện cấp theo cấp độ và Pokémon hoang dã ở từng nơi.',
    muc: [{ id: 'vung', ten: 'Các vùng' }, { id: 'luyen-cap', ten: 'Khu luyện cấp' }, { id: 'danh-sach', ten: 'Mọi bản đồ' }],
    tuKhoa: 'map ban do khu vuc luyen cap pallet viridian kanto johto',
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

/** Tuần trong năm (1..52) của một ngày — đơn giản: ngày thứ n trong năm / 7. */
export function tuanTrongNam(d: Date): number {
  const dau = Date.UTC(d.getFullYear(), 0, 1)
  const ngay = Math.floor((Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()) - dau) / 86400000)
  return Math.min(52, Math.floor(ngay / 7) + 1)
}

/** Sự kiện của tuần `tuan` và `soTuanSau` tuần kế tiếp (vòng qua năm mới). */
export function suKienTuTuan(tuan: number, soTuanSau = 2, ds: SuKienTuan[] = CAM_NANG.suKien): SuKienTuan[] {
  const ra: SuKienTuan[] = []
  for (let i = 0; i <= soTuanSau; i++) {
    const t = ((tuan - 1 + i) % 52) + 1
    const sk = ds.find(s => s.tuan === t)
    if (sk) ra.push(sk)
  }
  return ra
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
