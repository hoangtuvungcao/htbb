/** Mọi khối cài đặt: giá trị đang dùng (đã trộn mặc định) + khối nào đang lấy từ CSDL. */
import { CMS_MAC_DINH, KHOA_CMS, hopNhatCms, lienKetAnToan, tronSau } from '../../../../../shared/utils/cmsMacDinh'
import type { CmsCongKhai, KhoaCms } from '../../../../../shared/utils/cmsMacDinh'

export default defineAdminHandler(async () => {
  const coBangCaiDat = await coBangCms('web_cai_dat')
  const db = await docCaiDatDb()
  return {
    coBang: coBangCaiDat,
    giaTri: hopNhatCms(db),
    macDinh: CMS_MAC_DINH,
    tuDb: Object.fromEntries(KHOA_CMS.map(k => [k, db[k] !== undefined])),
  }
})
