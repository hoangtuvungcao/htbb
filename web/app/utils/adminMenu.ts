/**
 * Menu khu quản trị, chia nhóm theo loại việc.
 * Mỗi trang admin nằm ở app/pages/admin/<key>.vue (riêng Tổng quan là /admin).
 */
export type VaiTroAdmin = 'sieu_quan_tri' | 'bien_tap' | 'ho_tro' | 'xem'
export interface AdminMenuItem {
  key: string
  to: string
  label: string
  icon: string
  /** Vai trò THẤY mục này (bỏ trống = mọi vai trò). Quyền thật kiểm ở server (server/utils/vaiTro.ts). */
  vai?: VaiTroAdmin[]
}
const SIEU: VaiTroAdmin[] = ['sieu_quan_tri']
const NOI_DUNG: VaiTroAdmin[] = ['sieu_quan_tri', 'bien_tap', 'xem']
const HO_TRO: VaiTroAdmin[] = ['sieu_quan_tri', 'ho_tro', 'xem']

export const TEN_VAI_TRO_WEB: Record<VaiTroAdmin, string> = {
  sieu_quan_tri: 'Siêu quản trị', bien_tap: 'Biên tập', ho_tro: 'Hỗ trợ', xem: 'Chỉ xem',
}

export const ADMIN_MENU: { nhom: string, items: AdminMenuItem[] }[] = [
  {
    nhom: 'Theo dõi',
    items: [
      { key: 'tongquan', to: '/admin', label: 'Tổng quan', icon: 'i-lucide-layout-grid' },
      { key: 'online', to: '/admin/online', label: 'Ai đang ở đâu · client', icon: 'i-lucide-map-pin' },
      { key: 'bang_game', to: '/admin/bang-game', label: 'Kết quả trong game', icon: 'i-lucide-castle' },
    ],
  },
  {
    nhom: 'Nội dung website',
    items: [
      { key: 'tin_tuc', to: '/admin/tin-tuc', label: 'Tin tức & sự kiện', icon: 'i-lucide-newspaper', vai: NOI_DUNG },
      { key: 'trang', to: '/admin/trang', label: 'Trang cẩm nang', icon: 'i-lucide-library', vai: NOI_DUNG },
      { key: 'trang_chu', to: '/admin/trang-chu', label: 'Trang chủ', icon: 'i-lucide-house', vai: NOI_DUNG },
      { key: 'lich_su_kien', to: '/admin/lich-su-kien', label: 'Lịch sự kiện năm', icon: 'i-lucide-calendar-range', vai: NOI_DUNG },
      { key: 'faq', to: '/admin/faq', label: 'Câu hỏi thường gặp', icon: 'i-lucide-circle-help', vai: NOI_DUNG },
      { key: 'tai_game', to: '/admin/tai-game', label: 'Bản tải game', icon: 'i-lucide-download', vai: NOI_DUNG },
      { key: 'menu', to: '/admin/menu', label: 'Menu & chân trang', icon: 'i-lucide-panel-top', vai: NOI_DUNG },
      { key: 'media', to: '/admin/media', label: 'Thư viện ảnh', icon: 'i-lucide-image', vai: NOI_DUNG },
      { key: 'cai_dat', to: '/admin/cai-dat', label: 'Cài đặt website', icon: 'i-lucide-settings', vai: NOI_DUNG },
    ],
  },
  {
    nhom: 'Người chơi',
    items: [
      { key: 'nguoi_choi', to: '/admin/nguoi-choi', label: 'Trung tâm người chơi', icon: 'i-lucide-contact', vai: HO_TRO },
      { key: 'taikhoan', to: '/admin/taikhoan', label: 'Tài khoản', icon: 'i-lucide-user-round', vai: HO_TRO },
      { key: 'nhanvat', to: '/admin/nhanvat', label: 'Pokémon', icon: 'i-lucide-paw-print', vai: HO_TRO },
      { key: 'cammay', to: '/admin/cammay', label: 'Cấm theo máy', icon: 'i-lucide-lock', vai: HO_TRO },
      { key: 'giam_level', to: '/admin/giam-level', label: 'Giảm Level', icon: 'i-lucide-user-minus', vai: SIEU },
      { key: 'nang_level_toanbo', to: '/admin/nang-level-toanbo', label: 'Nâng Level toàn bộ', icon: 'i-lucide-users', vai: SIEU },
      { key: 'tao_hang_loat', to: '/admin/tao-hang-loat', label: 'Tạo hàng loạt', icon: 'i-lucide-users-round', vai: SIEU },
      { key: 'reset_toanbo', to: '/admin/reset-toanbo', label: 'Reset toàn server', icon: 'i-lucide-rotate-ccw', vai: SIEU },
    ],
  },
  {
    nhom: 'Vận hành',
    items: [
      { vai: SIEU, key: 'sukien', to: '/admin/sukien', label: 'Sự kiện', icon: 'i-lucide-calendar-days' },
      { vai: SIEU, key: 'giftcode', to: '/admin/giftcode', label: 'Giftcode', icon: 'i-lucide-gift' },
      { vai: SIEU, key: 'dua_top', to: '/admin/dua-top', label: 'Đua top', icon: 'i-lucide-medal' },
      { vai: SIEU, key: 'cauhinh', to: '/admin/cauhinh', label: 'Cấu hình server', icon: 'i-lucide-sliders-horizontal' },
      { vai: SIEU, key: 'server', to: '/admin/server', label: 'Bật/tắt server', icon: 'i-lucide-power' },
      { vai: SIEU, key: 'naptien', to: '/admin/naptien', label: 'Đơn nạp mốc', icon: 'i-lucide-banknote' },
    ],
  },
  {
    nhom: 'Ghi chép',
    items: [
      { key: 'log', to: '/admin/log', label: 'Log game', icon: 'i-lucide-list' },
      { key: 'chandoan', to: '/admin/chandoan', label: 'Chẩn đoán', icon: 'i-lucide-activity' },
      { key: 'nhatky', to: '/admin/nhatky', label: 'Nhật ký admin', icon: 'i-lucide-clock' },
      { key: 'thay_doi', to: '/admin/lich-su-thay-doi', label: 'Lịch sử thay đổi', icon: 'i-lucide-history' },
      { key: 'phan_quyen', to: '/admin/phan-quyen', label: 'Phân quyền', icon: 'i-lucide-shield-check', vai: SIEU },
      { key: 'dong_bo', to: '/admin/dong-bo', label: 'Đồng bộ dữ liệu game', icon: 'i-lucide-refresh-cw', vai: SIEU },
    ],
  },
]
