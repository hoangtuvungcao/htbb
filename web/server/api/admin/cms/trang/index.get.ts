/** Trang trong CSDL + danh sách trang có sẵn (để ghi đè). */
export default defineAdminHandler(async () => {
  const coSan = DS_CAM_NANG.map(p => ({ slug: p.slug, ten: p.ten, nhom: p.nhom, icon: p.icon, moTa: p.moTa }))
  if (!(await coBangCms('web_trang'))) return { coBang: false, ds: [], coSan }
  const ds = await q(dbGame(), 'SELECT id, slug, tieu_de, nhom, icon, thu_tu, trang_thai, tac_gia, sua_luc FROM web_trang ORDER BY thu_tu, id')
  return { coBang: true, ds, coSan }
})
