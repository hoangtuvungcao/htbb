/**
 * Đăng ký tài khoản — quy tắc nhập liệu và các câu SQL dùng chung.
 */
import { USERNAME_MIN, USERNAME_MAX, PASSWORD_MIN, PASSWORD_MAX, validUsername, matKhauGameHopLe, dangKyEmailHopLe } from '../../shared/utils/htbb'

/**
 * Số tài khoản một địa chỉ IP được tạo trong một giờ. Người thật hiếm khi cần
 * quá vài cái (bạn bè chung nhà, chung quán net), còn script thì tạo hàng nghìn.
 */
export const DANG_KY_TOI_DA = 5
export const DANG_KY_TRONG_GIAY = 3600

/**
 * Câu INSERT tạo tài khoản. Lưu mật khẩu bằng PASSWORD() để KHỚP với login server
 * của game. Trang Tạo hàng loạt của admin PHẢI dùng đúng 7 cột đầu y hệt câu này
 * (chỉ cột email được khác: ở đây là tham số ?, tạo hàng loạt ghi NULL) — hai nơi
 * tạo tài khoản không được lệch cột.
 */
export const DANG_KY_INSERT_SQL
  = 'INSERT INTO team_user (`username`, `password`, `phone`, `regdate`, `ban`, `provider`, `fromgame`, `email`)'
    + ' VALUES (?, PASSWORD(?), \'\', NOW(), 0, 0, 0, ?)'

export const DANG_KY_TRUNG_TEN_SQL = 'SELECT 1 FROM team_user WHERE username = ? LIMIT 1'

// Kiểm tra email (dangKyEmailHopLe) nằm ở shared/utils/htbb.ts để form đăng ký
// kiểm tra trước đúng như server.

export interface DangKyInput {
  username: string
  password: string
  confirm: string
  email: string
}

/**
 * Chuẩn hoá dữ liệu form: username trim + chữ thường (login server chỉ nhận chữ
 * thường), email trim. Trường không phải chuỗi coi như rỗng.
 */
export function dangKyChuanHoa(body: Record<string, unknown> | null | undefined): DangKyInput {
  const s = (v: unknown) => (typeof v === 'string' ? v : '')
  return {
    username: s(body?.username).trim().toLowerCase(),
    password: s(body?.password),
    confirm: s(body?.confirm),
    email: s(body?.email).trim(),
  }
}

/** Mọi lỗi nhập liệu, theo thứ tự các ô trên form. Rỗng = hợp lệ. */
export function dangKyKiemTra(d: DangKyInput): string[] {
  const loi: string[] = []
  if (!validUsername(d.username)) {
    loi.push(`Tên tài khoản phải từ ${USERNAME_MIN}-${USERNAME_MAX} ký tự, chỉ gồm chữ thường và số.`)
  }
  // Login server của game chỉ nhận chữ cái không dấu + số (CreateCharHandler.checkInfoLogin) và bỏ
  // qua "000000": đăng ký mật khẩu khác thì tài khoản tạo được nhưng không bao giờ vào được game.
  if (!matKhauGameHopLe(d.password)) {
    loi.push(`Mật khẩu phải từ ${PASSWORD_MIN}-${PASSWORD_MAX} ký tự, chỉ gồm chữ cái không dấu và số, và không được là 000000.`)
  }
  if (d.password !== d.confirm) {
    loi.push('Mật khẩu nhập lại không khớp.')
  }
  // Email bắt buộc: là đường duy nhất để lấy lại mật khẩu (trang Quên mật khẩu).
  if (d.email === '') {
    loi.push('Vui lòng nhập email (dùng để lấy lại mật khẩu).')
  } else if (!dangKyEmailHopLe(d.email)) {
    loi.push('Email không hợp lệ.')
  }
  return loi
}
