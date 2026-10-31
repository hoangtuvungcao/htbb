/**
 * Chỉ số VPS trực tiếp (trang /admin/he-thong, thẻ VPS; trang hỏi lại mỗi 2 giây — không dùng SSE vì token quản trị
 * đi trong header Authorization mà EventSource không gửi được).
 *
 * Query: ?sau=<ms epoch> — chỉ trả các điểm của cửa sổ 30 phút MỚI HƠN mốc này (lần đầu không có -> trả cả 30 phút).
 * Trả: { dangLayMau, chuKyMs, batDau, moiNhat: MauVps | null, diem: DiemVps[] }.
 * Mọi số đọc từ bộ lấy mẫu trong bộ nhớ (server/utils/vpsLayMau.ts): request KHÔNG chạy lệnh / đọc tệp nào.
 */
import { CHU_KY_VPS_MS, vpsBatDauLuc, vpsDangLayMau, vpsMoiNhat, vpsVong } from '../../../utils/vpsLayMau'

export default defineAdminHandler((event) => {
  const sau = Number(getQuery(event).sau ?? 0)
  const moc = Number.isFinite(sau) && sau > 0 ? sau : 0
  return {
    dangLayMau: vpsDangLayMau(),
    chuKyMs: CHU_KY_VPS_MS,
    batDau: vpsBatDauLuc(),
    moiNhat: vpsMoiNhat(),
    diem: vpsVong().filter(d => d.t > moc),
  }
})
