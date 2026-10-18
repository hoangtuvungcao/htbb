/**
 * Lưu một khối cài đặt. Body { khoa: 'site'|'trangChu'|'menu'|'taiGame', giaTri }.
 * Giá trị được "nắn" theo khuôn mặc định (tronSau): khoá lạ bị bỏ, sai kiểu thì giữ mặc định —
 * nên dữ liệu hỏng không làm vỡ trang công khai.
 */
import { CMS_MAC_DINH, KHOA_CMS, hopNhatCms, lienKetAnToan, tronSau } from '../../../../../shared/utils/cmsMacDinh'
import type { CmsCongKhai, KhoaCms } from '../../../../../shared/utils/cmsMacDinh'

export default defineAdminHandler(async (event, admin) => {
  await canBangCms('web_cai_dat')
  const b = await bodyObj(event)
  const khoa = str(b.khoa) as KhoaCms
  if (!(KHOA_CMS as readonly string[]).includes(khoa)) throw apiLoi(400, 'Khối cài đặt không hợp lệ.')
  const moi = tronSau(CMS_MAC_DINH[khoa], b.giaTri)
  const json = JSON.stringify(moi)
  if (json.length > 200_000) throw apiLoi(400, 'Nội dung quá lớn (tối đa 200 KB).')
  const cu = await qv<string>(dbGame(), 'SELECT gia_tri FROM web_cai_dat WHERE khoa = ? LIMIT 1', [khoa])
  await exec(dbGame(),
    `INSERT INTO web_cai_dat (khoa, gia_tri, cap_nhat_luc, cap_nhat_boi) VALUES (?, ?, NOW(), ?)
     ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), cap_nhat_luc = VALUES(cap_nhat_luc), cap_nhat_boi = VALUES(cap_nhat_boi)`,
    [khoa, json, admin.username])
  xoaDemCms()
  await ghiThayDoi(event, 'cms_cai_dat', `cai_dat:${khoa}`, cu ?? '(mặc định)', json)
  return { ok: true, giaTri: moi }
})
