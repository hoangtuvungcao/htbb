/**
 * Đọc Pokédex 1.025 loài từ CSDL — logic THUẦN, nhận hàm truy vấn tiêm vào (`truy`) để test được không cần
 * MariaDB. Phần nối CSDL thật: server/utils/loaiDb.ts. Mọi câu SQL dùng tham số `?`.
 */
import {
  SO_LOAI_TOI_DA, docSoDex, heCuaLoai, tongChiSo,
  type CanhTienHoa, type ChiTietLoai, type LoaiTomTat, type ThanhVienHo,
} from './loai.ts'

export type Truy = (sql: string, thamSo?: unknown[]) => Promise<Record<string, any>[]>

/** Lỗi "bảng chưa có" của MariaDB (chưa chạy sql/nang_cap.sql, khối them_loai_day_du.sql). */
export function laThieuBang(e: any): boolean {
  return e?.code === 'ER_NO_SUCH_TABLE' || e?.errno === 1146 || /doesn't exist|no such table/i.test(String(e?.message ?? ''))
}

const n = (v: unknown) => Number(v ?? 0)

export async function taiDanhSachLoai(truy: Truy): Promise<LoaiTomTat[]> {
  const rows = await truy('SELECT dex, ten, he1, he2, bac, the_he, bat, nhom FROM data_loai WHERE dex BETWEEN 1 AND ? ORDER BY dex', [SO_LOAI_TOI_DA])
  return rows.map(r => ({
    dex: n(r.dex), ten: String(r.ten), he: heCuaLoai(n(r.he1), n(r.he2)), bac: n(r.bac), theHe: n(r.the_he),
    bat: n(r.bat) === 1, nhom: n(r.nhom),
  }))
}

