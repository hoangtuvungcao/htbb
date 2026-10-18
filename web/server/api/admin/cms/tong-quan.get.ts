/** Số liệu nhanh cho bảng điều khiển: nội dung, người chơi mới, sự kiện tuần này, trạng thái máy chủ. */
export default defineAdminHandler(async () => {
  const dem = async (bang: typeof BANG_CMS[number], where = '') => (await coBangCms(bang)) ? Number(await qv(dbGame(), `SELECT COUNT(*) FROM ${bang} ${where}`)) : null
  let tkMoi: { ngay: string, so: number }[] = []
  try {
    tkMoi = await q(dbAccount(), `SELECT DATE(regdate) AS ngay, COUNT(*) AS so FROM team_user WHERE regdate >= DATE_SUB(CURDATE(), INTERVAL 13 DAY) GROUP BY DATE(regdate) ORDER BY ngay`)
  }
  catch { tkMoi = [] }
  let online: number | null = null
  try { online = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tob_vi_tri_online')) }
  catch { online = null }
  const tinMoi = (await coBangCms('web_tin_tuc')) ? await q(dbGame(), 'SELECT id, slug, tieu_de, trang_thai, dang_luc, sua_luc FROM web_tin_tuc ORDER BY sua_luc DESC LIMIT 5') : []
  return {
    noiDung: { tin: await dem('web_tin_tuc'), tinNhap: await dem('web_tin_tuc', 'WHERE trang_thai = 0'), trang: await dem('web_trang'), faq: await dem('web_faq'), anh: await dem('web_media'), lich: await dem('web_su_kien_lich') },
    tkMoi: tkMoi.map(r => ({ ngay: String(r.ngay).slice(0, 10), so: Number(r.so) })),
    online,
    tinMoi,
    coBangCms: await coBangCms(),
  }
})
