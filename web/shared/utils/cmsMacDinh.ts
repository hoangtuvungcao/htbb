/**
 * Nội dung MẶC ĐỊNH của các khối sửa được trong khu quản trị (CMS).
 *
 * Website luôn chạy được khi CSDL chưa có bảng web_cai_dat (hoặc admin chưa nhập gì): trang đọc
 * /api/cong-khai, server trộn giá trị trong CSDL ĐÈ LÊN mặc định ở đây (hopNhatCms). Admin bấm
 * "Khôi phục mặc định" là xoá dòng trong CSDL -> quay về đúng các giá trị này.
 */

export interface LienKet { label: string, to: string, icon?: string }
export interface NutCta { label: string, to: string, kieu: 'chinh' | 'phu' }
export interface TheTinhNang { icon: string, t: string, m: string, to: string }
export interface Banner { anh: string, tieuDe: string, moTa: string, to: string, hien: boolean }

export interface CmsSite {
  tenSite: string
  khauHieu: string
  logo: string
  moTaSeo: string
  anhOg: string
  mangXaHoi: { ten: string, url: string, icon: string }[]
  baoTri: { bat: boolean, tieuDe: string, noiDung: string }
  thongBao: { bat: boolean, noiDung: string, to: string, mau: 'vang' | 'do' | 'xanh' }
  /** Nguồn trạng thái máy chủ: '' = bảng tob_vi_tri_online; hoặc URL JSON trả { song, online }. */
  trangThaiUrl: string
}
export interface CmsTrangChu {
  hero: { nhan: string, dong1: string, dong2: string, moTa: string, nut: NutCta[] }
  tinhNangTieuDe: string
  tinhNang: TheTinhNang[]
  khoiDauThuTu: number[]
  banner: Banner[]
  hienCanhPhim: boolean
}
export interface CmsMenu {
  dauTrang: LienKet[]
  chanTrang: { ten: string, links: LienKet[] }[]
}
export interface BanTai {
  ma: 'android' | 'pc' | 'jar' | 'ios'
  ten: string
  moTa: string
  url: string
  phienBan: string
  dungLuong: string
  capNhat: string
  nhatKy: string
  hien: boolean
}
export interface CmsTaiGame { ban: BanTai[] }

export interface CmsCongKhai {
  site: CmsSite
  trangChu: CmsTrangChu
  menu: CmsMenu
  taiGame: CmsTaiGame
}

