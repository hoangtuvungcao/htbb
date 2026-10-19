/**
 * Bản tải game — phần THUẦN (không import gì, có test): danh mục nền tảng, khuôn một bản tải,
 * nắn dữ liệu CMS (web_cai_dat khoá 'taiGame'), dựng link cài đặt, nhận diện thiết bị.
 * Dùng chung: shared/utils/cmsMacDinh.ts (mặc định + hợp nhất), server lưu (nanCaiDatLuu),
 * /admin/tai-game, /download, tests/tai-game.test.ts.
 */

/**
 * Đúng 5 bản tải (chủ game chốt 19/10/2026), mỗi nền tảng MỘT tệp:
 *   jar      client_jar (J2ME) bản "thuong" — điện thoại Java và giả lập J2ME (J2ME Loader trên Android)
 *   windows  client_pc bộ cài Windows (HTBB-PC-Setup-<bản>.exe, có Java đi kèm)
 *   linux    client_pc bản Linux — MỘT tệp .AppImage có Java đi kèm (từ 0.9.5 không còn .tar.gz)
 *   android  client_apk (APK)
 *   ios      client_pc_ios (Unity iOS) — sắp ra mắt
 * Đã bỏ khỏi website: jar "nhẹ", bản Windows .zip, thẻ "PC Java (.jar)", thẻ Unity "PC Windows".
 * Mã cũ trong CMS đã lưu được đổi khi đọc (MA_CU, chuanHoaBanTai).
 */
export type MaNenTang = 'jar' | 'windows' | 'linux' | 'android' | 'ios'

/**
 * Kiểu link:
 *   tep          : tải file trực tiếp (.apk / .exe / .jar / .AppImage / .tar.gz) — url là https://… hoặc /tai/<tệp>
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
  /**
   * Số bản (BAN của tools/phat-hanh/phien_ban.conf, vd 907) của tệp đang phát — máy chủ game coi đây là BẢN MỚI NHẤT
   * của nền tảng: client gửi số bản nhỏ hơn thấy "Đã có bản mới" (com.htbb.capnhat.CapNhatClient). 0 = chưa ghi.
   */
  ban: number
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
  /** Đuôi tệp được nhận khi kiểu link là 'tep' (chữ thường, không dấu chấm đầu). Rỗng = không kiểm. */
  duoi: string[]
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
  { ma: 'jar', ten: 'Java (J2ME)', moTa: 'Một file .jar cho điện thoại Java và giả lập J2ME (J2ME Loader trên Android)', icon: 'i-lucide-cpu', kieu: ['tep'], duoi: ['jar'], heDieuHanh: 'Java MIDP 2.0, hoặc J2ME Loader trên Android', chiTiet: ['File .jar', 'Điện thoại Java · giả lập J2ME', 'Phím số'], buoc: ['Dành cho điện thoại hỗ trợ Java (J2ME), hoặc điện thoại Android cài giả lập J2ME Loader (miễn phí).', 'Tải file .jar của game.', 'Điện thoại Java: mở file .jar để cài. J2ME Loader: bấm dấu + rồi chọn file .jar vừa tải.', 'Mở game và đăng nhập bằng cùng tài khoản như các bản khác.'], hien: true, trangThai: 'san-sang' },
  { ma: 'windows', ten: 'Windows', moTa: 'Bộ cài .exe có sẵn Java đi kèm, không phải cài thêm gì', icon: 'i-lucide-monitor', kieu: ['tep'], duoi: ['exe'], heDieuHanh: 'Windows 10 / 11 (64-bit)', chiTiet: ['Bộ cài .exe', 'Không cần cài Java', 'Bàn phím + chuột'], buoc: ['Tải bộ cài Windows (file .exe).', 'Chạy bộ cài và bấm Tiếp cho tới khi xong. Nếu Windows SmartScreen cảnh báo: "Thông tin thêm" → "Vẫn chạy".', 'Mở game bằng lối tắt Huyền Thoại Bảo Bối trên màn hình nền hoặc menu Start.', 'Đăng nhập bằng cùng tài khoản như các bản khác.'], hien: true, trangThai: 'san-sang' },
  { ma: 'linux', ten: 'Linux', moTa: 'Bản Linux 64-bit một tệp .AppImage, có sẵn Java đi kèm', icon: 'i-lucide-terminal', kieu: ['tep'], duoi: ['appimage'], heDieuHanh: 'Linux x86-64 (Ubuntu 20.04 trở lên hoặc tương đương)', chiTiet: ['Một file .AppImage', 'Không cần cài Java', 'Bàn phím + chuột'], buoc: ['Tải tệp .AppImage của game.', 'Cho phép chạy: chuột phải → Thuộc tính → Cho phép chạy như chương trình, hoặc gõ chmod +x HTBB-*.AppImage.', 'Bấm đúp tệp để mở game (không cần cài đặt).', 'Đăng nhập bằng cùng tài khoản như các bản khác.'], hien: true, trangThai: 'san-sang' },
  { ma: 'android', ten: 'Android', moTa: 'Bản cài đặt APK — đồ hoạ đầy đủ, điều khiển cảm ứng', icon: 'i-lucide-smartphone', kieu: ['tep'], duoi: ['apk'], heDieuHanh: 'Android 8.0 trở lên', chiTiet: ['File .apk', 'Cài ngoài CH Play', 'Cảm ứng + bàn phím'], buoc: ['Mở trang Tải game trên điện thoại, bấm tải bản Android.', 'Khi được hỏi, cho phép "Cài ứng dụng không rõ nguồn" cho trình duyệt.', 'Mở file .apk vừa tải và bấm Cài đặt.', 'Mở game, chọn máy chủ và đăng nhập.'], hien: true, trangThai: 'san-sang' },
  { ma: 'ios', ten: 'iOS', moTa: 'iPhone / iPad', icon: 'i-lucide-tablet-smartphone', kieu: ['testflight', 'app-store', 'ipa-manifest'], duoi: [], heDieuHanh: 'iOS 15 trở lên', chiTiet: ['TestFlight / App Store'], buoc: [], hien: true, trangThai: 'sap-ra-mat' },
]

