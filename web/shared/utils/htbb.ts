/**
 * Hằng số và hàm thuần dùng chung cho cả client (app/) lẫn server (server/).
 * Nuxt 4 tự import mọi thứ trong shared/utils ở cả hai phía.
 */

import { QUY_TAC_DANG_KY, dangKyEmailDungDang, dangKyMatKhauHopLe, dangKyTenDungDang, dangKyThongBao } from './quyTacDangKy'

/**
 * Quy tắc tài khoản — lấy từ shared/data/quy-tac-dang-ky.json (nguồn duy nhất cho web + đăng ký
 * trong game, xem shared/utils/quyTacDangKy.ts). KHÔNG đặt số cứng ở đây.
 */
export const USERNAME_MIN = QUY_TAC_DANG_KY.tenTaiKhoan.min
export const USERNAME_MAX = QUY_TAC_DANG_KY.tenTaiKhoan.max
export const PASSWORD_MIN = QUY_TAC_DANG_KY.matKhau.min
export const PASSWORD_MAX = QUY_TAC_DANG_KY.matKhau.max

export function validUsername(u: string): boolean {
  return dangKyTenDungDang(u)
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
  return dangKyMatKhauHopLe(p)
}

/** Lỗi khi mật khẩu mới không đúng matKhauGameHopLe (dùng cả ở form lẫn server). */
export const LOI_MAT_KHAU_GAME = dangKyThongBao('matKhauSai').replace(/^Mật khẩu/, 'Mật khẩu mới')

/**
 * Kiểm tra email (một @, tên miền ≥ 2 nhãn, ≤ độ dài cột team_user.email).
 * Dùng chung cho form (kiểm tra trước khi gửi) và server (kiểm tra thật).
 */
export function dangKyEmailHopLe(email: string): boolean {
  return dangKyEmailDungDang(email)
}

/**
 * Dòng Pokémon khởi đầu theo cột tob_char.class (nhánh pokemon/chuyen-doi, quyết định Q1:
 * class 0..4 giữ nguyên, chỉ đổi ý nghĩa). Tên cũ (Kiếm khách, Chiến binh, Pháp sư,
 * Đấu sĩ, Cung thủ) ở CLASS_NAMES_CU. Loài hiện tại theo cấp: speciesName() ở pokemon.ts.
 */
export const CLASS_NAMES = ['Charmander', 'Bulbasaur', 'Eevee', 'Squirtle', 'Pichu'] as const

/** Tên nghề cũ của game gốc (khớp Res.nameClazz của client, MapCheTao.java:930). */
export const CLASS_NAMES_CU = ['Dòng Charmander', 'Dòng Bulbasaur', 'Dòng Eevee', 'Dòng Squirtle', 'Dòng Pichu'] as const

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

/** Định dạng tiền VNĐ: 49.000 đ */
export function vnd(n: number | string | null | undefined): string {
  return `${so(n)} đ`
}

/** 3 mốc nạp tân thủ: giá tiền (VNĐ) + phần thưởng hiển thị trên web. */
export interface MocNap {
  gia: number
  items: string[]
}

export const MOC_NAP: Record<1 | 2 | 3, MocNap> = {
  1: {
    gia: 49000,
    items: ['100.000 lượng mở', '200.000 lượng khoá'],
  },
  2: {
    gia: 99000,
    items: ['1 trứng Cleffa', '1 trứng Hoothoot', '2 Kẹo hiếm', '100.000 lượng mở', '200.000 lượng khoá'],
  },
  3: {
    gia: 299000,
    items: [
      '1 trứng Rufflet', '1 trứng Dratini', '2 Kẹo hiếm',
      'Moltres hoặc Articuno (chọn khi nhận tại NPC Tổng Quản)',
      'Đồ cầm tay thời trang max chỉ số (7 ngày)',
      '100.000 lượng mở', '200.000 lượng khoá',
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
