/**
 * Bản tải game — phần THUẦN (không import gì, có test): danh mục nền tảng, khuôn một bản tải,
 * nắn dữ liệu CMS (web_cai_dat khoá 'taiGame'), dựng link cài đặt, nhận diện thiết bị.
 * Dùng chung: shared/utils/cmsMacDinh.ts (mặc định + hợp nhất), server lưu (nanCaiDatLuu),
 * /admin/tai-game, /download, tests/tai-game.test.ts.
 */

/**
 * Đúng 6 nền tảng, theo các client có thật trong monorepo:
 *   android  client_apk (APK)            pc       client_pc_ios (Unity, Windows .zip)
 *   pc-java  client_pc gói Windows (.exe + Java đi kèm)   pc-jar  client_pc HTBB-PC.jar (Windows/macOS/Linux)
 *   jar      client_jar (J2ME)            ios      client_pc_ios (Unity iOS) — sắp ra mắt
 */
export type MaNenTang = 'android' | 'pc' | 'pc-java' | 'pc-jar' | 'jar' | 'ios'

/**
 * Kiểu link:
 *   tep          : tải file trực tiếp (.apk / .zip / .jar / .AppImage …) — url là https://… hoặc /tai/<tệp>
 *   testflight   : https://testflight.apple.com/join/…
 *   app-store    : https://apps.apple.com/…
 *   ipa-manifest : URL https của manifest.plist (cài enterprise / ad-hoc) -> itms-services://
 */
export type KieuLinkTai = 'tep' | 'testflight' | 'app-store' | 'ipa-manifest'
export type TrangThaiBanTai = 'san-sang' | 'sap-ra-mat'

export interface BanTai {
  ma: MaNenTang
  ten: string
  moTa: string
  /** https://…, /tai/<tệp> (kho tệp game của web) hoặc đường dẫn trong site. */
  url: string
  kieu: KieuLinkTai
  phienBan: string
  dungLuong: string
  capNhat: string
  nhatKy: string
  /** SHA-256 hex (64 ký tự) của tệp — người chơi tự kiểm được tệp không bị sửa. */
  sha256: string
  /** Hệ điều hành tối thiểu, vd. "Android 8.0", "Windows 10 (64-bit)", "iOS 15". */
  heDieuHanh: string
  trangThai: TrangThaiBanTai
  hien: boolean
}

export interface NenTang {
  ma: MaNenTang
  ten: string
  moTa: string
  icon: string
  /** Kiểu link được phép. */
  kieu: KieuLinkTai[]
  heDieuHanh: string
  /** Gợi ý ngắn trên thẻ. */
  chiTiet: string[]
  /** Hiện mặc định (chưa lưu CMS). */
  hien: boolean
  trangThai: TrangThaiBanTai
  /** Các bước cài đặt (trang Hướng dẫn tân thủ). iOS: theo kiểu link, xem BUOC_IOS. */
  buoc: string[]
}

