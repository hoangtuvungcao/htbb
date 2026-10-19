/**
 * MA TRẬN PHÂN QUYỀN KHU QUẢN TRỊ — nguồn DUY NHẤT.
 *
 * Dùng ở ba nơi, nên không bao giờ lệch nhau:
 *   - server/utils/vaiTro.ts  : defineAdminHandler gọi kiemQuyenAdmin() cho MỌI /api/admin/**.
 *   - app/pages/admin/vai-tro.vue : trang "Ma trận vai trò" vẽ thẳng từ QUY_TAC_QUYEN.
 *   - tests/quyen-admin.test.ts : liệt kê MỌI file trong server/api/admin và đòi mỗi route khớp
 *     đúng một quy tắc tường minh (không có quy tắc = test đỏ = không ai ngoài siêu quản trị gọi được).
 *
 * Luật:
 *   - Siêu quản trị: luôn được (kể cả route chưa có quy tắc — để không tự khoá mình).
 *   - Vai trò khác: route PHẢI khớp một quy tắc; không khớp = CẤM (mặc định đóng).
 *   - GET/HEAD xét `doc`; POST/PUT/PATCH/DELETE xét `ghi`.
 *   - Mẫu: '*' khớp đúng MỘT đoạn đường dẫn, '**' ở cuối khớp phần còn lại (kể cả rỗng).
 *     Quy tắc xét theo thứ tự — đặt quy tắc cụ thể TRƯỚC quy tắc chung.
 *
 * File thuần TypeScript, không import gì (Nuxt tự import ở cả app lẫn server; test import trực tiếp).
 */
export type VaiTroQuyen = 'sieu_quan_tri' | 'bien_tap' | 'ho_tro' | 'xem'

export const DS_VAI_TRO_QUYEN: VaiTroQuyen[] = ['sieu_quan_tri', 'bien_tap', 'ho_tro', 'xem']

export const TEN_VAI_TRO_QUYEN: Record<VaiTroQuyen, string> = {
  sieu_quan_tri: 'Siêu quản trị',
  bien_tap: 'Biên tập nội dung',
  ho_tro: 'Hỗ trợ người chơi',
  xem: 'Chỉ xem',
}

export const MO_TA_VAI_TRO_QUYEN: Record<VaiTroQuyen, string> = {
  sieu_quan_tri: 'Toàn quyền: máy chủ, cấu hình, giftcode, kinh tế, nạp tiền, thao tác hàng loạt, phân quyền.',
  bien_tap: 'Soạn và đăng nội dung website (tin tức, cẩm nang, trang chủ, FAQ, menu, ảnh, lịch sự kiện). Xem dữ liệu game để viết đúng số.',
  ho_tro: 'Chăm sóc người chơi: tra cứu, khoá/mở có hạn, cấm chat, gửi thư, đổi mật khẩu, đưa về điểm an toàn. Xem nhật ký để đối chiếu.',
  xem: 'Chỉ xem mọi bảng (trừ phân quyền, tệp chẩn đoán, nạp tiền); không sửa được gì.',
}

export type NhomQuyen =
  | 'Phiên đăng nhập' | 'Tổng quan' | 'Người chơi' | 'Nội dung web' | 'Trò chơi & kinh tế'
  | 'Sự kiện' | 'Giftcode' | 'Nạp tiền' | 'Hệ thống' | 'Nhật ký'

export interface QuyTacQuyen {
  /** Mẫu đường dẫn, bắt đầu bằng /api/admin/. */
  mau: string
  nhom: NhomQuyen
  /** Mô tả ngắn cho trang ma trận. */
  viec: string
  /** Vai trò được GET/HEAD. */
  doc: VaiTroQuyen[]
  /** Vai trò được POST/PUT/PATCH/DELETE. [] = chỉ siêu quản trị. */
  ghi: VaiTroQuyen[]
}

const TAT_CA: VaiTroQuyen[] = DS_VAI_TRO_QUYEN
const SIEU: VaiTroQuyen[] = ['sieu_quan_tri']
const SIEU_XEM: VaiTroQuyen[] = ['sieu_quan_tri', 'xem']
const HO_TRO: VaiTroQuyen[] = ['sieu_quan_tri', 'ho_tro']
const HO_TRO_XEM: VaiTroQuyen[] = ['sieu_quan_tri', 'ho_tro', 'xem']
const BIEN_TAP: VaiTroQuyen[] = ['sieu_quan_tri', 'bien_tap']
const KHONG_AI: VaiTroQuyen[] = []

