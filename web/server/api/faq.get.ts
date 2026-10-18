/** FAQ trong CSDL (đang hiện), đã chuyển HTML an toàn. null = chưa có bảng/chưa nhập -> trang dùng FAQ tĩnh. */
export default defineEventHandler(async () => quaDem('faq', 15, async () => {
  if (!(await coBangCms('web_faq'))) return null
  try {
    const rows = await q<{ id: number, nhom: string, cau_hoi: string, tra_loi: string }>(dbGame(),
      'SELECT id, nhom, cau_hoi, tra_loi FROM web_faq WHERE hien = 1 ORDER BY thu_tu, id')
    if (!rows.length) return null
    return rows.map(r => ({ id: Number(r.id), nhom: r.nhom, cauHoi: r.cau_hoi, html: markdownSangHtml(r.tra_loi) }))
  }
  catch { return null }
}))
