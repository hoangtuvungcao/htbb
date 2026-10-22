/**
 * Nội dung MẶC ĐỊNH của các khối sửa được trong khu quản trị (CMS).
 *
 * Website luôn chạy được khi CSDL chưa có bảng web_cai_dat (hoặc admin chưa nhập gì): trang đọc
 * /api/cong-khai, server trộn giá trị trong CSDL ĐÈ LÊN mặc định ở đây (hopNhatCms). Admin bấm
 * "Khôi phục mặc định" là xoá dòng trong CSDL -> quay về đúng các giá trị này.
 */
import { DS_NEN_TANG, banTaiMacDinh, chuanHoaBanTai, type BanTai } from './taiGame.ts'
import { chuanHoaDauTrang, type MucMenu } from './menuDauTrang.ts'

export interface LienKet { label: string, to: string, icon?: string }
export interface NutCta { label: string, to: string, kieu: 'chinh' | 'phu' }
export interface TheTinhNang { icon: string, t: string, m: string, to: string }
export interface Banner { anh: string, tieuDe: string, moTa: string, to: string, hien: boolean }

/** Kênh hỗ trợ chính thức: nhóm / Official Account Zalo. url trống = chưa có (ẩn mọi nút). */
export interface HoTroZalo {
  /** https://zalo.me/g/... (nhóm) hoặc https://zalo.me/<id OA> / https://oa.zalo.me/... — xem lienKetZalo. */
  url: string
  /** Ảnh QR chọn từ thư viện ảnh (/uploads/...) — tuỳ chọn. */
  anhQr: string
  /** Tên hiện trên nút/thẻ, vd. "Nhóm Zalo Huyền Thoại Bảo Bối". */
  ten: string
  /** Một câu mô tả ngắn dưới tên. */
  moTa: string
}

