/**
 * Menu khu quản trị, chia nhóm theo loại việc.
 * Mỗi trang admin nằm ở app/pages/admin/<key>.vue (riêng Tổng quan là /admin).
 */
export interface AdminMenuItem {
  key: string
  to: string
  label: string
  icon: string
}

export const ADMIN_MENU: { nhom: string, items: AdminMenuItem[] }[] = [
  {
    nhom: 'Theo dõi',
    items: [
      { key: 'tongquan', to: '/admin', label: 'Tổng quan', icon: 'i-lucide-layout-grid' },
      { key: 'online', to: '/admin/online', label: 'Ai đang ở đâu · client', icon: 'i-lucide-map-pin' },
    ],
  },
  {
    nhom: 'Người chơi',
    items: [
      { key: 'giam_level', to: '/admin/giam-level', label: 'Giảm Level', icon: 'i-lucide-user-minus' },
      { key: 'nang_level_toanbo', to: '/admin/nang-level-toanbo', label: 'Nâng Level toàn bộ', icon: 'i-lucide-users' },
      { key: 'taikhoan', to: '/admin/taikhoan', label: 'Tài khoản', icon: 'i-lucide-user-round' },
      { key: 'tao_hang_loat', to: '/admin/tao-hang-loat', label: 'Tạo hàng loạt', icon: 'i-lucide-users-round' },
      { key: 'nhanvat', to: '/admin/nhanvat', label: 'Pokémon', icon: 'i-lucide-paw-print' },
      { key: 'cammay', to: '/admin/cammay', label: 'Cấm theo máy', icon: 'i-lucide-lock' },
      { key: 'reset_toanbo', to: '/admin/reset-toanbo', label: 'Reset toàn server', icon: 'i-lucide-rotate-ccw' },
    ],
  },
  {
    nhom: 'Vận hành',
    items: [
      { key: 'sukien', to: '/admin/sukien', label: 'Sự kiện', icon: 'i-lucide-calendar-days' },
      { key: 'giftcode', to: '/admin/giftcode', label: 'Giftcode', icon: 'i-lucide-gift' },
      { key: 'dua_top', to: '/admin/dua-top', label: 'Đua top', icon: 'i-lucide-medal' },
      { key: 'cauhinh', to: '/admin/cauhinh', label: 'Cấu hình server', icon: 'i-lucide-sliders-horizontal' },
      { key: 'server', to: '/admin/server', label: 'Bật/tắt server', icon: 'i-lucide-power' },
      { key: 'naptien', to: '/admin/naptien', label: 'Đơn nạp mốc', icon: 'i-lucide-banknote' },
    ],
  },
  {
    nhom: 'Ghi chép',
    items: [
      { key: 'log', to: '/admin/log', label: 'Log game', icon: 'i-lucide-list' },
      { key: 'chandoan', to: '/admin/chandoan', label: 'Chẩn đoán', icon: 'i-lucide-activity' },
      { key: 'nhatky', to: '/admin/nhatky', label: 'Nhật ký admin', icon: 'i-lucide-clock' },
    ],
  },
]