export const DS_NEN_TANG: readonly NenTang[] = [
  { ma: 'android', ten: 'Android', moTa: 'Bản cài đặt APK — đồ hoạ đầy đủ, điều khiển cảm ứng', icon: 'i-lucide-smartphone', kieu: ['tep'], heDieuHanh: 'Android 8.0 trở lên', chiTiet: ['File .apk', 'Cài ngoài CH Play', 'Cảm ứng + bàn phím'], buoc: ['Mở trang Tải game trên điện thoại, bấm tải bản Android.', 'Khi được hỏi, cho phép "Cài ứng dụng không rõ nguồn" cho trình duyệt.', 'Mở file .apk vừa tải và bấm Cài đặt.', 'Mở game, chọn máy chủ và đăng nhập.'], hien: true, trangThai: 'san-sang' },
  { ma: 'pc', ten: 'PC Windows', moTa: 'Bản PC đồ hoạ đầy đủ — giải nén rồi chạy HuyenThoaiBaoBoi.exe', icon: 'i-lucide-monitor', kieu: ['tep'], heDieuHanh: 'Windows 10 / 11 (64-bit)', chiTiet: ['File nén .zip', 'Giải nén rồi chạy .exe', 'Màn hình lớn'], buoc: ['Tải bản PC Windows (file nén .zip).', 'Giải nén ra một thư mục (không chạy trực tiếp trong file nén).', 'Chạy HuyenThoaiBaoBoi.exe trong thư mục. Nếu Windows SmartScreen cảnh báo: "Thông tin thêm" → "Vẫn chạy".', 'Chọn máy chủ, đăng nhập và chơi bằng bàn phím.'], hien: true, trangThai: 'san-sang' },
  { ma: 'pc-java', ten: 'PC Windows (bản Java)', moTa: 'Bản PC chạy cùng lõi với bản Java — có sẵn Java đi kèm, không phải cài thêm gì', icon: 'i-lucide-laptop', kieu: ['tep'], heDieuHanh: 'Windows 10 / 11 (64-bit)', chiTiet: ['File .zip hoặc bộ cài .exe', 'Không cần cài Java', 'Bàn phím + chuột'], buoc: ['Tải bản PC Windows (bản Java): file nén .zip hoặc bộ cài .exe.', 'Bộ cài: chạy và bấm Tiếp cho tới khi xong. File nén: giải nén ra một thư mục.', 'Chạy HTBB.exe (hoặc lối tắt Huyền Thoại Bảo Bối). Nếu Windows SmartScreen cảnh báo: "Thông tin thêm" → "Vẫn chạy".', 'Đăng nhập bằng cùng tài khoản như các bản khác.'], hien: true, trangThai: 'san-sang' },
  { ma: 'pc-jar', ten: 'PC Java (.jar)', moTa: 'Một file chạy trên Windows, macOS và Linux đã cài Java', icon: 'i-lucide-coffee', kieu: ['tep'], heDieuHanh: 'Windows / macOS / Linux có Java 8 trở lên', chiTiet: ['File .jar', 'Windows · macOS · Linux', 'Bàn phím + chuột'], buoc: ['Cài Java 8 trở lên nếu máy chưa có (ví dụ Eclipse Temurin, miễn phí).', 'Tải bản PC Java (file .jar) và để trong một thư mục riêng.', 'Bấm đúp file .jar, hoặc chạy lệnh: java -jar HTBB-PC.jar', 'Đăng nhập bằng cùng tài khoản như các bản khác.'], hien: true, trangThai: 'san-sang' },
  { ma: 'jar', ten: 'Điện thoại Java (J2ME)', moTa: 'Điện thoại phím Java hoặc giả lập J2ME', icon: 'i-lucide-cpu', kieu: ['tep'], heDieuHanh: 'Java MIDP 2.0, hoặc J2ME Loader trên Android', chiTiet: ['Bản thường & bản nhẹ', 'Máy yếu chơi mượt', 'Phím số'], buoc: ['Dành cho điện thoại hỗ trợ Java (J2ME) hoặc giả lập (J2ME Loader trên Android).', 'Tải file .jar (bản thường) hoặc bản nhẹ cho máy yếu.', 'Mở file .jar bằng điện thoại/giả lập để cài.', 'Đăng nhập bằng cùng tài khoản như các bản khác.'], hien: true, trangThai: 'san-sang' },
  { ma: 'ios', ten: 'iOS', moTa: 'iPhone / iPad', icon: 'i-lucide-tablet-smartphone', kieu: ['testflight', 'app-store', 'ipa-manifest'], heDieuHanh: 'iOS 15 trở lên', chiTiet: ['TestFlight / App Store'], buoc: [], hien: true, trangThai: 'sap-ra-mat' },
]

/** Bước cài iOS theo kiểu link admin chọn. */
export const BUOC_IOS: Record<KieuLinkTai, string[]> = {
  'testflight': ['Cài ứng dụng TestFlight (miễn phí) từ App Store.', 'Mở trang Tải game trên iPhone/iPad, bấm “Cài qua TestFlight”.', 'Trong TestFlight bấm Chấp nhận → Cài đặt.', 'Mở game và đăng nhập bằng tài khoản của bạn.'],
  'app-store': ['Mở trang Tải game trên iPhone/iPad, bấm “Mở App Store”.', 'Bấm Nhận / Cài đặt.', 'Mở game và đăng nhập bằng tài khoản của bạn.'],
  'ipa-manifest': ['Mở trang Tải game bằng Safari trên iPhone/iPad, bấm “Cài trên iPhone/iPad”.', 'Vào Cài đặt → Cài đặt chung → Quản lý VPN & thiết bị, tin cậy nhà phát triển.', 'Mở game và đăng nhập bằng tài khoản của bạn.'],
  'tep': [],
}

/** Bước cài của một bản tải (iOS theo kiểu link; còn lại theo danh mục). */
export function buocCaiDat(b: Pick<BanTai, 'ma' | 'kieu'>): string[] {
  if (b.ma === 'ios') return BUOC_IOS[b.kieu]
  return nenTang(b.ma)?.buoc ?? []
}

