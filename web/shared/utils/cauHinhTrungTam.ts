/**
 * CẤU HÌNH TRUNG TÂM — hàm THUẦN (không DB, không auto-import Nuxt): kiểm giá trị, nhóm, tìm kiếm, so sánh.
 *
 * Danh mục khoá: shared/utils/cauHinhTrungTam.gen.ts, SINH từ HTBB/tools/cauhinh/danh_muc.json
 * (python3 tools/cauhinh/sinh_cau_hinh.py) — cùng nguồn với máy chủ game (com.htbb.cauhinh.DanhMucSinh).
 * Luật kiểm khớp HTBB/src/com/htbb/cauhinh/KiemGiaTri.java; bộ ví dụ dùng chung HTBB/tools/cauhinh/vi_du_kiem.tsv
 * (cả tests/cau-hinh-trung-tam.test.ts lẫn HTBB/test/KiemCauHinhTrungTam.java chạy cùng bộ đó).
 *
 * Thứ tự ưu tiên khi máy chủ đọc: bảng cau_hinh (CSDL) > server.ini > mặc định. Trang chỉ ghi CSDL.
 *
 * PHÂN LOẠI (trường `lop`, docs/cau-hinh.md): vắng = chỉnh trên web; 'co_dinh' / 'luon_bat' = khoá ẨN — không hiện
 * trên trang, máy thật bỏ qua CSDL (giá trị cố định = macDinh; đổi khẩn cấp bằng server.ini), API lưu từ chối.
 * Máy thử (choPhepThuNghiem) vẫn thấy và sửa được.
 */
import { DANH_MUC_TT, NHOM_TT, type KhoaTT } from './cauHinhTrungTam.gen.ts'


const THEO_KHOA: Record<string, KhoaTT> = Object.fromEntries(DANH_MUC_TT.map(k => [k.khoa, k]))

/** Định nghĩa một khoá (an toàn với __proto__ / toString). */
export function khoaTT(khoa: unknown): KhoaTT | null {
  if (typeof khoa !== 'string' || !Object.prototype.hasOwnProperty.call(THEO_KHOA, khoa)) return null
  return THEO_KHOA[khoa]!
}

export const DO_DAI_COT_TT = 255

export type KetQuaKiemTT = { ok: true, giaTri: string } | { ok: false, loi: string }

function chuoiVao(v: unknown): string | null {
  if (typeof v === 'string') return v
  if (typeof v === 'number' && Number.isFinite(v)) return String(v)
  if (typeof v === 'boolean') return v ? '1' : '0'
  return null
}

function kiemSo(k: KhoaTT, v: string): KetQuaKiemTT {
  if (!/^-?(0|[1-9]\d{0,17})$/.test(v)) return { ok: false, loi: 'phải là số nguyên' }
  const n = Number(v)
  if (!Number.isSafeInteger(n)) return { ok: false, loi: 'phải là số nguyên' }
  if (k.min !== undefined && n < k.min) return { ok: false, loi: `nhỏ nhất ${k.min}` }
  if (k.max !== undefined && n > k.max) return { ok: false, loi: `lớn nhất ${k.max}` }
  if (k.buoc && n % k.buoc !== 0) return { ok: false, loi: `phải là bội số của ${k.buoc}` }
  return { ok: true, giaTri: String(n) }
}

function ngayHopLe(v: string, coGio: boolean): boolean {
  const m = coGio ? /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/.exec(v) : /^(\d{4})-(\d{2})-(\d{2})$/.exec(v)
  if (!m) return false
  const [y, mo, d] = [Number(m[1]), Number(m[2]), Number(m[3])]
  const t = new Date(Date.UTC(y, mo - 1, d))
  if (t.getUTCFullYear() !== y || t.getUTCMonth() !== mo - 1 || t.getUTCDate() !== d) return false
  if (coGio) {
    const [h, mi, s] = [Number(m[4]), Number(m[5]), Number(m[6])]
    if (h > 23 || mi > 59 || s > 59) return false
  }
  return true
}

