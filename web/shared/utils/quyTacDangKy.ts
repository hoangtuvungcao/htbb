/**
 * Quy tắc đăng ký dùng chung (web + game). Dữ liệu: shared/data/quy-tac-dang-ky.json,
 * logic thuần: shared/dang-ky/kiem-tra.ts (bản Java dịch 1-1: HTBB/src/data/QuyTacDangKy.java).
 * Nuxt tự import các hàm ở đây cho cả app/ lẫn server/.
 */
import raw from '../data/quy-tac-dang-ky.json'
import {
  emailHopLe, kiemTraDangKy, matKhauHopLe, tenTaiKhoanDungDang, thongBaoDangKy, vuotHanMuc,
  type DuLieuDangKy, type LoiDangKy, type NguonDangKy, type QuyTacDangKy,
} from '../dang-ky/kiem-tra'

export type { DuLieuDangKy, LoiDangKy, NguonDangKy, QuyTacDangKy }

export const QUY_TAC_DANG_KY = raw as unknown as QuyTacDangKy

export function dangKyLoi(d: DuLieuDangKy, nguon: NguonDangKy = 'web'): LoiDangKy[] {
  return kiemTraDangKy(QUY_TAC_DANG_KY, d, nguon)
}

export function dangKyThongBao(ma: string): string {
  return thongBaoDangKy(QUY_TAC_DANG_KY, ma)
}

export function dangKyVuotHanMuc(demGio: number, demNgay: number, dangChay: number): 'quaGio' | 'quaNgay' | null {
  return vuotHanMuc(QUY_TAC_DANG_KY, demGio, demNgay, dangChay)
}

export function dangKyTenDungDang(u: string): boolean {
  return tenTaiKhoanDungDang(QUY_TAC_DANG_KY, u)
}

export function dangKyMatKhauHopLe(p: string): boolean {
  return matKhauHopLe(QUY_TAC_DANG_KY, p)
}

export function dangKyEmailDungDang(e: string): boolean {
  return emailHopLe(QUY_TAC_DANG_KY, e)
}
