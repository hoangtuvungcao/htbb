/**
 * Hằng số và hàm thuần dùng chung cho cả client (app/) lẫn server (server/).
 * Nuxt 4 tự import mọi thứ trong shared/utils ở cả hai phía.
 */

/** Quy tắc tài khoản (khớp với server: 4-20 ký tự, chỉ chữ thường và số). */
export const USERNAME_MIN = 4
export const USERNAME_MAX = 20
export const PASSWORD_MIN = 4
export const PASSWORD_MAX = 20

export function validUsername(u: string): boolean {
  return new RegExp(`^[a-z0-9]{${USERNAME_MIN},${USERNAME_MAX}}$`).test(u)
}

/**
 * Độ dài tính theo BYTE UTF-8 (không phải số ký tự): cột mật khẩu và login server
 * giới hạn theo byte, mà chữ có dấu tốn 2-3 byte.
 */
export function validPassword(p: string): boolean {
  const len = new TextEncoder().encode(p).length
  return len >= PASSWORD_MIN && len <= PASSWORD_MAX
}

/**
 * Mật khẩu mà login server của game chấp nhận: chỉ chữ cái Latinh không dấu và số
 * (CreateCharHandler.checkInfoLogin, gọi trong LoginHandler.process), và không được
 * là "000000" (LoginHandler.passAdmin, so không phân biệt hoa thường — gặp là bỏ
 * qua gói đăng nhập). Dùng cho đổi mật khẩu / mật khẩu sinh ra khi quên mật khẩu.
 * Dùng cả cho form đăng ký.
 */
export function matKhauGameHopLe(p: string): boolean {
  return validPassword(p) && /^[A-Za-z0-9]+$/.test(p) && p.toLowerCase() !== '000000'
}

/** Lỗi khi mật khẩu mới không đúng matKhauGameHopLe (dùng cả ở form lẫn server). */
export const LOI_MAT_KHAU_GAME
  = `Mật khẩu mới phải từ ${PASSWORD_MIN}-${PASSWORD_MAX} ký tự, chỉ gồm chữ cái không dấu và số, và không được là 000000.`

/**
 * Kiểm tra email ở mức hợp lý: một @, phần trước không rỗng và không có khoảng
 * trắng, tên miền có ít nhất một dấu chấm, nhãn chỉ gồm chữ/số/gạch ngang.
 * Dùng chung cho form (kiểm tra trước khi gửi) và server (kiểm tra thật).
 */
export function dangKyEmailHopLe(email: string): boolean {
  if (email.length > 254) return false
  const m = email.match(/^([A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]{1,64})@([A-Za-z0-9.-]+)$/)
  if (!m) return false
  const [, local, domain] = m as unknown as [string, string, string]
  if (local.startsWith('.') || local.endsWith('.') || local.includes('..')) return false
  const nhan = domain.split('.')
  if (nhan.length < 2) return false
  return nhan.every(n => /^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$/.test(n))
}

/**
 * Dòng Pokémon khởi đầu theo cột tob_char.class (nhánh pokemon/chuyen-doi, quyết định Q1:
 * class 0..4 giữ nguyên, chỉ đổi ý nghĩa). Tên cũ (Kiếm khách, Chiến binh, Pháp sư,
 * Đấu sĩ, Cung thủ) ở CLASS_NAMES_CU. Loài hiện tại theo cấp: speciesName() ở pokemon.ts.
 */
export const CLASS_NAMES = ['Charmander', 'Bulbasaur', 'Eevee', 'Squirtle', 'Pichu'] as const

/** Tên nghề cũ của KPAH (khớp Res.nameClazz của client, MapCheTao.java:930). */
export const CLASS_NAMES_CU = ['Kiếm khách', 'Chiến binh', 'Pháp sư', 'Đấu sĩ', 'Cung thủ'] as const

export function tenNghe(c: number | string | null | undefined): string {
  const i = Number(c)
  return CLASS_NAMES[i] ?? `Dòng ${Number.isFinite(i) ? Math.trunc(i) : '?'}`
}

/** Số nguyên làm tròn, dấu chấm ngăn cách hàng nghìn: 1234567 -> "1.234.567". */
export function so(n: number | string | null | undefined): string {
  const x = Math.round(Number(n ?? 0))
  if (!Number.isFinite(x)) return '0'
  const am = x < 0
  const s = Math.abs(x).toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.')
  return am ? `-${s}` : s
}

/** Định dạng tiền VNĐ: 49.000đ */
export function vnd(n: number | string | null | undefined): string {
  return `${so(n)}đ`
}

/** 3 mốc nạp tân thủ: giá tiền (VNĐ) + phần thưởng hiển thị trên web. */
export interface MocNap {
  gia: number
  items: string[]
}

export const MOC_NAP: Record<1 | 2 | 3, MocNap> = {
  1: {
    gia: 49000,
    items: ['100.000 lượng mở', '200.000 lượng khóa'],
  },
  2: {
    gia: 99000,
    items: ['1 Trứng Yêu Tinh', '1 Trứng Cú', '2 Huyết Linh Thảo', '100.000 lượng mở', '200.000 lượng khóa'],
  },
  3: {
    gia: 299000,
    items: [
      '1 Trứng Đại Bàng', '1 Trứng Rồng', '2 Huyết Linh Thảo',
      'Phượng Hoàng HOẶC Phượng Hoàng Băng (chọn khi nhận tại NPC Tổng Quản)',
      'Vũ khí thời trang max chỉ số (7 ngày)',
      '100.000 lượng mở', '200.000 lượng khóa',
    ],
  },
}

export type TrangThaiDon = 'cho_thanh_toan' | 'da_thanh_toan' | 'da_nhan' | 'huy' | string

export const TEN_TRANG_THAI_DON: Record<string, string> = {
  cho_thanh_toan: 'Chờ thanh toán',
  da_thanh_toan: 'Đã thanh toán',
  da_nhan: 'Đã nhận quà',
  huy: 'Đã huỷ',
}