const luaChonCo = (k: KhoaTT, x: string) => !k.luaChon || k.luaChon.some(([gt]) => gt === x)

/** Kiểm + chuẩn hoá MỘT giá trị theo luật của khoá (giống KiemGiaTri.java). KHÔNG kẹp ngầm. */
export function kiemGiaTriTT(k: KhoaTT, tho: unknown): KetQuaKiemTT {
  const s0 = chuoiVao(tho)
  if (s0 === null) return { ok: false, loi: 'thiếu giá trị' }
  let v = s0.trim()
  if (k.hoa) v = v.toUpperCase()
  if (v.length > DO_DAI_COT_TT) return { ok: false, loi: `dài quá ${DO_DAI_COT_TT} ký tự` }
  // eslint-disable-next-line no-control-regex
  if (/[\u0000-\u001f\u007f-\u009f]/.test(v)) return { ok: false, loi: 'có ký tự điều khiển / xuống dòng' }
  const doDai = k.doDai ?? 255
  switch (k.kieu) {
    case 'co':
      return v === '0' || v === '1' ? { ok: true, giaTri: v } : { ok: false, loi: 'chỉ nhận 0 (tắt) hoặc 1 (bật)' }
    case 'so':
      return kiemSo(k, v)
    case 'chon':
      return luaChonCo(k, v) ? { ok: true, giaTri: v } : { ok: false, loi: 'không nằm trong danh sách lựa chọn' }
    case 'chuoi':
      if (Array.from(v).length > doDai) return { ok: false, loi: `tối đa ${doDai} ký tự` }
      if (k.mau && !new RegExp(k.mau).test(v)) return { ok: false, loi: 'sai định dạng' }
      return { ok: true, giaTri: v }
    case 'ds_chuoi': {
      const ds: string[] = []
      for (const phan of v.split(',')) {
        const x = phan.trim()
        if (!x) continue
        if (k.mau && !new RegExp(k.mau).test(x)) return { ok: false, loi: `phần tử "${x}" sai định dạng` }
        if (!luaChonCo(k, x)) return { ok: false, loi: `phần tử "${x}" không nằm trong danh sách lựa chọn` }
        if (!ds.includes(x)) ds.push(x)
      }
      const ra = ds.join(',')
      if (ra.length > doDai) return { ok: false, loi: `tối đa ${doDai} ký tự` }
      return { ok: true, giaTri: ra }
    }
    case 'ds_so': {
      if (!v) return { ok: false, loi: 'cần ít nhất một số' }
      const phan = v.split(',')
      if (k.soPhanTu && phan.length !== k.soPhanTu) return { ok: false, loi: `cần đúng ${k.soPhanTu} số` }
      const ra: string[] = []
      for (let i = 0; i < phan.length; i++) {
        const r = kiemSo(k, phan[i]!.trim())
        if (!r.ok) return { ok: false, loi: `số thứ ${i + 1}: ${r.loi}` }
        ra.push(r.giaTri)
      }
      return { ok: true, giaTri: ra.join(',') }
    }
    case 'ngay':
      return !v || ngayHopLe(v, false) ? { ok: true, giaTri: v } : { ok: false, loi: 'cần yyyy-MM-dd hoặc để trống' }
    case 'ngay_gio':
      return !v || ngayHopLe(v, true) ? { ok: true, giaTri: v } : { ok: false, loi: 'cần yyyy-MM-dd HH:mm:ss hoặc để trống' }
    case 'nhip':
      return /^[1-9]\d{0,5}\/[1-9]\d{0,5}$/.test(v) ? { ok: true, giaTri: v } : { ok: false, loi: 'cần dạng số_lần/giây, vd 5/10' }
  }
  return { ok: false, loi: 'kiểu chưa hỗ trợ' }
}

