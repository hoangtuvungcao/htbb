/** Thành viên HTBB (admin): cấu hình hiện tại, bán được không (và vì sao), thống kê, sổ đăng ký tính năng. */
import { TV_CHUC_NANG } from '../../../utils/thanhVienThuan'

export default defineAdminHandler(async () => {
  const coBangTv = await tvCoBang()
  const cauHinh = await tvCauHinh(true)
  const ban = await tvBanDuoc().catch(() => ({ duoc: false, lyDo: ['lỗi đọc cấu hình'] }))
  return {
    coBang: coBangTv,
    cauHinh,
    ban,
    chucNang: TV_CHUC_NANG,
    thongKe: coBangTv ? await tvThongKe() : null,
    daCoGiayPhep: useHtbbConfig().nap.daCoGiayPhep,
    demo: useHtbbConfig().nap.demo,
    demoChiAdmin: useHtbbConfig().nap.demoChiAdmin,
  }
})
