/**
 * DANH MỤC DANH HIỆU cho ô chọn phần thưởng giftcode (loại DH).
 *
 * DỮ LIỆU: shared/data/danh-muc-danh-hieu.json — SINH TỰ ĐỘNG, ĐỪNG SỬA TAY.
 *   Nguồn gốc : data.DanhHieu.getName() (src/data/DanhHieu.java), repo game HTBB.
 *   Công cụ   : tools/luu_tru_sinh_du_lieu/java/SinhDanhMucDanhHieu.java trong repo game HTBB (chạy bằng Java trên mã nguồn đã
 *               biên dịch); công cụ GHI THẲNG tệp JSON trên vào HTBB-Web (../HTBB-Web hoặc
 *               ../web trong monorepo). test/KiemDanhMucDanhHieu.java bên repo game đối chiếu lại tệp đó
 *               với mảng Java — quên sinh lại là bộ kiểm đỏ, không âm thầm lệch.
 *
 * Tên ghi cứng trong mã nguồn Java, KHÔNG có trong CSDL nào. Phải sinh bằng Java
 * chứ không đọc .java bằng regex (nhiều tên là hằng số data.Text.*, hoặc nằm trong
 * switch có case-fallthrough).
 */
import raw from '../../shared/data/danh-muc-danh-hieu.json'

// `ngauNhien` = true CHỈ cho đúng các id trùng mảng availableTitles ghi cứng trong
// src/data/DatabaseGiftCode.java (nhánh DH, id=0 -> chọn ngẫu nhiên MỘT trong số
// này). ngauNhien=false thì PHẢI ghi id thật (DH:<id>:...).

export interface DanhHieuGiftcode {
  ten: string
  ngauNhien: boolean
}

const DANH_MUC_DANH_HIEU: Record<number, DanhHieuGiftcode> = raw.danhHieu as unknown as Record<number, DanhHieuGiftcode>

/** id danh hiệu => tên + có ra được bằng "ngẫu nhiên" (id 0) không. 48 danh hiệu. */
export function danhMucDanhHieu(): Record<number, DanhHieuGiftcode> {
  return DANH_MUC_DANH_HIEU
}