/** Khoá ẩn (cố định / luôn bật): không hiện trên trang, máy thật luôn dùng giá trị cố định. */
export function anTT(k: KhoaTT): boolean {
  return k.lop === 'co_dinh' || k.lop === 'luon_bat'
}

export const TEN_LOP_TT: Record<'chinh' | 'co_dinh' | 'luon_bat', string> = {
  chinh: 'Chỉnh trên web', co_dinh: 'Cố định (ẩn)', luon_bat: 'Luôn bật (ẩn)',
}

/** Số khoá theo lớp (trang + tài liệu). */
export function demLopTT(ds: KhoaTT[] = DANH_MUC_TT): Record<'chinh' | 'co_dinh' | 'luon_bat', number> {
  const ra = { chinh: 0, co_dinh: 0, luon_bat: 0 }
  for (const k of ds) ra[k.lop ?? 'chinh']++
  return ra
}

/**
 * Giá trị có hiệu lực ở phía web cho một khoá đã biết giá trị lưu `luu` (null = không có dòng). Khoá ẩn trên máy
 * thật: luôn macDinh (giống máy chủ game bỏ qua CSDL). Giá trị lưu sai luật: macDinh.
 */
export function hieuLucWebTT(k: KhoaTT, luu: string | null | undefined, choPhepThuNghiem = false): string {
  if (anTT(k) && !choPhepThuNghiem) return k.macDinh
  if (luu === null || luu === undefined) return k.macDinh
  const r = kiemGiaTriTT(k, luu)
  return r.ok ? r.giaTri : k.macDinh
}

/** Khoá sửa được từ trang không (không phải env / chỉ đọc / khoá ẩn; khoá thử nghiệm chỉ khi cho phép). */
export function suaDuocTT(k: KhoaTT, choPhepThuNghiem: boolean): { duoc: boolean, lyDo?: string } {
  if (k.chiDoc || k.bang === 'env') return { duoc: false, lyDo: 'Đặt trong biến môi trường của web — không sửa từ trang.' }
  if (anTT(k) && !choPhepThuNghiem) return { duoc: false, lyDo: `Khoá cố định — máy thật luôn dùng "${k.macDinh}" (đổi khẩn cấp: server.ini).` }
  if (k.khoaProd && !choPhepThuNghiem) return { duoc: false, lyDo: 'Khoá thử nghiệm — máy thật chỉ dùng mặc định.' }
  return { duoc: true }
}

export interface ThayDoiTT {
  khoa: string
  ten: string
  nhom: string
  cu: string | null
  moi: string | null
  apDung: KhoaTT['apDung']
  loi?: string
}

/**
 * So sánh form với giá trị đang lưu: danh sách thay đổi (đã chuẩn hoá) + lỗi từng khoá. `moi[k] === null` = xoá
 * dòng (máy chủ về server.ini / mặc định). Khoá không đổi bị bỏ.
 */
export function soSanhTT(dangLuu: Record<string, string | null | undefined>, moi: Record<string, unknown>,
  choPhepThuNghiem = false): { thayDoi: ThayDoiTT[], loi: Record<string, string> } {
  const thayDoi: ThayDoiTT[] = []
  const loi: Record<string, string> = {}
  for (const [khoa, v] of Object.entries(moi)) {
    const k = khoaTT(khoa)
    if (!k) { loi[khoa] = 'Khoá không có trong danh mục.'; continue }
    const sd = suaDuocTT(k, choPhepThuNghiem)
    const cu = dangLuu[khoa] ?? null
    let moiChuan: string | null
    if (v === null) {
      moiChuan = null
    } else {
      const r = kiemGiaTriTT(k, v)
      if (!r.ok) { loi[khoa] = `${k.ten}: ${r.loi}`; continue }
      moiChuan = r.giaTri
    }
    if (moiChuan === cu) continue
    if (!sd.duoc) {
      // Cho phép đưa khoá thử nghiệm / khoá ẩn VỀ mặc định / xoá dòng (an toàn, dọn dòng cũ), cấm mọi giá trị khác.
      if (!((k.khoaProd || anTT(k)) && !k.chiDoc && k.bang !== 'env' && (moiChuan === null || moiChuan === k.macDinh))) {
        loi[khoa] = `${k.ten}: ${sd.lyDo}`
        continue
      }
    }
    thayDoi.push({ khoa, ten: k.ten, nhom: k.nhom, cu, moi: moiChuan, apDung: k.apDung })
  }
  return { thayDoi, loi }
}

