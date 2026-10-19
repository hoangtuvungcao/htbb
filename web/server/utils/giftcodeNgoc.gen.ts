/**
 * DANH MỤC NGỌC cho ô chọn phần thưởng giftcode (loại GEM).
 *
 * DỮ LIỆU: shared/data/danh-muc-ngoc.json — SINH TỰ ĐỘNG, ĐỪNG SỬA TAY.
 *   Nguồn gốc : mảng real.Map.gemTemplate (src/real/MapNapTaiNguyen.java), repo game HTBB.
 *   Công cụ   : tools/luu_tru_sinh_du_lieu/java/SinhDanhMucNgoc.java trong repo game HTBB (chạy bằng Java trên mã nguồn đã
 *               biên dịch); công cụ GHI THẲNG tệp JSON trên vào HTBB-Web (../HTBB-Web hoặc
 *               ../web trong monorepo). test/KiemDanhMucNgoc.java bên repo game đối chiếu lại tệp đó
 *               với mảng Java — quên sinh lại là bộ kiểm đỏ, không âm thầm lệch.
 *
 * Tên ghi cứng trong mã nguồn Java, KHÔNG có trong CSDL nào. Phải sinh bằng Java
 * chứ không đọc .java bằng regex (nhiều tên là hằng số data.Text.*, hoặc nằm trong
 * switch có case-fallthrough).
 */
import raw from '../../shared/data/danh-muc-ngoc.json'

export interface NgocGiftcode {
  /** nhãn hiển thị (xuống dòng trong tên gốc đã đổi thành " · ") */
  ten: string
  /** có bán trong cửa hàng NPC không (false = chỉ phát được bằng giftcode) */
  ban: boolean
  /** 'NLSC1'..'NLSC6' / 'NLCC1'..'NLCC6' nếu là ngọc nguyên liệu, còn lại '' */
  nhom: string
}

const DANH_MUC_NGOC: Record<number, NgocGiftcode> = raw.ngoc as unknown as Record<number, NgocGiftcode>

/** id ngọc (chỉ số Map.gemTemplate) => thông tin. 270 viên. */
export function danhMucNgoc(): Record<number, NgocGiftcode> {
  return DANH_MUC_NGOC
}
