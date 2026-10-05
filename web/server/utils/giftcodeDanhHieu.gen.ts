/**
 * DỮ LIỆU SINH TỰ ĐỘNG — ĐỪNG SỬA TAY.
 *
 *   Nguồn gốc : data.DanhHieu.getName() (src/data/DanhHieu.java), repo game KPAH.
 *   Công cụ   : tools/SinhDanhMucDanhHieu.java trong repo game KPAH (chạy bằng Java trên mã
 *               nguồn đã biên dịch). Dữ liệu ở đây được chép từ kết quả của công cụ đó.
 *
 * Tên ghi cứng trong mã nguồn Java, KHÔNG có trong CSDL nào. Phải sinh bằng Java
 * chứ không đọc .java bằng regex (nhiều tên là hằng số data.Text.*, hoặc nằm trong
 * switch có case-fallthrough). Muốn cập nhật thì chạy lại công cụ Java rồi thay
 * nguyên khối dữ liệu bên dưới — không chỉnh tay từng dòng.
 */

// `ngauNhien` = true CHỈ cho đúng các id trùng mảng availableTitles ghi cứng trong
// src/data/DatabaseGiftCode.java (nhánh DH, id=0 -> chọn ngẫu nhiên MỘT trong số
// này). ngauNhien=false thì PHẢI ghi id thật (DH:<id>:...).

export interface DanhHieuGiftcode {
  ten: string
  ngauNhien: boolean
}

const DANH_MUC_DANH_HIEU: Record<number, DanhHieuGiftcode> = {
  8767: { ten: "Anh hùng", ngauNhien: true },
  8768: { ten: "Bá chủ", ngauNhien: true },
  8769: { ten: "Chí tôn", ngauNhien: true },
  8770: { ten: "Đại gia", ngauNhien: true },
  8771: { ten: "Đệ nhất", ngauNhien: true },
  8772: { ten: "Duy ngã độc tôn", ngauNhien: true },
  8773: { ten: "Hiệp khách", ngauNhien: true },
  8774: { ten: "Hảo hán", ngauNhien: true },
  8775: { ten: "Huyền thoại", ngauNhien: true },
  8776: { ten: "Tông sư", ngauNhien: true },
  8777: { ten: "Uy phong", ngauNhien: true },
  8778: { ten: "Vô địch", ngauNhien: true },
  8779: { ten: "Võ lâm chí tôn", ngauNhien: true },
  8785: { ten: "Nhất dương tiễn", ngauNhien: true },
  8786: { ten: "Tuyệt thế vô song", ngauNhien: true },
  8787: { ten: "ACE", ngauNhien: true },
  8788: { ten: "Fan cứng", ngauNhien: true },
  8900: { ten: "Angle Of Death", ngauNhien: true },
  8901: { ten: "Bất Chấp Cuộc Chơi", ngauNhien: true },
  8902: { ten: "Chiến Thần Bất Diệt", ngauNhien: true },
  8903: { ten: "Long Tiễn Chấn Thiên", ngauNhien: true },
  8904: { ten: "Nghịch Thiên Nhi Hành", ngauNhien: true },
  8905: { ten: "Khai Thiên Thần Kiếm", ngauNhien: true },
  8906: { ten: "Thiên Hạ Chí Tôn", ngauNhien: true },
  8907: { ten: "Vô Địch Thiên Hạ", ngauNhien: true },
  8908: { ten: "Thần Binh Bất Bại", ngauNhien: true },
  8909: { ten: "Độc Bá Nhất Phương", ngauNhien: true },
  8910: { ten: "Tung Hoành Thiên Hạ", ngauNhien: true },
  8911: { ten: "Tuyệt Đỉnh Cao Thủ", ngauNhien: true },
  8924: { ten: "Độc Nhất Vô Nhị", ngauNhien: true },
  8925: { ten: "Nhất Tiễn Phá Thiên", ngauNhien: true },
  8935: { ten: "Vinh Danh Bang Hội", ngauNhien: true },
  8956: { ten: "Chân Mên Thiên Tử", ngauNhien: true },
  8957: { ten: "Võ Lâm Chí Tôn 2", ngauNhien: true },
  8983: { ten: "Chiến Thần Bất Diệt 2", ngauNhien: false },
  8984: { ten: "Cửu Huyền Thất Tổ", ngauNhien: false },
  8985: { ten: "Hủy Thiên Diệt Địa", ngauNhien: false },
  8986: { ten: "Kinh Tài Tuyệt Diễm", ngauNhien: false },
  8987: { ten: "Nữ Thần Tuyệt Sắc", ngauNhien: false },
  8988: { ten: "Phong Vân Tùy Bút", ngauNhien: false },
  8989: { ten: "Quy Ân Giang Hồ", ngauNhien: false },
  8990: { ten: "Thiên Hạ Đệ Nhất", ngauNhien: false },
  8991: { ten: "Thiên Hạ Vô Song", ngauNhien: false },
  8992: { ten: "Tung Hoành Thiên Hạ", ngauNhien: false },
  8993: { ten: "Tuyệt Thế Vô Song", ngauNhien: false },
  8994: { ten: "Vinh Hoa Phú Qúy", ngauNhien: false },
  8995: { ten: "Quân Lâm Thiên Hạ", ngauNhien: false },
  8996: { ten: "Tuyệt Thế Giai Nhân", ngauNhien: false },
}

/** id danh hiệu => tên + có ra được bằng "ngẫu nhiên" (id 0) không. 48 danh hiệu. */
export function danhMucDanhHieu(): Record<number, DanhHieuGiftcode> {
  return DANH_MUC_DANH_HIEU
}
