/**
 * DANH MỤC THÚ NUÔI (PET/PETV) cho ô chọn phần thưởng giftcode.
 *
 * DỮ LIỆU: shared/data/danh-muc-thu.json — SINH TỰ ĐỘNG, ĐỪNG SỬA TAY.
 *   Nguồn gốc : mảng real.Map.petTemplate (src/real/Map.java, loadPetTemplate), repo game HTBB.
 *   Công cụ   : tools/luu_tru_sinh_du_lieu/java/SinhDanhMucThu.java trong repo game HTBB (chạy bằng Java trên mã nguồn đã
 *               biên dịch); công cụ GHI THẲNG tệp JSON trên vào HTBB-Web (../HTBB-Web hoặc
 *               ../web trong monorepo). test/KiemDanhMucThu.java bên repo game đối chiếu lại tệp đó
 *               với mảng Java — quên sinh lại là bộ kiểm đỏ, không âm thầm lệch.
 *
 * Tên ghi cứng trong mã nguồn Java, KHÔNG có trong CSDL nào. Phải sinh bằng Java
 * chứ không đọc .java bằng regex (nhiều tên là hằng số data.Text.*, hoặc nằm trong
 * switch có case-fallthrough).
 */
import raw from '../../shared/data/danh-muc-thu.json'

// KHOÁ LÀ CHỈ SỐ MẢNG, KHÔNG PHẢI ID VẬT PHẨM — Char.createPet() và
// createPetVinhVien() đọc thẳng Map.petTemplate[idpet] không kiểm biên.

const DANH_MUC_THU: Record<number, string> = raw.thu as unknown as Record<number, string>

/** chỉ số 0..30 => tên thú (KHÔNG phải id vật phẩm). */
export function danhMucThu(): Record<number, string> {
  return DANH_MUC_THU
}
