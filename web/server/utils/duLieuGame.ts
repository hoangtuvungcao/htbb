/**
 * Đọc dữ liệu game cho /admin/du-lieu-game (chỉ đọc). Danh mục + dựng SQL ở duLieuGameDanhMuc.ts (thuần).
 */
import type { BoDuLieu } from './duLieuGameDanhMuc'

export interface KetQuaBoDuLieu {
  ma: string
  coBang: boolean
  tong: number
  dong: Record<string, unknown>[]
  catBot: boolean
}

function catGiaTri(v: unknown): unknown {
  if (typeof v === 'bigint') return Number(v)
  if (v instanceof Date) return Number.isNaN(v.getTime()) ? null : v.toISOString().replace('T', ' ').slice(0, 19)
  if (Buffer.isBuffer(v)) return '(nhị phân)'
  if (typeof v === 'string' && v.length > DU_LIEU_GAME_CAT_CHUOI) return `${v.slice(0, DU_LIEU_GAME_CAT_CHUOI)}…`
  return v
}

/** Bản đồ: danh sách tĩnh trong cẩm nang + đếm nhiệm vụ đặt ở map + nhân vật đang lưu vị trí ở map. */
async function docBanDo(tim: string): Promise<KetQuaBoDuLieu> {
  const nv = new Map<string, number>()
  const nvat = new Map<string, number>()
  if (await coBang(dbGame(), 'data_nhiem_vu_noi_dung')) {
    for (const r of await q(dbGame(), `SELECT map_id, COUNT(*) AS n FROM data_nhiem_vu_noi_dung WHERE map_id <> '' GROUP BY map_id`)) nv.set(String(r.map_id), Number(r.n))
  }
  if (await coBang(dbGame(), 'tob_char')) {
    for (const r of await q(dbGame(), 'SELECT `map` AS m, COUNT(*) AS n FROM tob_char GROUP BY `map`')) nvat.set(String(r.m), Number(r.n))
  }
  const t = tim.trim().toLowerCase()
  const dong = CAM_NANG.banDo
    .map(b => ({ id: b.id, ten: b.ten, loai: b.loai, vung: b.vung, soNhiemVu: nv.get(String(b.id)) ?? 0, soNhanVat: nvat.get(String(b.id)) ?? 0, anh: String(!!b.anh) }))
    .filter(b => !t || `${b.id} ${b.ten} ${b.loai} ${b.vung}`.toLowerCase().includes(t))
  return { ma: 'ban-do', coBang: true, tong: dong.length, dong, catBot: false }
}

export async function docBoDuLieu(b: BoDuLieu, tim: string, gioiHan: number, boQua: number): Promise<KetQuaBoDuLieu> {
  if (b.loai === 'ban_do') return docBanDo(tim)
  if (!(await coBang(dbGame(), b.bang!))) return { ma: b.ma, coBang: false, tong: 0, dong: [], catBot: false }
  const s = dungSqlBoDuLieu(b, tim, gioiHan, boQua)
  const [dong, tong] = await Promise.all([
    q(dbGame(), s.sql, s.thamSo),
    qv<number>(dbGame(), s.sqlDem, s.thamSoDem),
  ])
  const ra = dong.map(r => Object.fromEntries(b.cot.map(c => [c.key, catGiaTri(r[c.key])])))
  return { ma: b.ma, coBang: true, tong: Number(tong ?? ra.length), dong: ra, catBot: Number(tong ?? 0) > ra.length + Math.max(0, boQua) }
}

let tongQuanCache: { luc: number, ds: { ma: string, tab: string, ten: string, moTa: string, bang: string | null, coBang: boolean, soDong: number }[] } | null = null

/** Số dòng từng bộ dữ liệu (nhớ 60 giây — dữ liệu tĩnh, chỉ đổi khi cập nhật game). */
export async function tongQuanDuLieuGame() {
  if (tongQuanCache && Date.now() - tongQuanCache.luc < 60_000) return tongQuanCache.ds
  const ds = []
  for (const b of DS_DU_LIEU_GAME) {
    if (b.loai === 'ban_do') {
      ds.push({ ma: b.ma, tab: b.tab, ten: b.ten, moTa: b.moTa, bang: null, coBang: true, soDong: CAM_NANG.banDo.length })
      continue
    }
    const co = await coBang(dbGame(), b.bang!)
    let soDong = 0
    if (co) {
      try { soDong = Number(await qv(dbGame(), `SELECT COUNT(*) FROM \`${b.bang}\``) ?? 0) } catch { soDong = 0 }
    }
    ds.push({ ma: b.ma, tab: b.tab, ten: b.ten, moTa: b.moTa, bang: b.bang!, coBang: co, soDong })
  }
  tongQuanCache = { luc: Date.now(), ds }
  return ds
}
