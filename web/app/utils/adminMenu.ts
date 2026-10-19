/**
 * Menu khu quản trị — MỘT cấu trúc thông tin, chia nhóm theo loại việc.
 * Mỗi trang admin nằm ở app/pages/admin/<...>.vue (riêng Tổng quan là /admin).
 *
 * Ai THẤY mục nào: suy ra từ ma trận quyền (shared/utils/quyenAdmin.ts) qua `api` = API chính
 * mà trang đó đọc. Vậy menu không bao giờ hiện trang mà vai trò đó gọi API bị 403.
 * Quyền thật vẫn kiểm ở server cho mọi /api/admin/**.
 */
export type VaiTroAdmin = VaiTroQuyen
export interface AdminMenuItem {
  key: string
  to: string
  label: string
  icon: string
  /** API chính của trang — quyết định vai trò nào thấy mục này. */
  api: string
  /** Mô tả ngắn (tooltip / trang tổng quan). */
  moTa?: string
}

export const TEN_VAI_TRO_WEB: Record<VaiTroAdmin, string> = {
  sieu_quan_tri: 'Siêu quản trị', bien_tap: 'Biên tập', ho_tro: 'Hỗ trợ', xem: 'Chỉ xem',
}

export const ADMIN_MENU: { nhom: string, items: AdminMenuItem[] }[] = [
  {
    nhom: 'Tổng quan',
    items: [
      { key: 'tongquan', to: '/admin', label: 'Bảng điều khiển', icon: 'i-lucide-layout-grid', api: '/api/admin/tong-quan' },
      { key: 'online', to: '/admin/online', label: 'Đang online', icon: 'i-lucide-map-pin', api: '/api/admin/online', moTa: 'Ai đang ở bản đồ nào, client nào' },
    ],
  },
  {
    nhom: 'Người chơi',
    items: [
      { key: 'nguoi_choi', to: '/admin/nguoi-choi', label: 'Trung tâm người chơi', icon: 'i-lucide-contact', api: '/api/admin/nguoi-choi/tim', moTa: 'Tìm theo tài khoản, Pokémon, IP, mã máy; hồ sơ đầy đủ và thao tác' },
      { key: 'taikhoan', to: '/admin/taikhoan', label: 'Tài khoản', icon: 'i-lucide-user-round', api: '/api/admin/taikhoan' },
      { key: 'nhanvat', to: '/admin/nhanvat', label: 'Pokémon', icon: 'i-lucide-paw-print', api: '/api/admin/nhanvat' },
      { key: 'cammay', to: '/admin/cammay', label: 'Cấm theo máy', icon: 'i-lucide-lock', api: '/api/admin/cammay' },
    ],
  },
  {
    nhom: 'Trò chơi & kinh tế',
    items: [
      { key: 'kinh_te', to: '/admin/kinh-te', label: 'Kinh tế', icon: 'i-lucide-chart-line', api: '/api/admin/kinh-te/tong-quan', moTa: 'Nguồn vào/ra, lạm phát, người giữ nhiều, cảnh báo' },
      { key: 'du_lieu_game', to: '/admin/du-lieu-game', label: 'Dữ liệu game', icon: 'i-lucide-database', api: '/api/admin/du-lieu-game/tong-quan', moTa: 'Vật phẩm, quái, bản đồ, nhiệm vụ, cửa hàng, rơi đồ (chỉ đọc)' },
      { key: 'ten_hien', to: '/admin/ten-hien', label: 'Tên NPC & bản đồ', icon: 'i-lucide-map-pin', api: '/api/admin/ten-hien', moTa: 'Sửa tên NPC và tên bản đồ người chơi thấy (game tự nạp trong 30 giây)' },
      { key: 'bang_game', to: '/admin/bang-game', label: 'Kết quả trong game', icon: 'i-lucide-castle', api: '/api/admin/bang-game' },
      { key: 'xep_hang', to: '/admin/xep-hang', label: 'Bảng xếp hạng web', icon: 'i-lucide-trophy', api: '/api/admin/xep-hang', moTa: 'Bảng nào hiện, thứ tự, làm mới, loại trừ' },
      { key: 'dua_top', to: '/admin/dua-top', label: 'Đua top', icon: 'i-lucide-medal', api: '/api/admin/dua-top' },
    ],
  },
  {
    nhom: 'Sự kiện & giftcode',
    items: [
      { key: 'sukien', to: '/admin/sukien', label: 'Sự kiện trong game', icon: 'i-lucide-calendar-days', api: '/api/admin/sukien' },
      { key: 'lich_su_kien', to: '/admin/lich-su-kien', label: 'Lịch sự kiện năm', icon: 'i-lucide-calendar-range', api: '/api/admin/cms/lich' },
      { key: 'giftcode', to: '/admin/giftcode', label: 'Giftcode', icon: 'i-lucide-gift', api: '/api/admin/giftcode-moi' },
    ],
  },
  {
    nhom: 'Nạp tiền / Thành viên',
    items: [
      { key: 'naptien', to: '/admin/naptien', label: 'Nạp tiền', icon: 'i-lucide-banknote', api: '/api/admin/naptien/tong-quan' },
      { key: 'thanh_vien', to: '/admin/thanh-vien', label: 'Thành viên HTBB', icon: 'i-lucide-badge-check', api: '/api/admin/thanh-vien/tong-quan', moTa: 'Cấp/thu hồi, danh sách, tín hiệu clone' },
    ],
  },
  {
    nhom: 'Nội dung web',
    items: [
      { key: 'tin_tuc', to: '/admin/tin-tuc', label: 'Tin tức', icon: 'i-lucide-newspaper', api: '/api/admin/cms/tin-tuc' },
      { key: 'trang', to: '/admin/trang', label: 'Trang cẩm nang', icon: 'i-lucide-library', api: '/api/admin/cms/trang' },
      { key: 'trang_chu', to: '/admin/trang-chu', label: 'Trang chủ', icon: 'i-lucide-house', api: '/api/admin/cms/cai-dat' },
      { key: 'faq', to: '/admin/faq', label: 'Câu hỏi thường gặp', icon: 'i-lucide-circle-help', api: '/api/admin/cms/faq' },
      { key: 'tai_game', to: '/admin/tai-game', label: 'Bản tải game', icon: 'i-lucide-download', api: '/api/admin/cms/cai-dat' },
      { key: 'menu', to: '/admin/menu', label: 'Menu & chân trang', icon: 'i-lucide-panel-top', api: '/api/admin/cms/cai-dat' },
      { key: 'media', to: '/admin/media', label: 'Thư viện ảnh', icon: 'i-lucide-image', api: '/api/admin/cms/media' },
      { key: 'cai_dat', to: '/admin/cai-dat', label: 'Cài đặt website', icon: 'i-lucide-settings', api: '/api/admin/cms/cai-dat' },
    ],
  },
  {
    nhom: 'Hệ thống',
    items: [
      { key: 'cau_hinh_he_thong', to: '/admin/cau-hinh-he-thong', label: 'Cấu hình', icon: 'i-lucide-settings-2', api: '/api/admin/cau-hinh-he-thong', moTa: 'Các thông số vận hành chỉnh được (tỉ lệ rơi, sự kiện, bảo trì, thành viên…); phần cố định đã ẩn' },
      { key: 'he_thong', to: '/admin/he-thong', label: 'Máy chủ & bảo trì', icon: 'i-lucide-server', api: '/api/admin/he-thong/bao-tri', moTa: 'Biểu đồ chỉ số, bảo trì đăng nhập, bật/tắt/khởi động lại máy chủ' },
      { key: 'chandoan', to: '/admin/chandoan', label: 'Chẩn đoán', icon: 'i-lucide-activity', api: '/api/admin/chandoan' },
      { key: 'vai_tro', to: '/admin/vai-tro', label: 'Phân quyền', icon: 'i-lucide-shield-check', api: '/api/admin/auth/me', moTa: 'Cấp vai trò quản trị, ma trận quyền' },
    ],
  },
  {
    nhom: 'Nhật ký',
    items: [
      { key: 'nhatky', to: '/admin/nhatky', label: 'Nhật ký quản trị', icon: 'i-lucide-clock', api: '/api/admin/nhatky' },
      { key: 'thay_doi', to: '/admin/lich-su-thay-doi', label: 'Lịch sử thay đổi', icon: 'i-lucide-history', api: '/api/admin/cms/thay-doi' },
      { key: 'log', to: '/admin/log', label: 'Log game', icon: 'i-lucide-list', api: '/api/admin/log' },
    ],
  },
  {
    nhom: 'Thao tác hàng loạt (cẩn thận)',
    items: [
      { key: 'giam_level', to: '/admin/giam-level', label: 'Giảm cấp', icon: 'i-lucide-user-minus', api: '/api/admin/giam-level' },
      { key: 'nang_level_toanbo', to: '/admin/nang-level-toanbo', label: 'Nâng cấp toàn bộ', icon: 'i-lucide-users', api: '/api/admin/nang-level-toanbo' },
      { key: 'tao_hang_loat', to: '/admin/tao-hang-loat', label: 'Tạo hàng loạt', icon: 'i-lucide-users-round', api: '/api/admin/tao-hang-loat' },
      { key: 'reset_toanbo', to: '/admin/reset-toanbo', label: 'Reset toàn máy chủ', icon: 'i-lucide-rotate-ccw', api: '/api/admin/reset-toanbo' },
    ],
  },
]

/** Mục menu ứng với một đường dẫn trang (khớp dài nhất). */
export function mucMenuCua(path: string): { nhom: string, item: AdminMenuItem } | null {
  let tot: { nhom: string, item: AdminMenuItem } | null = null
  for (const g of ADMIN_MENU) {
    for (const i of g.items) {
      const khop = i.to === '/admin' ? path === '/admin' : (path === i.to || path.startsWith(`${i.to}/`))
      if (khop && (!tot || i.to.length > tot.item.to.length)) tot = { nhom: g.nhom, item: i }
    }
  }
  return tot
}

/** Vai trò có thấy mục này không (= đọc được API chính của trang). */
export function thayMucMenu(i: AdminMenuItem, vt: VaiTroAdmin): boolean {
  return kiemQuyenAdmin(i.api, 'GET', vt)
}
