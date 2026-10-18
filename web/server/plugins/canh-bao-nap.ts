/**
 * Cảnh báo lúc khởi động khi BẬT nạp tiền (KPAH_NAP_BAT=true) mà chủ server chưa xác nhận đã có
 * giấy phép hình ảnh/IP (NAP_DA_CO_GIAY_PHEP=1). Không chặn — trang /admin/naptien cũng hiện
 * cảnh báo đỏ. Xem HTBB/docs/pokemon/NAP_TIEN.md mục 9.
 */
export default defineNitroPlugin(() => {
  const n = useHtbbConfig().nap
  if (n.bat && !n.daCoGiayPhep) {
    console.warn('[htbb] CANH BAO: KPAH_NAP_BAT=true nhung CHUA dat NAP_DA_CO_GIAY_PHEP=1. Thu tien khi chua co giay phep hinh anh/IP'
      + ' (asset CC BY-NC, ten Pokemon cua Nintendo) la VI PHAM. Xem HTBB/docs/pokemon/NAP_TIEN.md muc 9.')
  }
  if (n.bat && n.sandbox) {
    console.warn('[htbb] CANH BAO: KPAH_NAP_SANDBOX=1 dang bat cung luc voi nap that - tat sandbox tren may that.')
  }
})