export async function taiChiTietLoai(truy: Truy, dex: number): Promise<ChiTietLoai | null> {
  const [l] = await truy('SELECT * FROM data_loai WHERE dex = ? LIMIT 1', [dex])
  if (!l) return null
  const goc = n(l.dex_goc) || dex
  const tv = await truy('SELECT dex, ten, he1, he2, giai_doan, bat, tien_hoa_tu FROM data_loai WHERE dex_goc = ? OR dex = ? ORDER BY dex', [goc, goc])
  const thanhVien: ThanhVienHo[] = tv.map(r => ({
    dex: n(r.dex), ten: String(r.ten), he: heCuaLoai(n(r.he1), n(r.he2)), giaiDoan: n(r.giai_doan), bat: n(r.bat) === 1,
    tienHoaTu: n(r.tien_hoa_tu),
  }))
  const ids = thanhVien.map(t => t.dex)
  const canhRows = ids.length
    ? await truy(`SELECT tu_dex, sang_dex, kieu, co_che, cap, ten_vat_pham, ngay_dem, keo, dieu_kien FROM data_loai_tien_hoa WHERE tu_dex IN (${ids.map(() => '?').join(',')}) ORDER BY tu_dex, sang_dex`, ids)
    : []
  const canh: CanhTienHoa[] = canhRows.map(r => ({
    tu: n(r.tu_dex), sang: n(r.sang_dex), kieu: String(r.kieu), coChe: String(r.co_che), cap: n(r.cap),
    tenVatPham: String(r.ten_vat_pham ?? ''), ngayDem: n(r.ngay_dem), keo: n(r.keo), dieuKien: String(r.dieu_kien ?? ''),
  }))
  const chieu = (await truy('SELECT lc.o, lc.ma_chieu, lc.vai, lc.cap_hoc, c.ten, c.he, c.kieu, c.vai_tro FROM data_loai_chieu lc'
    + ' JOIN data_chieu c ON c.ma = lc.ma_chieu WHERE lc.dex = ? ORDER BY lc.o', [dex])).map(r => ({
    o: n(r.o), ma: String(r.ma_chieu), vai: String(r.vai), capHoc: n(r.cap_hoc), ten: String(r.ten), he: n(r.he),
    kieu: String(r.kieu), vaiTro: String(r.vai_tro),
  }))
  const vung = (await truy('SELECT khu_vuc, vung, cap_min, cap_max, trong_so, ngay_dem, thoi_tiet, bac_gap FROM data_loai_vung'
    + ' WHERE dex = ? ORDER BY trong_so DESC, khu_vuc, vung', [dex])).map(r => ({
    khuVuc: String(r.khu_vuc), vung: n(r.vung), capMin: n(r.cap_min), capMax: n(r.cap_max), trongSo: n(r.trong_so),
    ngayDem: n(r.ngay_dem), thoiTiet: n(r.thoi_tiet), bacGap: n(r.bac_gap),
  }))
  const nhomId = [l.nhom_trung1, l.nhom_trung2].filter(x => x !== null && x !== undefined).map(Number)
  let nhomTrung: string[] = []
  if (nhomId.length) {
    try {
      const t = await truy(`SELECT id, ten FROM data_nhom_trung_ten WHERE id IN (${nhomId.map(() => '?').join(',')})`, nhomId)
      const ten = new Map(t.map(r => [n(r.id), String(r.ten)]))
      nhomTrung = nhomId.map(i => ten.get(i) ?? `Nhóm ${i}`)
    }
    catch (e) {
      if (!laThieuBang(e)) throw e
      nhomTrung = nhomId.map(i => `Nhóm ${i}`)
    }
  }
  // Vùng hoang dã xoay vòng (loai-bo-sung): chu kỳ tuần loài này xuất hiện
  let xoayVong: ChiTietLoai['xoayVong'] = []
  try {
    xoayVong = (await truy('SELECT x.chu_ky, l.ten, MIN(x.cap_min) AS cap_min, MAX(x.cap_max) AS cap_max, MAX(x.nhom) AS thap'
      + ' FROM data_loai_xoay_vong x JOIN data_loai_xoay_vong_lich l ON l.chu_ky = x.chu_ky WHERE x.dex = ?'
      + ' GROUP BY x.chu_ky, l.ten ORDER BY x.chu_ky', [dex])).map(r => ({
      chuKy: n(r.chu_ky), ten: String(r.ten), capMin: n(r.cap_min), capMax: n(r.cap_max), thap: n(r.thap) === 1,
    }))
  }
  catch (e) {
    if (!laThieuBang(e)) throw e
  }
  let sprite: ChiTietLoai['sprite'] = null
  try {
    const [s] = await truy('SELECT trang_thai, chan_dung, trong_jar, goi_may_chu, ghi_cong FROM data_loai_sprite WHERE dex = ? LIMIT 1', [dex])
    if (s) {
      sprite = { trangThai: n(s.trang_thai), chanDung: n(s.chan_dung) === 1, trongJar: n(s.trong_jar) === 1, goiMayChu: n(s.goi_may_chu) === 1, ghiCong: String(s.ghi_cong ?? '') }
    }
  }
  catch (e) {
    if (!laThieuBang(e)) throw e
  }
  const cs = { hp: n(l.hp), tanCong: n(l.tan_cong), phongThu: n(l.phong_thu), tanCongDb: n(l.tan_cong_db), phongThuDb: n(l.phong_thu_db), tocDo: n(l.toc_do) }
  return {
    loai: {
      dex, ten: String(l.ten), he: heCuaLoai(n(l.he1), n(l.he2)), bac: n(l.bac), theHe: n(l.the_he), bat: n(l.bat) === 1,
      nhom: n(l.nhom), cs, tong: n(l.tong_chi_so) || tongChiSo(cs), gocBat: n(l.goc_bat), batDuoc: n(l.bat_duoc) === 1,
      chiSuKien: n(l.chi_su_kien) === 1, giaiDoan: n(l.giai_doan), dexGoc: goc, nhomTrung, moiTruong: String(l.moi_truong ?? ''),
      tiLeCai: n(l.ti_le_cai), chieuCao: n(l.chieu_cao), canNang: n(l.can_nang), moTa: String(l.mo_ta ?? ''),
    },
    ho: { thanhVien, canh },
    chieu,
    vung,
    xoayVong,
    sprite,
  }
}

/** Kết quả cho GET /api/pokedex/[dex]: mã HTTP + dữ liệu (hoặc thông báo). */
export async function ketQuaChiTietLoai(thamSo: unknown, truy: Truy): Promise<{ ma: number, duLieu?: ChiTietLoai, thongBao?: string }> {
  const dex = docSoDex(thamSo)
  if (dex === null) return { ma: 400, thongBao: `Số Pokédex phải là số nguyên từ 1 đến ${SO_LOAI_TOI_DA}.` }
  try {
    const d = await taiChiTietLoai(truy, dex)
    return d ? { ma: 200, duLieu: d } : { ma: 404, thongBao: 'Chưa có dữ liệu loài này.' }
  }
  catch (e) {
    if (laThieuBang(e)) return { ma: 404, thongBao: 'Máy chủ chưa có dữ liệu Pokédex đầy đủ.' }
    throw e
  }
}
