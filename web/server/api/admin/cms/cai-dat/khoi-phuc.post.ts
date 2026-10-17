/** Xoá khối cài đặt khỏi CSDL -> trang quay về nội dung mặc định. Body { khoa }. */
import { CMS_MAC_DINH, KHOA_CMS, hopNhatCms, lienKetAnToan, tronSau } from '../../../../../shared/utils/cmsMacDinh'
import type { CmsCongKhai, KhoaCms } from '../../../../../shared/utils/cmsMacDinh'

export default defineAdminHandler(async (event) => {
  await canBangCms('web_cai_dat')
  const khoa = str((await bodyObj(event)).khoa)
  if (!(KHOA_CMS as readonly string[]).includes(khoa)) throw apiLoi(400, 'Khối cài đặt không hợp lệ.')
  const cu = await qv<string>(dbGame(), 'SELECT gia_tri FROM web_cai_dat WHERE khoa = ? LIMIT 1', [khoa])
  await exec(dbGame(), 'DELETE FROM web_cai_dat WHERE khoa = ?', [khoa])
  xoaDemCms()
  await ghiThayDoi(event, 'cms_khoi_phuc', `cai_dat:${khoa}`, cu ?? '', '(mặc định)')
  return { ok: true, giaTri: CMS_MAC_DINH[khoa as KhoaCms] }
})