export interface CmsSite {
  tenSite: string
  khauHieu: string
  logo: string
  moTaSeo: string
  anhOg: string
  mangXaHoi: { ten: string, url: string, icon: string }[]
  hoTroZalo: HoTroZalo
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
  /** Menu đầu trang — vị trí từng mục (thanh / Thêm ▾ / nút nổi bật): shared/utils/menuDauTrang.ts. */
  dauTrang: MucMenu[]
  chanTrang: { ten: string, links: LienKet[] }[]
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
    moTaSeo: 'MMORPG nơi chính bạn là một Pokémon: chọn 1 trong 5 Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu cùng bạn bè. Miễn phí, không bán sức mạnh.',
    anhOg: '/og/htbb.png',
    mangXaHoi: [],
    hoTroZalo: {
      url: '',
      anhQr: '',
      ten: 'Hỗ trợ qua Zalo',
      moTa: 'Hỏi đáp, báo lỗi, khiếu nại khoá tài khoản. Ban quản trị không bao giờ hỏi mật khẩu của bạn.',
    },
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
      { icon: 'i-lucide-paw-print', t: 'Bạn là Pokémon', m: 'Không huấn luyện — hoá thân. Chọn 1 trong 5 Pokémon khởi đầu, lên cấp là tiến hoá ngay trên bản đồ. 1.025 loài trong Pokédex.', to: '/gioi-thieu' },
      { icon: 'i-lucide-compass', t: 'Hướng dẫn tân thủ', m: '10 bước trong làng khởi đầu Kanto hoặc Johto; đã quen chơi thì bỏ qua mà vẫn đủ thưởng.', to: '/huong-dan#30-phut' },
      { icon: 'i-lucide-circle-dot', t: 'Thu phục & Hộp PC', m: 'Đánh yếu rồi ném Poké / Great / Ultra Ball, nuôi Kẹo loài, tiến hoá nhiều nhánh, chọn bạn đồng hành đi theo.', to: '/cam-nang/thu-phuc' },
      { icon: 'i-lucide-repeat', t: 'Trao đổi & chợ Pokémon', m: 'Đổi Pokémon với bạn bè, mua bán ở chợ có khung giá máy chủ — loài hiếm giữ giá.', to: '/cam-nang/trao-doi-cho' },
      { icon: 'i-lucide-castle', t: 'Phó bản Hầm ngục', m: 'Nhóm tối đa 4 người, 5 tầng, boss tầng 5, chủ đề đổi theo thứ trong tuần.', to: '/cam-nang/ham-nguc#pho-ban' },
      { icon: 'i-lucide-crosshair', t: 'PvP ngắm tay & Cúp tuần', m: 'Kéo chiêu để ngắm trong mọi trận PvP; Cúp PvP Thuỵ Sĩ mỗi Chủ nhật; thang xếp hạng mùa.', to: '/cam-nang/pvp' },
      { icon: 'i-lucide-cloud-sun-rain', t: 'Thời tiết & ngày cộng đồng', m: 'Trời đổi theo khung 6 giờ, hệ hợp thời tiết xuất hiện nhiều; mỗi tháng 3 giờ vàng săn shiny.', to: '/cam-nang/thoi-tiet' },
      { icon: 'i-lucide-shirt', t: 'Tủ đồ & danh hiệu', m: 'Trang phục hiện trên Pokémon, hào quang, skin thú, danh hiệu 4 bậc — chỉ số nhỏ theo ô, tổng ≤ 5%.', to: '/cam-nang/trang-phuc' },
      { icon: 'i-lucide-messages-square', t: 'Chat nhiều kênh', m: 'Bản đồ, Thế giới, Đội, Nhóm, Bạn bè — hội thoại riêng hai phía, chỉ giữ trong phiên chơi.', to: '/cam-nang/kenh-chat' },
      { icon: 'i-lucide-heart', t: 'Lễ kết đôi', m: 'Hai Pokémon cùng nhóm trứng tổ chức lễ, mời bạn bè tới dự.', to: '/cam-nang/ket-doi' },
      { icon: 'i-lucide-skull', t: 'Đột kích huyền thoại', m: 'Zapdos, Rayquaza, Kyogre & Groudon — hạ boss cùng đội.', to: '/cam-nang/boss-dot-kich' },
      { icon: 'i-lucide-heart-handshake', t: 'Không bán sức mạnh', m: 'Lượng chỉ mua hình; lượng khoá không giao dịch được, đồ khoá khi mặc, chuyển giá trị có chốt chống clone.', to: '/cam-nang/kinh-te' },
    ],
    khoiDauThuTu: [0, 1, 3, 4, 2],
    banner: [],
    hienCanhPhim: true,
  },
  menu: {
    // Thanh: Trang chủ · Tin tức · Cẩm nang · Pokédex · Xếp hạng + "Thêm ▾" (Tân thủ, Giới thiệu) + nút Tải game.
    // Nạp / Thành viên / Kích hoạt nằm ở menu tài khoản (bên phải).
    dauTrang: [
      { label: 'Trang chủ', to: '/', icon: 'i-lucide-house', vi: 'chinh' },
      { label: 'Tin tức', to: '/news', icon: 'i-lucide-newspaper', vi: 'chinh' },
      { label: 'Cẩm nang', to: '/cam-nang', icon: 'i-lucide-library', vi: 'chinh' },
      { label: 'Pokédex', to: '/pokedex', icon: 'i-lucide-book-open', vi: 'chinh' },
      { label: 'Xếp hạng', to: '/ranking', icon: 'i-lucide-trophy', vi: 'chinh' },
      { label: 'Tân thủ', to: '/huong-dan', icon: 'i-lucide-compass', vi: 'them' },
      { label: 'Giới thiệu', to: '/gioi-thieu', icon: 'i-lucide-info', vi: 'them' },
      { label: 'Tải game', to: '/download', icon: 'i-lucide-download', vi: 'nut' },
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
        { label: 'Cấp độ & EXP', to: '/cam-nang/cap-do' },
        { label: 'Tiến hoá', to: '/cam-nang/tien-hoa' },
        { label: 'Trang bị & nâng cấp', to: '/cam-nang/trang-bi' },
        { label: 'Sổ tay huấn luyện', to: '/cam-nang/so-tay' },
        { label: 'Gym & huy hiệu', to: '/cam-nang/gym' },
        { label: 'Lịch sự kiện năm', to: '/cam-nang/su-kien' },
        { label: 'Câu hỏi thường gặp', to: '/cam-nang/faq' },
      ] },
      { ten: 'Cộng đồng', links: [
        { label: 'Pokédex', to: '/pokedex' },
        { label: 'Bảng xếp hạng', to: '/ranking' },
        { label: 'Hỗ trợ', to: '/download#support' },
        { label: 'Đăng ký tài khoản', to: '/register' },
        { label: 'Quy tắc tài khoản', to: '/cam-nang/tai-khoan' },
      ] },
      { ten: 'Pháp lý', links: [
        { label: 'Điều khoản sử dụng', to: '/dieu-khoan' },
        { label: 'Quyền riêng tư', to: '/quyen-rieng-tu' },
      ] },
    ],
  },
  // Danh mục nền tảng + khuôn bản tải: shared/utils/taiGame.ts
  taiGame: { ban: DS_NEN_TANG.map(banTaiMacDinh) },
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

