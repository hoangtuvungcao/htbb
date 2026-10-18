/**
 * BẢNG QUY ĐỔI CẤP ĐỘ -> ĐIỂM KINH NGHIỆM.
 *
 * DỮ LIỆU: shared/data/bang-cap-do.json — SINH TỰ ĐỘNG, ĐỪNG SỬA TAY.
 *   Nguồn : real.LevelDetail.expMain / expRequire (src/real/LevelDetail.java, repo game HTBB).
 *   Công cụ: tools/SinhBangCapDo.java trong repo game, GHI THẲNG tệp JSON trên vào HTBB-Web.
 *   test/KiemBangCapDo.java bên repo game đối chiếu từng số với mảng Java (lệch một số là
 *   lệch cấp của người chơi) và ghim CAPDO_TOI_DA với data.LenhWeb.CAP_TOI_DA.
 *
 * Web KHÔNG tự tính exp để ghi tob_char (xem server/utils/lenhNhanVat.ts) — server game
 * tính bằng chính real.LevelDetail. Bảng này chỉ còn dùng để giới hạn cấp (CAPDO_TOI_DA)
 * và để đối chiếu khi cần. MỌI chỉ số khác của nhân vật (sức mạnh/nhanh nhẹn/HP/điểm kỹ
 * năng...) do máy chủ game TỰ TÍNH LẠI từ cấp lúc chọn nhân vật
 * (src/real/cmd/SelectCharHandler.java:206-215, rồi p.calculateAttrib()) — web KHÔNG được
 * và KHÔNG cần đụng vào cột nào khác.
 *
 * Mọi giá trị đều < Number.MAX_SAFE_INTEGER (công cụ sinh từ chối nếu vượt) nên dùng number.
 */
import raw from '../../shared/data/bang-cap-do.json'

/** Cấp độ cao nhất trang quản trị cho phép đặt (= expMain.length - 1 = 90). */
export const CAPDO_TOI_DA: number = raw.capDoToiDa

/**
 * Điểm kinh nghiệm đúng tại LÚC VỪA LÊN cấp độ này (0% vào cấp tiếp theo).
 * Chỉ số mảng = cấp độ - 1, đúng như real.LevelDetail.expMain bên Java.
 */
const EXP_MAIN: readonly number[] = Object.freeze([...raw.expMain])

/**
 * Exp cần để đi hết một cấp (real.LevelDetail.expRequire).
 * Chỉ số mảng = cấp độ (KHÔNG phải cấp độ - 1) — trùng với cách Java đọc expRequire[lv].
 */
const EXP_REQUIRE: readonly number[] = Object.freeze([...raw.expRequire])

export function capDoRaExp(): readonly number[] {
  return EXP_MAIN
}

export function capDoRaExpRequire(): readonly number[] {
  return EXP_REQUIRE
}