/** Thứ tự quan trọng: cụ thể trước, chung sau. */
export const QUY_TAC_QUYEN: QuyTacQuyen[] = [
  // --- Phiên đăng nhập (ai đã đăng nhập cũng tự quản phiên của mình) ---
  { mau: '/api/admin/auth/**', nhom: 'Phiên đăng nhập', viec: 'Đăng nhập, xác minh 2 bước, thông tin phiên, đăng xuất', doc: TAT_CA, ghi: TAT_CA },

  // --- Tổng quan ---
  { mau: '/api/admin/tong-quan', nhom: 'Tổng quan', viec: 'Số liệu tổng quan', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/cms/tong-quan', nhom: 'Tổng quan', viec: 'Tổng quan nội dung', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/online', nhom: 'Tổng quan', viec: 'Ai đang online, ở bản đồ nào', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/client', nhom: 'Tổng quan', viec: 'Thống kê phiên bản client', doc: TAT_CA, ghi: KHONG_AI },

  // --- Người chơi ---
  { mau: '/api/admin/nhanvat/*/xoa', nhom: 'Người chơi', viec: 'Xoá vĩnh viễn một Pokémon', doc: HO_TRO_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/nhanvat/*/cap-do', nhom: 'Người chơi', viec: 'Đặt cấp độ Pokémon', doc: HO_TRO_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/nhanvat/**', nhom: 'Người chơi', viec: 'Xem Pokémon; khoá, gỡ vật phẩm lỗi, huỷ lệnh chờ', doc: HO_TRO_XEM, ghi: HO_TRO },
  { mau: '/api/admin/taikhoan/xoa-nhieu', nhom: 'Người chơi', viec: 'Xoá hàng loạt tài khoản', doc: HO_TRO_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/taikhoan/**', nhom: 'Người chơi', viec: 'Xem/sửa tài khoản (khoá, đổi mật khẩu, email)', doc: HO_TRO_XEM, ghi: HO_TRO },
  { mau: '/api/admin/nguoi-choi/tang-qua', nhom: 'Người chơi', viec: 'Tặng vật phẩm (qua danh sách trắng giftcode, có nhật ký)', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/nguoi-choi/**', nhom: 'Người chơi', viec: 'Trung tâm người chơi: tra cứu, hồ sơ, khoá/mở, cấm chat, thư, về điểm an toàn', doc: HO_TRO_XEM, ghi: HO_TRO },
  { mau: '/api/admin/cammay', nhom: 'Người chơi', viec: 'Cấm theo máy (mã thiết bị)', doc: HO_TRO_XEM, ghi: HO_TRO },
  { mau: '/api/admin/giam-level/**', nhom: 'Người chơi', viec: 'Giảm cấp hàng loạt', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/nang-level-toanbo/**', nhom: 'Người chơi', viec: 'Nâng cấp toàn bộ', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/tao-hang-loat/**', nhom: 'Người chơi', viec: 'Tạo tài khoản hàng loạt', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/reset-toanbo/**', nhom: 'Người chơi', viec: 'Reset toàn máy chủ', doc: SIEU_XEM, ghi: KHONG_AI },

  // --- Nội dung web ---
  { mau: '/api/admin/cms/**', nhom: 'Nội dung web', viec: 'Tin tức, trang cẩm nang, trang chủ, FAQ, menu, ảnh, lịch sự kiện, cài đặt website', doc: TAT_CA, ghi: BIEN_TAP },

  // --- Trò chơi & kinh tế ---
  { mau: '/api/admin/du-lieu-game/**', nhom: 'Trò chơi & kinh tế', viec: 'Tra cứu dữ liệu game (vật phẩm, quái, bản đồ, nhiệm vụ, cửa hàng, rơi đồ) — chỉ đọc', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/ten-hien/**', nhom: 'Trò chơi & kinh tế', viec: 'Tên NPC & tên bản đồ người chơi thấy (bảng data_npc / data_map_ten, có nhật ký)', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/bang-game/**', nhom: 'Trò chơi & kinh tế', viec: 'Kết quả trong game (đua top, chiếm Gym…)', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/kinh-te-ngay', nhom: 'Trò chơi & kinh tế', viec: 'Chỉ số kinh tế theo ngày', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/kinh-te/**', nhom: 'Trò chơi & kinh tế', viec: 'Bảng kinh tế: nguồn vào/ra, lạm phát, người giữ nhiều, cảnh báo', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/xep-hang/**', nhom: 'Trò chơi & kinh tế', viec: 'Bảng xếp hạng công khai: bảng nào hiện, thứ tự, làm mới, danh sách loại trừ, bảng ủng hộ', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/dua-top/**', nhom: 'Trò chơi & kinh tế', viec: 'Đợt đua top và phần thưởng', doc: TAT_CA, ghi: KHONG_AI },

  // --- Sự kiện ---
  { mau: '/api/admin/sukien/**', nhom: 'Sự kiện', viec: 'Bật/tắt và hẹn lịch sự kiện trong game', doc: TAT_CA, ghi: KHONG_AI },

  // --- Giftcode ---
  { mau: '/api/admin/giftcode-moi/**', nhom: 'Giftcode', viec: 'Giftcode an toàn (tob_giftcode)', doc: SIEU_XEM, ghi: KHONG_AI },

  // --- Nạp tiền ---
  { mau: '/api/admin/thanh-vien/**', nhom: 'Nạp tiền', viec: 'Thành viên HTBB: xem danh sách, nhật ký, tín hiệu nhiều tài khoản (Hỗ trợ); cấu hình, cấp/thu hồi (chỉ Siêu quản trị)', doc: HO_TRO, ghi: KHONG_AI },
  { mau: '/api/admin/naptien/**', nhom: 'Nạp tiền', viec: 'Gói nạp, đơn, xác nhận, hoàn tiền', doc: SIEU, ghi: KHONG_AI },

  // --- Hệ thống ---
  { mau: '/api/admin/cau-hinh-he-thong/**', nhom: 'Hệ thống', viec: 'Cấu hình hệ thống trung tâm (chế độ, kinh tế, nạp tiền, bảo trì…), lịch sử + hoàn tác, khởi động lại an toàn', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/server/**', nhom: 'Hệ thống', viec: 'Bật/tắt/khởi động lại máy chủ game', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/chi-so-may-chu', nhom: 'Hệ thống', viec: 'Chỉ số máy chủ (CPU, bộ nhớ, online)', doc: TAT_CA, ghi: KHONG_AI },
  { mau: '/api/admin/he-thong/**', nhom: 'Hệ thống', viec: 'Chế độ bảo trì đăng nhập, thông tin vận hành', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/chandoan/file', nhom: 'Hệ thống', viec: 'Tải tệp chẩn đoán', doc: SIEU, ghi: KHONG_AI },
  { mau: '/api/admin/chandoan/**', nhom: 'Hệ thống', viec: 'Chẩn đoán kết nối, cấu hình web', doc: SIEU_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/phan-quyen/**', nhom: 'Hệ thống', viec: 'Cấp/thu vai trò quản trị', doc: SIEU, ghi: KHONG_AI },

  // --- Nhật ký ---
  { mau: '/api/admin/nhatky/**', nhom: 'Nhật ký', viec: 'Nhật ký thao tác quản trị', doc: HO_TRO_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/log/**', nhom: 'Nhật ký', viec: 'Log game (giao dịch, vật phẩm, lượng)', doc: HO_TRO_XEM, ghi: KHONG_AI },
  { mau: '/api/admin/cms/thay-doi', nhom: 'Nhật ký', viec: 'Lịch sử thay đổi (trước/sau)', doc: TAT_CA, ghi: KHONG_AI },
]

function khopMau(mau: string, path: string): boolean {
  const m = mau.split('/').filter(Boolean)
  const p = path.split('/').filter(Boolean)
  for (let i = 0; i < m.length; i++) {
    if (m[i] === '**' && i === m.length - 1) return true
    if (i >= p.length) return false
    if (m[i] !== '*' && m[i] !== p[i]) return false
  }
  return m.length === p.length
}

/** Quy tắc khớp đầu tiên, hoặc null. `/api/admin/cms/thay-doi` đặt trước `cms/**` nhờ tìm cụ thể trước. */
export function timQuyTacQuyen(path: string): QuyTacQuyen | null {
  const p = (path.split('?')[0] ?? '').replace(/\/+$/, '') || '/'
  // Ưu tiên mẫu không có wildcard khớp chính xác (vd. cms/thay-doi, cms/tong-quan trước cms/**).
  const chinhXac = QUY_TAC_QUYEN.find(r => !r.mau.includes('*') && r.mau === p)
  if (chinhXac) return chinhXac
  return QUY_TAC_QUYEN.find(r => khopMau(r.mau, p)) ?? null
}

export function laPhuongThucDoc(method: string): boolean {
  const m = method.toUpperCase()
  return m === 'GET' || m === 'HEAD'
}

export function kiemQuyenAdmin(path: string, method: string, vt: VaiTroQuyen): boolean {
  if (vt === 'sieu_quan_tri') return true
  const r = timQuyTacQuyen(path)
  if (!r) return false
  return (laPhuongThucDoc(method) ? r.doc : r.ghi).includes(vt)
}

/** Vai trò THẤY một trang admin (menu): đọc được API chính của trang. */
export function vaiTroDocDuoc(apiPath: string): VaiTroQuyen[] {
  return DS_VAI_TRO_QUYEN.filter(v => kiemQuyenAdmin(apiPath, 'GET', v))
}