/** Tìm khoá theo tên khoá / nhãn / mô tả, không phân biệt dấu & hoa thường. */
export function timTT(q: string, ds: KhoaTT[] = DANH_MUC_TT): KhoaTT[] {
  const bo = (s: string) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase()
  const t = bo(q.trim())
  if (!t) return ds
  return ds.filter(k => bo(`${k.khoa} ${k.ten} ${k.moTa}`).includes(t))
}

/** Nhóm theo thứ tự NHOM_TT. Mặc định BỎ khoá ẩn (cố định / luôn bật); nhóm rỗng bị bỏ. */
export function nhomTT(ds: KhoaTT[] = DANH_MUC_TT, hienAn = false): { ma: string, ten: string, moTa: string, khoa: KhoaTT[] }[] {
  return NHOM_TT.map(n => ({ ma: n.ma, ten: n.ten, moTa: n.moTa, khoa: ds.filter(k => k.nhom === n.ma && (hienAn || !anTT(k))) }))
    .filter(n => n.khoa.length > 0)
}

/** Giá trị hiển thị (khoá nhạy cảm che đi). */
export function hienThiTT(k: KhoaTT | null, v: string | null | undefined): string {
  if (v === null || v === undefined) return '—'
  if (k?.nhay && v !== '') return '(ẩn)'
  if (v === '') return '(trống)'
  if (k?.kieu === 'co') return v === '1' ? 'Bật' : v === '0' ? 'Tắt' : v
  if (k?.luaChon && k.kieu === 'chon') return k.luaChon.find(([gt]) => gt === v)?.[1] ?? v
  return v
}

/** Trạng thái yêu cầu khởi động lại an toàn (bảng cau_hinh_khoi_dong_lai). */
export const TRANG_THAI_KDL: Record<string, string> = {
  '': 'Không có yêu cầu',
  cho: 'Đang chờ máy chủ nhận (≤ 5 giây)',
  dang_dem: 'Đang đếm ngược',
  huy: 'Đang huỷ…',
  da_huy: 'Đã huỷ',
  dang_thoat: 'Đang lưu dữ liệu & thoát',
  da_xong: 'Đã khởi động lại',
}

export const KDL_GIAY_MIN = 10
export const KDL_GIAY_MAX = 1800

export function kiemYeuCauKhoiDongLai(b: unknown): { ok: true, giay: number, lyDo: string } | { ok: false, loi: string } {
  const o = (b && typeof b === 'object' ? b : {}) as Record<string, unknown>
  const giay = Number(o.giay)
  const lyDo = typeof o.lyDo === 'string' ? o.lyDo.trim() : ''
  if (!Number.isInteger(giay) || giay < KDL_GIAY_MIN || giay > KDL_GIAY_MAX) return { ok: false, loi: `Đếm ngược ${KDL_GIAY_MIN}–${KDL_GIAY_MAX} giây.` }
  if (Array.from(lyDo).length < 5 || Array.from(lyDo).length > 200) return { ok: false, loi: 'Ghi lý do (5–200 ký tự) — người chơi sẽ thấy.' }
  // eslint-disable-next-line no-control-regex
  if (/[\u0000-\u001f]/.test(lyDo)) return { ok: false, loi: 'Lý do chỉ một dòng.' }
  return { ok: true, giay, lyDo }
}
