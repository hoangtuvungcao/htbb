/**
 * QUY TẮC ĐĂNG KÝ TÀI KHOẢN — lõi thuần, KHÔNG import gì (chạy được bằng `node --test`).
 *
 * Dữ liệu (độ dài, tên cấm, từ cấm, hạn mức, câu báo lỗi) nằm ở shared/data/quy-tac-dang-ky.json
 * — nguồn DUY NHẤT cho cả web lẫn đăng ký trong game. Bản Java (HTBB/src/data/QuyTacDangKy.java)
 * là bản dịch 1-1 của tệp này, chạy chung bộ `thuNghiem` trong JSON; sửa logic ở đây thì PHẢI sửa
 * bên Java (tests/dang-ky.test.ts + test/KiemDangKyTrongGame.java sẽ báo nếu lệch).
 *
 * Chỉ dùng cú pháp TS "xoá được kiểu" (không enum, không namespace) để Node 22 chạy thẳng.
 */

export interface QuyTacDangKy {
  phienBan: number
  tenTaiKhoan: { min: number, max: number }
  matKhau: { min: number, max: number, cam: string[] }
  email: { toiDa: number, batBuocWeb: boolean, batBuocGame: boolean }
  tenQuanTri: { bang: string[], batDau: string[], chua: string[] }
  tuCam: string[]
  thayTheSo: Record<string, string>
  hanMuc: { moiGio: number, moiNgay: number }
  thongBao: Record<string, string>
}

/** Nguồn đăng ký: web bắt buộc email (đường lấy lại mật khẩu), game không bắt buộc (gõ trên phím điện thoại). */
export type NguonDangKy = 'web' | 'game'

export interface DuLieuDangKy {
  username: string
  password: string
  confirm: string
  email: string
}

export interface LoiDangKy {
  /** Mã lỗi = khoá trong thongBao (tenSai, tenQuanTri, tenTuCam, matKhauSai, matKhauKhongKhop, emailThieu, emailSai). */
  ma: string
  loi: string
}

/** Câu báo lỗi theo mã, thay {min}/{max}. */
export function thongBaoDangKy(qt: QuyTacDangKy, ma: string): string {
  const mau = qt.thongBao[ma] ?? ma
  const gioiHan = ma.startsWith('ten') ? qt.tenTaiKhoan : ma.startsWith('matKhau') ? qt.matKhau : null
  if (!gioiHan) return mau
  return mau.split('{min}').join(String(gioiHan.min)).split('{max}').join(String(gioiHan.max))
}

function chiChuThuongVaSo(s: string): boolean {
  for (let i = 0; i < s.length; i++) {
    const c = s.charCodeAt(i)
    if (!((c >= 97 && c <= 122) || (c >= 48 && c <= 57))) return false
  }
  return true
}

function chiChuKhongDauVaSo(s: string): boolean {
  for (let i = 0; i < s.length; i++) {
    const c = s.charCodeAt(i)
    if (!((c >= 97 && c <= 122) || (c >= 65 && c <= 90) || (c >= 48 && c <= 57))) return false
  }
  return true
}

/** Tên tài khoản đúng dạng: min..max ký tự, chỉ a-z 0-9. */
export function tenTaiKhoanDungDang(qt: QuyTacDangKy, u: string): boolean {
  return u.length >= qt.tenTaiKhoan.min && u.length <= qt.tenTaiKhoan.max && chiChuThuongVaSo(u)
}

/** Số UTF-8 byte (cột mật khẩu và login server giới hạn theo byte). */
function soByteUtf8(s: string): number {
  let n = 0
  for (let i = 0; i < s.length; i++) {
    const c = s.charCodeAt(i)
    if (c < 0x80) n += 1
    else if (c < 0x800) n += 2
    else if (c >= 0xD800 && c <= 0xDBFF) { n += 4; i++ }
    else n += 3
  }
  return n
}

/**
 * Mật khẩu login server của game chấp nhận: min..max byte, chỉ chữ cái Latinh không dấu + số
 * (CreateCharHandler.checkInfoLogin), và không nằm trong danh sách cấm (000000 = passAdmin, so không
 * phân biệt hoa thường).
 */