/** Nắn khối menu: mục đầu trang có vị trí (menu cũ không có trường `vi` vẫn hiện gọn). */
export function chuanHoaMenu(m: CmsMenu): CmsMenu {
  const dauTrang = chuanHoaDauTrang(m.dauTrang)
  return { ...m, dauTrang: dauTrang.length ? dauTrang : chuanHoaDauTrang(CMS_MAC_DINH.menu.dauTrang) }
}

export function hopNhatCms(db: Partial<Record<KhoaCms, unknown>>): CmsCongKhai {
  return {
    site: chuanHoaCmsSite(tronSau(CMS_MAC_DINH.site, db.site)),
    trangChu: tronSau(CMS_MAC_DINH.trangChu, db.trangChu),
    menu: chuanHoaMenu(tronSau(CMS_MAC_DINH.menu, db.menu)),
    taiGame: { ban: chuanHoaBanTai((db.taiGame as { ban?: unknown } | undefined)?.ban) },
  }
}

/** Liên kết an toàn để đặt vào href: đường dẫn trong site, #, http(s), mailto. */
export function lienKetAnToan(u: unknown): string {
  const s = typeof u === 'string' ? u.trim() : ''
  if (s === '') return ''
  if (/^(https?:\/\/|mailto:|\/(?!\/)|#)/i.test(s)) return s
  return ''
}

/** Host Zalo được nhận cho link hỗ trợ (nhóm: zalo.me/g/..., OA: zalo.me/<id> hoặc oa.zalo.me). */
export const HOST_ZALO = ['zalo.me', 'chat.zalo.me', 'oa.zalo.me'] as const

/** Link Zalo hợp lệ (https, đúng host Zalo, không cổng / user:pass) -> href đã chuẩn hoá; sai -> ''. */
export function lienKetZalo(u: unknown): string {
  const s = typeof u === 'string' ? u.trim() : ''
  if (s === '' || s.length > 300) return ''
  let url: URL
  try { url = new URL(s) }
  catch { return '' }
  if (url.protocol !== 'https:' || url.username || url.password || url.port) return ''
  if (!(HOST_ZALO as readonly string[]).includes(url.hostname.toLowerCase())) return ''
  if (url.pathname === '/' && !url.search) return '' // chỉ trang chủ Zalo, không phải nhóm/OA nào
  return url.href
}

/** Ảnh trong site (thư viện /uploads/... hoặc file tĩnh /...) — không nhận // hay URL ngoài. */
export function anhTrongSite(u: unknown): string {
  const s = typeof u === 'string' ? u.trim() : ''
  if (s === '' || s.length > 300) return ''
  return /^\/(?!\/)[A-Za-z0-9._~\/-]+$/.test(s) && !s.includes('..') ? s : ''
}

/** Link mạng xã hội thật: http(s) có host. "#", "https://" (ô để trống) bị bỏ -> không có link chết. */
export function lienKetNgoaiThat(u: unknown): string {
  const s = typeof u === 'string' ? u.trim() : ''
  if (!/^https?:\/\//i.test(s)) return ''
  try {
    const url = new URL(s)
    return url.hostname.includes('.') && !url.username && !url.password ? url.href : ''
  }
  catch { return '' }
}

/** Nắn khối 'site' trước khi lưu / trả ra: link Zalo, ảnh QR, mạng xã hội rỗng. */
export function chuanHoaCmsSite(site: CmsSite): CmsSite {
  const z = site.hoTroZalo
  return {
    ...site,
    hoTroZalo: {
      url: lienKetZalo(z.url),
      anhQr: anhTrongSite(z.anhQr),
      ten: z.ten.trim().slice(0, 80) || CMS_MAC_DINH.site.hoTroZalo.ten,
      moTa: z.moTa.trim().slice(0, 300),
    },
    mangXaHoi: site.mangXaHoi
      .filter(m => m && typeof m.url === 'string' && lienKetNgoaiThat(m.url))
      .map(m => ({ ten: String(m.ten ?? '').trim().slice(0, 40), url: lienKetNgoaiThat(m.url), icon: /^i-[a-z0-9-]+$/.test(String(m.icon ?? '')) ? m.icon : 'i-lucide-link' })),
  }
}