/**
 * Mã nền tảng cũ đã lưu trong CMS (web_cai_dat 'taiGame') -> mã mới; null = bỏ thẻ.
 *   pc-java (bộ cài Windows có Java) -> windows
 *   pc      (Unity PC Windows)        -> bỏ
 *   pc-jar  (PC Java .jar)             -> bỏ
 *   pc-linux (mã cũ hơn nữa)           -> linux;  macos -> bỏ
 */
export const MA_CU: Readonly<Record<string, MaNenTang | null>> = {
  'pc-java': 'windows',
  'pc': null,
  'pc-jar': null,
  'pc-linux': 'linux',
  'macos': null,
}

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
    ma: n.ma, ten: n.ten, moTa: n.moTa, url: '', kieu: n.kieu[0]!, phienBan: '', ban: 0, dungLuong: '', capNhat: '', nhatKy: '',
    sha256: '', heDieuHanh: n.heDieuHanh, trangThai: n.trangThai, hien: n.hien,
  }
}

const chuoi = (v: unknown, max: number, macDinh = '') => (typeof v === 'string' ? v.trim().slice(0, max) : macDinh)

/** Số bản hợp lệ: số nguyên 0..99 999 999 (chuỗi số cũng nhận); sai -> 0. */
export function soBanHopLe(v: unknown): number {
  const n = typeof v === 'string' && /^\s*\d+\s*$/.test(v) ? Number(v) : v
  return typeof n === 'number' && Number.isInteger(n) && n >= 0 && n < 100_000_000 ? n : 0
}

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
    if (!u) return `${n.ten}: link tải phải là https://… hoặc đường dẫn trong website (vd. /tai/HTBB-PC-Setup-0.9.6.exe).`
    const host = HOST_KIEU[b.kieu]
    if (host && !host.includes(new URL(u, 'https://x.invalid').hostname)) return `${n.ten}: link ${TEN_KIEU_LINK[b.kieu]} phải thuộc ${host.join(' / ')}.`
    if (b.kieu === 'ipa-manifest' && !u.startsWith('https://')) return `${n.ten}: manifest .ipa phải là URL https:// đầy đủ (iOS bắt buộc).`
    if (b.kieu === 'tep' && n.duoi.length && !dungDuoi(n, u)) return `${n.ten}: tệp phải có đuôi ${n.duoi.map(d => (d === 'appimage' ? '.AppImage' : `.${d}`)).join(' / ')}.`
  }
  if (b.sha256 && !/^[0-9a-f]{64}$/.test(b.sha256)) return `${n.ten}: SHA-256 phải gồm đúng 64 ký tự 0-9 a-f.`
  const ban = b.ban as unknown
  if (ban !== '' && ban != null && ban !== 0 && soBanHopLe(ban) === 0) return `${n.ten}: số bản phải là số nguyên ≥ 0 (vd 907).`
  return null
}