export const MA_NEN_TANG: readonly MaNenTang[] = DS_NEN_TANG.map(n => n.ma)

export const TEN_KIEU_LINK: Record<KieuLinkTai, string> = {
  'tep': 'Tải tệp',
  'testflight': 'TestFlight',
  'app-store': 'App Store',
  'ipa-manifest': 'Manifest .ipa (cài enterprise / ad-hoc)',
}

export function nenTang(ma: string): NenTang | undefined {
  return DS_NEN_TANG.find(n => n.ma === ma)
}

export function banTaiMacDinh(n: NenTang): BanTai {
  return {
    ma: n.ma, ten: n.ten, moTa: n.moTa, url: '', kieu: n.kieu[0]!, phienBan: '', dungLuong: '', capNhat: '', nhatKy: '',
    sha256: '', heDieuHanh: n.heDieuHanh, trangThai: n.trangThai, hien: n.hien,
  }
}

const chuoi = (v: unknown, max: number, macDinh = '') => (typeof v === 'string' ? v.trim().slice(0, max) : macDinh)

/** Link được nhận: https://…, http://localhost (dev), hoặc đường dẫn trong site (/tai/…, /huong-dan…). Sai -> ''. */
export function urlTaiHopLe(u: unknown): string {
  const s = typeof u === 'string' ? u.trim() : ''
  if (!s || s === '#') return ''
  if (/^\/(?!\/)[\w\-./%~]*$/.test(s) && !s.includes('..')) return s
  try {
    const x = new URL(s)
    if (x.username || x.password) return ''
    if (x.protocol === 'https:') return x.href
    if (x.protocol === 'http:' && (x.hostname === 'localhost' || x.hostname === '127.0.0.1')) return x.href
  }
  catch { /* sai URL */ }
  return ''
}

const HOST_KIEU: Partial<Record<KieuLinkTai, string[]>> = {
  'testflight': ['testflight.apple.com'],
  'app-store': ['apps.apple.com', 'itunes.apple.com'],
}

/** Lỗi của một bản tải (để server báo 400), hoặc null. */
export function loiBanTai(b: BanTai): string | null {
  const n = nenTang(b.ma)
  if (!n) return 'Nền tảng không hợp lệ.'
  if (!n.kieu.includes(b.kieu)) return `${n.ten}: kiểu link "${b.kieu}" không dùng được cho nền tảng này.`
  if (b.url) {
    const u = urlTaiHopLe(b.url)
    if (!u) return `${n.ten}: link tải phải là https://… hoặc đường dẫn trong website (vd. /tai/ten-tep.zip).`
    const host = HOST_KIEU[b.kieu]
    if (host && !host.includes(new URL(u, 'https://x.invalid').hostname)) return `${n.ten}: link ${TEN_KIEU_LINK[b.kieu]} phải thuộc ${host.join(' / ')}.`
    if (b.kieu === 'ipa-manifest' && !u.startsWith('https://')) return `${n.ten}: manifest .ipa phải là URL https:// đầy đủ (iOS bắt buộc).`
  }
  if (b.sha256 && !/^[0-9a-f]{64}$/.test(b.sha256)) return `${n.ten}: SHA-256 phải gồm đúng 64 ký tự 0-9 a-f.`
  return null
}

/** Nắn danh sách bản tải từ CMS: mỗi nền tảng đúng một lần (theo thứ tự đã lưu), thiếu thì thêm mặc định. */
export function chuanHoaBanTai(raw: unknown): BanTai[] {
  const ds = Array.isArray(raw) ? raw : []
  const ra: BanTai[] = []
  for (const x of ds) {
    if (!x || typeof x !== 'object') continue
    const r = x as Record<string, unknown>
    const n = nenTang(String(r.ma ?? ''))
    if (!n || ra.some(b => b.ma === n.ma)) continue
    const md = banTaiMacDinh(n)
    const kieu = n.kieu.includes(r.kieu as KieuLinkTai) ? r.kieu as KieuLinkTai : md.kieu
    // Dữ liệu cũ (trước khi có trangThai): có link = sẵn sàng.
    const url = chuoi(r.url, 500)
    const trangThai: TrangThaiBanTai = r.trangThai === 'san-sang' || r.trangThai === 'sap-ra-mat'
      ? r.trangThai
      : (url && url !== '#' ? 'san-sang' : md.trangThai)
    ra.push({
      ma: n.ma,
      ten: chuoi(r.ten, 60) || md.ten,
      moTa: chuoi(r.moTa, 200, md.moTa),
      url: url === '#' ? '' : url,
      kieu,
      phienBan: chuoi(r.phienBan, 40),
      dungLuong: chuoi(r.dungLuong, 40),
      capNhat: chuoi(r.capNhat, 40),
      nhatKy: chuoi(r.nhatKy, 3000),
      sha256: chuoi(r.sha256, 64).toLowerCase(),
      heDieuHanh: chuoi(r.heDieuHanh, 120, md.heDieuHanh),
      trangThai,
      hien: typeof r.hien === 'boolean' ? r.hien : md.hien,
    })
  }
  for (const n of DS_NEN_TANG) if (!ra.some(b => b.ma === n.ma)) ra.push(banTaiMacDinh(n))
  return ra
}

