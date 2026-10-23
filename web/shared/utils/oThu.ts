/**
 * Ô thú (thú cưỡi + Pokémon đi theo dùng CHUNG một sức chứa) cho trang cẩm nang Thú cưỡi & pet.
 * Nguồn thật: cau_hinh thu.o_co_ban / thu.o_toi_da / thu.tha_xu_ngay (com.htbb.pokemon.OThu, real.QuanLyThuGame) và
 * dòng nap_cua_hang 've_mo_o_thu' (khối `-- >>> them_quan_ly_thu.sql` của sql/htbb.sql).
 * tests/hien-thi-nguoi-choi.test.ts đối chiếu từng số với danh mục cấu hình + SQL: đổi mặc định ở đó thì sửa ở đây.
 */
export const O_THU = Object.freeze({
  /** Ô có sẵn mỗi người chơi. */
  coBan: 20,
  /** Trần ô mở bằng vé. */
  toiDa: 60,
  /** Mỗi Vé mở ô thú thêm chừng này ô. */
  oMoiVe: 1,
  /** Giá một vé ở Cửa hàng lượng. */
  giaVeLuong: 10,
  /** Trần xu nhận được từ thả thú mỗi ngày. */
  xuThaNgay: 2000,
})
