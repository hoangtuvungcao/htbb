/**
 * BẢNG QUY ĐỔI CẤP ĐỘ -> ĐIỂM KINH NGHIỆM.
 *
 * NGUỒN LÚC CHẠY: bảng data_cap_do_lau_dai (cột exp_cu = real.LevelDetail.expRequire, bảng EXP cũ cấp 1..90) trong
 *   CSDL game, đọc qua server/utils/duLieuSql.ts (cache 5 phút) và biến đổi bằng bangCapDoTuSql
 *   (shared/utils/duLieuSqlThuan.ts).
 * DỰ PHÒNG LÚC BUILD: shared/data/bang-cap-do.json — SINH TỰ ĐỘNG (tools/SinhBangCapDo.java), ĐỪNG SỬA TAY. Dùng khi
 *   CSDL/bảng không đọc được. tests/du-lieu-sql.test.ts kiểm SQL == JSON; test/KiemBangCapDo.java bên server đối
 *   chiếu JSON với mảng Java và ghim CAPDO_TOI_DA với data.LenhWeb.CAP_TOI_DA.
 *
 * Web KHÔNG tự tính exp để ghi tob_char (xem server/utils/lenhNhanVat.ts) — server game
 * tính bằng chính real.LevelDetail. Bảng này chỉ còn dùng để giới hạn cấp (capDoToiDa())
 * và để đối chiếu khi cần. MỌI chỉ số khác của nhân vật (sức mạnh/nhanh nhẹn/HP/điểm kỹ
 * năng...) do máy chủ game TỰ TÍNH LẠI từ cấp lúc chọn nhân vật
 * (src/real/cmd/SelectCharHandler.java:206-215, rồi p.calculateAttrib()) — web KHÔNG được
 * và KHÔNG cần đụng vào cột nào khác.
 *
 * Mọi giá trị đều < Number.MAX_SAFE_INTEGER (bangCapDoTuSql từ chối nếu vượt) nên dùng number.
 */
import raw from '../../shared/data/bang-cap-do.json'
import { bangCapDoTuSql, type BangCapDoWeb } from '../../shared/utils/duLieuSqlThuan'
import { docBangGame } from './duLieuSql'

/** Bản dự phòng (JSON lúc build): cấp độ cao nhất trang quản trị cho phép đặt (= expMain.length - 1 = 90). */
export const CAPDO_TOI_DA: number = raw.capDoToiDa

const JSON_CAP_DO: BangCapDoWeb = {
  capDoToiDa: CAPDO_TOI_DA,
  expMain: [...raw.expMain],
  expRequire: [...raw.expRequire],
}

/** Bảng đang dùng: SQL nếu đọc được, không thì JSON. */
export async function bangCapDo(): Promise<BangCapDoWeb> {
  return bangCapDoTuSql(await docBangGame('data_cap_do_lau_dai')) ?? JSON_CAP_DO
}

/** Cấp độ cao nhất trang quản trị cho phép đặt. */
export async function capDoToiDa(): Promise<number> {
  return (await bangCapDo()).capDoToiDa
}

/**
 * Điểm kinh nghiệm đúng tại LÚC VỪA LÊN cấp độ (0% vào cấp tiếp theo).
 * Chỉ số mảng = cấp độ - 1, đúng như real.LevelDetail.expMain bên Java.
 */
export async function capDoRaExp(): Promise<readonly number[]> {
  return (await bangCapDo()).expMain
}

/**
 * Exp cần để đi hết một cấp (real.LevelDetail.expRequire).
 * Chỉ số mảng = cấp độ (KHÔNG phải cấp độ - 1) — trùng với cách Java đọc expRequire[lv].
 */
export async function capDoRaExpRequire(): Promise<readonly number[]> {
  return (await bangCapDo()).expRequire
}
