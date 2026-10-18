/**
 * Bật/tắt server game (htbb-game) từ xa — hằng số và logic thuần, không đụng CSDL.
 *
 * VÌ SAO KHÔNG GỌI LỆNH HỆ THỐNG: server chạy bằng systemd (Restart=always). Khi đang
 * SỐNG thì có sẵn cơ chế tự khởi động lại qua cờ trong bảng `5h_systems` (server tự poll
 * mỗi 5 giây, xem Database.baotriCMD()). Nhưng khi server đã TẮT HẲN thì không còn gì bên
 * trong Java để poll DB nữa — web KHÔNG tự bật/tắt được, nó chỉ GHI "trạng thái mong
 * muốn" vào bảng `dieu_khien_server`; một tiến trình canh gác chạy NGOÀI JVM game trên VPS
 * (vps/dieu-khien-server.sh, cài qua cron của ROOT vì htbb-game.service chạy bằng user
 * "htbb" không đủ quyền systemctl) đọc bảng đó mỗi phút rồi tự gọi systemctl — kể cả lúc
 * server đang tắt hẳn. Web không bao giờ tự chạy lệnh hệ thống: tiến trình web không có
 * (và không được có) quyền systemctl.
 *
 * Vì vậy CẦN CÀI WATCHER MỘT LẦN trên VPS; nếu chưa cài, các nút vẫn ghi được vào DB
 * nhưng không có gì xảy ra — "nhịp tim" (lan_kiem_cuoi) cho biết watcher còn sống không.
 *
 * AN TOÀN: Tắt/Bật server làm VĂNG TẤT CẢ người chơi đang online, nên Tắt bắt gõ đúng cụm
 * xác nhận (như trang Reset toàn server) và có hộp thoại xác nhận.
 */
import { suKienDocMoc } from './suKien'

export const DIEU_KHIEN_XAC_NHAN_TAT = 'TAT SERVER'

/** Watcher coi là "còn sống" nếu báo cáo trong khoảng này — cron chạy mỗi 60s. */
export const DIEU_KHIEN_NGUONG_IM_LANG_GIAY = 180

/** Đọc ô giờ hẹn bật: cùng một luật đọc mốc với lịch hẹn của trang Sự kiện. */
export const dieuKhienDocMoc = suKienDocMoc

export interface DieuKhienDong {
  muon_chay: number
  gio_hen: string | null
  da_ap_dung: number
  trang_thai_thuc_te: string | null
  lan_kiem_cuoi: string | null
  yeu_cau_boi: string | null
  yeu_cau_luc: string | null
  giay_tu_lan_kiem: number | null
  dang_hen_gio: number | null
}

/** Watcher còn sống: có nhịp tim và nhịp tim gần đây (≤ ngưỡng). */
export function dieuKhienWatcherSong(tt: DieuKhienDong | null): boolean {
  return !!tt && tt.lan_kiem_cuoi !== null
    && Math.trunc(Number(tt.giay_tu_lan_kiem ?? 0)) <= DIEU_KHIEN_NGUONG_IM_LANG_GIAY
}

/** Đang có yêu cầu chờ watcher xử lý. */
export function dieuKhienDangCho(tt: DieuKhienDong | null): boolean {
  return !!tt && Number(tt.da_ap_dung) === 0
}
