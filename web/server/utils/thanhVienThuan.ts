/**
 * THÀNH VIÊN HTBB — hàm THUẦN (không database, không auto-import). Bản sao phía web của
 * HTBB/src/com/htbb/pokemon/ThanhVienLogic.java — test HTBB/test/KiemThanhVien.java đối chiếu mã,
 * khoá cau_hinh và danh sách mặc định. Thiết kế: docs/van-hanh.md.
 */

export const TV_K_BAT = 'thanh_vien.bat'
export const TV_K_BAN_BAT = 'thanh_vien.ban_bat'
export const TV_K_GIA = 'thanh_vien.gia'
export const TV_K_THOI_HAN = 'thanh_vien.thoi_han_ngay'
export const TV_K_CHUC_NANG = 'thanh_vien.chuc_nang'
export const TV_K_MIEN_CAP = 'thanh_vien.mien_cap'
export const TV_K_THONG_BAO = 'thanh_vien.thong_bao'
// Kích hoạt tài khoản (5h_active): chưa kích hoạt vẫn chơi, chỉ khoá vài tính năng — cùng một cổng với Thành viên.
export const KH_K_CHAN_DANG_NHAP = 'kich_hoat.chan_dang_nhap'
export const KH_K_CHUC_NANG = 'kich_hoat.chuc_nang'
export const KH_K_THONG_BAO = 'kich_hoat.thong_bao'
export const KH_K_TV_LA_KH = 'kich_hoat.thanh_vien_la_kich_hoat'
export const KH_CHUC_NANG_MAC_DINH = 'giao_dich,chat_the_gioi,cho,chuyen_luong,nhan_tang,giftcode'
export const TV_KHOA = [TV_K_BAT, TV_K_BAN_BAT, TV_K_GIA, TV_K_THOI_HAN, TV_K_CHUC_NANG, TV_K_MIEN_CAP, TV_K_THONG_BAO,
  KH_K_CHAN_DANG_NHAP, KH_K_CHUC_NANG, KH_K_THONG_BAO, KH_K_TV_LA_KH]

export const TV_GIA_MAC_DINH = 20000
export const TV_MA_GOI = 'THANH_VIEN'
export const TV_NGAY_MS = 86400000

export interface ChucNangTv { ma: string, ten: string, cho: string }

/** Sổ đăng ký tính năng khoá được (khớp ThanhVienLogic.CHUC_NANG). */
export const TV_CHUC_NANG: ChucNangTv[] = [
  { ma: 'giao_dich', ten: 'Giao dịch trực tiếp', cho: 'mời / nhận lời giao dịch' },
  { ma: 'nhan_tang', ten: 'Nhận giao dịch / quà từ người khác', cho: 'là người được mời giao dịch, người nhận hoa/quà' },
  { ma: 'chat_the_gioi', ten: 'Chat thế giới (loa)', cho: 'dùng loa phát toàn máy chủ' },
  { ma: 'cho', ten: 'Chợ (ký gửi + chợ trang trí)', cho: 'rao bán / mua ở chợ người chơi' },
  { ma: 'cho_sinh_thai', ten: 'Chợ sinh thái', cho: 'rao / mua / đặt đơn ở chợ sinh thái' },
  { ma: 'chuyen_luong', ten: 'Chuyển lượng', cho: 'chuyển lượng cho người khác' },
  { ma: 'bang', ten: 'Đội', cho: 'tạo đội, mời / nhận lời vào đội' },
  { ma: 'pvp_xep_hang', ten: 'Giải Liên minh xếp hạng', cho: 'đăng ký giải Liên minh' },
  { ma: 'giftcode', ten: 'Giftcode', cho: 'nhập giftcode' },
  { ma: 'qua_online', ten: 'Quà online', cho: 'nhận quà thời gian online' },
  { ma: 'gui_thu', ten: 'Nhắn tin riêng / tặng quà', cho: 'gửi tin nhắn riêng, tặng hoa / quà' },
]

export const TV_CHUC_NANG_MAC_DINH = 'giao_dich,nhan_tang,chat_the_gioi,cho,cho_sinh_thai,chuyen_luong,bang,pvp_xep_hang,giftcode'

export interface CauHinhTv {
  bat: boolean
  banBat: boolean
  gia: number
  thoiHanNgay: number
  chucNang: string[]
  mienCap: number
  thongBao: string
  /** Kích hoạt: 1 = chưa kích hoạt không vào được game (mặc định 0). */
  khChanDangNhap: boolean
  /** Tính năng khoá khi CHƯA kích hoạt. */
  khChucNang: string[]
  khThongBao: string
  /** Thành viên được coi như đã kích hoạt (mặc định có). */
  khTvLaKh: boolean
}