/** href để bấm cài: ipa-manifest -> itms-services; còn lại -> url đã kiểm. '' = chưa có link. */
export function hrefCaiDat(b: Pick<BanTai, 'url' | 'kieu'>): string {
  const u = urlTaiHopLe(b.url)
  if (!u) return ''
  if (b.kieu === 'ipa-manifest') return u.startsWith('https://') ? `itms-services://?action=download-manifest&url=${encodeURIComponent(u)}` : ''
  return u
}

/** Bản có bấm tải được không (hiện + sẵn sàng + có link). */
export function banTaiSanSang(b: BanTai): boolean {
  return b.hien && b.trangThai === 'san-sang' && hrefCaiDat(b) !== ''
}

/** Nền tảng gợi ý theo User-Agent (chỉ để chọn nút chính; không gửi đi đâu). */
export function nenTangTheoUa(ua: string): MaNenTang | null {
  const s = ua.toLowerCase()
  if (/iphone|ipad|ipod/.test(s) || (s.includes('macintosh') && s.includes('mobile'))) return 'ios'
  if (s.includes('android')) return 'android'
  if (/j2me|midp|cldc/.test(s)) return 'jar'
  if (s.includes('windows')) return 'pc'
  if (s.includes('mac os x') || s.includes('macintosh') || s.includes('linux') || s.includes('x11')) return 'pc-jar'
  return null
}

/** Tên tệp game được nhận khi tải lên kho /tai/: đuôi theo nền tảng. */
export const DUOI_TEP_GAME = ['apk', 'zip', 'jar', 'jad', 'exe', 'gz', 'tgz', 'appimage', 'ipa', 'plist'] as const

/** Tên tệp an toàn để lưu/phục vụ: chữ không dấu, số, . _ -, đuôi trong danh sách. null = không nhận. */
export function tenTepGameAnToan(ten: string): string | null {
  const goc = String(ten ?? '').split(/[\\/]/).pop() ?? ''
  const s = goc.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D')
    .replace(/[^\w.\-]+/g, '-').replace(/-+/g, '-').replace(/^[-.]+/, '').slice(0, 100)
  const m = s.match(/\.([a-z0-9]+)$/i)
  if (!m || !(DUOI_TEP_GAME as readonly string[]).includes(m[1]!.toLowerCase())) return null
  if (s.length < 3) return null
  return s
}

/**
 * Kiểm chữ ký byte đầu tệp theo đuôi (không tin tên tệp): zip/apk/jar/ipa = "PK\x03\x04", exe = "MZ",
 * gz/tgz = 1F 8B, AppImage = ELF, jad/plist = văn bản (không có byte 0 trong 4 KB đầu).
 */
export function chuKyTepHopLe(duoi: string, dau: Uint8Array): boolean {
  const d = duoi.toLowerCase()
  const b = (i: number) => dau[i] ?? -1
  if (['zip', 'apk', 'jar', 'ipa'].includes(d)) return b(0) === 0x50 && b(1) === 0x4B && b(2) === 0x03 && b(3) === 0x04
  if (d === 'exe') return b(0) === 0x4D && b(1) === 0x5A
  if (d === 'gz' || d === 'tgz') return b(0) === 0x1F && b(1) === 0x8B
  if (d === 'appimage') return b(0) === 0x7F && b(1) === 0x45 && b(2) === 0x4C && b(3) === 0x46
  if (d === 'jad' || d === 'plist') return dau.length > 0 && !dau.subarray(0, 4096).includes(0)
  return false
}

/** Dung lượng dễ đọc: 85,3 MB. */
export function dungLuongDeDoc(byte: number): string {
  if (!Number.isFinite(byte) || byte <= 0) return ''
  const dv = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let v = byte
  while (v >= 1024 && i < dv.length - 1) { v /= 1024; i++ }
  return `${i === 0 ? v : v.toFixed(1).replace('.', ',')} ${dv[i]}`
}
