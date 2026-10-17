/**
 * Tin tức: bài trong CSDL (web_tin_tuc, đã xuất bản và tới giờ đăng) + bài tĩnh trong
 * shared/utils/news.ts (bài tĩnh trùng slug với bài CSDL thì bài CSDL thắng).
 */
import { CMS_MAC_DINH, KHOA_CMS, hopNhatCms, lienKetAnToan, tronSau } from '../../shared/utils/cmsMacDinh'
import type { CmsCongKhai, KhoaCms } from '../../shared/utils/cmsMacDinh'

import type { NewsArticle } from '../../shared/utils/news'

export interface TinTomTat {
  slug: string
  tieuDe: string
  tomTat: string
  anhBia: string
  loai: string
  the: string[]
  ngay: string
  ghim: boolean
  nguon: 'db' | 'tinh'
}
export interface TinChiTiet extends TinTomTat {
  html: string
  muc: { id: string, ten: string }[]
  /** Bài tĩnh: giữ cấu trúc cũ để trang hiển thị như trước. */
  tinh?: NewsArticle
}

interface DongTin { slug: string, tieu_de: string, tom_tat: string, anh_bia: string, loai: string, the: string, noi_dung?: string, dang_luc: string | null, ghim: number }

const ngayVn = (s: string | null) => {
  const m = (s ?? '').match(/^(\d{4})-(\d{2})-(\d{2})/)
  return m ? `${m[3]}/${m[2]}/${m[1]}` : ''
}
const sangTomTat = (r: DongTin): TinTomTat => ({
  slug: r.slug, tieuDe: r.tieu_de, tomTat: r.tom_tat || markdownSangChu(r.noi_dung, 200), anhBia: lienKetAnToan(r.anh_bia), loai: r.loai,
  the: r.the.split(',').map(s => s.trim()).filter(Boolean), ngay: ngayVn(r.dang_luc), ghim: Number(r.ghim) === 1, nguon: 'db',
})

export async function dsTinCongKhai(): Promise<TinTomTat[]> {
  return quaDem('tin-tuc', 15, async () => {
    let db: TinTomTat[] = []
    if (await coBangCms('web_tin_tuc')) {
      try {
        const rows = await q<DongTin>(dbGame(),
          `SELECT slug, tieu_de, tom_tat, anh_bia, loai, the, noi_dung, dang_luc, ghim FROM web_tin_tuc
            WHERE trang_thai = 1 AND (dang_luc IS NULL OR dang_luc <= NOW())
            ORDER BY ghim DESC, dang_luc DESC, id DESC LIMIT 200`)
        db = rows.map(sangTomTat)
      }
      catch { db = [] }
    }
    const daCo = new Set(db.map(t => t.slug))
    const tinh: TinTomTat[] = newsList().filter(([s]) => !daCo.has(s)).map(([slug, a]) => ({
      slug, tieuDe: a.title, tomTat: a.body, anhBia: '', loai: a.category, the: [], ngay: a.date, ghim: false, nguon: 'tinh' as const,
    }))
    return [...db, ...tinh]
  })
}

export async function tinCongKhai(slug: string): Promise<TinChiTiet | null> {
  if (await coBangCms('web_tin_tuc')) {
    try {
      const r = await q1<DongTin>(dbGame(),
        `SELECT slug, tieu_de, tom_tat, anh_bia, loai, the, noi_dung, dang_luc, ghim FROM web_tin_tuc
          WHERE slug = ? AND trang_thai = 1 AND (dang_luc IS NULL OR dang_luc <= NOW()) LIMIT 1`, [slug])
      if (r) return { ...sangTomTat(r), html: markdownSangHtml(r.noi_dung), muc: mucLucMarkdown(r.noi_dung) }
    }
    catch { /* rơi về bài tĩnh */ }
  }
  const a = newsBySlug(slug)
  if (!a) return null
  return { slug, tieuDe: a.title, tomTat: a.body, anhBia: '', loai: a.category, the: [], ngay: a.date, ghim: false, nguon: 'tinh', html: '', muc: [], tinh: a }
}