/** Đường dẫn (bỏ ?query / #hash) có đuôi tệp thuộc nền tảng không. */
export function dungDuoi(n: Pick<NenTang, 'duoi'>, url: string): boolean {
  const duongDan = url.split(/[?#]/)[0]!.toLowerCase()
  return n.duoi.some(d => duongDan.endsWith(`.${d}`))
}

/** Link tệp trong kho /tai/ ghi trong ô "Có gì mới" (tính năng "tệp phụ" cũ, đã bỏ). */
const RE_TEP_PHU = /\/tai\/[\w.\-]+/

/** Bỏ các dòng nhắc tệp phụ (/tai/<tệp>) khỏi "Có gì mới": web chỉ phát đúng một tệp mỗi nền tảng. */
export function boDongTepPhu(nhatKy: string): string {
  return nhatKy.replace(/\\n/g, '\n').split('\n').filter(d => !RE_TEP_PHU.test(d)).join('\n').trim()
}

/**
 * Nắn danh sách bản tải từ CMS: mỗi nền tảng đúng một lần (theo thứ tự đã lưu), thiếu thì thêm mặc định.
 * Dữ liệu kiểu cũ (có mã trong MA_CU): đổi mã, bỏ thẻ đã gỡ, xếp lại theo DS_NEN_TANG; bản Linux chưa có
 * thì lấy link .tar.gz / .AppImage Linux từng ghi làm "tệp phụ" (vd. /tai/HTBB-PC-Linux-0.9.4.tar.gz).
 */
export function chuanHoaBanTai(raw: unknown): BanTai[] {
  const ds = Array.isArray(raw) ? raw : []
  const ra: BanTai[] = []
  let kieuCu = false
  let linuxTuTepPhu: { url: string, phienBan: string, capNhat: string } | null = null
  for (const x of ds) {
    if (!x || typeof x !== 'object') continue
    const r = x as Record<string, unknown>
    const maGoc = String(r.ma ?? '')
    if (maGoc in MA_CU) {
      kieuCu = true
      const tp = typeof r.nhatKy === 'string' ? r.nhatKy.match(/\/tai\/[\w.\-]*linux[\w.\-]*\.(?:tar\.gz|tgz|appimage)\b/i) : null
      if (tp && !linuxTuTepPhu) linuxTuTepPhu = { url: tp[0], phienBan: chuoi(r.phienBan, 40), capNhat: chuoi(r.capNhat, 40) }
    }
    const ma = maGoc in MA_CU ? MA_CU[maGoc] : maGoc
    const n = ma ? nenTang(ma) : undefined
    if (!n || ra.some(b => b.ma === n.ma)) continue
    const md = banTaiMacDinh(n)
    const kieu = n.kieu.includes(r.kieu as KieuLinkTai) ? r.kieu as KieuLinkTai : md.kieu
    // Dữ liệu cũ (trước khi có trangThai): có link = sẵn sàng.
    let url = chuoi(r.url, 500)
    const doiMa = maGoc !== n.ma
    // Mã cũ đổi sang mã mới mà tệp sai loại (vd. .zip cho Windows): bỏ link, thẻ chờ admin tải tệp đúng.
    if (doiMa && kieu === 'tep' && url && url !== '#' && n.duoi.length && !dungDuoi(n, url)) url = ''
    const trangThai: TrangThaiBanTai = r.trangThai === 'san-sang' || r.trangThai === 'sap-ra-mat'
      ? r.trangThai
      : (url && url !== '#' ? 'san-sang' : md.trangThai)
    ra.push({
      ma: n.ma,
      ten: (doiMa ? '' : chuoi(r.ten, 60)) || md.ten,
      moTa: doiMa ? md.moTa : chuoi(r.moTa, 200, md.moTa),
      url: url === '#' ? '' : url,
      kieu,
      phienBan: chuoi(r.phienBan, 40),
      ban: doiMa ? 0 : soBanHopLe(r.ban),
      dungLuong: chuoi(r.dungLuong, 40),
      capNhat: chuoi(r.capNhat, 40),
      nhatKy: boDongTepPhu(chuoi(r.nhatKy, 3000)),
      sha256: chuoi(r.sha256, 64).toLowerCase(),
      heDieuHanh: doiMa ? md.heDieuHanh : chuoi(r.heDieuHanh, 120, md.heDieuHanh),
      trangThai: doiMa && !url ? md.trangThai : trangThai,
      hien: typeof r.hien === 'boolean' ? r.hien : md.hien,
    })
  }
  for (const n of DS_NEN_TANG) {
    if (ra.some(b => b.ma === n.ma)) continue
    const md = banTaiMacDinh(n)
    if (n.ma === 'linux' && linuxTuTepPhu) ra.push({ ...md, url: linuxTuTepPhu.url, phienBan: linuxTuTepPhu.phienBan, capNhat: linuxTuTepPhu.capNhat, trangThai: 'san-sang' })
    else ra.push(md)
  }
  if (kieuCu) ra.sort((a, b) => MA_NEN_TANG.indexOf(a.ma) - MA_NEN_TANG.indexOf(b.ma))
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
  if (s.includes('windows')) return 'windows'
  if (s.includes('cros')) return null
  if (s.includes('linux') || s.includes('x11')) return 'linux'
  return null // macOS: chưa có bản riêng
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