export const CMS_MAC_DINH: CmsCongKhai = {
  site: {
    tenSite: 'Huyền Thoại Bảo Bối',
    khauHieu: 'MMORPG nơi chính bạn là một Pokémon',
    logo: '',
    moTaSeo: 'MMORPG nơi chính bạn là một Pokémon: chọn 1 trong 5 Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu cùng bạn bè. Miễn phí, không nạp tiền.',
    anhOg: '/og/htbb.png',
    mangXaHoi: [],
    baoTri: { bat: false, tieuDe: 'Máy chủ đang bảo trì', noiDung: 'Chúng tôi đang nâng cấp hệ thống. Vui lòng quay lại sau ít phút.' },
    thongBao: { bat: false, noiDung: '', to: '', mau: 'vang' },
    trangThaiUrl: '',
  },
  trangChu: {
    hero: {
      nhan: 'Huyền Thoại Bảo Bối · MMORPG',
      dong1: 'Bạn không huấn luyện Pokémon.',
      dong2: 'Bạn chính là Pokémon!',
      moTa: 'Chọn một Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu thời gian thực cùng bạn bè: săn Pokémon hoang dã, hạ boss huyền thoại, lập đội và tranh tài PvP.',
      nut: [
        { label: 'Tải game ngay', to: '/download', kieu: 'chinh' },
        { label: 'Tạo tài khoản', to: '/register', kieu: 'phu' },
      ],
    },
    tinhNangTieuDe: 'MMO Pokémon, theo cách của bạn',
    tinhNang: [
      { icon: 'i-lucide-paw-print', t: 'Bạn là Pokémon', m: 'Không huấn luyện — hoá thân. Lên cấp là tiến hoá ngay trên bản đồ.', to: '/gioi-thieu' },
      { icon: 'i-lucide-flame', t: '18 hệ khắc chế', m: 'Đánh đúng hệ ×1,5–×2. Bảng khắc hệ tương tác 18×18.', to: '/cam-nang/he-khac-che' },
      { icon: 'i-lucide-sparkles', t: 'Tiến hoá & 8 nhánh Eevee', m: 'Charizard, Venusaur, Blastoise, Raichu hay Sylveon?', to: '/cam-nang/tien-hoa' },
      { icon: 'i-lucide-skull', t: 'Đột kích huyền thoại', m: 'Zapdos, Rayquaza, Kyogre & Groudon — hạ boss cùng đội.', to: '/cam-nang/boss-dot-kich' },
      { icon: 'i-lucide-trophy', t: 'PvP xếp hạng mùa', m: 'Bốn mùa một năm, leo bậc Poké → Master.', to: '/cam-nang/pvp' },
      { icon: 'i-lucide-heart-handshake', t: 'Không nạp tiền', m: 'Phi thương mại, không bán sức mạnh. Kinh tế được mô phỏng 2 năm.', to: '/cam-nang/kinh-te' },
    ],
    khoiDauThuTu: [0, 1, 3, 4, 2],
    banner: [],
    hienCanhPhim: true,
  },
  menu: {
    dauTrang: [
      { label: 'Giới thiệu', to: '/gioi-thieu', icon: 'i-lucide-info' },
      { label: 'Tân thủ', to: '/huong-dan', icon: 'i-lucide-compass' },
      { label: 'Cẩm nang', to: '/cam-nang', icon: 'i-lucide-library' },
      { label: 'Pokédex', to: '/pokedex', icon: 'i-lucide-book-open' },
      { label: 'Tin tức', to: '/news', icon: 'i-lucide-newspaper' },
      { label: 'Xếp hạng', to: '/ranking', icon: 'i-lucide-trophy' },
      { label: 'Tải game', to: '/download', icon: 'i-lucide-download' },
    ],
    chanTrang: [
      { ten: 'Trò chơi', links: [
        { label: 'Giới thiệu', to: '/gioi-thieu' },
        { label: 'Hướng dẫn tân thủ', to: '/huong-dan' },
        { label: 'Tải game', to: '/download' },
        { label: 'Tin tức & sự kiện', to: '/news' },
      ] },
      { ten: 'Cẩm nang', links: [
        { label: 'Hệ & khắc hệ', to: '/cam-nang/he-khac-che' },
        { label: 'Cấp độ & tiến hoá', to: '/cam-nang/cap-do' },
        { label: 'Trang bị & nâng cấp', to: '/cam-nang/trang-bi' },
        { label: 'Lịch sự kiện năm', to: '/cam-nang/su-kien' },
        { label: 'Câu hỏi thường gặp', to: '/cam-nang/faq' },
      ] },
      { ten: 'Cộng đồng', links: [
        { label: 'Pokédex', to: '/pokedex' },
        { label: 'Bảng xếp hạng', to: '/ranking' },
        { label: 'Hỗ trợ', to: '/download#support' },
        { label: 'Đăng ký tài khoản', to: '/register' },
      ] },
      { ten: 'Pháp lý', links: [
        { label: 'Ghi công & giấy phép', to: '/ghi-cong' },
        { label: 'Điều khoản sử dụng', to: '/dieu-khoan' },
        { label: 'Quyền riêng tư', to: '/quyen-rieng-tu' },
      ] },
    ],
  },
  taiGame: {
    ban: [
      { ma: 'android', ten: 'Android', moTa: 'Bản cài đặt APK — đồ hoạ đầy đủ, điều khiển cảm ứng', url: '', phienBan: '', dungLuong: '', capNhat: '', nhatKy: '', hien: true },
      { ma: 'pc', ten: 'PC / Windows', moTa: 'Chơi trên máy tính bằng bàn phím', url: '', phienBan: '', dungLuong: '', capNhat: '', nhatKy: '', hien: true },
      { ma: 'jar', ten: 'Java (.jar)', moTa: 'Điện thoại phím Java hoặc giả lập J2ME', url: '', phienBan: '', dungLuong: '', capNhat: '', nhatKy: '', hien: true },
      { ma: 'ios', ten: 'iOS', moTa: 'Hướng dẫn cài đặt', url: '', phienBan: '', dungLuong: '', capNhat: '', nhatKy: '', hien: false },
    ],
  },
}

export const KHOA_CMS = ['site', 'trangChu', 'menu', 'taiGame'] as const
export type KhoaCms = typeof KHOA_CMS[number]

/** Trộn sâu: object con trộn theo khoá, mảng/giá trị đơn từ `de` thay hẳn `goc` (nếu đúng kiểu). */
export function tronSau<T>(goc: T, de: unknown): T {
  if (de === undefined || de === null) return goc
  if (Array.isArray(goc)) return (Array.isArray(de) ? de : goc) as T
  if (goc && typeof goc === 'object') {
    if (typeof de !== 'object' || Array.isArray(de)) return goc
    const ra: Record<string, unknown> = { ...(goc as Record<string, unknown>) }
    for (const k of Object.keys(goc as object)) ra[k] = tronSau((goc as Record<string, unknown>)[k], (de as Record<string, unknown>)[k])
    return ra as T
  }
  return (typeof de === typeof goc ? de : goc) as T
}

export function hopNhatCms(db: Partial<Record<KhoaCms, unknown>>): CmsCongKhai {
  return {
    site: tronSau(CMS_MAC_DINH.site, db.site),
    trangChu: tronSau(CMS_MAC_DINH.trangChu, db.trangChu),
    menu: tronSau(CMS_MAC_DINH.menu, db.menu),
    taiGame: tronSau(CMS_MAC_DINH.taiGame, db.taiGame),
  }
}

/** Liên kết an toàn để đặt vào href: đường dẫn trong site, #, http(s), mailto. */
export function lienKetAnToan(u: unknown): string {
  const s = typeof u === 'string' ? u.trim() : ''
  if (s === '') return ''
  if (/^(https?:\/\/|mailto:|\/(?!\/)|#)/i.test(s)) return s
  return ''
}
