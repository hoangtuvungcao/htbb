/**
 * Giftcode AN TOÀN (bảng tob_giftcode / tob_giftcode_log, HTBB/du_lieu/sql_nguon/them_bang_lau_dai.sql).
 * Bản TS của com.htbb.pokemon.GiftcodeAnToanLogic — sửa bên Java thì sửa cả ở đây. Server game
 * kiểm lại y hệt khi phát quà, web kiểm trước để báo lỗi sớm.
 *   Phần thưởng chỉ gồm: LUONGKHOA:n | GEM:id:n (luôn khoá) | ITEM:id:n (n ≤ 10, khoá).
 *   Giá trị mã = lượng khoá + số đá + 5 × số ITEM ≤ giftcode.gia_tri_toi_da (30).
 *   Ngân sách tháng: tổng gia_tri trong tob_giftcode_log của tháng ≤ giftcode.ngan_sach_thang (20 000).
 *   Vật phẩm: danh sách trắng gcKiemVatPham (shared/data/giftcode-an-toan.json), server game kiểm lại theo cấp nhân vật.
 *   Danh sách TRANG BỊ phát được đọc lúc chạy từ data_noi_dung_trang_bi (id_item, cùng nguồn với server game);
 *   bản trangBi trong JSON chỉ là dự phòng lúc build khi CSDL không đọc được.
 */
import duLieuGiftcode from '../../shared/data/giftcode-an-toan.json'
import { kiemVatPhamGiftcode, type DuLieuGiftcodeAnToan } from '../../shared/giftcode/kiem-vat-pham'
import { trangBiGiftcodeTuSql } from '../../shared/utils/duLieuSqlThuan'
import { docBangGame } from './duLieuSql'

/** Bản dự phòng (JSON lúc build). */
export const GC_DU_LIEU = duLieuGiftcode as unknown as DuLieuGiftcodeAnToan

/** Danh sách trắng đang dùng: trangBi từ data_noi_dung_trang_bi nếu đọc được, phần còn lại từ JSON. */
export async function gcDuLieu(): Promise<DuLieuGiftcodeAnToan> {
  const trangBi = trangBiGiftcodeTuSql(await docBangGame('data_noi_dung_trang_bi'))
  return trangBi ? { ...GC_DU_LIEU, trangBi } : GC_DU_LIEU
}

export const GC_GIA_TRI_ITEM = 5
export const GC_MAC_DINH = { giaTriToiDa: 30, nganSachThang: 20000, toiDaThang: 2 }

export function gcChuanHoaMa(s: unknown): string | null {
  const m = typeof s === 'string' ? s.trim().toLowerCase() : ''
  return m.length >= 3 && m.length <= 32 && /^[a-z0-9_-]+$/.test(m) ? m : null
}

export function gcChuanHoaThuong(s: unknown): string | null {
  const v = typeof s === 'string' ? s.trim() : ''
  if (!v) return null
  const ra: string[] = []
  for (const phan of v.split(',')) {
    const t = phan.trim().split(':').map(x => x.trim())
    const k = (t[0] ?? '').toUpperCase()
    const so = (x: string | undefined) => (x !== undefined && /^-?\d+$/.test(x) ? Number(x) : Number.NaN)
    if (k === 'LUONGKHOA' && t.length === 2) {
      const n = so(t[1])
      if (!(n > 0)) return null
      ra.push(`LUONGKHOA:${n}`)
    }
    else if (k === 'GEM' && (t.length === 3 || t.length === 4)) {
      const id = so(t[1]); const n = so(t[2])
      if (!(id >= 0) || !(n > 0)) return null
      ra.push(`GEM:${id}:${n}:-1`)
    }
    else if (k === 'ITEM' && t.length === 3) {
      const id = so(t[1]); const n = so(t[2])
      if (!(id >= 0) || !(n > 0) || n > 10) return null
      ra.push(`ITEM:${id}:${n}`)
    }
    else return null
  }
  return ra.join(',')
}

export function gcGiaTri(chuan: string): number {
  let v = 0
  for (const p of chuan.split(',')) {
    const t = p.split(':')
    if (t[0] === 'LUONGKHOA') v += Number(t[1])
    else if (t[0] === 'GEM') v += Number(t[2])
    else if (t[0] === 'ITEM') v += GC_GIA_TRI_ITEM * Number(t[2])
  }
  return v
}

/**
 * Danh sách trắng vật phẩm (shared/data/giftcode-an-toan.json, cùng luật với server game): tiêu hao / nguyên liệu /
 * tín vật, trang bị thường có cấp ≤ cấp tối thiểu của mã, ngọc không bán bằng lượng. null = hợp lệ.
 */
export async function gcKiemVatPham(chuan: string, capToiThieu: number): Promise<string | null> {
  const ids = chuan.split(',').filter(p => p.startsWith('ITEM:')).map(p => Number(p.split(':')[1]))
  const cap: Record<number, number> = {}
  if (ids.length > 0) {
    const cho = ids.map(() => '?').join(',')
    const rows = await q<{ id: number, level: number }>(dbGame(), `SELECT id, level FROM data_item WHERE id IN (${cho})`, ids)
      .catch(() => [] as { id: number, level: number }[])
    for (const r of rows) cap[Number(r.id)] = Number(r.level)
  }
  return kiemVatPhamGiftcode(await gcDuLieu(), chuan, capToiThieu, id => cap[id] ?? -1)
}

/** Giới hạn đang dùng: cau_hinh (nếu có khoá) đè mặc định. */
export async function gcGioiHan(): Promise<typeof GC_MAC_DINH> {
  const ra = { ...GC_MAC_DINH }
  try {
    const rows = await q<{ khoa: string, gia_tri: string }>(dbGame(),
      "SELECT khoa, gia_tri FROM cau_hinh WHERE khoa IN ('giftcode.gia_tri_toi_da','giftcode.ngan_sach_thang','giftcode.toi_da_thang')")
    for (const r of rows) {
      const n = Number(r.gia_tri)
      if (!Number.isFinite(n)) continue
      if (r.khoa === 'giftcode.gia_tri_toi_da') ra.giaTriToiDa = n
      if (r.khoa === 'giftcode.ngan_sach_thang') ra.nganSachThang = n
      if (r.khoa === 'giftcode.toi_da_thang') ra.toiDaThang = n
    }
  }
  catch { /* chưa có bảng cau_hinh: dùng mặc định */ }
  return ra
}

export function gcThangNay(): number {
  const d = new Date()
  return d.getFullYear() * 100 + d.getMonth() + 1
}

/** Mô tả dễ đọc + tên vật phẩm cho chuỗi đã chuẩn hoá. */
export async function gcMoTa(chuan: string): Promise<string[]> {
  const ids = chuan.split(',').filter(p => p.startsWith('ITEM:') || p.startsWith('GEM:')).map(p => Number(p.split(':')[1]))
  const ten = await tenVatPhamTheoId(dbGame(), ids).catch(() => ({} as Record<number, string>))
  return chuan.split(',').map((p) => {
    const t = p.split(':')
    if (t[0] === 'LUONGKHOA') return `${Number(t[1]).toLocaleString('vi-VN')} lượng khoá`
    if (t[0] === 'GEM') return `${t[2]} × ${ten[Number(t[1])] ?? `đá/ngọc #${t[1]}`} (khoá)`
    return `${t[2]} × ${ten[Number(t[1])] ?? `vật phẩm #${t[1]}`} (khoá)`
  })
}
