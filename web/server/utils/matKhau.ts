/**
 * Mật khẩu người chơi: kiểm tra mật khẩu hiện tại, sinh mật khẩu mới, hạn mức
 * quên mật khẩu theo tài khoản. Dùng cho các API trong server/api/auth/.
 */
import { createHmac, randomInt, timingSafeEqual } from 'node:crypto'

/** Gõ SAI mật khẩu hiện tại (đổi mật khẩu / đổi email) tối đa bao nhiêu lần từ một IP. */
export const DOI_MK_SAI_TOI_DA = 10
export const DOI_MK_SAI_TRONG_GIAY = 900

/** Gửi form Quên mật khẩu tối đa bao nhiêu lần từ một IP (đếm MỌI lần gửi). */
export const QUEN_MK_TOI_DA = 5
export const QUEN_MK_TRONG_GIAY = 3600

/** Một tài khoản được cấp lại mật khẩu tối đa bao nhiêu lần trong 24 giờ. */
export const QUEN_MK_MOI_TK_TOI_DA = 3
const QUEN_MK_MOI_TK_TRONG_GIAY = 86400

/**
 * Mật khẩu ngẫu nhiên 8 ký tự [a-z0-9] (randomInt của crypto, không dùng Math.random).
 * Chỉ chữ thường + số: login server chỉ nhận chữ + số, và người chơi gõ lại trên điện
 * thoại không phải phân biệt hoa thường.
 */
export function taoMatKhauNgauNhien(doDai = 8): string {
  const kyTu = 'abcdefghijklmnopqrstuvwxyz0123456789'
  let s = ''
  for (let i = 0; i < doDai; i++) s += kyTu[randomInt(kyTu.length)]
  return s
}

/**
 * Mật khẩu hiện tại có đúng không — so bằng PASSWORD() ngay trong SQL như login
 * server, không đọc cột mật khẩu ra. Đúng thì trả email (có thể null) của tài khoản.
 * Lỗi database thì NÉM để API trả 503.
 */
export async function kiemTraMatKhauHienTai(userId: number, matKhau: string): Promise<{ email: string | null } | null> {
  const row = await q1<{ email: string | null }>(
    dbAccount(),
    'SELECT email FROM team_user WHERE id = ? AND `password` = PASSWORD(?) LIMIT 1',
    [Math.trunc(userId), matKhau],
  )
  if (!row) return null
  const email = typeof row.email === 'string' && row.email.trim() !== '' ? row.email.trim() : null
  return { email }
}

/**
 * Hạn mức cấp lại mật khẩu THEO TÀI KHOẢN (chặn người khác cứ nhập tên + email của
 * bạn để đổi mật khẩu của bạn liên tục — mỗi lần đổi là bạn phải vào mail lấy mật
 * khẩu mới).
 *
 * Đếm trong BỘ NHỚ của tiến trình Node, khoá theo username: bảng web_chan_spam chỉ
 * đếm theo IP. Mất khi khởi động lại dịch vụ — chấp nhận được, vì đây chỉ là lớp
 * chặn thêm; lớp chính vẫn là hạn mức theo IP ở trên.
 */
const quenMkTheoTk = new Map<string, number[]>()

export function quenMkTaiKhoanBiChan(username: string): boolean {
  const moc = nowSec() - QUEN_MK_MOI_TK_TRONG_GIAY
  const ds = (quenMkTheoTk.get(username) ?? []).filter(t => t > moc)
  return ds.length >= QUEN_MK_MOI_TK_TOI_DA
}

export function quenMkGhiTaiKhoan(username: string): void {
  const moc = nowSec() - QUEN_MK_MOI_TK_TRONG_GIAY
  const ds = (quenMkTheoTk.get(username) ?? []).filter(t => t > moc)
  ds.push(nowSec())
  quenMkTheoTk.set(username, ds)
  // Dọn tài khoản đã hết hạn giữ để bộ nhớ không phình mãi.
  if (quenMkTheoTk.size > 5000) {
    for (const [k, v] of quenMkTheoTk) {
      if (!v.some(t => t > moc)) quenMkTheoTk.delete(k)
    }
  }
}

/**
 * MÃ XÁC NHẬN QUÊN MẬT KHẨU (không lưu gì phía máy chủ): HMAC(khoá bí mật, id | hash mật khẩu hiện tại | khung
 * 30 phút) -> 8 ký tự. Gắn với hash mật khẩu nên đổi mật khẩu xong là mã cũ hết hiệu lực (dùng một lần); nhận
 * khung hiện tại + khung trước -> sống 30-60 phút. Bỏ chữ dễ nhầm (0/O, 1/I/L).
 */
const KY_TU_MA = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789'
const KHUNG_MA_GIAY = 1800

function maTheoKhung(userId: number, hash: string, khung: number): string {
  const d = createHmac('sha256', useKpahConfig().secret).update(`qmk|${Math.trunc(userId)}|${hash}|${khung}`).digest()
  let s = ''
  for (let i = 0; i < 8; i++) s += KY_TU_MA[d[i]! % KY_TU_MA.length]
  return s
}

export function taoMaQuenMk(userId: number, hash: string): string {
  return maTheoKhung(userId, hash, Math.floor(nowSec() / KHUNG_MA_GIAY))
}

export function maQuenMkDung(userId: number, hash: string, ma: string): boolean {
  const goi = Buffer.from(ma.trim().toUpperCase())
  const k = Math.floor(nowSec() / KHUNG_MA_GIAY)
  for (const khung of [k, k - 1]) {
    const dung = Buffer.from(maTheoKhung(userId, hash, khung))
    if (dung.length === goi.length && timingSafeEqual(dung, goi)) return true
  }
  return false
}

/**
 * Nhập SAI mã theo tài khoản: 20 lần / giờ (cùng với hạn mức theo IP). Không để thấp: ai cũng gõ sai mã cho tên
 * người khác được -> hạn thấp là chặn được chủ tài khoản đặt lại mật khẩu. Mã 8 ký tự / 31 chữ (~8,5e11) nên
 * 20 lần/giờ vẫn không dò nổi.
 */
const saiMaTheoTk = new Map<string, number[]>()
export function saiMaBiChan(username: string): boolean {
  const moc = nowSec() - 3600
  return (saiMaTheoTk.get(username) ?? []).filter(t => t > moc).length >= 20
}
export function ghiSaiMa(username: string): void {
  const moc = nowSec() - 3600
  const ds = (saiMaTheoTk.get(username) ?? []).filter(t => t > moc)
  ds.push(nowSec())
  saiMaTheoTk.set(username, ds)
  if (saiMaTheoTk.size > 5000) {
    for (const [k, v] of saiMaTheoTk) if (!v.some(t => t > moc)) saiMaTheoTk.delete(k)
  }
}
