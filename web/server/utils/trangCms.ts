/**
 * Trang cẩm nang trong CSDL (web_trang): ghi đè trang có sẵn (tên, mô tả, thứ tự, ẩn, nội dung thêm)
 * hoặc trang mới /cam-nang/<slug>.
 */
export interface TrangDb {
  slug: string
  ten: string
  nhom: string
  icon: string
  moTa: string
  tuKhoa: string
  thuTu: number
  hien: boolean
  coSan: boolean
  muc: { id: string, ten: string }[]
}
interface Dong { slug: string, tieu_de: string, nhom: string, icon: string, mo_ta: string, tu_khoa: string, thu_tu: number, noi_dung: string, trang_thai: number }

const sang = (r: Dong): TrangDb => ({
  slug: r.slug, ten: r.tieu_de, nhom: r.nhom, icon: r.icon || 'i-lucide-file-text', moTa: r.mo_ta, tuKhoa: r.tu_khoa,
  thuTu: Number(r.thu_tu), hien: Number(r.trang_thai) === 1, coSan: !!trangCamNang(r.slug), muc: mucLucMarkdown(r.noi_dung),
})

export async function dsTrangDb(): Promise<TrangDb[]> {
  return quaDem('trang', 15, async () => {
    if (!(await coBangCms('web_trang'))) return []
    try {
      return (await q<Dong>(dbGame(), 'SELECT slug, tieu_de, nhom, icon, mo_ta, tu_khoa, thu_tu, noi_dung, trang_thai FROM web_trang ORDER BY thu_tu, id')).map(sang)
    }
    catch { return [] }
  })
}

export async function trangDb(slug: string): Promise<(TrangDb & { html: string }) | null> {
  if (!(await coBangCms('web_trang'))) return null
  try {
    const r = await q1<Dong>(dbGame(), 'SELECT slug, tieu_de, nhom, icon, mo_ta, tu_khoa, thu_tu, noi_dung, trang_thai FROM web_trang WHERE slug = ? LIMIT 1', [slug])
    if (!r || Number(r.trang_thai) !== 1) return null
    return { ...sang(r), html: markdownSangHtml(r.noi_dung) }
  }
  catch { return null }
}
