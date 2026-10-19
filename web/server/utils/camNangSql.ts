/**
 * Cẩm nang đọc từ CSDL game lúc chạy: các mục có bảng SQL (bảng cấp, lịch 52 tuần, nâng cấp, kinh tế, cửa hàng,
 * Gym, chế tạo, nhánh Eevee, trang trí, vật phẩm sự kiện, lịch boss) lấy từ htbb_db; mục khác giữ JSON lúc build.
 * Biến đổi: shared/utils/duLieuSqlThuan.ts (test: tests/du-lieu-sql.test.ts). Cache bảng: server/utils/duLieuSql.ts.
 */
import { BANG_CAM_NANG, camNangDeTuSql, ghepCamNang } from '../../shared/utils/duLieuSqlThuan'
import { docNhieuBangGame } from './duLieuSql'

/** Phần đè (chỉ mục đọc được từ SQL) + danh sách mục đã lấy từ SQL. */
export async function camNangDeSql(): Promise<ReturnType<typeof camNangDeTuSql>> {
  return camNangDeTuSql(await docNhieuBangGame(BANG_CAM_NANG), CAM_NANG)
}

/** cam-nang.json đầy đủ, mục có SQL đã thay bằng dữ liệu CSDL (cho API phía server). */
export async function camNangServer(): Promise<CamNangData> {
  return ghepCamNang(CAM_NANG, (await camNangDeSql()).camNang)
}
