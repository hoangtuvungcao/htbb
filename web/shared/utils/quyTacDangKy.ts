/**
 * Quy tắc đăng ký dùng chung (web + game). Dữ liệu: shared/data/quy-tac-dang-ky.json,
 * logic thuần: shared/dang-ky/kiem-tra.ts (bản Java dịch 1-1: HTBB/src/data/QuyTacDangKy.java).
 * Nuxt tự import các hàm ở đây cho cả app/ lẫn server/.
 */
import raw from '../data/quy-tac-dang-ky.json'
import {
  emailHopLe, hanMucHieuLuc, kiemTraDangKy, matKhauHopLe, tenTaiKhoanDungDang, thongBaoDangKy, vuotHanMuc,
  type DuLieuDangKy, type HanMucDangKy, type LoiDangKy, type NguonDangKy, type QuyTacDangKy,
} from '../dang-ky/kiem-tra'

export type { DuLieuDangKy, HanMucDangKy, LoiDangKy, NguonDangKy, QuyTacDangKy }
export { KHOA_HAN_MUC_GIO, KHOA_HAN_MUC_NGAY } from '../dang-ky/kiem-tra'


export const QUY_TAC_DANG_KY = raw as unknown as QuyTacDangKy

export function dangKyLoi(d: DuLieuDangKy, nguon: NguonDangKy = 'web'): LoiDangKy[] {
  return kiemTraDangKy(QUY_TAC_DANG_KY, d, nguon)
}

export function dangKyThongBao(ma: string): string {
  return thongBaoDangKy(QUY_TAC_DANG_KY, ma)
}

export function dangKyVuotHanMuc(demGio: number, demNgay: number, dangChay: number,
  hanMuc: HanMucDangKy = QUY_TAC_DANG_KY.hanMuc): 'quaGio' | 'quaNgay' | null {
  return vuotHanMuc(QUY_TAC_DANG_KY, demGio, demNgay, dangChay, hanMuc)
}

/** Hạn mức từ giá trị cau_hinh thô (vắng / hỏng -> hanMuc của tệp JSON). */
export function dangKyHanMucTu(gio?: unknown, ngay?: unknown): HanMucDangKy {
  return hanMucHieuLuc(QUY_TAC_DANG_KY, gio, ngay)
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