function soDuong(v: string | undefined, macDinh: number, toiDa: number): number {
  const n = Number(String(v ?? '').trim())
  if (!Number.isInteger(n) || String(v ?? '').trim() === '' || n < 0) return macDinh
  return Math.min(n, toiDa)
}

export function laMaTv(ma: string): boolean {
  return TV_CHUC_NANG.some(c => c.ma === ma)
}

/** Đọc từ các dòng cau_hinh (khớp ThanhVienLogic.docCauHinh). */
function docDs(ds: string): string[] {
  return [...new Set(ds.split(/[,;\s]+/).map(x => x.trim().toLowerCase()).filter(laMaTv))]
}

export function docCauHinhTv(kv: Record<string, string | undefined>): CauHinhTv {
  const chucNang = docDs(kv[TV_K_CHUC_NANG] ?? TV_CHUC_NANG_MAC_DINH)
  return {
    khChanDangNhap: (kv[KH_K_CHAN_DANG_NHAP] ?? '').trim() === '1',
    khChucNang: docDs(kv[KH_K_CHUC_NANG] ?? KH_CHUC_NANG_MAC_DINH),
    khThongBao: (kv[KH_K_THONG_BAO] ?? '').trim(),
    khTvLaKh: (kv[KH_K_TV_LA_KH] ?? '').trim() !== '0',
    bat: (kv[TV_K_BAT] ?? '').trim() === '1',
    banBat: (kv[TV_K_BAN_BAT] ?? '').trim() === '1',
    gia: soDuong(kv[TV_K_GIA], TV_GIA_MAC_DINH, 10000000),
    thoiHanNgay: soDuong(kv[TV_K_THOI_HAN], 0, 36500),
    chucNang,
    mienCap: soDuong(kv[TV_K_MIEN_CAP], 0, 200),
    thongBao: (kv[TV_K_THONG_BAO] ?? '').trim(),
  }
}

/** Kiểm dữ liệu admin gửi lên. null = hợp lệ. */
export function kiemCauHinhTv(c: Partial<CauHinhTv>): string | null {
  if (c.gia !== undefined && (!Number.isInteger(c.gia) || c.gia < 10000 || c.gia > 2000000 || c.gia % 1000 !== 0)) {
    return 'Giá phải chẵn nghìn, 10.000 – 2.000.000 đ.'
  }
  if (c.thoiHanNgay !== undefined && (!Number.isInteger(c.thoiHanNgay) || c.thoiHanNgay < 0 || c.thoiHanNgay > 36500)) {
    return 'Thời hạn: 0 (vĩnh viễn) đến 36.500 ngày.'
  }
  if (c.mienCap !== undefined && (!Number.isInteger(c.mienCap) || c.mienCap < 0 || c.mienCap > 200)) return 'Miễn cấp: 0–200.'
  if (c.chucNang !== undefined && c.chucNang.some(m => !laMaTv(m))) return 'Có mã tính năng không hợp lệ.'
  if (c.thongBao !== undefined && c.thongBao.length > 250) return 'Thông báo tối đa 250 ký tự.'
  if (c.khChucNang !== undefined && c.khChucNang.some(m => !laMaTv(m))) return 'Có mã tính năng (kích hoạt) không hợp lệ.'
  if (c.khThongBao !== undefined && c.khThongBao.length > 250) return 'Thông báo kích hoạt tối đa 250 ký tự.'
  return null
}

/** het_han_ms: 0 = vĩnh viễn, < 0 = thu hồi, > 0 = còn hạn tới lúc đó. */
export function conHanTv(hetHanMs: number | null | undefined, bayGio = Date.now()): boolean {
  if (hetHanMs === null || hetHanMs === undefined) return false
  return hetHanMs === 0 || hetHanMs > bayGio
}

export function hanMoiTv(coDong: boolean, hetHanCu: number, bayGio: number, ngay: number): number {
  if (coDong && hetHanCu === 0) return 0
  if (ngay <= 0) return 0
  const goc = coDong && hetHanCu > bayGio ? hetHanCu : bayGio
  return goc + ngay * TV_NGAY_MS
}

export function hanSauThuHoiTv(hetHanCu: number, bayGio: number, ngayDaCap: number): number {
  if (hetHanCu === 0) return ngayDaCap <= 0 ? -1 : 0
  if (ngayDaCap <= 0) return -1
  if (hetHanCu < 0) return hetHanCu
  const moi = hetHanCu - ngayDaCap * TV_NGAY_MS
  return moi <= bayGio ? -1 : moi
}

/** Danh sách tài khoản admin dán vào (dấu phẩy / xuống dòng / khoảng trắng). Tối đa 500. */
export function tachDanhSachTk(s: string): string[] {
  return [...new Set(String(s ?? '').split(/[\s,;]+/).map(x => x.trim().toLowerCase()).filter(x => /^[a-z0-9_.@-]{2,50}$/.test(x)))].slice(0, 500)
}
