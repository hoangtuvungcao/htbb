/**
 * Cẩm nang (wiki cơ chế) — dữ liệu + danh mục trang.
 *
 * - Dữ liệu số: shared/data/cam-nang.json, sinh bởi `node scripts/dong-bo-cam-nang.mjs` từ repo
 *   game HTBB (CSV thiết kế, tools/luu_tru_sinh_du_lieu/sql_nguon/pokemon_lau_dai.sql + ThongSoLauDai.java). KHÔNG sửa tay JSON. Lúc chạy, mục có bảng SQL
 *   được đè bằng dữ liệu CSDL game (app/composables/useCamNang.ts, server/utils/camNangSql.ts).
 * - Danh mục trang (DS_CAM_NANG) nằm ở danhMucCamNang.ts (không kéo JSON này vào mọi trang).
 * - Chỉ import (tự động) CAM_NANG ở trang cần số liệu — module này mang cả JSON (~90 KB).
 */
import raw from '../data/cam-nang.json'

export interface DongCap {
  cap: number
  expLenCap: number
  gioTichLuy: number
  gioCap: number
  quaiMoiGio: number
  expMoiQuai: number
  xuMoiQuai: number
  xuMoiGio: number
}
export interface ChuongNV { so: number, ten: string, diaDanh: string, cap: number, soBuoc: number, ngayMo: number }
export interface CungNV { ma: string, ten: string, vung: string, nam: number, chuong: ChuongNV[], capTu: number, capDen: number }
export interface NhomNV { ten: string, vung: string, capTu: number, soBuoc: number }
export interface NhiemVuDong {
  ten: string
  cap: number
  npc: string
  diaDanh: string
  mucTieu: string
  soLuong: number | null
  lapLai: string
  /** Câu mục tiêu hiển thị trong game (muc_tieu_text). */
  yeuCau: string
}
export interface SuKienTuan {
  tuan: number
  thang: number
  ten1: string
  ten2: string
  phan: string
  loai: string
  coChe: string
  soNgay: number
  phanThuong: string
  mocGiuChan: string
}
/** anh: đường dẫn ảnh xem trước (rỗng nếu chưa có), rong/cao: kích thước thật của ảnh. */
export interface BanDoInfo { id: number, loai: string, ten: string, vung: string, anh: string, rong: number, cao: number }

export interface CamNangData {
  capDo: {
    capToiDa: number
    tranCap: { ngay: number, cap: number }[]
    bang: DongCap[]
    nangLuong: { phutNgay: number, phutTichToiDa: number, bacMet: { phut: number, tocDo: number }[] }
    batKip: { heSoPhanTram: number, khoangCap: number }
    tanThu: { duoiCap: number, heSoPhanTram: number }
    tinhThong: { phanTramCap: number, diemMoiMua: number, toiDa: number, phanNghinMoiDiem: number }
  }
  trangBi: {
    soO: number
    o: { loai: 'mac' | 'vat-cam' | 'huy-hieu' | 'ruy-bang', ten: string, vai: string, soMon: number }[]
    bac: { bac: number, ten: string, cap: number, ngayMo: number, gia: number }[]
    soMonMuaMoiBac: number
    nangCap: { plus: number, tiLe: number, da: number, buaLuongKhoa: number, chiSoPhanTram: number }[]
    chiPhiNangCap: number[][]
    pityChia: number
    matCapTu: number
    giuCapKhiDoiBac: number
    daRoiPhanNghin: number
    daTuPhanRa: number
    boTrangBi: { soMon: number, chiSo: string, phanTram: number }[]
    ngocHe: { capToiDa: number, giaGioPhanTram: number, tangPhanTram: number }
  }
  kinhTe: {
    tiLeRoiXu: number
    thueCho: number
    phiDangBan: number
    tranGiaoDichGio: number
    tieuHaoPhanTram: number
    suaDoPhanTram: number
    dichChuyenLanNgay: number
    dichChuyenPhanTramGio: number
    suKienPhanXu: number
    giftcodeToiDaThang: number
    trangTri: { phanNghinNgay: number, nguongGio: number }
    lichDangNhap: number[]
    luongXepHangTuan: { top: number, luong: number }[]
  }
  nhiemVu: {
    demLoai: Record<string, number>
    gioiHanNgay: number
    gioiHanTuan: number
    veChuongTuan: number
    veChuongToiDa: number
    veChuongBanDau: number
    huongDan: NhiemVuDong[]
    cung: CungNV[]
    phu: NhomNV[]
    tuyen: NhomNV[]
    hamNguc: NhiemVuDong[]
    suPham: NhiemVuDong[]
    doi: NhiemVuDong[]
    quayLai: NhiemVuDong[]
    thanhTuu: { ten: string, soLuong: number, kieu: string, luongKhoa: number, luong: number }[]
    ngay: string[]
    tuan: string[]
    pokedexSoLoai: number
  }
  suKien: SuKienTuan[]
  banDo: BanDoInfo[]
}

export const CAM_NANG = raw as unknown as CamNangData

/** Sự kiện của tuần `tuan` và `soTuanSau` tuần kế tiếp (vòng qua năm mới). */
export function suKienTuTuan(tuan: number, soTuanSau = 2, ds: SuKienTuan[] = CAM_NANG.suKien): SuKienTuan[] {
  const ra: SuKienTuan[] = []
  for (let i = 0; i <= soTuanSau; i++) {
    const t = ((tuan - 1 + i) % 52) + 1
    const sk = ds.find(s => s.tuan === t)
    if (sk) ra.push(sk)
  }
  return ra
}
