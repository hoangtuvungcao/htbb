/**
 * DỮ LIỆU SINH TỰ ĐỘNG — ĐỪNG SỬA TAY.
 *
 *   Nguồn gốc : mảng real.Map.petTemplate (src/real/Map.java, loadPetTemplate), repo game HTBB.
 *   Công cụ   : tools/SinhDanhMucThu.java trong repo game HTBB (chạy bằng Java trên mã
 *               nguồn đã biên dịch). Dữ liệu ở đây được chép từ kết quả của công cụ đó.
 *
 * Tên ghi cứng trong mã nguồn Java, KHÔNG có trong CSDL nào. Phải sinh bằng Java
 * chứ không đọc .java bằng regex (nhiều tên là hằng số data.Text.*, hoặc nằm trong
 * switch có case-fallthrough). Muốn cập nhật thì chạy lại công cụ Java rồi thay
 * nguyên khối dữ liệu bên dưới — không chỉnh tay từng dòng.
 */

// KHOÁ LÀ CHỈ SỐ MẢNG, KHÔNG PHẢI ID VẬT PHẨM — Char.createPet() và
// createPetVinhVien() đọc thẳng Map.petTemplate[idpet] không kiểm biên.

const DANH_MUC_THU: Record<number, string> = {
  0: "Lồng đèn xếp",
  1: "Thanh tước",
  2: "Cánh cam",
  3: "Dơi quỷ",
  4: "Tim bay",
  5: "Lồng đèn hoa sen",
  6: "Lồng đèn ngôi sao",
  7: "Pet Hoa tuyết",
  8: "Pet Giáp Ngọ",
  9: "Pet Armadillo băng",
  10: "Pet Armadillo sét",
  11: "Pet Armadillo độc",
  12: "Lồng đèn cá chép",
  13: "Chim cánh cụt",
  14: "Pet ất mùi",
  15: "Ngôi sao băng",
  16: "Phượng hoàng lửa",
  17: "Phượng hoàng băng",
  18: "Lồng đèn",
  19: "Lồng đèn Ngôi sao",
  20: "Lồng đèn Trăng khuyết",
  21: "Lồng đèn Rồng xanh",
  22: "Khỉ vàng",
  23: "Cú",
  24: "Rồng",
  25: "Thỏ bay",
  26: "Dơi",
  27: "Đại bàng",
  28: "Lồng đèn hoa sen",
  29: "Bạch Thử",
  30: "Capybara",
}

/** chỉ số 0..30 => tên thú (KHÔNG phải id vật phẩm). */
export function danhMucThu(): Record<number, string> {
  return DANH_MUC_THU
}
