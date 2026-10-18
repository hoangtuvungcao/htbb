/** Lịch sử thay đổi nội dung (trước/sau). ?doiTuong= lọc, ?id= xem chi tiết một dòng. */
export default defineAdminHandler(async (event) => {
  if (!(await coBangCms('web_admin_thay_doi'))) return { coBang: false, ds: [] }
  const qr = getQuery(event)
  const id = int(qr.id)
  if (id > 0) return { coBang: true, dong: await q1(dbGame(), 'SELECT * FROM web_admin_thay_doi WHERE id = ? LIMIT 1', [id]) }
  const dt = str(qr.doiTuong)
  const ds = await q(dbGame(),
    `SELECT id, admin_user, hanh_dong, doi_tuong, ip, thoi_gian FROM web_admin_thay_doi ${dt ? 'WHERE doi_tuong LIKE ?' : ''} ORDER BY id DESC LIMIT 300`,
    dt ? [`%${dt}%`] : [])
  return { coBang: true, ds }
})
