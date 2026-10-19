/**
 * Danh mục cấu hình trung tâm phía server web: đọc cau_hinh_danh_muc + cau_hinh_nhom từ CSDL game (cùng hai bảng
 * máy chủ game nạp lúc khởi động), cache qua server/utils/duLieuSql.ts. Bảng thiếu / chưa chạy migration (cột
 * thu_tu) -> giữ bản sinh shared/utils/cauHinhTrungTam.gen.ts (một dòng cảnh báo mỗi chu kỳ cache).
 *
 * Gọi `await napDanhMucTTServer()` trước khi dùng khoaTT / danhMucTT / kiemGiaTriTT ở server.
 */
import type { DongBang } from '../../shared/utils/duLieuSqlThuan'
import { docBangGame } from './duLieuSql'

let truoc: { k: DongBang, n: DongBang, kq: 'sql' | 'gen' } | null = null
let daBao = 0

export async function napDanhMucTTServer(): Promise<'sql' | 'gen'> {
  const [k, n] = await Promise.all([docBangGame('cau_hinh_danh_muc'), docBangGame('cau_hinh_nhom')])
  if (truoc && truoc.k === k && truoc.n === n) return truoc.kq
  const dm = danhMucTuSqlTT(k, n)
  if (!dm && k && n && Date.now() - daBao > 5 * 60_000) {
    daBao = Date.now()
    console.warn('[cau-hinh-tt] cau_hinh_danh_muc / cau_hinh_nhom không đúng định dạng (thiếu cột thu_tu? chạy migration P1) — dùng danh mục sinh lúc build.')
  }
  datDanhMucTT(dm)
  truoc = { k, n, kq: dm ? 'sql' : 'gen' }
  return truoc.kq
}
