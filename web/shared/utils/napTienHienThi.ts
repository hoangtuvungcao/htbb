/**
 * Nhãn hiển thị của hệ thống nạp tiền (dùng chung trang /naptien, /xac-nhan-nap, /admin/naptien).
 * Không chứa bí mật / luật — luật nằm ở server/utils/napTienThuan.ts (server) và
 * HTBB/src/com/htbb/pokemon/NapTienLogic.java (game).
 */
export const TEN_TRANG_THAI_NAP: Record<string, string> = {
  cho_thanh_toan: 'Chờ thanh toán',
  da_thanh_toan: 'Đã thanh toán — chờ vào game',
  da_cong: 'Đã cộng vào Pokémon',
  that_bai: 'Hết hạn / thất bại',
  hoan_tien: 'Đã hoàn tiền',
  cho_thu_hoi: 'Hoàn tiền — chờ thu hồi',
  da_thu_hoi: 'Hoàn tiền — đã thu hồi',
}

export const MAU_TRANG_THAI_NAP: Record<string, 'warning' | 'success' | 'neutral' | 'info' | 'error'> = {
  cho_thanh_toan: 'warning',
  da_thanh_toan: 'info',
  da_cong: 'success',
  that_bai: 'neutral',
  hoan_tien: 'error',
  cho_thu_hoi: 'error',
  da_thu_hoi: 'neutral',
}

export const TEN_LOAI_GOI: Record<string, string> = {
  luong: 'Gói lượng',
  the_thang: 'Thẻ tháng',
  ve_mua: 'Vé mùa',
  thanh_vien: 'Thành viên HTBB',
}
