/**
 * Trình duyệt hỏi: người đang xem có thấy bản DEMO nạp tiền / Thành viên không (HTBB_NAP_DEMO=1).
 * Đọc token người chơi nếu có (không bắt buộc đăng nhập). Không được xem (hoặc không demo) -> { demo: false }:
 * người ngoài không biết là đang có demo. Không lộ bí mật nào (số TK là số minh hoạ có chữ x).
 */
import { DEMO_BANNER } from '../utils/napDemoThuan'

export default defineEventHandler((event) => {
  const n = useHtbbConfig().nap
  if (!n.demo || !napDemoXem(napDemoTenNguoiXem(event))) return { demo: false }
  return {
    demo: true,
    chiAdmin: n.demoChiAdmin,
    banner: DEMO_BANNER,
    nganHang: napDemoNganHang(),
  }
})
