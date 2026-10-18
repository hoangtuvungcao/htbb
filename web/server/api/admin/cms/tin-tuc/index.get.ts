/** Danh sách bài (mọi trạng thái). ?q= tìm theo tiêu đề/slug. */
export default defineAdminHandler(async (event) => {
  if (!(await coBangCms('web_tin_tuc'))) return { coBang: false, ds: [], tinh: newsList().map(([slug, a]) => ({ slug, tieuDe: a.title, loai: a.category, ngay: a.date })) }
  const tu = str(getQuery(event).q)
  const ds = await q(dbGame(),
    `SELECT id, slug, tieu_de, loai, trang_thai, dang_luc, ghim, tac_gia, sua_luc FROM web_tin_tuc
      ${tu ? 'WHERE tieu_de LIKE ? OR slug LIKE ?' : ''} ORDER BY sua_luc DESC, id DESC LIMIT 500`,
    tu ? [`%${tu}%`, `%${tu}%`] : [])
  return { coBang: true, ds, tinh: newsList().map(([slug, a]) => ({ slug, tieuDe: a.title, loai: a.category, ngay: a.date })) }
})
