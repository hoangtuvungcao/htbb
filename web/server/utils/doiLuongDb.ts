/**
 * Đổi lượng trên web /account — phần đụng CSDL (luật thuần: shared/utils/doiLuong.ts; máy chủ game: real.DoiLuongGame).
 * Web chỉ ĐỌC (tỉ giá, trần ngày, sổ lệnh sàn) và CHÈN dòng lệnh vào web_lenh_nhan_vat; game chốt dòng rồi đổi trên RAM.
 * Bảng tob_doi_tien_ngay / tob_ti_gia_luong_xu / tob_san_luong_* do game tạo (sql/htbb.sql khối them_doi_luong.sql).
 */
import { q, q1, qv, exec, coBang, dbGame } from './db'
import { LENH_CHO, LENH_CHO_DANG_NHAP, LENH_DANG_XU_LY, catLenh, taoBangLenhNhanVat } from './lenhNhanVat'
import { LENH_DOI_LUONG_KHOA, LENH_DOI_LUONG_XU } from '../../shared/utils/doiLuong'

type Ex = ReturnType<typeof dbGame>

export const DOI_LOAI_LENH = [LENH_DOI_LUONG_KHOA, LENH_DOI_LUONG_XU] as const

export interface TiGiaHomNay { ngay: string, giaTri: number, capTrungVi: number, soNguoi: number }

/** Tỉ giá lượng → xu hôm nay (giờ VN); null = máy chủ chưa tính (00:00) / chưa có bảng. */
export async function docTiGia(db: Ex, ngay: string): Promise<TiGiaHomNay | null> {
  if (!(await coBang(db, 'tob_ti_gia_luong_xu'))) return null
  const r = await q1<Record<string, unknown>>(db,
    'SELECT ti_gia, cap_trung_vi, so_nguoi FROM tob_ti_gia_luong_xu WHERE ngay = ? LIMIT 1', [ngay])
  if (!r) return null
  return { ngay, giaTri: Math.trunc(Number(r.ti_gia) || 0), capTrungVi: Math.trunc(Number(r.cap_trung_vi) || 0), soNguoi: Math.trunc(Number(r.so_nguoi) || 0) }
}

/** Đã dùng bao nhiêu trong ngày (loai = doi_luong_xu | san_luong) cho tài khoản. */
export async function docDaDung(db: Ex, userId: number, loai: string, ngay: string): Promise<number> {
  if (!(await coBang(db, 'tob_doi_tien_ngay'))) return 0
  const v = await qv<number>(db, 'SELECT da_dung FROM tob_doi_tien_ngay WHERE user_id = ? AND loai = ? AND ngay = ? LIMIT 1', [userId, loai, ngay])
  return Math.max(0, Math.trunc(Number(v ?? 0)))
}

export interface YeuCauDoi { id: number, loai: string, charname: string, so: number, trangThai: number, ketQua: string, taoLuc: string | null }

/** Yêu cầu đổi gần đây của mọi Pokémon thuộc tài khoản (mới nhất trước). */
export async function docYeuCau(db: Ex, userId: number, toiDa = 15): Promise<YeuCauDoi[]> {
  if (!(await coBang(db, 'web_lenh_nhan_vat'))) return []
  const ds = await q<Record<string, unknown>>(db,
    `SELECT l.id, l.loai, l.charname, l.cap_do, l.trang_thai, l.ket_qua, l.tao_luc
       FROM web_lenh_nhan_vat l JOIN tob_char c ON c.id = l.char_id
      WHERE c.userId = ? AND l.loai IN (?, ?) ORDER BY l.id DESC LIMIT ${Math.max(1, Math.min(50, Math.trunc(toiDa)))}`,
    [userId, ...DOI_LOAI_LENH])
  return ds.map(r => ({
    id: Math.trunc(Number(r.id)),
    loai: String(r.loai ?? ''),
    charname: String(r.charname ?? ''),
    so: Math.trunc(Number(r.cap_do) || 0),
    trangThai: Math.trunc(Number(r.trang_thai) || 0),
    ketQua: String(r.ket_qua ?? ''),
    taoLuc: r.tao_luc instanceof Date ? r.tao_luc.toISOString() : (r.tao_luc ? String(r.tao_luc) : null),
  }))
}

/** Đang chờ = server chưa đổi xong (chờ / chờ vào game / đang đổi). */
export function laDangCho(trangThai: number): boolean {
  return trangThai === LENH_CHO || trangThai === LENH_CHO_DANG_NHAP || trangThai === LENH_DANG_XU_LY
}

export interface SoLenhSan { ban: { gia: number, so: number }[], mua: { gia: number, so: number }[], tongXu24h: number, tongLuong24h: number }

/** Sổ lệnh sàn (6 mức mỗi bên) + khớp 24 giờ. Chỉ đọc. */
export async function docSoLenh(db: Ex): Promise<SoLenhSan | null> {
  if (!(await coBang(db, 'tob_san_luong_lenh'))) return null
  const muc = async (loai: number, chieu: 'ASC' | 'DESC') => (await q<Record<string, unknown>>(db,
    `SELECT gia, SUM(con_lai) AS so FROM tob_san_luong_lenh WHERE trang_thai = 0 AND loai = ? GROUP BY gia ORDER BY gia ${chieu} LIMIT 6`, [loai]))
    .map(r => ({ gia: Math.trunc(Number(r.gia) || 0), so: Math.trunc(Number(r.so) || 0) }))
  const k = await q1<Record<string, unknown>>(db,
    'SELECT COALESCE(SUM(gia * so_luong), 0) AS x, COALESCE(SUM(so_luong), 0) AS l FROM tob_san_luong_khop WHERE luc >= DATE_SUB(NOW(), INTERVAL 1 DAY)')
  return { ban: await muc(1, 'ASC'), mua: await muc(2, 'DESC'), tongXu24h: Number(k?.x ?? 0), tongLuong24h: Number(k?.l ?? 0) }
}

/** Chèn lệnh đổi (cap_do = số lượng). Trả về id dòng lệnh. */
export async function guiLenhDoi(db: Ex, loai: string, charId: number, charname: string, so: number, nguoiGui: string): Promise<number> {
  if (!(DOI_LOAI_LENH as readonly string[]).includes(loai)) throw new Error('Loại đổi không hợp lệ.')
  await taoBangLenhNhanVat(db)
  const res = await exec(db,
    'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, admin_user) VALUES (?, ?, ?, ?, ?)',
    [loai, charId, catLenh(charname, 50), Math.trunc(so), catLenh(nguoiGui, 50)])
  return Number(res.insertId)
}