export function matKhauHopLe(qt: QuyTacDangKy, p: string): boolean {
  const n = soByteUtf8(p)
  if (n < qt.matKhau.min || n > qt.matKhau.max || !chiChuKhongDauVaSo(p)) return false
  const thuong = p.toLowerCase()
  return !qt.matKhau.cam.some(c => c.toLowerCase() === thuong)
}

/** Các dạng của tên để dò: nguyên gốc, đổi số giống chữ (d1t -> dit), bỏ hết số. */
function cacDang(qt: QuyTacDangKy, u: string): string[] {
  let doi = ''
  let boSo = ''
  for (const ch of u) {
    doi += qt.thayTheSo[ch] ?? ch
    if (ch < '0' || ch > '9') boSo += ch
  }
  return [u, doi, boSo]
}

/** 'quanTri' | 'tuCam' | null. Tên đã đúng dạng (chữ thường + số). */
export function tenBiCam(qt: QuyTacDangKy, u: string): 'tenQuanTri' | 'tenTuCam' | null {
  const ds = cacDang(qt, u)
  const q = qt.tenQuanTri
  for (const t of ds) {
    if (q.bang.includes(t) || q.batDau.some(b => t.startsWith(b)) || q.chua.some(c => t.includes(c))) return 'tenQuanTri'
  }
  for (const t of ds) {
    if (qt.tuCam.some(w => t.includes(w))) return 'tenTuCam'
  }
  return null
}

/**
 * Email ở mức hợp lý: một @, phần trước 1..64 ký tự cho phép và không có dấu chấm ở đầu/cuối/liền
 * nhau, tên miền ≥ 2 nhãn, nhãn chỉ chữ/số/gạch ngang (không ở đầu/cuối), tổng ≤ email.toiDa (cột
 * team_user.email là varchar(100)).
 */
export function emailHopLe(qt: QuyTacDangKy, email: string): boolean {
  if (email.length > qt.email.toiDa) return false
  const m = email.match(/^([A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]{1,64})@([A-Za-z0-9.-]+)$/)
  if (!m) return false
  const local = m[1] as string
  const domain = m[2] as string
  if (local.startsWith('.') || local.endsWith('.') || local.includes('..')) return false
  const nhan = domain.split('.')
  if (nhan.length < 2) return false
  return nhan.every(n => /^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$/.test(n))
}

/** Mọi lỗi nhập liệu, theo thứ tự các ô trên form (tên, mật khẩu, nhập lại, email). Rỗng = hợp lệ. */
export function kiemTraDangKy(qt: QuyTacDangKy, d: DuLieuDangKy, nguon: NguonDangKy): LoiDangKy[] {
  const ma: string[] = []
  if (!tenTaiKhoanDungDang(qt, d.username)) {
    ma.push('tenSai')
  } else {
    const cam = tenBiCam(qt, d.username)
    if (cam) ma.push(cam)
  }
  if (!matKhauHopLe(qt, d.password)) ma.push('matKhauSai')
  if (d.password !== d.confirm) ma.push('matKhauKhongKhop')
  const batBuoc = nguon === 'web' ? qt.email.batBuocWeb : qt.email.batBuocGame
  if (d.email === '') {
    if (batBuoc) ma.push('emailThieu')
  } else if (!emailHopLe(qt, d.email)) {
    ma.push('emailSai')
  }
  return ma.map(m => ({ ma: m, loi: thongBaoDangKy(qt, m) }))
}

/**
 * HẠN MỨC (thuần, không đụng DB): đã tạo `demGio` tài khoản trong 1 giờ, `demNgay` trong 24 giờ
 * qua (tính GỘP cả web lẫn game cùng IP), cộng `dangChay` lượt đang xử lý song song.
 * Trả mã lỗi 'quaGio' | 'quaNgay' hoặc null = còn được tạo.
 */
export function vuotHanMuc(qt: QuyTacDangKy, demGio: number, demNgay: number, dangChay: number): 'quaGio' | 'quaNgay' | null {
  if (demNgay + dangChay >= qt.hanMuc.moiNgay) return 'quaNgay'
  if (demGio + dangChay >= qt.hanMuc.moiGio) return 'quaGio'
  return null
}
